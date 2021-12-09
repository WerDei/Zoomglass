package net.werdei.zoomglass.mixin;

import net.minecraft.client.option.KeyBinding;
import net.werdei.zoomglass.extensions.KeyBindingExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class KeyBindingMixin implements KeyBindingExtensions
{
    @Unique
    private boolean overridePressed;

    @Shadow
    private int timesPressed;


    @Inject(method = "isPressed", at = @At("RETURN"), cancellable = true)
    private void forcePressUseKeyBinding(CallbackInfoReturnable<Boolean> cir)
    {
        if (overridePressed)
            cir.setReturnValue(true);
    }

    @Override
    public void setOverridePressed(boolean overridePressed)
    {
        if (!this.overridePressed && overridePressed)
            ++timesPressed;
        this.overridePressed = overridePressed;
    }
}
