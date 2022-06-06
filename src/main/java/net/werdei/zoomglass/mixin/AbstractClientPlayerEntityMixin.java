package net.werdei.zoomglass.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.werdei.zoomglass.client.ZoomglassClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin
{
    @Inject(method = "getFovMultiplier", at=@At("HEAD"), cancellable = true)
    public void forceSpyglassFov(CallbackInfoReturnable<Float> cir)
    {
        if (ZoomglassClient.isFakeSpyglassActive())
            cir.setReturnValue(0.1F);
    }
}
