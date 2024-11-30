package net.werdei.zoomglass.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;


public class SpyglassFinder
{
    private final Item item;
    private UseController useController = null;
    private boolean canStart;
    private boolean isActiveFake;
    
    
    public SpyglassFinder(Item item)
    {
        this.item = item;
    }
    
    
    public void tick(boolean shouldBeUsing, MinecraftClient client)
    {
        if (client.interactionManager == null) return;
        
        if (shouldBeUsing && canStart && useController == null)
            useController = start(client);
        canStart = !shouldBeUsing;
        
        if (useController == null) return;
        
        useController.tick();
        if (!shouldBeUsing)
            stop();
        else if (useController.shouldStop())
            stop();
    }
    
    private UseController start(MinecraftClient client)
    {
        var player = client.player;
        if (player == null) return null;
        
        var inventory = client.player.getInventory();
        
        if (inventory.offHand.get(0).isOf(item))
            return new UseFromOffhand(client);
        
        var mainInventory = inventory.main;
        for (int i = 0; i < mainInventory.size(); ++i)
        {
            if (!mainInventory.get(i).isOf(item)) continue;
            
            if (PlayerInventory.isValidHotbarIndex(i))
                return new UseFromHotbar(client, i);
            else
                return new UseFromInventory(client, i);
        }
        
        if (player.isSpectator() || player.isInCreativeMode())
            return new UseFake(client);
        
        player.sendMessage(Text.translatable("zoomglass.nospyglass"), true);
        return null;
    }
    
    private void stop()
    {
        useController.stop();
        useController = null;
    }
    
    public boolean isActive()
    {
        return useController != null && !isActiveFake;
    }
    
    public boolean isActiveFake()
    {
        return isActiveFake;
    }
    
    
    
    // --- Slot type dependent code ---
    
    private abstract class UseController
    {
        protected final PlayerInventory inventory;
        protected final ClientPlayerEntity player;
        protected final MinecraftClient client;
        protected int selectedSlot;
        private int useServersideDelay = -1;
        private Runnable useServerside;


        public UseController(MinecraftClient client)
        {
            this.client = client;
            player = client.player;
            inventory = player.getInventory();
            selectedSlot = inventory.selectedSlot;
        }
        
        // Delay is needed in multiplayer because of the broken animation
        // when switching slots and using item at the same time (vanilla bug)
        protected final void useItemInHand(Hand hand, boolean addDelay)
        {
            useServerside = () -> client.interactionManager.interactItem(player, hand);
            
            if (addDelay && !client.isConnectedToLocalServer())
            {
                player.getStackInHand(hand).use(this.client.world, player, hand);
                useServersideDelay = 2; // make configurable, maybe?
            }
            else useServerside.run();
        }
        
        protected final boolean didSelectedSlotChange()
        {
            return inventory.selectedSlot != selectedSlot;
        }
        
        protected final boolean didHandItemChange()
        {
            return !inventory.getMainHandStack().isOf(item);
        }
        
        public void tick()
        {
            if (useServersideDelay == 0)
                useServerside.run();
            --useServersideDelay;
        }
        
        public abstract boolean shouldStop();
        
        public abstract void stop();
    }
    
    
    private class UseFromInventory extends UseController
    {
        private final int previousItemSlot;
        
        
        public UseFromInventory(MinecraftClient client, int itemSlot)
        {
            super(client);
            this.previousItemSlot = itemSlot;
            swapSlots(itemSlot, selectedSlot);
            useItemInHand(Hand.MAIN_HAND, true);
        }
        
        private void swapSlots(int inventorySlot, int hotbarSlot)
        {
            client.interactionManager.clickSlot(
                    player.playerScreenHandler.syncId,
                    inventorySlot,
                    hotbarSlot,
                    SlotActionType.SWAP,
                    player);
        }
        
        @Override
        public boolean shouldStop()
        {
            return didSelectedSlotChange() || didHandItemChange();
        }
        
        @Override
        public void stop()
        {
            swapSlots(previousItemSlot, selectedSlot);
        }
    }
    
    
    private class UseFromHotbar extends UseController
    {
        private final int slotToReturnTo;
        
        public UseFromHotbar(MinecraftClient client, int itemSlot)
        {
            super(client);
            slotToReturnTo = selectedSlot;
            selectedSlot = inventory.selectedSlot = itemSlot;
            useItemInHand(Hand.MAIN_HAND, slotToReturnTo != selectedSlot);
        }
        
        @Override
        public boolean shouldStop()
        {
            return didSelectedSlotChange() || didHandItemChange();
        }
        
        @Override
        public void stop()
        {
            if (!didSelectedSlotChange())
                inventory.selectedSlot = slotToReturnTo;
        }
    }
    
    
    private class UseFromOffhand extends UseController
    {
        
        public UseFromOffhand(MinecraftClient client)
        {
            super(client);
            useItemInHand(Hand.OFF_HAND, false);
        }
        
        @Override
        public boolean shouldStop()
        {
            return inventory.offHand.get(0).getItem() != item;
        }
        
        @Override
        public void stop() {}
    }
    
    
    private class UseFake extends UseController
    {
        public UseFake(MinecraftClient client)
        {
            super(client);
            player.playSound(SoundEvents.ITEM_SPYGLASS_USE, 1.0F, 1.0F);
            isActiveFake = true;
        }
        
        @Override
        public boolean shouldStop()
        {
            return false;
        }
        
        @Override
        public void stop()
        {
            player.playSound(SoundEvents.ITEM_SPYGLASS_STOP_USING, 1.0F, 1.0F);
            isActiveFake = false;
        }
    }
}
