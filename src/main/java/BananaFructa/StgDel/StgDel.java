package BananaFructa.StgDel;

import BananaFructa.StgDel.Proxy.ClientProxy;
import BananaFructa.StgDel.Proxy.CommonProxy;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;


@Mod(modid = StgDel.modId, version = StgDel.version, name = StgDel.name,dependencies = "after:immersiveengineering;before:immersiveintelligence")
public class StgDel {

    public static final String modId = "stgdel";
    public static final String name = "StageDelimiter";
    public static final String version = "1.2.0";

    public static boolean immersiveEngineeringLoaded = false;

    @SidedProxy(modId = StgDel.modId,clientSide = "BananaFructa.StgDel.Proxy.ClientProxy",serverSide = "BananaFructa.StgDel.Proxy.CommonProxy")
    public static CommonProxy proxy;

    public StgDel() {
       immersiveEngineeringLoaded = Loader.isModLoaded("immersiveengineering");
    }

    @Mod.EventHandler
    public void PreInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(proxy);
        Config.init(event.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void Init(FMLInitializationEvent event) {
        if (proxy instanceof ClientProxy) ((ClientProxy)proxy).InitClient(event);
    }

    @Mod.EventHandler
    public void Server(FMLServerStartingEvent event) {
        if (Config.syncWithBetterQuesting) BetterQuestingMethodWrapper.init();
        MinecraftForge.EVENT_BUS.register(proxy);
        proxy.InitServer(event);
    }


}
