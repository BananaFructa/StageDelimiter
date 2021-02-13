package BananaFructa.StgDel.Network;

import BananaFructa.StgDel.Proxy.ClientProxy;
import BananaFructa.StgDel.StgDel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SPacketStageChangeStateHandler implements IMessageHandler<SPacketStageChangeState, IMessage> {

    @Override
    public IMessage onMessage(SPacketStageChangeState message, MessageContext ctx) {

        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                ((ClientProxy)StgDel.proxy).SetStageState(message.ID,message.State);
            }
        });

        return null;
    }
}
