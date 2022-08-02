package BananaFructa.StgDel.Proxy;

import BananaFructa.StgDel.Config;
import BananaFructa.StgDel.Gui.GuiBQSynced;
import BananaFructa.StgDel.Network.StgDelPacketHandler;
import BananaFructa.StgDel.Stage;
import betterquesting.client.gui2.party.GuiPartyCreate;
import betterquesting.client.gui2.party.GuiPartyManage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class ClientProxy extends CommonProxy {

    Minecraft mc = Minecraft.getMinecraft();

    public List<Stage> Stages = new ArrayList<>();

    public void InitClient(FMLInitializationEvent event) {
        StgDelPacketHandler.RegisterPackets();
    }

    @SubscribeEvent
    public void onClientLeave(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Stages.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.currentScreen instanceof GuiInventory) {
            GuiContainer container = (GuiContainer) mc.currentScreen;
            if(!IsItemUseAllowed(container.inventorySlots.getSlot(0).getStack())) {
                for (int i = 1;i < 5;i++) {
                    mc.playerController.windowClick(container.inventorySlots.windowId,i,0,ClickType.QUICK_MOVE,mc.player);
                }
            }
        } else if (mc.currentScreen instanceof GuiCrafting) {
            GuiContainer container = (GuiContainer) mc.currentScreen;
            ContainerWorkbench containerWorkbench = (ContainerWorkbench) container.inventorySlots;
            if (!IsItemUseAllowed(containerWorkbench.craftResult.getStackInSlot(0))) {
                for (int i = 1; i < 10; i++) {
                    mc.playerController.windowClick(containerWorkbench.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent event) {
        Integer stage = GetStageUnlock(event.getItemStack());

        if (stage != null) {
            for (Stage s : Stages) {
                if (s.ID == stage) {
                    event.getToolTip().add("\u00a7aUnlocks " + s.getStageName() + "!");
                    break;
                }
            }
        }

        if (IsItemUseAllowed(event.getItemStack())) return;
        for (Stage s : Stages) {
            if (!s.isActive()) {
                if (s.RegistryNames.stream().anyMatch(o -> DoRegistriesMatch(o,event.getItemStack().getItem().getRegistryName().toString()))) {
                    event.getToolTip().add("\u00a7cRequires " + s.getStageName() + "!");
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractionClient(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isRemote) {
            if (!IsItemUseAllowed(event.getItemStack())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractionClient(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) {
            if (!IsItemUseAllowed(event.getItemStack())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractionClient(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getWorld().isRemote) {
            if (!IsItemUseAllowed(event.getItemStack())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (Config.syncWithBetterQuesting && event.getGui() != null) {
            if (event.getGui() instanceof GuiPartyManage || event.getGui() instanceof GuiPartyCreate) {
                event.setCanceled(true);
                Minecraft.getMinecraft().displayGuiScreen(new GuiBQSynced());
            }
        }
    }

    public boolean SetStageState(int ID,boolean state) {
        for (Stage s : Stages) {
            if (s.ID == ID) {
                s.setActive(state);
                return true;
            }
        }
        return false;
    }

    public boolean IsItemUseAllowed(ItemStack stack) {
        if (Minecraft.getMinecraft().player == null) return true;
        return IsItemUseAllowedForPlayer(Minecraft.getMinecraft().player, stack);
    }
}
