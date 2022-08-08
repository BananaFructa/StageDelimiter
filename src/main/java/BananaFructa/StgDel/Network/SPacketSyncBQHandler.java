package BananaFructa.StgDel.Network;

import BananaFructa.StgDel.Proxy.ClientProxy;
import BananaFructa.StgDel.StgDel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SPacketSyncBQHandler implements IMessageHandler<SPacketSyncBQ, IMessage> {
    @Override
    public IMessage onMessage(SPacketSyncBQ message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                ((ClientProxy) StgDel.proxy).syncWithBQ = message.syncWithBq;
            }
        });

        return null;
    }
}
