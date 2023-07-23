package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.p3pp3rf1y.sophisticatedcore.extensions.client.gui.screens.inventory.SophisticatedAbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin implements SophisticatedAbstractContainerScreen {
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
}
