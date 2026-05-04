package net.werdei.zoomglass.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;


@Environment(net.fabricmc.api.EnvType.CLIENT)
public class ZoomglassClient implements ClientModInitializer
{
    private static KeyMapping zoomglassKeybinding;
    private static SpyglassFinder spyglassFinder;
    
    @Override
    public void onInitializeClient()
    {
        zoomglassKeybinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.zoomglass.spyglass",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                KeyMapping.Category.INVENTORY
        ));
        
        spyglassFinder = new SpyglassFinder(Items.SPYGLASS);
        
        ClientTickEvents.START_CLIENT_TICK.register(client ->
                spyglassFinder.tick(zoomglassKeybinding.isDown(), client));
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