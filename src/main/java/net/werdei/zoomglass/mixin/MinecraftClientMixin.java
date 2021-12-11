package net.werdei.zoomglass.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.werdei.zoomglass.client.ZoomglassClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 2))
    private boolean dontCancelQuickSpyglass(KeyBinding keyUse)
    {
        return keyUse.isPressed() || ZoomglassClient.isQuickSpyglassActive();
    }
}
