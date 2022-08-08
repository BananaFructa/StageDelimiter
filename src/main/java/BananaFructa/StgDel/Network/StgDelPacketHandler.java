package BananaFructa.StgDel.Network;

import BananaFructa.StgDel.StgDel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class StgDelPacketHandler {
    public static final SimpleNetworkWrapper INSTNACE = NetworkRegistry.INSTANCE.newSimpleChannel(StgDel.modId);

    public static void RegisterPackets() {
        INSTNACE.registerMessage(SPacketStageChangeStateHandler.class, SPacketStageChangeState.class,0, Side.CLIENT);
        INSTNACE.registerMessage(SPacketExportRegistryNamesHandler.class, SPacketExportRegistryNames.class,1,Side.CLIENT);
        INSTNACE.registerMessage(SPacketSyncBQHandler.class,SPacketSyncBQ.class,2,Side.CLIENT);
    }


}
