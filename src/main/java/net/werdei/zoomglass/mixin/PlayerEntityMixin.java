package net.werdei.zoomglass.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.player.Player;
import net.werdei.zoomglass.client.ZoomglassClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerEntityMixin
{
    @ModifyReturnValue(method = "isScoping", at=@At("RETURN"))
    public boolean forceSpyglass(boolean original)
    {
        return original || ZoomglassClient.isFakeSpyglassActive();
    }
}
