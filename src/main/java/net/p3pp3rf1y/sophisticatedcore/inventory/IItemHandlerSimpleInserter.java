package net.p3pp3rf1y.sophisticatedcore.inventory;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;

public interface IItemHandlerSimpleInserter extends SlottedStackStorage {
	// This is already in Storage interface, so we only use this interface for compatibility
	//@Override
	//long insert(ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx);
}
