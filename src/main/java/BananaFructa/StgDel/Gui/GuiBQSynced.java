package BananaFructa.StgDel.Gui;

import betterquesting.client.gui2.GuiHome;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;

public class GuiBQSynced extends GuiScreen {

    private final String msg = "The quest party is synced with the /team command, it doesn't need to be managed separately.";

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sr = new ScaledResolution(mc);
        buttonList.add(new GuiButton(0,sr.getScaledWidth()/2-60,sr.getScaledHeight()-50,120,15, "Back"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        ScaledResolution sr = new ScaledResolution(mc);
        mc.fontRenderer.drawStringWithShadow(msg,sr.getScaledWidth()/2 - mc.fontRenderer.getStringWidth(msg)/2,sr.getScaledHeight()/2 - mc.fontRenderer.FONT_HEIGHT/2,0xffffff);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            mc.displayGuiScreen(new GuiHome(null));
        }
    }
}
