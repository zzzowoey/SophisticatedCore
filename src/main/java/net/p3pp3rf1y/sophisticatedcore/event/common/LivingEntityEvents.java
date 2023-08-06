package net.p3pp3rf1y.sophisticatedcore.event.common;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.Collection;

public class LivingEntityEvents {
    public static final Event<Drops> DROPS = EventFactory.createArrayBacked(Drops.class, callbacks -> (target, source, drops, lootingLevel, recentlyHit) -> {
        for (Drops callback : callbacks) {
            if (callback.onLivingEntityDrops(target, source, drops, lootingLevel, recentlyHit)) {
                return true;
            }
        }

        return false;
    });

    public static final Event<Tick> TICK = EventFactory.createArrayBacked(Tick.class, callbacks -> (entity) -> {
        for (Tick callback : callbacks) {
            callback.onLivingEntityTick(entity);
        }
    });

    @FunctionalInterface
    public interface Drops {
        boolean onLivingEntityDrops(LivingEntity target, DamageSource source, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit);
    }

    @FunctionalInterface
    public interface Tick {
        void onLivingEntityTick(LivingEntity entity);
    }
}