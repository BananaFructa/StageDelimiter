package BananaFructa.StgDel;

import java.util.UUID;

public class TeamInvite {
    public UUID reciever;
    public long teamId;
    public long expirationDate;

    public TeamInvite(UUID reciever,long teamId,long timeUntilExpires) {
        this.reciever = reciever;
        this.teamId = teamId;
        expirationDate = System.currentTimeMillis() + timeUntilExpires;
    }
}
