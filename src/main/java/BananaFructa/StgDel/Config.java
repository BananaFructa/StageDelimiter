package BananaFructa.StgDel;

import betterquesting.api.properties.NativeProps;
import betterquesting.storage.QuestSettings;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {

    public static Configuration configuration;
    public static boolean syncWithBetterQuesting;

    public static void init(File configDirectory) {
        configuration = new Configuration(new File(configDirectory,"stgdel.cfg"));

        syncWithBetterQuesting = configuration.getBoolean("sync_with_betterquesting","general",false,"Set to true if you want better questing parties to be synced with the /team command.");

        if (configuration.hasChanged()) configuration.save();
    }

}
