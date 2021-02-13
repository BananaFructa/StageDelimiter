package BananaFructa.StgDel.Commands;

import BananaFructa.StgDel.StageStateData;
import BananaFructa.StgDel.StgDel;
import BananaFructa.StgDel.Team;
import BananaFructa.StgDel.TeamInvite;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentString;

import java.util.UUID;

public class TeamInteractionCommand extends CommandBase {


    @Override
    public String getName() {
        return "team";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();

        switch (args[0].toLowerCase()) {
            case "create":
                switch (StageStateData.get(player.getServerWorld()).CreateTeam(player, args[1])) {
                    case -1:
                        player.sendMessage(new TextComponentString("\u00a7cYou are already in a team!"));
                        break;
                    case -2:
                        player.sendMessage(new TextComponentString("\u00a7cThis team name is taken!"));
                        break;
                    case 1:
                        player.sendMessage(new TextComponentString("\u00a7aSuccessfully created team " + args[1].toLowerCase() + "!"));
                        break;
                }
                break;
            case "join":
                switch (StageStateData.get(player.getServerWorld()).JoinTeam(player, args[1])) {
                    case -1:
                        player.sendMessage(new TextComponentString("\u00a7cYou are already in a team!"));
                        break;
                    case -2:
                        player.sendMessage(new TextComponentString("\u00a7cThis team does not exist!"));
                        break;
                    case -3:
                        player.sendMessage(new TextComponentString("\u00a7cYou have been banned from this team!"));
                        break;
                    case -4:
                        player.sendMessage(new TextComponentString("\u00a7cThis team requires an invitation to join!"));
                        break;
                    case 1:
                        player.sendMessage(new TextComponentString("\u00a7aSuccessfully joined team " + args[1].toLowerCase() + "!"));
                        break;
                }
                break;
            case "leave":
                Tuple<Integer,String> result = StageStateData.get(player.getServerWorld()).LeaveTeam(player);
                switch (result.getFirst()) {
                    case -1:
                        player.sendMessage(new TextComponentString("\u00a7cYou are not in a team!"));
                        break;
                    case 2:
                        player.sendMessage(new TextComponentString("\u00a7cSince you were the team leader the team was disbanded!"));
                    case 1:
                        player.sendMessage(new TextComponentString("\u00a7aSuccessfully left team " + result.getSecond() + "!"));
                        break;
                }
                break;
            case "kick":
                switch (StageStateData.get(player.getServerWorld()).KickFromTeam(player,args[1])) {
                    case -4:
                        player.sendMessage(new TextComponentString("\u00a7cYou are not in a team!"));
                        break;
                    case -3:
                        player.sendMessage(new TextComponentString("\u00a7cYou are not the team owner!"));
                        break;
                    case -2:
                        player.sendMessage(new TextComponentString("\u00a7cYou cannot kick yourself!"));
                        break;
                    case -1:
                        player.sendMessage(new TextComponentString("\u00a7cThe player is not part of this team or doesn't exist!"));
                        break;
                    case 1:
                        player.sendMessage(new TextComponentString("\u00a7aSuccessfully kicked player " + args[1] + "!"));
                        break;
                }
                break;
            case "ban":
                switch (StageStateData.get(player.getServerWorld()).BanFromTeam(player,args[1])) {
                    case -4:
                        player.sendMessage(new TextComponentString("\u00a7cYou are not in a team!"));
                        break;
                    case -3:
                        player.sendMessage(new TextComponentString("\u00a7cYou are not the team owner!"));
                        break;
                    case -2:
                        player.sendMessage(new TextComponentString("\u00a7cYou cannot ban yourself!"));
                        break;
                    case -1:
                        player.sendMessage(new TextComponentString("\u00a7cThe player is not part of this team or doesn't exist!"));
                        break;
                    case 1:
                        player.sendMessage(new TextComponentString("\u00a7aSuccessfully banned player " + args[1] + "!"));
                        break;
                }
                break;
            case "unban":
                switch (StageStateData.get(player.getServerWorld()).UnBanFromTeam(player,args[1])) {
                    case -4:
                        player.sendMessage(new TextComponentString("\u00a7cYou are not in a team!"));
                        break;
                    case -3:
                        player.sendMessage(new TextComponentString("\u00a7cYou are not the team owner!"));
                        break;
                    case -2:
                        player.sendMessage(new TextComponentString("\u00a7cYou cannot unban yourself!"));
                        break;
                    case -1:
                        player.sendMessage(new TextComponentString("\u00a7cThe player is not banned or doesn't exist!"));
                        break;
                    case 1:
                        player.sendMessage(new TextComponentString("\u00a7aSuccessfully unbanned player " + args[1] + "!"));
                        break;
                }
                break;
            case "inviteonly":
                switch (StageStateData.get(player.getServerWorld()).ToggleTeamInviteOnly(player)) {
                    case -2:
                        player.sendMessage(new TextComponentString("\u00a7cYou are not in a team!"));
                        break;
                    case -1:
                        player.sendMessage(new TextComponentString("\u00a7cYou are not the team owner!"));
                        break;
                    case 1:
                        player.sendMessage(new TextComponentString("\u00a7aInvite only mode turned on!"));
                        break;
                    case 2:
                        player.sendMessage(new TextComponentString("\u00a7aInvite only mode turned off!"));
                        break;
                }
                break;
            case "invite": {
                    StageStateData data = StageStateData.get(player.getServerWorld());
                    UUID senderId = player.getUniqueID();

                    boolean IsPlayerInTeam = data.Teams.stream().anyMatch(o -> o.IsMemeber(senderId));

                    if (!IsPlayerInTeam) player.sendMessage(new TextComponentString("\u00a7cYou are not in a team!"));
                    else {
                        boolean FoundATeam = false;

                        for (Team t : data.Teams) {
                            if (t.Owner.equals(senderId)) {
                                FoundATeam = true;
                                EntityPlayerMP playerToInvite = (EntityPlayerMP) player.getServerWorld().getPlayerEntityByName(args[1]);

                                if (playerToInvite == null) {

                                    player.sendMessage(new TextComponentString("\u00a7cThe player is offline or doesn't exist!"));

                                } else if (playerToInvite.getUniqueID().equals(senderId)) {

                                    player.sendMessage(new TextComponentString("\u00a7cYou cannot invite yourself!"));

                                } else if (t.IsMemeber(playerToInvite.getUniqueID())) {

                                    player.sendMessage(new TextComponentString("\u00a7cThe player you tried to invite is already in your team!"));
                                } else if (t.IsPlayerBanned(playerToInvite.getUniqueID())) {

                                    player.sendMessage(new TextComponentString("\u00a7cThe player you tried to invite is banned!"));

                                } else {

                                    if (StgDel.proxy.AddTeamInvite(new TeamInvite(playerToInvite.getUniqueID(), t.ID, 60000))) {
                                        player.sendMessage(new TextComponentString("\u00a7aSuccessfully sent an invite to " + args[1] + "!"));
                                        playerToInvite.sendMessage(new TextComponentString("\u00a73You were invited to join team " + t.Name + " you have 60 seconds to accept/decline"));
                                        playerToInvite.sendMessage(new TextComponentString("\u00a73You can accept using /team accept and decline using /team decline"));
                                    } else {
                                        player.sendMessage(new TextComponentString("\u00a7cYou have already sent an invite to this player!"));
                                    }

                                }

                                break;

                            }

                        }

                        if (!FoundATeam) player.sendMessage(new TextComponentString("\u00a7cYou are not the team owner!"));
                    }
                }
                break;
            case "accept": {
                    TeamInvite invite = StgDel.proxy.GetInvite(player.getUniqueID());
                    if (invite == null) {
                        player.sendMessage(new TextComponentString("\u00a7cYou do not have a pending invite!"));
                    } else {
                        boolean IsPlayerInTeam = StageStateData.get(player.getServerWorld()).Teams.stream().anyMatch(o -> o.IsMemeber(player.getUniqueID()));
                        if (IsPlayerInTeam) {
                            player.sendMessage(new TextComponentString("\u00a7cYou are already in a team, leave the team in which you are currently in to be able to join another one!"));
                        } else {
                            String joinedTeam = StageStateData.get(player.getServerWorld()).AddPlayerToTeam(player, invite.teamId);
                            if (joinedTeam != null) {
                                StgDel.proxy.VoidInvite(invite);
                                player.sendMessage(new TextComponentString("\u00a7aSuccessfully joined team " + joinedTeam + "!"));
                            } else {
                                player.sendMessage(new TextComponentString("\u00a7cAn unexpected error has occurred!"));
                            }
                        }
                    }
                }
                break;
            case "decline":
                TeamInvite ti = StgDel.proxy.GetInvite(player.getUniqueID());
                if (ti == null) {
                    player.sendMessage(new TextComponentString("\u00a7cYou do not have a pending invite!"));
                } else {
                    StgDel.proxy.VoidInvite(ti);
                    player.sendMessage(new TextComponentString("\u00a7aInvite declined to join team " + StageStateData.get(player.getServerWorld()).GetTeam(ti.teamId) + "!"));
                }
                break;
            default:
                player.sendMessage(new TextComponentString("\u00a7cUnknown argument!"));
                break;
        }
    }
}
