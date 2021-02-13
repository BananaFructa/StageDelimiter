package BananaFructa.StgDel;


import java.util.ArrayList;
import java.util.List;

public class Stage {

    private String stageName;
    private boolean active = false;
    public int ID = 0;

    public List<String> RegistryNames = new ArrayList<>();

    public Stage(String stageName,int ID) {
        this.stageName = stageName;
        this.ID = ID;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
