package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.extensions.item.SophisticatedItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public class ItemStackMixin implements SophisticatedItemStack {
}
