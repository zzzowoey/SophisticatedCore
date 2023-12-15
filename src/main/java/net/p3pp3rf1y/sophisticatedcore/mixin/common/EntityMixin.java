package net.p3pp3rf1y.sophisticatedcore.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.util.MixinHelper;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow public Level level;

    @Inject(method = "spawnSprintParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void sophisticatedcore$addRunningEffects(CallbackInfo ci, BlockPos blockPos, BlockState blockState) {
        if (blockState.addRunningEffects(level, blockPos, MixinHelper.cast(this))) {
            ci.cancel();
        }
    }
}
