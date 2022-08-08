package BananaFructa.StgDel.Network;


import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SPacketSyncBQ implements IMessage {

    public boolean syncWithBq;

    public SPacketSyncBQ() {

    }

    public SPacketSyncBQ(boolean sync) {
        syncWithBq = sync;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        syncWithBq = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(syncWithBq);
    }
}
