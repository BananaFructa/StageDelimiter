package BananaFructa.StgDel;

import BananaFructa.StgDel.Network.SPacketStageChangeState;
import BananaFructa.StgDel.Network.StgDelPacketHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team implements Serializable {

    public long ID;
    public UUID Owner;
    public List<UUID> Memebers = new ArrayList<>();
    public List<UUID> BannedMembers = new ArrayList<>();
    public String Name;
    public List<Integer> TeamUnlocked = new ArrayList<>();
    boolean IsInviteOnly = true;

    Team(EntityPlayerMP player,String Name,long ID) {
        this.Owner = player.getUniqueID();
        AddMemeber(player);
        this.Name = Name;
        this.ID = ID;
    }

    public boolean AddMemeber(EntityPlayerMP player) {

        SendMessageToAll(player.getServerWorld(), "\u00a7a" + player.getName() + " joined the team!");

        UUID NewMember = player.getUniqueID();

        StageStateData stageStateData = StageStateData.get(player.getServerWorld());
        boolean AlreadyExists = Memebers.stream().anyMatch(o -> o.equals(NewMember));
        if (AlreadyExists) return false;
        Memebers.add(NewMember);

        List<Integer> MemberUnlocked = stageStateData.PlayerData.get(NewMember);
        for (int i : MemberUnlocked) {
            if (!TeamUnlocked.contains(i)) {
                ChangeForAll(player,i,true);
            }
        }
        for (int i : TeamUnlocked) {
            if (!MemberUnlocked.contains(i)) {
                StgDel.proxy.ChangeStageStateFor(player,i,true);
            }
        }

        return true;
    }

    public boolean RemoveMember(UUID Member) {
        boolean MemberExists = Memebers.stream().anyMatch(o -> o.equals(Member));
        if (!MemberExists) return false;
        Memebers.remove(Member);
        return true;
    }

    public boolean BanMemeber(UUID Member) {
        if (RemoveMember(Member)) {
            BannedMembers.add(Member);
            return true;
        }
        return false;
    }

    public boolean UnBanMemeber(UUID Member) {
        if (BannedMembers.contains(Member)) {
            BannedMembers.remove(Member);
            return true;
        }
        return false;
    }

    public boolean IsPlayerBanned(UUID player) {
        return BannedMembers.contains(player);
    }

    public boolean IsMemeber(UUID player) {
        return Memebers.stream().anyMatch(o -> o.equals(player));
    }

    public void ChangeForAll(EntityPlayerMP source,int ID,boolean state) {

        if (state && !TeamUnlocked.contains(ID)) {
            TeamUnlocked.add(ID);
        } else if (!state && TeamUnlocked.contains(ID)) {
            int count = TeamUnlocked.size();
            for (int i = 0; i < count; i++) {
                if (TeamUnlocked.get(i) == ID) {
                    TeamUnlocked.remove(i);
                    return;
                }
            }
        }

        StageStateData stageStateData = StageStateData.get(source.getServerWorld());

        for (UUID playerId : Memebers) {
            if (stageStateData.SetIdState(playerId, ID, state)) {
                EntityPlayerMP player = (EntityPlayerMP) source.getServerWorld().getPlayerEntityByUUID(playerId);
                if (player != null) { // The player might not be online
                    StgDelPacketHandler.INSTNACE.sendTo(new SPacketStageChangeState(ID, state), player);
                    if (state) {
                        // Means the player unlocked a new stage
                        player.sendMessage(new TextComponentString("\u00a7aStage " + StgDel.proxy.StageNameDictionary.get(ID) + " unlocked !"));
                    }
                }
            }
        }

        stageStateData.markDirty();
    }

    public void SendMessageToAll(WorldServer world,String msg) {
        for (UUID uuid : Memebers) {
            EntityPlayerMP player = (EntityPlayerMP)world.getPlayerEntityByUUID(uuid);
            if (player != null) {
                player.sendMessage(new TextComponentString(msg));
            }
        }
    }

    public int GetMemeberCount() {
        return Memebers.size();
    }

}
