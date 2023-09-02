package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.extensions.client.gui.screens.inventory.SophisticatedAbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements SophisticatedAbstractContainerScreen {
    @Shadow
    int leftPos;

    @Shadow
    int topPos;

    @Override
    public int getGuiLeft() {
        return leftPos;
    }

    @Override
    public int getGuiTop() {
        return topPos;
    }

    private AbstractContainerScreen getSelf() {
        return (AbstractContainerScreen)(Object)this;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
    private int sophisticatedcore$MenuSlotSize(NonNullList instance) {
        if (getSelf() instanceof StorageScreenBase) {
            return StorageContainerMenuBase.NUMBER_OF_PLAYER_SLOTS;
        }

        return instance.size();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;"))
    private Object sophisticatedcore$MenuSlotGet(NonNullList<Slot> instance, int i) {
        if (getSelf() instanceof StorageScreenBase) {
            return getSelf().getMenu().getSlot(((StorageScreenBase<? extends StorageContainerMenuBase<?>>)getSelf()).getMenu().getInventorySlotsSize() - StorageContainerMenuBase.NUMBER_OF_PLAYER_SLOTS + i);
        }

        return instance.get(i);
    }
}
