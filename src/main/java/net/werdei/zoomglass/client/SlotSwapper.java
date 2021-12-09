package net.werdei.zoomglass.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.werdei.zoomglass.extensions.KeyBindingExtensions;

public class SlotSwapper
{
    private final Item item;
    private final Text noItemMessage;

    private boolean shouldBeSwapped;
    private boolean isSwapped;
    private int swappedItemSlotId;
    private int selectedSlotId;

    private PlayerInventory inventory;
    private ClientPlayerEntity player;

    public SlotSwapper(Item item, Text noItemMessage)
    {
        this.item = item;
        this.noItemMessage = noItemMessage;
    }

    public void tick(boolean swapState, MinecraftClient client)
    {
        if (client.player == null | client.interactionManager == null) return;

        if (!shouldBeSwapped && swapState)
            swapSlotStart(client);
        else if (shouldBeSwapped && !swapState)
            swapSlotFinish(client);
        else if (isSwapped)
            tickWhileSwapped(client);
        shouldBeSwapped = swapState;
    }

    private void tickWhileSwapped(MinecraftClient client)
    {
        if (selectedSlotId != inventory.selectedSlot ||
                inventory.getStack(selectedSlotId).getItem() != item)
            swapSlotFinish(client);
    }

    private void swapSlotStart(MinecraftClient client)
    {
        if (isSwapped) return;

        player = client.player;
        inventory = player.getInventory();

        swappedItemSlotId = inventory.getSlotWithStack(new ItemStack(item));
        if (swappedItemSlotId == -1)
        {
            client.player.sendMessage(noItemMessage, true);
            return;
        }
        selectedSlotId = inventory.selectedSlot;
        swapSlots(swappedItemSlotId, selectedSlotId, client);
        setForceUse(true, client);
        //client.interactionManager.interactItem(player, player.world, Hand.MAIN_HAND);
        isSwapped = true;
    }

    private void swapSlotFinish(MinecraftClient client)
    {
        if (!isSwapped) return;


        setForceUse(false, client);
        swapSlots(swappedItemSlotId, selectedSlotId, client);
        isSwapped = false;
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
}
