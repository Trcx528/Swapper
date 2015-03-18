package com.trcx.swapper.Client;

import com.trcx.swapper.Common.OpenMods.PlayerItemInventory;
import com.trcx.swapper.Common.SwapperContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Created by Trcx on 3/14/2015.
 */
public class SwapperGui extends GuiContainer {
    public SwapperGui (InventoryPlayer invPlayer, PlayerItemInventory invSwapper) {
        //the container is instantiated and passed to the superclass for handling
        super(new SwapperContainer(invPlayer, invSwapper));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("swapper:textures/gui/swapper.png"));
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }
}
