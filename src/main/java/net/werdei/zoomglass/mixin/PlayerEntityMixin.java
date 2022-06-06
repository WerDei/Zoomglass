package net.werdei.zoomglass.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.werdei.zoomglass.client.ZoomglassClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin
{
    @Inject(method = "isUsingSpyglass", at=@At("RETURN"),cancellable = true)
    public void forceSpyglass(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(cir.getReturnValue() || ZoomglassClient.isFakeSpyglassActive());
    }
}
