package net.p3pp3rf1y.sophisticatedcore.inventory;

import org.apache.commons.lang3.NotImplementedException;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class FilteredItemHandler<T extends SlottedStorage<ItemVariant>> implements SlottedStorage<ItemVariant> {
	protected final T inventoryHandler;
	protected final List<FilterLogic> inputFilters;
	private final List<FilterLogic> outputFilters;

	public FilteredItemHandler(T inventoryHandler, List<FilterLogic> inputFilters, List<FilterLogic> outputFilters) {
		this.inventoryHandler = inventoryHandler;
		this.inputFilters = inputFilters;
		this.outputFilters = outputFilters;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return inventoryHandler.iterator();
	}

	@Override
	public int getSlotCount() {
		return inventoryHandler.getSlotCount();
	}

/*	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventoryHandler.getStackInSlot(slot);
	}

	@Override
	public void setStackInSlot(int i, @NotNull ItemStack itemStack) {
		inventoryHandler.setStackInSlot(i, itemStack);
	}*/

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext ctx) {
		if (inputFilters.isEmpty()) {
			return inventoryHandler.insert(resource, maxAmount, ctx);
		}

		ItemStack stack = resource.toStack((int) maxAmount);
		for (FilterLogic filter : inputFilters) {
			if (filter.matchesFilter(stack)) {
				return inventoryHandler.insert(resource, maxAmount, ctx);
			}
		}

		return 0;
	}

/*	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext ctx) {
		if (inputFilters.isEmpty()) {
			return inventoryHandler.insertSlot(slot, resource, maxAmount, ctx);
		}

		ItemStack stack = resource.toStack((int) maxAmount);
		for (FilterLogic filter : inputFilters) {
			if (filter.matchesFilter(stack)) {
				return inventoryHandler.insertSlot(slot, resource, maxAmount, ctx);
			}
		}

		return 0;
	}*/

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext ctx) {
		if (outputFilters.isEmpty()) {
			return inventoryHandler.extract(resource, maxAmount, ctx);
		}

		ItemStack stack = resource.toStack((int) maxAmount);
		for (FilterLogic filter : outputFilters) {
			if (filter.matchesFilter(stack)) {
				return inventoryHandler.extract(resource, maxAmount, ctx);
			}
		}
		return 0;
	}

/*	@Override
	public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext ctx) {
		if (outputFilters.isEmpty()) {
			return inventoryHandler.extractSlot(slot, resource, maxAmount, ctx);
		}

		for (FilterLogic filter : outputFilters) {
			if (filter.matchesFilter(getStackInSlot(slot))) {
				return inventoryHandler.extractSlot(slot, resource, maxAmount, ctx);
			}
		}
		return 0;
	}*/

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return inventoryHandler.getSlot(slot);
	}

/*	@Override
	public int getSlotLimit(int slot) {
		return inventoryHandler.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource) {
		return inventoryHandler.isItemValid(slot, resource);
	}*/

	public static class Modifiable extends FilteredItemHandler<ITrackedContentsItemHandler> implements ITrackedContentsItemHandler {
		public Modifiable(ITrackedContentsItemHandler inventoryHandler, List<FilterLogic> inputFilters, List<FilterLogic> outputFilters) {
			super(inventoryHandler, inputFilters, outputFilters);
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			throw new NotImplementedException();
		}

		@Override
		public void setStackInSlot(int slot, @NotNull ItemStack stack) {
			throw new NotImplementedException();
		}

/*		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			inventoryHandler.setStackInSlot(slot, stack);
		}*/

		@Override
		public long insert(ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx) {
			if (inputFilters.isEmpty()) {
				return inventoryHandler.insert(resource, maxAmount, ctx);
			}

			for (FilterLogic filter : inputFilters) {
				if (filter.matchesFilter(resource.toStack((int) maxAmount))) {
					return inventoryHandler.insert(resource, maxAmount, ctx);
				}
			}

			return 0;
		}

		@Override
		public Set<ItemStackKey> getTrackedStacks() {
			Set<ItemStackKey> ret = new HashSet<>();

			inventoryHandler.getTrackedStacks().forEach(ts -> {
				if (inputFiltersMatchStack(ts.stack())) {
					ret.add(ts);
				}
			});

			return ret;
		}

		private boolean inputFiltersMatchStack(ItemStack stack) {
			for (FilterLogic filter : inputFilters) {
				if (filter.matchesFilter(stack)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void registerTrackingListeners(Consumer<ItemStackKey> onAddStackKey, Consumer<ItemStackKey> onRemoveStackKey, Runnable onAddFirstEmptySlot, Runnable onRemoveLastEmptySlot) {
			inventoryHandler.registerTrackingListeners(
					isk -> {
						if (inputFiltersMatchStack(isk.stack())) {
							onAddStackKey.accept(isk);
						}
					},
					isk -> {
						if (inputFiltersMatchStack(isk.stack())) {
							onRemoveStackKey.accept(isk);
						}
					},
					onAddFirstEmptySlot,
					onRemoveLastEmptySlot
			);
		}

		@Override
		public void unregisterStackKeyListeners() {
			inventoryHandler.unregisterStackKeyListeners();
		}

		@Override
		public boolean hasEmptySlots() {
			return inventoryHandler.hasEmptySlots();
		}

		@Override
		public int getInternalSlotLimit(int slot) {
			return inventoryHandler.getInternalSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, ItemVariant resource) {
			return inventoryHandler.isItemValid(slot, resource);
		}

		@Override
		public int getSlotLimit(int slot) {
			return inventoryHandler.getSlotLimit(slot);
		}
	}
}
