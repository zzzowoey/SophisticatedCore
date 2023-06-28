package net.p3pp3rf1y.sophisticatedcore.event;

import io.github.fabricators_of_create.porting_lib.event.BaseEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public interface ItemEntityEvents {
    Event<ItemEntityEvents> PICKUP = EventFactory.createArrayBacked(ItemEntityEvents.class, callbacks -> (event) -> {
        for (ItemEntityEvents e : callbacks) {
            e.onPickupItem(event);
            if (event.isCanceled()) {
                return;
            }
        }
    });

    void onPickupItem(ItemEntityPickupEvent event);

    class ItemEntityPickupEvent extends BaseEvent {
        private final Player player;
        private final ItemEntity itemEntity;

        public ItemEntityPickupEvent(ItemEntity itemEntity, Player player) {
            this.itemEntity = itemEntity;
            this.player = player;
        }

        public Player getPlayer() { return this.player; }
        public ItemEntity getItem() { return this.itemEntity; }

        @Override
        public void sendEvent() {
            ItemEntityEvents.PICKUP.invoker().onPickupItem(this);
        }
    }
}
