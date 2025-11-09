package net.werdei.zoomglass.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;


@Environment(net.fabricmc.api.EnvType.CLIENT)
public class ZoomglassClient implements ClientModInitializer
{
    private static KeyBinding zoomglassKeybinding;
    private static SpyglassFinder spyglassFinder;
    
    @Override
    public void onInitializeClient()
    {
        zoomglassKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zoomglass.spyglass",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                KeyBinding.Category.INVENTORY
        ));
        
        spyglassFinder = new SpyglassFinder(Items.SPYGLASS);
        
        ClientTickEvents.START_CLIENT_TICK.register(client ->
                spyglassFinder.tick(zoomglassKeybinding.isPressed(), client));
    }
    
    public static boolean isQuickSpyglassActive()
    {
        return spyglassFinder.isActive();
    }
    
    public static boolean isFakeSpyglassActive()
    {
        return spyglassFinder.isActiveFake();
    }
}