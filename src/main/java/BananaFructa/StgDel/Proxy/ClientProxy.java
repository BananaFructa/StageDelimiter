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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ClientProxy extends CommonProxy {

    Minecraft mc = Minecraft.getMinecraft();

    public List<Stage> Stages = new ArrayList<>();

    private final List<String> BannedUsageCacheClient = new ArrayList<String>();
    private final List<String> AllowedUsageCacheClient = new ArrayList<String>();

    // client side boolean
    public boolean syncWithBQ = false;

    public void InitClient(FMLInitializationEvent event) {
        StgDelPacketHandler.RegisterPackets();
    }

    @SubscribeEvent
    public void onClientLeave(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Stages.clear();
        BannedUsageCacheClient.clear();
        AllowedUsageCacheClient.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.currentScreen instanceof GuiInventory) {
            GuiContainer container = (GuiContainer) mc.currentScreen;
            if(!IsItemUseAllowedForMe(container.inventorySlots.getSlot(0).getStack())) {
                for (int i = 1;i < 5;i++) {
                    mc.playerController.windowClick(container.inventorySlots.windowId,i,0,ClickType.QUICK_MOVE,mc.player);
                }
            }
        } else if (mc.currentScreen instanceof GuiCrafting) {
            GuiContainer container = (GuiContainer) mc.currentScreen;
            ContainerWorkbench containerWorkbench = (ContainerWorkbench) container.inventorySlots;
            if (!IsItemUseAllowedForMe(containerWorkbench.craftResult.getStackInSlot(0))) {
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

        if (IsItemUseAllowedForMe(event.getItemStack())) return;
        for (Stage s : Stages) {
            if (!s.isActive()) {
                final String registry = getCompleteRegistry(event.getItemStack());
                if (!s.RegistryNames.stream().anyMatch(o -> IsRegistryExcepted(o,registry)) && s.RegistryNames.stream().anyMatch(o -> DoRegistriesMatch(o,registry))) {
                    event.getToolTip().add("\u00a7cRequires " + s.getStageName() + "!");
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractionClient(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isRemote) {
            if (!IsItemUseAllowedForMe(event.getItemStack())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractionClient(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) {
            if (!IsItemUseAllowedForMe(event.getItemStack())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractionClient(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getWorld().isRemote) {
            if (!IsItemUseAllowedForMe(event.getItemStack())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (syncWithBQ && event.getGui() != null) {
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

    public boolean IsItemUseAllowedForMe(ItemStack is) {
        if (Minecraft.getMinecraft().player == null) return true;
        try {
            if (is == null || Minecraft.getMinecraft().player.capabilities.isCreativeMode) return true;
            String name = getCompleteRegistry(is);
            if (name.equals("minecraft:air")) return true;
            if (BannedUsageCacheClient == null || AllowedUsageCacheClient == null) return true;
            if (BannedUsageCacheClient.contains(name)) return false;
            if (AllowedUsageCacheClient.contains(name)) return true;
            for (Stage s : Stages) {
                if (!s.isActive()) {
                    for (String register : s.RegistryNames) {
                        if (IsRegistryExcepted(register, name)) continue;
                        if (DoRegistriesMatch(register, name)) {
                            if (BannedUsageCacheClient.size() > 4) BannedUsageCacheClient.remove(0);
                            BannedUsageCacheClient.add(name);
                            return false;
                        }
                    }
                }
            }
            if (AllowedUsageCacheClient.size() > 4) AllowedUsageCacheClient.remove(0);
            AllowedUsageCacheClient.add(name);
            return true;
        } catch (Exception err) {
            err.printStackTrace();
            return true;
        }

    }
}
