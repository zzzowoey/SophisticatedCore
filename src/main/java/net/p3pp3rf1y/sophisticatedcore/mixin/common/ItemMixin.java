package net.p3pp3rf1y.sophisticatedcore.mixin.common;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.extensions.item.SophisticatedItem;

@Mixin(Item.class)
public class ItemMixin implements SophisticatedItem {
}
