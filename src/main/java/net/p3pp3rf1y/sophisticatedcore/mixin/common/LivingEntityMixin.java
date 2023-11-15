package net.p3pp3rf1y.sophisticatedcore.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.EntityExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.event.common.LivingEntityEvents;
import net.p3pp3rf1y.sophisticatedcore.util.MixinHelper;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(value = LivingEntity.class, priority = 500)
public abstract class LivingEntityMixin extends Entity implements EntityExtensions {
    @Shadow
    protected int lastHurtByPlayerTime;

    @Unique
    private int lootingLevel;

    public LivingEntityMixin(EntityType<?> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void sophisticatedcore$captureDrops(DamageSource damageSource, CallbackInfo ci) {
        captureDrops(new ArrayList<>());
    }

    @ModifyVariable(method = "dropAllDeathLoot", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;lastHurtByPlayerTime:I"))
    private int port_lib$grabLootingLevel(int lootingLevel) {
        this.lootingLevel = lootingLevel;
        return lootingLevel;
    }

    @Inject(method = "dropAllDeathLoot", at = @At(value = "RETURN"))
    private void sophisticatedcore$dropCapturedDrops(DamageSource damageSource, CallbackInfo ci) {
        Collection<ItemEntity> drops = this.captureDrops(null);

        boolean cancelled = LivingEntityEvents.DROPS.invoker()
				.onLivingEntityDrops(MixinHelper.cast(this), damageSource, drops, lootingLevel, lastHurtByPlayerTime > 0);
        if (!cancelled)
            drops.forEach(e -> level.addFreshEntity(e));
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void sophisticatedcore$tick(CallbackInfo ci) {
        LivingEntityEvents.TICK.invoker().onLivingEntityTick(MixinHelper.cast(this));
    }

	@Redirect(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
	public int sophisticatedcore$addLandingEffects(ServerLevel level, ParticleOptions type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed, double y, boolean onGround, BlockState state, BlockPos pos) {
		if (!state.addLandingEffects(level, pos, state, MixinHelper.cast(this), particleCount)) {
			level.sendParticles(type, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed);
		}
		return particleCount;
	}
}
