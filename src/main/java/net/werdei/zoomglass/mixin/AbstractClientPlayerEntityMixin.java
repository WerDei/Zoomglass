package net.werdei.zoomglass.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.SpyglassItem;
import net.werdei.zoomglass.client.ZoomglassClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin
{
    @ModifyReturnValue(method = "getFovMultiplier", at=@At("RETURN"))
    public float forceSpyglassFov(float original, boolean firstPerson)
    {
        if (firstPerson && ZoomglassClient.isFakeSpyglassActive())
            return SpyglassItem.FOV_MULTIPLIER;
        return original;
    }
}
