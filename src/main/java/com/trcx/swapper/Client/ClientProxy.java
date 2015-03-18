package com.trcx.swapper.Client;

import com.trcx.swapper.Common.CommonProxy;
import com.trcx.swapper.Main;
import net.minecraftforge.client.MinecraftForgeClient;

/**
 * Created by Trcx on 3/16/2015.
 */
public class ClientProxy extends CommonProxy {
    @Override
    public void registerRenderers() {
        MinecraftForgeClient.registerItemRenderer(Main.Swapper,new SwapperRenderer());
    }
}
