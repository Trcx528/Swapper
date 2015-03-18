package com.trcx.swapper.Common.OpenMods;

import com.google.common.base.Preconditions;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemInventory extends GenericInventory {

    public static final String TAG_INVENTORY = "inventory";

    protected final ItemStack containerStack;

    public ItemInventory(ItemStack containerStack, int size) {
        super("", false, size);
        Preconditions.checkNotNull(containerStack);
        this.containerStack = containerStack;
        if (!containerStack.hasTagCompound())
            containerStack.stackTagCompound = new NBTTagCompound();
        final NBTTagCompound tag  = containerStack.stackTagCompound;
        readFromNBT(getInventoryTag(tag));

    }

    @Override
    public void onInventoryChanged(int slotNumber) {
        super.onInventoryChanged(slotNumber);
        if (!containerStack.hasTagCompound())
            containerStack.stackTagCompound = new NBTTagCompound();
        NBTTagCompound tag = containerStack.stackTagCompound;
        NBTTagCompound inventoryTag = getInventoryTag(tag);
        writeToNBT(inventoryTag);
        tag.setTag(TAG_INVENTORY, inventoryTag);
        containerStack.setTagCompound(tag);
    }

    public static NBTTagCompound getInventoryTag(NBTTagCompound tag) {
        return tag.getCompoundTag(TAG_INVENTORY);
    }

}