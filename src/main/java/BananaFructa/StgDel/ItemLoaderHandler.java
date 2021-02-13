package BananaFructa.StgDel;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


@Mod.EventBusSubscriber
public class ItemLoaderHandler {

    public static StageKey key;

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        key = new StageKey();
        event.getRegistry().register(key);
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(key,0,new ModelResourceLocation(key.getRegistryName(), "inventory"));
    }
}
