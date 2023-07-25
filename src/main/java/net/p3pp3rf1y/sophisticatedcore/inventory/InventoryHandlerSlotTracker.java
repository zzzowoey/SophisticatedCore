package net.p3pp3rf1y.sophisticatedcore.inventory;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class InventoryHandlerSlotTracker implements ISlotTracker {
	private final Map<ItemStackKey, Set<Integer>> fullStackSlots = new HashMap<>();
	private final Map<Integer, ItemStackKey> fullSlotStacks = new HashMap<>();
	private final Map<ItemStackKey, Set<Integer>> partiallyFilledStackSlots = new HashMap<>();
	private final Map<Integer, ItemStackKey> partiallyFilledSlotStacks = new HashMap<>();
	private final Map<Item, Set<ItemStackKey>> itemStackKeys = new HashMap<>();
	private final Set<Integer> emptySlots = new TreeSet<>();
	private final MemorySettingsCategory memorySettings;
	private Map<Item, Set<Integer>> filterItemSlots;
	private Consumer<ItemStackKey> onAddStackKey = sk -> {};
	private Consumer<ItemStackKey> onRemoveStackKey = sk -> {};

	private Runnable onAddFirstEmptySlot = () -> {};
	private Runnable onRemoveLastEmptySlot = () -> {};

	private BooleanSupplier shouldInsertIntoEmpty = () -> true;

	public InventoryHandlerSlotTracker(MemorySettingsCategory memorySettings, Map<Item, Set<Integer>> filterItemSlots) {
		this.memorySettings = memorySettings;
		this.filterItemSlots = filterItemSlots;
	}

	@Override
	public void setShouldInsertIntoEmpty(BooleanSupplier shouldInsertIntoEmpty) {
		this.shouldInsertIntoEmpty = shouldInsertIntoEmpty;
	}

	public void addPartiallyFilled(int slot, ItemStack stack) {
		ItemStackKey stackKey = new ItemStackKey(stack);
		partiallyFilledStackSlots.computeIfAbsent(stackKey, k -> {
			if (!fullStackSlots.containsKey(k)) {
				onAddStackKey.accept(k);
			}
			return new TreeSet<>();
		}).add(slot);
		partiallyFilledSlotStacks.put(slot, stackKey);
		itemStackKeys.computeIfAbsent(stack.getItem(), i -> new HashSet<>()).add(stackKey);
	}

	@Override
	public Set<ItemStackKey> getFullStacks() {
		return fullStackSlots.keySet();
	}

	@Override
	public Set<ItemStackKey> getPartialStacks() {
		return partiallyFilledStackSlots.keySet();
	}

	@Override
	public Set<Item> getItems() {
		return itemStackKeys.keySet();
	}

	public void addFull(int slot, ItemStack stack) {
		ItemStackKey stackKey = new ItemStackKey(stack);
		fullStackSlots.computeIfAbsent(stackKey, k -> {
			if (!partiallyFilledStackSlots.containsKey(k)) {
				onAddStackKey.accept(k);
			}
			return new HashSet<>();
		}).add(slot);
		fullSlotStacks.put(slot, stackKey);
		itemStackKeys.computeIfAbsent(stack.getItem(), i -> new HashSet<>()).add(stackKey);
	}

	public void removePartiallyFilled(int slot) {
		if (partiallyFilledSlotStacks.containsKey(slot)) {
			ItemStackKey stackKey = partiallyFilledSlotStacks.remove(slot);
			@Nullable
			Set<Integer> partialSlots = partiallyFilledStackSlots.get(stackKey);
			if (partialSlots == null) {
				SophisticatedCore.LOGGER.error("Unstable ItemStack detected in slot tracking: {}", () -> stackKey != null ? stackKey.stack().toString() : "null");
			} else {
				partialSlots.remove(slot);
			}
			if (partialSlots == null || partialSlots.isEmpty()) {
				partiallyFilledStackSlots.remove(stackKey);
				if (!fullStackSlots.containsKey(stackKey)) {
					onStackKeyRemoved(stackKey);
				}
			}
		}
	}

	private void removeFull(int slot) {
		if (fullSlotStacks.containsKey(slot)) {
			ItemStackKey stackKey = fullSlotStacks.remove(slot);
			@Nullable
			Set<Integer> fullSlots = fullStackSlots.get(stackKey);
			if (fullSlots == null) {
				SophisticatedCore.LOGGER.error("Unstable ItemStack detected in slot tracking: {}", () -> stackKey != null ? stackKey.stack().toString() : "null");
			} else {
				fullSlots.remove(slot);
			}
			if (fullSlots == null || fullSlots.isEmpty()) {
				fullStackSlots.remove(stackKey);
				if (!partiallyFilledStackSlots.containsKey(stackKey)) {
					onStackKeyRemoved(stackKey);
				}
			}
		}
	}

	private void onStackKeyRemoved(ItemStackKey stackKey) {
		itemStackKeys.computeIfPresent(stackKey.getStack().getItem(), (i, stackKeys) -> {
			stackKeys.remove(stackKey);
			return stackKeys;
		});
		if (itemStackKeys.containsKey(stackKey.getStack().getItem()) && itemStackKeys.get(stackKey.getStack().getItem()).isEmpty()) {
			itemStackKeys.remove(stackKey.getStack().getItem());
		}

		onRemoveStackKey.accept(stackKey);
	}

	@Override
	public void removeAndSetSlotIndexes(InventoryHandler inventoryHandler, int slot, ItemStack stack) {
		if (stack.isEmpty()) {
			removePartiallyFilled(slot);
			removeFull(slot);
			addEmptySlot(slot);
			return;
		}

		if (emptySlots.contains(slot)) {
			removeEmpty(slot);
		}

		if (isPartiallyFilled(inventoryHandler, slot, stack)) {
			setPartiallyFilled(slot, stack);
		} else {
			setFull(slot, stack);
		}
	}

	private void setFull(int slot, ItemStack stack) {
		boolean containsSlot = fullSlotStacks.containsKey(slot);
		if (!containsSlot || fullSlotStacks.get(slot).hashCodeNotEquals(stack)) {
			if (containsSlot) {
				removeFull(slot);
			}
			addFull(slot, stack);
		}
		if (partiallyFilledSlotStacks.containsKey(slot)) {
			removePartiallyFilled(slot);
		}
	}

	private void setPartiallyFilled(int slot, ItemStack stack) {
		boolean containsSlot = partiallyFilledSlotStacks.containsKey(slot);
		if (!containsSlot || partiallyFilledSlotStacks.get(slot).hashCodeNotEquals(stack)) {
			if (containsSlot) {
				removePartiallyFilled(slot);
			}
			addPartiallyFilled(slot, stack);
		}
		if (fullSlotStacks.containsKey(slot)) {
			removeFull(slot);
		}
	}

	private void removeEmpty(int slot) {
		emptySlots.remove(slot);
		if (emptySlots.isEmpty()) {
			onRemoveLastEmptySlot.run();
		}
	}

	private void set(InventoryHandler inventoryHandler, int slot, ItemStack stack) {
		if (stack.isEmpty()) {
			addEmptySlot(slot);
		} else {
			if (isPartiallyFilled(inventoryHandler, slot, stack)) {
				addPartiallyFilled(slot, stack);
			} else {
				addFull(slot, stack);
			}
		}
	}

	private void addEmptySlot(int slot) {
		emptySlots.add(slot);
		if (emptySlots.size() == 1) {
			onAddFirstEmptySlot.run();
		}
	}

	@Override
	public void clear() {
		partiallyFilledStackSlots.clear();
		partiallyFilledSlotStacks.clear();
	}

	@Override
	public void refreshSlotIndexesFrom(InventoryHandler itemHandler) {
		fullStackSlots.keySet().forEach(sk -> onRemoveStackKey.accept(sk));
		fullStackSlots.clear();
		fullSlotStacks.clear();
		partiallyFilledStackSlots.keySet().forEach(sk -> onRemoveStackKey.accept(sk));
		partiallyFilledStackSlots.clear();
		partiallyFilledSlotStacks.clear();
		itemStackKeys.clear();

		emptySlots.clear();
		onRemoveLastEmptySlot.run();

		for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
			ItemStack stack = itemHandler.getStackInSlot(slot);
			set(itemHandler, slot, stack);
		}
	}

	private boolean isPartiallyFilled(InventoryHandler itemHandler, int slot, ItemStack stack) {
		return stack.getCount() < itemHandler.getStackLimit(slot, ItemVariant.of(stack));
	}

	@Override
	public long insertItemIntoHandler(InventoryHandler itemHandler, IItemHandlerInserter inserter, UnaryOperator<ItemStack> overflowHandler, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx) {
		ItemStackKey stackKey = new ItemStackKey(resource);

		long remaining = maxAmount;
		remaining -= handleOverflow(overflowHandler, stackKey, resource, remaining);
		if (remaining <= 0) {
			return 0;
		}

		remaining -= insertIntoSlotsThatMatchStack(inserter, resource, remaining, ctx, stackKey);
		if (remaining > 0) {
			remaining -= insertIntoEmptySlots(inserter, resource, remaining, ctx);
		}
		if (remaining > 0) {
			remaining -= handleOverflow(overflowHandler, stackKey, resource, remaining);
		}

		return (int)maxAmount - remaining;
	}

	@Override
	public long insertItemIntoHandler(InventoryHandler itemHandler, IItemHandlerInserter inserter, UnaryOperator<ItemStack> overflowHandler, int slot, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx) {
		ItemStackKey stackKey = new ItemStackKey(resource);

		long remaining = maxAmount;
		remaining -= handleOverflow(overflowHandler, stackKey, resource, remaining);
		if (remaining <= 0) {
			return 0;
		}

		ItemStack existing = itemHandler.getStackInSlot(slot);
		boolean wasEmpty = existing.isEmpty();

		boolean doesNotMatchCurrentSlot = !ItemHandlerHelper.canItemStacksStack(resource.toStack((int) remaining), existing);
		if (wasEmpty || doesNotMatchCurrentSlot) {
			remaining -= insertIntoSlotsThatMatchStack(inserter, resource, remaining, ctx, stackKey);
		}
		if (remaining > 0 && doesNotMatchCurrentSlot) {
			remaining -= insertIntoEmptySlots(inserter, resource, remaining, ctx);
		}
		if (remaining > 0 && (!emptySlots.contains(slot) || shouldInsertIntoEmpty.getAsBoolean())) {
			remaining -= inserter.insertItem(slot, resource, remaining, ctx);
		}

		if (remaining > 0) {
			remaining -= handleOverflow(overflowHandler, stackKey, resource, remaining);
		}

		return (int)maxAmount - remaining;
	}

	@Override
	public void registerListeners(Consumer<ItemStackKey> onAddStackKey, Consumer<ItemStackKey> onRemoveStackKey, Runnable onAddFirstEmptySlot, Runnable onRemoveLastEmptySlot) {
		this.onAddStackKey = onAddStackKey;
		this.onRemoveStackKey = onRemoveStackKey;
		this.onAddFirstEmptySlot = onAddFirstEmptySlot;
		this.onRemoveLastEmptySlot = onRemoveLastEmptySlot;
	}

	@Override
	public void unregisterStackKeyListeners() {
		onAddStackKey = sk -> {};
		onRemoveStackKey = sk -> {};
	}

	@Override
	public boolean hasEmptySlots() {
		return shouldInsertIntoEmpty.getAsBoolean() && !emptySlots.isEmpty();
	}

	private long handleOverflow(UnaryOperator<ItemStack> overflowHandler, ItemStackKey stackKey, ItemVariant resource, long maxAmount) {
		ItemStack remainingStack = resource.toStack((int) maxAmount);
		if (fullStackSlots.containsKey(stackKey) && !fullStackSlots.get(stackKey).isEmpty()) {
			remainingStack = overflowHandler.apply(remainingStack);
		}
		return (int)maxAmount - remainingStack.getCount();
	}

	private long insertIntoSlotsThatMatchStack(IItemHandlerInserter inserter, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx, ItemStackKey stackKey) {
		long remaining = maxAmount;

		Set<Integer> slots = partiallyFilledStackSlots.get(stackKey);
		int sizeBefore = slots == null ? 0 : slots.size();
		int i = 0;
		// Always taking first element here and iterating while not empty as iterating using iterator would produce CME due to void/compacting reacting to inserts
		// and going into this logic as well and because of that causing collection to be updated outside of first level iterator. The increment is here just
		// in case updating cache fails to prevent infinite loop
		while (partiallyFilledStackSlots.get(stackKey) != null && !partiallyFilledStackSlots.get(stackKey).isEmpty() && i++ < sizeBefore) {
			int matchingSlot = partiallyFilledStackSlots.get(stackKey).iterator().next();
			remaining -= inserter.insertItem(matchingSlot, resource, remaining, ctx);
			if (remaining <= 0) {
				break;
			}
		}
		return (int)maxAmount - remaining;
	}

	private long insertIntoEmptySlots(IItemHandlerInserter inserter, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx) {
		long remaining = maxAmount;
		remaining -= insertIntoEmptyMemorySlots(inserter, resource, remaining, ctx);
		remaining -= insertIntoEmptyFilterSlots(inserter, resource, remaining, ctx);
		if (shouldInsertIntoEmpty.getAsBoolean() && remaining > 0) {
			int sizeBefore = emptySlots.size();
			int i = 0;
			// Always taking first element here and iterating while not empty as iterating using iterator would produce CME due to void/compacting reacting to inserts
			// and going into this logic as well and because of that causing collection to be updated outside of first level iterator. The increment is here just
			// in case updating cache fails to prevent infinite loop
			while (!emptySlots.isEmpty() && i++ < sizeBefore) {
				Iterator<Integer> it = emptySlots.iterator();
				int slot = it.next();
				while (memorySettings.isSlotSelected(slot)) {
					if (!it.hasNext()) {
						return remaining;
					}
					slot = it.next();
				}

				remaining -= inserter.insertItem(slot, resource, remaining, ctx);
				if (remaining <= 0) {
					break;
				}
			}
		}

		return (int)maxAmount - remaining;
	}

	private long insertIntoEmptyFilterSlots(IItemHandlerInserter inserter, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx) {
		Item item = resource.getItem();
		long remaining = maxAmount;
		if (filterItemSlots.containsKey(item)) {
			for (int filterSlot : filterItemSlots.get(item)) {
				if (emptySlots.contains(filterSlot)) {
					remaining -= inserter.insertItem(filterSlot, resource, remaining, ctx);
					if (remaining <= 0) {
						break;
					}
				}
			}
		}

		return (int)maxAmount - remaining;
	}

	private long insertIntoEmptyMemorySlots(IItemHandlerInserter inserter, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx) {
		Map<Item, Set<Integer>> memoryFilterItemSlots = memorySettings.getFilterItemSlots();
		Item item = resource.getItem();
		long remaining = maxAmount;
		if (memoryFilterItemSlots.containsKey(item)) {
			for (int memorySlot : memoryFilterItemSlots.get(item)) {
				if (emptySlots.contains(memorySlot)) {
					remaining -= inserter.insertItem(memorySlot, resource, remaining, ctx);
					if (remaining <= 0) {
						break;
					}
				}
			}
		}

		Map<Integer, Set<Integer>> memoryFilterStackSlots = memorySettings.getFilterStackSlots();
		if (!memoryFilterStackSlots.isEmpty()) {
			int stackHash = ItemStackKey.getHashCode(resource);
			if (memoryFilterStackSlots.containsKey(stackHash)) {
				for (int memorySlot : memoryFilterStackSlots.get(stackHash)) {
					if (emptySlots.contains(memorySlot)) {
						remaining -= inserter.insertItem(memorySlot, resource, remaining, ctx);
						if (remaining <= 0) {
							break;
						}
					}
				}
			}
		}
		return (int)maxAmount - remaining;
	}
}
