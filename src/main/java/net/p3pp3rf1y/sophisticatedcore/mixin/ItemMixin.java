package net.p3pp3rf1y.sophisticatedcore.mixin;

import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.extensions.item.SophisticatedItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin implements SophisticatedItem {
}
