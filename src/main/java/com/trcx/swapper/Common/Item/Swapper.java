package com.trcx.swapper.Common.Item;

import cofh.api.energy.IEnergyContainerItem;
import com.google.common.collect.Multimap;
import com.trcx.swapper.Common.OpenMods.ItemInventory;
import com.trcx.swapper.Main;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Trcx on 3/14/2015.
*/

//TODO funny rendering with itemBlock

@Optional.Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "ITA")
public class Swapper extends Item implements IEnergyContainerItem{

    private static final String stringLASTTOOL = "LastTool";

    public static IIcon outlineIcon;

    private static final int slotSHOVEL = 0;
    private static final int slotPICK =1;
    private static final int slotAXE = 2;
    private static final int slotSword = 3;
    private static final int slotRightClick = 4;

    public static final int swapperSlots = 5;

    private static final HashMap<String, ItemStack> testTools = new HashMap<String, ItemStack>();
    static
    {
        testTools.put("pickaxe", new ItemStack(Items.wooden_pickaxe));
        testTools.put("shovel", new ItemStack(Items.wooden_shovel));
        testTools.put("axe", new ItemStack(Items.wooden_axe));
        testTools.put("sword", new ItemStack(Items.wooden_sword));
    }

    public Swapper(){
        super();
        setCreativeTab(CreativeTabs.tabTools);
        setMaxStackSize(1);
        setMaxDamage(Integer.MAX_VALUE);
        MinecraftForge.EVENT_BUS.register(this);
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(this), "bpa", " c ", " s ", 's', Items.stick,
                'b', new ItemStack(Items.stone_shovel), 'p', new ItemStack(Items.stone_pickaxe),
                'a', new ItemStack(Items.stone_axe), 'c', new ItemStack(Item.getItemFromBlock(Blocks.chest))));
    }
//region inventory stuff
    public static ItemStack getLastStack(ItemStack swapper){
        if (!swapper.hasTagCompound()){
            swapper.stackTagCompound = new NBTTagCompound();
        }
        int slot = swapper.stackTagCompound.getInteger(stringLASTTOOL);
        return getStack(slot, swapper);
    }

    public static ItemStack getStack(int slot, ItemStack swapper){
        ItemInventory inv = new ItemInventory(swapper, swapperSlots);
        if (!swapper.hasTagCompound())
            swapper.stackTagCompound = new NBTTagCompound();
        if (swapper.stackTagCompound.hasKey("swung"))
            slot = slotSword;
        int prevSlot = 0;
        if (swapper.stackTagCompound.hasKey(stringLASTTOOL))
            prevSlot = swapper.stackTagCompound.getInteger(stringLASTTOOL);
        swapper.stackTagCompound.setInteger(stringLASTTOOL, slot);
        if (inv.getStackInSlot(slot) != null && inv.getStackInSlot(slot).getItem() == Main.Swapper)
            return null;
        ItemStack returnStack = inv.getStackInSlot(slot);
        if (returnStack == null)
            return null;
        if (swapper.stackTagCompound.hasKey("ench")){
            if (inv.getStackInSlot(prevSlot) != null && inv.getStackInSlot(prevSlot).hasTagCompound()) {
                inv.getStackInSlot(prevSlot).stackTagCompound.setTag("ench", swapper.stackTagCompound.getTag("ench"));
            }
            swapper.stackTagCompound.removeTag("ench");
        }
        if (slot != slotRightClick){
            if (returnStack.hasTagCompound() && returnStack.stackTagCompound.hasKey("ench")) {
                swapper.stackTagCompound.setTag("ench", returnStack.stackTagCompound.getTag("ench"));
            }
        }
        return returnStack;
    }

    public static void putLastStack(ItemStack swapper, ItemStack is){
        ItemInventory inv = new ItemInventory(swapper,swapperSlots);
        int slot = swapper.stackTagCompound.getInteger(stringLASTTOOL);
        ItemStack currentIs = getLastStack(swapper);
        if (is != null) {
            if (is.stackSize > 0) {
                if (is.getItem() == currentIs.getItem() ||
                        currentIs.getItem().getContainerItem() == is.getItem() ||
                        is.getItem().getContainerItem() == currentIs.getItem()) {
                    inv.setInventorySlotContents(slot, is);
                } // else don't save, it'd probably dupe is
            } else {
                inv.setInventorySlotContents(slot, null);
            }
        } else {
            inv.setInventorySlotContents(slot, null);
        }
        inv.markDirty();
    }

//endregion

//region Dig

    @Override
    public boolean onBlockStartBreak(ItemStack swapper, int X, int Y, int Z, EntityPlayer player) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            boolean ret = is.getItem().onBlockStartBreak(is, X, Y, Z, player);
            putLastStack(swapper,is);
            return ret;
        }
        return super.onBlockStartBreak(swapper, X, Y, Z, player);
    }

    @Override
    public float getDigSpeed(ItemStack swapper, Block block, int metadata) {

        String effTool = block.getHarvestTool(metadata);
        ItemStack is;
        if (effTool == null){
            for (Map.Entry<String, ItemStack> testToolEntry : testTools.entrySet())
            {
                ItemStack testTool = testToolEntry.getValue();
                if (testTool != null && testTool.getItem() instanceof ItemTool && testTool.func_150997_a(block) >= ((ItemTool) testTool.getItem()).func_150913_i().getEfficiencyOnProperMaterial())
                {
                    effTool = testToolEntry.getKey();
                    break;
                }
            }
        }
        if (effTool != null) {
            if (effTool.equals("shovel")) {
                is = getStack(slotSHOVEL, swapper);
            } else if (effTool.equals("axe")) {
                is = getStack(slotAXE, swapper);
            } else if (effTool.equals("sword")) {
                is = getStack(slotSword, swapper);
            } else {
                is = getStack(slotPICK, swapper);
            }
        } else {
            if (block == Blocks.web) {
                is = getStack(slotSword, swapper);
            } else {
                is = getStack(slotPICK, swapper);
            }
        }
        if (is != null) {
            float ret = is.getItem().getDigSpeed(is, block, metadata);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getDigSpeed(swapper, block, metadata);
    }

    @Override
    public int getHarvestLevel(ItemStack swapper, String toolClass) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            int ret = is.getItem().getHarvestLevel(is, toolClass);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getHarvestLevel(swapper, toolClass);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack swapper, World world, Block block, int x, int y, int z, EntityLivingBase player) {

        ItemStack is = getLastStack(swapper);
        if (is != null) {
            boolean ret = is.getItem().onBlockDestroyed(is, world, block, x, y, z, player);
            putLastStack(swapper,is);
            return ret;
        }
        return super.onBlockDestroyed(swapper, world, block, x, y, z, player);
    }

    @Override
    public boolean canHarvestBlock(Block block, ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            boolean ret = is.getItem().canHarvestBlock(block, is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.canHarvestBlock(block, swapper);
    }

//endregion

//region attack/hit
    @Override
    public boolean onEntitySwing(EntityLivingBase player, ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if(is != null) {
            if (swapper.stackTagCompound.hasKey("swung") && swapper.stackTagCompound.getInteger(stringLASTTOOL) == slotSword)
                swapper.stackTagCompound.setInteger("swung",0);
            boolean ret = is.getItem().onEntitySwing(player, is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.onEntitySwing(player, swapper);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack swapper, EntityPlayer player, Entity mob) {
        ItemStack is = getStack(slotSword,swapper);
        swapper.stackTagCompound.setInteger("swung",0);
        if(is != null) {
            boolean ret = is.getItem().onLeftClickEntity(is, player, mob);
            putLastStack(swapper,is);
            return ret;
        }
        return super.onLeftClickEntity(swapper, player, mob);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack swapper, EntityPlayer player, EntityLivingBase mob) {
        ItemStack is = getStack(slotSword,swapper);
        swapper.stackTagCompound.setInteger("swung",0);
        if(is != null) {
            boolean ret = is.getItem().itemInteractionForEntity(is, player, mob);
            putLastStack(swapper,is);
            return  ret;
        }
        return super.itemInteractionForEntity(swapper, player, mob);
    }

    @Override
    public boolean hitEntity(ItemStack swapper, EntityLivingBase mob, EntityLivingBase player) {
        ItemStack is = getStack(slotSword,swapper);
        swapper.stackTagCompound.setInteger("swung",0);
        if (is != null) {
            boolean ret = is.getItem().hitEntity(is, mob, player);
            putLastStack(swapper,is);
            return ret;
        }
        return super.hitEntity(swapper, mob, player);
    }

//endregion

//region right click

    @Override
    public void onUsingTick(ItemStack swapper, EntityPlayer player, int count) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            is.getItem().onUsingTick(is, player, count);
            putLastStack(swapper,is);
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack swapper, World world, EntityPlayer player, int par4) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            is.getItem().onPlayerStoppedUsing(is, world, player, par4);
            putLastStack(swapper,is);
        }
    }

    @Override
    public EnumAction getItemUseAction(ItemStack swapper) {
        ItemStack is = getStack(slotRightClick, swapper);
        if (is != null) {
            EnumAction ret = is.getItem().getItemUseAction(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getItemUseAction(swapper);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack swapper) {
        ItemStack is = getStack(slotRightClick,swapper);
        if (is != null) {
            int ret = is.getItem().getMaxItemUseDuration(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getMaxItemUseDuration(swapper);
    }

    @Override
    public ItemStack onEaten(ItemStack swapper, World world, EntityPlayer player) {
        ItemStack is = getStack(slotRightClick,swapper);
        if (is != null) {
            ItemStack ret = is.getItem().onEaten(is, world, player);
            putLastStack(swapper,is);
            return ret;//not sure which stack this should return honestly...
        }
        return super.onEaten(swapper, world, player);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack swapper, World world, EntityPlayer player) {
        if (player.isSneaking()) {
            if (!world.isRemote)
                if (swapper!=null && swapper.hasTagCompound()) {
                    ItemStack is = getLastStack(swapper);
                    if (swapper.stackTagCompound.hasKey("ench")) {
                        if (is != null && is.hasTagCompound()) {
                            is.stackTagCompound.setTag("ench", swapper.stackTagCompound.getTag("ench"));
                        }
                        swapper.stackTagCompound.removeTag("ench");
                    }
                }
                player.openGui(Main.instance, 0, world, 0, 0, 0);
        } else {
            ItemStack is = getStack(slotRightClick, swapper);
            if (is!=null) {
                putLastStack(swapper,is.getItem().onItemRightClick(is, world, player));
            }
        }
        return swapper;
    }

    @Override
    public boolean onItemUseFirst(ItemStack swapper, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        ItemStack is = getStack(slotRightClick, swapper);
        if (is !=null){
            boolean ret;
            Item item = is.getItem();
            if (item instanceof ItemBlock){
                Block placeBlock = ((ItemBlock)item).field_150939_a;
                Block clickBlock = world.getBlock(x,y,z);
                if (clickBlock == Blocks.snow_layer) side =1;
                else if (!clickBlock.isReplaceable(world,x,y,z)){
                    ForgeDirection fSide = ForgeDirection.getOrientation(side);
                    x += fSide.offsetX;
                    y += fSide.offsetY;
                    z += fSide.offsetZ;
                }
                ret = !world.canPlaceEntityOnSide(placeBlock, x ,y,z,false,side,null,is);
            } else {
                ret = item.onItemUseFirst(is, player, world, x, y, z, side, hitX, hitY, hitZ);
            }
            putLastStack(swapper,is);
            return ret;
        }
        return false;
    }

    @Override
    public boolean onItemUse(ItemStack swapper, EntityPlayer player, World world, int x, int y, int z, int side, float clickX, float clickY, float clickZ) {
        ItemStack is = getStack(slotRightClick, swapper);
        if (is !=null) {
            Item item = is.getItem();
            boolean ret = item.onItemUse(is, player, world, x, y, z, side, clickX, clickY, clickZ);
            putLastStack(swapper,is);
            return ret;
        }
        return false;
    }

    //endregion

//region damage stuff
    @Override
    public double getDurabilityForDisplay(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            double ret = is.getItem().getDurabilityForDisplay(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getDurabilityForDisplay(swapper);
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public boolean showDurabilityBar(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if(is != null) {
            boolean ret = is.getItem().showDurabilityBar(is);
            putLastStack(swapper,is);
            return ret;
        }
        return false;
    }

    @Override
    public int getDisplayDamage(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            int ret = is.getItem().getDisplayDamage(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getDisplayDamage(swapper);
    }

    @Override
    public int getDamage(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            int ret = is.getItem().getDamage(is);
            putLastStack(swapper,is);
            return ret;
        }
        return 0;
    }

    @Override
    public int getMaxDamage(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            int ret = is.getItem().getMaxDamage(is);
            putLastStack(swapper,is);
            return ret;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isDamaged(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            boolean ret = is.getItem().isDamaged(is);
            putLastStack(swapper,is);
            return ret;
        }
        return false;
    }

    @Override
    public void setDamage(ItemStack swapper, int damage) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            is.getItem().setDamage(is, damage);
            putLastStack(swapper,is);
        }
    }

//endregion

//region icons/rendering
    @SideOnly(Side.CLIENT)
    @Override
    public FontRenderer getFontRenderer(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            FontRenderer ret = is.getItem().getFontRenderer(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getFontRenderer(swapper);
    }


    @SideOnly(Side.CLIENT)
    @Override
    public int getColorFromItemStack(ItemStack swapper, int pass) {
        ItemStack is = getLastStack(swapper);
        if (is != null && pass != 0) {
            pass --;
            int ret = is.getItem().getColorFromItemStack(swapper, pass);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getColorFromItemStack(swapper, pass);
    }

    @SideOnly(Side.CLIENT)
    @Override //called when rendering in inventory/hotbar
    public IIcon getIcon(ItemStack swapper, int renderPass) {
        ItemStack is = getLastStack(swapper);
        IIcon ret;
        if (renderPass == 0) {
            ret = outlineIcon;
        } else if (is != null) {
            if (is.getItem() instanceof ItemBlock) {
                ret = is.getItem().getIconFromDamage(is.getItemDamage());
            } else {
                ret = is.getItem().getIcon(is, renderPass - 1);
                if (ret == null ){
                    ret = is.getItem().getIconIndex(is);
                }
            }
            putLastStack(swapper,is);
        } else {
            ret = outlineIcon;
        }
        return ret;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register) {
        super.registerIcons(register);
        outlineIcon = register.registerIcon("swapper:Swapper");
    }

    @SideOnly(Side.CLIENT)
    @Override //called when held
    public IIcon getIcon(ItemStack swapper, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        ItemStack is = getLastStack(swapper);
        IIcon ret;
        if (renderPass == 0)
            return null;
        if (is != null) {
            if (is.getItem() instanceof ItemBlock) {
                ret = is.getItem().getIconFromDamage(is.getItemDamage());
            } else {
                ret = is.getItem().getIcon(is, renderPass-1, player, usingItem, useRemaining);
            }
            putLastStack(swapper,is);
        } else{
            ret = outlineIcon;
        }
        return ret;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconIndex(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is!=null) {
            IIcon ret = is.getItem().getIconIndex(is);
            putLastStack(swapper,is);
            return ret;
        }
        return outlineIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderPasses(int metadata) {
        return 8;
    }
//endregion

//region misc
    @Override
    public void addInformation(ItemStack swapper, EntityPlayer player, List data, boolean val) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            is.getItem().addInformation(is, player, data, val);
            putLastStack(swapper,is);
        }
    }

    @Override
    public void onUpdate(ItemStack swapper, World world, Entity player, int par4, boolean par5) {
        if (swapper.hasTagCompound()) {
            if (swapper.stackTagCompound.hasKey("swung")){
                int swingTime = swapper.stackTagCompound.getInteger("swung");
                if (swingTime >= 4){
                    swapper.stackTagCompound.removeTag("swung");
                } else {
                    swapper.stackTagCompound.setInteger("swung",swingTime + 1);
                }
            }
        }
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            is.getItem().onUpdate(is, world, player, par4, par5);
            putLastStack(swapper,is);
        }
    }

    @Override
    public String getPotionEffect(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            String ret = is.getItem().getPotionEffect(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getPotionEffect(swapper);
    }

    @Override
    public Multimap getAttributeModifiers(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            Multimap ret = is.getItem().getAttributeModifiers(swapper);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getAttributeModifiers(swapper);
    }

    @Override
    public boolean isItemTool(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            boolean ret = is.getItem().isItemTool(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.isItemTool(swapper);
    }

    @Override
    public EnumRarity getRarity(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            EnumRarity ret = is.getItem().getRarity(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getRarity(swapper);
    }

    @Override
    public boolean hasCustomEntity(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            boolean ret = is.getItem().hasCustomEntity(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.hasCustomEntity(swapper);
    }

    @Override
    public String getItemStackDisplayName(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null){
            String ret = "Swapper: " + is.getItem().getItemStackDisplayName(is);
            putLastStack(swapper,is);
            return ret;
        }
        return "Swapper";
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean hasEffect(ItemStack swapper, int pass) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            boolean ret = is.getItem().hasEffect(is, pass);
            putLastStack(swapper,is);
            return ret;
        }
        return super.hasEffect(swapper, pass);
    }

    @Override
    public float getSmeltingExperience(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            float ret = is.getItem().getSmeltingExperience(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getSmeltingExperience(swapper);
    }

    @Override
    public float func_150893_a(ItemStack swapper, Block block) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            float ret = is.getItem().func_150893_a(is, block);
            putLastStack(swapper,is);
            return ret;
        }
        return super.func_150893_a(swapper, block);
    }

    @Override
    public boolean hasEffect(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            boolean ret = is.getItem().hasEffect(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.hasEffect(swapper);
    }

    @Override
    public Set<String> getToolClasses(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            Set<String> ret = is.getItem().getToolClasses(is);
            putLastStack(swapper,is);
            return ret;
        }
        return super.getToolClasses(swapper);
    }
    //endregion

//region rf
    @Override
    public int receiveEnergy(ItemStack swapper, int maxReceive, boolean simulate) {
        ItemStack is = getLastStack(swapper);
        if (is != null) {
            if(is.getItem() instanceof IEnergyContainerItem){
                int  ret = ((IEnergyContainerItem) is.getItem()).receiveEnergy(is,maxReceive,simulate);
                putLastStack(swapper,is);
                return ret;
            }
        }
        return 0;
    }

    @Override
    public int extractEnergy(ItemStack swapper, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null){
            if(is.getItem() instanceof IEnergyContainerItem) {
                int ret =((IEnergyContainerItem) is.getItem()).getEnergyStored(is);
                putLastStack(swapper,is);
                return ret;
            }
        }
        return 0;
    }

    @Override
    public int getMaxEnergyStored(ItemStack swapper) {
        ItemStack is = getLastStack(swapper);
        if (is != null){
            if (is.getItem() instanceof IEnergyContainerItem){
                int ret =((IEnergyContainerItem) is.getItem()).getMaxEnergyStored(is);
                putLastStack(swapper,is);
                return ret;
            }
        }
        return 0;
    }
    //endregion

    @SubscribeEvent
    public void onPickUp(EntityItemPickupEvent event){
        for (int i =0; i!=event.entityPlayer.inventory.getSizeInventory(); i ++){
            ItemStack swapper = event.entityPlayer.inventory.getStackInSlot(i);
            if (swapper != null && swapper.getItem() == this){
                ItemInventory inv = new ItemInventory(swapper, swapperSlots);
                ItemStack swapperStack = inv.getStackInSlot(slotRightClick);
                if (swapperStack != null){
                    if (swapperStack.isItemEqual(event.item.getEntityItem()) && ItemStack.areItemStackTagsEqual(swapperStack, event.item.getEntityItem())){
                        if (swapperStack.stackSize + event.item.getEntityItem().stackSize <= swapperStack.getMaxStackSize()){
                            swapperStack.stackSize += event.item.getEntityItem().stackSize;
                            event.item.getEntityItem().stackSize = 0;
                        } else {
                            int qtyToMove = swapperStack.getMaxStackSize() - swapperStack.stackSize;
                            swapperStack.stackSize += qtyToMove;
                            event.item.getEntityItem().stackSize -= qtyToMove;
                        }
                        inv.setInventorySlotContents(slotRightClick, swapperStack);
                    }
                }
            }
        }
    }
}
