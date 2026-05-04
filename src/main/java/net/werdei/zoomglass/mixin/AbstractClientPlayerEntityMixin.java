package net.werdei.zoomglass.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.SpyglassItem;
import net.werdei.zoomglass.client.ZoomglassClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerEntityMixin
{
    @ModifyReturnValue(method = "getFieldOfViewModifier", at=@At("RETURN"))
    public float forceSpyglassFov(float original, boolean firstPerson)
    {
        if (firstPerson && ZoomglassClient.isFakeSpyglassActive())
            return SpyglassItem.ZOOM_FOV_MODIFIER;
        return original;
    }
}
