package net.werdei.zoomglass.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class ZoomglassClient implements ClientModInitializer
{
    private static KeyBinding zoomglassKeybinding;

    private static SlotSwapper spyglassSlotSwapper;

    @Override
    public void onInitializeClient()
    {
        zoomglassKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zoomglass.spyglass",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "key.categories.inventory"
        ));

        spyglassSlotSwapper = new SlotSwapper(Items.SPYGLASS, new TranslatableText("zoomglass.nospyglass"));

        ClientTickEvents.END_CLIENT_TICK.register(client ->
                spyglassSlotSwapper.tick(zoomglassKeybinding.isPressed(), client));
    }

    public static boolean isQuickSpyglassActive()
    {
        return spyglassSlotSwapper.isSwapped();
    }
}
