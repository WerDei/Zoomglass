package net.werdei.zoomglass.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.werdei.zoomglass.extensions.KeyBindingExtensions;

public class SlotSwapper
{
    private final Item item;
    private final Text noItemMessage;

    private boolean wasSwapRequested;
    private SwapController swapController = null;


    public SlotSwapper(Item item, Text noItemMessage)
    {
        this.item = item;
        this.noItemMessage = noItemMessage;
    }

    public void tick(boolean isSwapRequested, MinecraftClient client)
    {
        if (client.player == null | client.interactionManager == null) return;

        if (!wasSwapRequested && isSwapRequested)
            swapStart(client);
        else if (wasSwapRequested && !isSwapRequested)
            swapFinish();
        else if (isSwapped() && swapController.shouldSwapBack())
            swapFinish();
        wasSwapRequested = isSwapRequested;
    }

    private void swapStart(MinecraftClient client)
    {
        if (isSwapped()) return;

        var itemSlotId = client.player.getInventory().getSlotWithStack(new ItemStack(item));
        if (itemSlotId == -1)
        {
            client.player.sendMessage(noItemMessage, true);
            return;
        }

        if (PlayerInventory.isValidHotbarIndex(itemSlotId))
            swapController = new SwapFromHotbar(client, itemSlotId);
        else
            swapController = new SwapFromInventory(client, itemSlotId);

    }

    private void swapFinish()
    {
        if (!isSwapped()) return;

        swapController.swapBack();
        swapController = null;
    }

    public boolean isSwapped()
    {
        return swapController != null;
    }



    private static void swapSlots(int inventorySlot, int hotbarSlot, MinecraftClient client)
    {
        System.out.println(inventorySlot + " " + hotbarSlot);
        client.interactionManager.clickSlot(
                client.player.playerScreenHandler.syncId,
                inventorySlot,
                hotbarSlot,
                SlotActionType.SWAP,
                client.player);
    }

    private static void setForceUse(boolean value, MinecraftClient client)
    {
        ((KeyBindingExtensions) client.options.keyUse).setOverridePressed(value);
    }



    // Swap logic

    private abstract class SwapController
    {
        protected PlayerInventory inventory;
        protected ClientPlayerEntity player;
        protected MinecraftClient client;
        protected int itemSlotId;
        protected int selectedSlotId;

        public SwapController(MinecraftClient client, int itemSlotId)
        {
            this.client = client;
            player = client.player;
            inventory = player.getInventory();
            this.itemSlotId = itemSlotId;
            this.selectedSlotId = inventory.selectedSlot;
        }

        public boolean shouldSwapBack()
        {
            return didSelectedSlotChange() || didHandItemChange();
        }

        public abstract void swapBack();

        protected final void startForceUseItem()
        {
            client.interactionManager.interactItem(player, player.world, Hand.MAIN_HAND);
            setForceUse(true, client);
        }

        protected final void stopForceUseItem()
        {
            setForceUse(false, client);
        }

        protected final boolean didSelectedSlotChange()
        {
            return inventory.selectedSlot != selectedSlotId;
        }

        protected final boolean didHandItemChange()
        {
            return inventory.getMainHandStack().getItem() != item;
        }
    }


    private class SwapFromInventory extends SwapController
    {

        public SwapFromInventory(MinecraftClient client, int itemSlotId)
        {
            super(client, itemSlotId);
            swapSlots(itemSlotId, selectedSlotId, client);
            startForceUseItem();
        }

        @Override
        public void swapBack()
        {
            stopForceUseItem();
            swapSlots(itemSlotId, selectedSlotId, client);
        }
    }


    private class SwapFromHotbar extends SwapController
    {
        private final int previousSelectedSlotId;

        public SwapFromHotbar(MinecraftClient client, int itemSlotId)
        {
            super(client, itemSlotId);
            previousSelectedSlotId = selectedSlotId;
            selectedSlotId = inventory.selectedSlot = itemSlotId;
            startForceUseItem();
        }

        @Override
        public void swapBack()
        {
            stopForceUseItem();
            if (!didSelectedSlotChange())
                inventory.selectedSlot = previousSelectedSlotId;
        }
    }
}
