package BananaFructa.StgDel.Proxy;

import BananaFructa.StgDel.Commands.StageKeyCreationCommand;
import BananaFructa.StgDel.Commands.TeamInteractionCommand;
import BananaFructa.StgDel.Network.SPacketExportRegistryNames;
import BananaFructa.StgDel.Network.SPacketStageChangeState;
import BananaFructa.StgDel.Network.SPacketSyncBQ;
import BananaFructa.StgDel.Network.StgDelPacketHandler;
import BananaFructa.StgDel.*;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTurret;
import io.netty.handler.codec.http2.Http2InboundFrameLogger;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.lwjgl.Sys;

import java.io.File;
import java.util.*;

public class CommonProxy {

    public HashMap<Integer,String> StageNameDictionary = new HashMap<>();
    public HashMap<Integer, List<String>> StageRegistryNames = new HashMap<>();
    public List<TeamInvite> ActiveInvites = new ArrayList<>();

    private final List<TeamInvite> InvitesToDelete = new ArrayList<>();

    private final HashMap<UUID,List<String>> BannedUsageCache = new HashMap<>();
    private final HashMap<UUID,List<String>> AllowedUsageCache = new HashMap<>();

    public StageStateData stageStateData;

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

            stageStateData = StageStateData.get(event.getServer().getWorld(0));

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
        BannedUsageCache.put(event.player.getUniqueID(),new ArrayList<>());
        AllowedUsageCache.put(event.player.getUniqueID(),new ArrayList<>());

        stageStateData.AddPlayer(event.player.getUniqueID());
        stageStateData.AddUsernameUUID(event.player.getName(),event.player.getUniqueID());

        // Exports the stage data to the client
        StgDelPacketHandler.INSTNACE.sendTo(new SPacketExportRegistryNames(StageNameDictionary, StageRegistryNames), (EntityPlayerMP) event.player);

        // Sets the better questing sync flag
        StgDelPacketHandler.INSTNACE.sendTo(new SPacketSyncBQ(Config.syncWithBetterQuesting), (EntityPlayerMP) event.player);

        // Loads all the unlocked stages to the client
        UUID playerId = event.player.getUniqueID();

        for (int i : stageStateData.PlayerData.get(playerId)) {
            StgDelPacketHandler.INSTNACE.sendTo(new SPacketStageChangeState(i, true), (EntityPlayerMP) event.player);
        }

    }

    @SubscribeEvent
    public void onPlayerInteraction(PlayerInteractEvent.RightClickItem event) {
        if (!event.getWorld().isRemote) {
            if (!IsItemUseAllowedForPlayer((EntityPlayerMP) event.getEntityPlayer(),event.getItemStack())) {
                event.setCanceled(true);
                return;
            }
            Integer stage = GetStageUnlock(event.getItemStack());
            if (stage != null) {
                ClearBannedCache();
                ChangeStageStateFor((EntityPlayerMP) event.getEntityPlayer(), stage, true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteraction(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getWorld().isRemote) {
            if (!IsItemUseAllowedForPlayer((EntityPlayerMP) event.getEntityPlayer(),event.getItemStack())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteraction(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getWorld().isRemote) {
            if (!IsItemUseAllowedForPlayer((EntityPlayerMP) event.getEntityPlayer(),event.getItemStack())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteraction(AttackEntityEvent event) {
        if (!event.getEntityPlayer().getEntityWorld().isRemote) {
            if (!IsItemUseAllowedForPlayer((EntityPlayerMP) event.getEntityPlayer(), event.getEntityPlayer().getHeldItemMainhand())) {
                event.setCanceled(true);
            }
        }
    }

    public void ClearBannedCache() {
        for (UUID uuid : BannedUsageCache.keySet()) {
            BannedUsageCache.get(uuid).clear();
        }
    }

    public void ChangeStageStateFor(EntityPlayerMP player, int ID, boolean state) {
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
            } else {
                player.sendMessage(new TextComponentString("\u00a7c" + StgDel.proxy.StageNameDictionary.get(ID) + " locked !"));
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

    protected static String getCompleteRegistry(ItemStack is) {
        String r = is.getItem().getRegistryName().toString();
        if (is.getItem().getHasSubtypes()) {
            int meta = is.getMetadata();
            if (meta != 0) {
                r += ":" + meta;
            }
        }
        return r;
    }

    public boolean IsItemUseAllowedForPlayer (EntityPlayer player, ItemStack is) {
        try {
            if (is == null || player.capabilities.isCreativeMode) return true;
            String name = getCompleteRegistry(is);
            if (name.equals("minecraft:air")) return true;
            UUID uuid = player.getUniqueID();
            if (BannedUsageCache.get(uuid) == null || AllowedUsageCache.get(uuid) == null) return true;
            if (BannedUsageCache.get(uuid).contains(name)) return false;
            if (AllowedUsageCache.get(uuid).contains(name)) return true;
            List<Integer> UnlockedStages = stageStateData.PlayerData.get(player.getUniqueID());
            for (Integer id : StageRegistryNames.keySet()) {
                if (!UnlockedStages.contains(id)) {
                    for (String register : StageRegistryNames.get(id)) {
                        if (IsRegistryExcepted(register, name)) continue;
                        if (DoRegistriesMatch(register, name)) {
                            if (BannedUsageCache.size() > 4) BannedUsageCache.remove(0);
                            BannedUsageCache.get(player.getUniqueID()).add(name);
                            return false;
                        }
                    }
                }
            }
            if (AllowedUsageCache.get(uuid).size() > 4) AllowedUsageCache.get(uuid).remove(0);
            AllowedUsageCache.get(uuid).add(name);
            return true;
        } catch (Exception err) {
            err.printStackTrace();
            return true;
        }
    }

    public boolean IsRegistryExcepted(String a,String b) {
        if (a.startsWith("@") && b.startsWith(a.replace("@",""))) return true;
        return false;
    }

    public boolean DoRegistriesMatch(String a,String b) {
        if (a.equals(b)) return true;
        if (a.startsWith("!") && b.startsWith(a.replace("!",""))) return true;
        return false;
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote || !StgDel.immersiveEngineeringLoaded) return;
        event.getWorld().getMinecraftServer().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                TileEntity entity = event.getWorld().getTileEntity(event.getPos());
                BlockPos pos = event.getPos();

                if (entity instanceof TileEntityTurret) {
                    TileEntityTurret turretEntity = (TileEntityTurret) entity;

                    long teamId = stageStateData.GetTeamID(event.getPlayer().getUniqueID());

                    if (teamId != -1) {
                        Team t = stageStateData.GetTeam(teamId);
                        for (UUID uuid : t.Memebers) {
                            if (uuid.equals(event.getPlayer().getUniqueID())) continue;
                            String name = stageStateData.GetLocalFromUUID(uuid);
                            turretEntity.targetList.add(name);
                        }
                        turretEntity.owner = stageStateData.GetLocalFromUUID(t.Owner);
                    }

                }

            }
        });
    }

    public void trySwitchTurretOwner(World world, BlockPos possiblePos, UUID uuid) {
        TileEntity entity = world.getTileEntity(possiblePos);
        if (entity instanceof TileEntityTurret) {
            TileEntityTurret turretEntity = (TileEntityTurret) entity;
            if (turretEntity.dummy) {
                trySwitchTurretOwner(world, possiblePos.down(), uuid);
                return;
            }
            long id = stageStateData.GetTeamID(uuid);
            if (id != -1) {
                Team t = stageStateData.GetTeam(id);
                if (!t.IsMemeber(stageStateData.GetUUIDFromLocal(turretEntity.owner))) return;
            } else return;
            turretEntity.owner = stageStateData.GetLocalFromUUID(uuid);

            queue.add(new Runnable() {
                @Override
                public void run() {
                    turretEntity.owner = stageStateData.GetLocalFromUUID(stageStateData.GetTeam(id).Owner);
                }
            });

        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote || !StgDel.immersiveEngineeringLoaded) return;
        trySwitchTurretOwner(event.getWorld(),event.getPos(),event.getEntityPlayer().getUniqueID());
    }

    Queue<Runnable> queue = new PriorityQueue<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(TickEvent.ServerTickEvent event) {
        if (!queue.isEmpty()) queue.poll().run();
    }


}
