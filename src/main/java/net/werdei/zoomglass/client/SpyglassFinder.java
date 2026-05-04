package net.werdei.zoomglass.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;


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
    
    
    public void tick(boolean shouldBeUsing, Minecraft client)
    {
        if (client.gameMode == null) return;
        
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
    
    private UseController start(Minecraft client)
    {
        var player = client.player;
        if (player == null) return null;
        
        var inventory = player.getInventory();
        
        if (player.getOffhandItem().is(item))
            return new UseFromOffhand(client);
        
        var mainInventory = inventory.getNonEquipmentItems();
        for (int i = 0; i < mainInventory.size(); ++i)
        {
            if (!mainInventory.get(i).is(item)) continue;
            
            if (Inventory.isHotbarSlot(i))
                return new UseFromHotbar(client, i);
            else
                return new UseFromInventory(client, i);
        }
        
        if (player.isSpectator() || player.hasInfiniteMaterials())
            return new UseFake(client);
        
        player.sendOverlayMessage(Component.translatable("zoomglass.nospyglass"));
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
        protected final Inventory inventory;
        protected final LocalPlayer player;
        protected final Minecraft client;
        protected int selectedSlot;
        private int useServersideDelay = -1;
        private Runnable useServerside;


        public UseController(Minecraft client)
        {
            this.client = client;
            player = client.player;
            inventory = player.getInventory();
            selectedSlot = inventory.getSelectedSlot();
        }
        
        // Delay is needed in multiplayer because of the broken animation
        // when switching slots and using item at the same time (vanilla bug)
        protected final void useItemInHand(InteractionHand hand, boolean addDelay)
        {
            useServerside = () -> client.gameMode.useItem(player, hand);
            
            if (addDelay && !client.isSingleplayer())
            {
                player.getItemInHand(hand).use(this.client.level, player, hand);
                useServersideDelay = 2; // make configurable, maybe?
            }
            else useServerside.run();
        }
        
        protected final boolean didSelectedSlotChange()
        {
            return inventory.getSelectedSlot() != selectedSlot;
        }
        
        protected final boolean didHandItemChange()
        {
            return !player.getUseItem().is(item);
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
        
        
        public UseFromInventory(Minecraft client, int itemSlot)
        {
            super(client);
            this.previousItemSlot = itemSlot;
            swapSlots(itemSlot, selectedSlot);
            useItemInHand(InteractionHand.MAIN_HAND, true);
        }
        
        private void swapSlots(int inventorySlot, int hotbarSlot)
        {
            client.gameMode.handleContainerInput(
                    player.inventoryMenu.containerId,
                    inventorySlot,
                    hotbarSlot,
                    ContainerInput.SWAP,
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
        
        public UseFromHotbar(Minecraft client, int itemSlot)
        {
            super(client);
            slotToReturnTo = selectedSlot;
            inventory.setSelectedSlot(selectedSlot = itemSlot);
            useItemInHand(InteractionHand.MAIN_HAND, slotToReturnTo != selectedSlot);
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
                inventory.setSelectedSlot(slotToReturnTo);
        }
    }
    
    
    private class UseFromOffhand extends UseController
    {
        
        public UseFromOffhand(Minecraft client)
        {
            super(client);
            useItemInHand(InteractionHand.OFF_HAND, false);
        }
        
        @Override
        public boolean shouldStop()
        {
            return !player.getOffhandItem().is(item);
        }
        
        @Override
        public void stop() {}
    }
    
    
    private class UseFake extends UseController
    {
        public UseFake(Minecraft client)
        {
            super(client);
            player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
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
            player.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
            isActiveFake = false;
        }
    }
}
