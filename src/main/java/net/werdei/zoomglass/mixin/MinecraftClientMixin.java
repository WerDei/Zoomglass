package net.werdei.zoomglass.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.werdei.zoomglass.client.ZoomglassClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
    @ModifyExpressionValue(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 2))
    private boolean dontCancelQuickSpyglass(boolean original)
    {
        return original || ZoomglassClient.isQuickSpyglassActive();
    }
}
