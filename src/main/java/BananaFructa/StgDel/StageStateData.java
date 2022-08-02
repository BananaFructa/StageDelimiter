package BananaFructa.StgDel;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class StageStateData extends WorldSavedData {


    public static final String DATA_NAME = StgDel.modId + "_StageStateData";

    public List<Team> Teams = new ArrayList<>();
    public HashMap<UUID,List<Integer>> PlayerData = new HashMap<>();
    public HashMap<String,UUID> StoredUsernamesToUUID = new HashMap<>();

    public HashMap<Long,Integer> stdIdToBqId = new HashMap<>();

    public StageStateData() {
        super(DATA_NAME);
    }

    public StageStateData(String s) {
        super(s);
    }

    public static StageStateData get(World world) {
        MapStorage storage = world.getMapStorage();
        StageStateData stageStateData = (StageStateData)storage.getOrLoadData(StageStateData.class,DATA_NAME);
        if (stageStateData == null) {
            stageStateData = new StageStateData();
            storage.setData(DATA_NAME,stageStateData);
        }
        return stageStateData;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        ByteArrayInputStream byteIn_PD = new ByteArrayInputStream(nbt.getByteArray("PlayerData"));
        ByteArrayInputStream byteIn_TD = new ByteArrayInputStream(nbt.getByteArray("Teams"));
        ByteArrayInputStream byteIn_UUID = new ByteArrayInputStream(nbt.getByteArray("StoredUUIDs"));
        ByteArrayInputStream byteIn_ids = new ByteArrayInputStream(nbt.getByteArray("SDBQ_IDS"));

        try {

            ObjectInputStream in_PD = new ObjectInputStream(byteIn_PD);
            PlayerData = (HashMap<UUID, List<Integer>>) in_PD.readObject();

            ObjectInputStream in_TD = new ObjectInputStream(byteIn_TD);
            Teams = (List<Team>) in_TD.readObject();

            ObjectInputStream in_UUID = new ObjectInputStream(byteIn_UUID);
            StoredUsernamesToUUID = (HashMap<String,UUID>) in_UUID.readObject();

            ObjectInputStream in_ids = new ObjectInputStream(byteIn_ids);
            stdIdToBqId = (HashMap<Long, Integer>) in_ids.readObject();

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        ByteArrayOutputStream byteOut_PD = new ByteArrayOutputStream();
        ByteArrayOutputStream byteOut_TD = new ByteArrayOutputStream();
        ByteArrayOutputStream byteOut_UUID = new ByteArrayOutputStream();
        ByteArrayOutputStream byteOut_ids = new ByteArrayOutputStream();

        try {

            ObjectOutputStream out_PD = new ObjectOutputStream(byteOut_PD);

            out_PD.writeObject(PlayerData);
            compound.setByteArray("PlayerData",byteOut_PD.toByteArray());

            ObjectOutputStream out_TD = new ObjectOutputStream(byteOut_TD);

            out_TD.writeObject(Teams);
            compound.setByteArray("Teams",byteOut_TD.toByteArray());

            ObjectOutputStream out_UUID = new ObjectOutputStream(byteOut_UUID);

            out_UUID.writeObject(StoredUsernamesToUUID);
            compound.setByteArray("StoredUUIDs", byteOut_UUID.toByteArray());

            ObjectOutputStream out_ids = new ObjectOutputStream(byteOut_ids);

            out_ids.writeObject(stdIdToBqId);
            compound.setByteArray("SDBQ_IDS", byteOut_ids.toByteArray());


        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return compound;
    }

    public Team GetTeam(long ID) {
        for (Team t : Teams) {
            if (t.ID == ID) {
                return t;
            }
        }
        return null;
    }

    public long GetTeamID(String name) {
        for (Team t : Teams) {
            if (t.Name.equals(name.toLowerCase())) {
                return t.ID;
            }
        }
        return -1;
    }

    public boolean AddPlayer(UUID player) {
        if (!PlayerData.containsKey(player)) {
            PlayerData.put(player,new ArrayList<>());
            markDirty();
            return true;
        }
        return false;
    }

    public boolean SetIdState(UUID player,int ID,boolean state) {

        if (!PlayerData.get(player).contains(ID) && state) {

            PlayerData.get(player).add(ID);
            markDirty();
            return true;

        } else if (PlayerData.get(player).contains(ID) && !state) {

            int IdSize = PlayerData.get(player).size();
            for (int i = 0; i < IdSize; i++) {
                if (PlayerData.get(player).get(i) == ID) {
                    PlayerData.get(player).remove(i);
                    markDirty();
                    break;
                }
            }

            return true;

        } else {
            return false;
        }
    }

    public int CreateTeam(EntityPlayerMP sender, String name) {
        UUID senderId = sender.getUniqueID();
        boolean DoesPlayerHaveTeam = Teams.stream().anyMatch(o -> o.IsMemeber(senderId));
        if (DoesPlayerHaveTeam) return -1; // The player already has a team
        boolean DoesTeamNameAlreadyExist = Teams.stream().anyMatch(o -> o.Name.toLowerCase().equals(name.toLowerCase()));
        if (DoesTeamNameAlreadyExist) return -2; // The team name already exists

        Teams.add(new Team(sender,name.toLowerCase(),(Teams.size() == 0 ? 0 : Teams.get(Teams.size() - 1).ID + 1)));
        markDirty();

        if (Config.syncWithBetterQuesting) {
            BetterQuestingMethodWrapper.onCreate(sender,name);
        }

        return 1; // Succes
    }

    public int JoinTeam(EntityPlayerMP sender, String name) {
        UUID senderId = sender.getUniqueID();
        boolean IsPlayerInTeam = Teams.stream().anyMatch(o -> o.IsMemeber(senderId));
        if (IsPlayerInTeam) return -1; // The player is already in a team
        boolean DoesNameExist = Teams.stream().anyMatch(o -> o.Name.toLowerCase().equals(name.toLowerCase()));
        if (!DoesNameExist) return -2; // The team name does not exist
        for (Team t : Teams) {
            if (t.Name.toLowerCase().equals(name.toLowerCase())) {
                if (t.IsInviteOnly) return -4; // The team is invite only
                if (t.IsPlayerBanned(sender.getUniqueID())) return -3; // You are banned
                t.AddMemeber(sender);

                if (Config.syncWithBetterQuesting) {
                    BetterQuestingMethodWrapper.onJoin(sender,t.ID);
                }

                break;
            }
        }
        markDirty();

        return 1; // Succes
    }

    public Tuple<Integer,String> LeaveTeam(EntityPlayerMP sender) {
        UUID senderId = sender.getUniqueID();
        boolean IsPlayerInTeam = Teams.stream().anyMatch(o -> o.IsMemeber(senderId));
        if (!IsPlayerInTeam) return new Tuple<>(-1,"null"); // The player is not in a team
        for (Team t : Teams) {
            if (t.IsMemeber(senderId)) {
                if (t.Owner.equals(senderId)) {

                    if (Config.syncWithBetterQuesting) {
                        BetterQuestingMethodWrapper.onLeaveAndDestroy(sender, t.ID);
                    }

                    Teams.remove(t);
                    markDirty();

                    return new Tuple<>(2,t.Name); // The player was the owner of the team (Succes)
                } else {
                    t.RemoveMember(senderId);
                    markDirty();

                    if (Config.syncWithBetterQuesting) {
                        BetterQuestingMethodWrapper.onLeave(sender, t.ID);
                    }

                    return new Tuple<>(1,t.Name); // Succes
                }
            }
        }
        return null;
    }

    public int KickFromTeam(EntityPlayerMP sender,String player) {
        UUID senderId = sender.getUniqueID();
        boolean IsPlayerInTeam = Teams.stream().anyMatch(o -> o.IsMemeber(senderId));
        if (!IsPlayerInTeam) return -4; // The player is not is a team

        for (Team t : Teams) {
            if (t.Owner.equals(senderId)) {
                EntityPlayerMP toBeKicked = (EntityPlayerMP) sender.getServerWorld().getPlayerEntityByName(player);

                UUID toBeKickedUUID;

                if (toBeKicked == null) toBeKickedUUID = GetUUIDFromLocal(player);
                else toBeKickedUUID = toBeKicked.getUniqueID();

                if (toBeKickedUUID == null) return -1; // The player doesn't exist

                if (toBeKickedUUID.equals(senderId)) return -2; // You cannot kick yourself
                if (!t.IsMemeber(toBeKickedUUID)) return -1; // The player is not a part of the team or doesn't exist

                t.RemoveMember(toBeKickedUUID);
                t.SendMessageToAll(sender.getServerWorld(),"\u00a7c" + player + " was kicked from the team!");

                if (toBeKicked != null) toBeKicked.sendMessage(new TextComponentString("\u00a7cYou have been kicked from the team " + t.Name + "!"));

                markDirty();

                if (Config.syncWithBetterQuesting) {
                    BetterQuestingMethodWrapper.onKick(toBeKicked,t.ID);
                }

                return 1;
            }
        }

        return -3; // The player is not the team owner

    }

    public int BanFromTeam(EntityPlayerMP sender,String player) {
        UUID senderId = sender.getUniqueID();
        boolean IsPlayerInTeam = Teams.stream().anyMatch(o -> o.IsMemeber(senderId));
        if (!IsPlayerInTeam) return -4; // The player is not is a team

        for (Team t : Teams) {
            if (t.Owner.equals(senderId)) {
                EntityPlayerMP toBeBanned = (EntityPlayerMP) sender.getServerWorld().getPlayerEntityByName(player);

                UUID toBeBannedUUID;

                if (toBeBanned == null) toBeBannedUUID = GetUUIDFromLocal(player); // Try to fetch the local uuid from local databse in case the player might be offline
                else toBeBannedUUID = toBeBanned.getUniqueID();

                if (toBeBannedUUID == null) return -1;

                if (toBeBannedUUID.equals(senderId)) return -2; // You cannot ban yourself
                if (!t.IsMemeber(toBeBannedUUID)) return -1; // The player is not a part of the team or doesn't exist

                t.BanMemeber(toBeBannedUUID);
                t.SendMessageToAll(sender.getServerWorld(),"\u00a7c" + player + " was banned from the team!");

                if (toBeBanned != null) toBeBanned.sendMessage(new TextComponentString("\u00a7cYou have been banned from the team " + t.Name + "!"));

                markDirty();

                if (Config.syncWithBetterQuesting) {
                    BetterQuestingMethodWrapper.onBan(toBeBanned,t.ID);
                }

                return 1;
            }
        }

        return -3; // The player is not the team owner
    }

    public int UnBanFromTeam(EntityPlayerMP sender,String player) {
        UUID senderId = sender.getUniqueID();
        boolean IsPlayerInTeam = Teams.stream().anyMatch(o -> o.IsMemeber(senderId));
        if (!IsPlayerInTeam) return -4; // The player is not is a team

        for (Team t : Teams) {
            if (t.Owner.equals(senderId)) {
                EntityPlayerMP toBeBanned = (EntityPlayerMP) sender.getServerWorld().getPlayerEntityByName(player);

                UUID toBeBannedUUID;

                if (toBeBanned == null) toBeBannedUUID = GetUUIDFromLocal(player); // Try to fetch the local uuid from local databse in case the player might be offline
                else toBeBannedUUID = toBeBanned.getUniqueID();

                if (toBeBannedUUID == null) return -1; // The player is not banned or doesn't exist

                if (toBeBannedUUID.equals(senderId)) return -2; // You cannot unban yourself
                if (!t.IsPlayerBanned(toBeBannedUUID)) return -1; // The player is not banned or doesn't exist

                t.UnBanMemeber(toBeBannedUUID);
                t.SendMessageToAll(sender.getServerWorld(),"\u00a7a" + player + " was unbanned from the team!");
                if (toBeBanned != null) toBeBanned.sendMessage(new TextComponentString("\u00a7aYou have been unbanned from the team " + t.Name + "!"));

                markDirty();
                return 1;
            }
        }

        return -3; // The player is not the team owner
    }

    public int ToggleTeamInviteOnly(EntityPlayerMP sender) {
        UUID senderId = sender.getUniqueID();
        boolean IsPlayerInTeam = Teams.stream().anyMatch(o -> o.IsMemeber(senderId));
        if (!IsPlayerInTeam) return -2; // The player is not is a team

        for (Team t : Teams) {
            if (t.Owner.equals(senderId)) {
                t.IsInviteOnly = !t.IsInviteOnly;
                markDirty();
                if (t.IsInviteOnly) return 1; // Turned on
                return 2; // Turned off
            }
        }

        return -1; // The player is not the team owner
    }

    public String AddPlayerToTeam(EntityPlayerMP player,long teamId) {
        for (Team t : Teams) {
            if (t.ID == teamId) {
                t.AddMemeber(player);
                markDirty();
                return t.Name;
            }
        }
        return null;
    }

    // Used for fetching uuids when the player is offline
    public UUID GetUUIDFromLocal(String name) {
        if (StoredUsernamesToUUID.containsKey(name)) return StoredUsernamesToUUID.get(name);
        return null;
    }

    public void AddUsernameUUID(String name,UUID uuid) {
        for (String username : StoredUsernamesToUUID.keySet()) {
            if (StoredUsernamesToUUID.get(username).equals(uuid) && !name.equals(username)) {
                StoredUsernamesToUUID.remove(username);
            }
            break;
        }
        StoredUsernamesToUUID.put(name,uuid);
        markDirty();
    }

    public void AddID(long sid,int bid) {
        stdIdToBqId.put(sid,bid);
        markDirty();
    }

    public void RemoveID(long sid) {
        stdIdToBqId.remove(sid);
        markDirty();
    }


}
