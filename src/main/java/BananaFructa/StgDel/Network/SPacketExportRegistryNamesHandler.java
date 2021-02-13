package BananaFructa.StgDel.Network;

import BananaFructa.StgDel.Proxy.ClientProxy;
import BananaFructa.StgDel.StgDel;
import BananaFructa.StgDel.Stage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SPacketExportRegistryNamesHandler implements IMessageHandler<SPacketExportRegistryNames, IMessage> {

    @Override
    public IMessage onMessage(SPacketExportRegistryNames message, MessageContext ctx) {

        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                for (int id : message.data.names.keySet()) {

                    Stage stage = new Stage(message.data.names.get(id),id);

                    stage.RegistryNames.addAll(message.data.registry.get(id));

                    ((ClientProxy)StgDel.proxy).Stages.add(stage);

                }
            }
        });

        return null;
    }
}
