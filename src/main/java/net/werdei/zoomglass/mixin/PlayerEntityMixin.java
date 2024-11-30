package net.werdei.zoomglass.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.player.PlayerEntity;
import net.werdei.zoomglass.client.ZoomglassClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin
{
    @ModifyReturnValue(method = "isUsingSpyglass", at=@At("RETURN"))
    public boolean forceSpyglass(boolean original)
    {
        return original || ZoomglassClient.isFakeSpyglassActive();
    }
}
