package com.trcx.swapper.Common.OpenMods;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Arrays;
import java.util.List;

public class GenericInventory implements IInventory {

    protected String inventoryTitle;
    protected int slotsCount;
    protected ItemStack[] inventoryContents;
    protected boolean isInvNameLocalized;

    public GenericInventory(String name, boolean isInvNameLocalized, int size) {
        this.isInvNameLocalized = isInvNameLocalized;
        this.slotsCount = size;
        this.inventoryTitle = name;
        this.inventoryContents = new ItemStack[size];
    }

    @Override
    public ItemStack decrStackSize(int par1, int par2)
    {
        if (this.inventoryContents[par1] != null)
        {
            ItemStack itemstack;

            if (this.inventoryContents[par1].stackSize <= par2)
            {
                itemstack = this.inventoryContents[par1];
                this.inventoryContents[par1] = null;
                onInventoryChanged(par1);
                return itemstack;
            }
            itemstack = this.inventoryContents[par1].splitStack(par2);
            if (this.inventoryContents[par1].stackSize == 0)
            {
                this.inventoryContents[par1] = null;
            }

            onInventoryChanged(par1);
            return itemstack;
        }
        return null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public int getSizeInventory() {
        return slotsCount;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return this.inventoryContents[i];
    }

    public ItemStack getStackInSlot(Enum<?> i) {
        return getStackInSlot(i.ordinal());
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        if (i >= this.inventoryContents.length) { return null; }
        if (this.inventoryContents[i] != null) {
            ItemStack itemstack = this.inventoryContents[i];
            this.inventoryContents[i] = null;
            return itemstack;
        }
        return null;
    }

    public boolean isItem(int slot, Item item) {
        return inventoryContents[slot] != null
                && inventoryContents[slot].getItem() == item;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return true;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return true;
    }

    public void onInventoryChanged(int slotNumber) { }

    public void clearAndSetSlotCount(int amount) {
        this.slotsCount = amount;
        inventoryContents = new ItemStack[amount];
        onInventoryChanged(0);
    }

    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("size")) {
            this.slotsCount = tag.getInteger("size");
        }
        NBTTagList nbttaglist = tag.getTagList("Items", 10);
        inventoryContents = new ItemStack[slotsCount];
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound stacktag = nbttaglist.getCompoundTagAt(i);
            int j = stacktag.getByte("Slot");
            if (j >= 0 && j < inventoryContents.length) {
                inventoryContents[j] = ItemStack.loadItemStackFromNBT(stacktag);
            }
        }
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        this.inventoryContents[i] = itemstack;

        if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
            itemstack.stackSize = getInventoryStackLimit();
        }

        onInventoryChanged(i);
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("size", getSizeInventory());
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < inventoryContents.length; i++) {
            if (inventoryContents[i] != null) {
                NBTTagCompound stacktag = new NBTTagCompound();
                stacktag.setByte("Slot", (byte)i);
                inventoryContents[i].writeToNBT(stacktag);
                nbttaglist.appendTag(stacktag);
            }
        }
        tag.setTag("Items", nbttaglist);
    }

    /**
     * This bastard never even gets called, so don't rely on it.
     */
    @Override
    public void markDirty() {
        onInventoryChanged(0);
    }

    public void copyFrom(IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (i < getSizeInventory()) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack != null) {
                    setInventorySlotContents(i, stack.copy());
                } else {
                    setInventorySlotContents(i, null);
                }
            }
        }
    }

    public List<ItemStack> contents() {
        return Arrays.asList(inventoryContents);
    }

    @Override
    public String getInventoryName() {
        return this.inventoryTitle;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return this.isInvNameLocalized;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}
}