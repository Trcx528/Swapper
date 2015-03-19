package com.trcx.swapper;

/**
 * Created by Trcx on 2/24/2015.
 */

import com.trcx.swapper.Common.*;
import com.trcx.swapper.Common.Item.Swapper;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import java.io.IOException;

@Mod(modid = "swapper", version = Main.VERSION, name = "Swapper")
public class Main
{
    public static final String VERSION = "1.0.3";

    public static Item Swapper;
    public static String[] swapperBlacklist = new String[2];


    @Mod.Instance("swapper")
    public static Main instance;

    @SidedProxy(clientSide = "com.trcx.swapper.Client.ClientProxy", serverSide = "com.trcx.swapper.Common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException{
        Configuration commonConfig = new Configuration(event.getSuggestedConfigurationFile());
        commonConfig.load();

        String[] swapperBlacklist = new String[2];
        swapperBlacklist[0] = "OpenBlocks:devnull";
        swapperBlacklist[1] = "appliedenergistics2:item.ToolWirelessTerminal";

        Main.swapperBlacklist = commonConfig.getStringList("Swapper Blacklist", "Configuration",swapperBlacklist,"List of Items to blacklist from insertion into the swapper");

        commonConfig.save();

        Swapper = new Swapper().setUnlocalizedName("Swapper").setTextureName("swapper:Swapper");
        GameRegistry.registerItem(Swapper, "swapper");
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void serverInit(FMLServerStartingEvent event){

    }

}
