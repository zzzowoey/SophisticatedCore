package net.p3pp3rf1y.sophisticatedcore.inventory;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage;
import net.minecraft.world.item.ItemStack;

public interface IItemHandlerSimpleInserter extends SlotExposedStorage {
	ItemStack insertItem(ItemStack stack, boolean simulate);
}
