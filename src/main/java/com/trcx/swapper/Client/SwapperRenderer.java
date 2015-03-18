package com.trcx.swapper.Client;

import com.trcx.swapper.Common.Item.Swapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/**
 * Created by Trcx on 3/16/2015.
 */
public class SwapperRenderer implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack swapper, ItemRenderType type) {
        ItemStack is = Swapper.getLastStack(swapper);
        if (is!=null)
            if (is.getItem() instanceof ItemBlock)
                return true;
        return false;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack swapper, ItemRendererHelper helper) {
        ItemStack is = Swapper.getLastStack(swapper);
        if (is!=null)
            if (is.getItem() instanceof ItemBlock)
                return true;
        return type != ItemRenderType.INVENTORY;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack swapper, Object... data) {
        try {
            ItemStack is = Swapper.getLastStack(swapper);
            Minecraft mc = Minecraft.getMinecraft();
            if (is != null)
                if (is.getItem() instanceof ItemBlock) {
                    //mc.renderEngine.bindTexture(TextureMap.locationItemsTexture);

                    if (type != ItemRenderType.INVENTORY) {
                        GL11.glTranslated(0.5, 0.5, 0.5);
                    } else {
                        GL11.glScaled(0.8, 0.8, 0.8);
                    }

                    RenderManager.instance.itemRenderer.renderItem(mc.thePlayer, is, 0, ItemRenderType.EQUIPPED);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
