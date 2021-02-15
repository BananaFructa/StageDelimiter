package BananaFructa.StgDel;

import BananaFructa.StgDel.Proxy.ClientProxy;
import BananaFructa.StgDel.Proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;


@Mod(modid = StgDel.modId, version = StgDel.version, name = StgDel.name)
public class StgDel {

    public static final String modId = "stgdel";
    public static final String name = "StageDelimiter";
    public static final String version = "1.1";

    @SidedProxy(modId = StgDel.modId,clientSide = "BananaFructa.StgDel.Proxy.ClientProxy",serverSide = "BananaFructa.StgDel.Proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void PreInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(proxy);
    }

    @Mod.EventHandler
    public void Init(FMLInitializationEvent event) {
        if (proxy instanceof ClientProxy) ((ClientProxy)proxy).InitClient(event);
    }

    @Mod.EventHandler
    public void Server(FMLServerStartingEvent event) {
        MinecraftForge.EVENT_BUS.register(proxy);
        proxy.InitServer(event);
    }


}
