package net.p3pp3rf1y.sophisticatedcore.upgrades;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter;

import javax.annotation.Nullable;

public interface IInsertResponseUpgrade {
	long onBeforeInsert(IItemHandlerSimpleInserter inventoryHandler, int slot, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx);
//	ItemStack onBeforeInsert(IItemHandlerSimpleInserter inventoryHandler, int slot, ItemStack stack, boolean simulate);

	void onAfterInsert(IItemHandlerSimpleInserter inventoryHandler, int slot, @Nullable TransactionContext ctx);
}
