package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    private boolean handled;
    @Shadow protected abstract boolean checkHotbarKeyPressed(int keyCode, int scanCode);

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;checkHotbarKeyPressed(II)Z"))
    private boolean sophisticatedcore$checkHotbarKeyPressed$keyPressed(AbstractContainerScreen instance, int keyCode, int scanCode) {
        return (handled = checkHotbarKeyPressed(keyCode, scanCode));
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", shift = At.Shift.AFTER))
    private void sophisticatedcore$slotClicked$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed", at = @At(value = "TAIL"), cancellable = true)
    private void sophisticatedcore$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(handled);
        cir.cancel();
    }
}
