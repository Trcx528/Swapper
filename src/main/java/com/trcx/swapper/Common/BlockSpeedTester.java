package com.trcx.swapper.Common;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

/**
 * Created by Trcx on 3/21/2015.
 */
public class BlockSpeedTester extends Block {

    public BlockSpeedTester() {
        super(Material.rock);
        setHardness(0.5f);
        setHarvestLevel("pickaxe",0);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int p_149636_3_, int p_149636_4_, int p_149636_5_, int p_149636_6_) {
        FMLLog.log("Swapper",Level.INFO,player.getHeldItem().toString() + " " + player.getHeldItem().getItem().getDigSpeed(player.getHeldItem(),this,0));
        super.harvestBlock(world, player, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_);
    }
}
