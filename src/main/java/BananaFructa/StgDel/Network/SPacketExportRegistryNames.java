package BananaFructa.StgDel.Network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SPacketExportRegistryNames implements IMessage {

    class StageData  implements Serializable {
        public HashMap<Integer, String> names; // The names of the stage
        public HashMap<Integer, List<String>> registry; // Banned registry names for each stage
        public StageData(HashMap<Integer, String> names, HashMap<Integer, List<String>> registry) {
            this.names = names;
            this.registry = registry;
        }
    }


    public SPacketExportRegistryNames() {

    }

    public SPacketExportRegistryNames(HashMap<Integer,String> NameDictionary,HashMap<Integer,List<String>> RegistryNames) {
        data = new StageData(NameDictionary,RegistryNames);
    }

    public transient StageData data;

    @Override
    public void fromBytes(ByteBuf buf) {
        HashMap<Integer,String> names = new HashMap<>();
        HashMap<Integer,List<String>> regitry = new HashMap<>();

        int CodeCount = buf.readInt();

        List<Integer> Codes = new ArrayList<>();

        for (int i = 0;i < CodeCount;i++) {
            Codes.add(buf.readInt());
        }

        for (int i = 0;i < CodeCount;i++) {
            int count = buf.readInt();
            names.put(Codes.get(i),buf.readCharSequence(count,Charset.defaultCharset()).toString());
        }

        for (int i = 0;i < CodeCount;i++) {
            List<String> l = new ArrayList<>();
            int count = buf.readInt();
            for (int j = 0;j < count;j++) {
                int length = buf.readInt();
                l.add(buf.readCharSequence(length,Charset.defaultCharset()).toString());
            }
            regitry.put(Codes.get(i),l);
        }

        data = new StageData(names,regitry);

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(data.names.keySet().size());
        for (int i : data.names.keySet()) {
            buf.writeInt(i);
        }

        for (int i : data.names.keySet()) {
            String s = data.names.get(i);
            buf.writeInt(s.length());
            buf.writeCharSequence(s, Charset.defaultCharset());
        }

        for (int i : data.names.keySet()) {
            List<String> names = data.registry.get(i);
            buf.writeInt(names.size());
            for (String s : names) {
                buf.writeInt(s.length());
                buf.writeCharSequence(s,Charset.defaultCharset());
            }
        }

    }
}
