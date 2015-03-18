package com.trcx.swapper.Common;

import com.trcx.swapper.Common.Item.Swapper;
import com.trcx.swapper.Common.OpenMods.PlayerItemInventory;
import com.trcx.swapper.Main;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Created by Trcx on 3/14/2015.
 */
public class SwapperContainer extends Container {

    private InventoryPlayer invPlayer;
    private PlayerItemInventory invSwapper;

    public SwapperContainer(InventoryPlayer invPlayer, final PlayerItemInventory invSwapper){
        this.invPlayer = invPlayer;
        this.invSwapper = invSwapper;
        for (int i = 0; i!= Swapper.swapperSlots; i++){
            final int index = i;
            addSlotToContainer(new Slot(invSwapper,i, 44 + i * 18, 63){
                @Override
                public boolean isItemValid(ItemStack itemStack){
                    GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(itemStack.getItem());
                    for(int i =0; i != Main.swapperBlacklist.length; i++) {
                        if (Main.swapperBlacklist[i].equals(uid.modId + ":" + uid.name))
                            return false;
                        if (Main.swapperBlacklist[i].equals(uid.modId + ":" + uid.name + itemStack.getItemDamage()))
                            return false;
                    }
                    if (getStack() == null) {
                        return (itemStack.stackSize <= 1 || index == 4);
                    } else {
                        if (getStack().isItemEqual(itemStack) && ItemStack.areItemStacksEqual(itemStack, getStack())){
                            return index == 4;
                        } else {
                            return false;
                        }
                    }
                }
            });
        }
        bindPlayerInventory(invPlayer);
    }

    private void bindPlayerInventory(final InventoryPlayer inventoryPlayer) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
                        8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            final int currentSlot = i;
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142) {
                @Override
                public boolean canTakeStack(EntityPlayer player){
                    return currentSlot!=inventoryPlayer.currentItem;
                }
            });
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return invSwapper.isUseableByPlayer(player);
    }

    @Override // important for shift clicking
    public ItemStack transferStackInSlot(EntityPlayer player, int slotID) { // shift clicking invalid itemStacks should be fixed in later versions of forge
        //slotID = slot of the object shift clicked on
        ItemStack stack = null;
        Slot slotObject = (Slot) inventorySlots.get(slotID);
        //null checks and checks if the item can be stacked (maxStackSize > 1)
        if (slotObject != null && slotObject.getHasStack()) {
            ItemStack stackInSlot = slotObject.getStack();
            stack = stackInSlot.copy();

            if (slotID < Swapper.swapperSlots) {//shift clicked from swapper
                if (!this.mergeItemStack(stackInSlot, 5, 33, true)) {
                    return null;
                }
            } else { //shift clicking into swapper
                for (int i=0; i != Swapper.swapperSlots; i ++) {
                    Slot targetSlot = (Slot)inventorySlots.get(i);
                    if (targetSlot != null) {
                        ItemStack targetStack = targetSlot.getStack();
                        if (targetSlot.isItemValid(stackInSlot)) {
                            if (i!=4) {
                                targetSlot.putStack(stackInSlot);
                                slotObject.putStack(null);
                                return null;
                            } else {
                                if (targetStack != null) {
                                    int delta = targetStack.getMaxStackSize() - targetStack.stackSize;
                                    delta = Math.min(delta, stackInSlot.stackSize);
                                    targetStack.stackSize += delta;
                                    stackInSlot.stackSize -= delta;
                                } else {
                                    targetSlot.putStack(stackInSlot);
                                    slotObject.putStack(null);
                                }
                            }
                        }
                    }
                }

            }

            if (stackInSlot.stackSize == 0) {
                slotObject.putStack(null);
            } else {
                slotObject.onSlotChanged();
            }

            if (stackInSlot.stackSize == stack.stackSize) {
                return null;
            }
            slotObject.onPickupFromSlot(player, stackInSlot);
        }
        return stack;
    }
}
