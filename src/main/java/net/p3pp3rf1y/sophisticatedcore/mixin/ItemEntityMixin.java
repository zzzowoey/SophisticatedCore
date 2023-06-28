package net.p3pp3rf1y.sophisticatedcore.mixin;

import io.github.fabricators_of_create.porting_lib.event.BaseEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.event.ItemEntityEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow public abstract ItemStack getItem();

    ItemEntityEvents.ItemEntityPickupEvent event;

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I", shift = At.Shift.AFTER), cancellable = true)
    private void sophisticatedcore$playerTouch(Player p, CallbackInfo ci) {
        event = new ItemEntityEvents.ItemEntityPickupEvent((ItemEntity) (Object)this, p);
        event.sendEvent();
        if (event.getResult() == BaseEvent.Result.DENY) {
            ci.cancel();
        }
    }

    @Redirect(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean sophisticatedcore$playerTouchAddItem(Inventory instance, ItemStack stack) {
        return (event.getResult() == BaseEvent.Result.ALLOW || this.getItem().getCount() <= 0 || instance.add(stack));
    }

}
