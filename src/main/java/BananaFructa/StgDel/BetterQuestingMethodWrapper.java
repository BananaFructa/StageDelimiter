package BananaFructa.StgDel;

import betterquesting.network.handlers.NetPartyAction;
import betterquesting.questing.party.PartyInvitations;
import betterquesting.questing.party.PartyManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BetterQuestingMethodWrapper {

    private static Method mOnServer,mInvite;

    public static void init() {

        try {
            mOnServer = NetPartyAction.class.getDeclaredMethod("onServer", Tuple.class);
            mOnServer.setAccessible(true);

            mInvite = NetPartyAction.class.getDeclaredMethod("inviteUser", int.class, String.class, long.class);
            mInvite.setAccessible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static int getBQId(long teamId) {
        return StgDel.proxy.stageStateData.stdIdToBqId.get(teamId);
    }

    private static void onServer(Tuple<NBTTagCompound,EntityPlayerMP> message) {
        try {
            mOnServer.invoke(null,message);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void inviteUser(int id,String username, long expiry) {
        try {
            mInvite.invoke(null,id,username,expiry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onCreate(EntityPlayerMP player,String teamName) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("action",0);
        compound.setString("name",teamName);
        StgDel.proxy.stageStateData.AddID(StgDel.proxy.stageStateData.GetTeamID(teamName),PartyManager.INSTANCE.nextID());
        onServer(new Tuple<>(compound,player));
    }

    public static void onInvite(EntityPlayerMP sender, String username,long expiry, long teamID) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("action",3);
        compound.setInteger("partyID",getBQId(teamID));
        compound.setLong("expiry",expiry);
        compound.setString("username",username);
        onServer(new Tuple<>(compound,sender));
    }

    public static void onDeclineInvite(EntityPlayerMP player, long teamId) {
        PartyInvitations.INSTANCE.revokeInvites(player.getUniqueID(),getBQId(teamId));
    }

    public static void onAcceptInvite(EntityPlayerMP player, long teamId) {
        int id = getBQId(teamId);
        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("action",4);
        compound.setInteger("partyID",id);

        onServer(new Tuple<>(compound,player));
    }

    public static void onJoin(EntityPlayerMP player,long teamId) {
        int id = getBQId(teamId);

        // bypass the onServer method
        inviteUser(id,player.getName(),5000);

        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("action",4);
        compound.setInteger("partyID",id);

        onServer(new Tuple<>(compound,player));
    }

    public static void onLeaveAndDestroy(EntityPlayerMP player,long teamId) {
        int id = StgDel.proxy.stageStateData.stdIdToBqId.get(teamId);
        StgDel.proxy.stageStateData.RemoveID(teamId);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("action",1);
        compound.setInteger("partyID",id);
        onServer(new Tuple<>(compound,player));
    }

    public static void onLeave(EntityPlayerMP player,long teamId) {
        int id = getBQId(teamId);

        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("action", 5);
        compound.setInteger("partyID", id);
        compound.setString("username",player.getName());

        onServer(new Tuple<>(compound,player));

    }

    public static void onKick(EntityPlayerMP player, long teamId) {
        onLeave(player,teamId);
    }

    public static void onBan(EntityPlayerMP player, long teamId) {
        onLeave(player,teamId);
    }

}
