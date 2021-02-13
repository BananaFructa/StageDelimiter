package BananaFructa.StgDel.Network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SPacketStageChangeState implements IMessage {

    public SPacketStageChangeState() {

    }

    public int ID;
    public boolean State;

    public SPacketStageChangeState(int ID,boolean State) {
        this.ID = ID;
        this.State = State;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        ID = buf.readInt();
        State = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(ID);
        buf.writeBoolean(State);

    }
}
