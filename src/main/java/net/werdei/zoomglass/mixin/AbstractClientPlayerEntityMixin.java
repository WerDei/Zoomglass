package net.werdei.zoomglass.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.werdei.zoomglass.client.ZoomglassClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin
{
    @ModifyReturnValue(method = "getFovMultiplier", at=@At("RETURN"))
    public float forceSpyglassFov(float original)
    {
        if (ZoomglassClient.isFakeSpyglassActive())
            return 0.1f;
        return original;
    }
}
