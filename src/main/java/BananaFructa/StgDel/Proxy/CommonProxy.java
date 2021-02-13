package BananaFructa.StgDel.Proxy;

import BananaFructa.StgDel.Commands.StageKeyCreationCommand;
import BananaFructa.StgDel.Commands.TeamInteractionCommand;
import BananaFructa.StgDel.Network.SPacketExportRegistryNames;
import BananaFructa.StgDel.Network.SPacketStageChangeState;
import BananaFructa.StgDel.Network.StgDelPacketHandler;
import BananaFructa.StgDel.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.util.*;

public class CommonProxy {

    public HashMap<Integer,String> StageNameDictionary = new HashMap<>();
    public HashMap<Integer, List<String>> StageRegistryNames = new HashMap<>();
    public List<TeamInvite> ActiveInvites = new ArrayList<>();

    private List<TeamInvite> InvitesToDelete = new ArrayList<>();

    public void InitServer(FMLServerStartingEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        event.registerServerCommand(new TeamInteractionCommand());
        event.registerServerCommand(new StageKeyCreationCommand());

        StgDelPacketHandler.RegisterPackets();

        try {

            new File("config/StgDel").mkdirs();

            File f = new File("config/StgDel/StageNames.stg");
            if (!f.exists()) f.createNewFile();

            Scanner scanner = new Scanner(f);

            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tempSplit = line.split("#");
                StageNameDictionary.put(Integer.parseInt(tempSplit[1]),tempSplit[0]);
            }

            for (int id : StageNameDictionary.keySet()) {
                File sd = new File("config/StgDel/"+StageNameDictionary.get(id)+".stg");
                if (!sd.exists()) sd.createNewFile();
                Scanner sdscanner = new Scanner(sd);
                StageRegistryNames.put(id,new ArrayList<>());
                while(sdscanner.hasNextLine()) {
                    String registryName = sdscanner.nextLine().replace("<","").replace(">","");
                    StageRegistryNames.get(id).add(registryName);
                }
                sdscanner.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean AddTeamInvite(TeamInvite ti) {
        synchronized (this) {
            for (TeamInvite t : ActiveInvites) {
                if (t.reciever.equals(ti.reciever) && t.teamId == ti.teamId) return false;
                if (t.reciever.equals(ti.reciever)) {
                    ActiveInvites.remove(t);
                    break;
                }
            }
            ActiveInvites.add(ti);
            return true;
        }
    }

    public TeamInvite GetInvite(UUID invitedPlayer) {
        synchronized (this) {
            for (TeamInvite ai : ActiveInvites) {
                if (ai.reciever.equals(invitedPlayer)) {
                    return ai;
                }
            }
            return null;
        }
    }

    public void VoidInvite(TeamInvite ti) {
        ActiveInvites.remove(ti);
    }

    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent event) {
        for (TeamInvite t : ActiveInvites) {
            if (t.expirationDate < System.currentTimeMillis()) InvitesToDelete.add(t);
        }
        ActiveInvites.removeAll(InvitesToDelete);
        InvitesToDelete.clear();
    }

    @SubscribeEvent
    public void onEntityJoined(PlayerEvent.PlayerLoggedInEvent event) {
        StageStateData stageStateData = StageStateData.get(((EntityPlayerMP) event.player).getServerWorld());
        stageStateData.AddPlayer(event.player.getUniqueID());
        stageStateData.AddUsernameUUID(event.player.getName(),event.player.getUniqueID());

        // Exports the stage data to the client
        StgDelPacketHandler.INSTNACE.sendTo(new SPacketExportRegistryNames(StageNameDictionary, StageRegistryNames), (EntityPlayerMP) event.player);

        // Loads all the unlocked stages to the client
        UUID playerId = event.player.getUniqueID();

        for (int i : stageStateData.PlayerData.get(playerId)) {
            StgDelPacketHandler.INSTNACE.sendTo(new SPacketStageChangeState(i, true), (EntityPlayerMP) event.player);
        }

    }

    @SubscribeEvent
    public void onPlayerInteraction(PlayerInteractEvent.RightClickItem event) {
        if (!event.getWorld().isRemote) {
            Integer stage = GetStageUnlock(event.getItemStack());
            if (stage != null) {
                ChangeStageStateFor((EntityPlayerMP) event.getEntityPlayer(), stage, true);
            }
        }
    }

    public void ChangeStageStateFor(EntityPlayerMP player, int ID, boolean state) {
        StageStateData stageStateData = StageStateData.get(player.getServerWorld());
        UUID playerID = player.getUniqueID();
        for (Team t : stageStateData.Teams) {
            if (t.IsMemeber(playerID)) {
                t.ChangeForAll(player,ID,state);
                return;
            }
        }
        if (stageStateData.SetIdState(playerID,ID,state)) {
            StgDelPacketHandler.INSTNACE.sendTo(new SPacketStageChangeState(ID, state), player);
            if (state) {
                player.sendMessage(new TextComponentString("\u00a7a" + StgDel.proxy.StageNameDictionary.get(ID) + " unlocked !"));
            }
        }
    }

    public Integer GetStageUnlock(ItemStack item) {
        if (item.getItem().getClass() == StageKey.class) {
            if (item.getTagCompound() != null) {
                if (item.getTagCompound().hasKey("stage")) {
                    return item.getTagCompound().getInteger("stage");
                }
            }
        }
        return null;
    }
}
