package net.p3pp3rf1y.sophisticatedcore.mixin;

import io.github.fabricators_of_create.porting_lib.util.MixinHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow public Level level;

    @Inject(method = "spawnSprintParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void sophisticatedcore$addRunningEffects(CallbackInfo ci, int i, int j, int k, BlockPos blockPos, BlockState blockState) {
        if (blockState.addRunningEffects(level, blockPos, MixinHelper.cast(this))) {
            ci.cancel();
        }
    }
}
