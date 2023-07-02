package net.p3pp3rf1y.sophisticatedcore.inventory;

import com.mojang.datafixers.util.Pair;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IInsertResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IOverflowResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ISlotLimitUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeConfig;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.MathHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public abstract class InventoryHandler extends ItemStackHandler implements ITrackedContentsItemHandler {
	public static final String INVENTORY_TAG = "inventory";
	private static final String PARTITIONER_TAG = "partitioner";
	private static final String REAL_COUNT_TAG = "realCount";
	protected final IStorageWrapper storageWrapper;
	private final CompoundTag contentsNbt;
	private final Runnable saveHandler;
	private final List<IntConsumer> onContentsChangedListeners = new ArrayList<>();
	private boolean persistent = true;
	private final Map<Integer, CompoundTag> stackNbts = new LinkedHashMap<>();

	private ISlotTracker slotTracker = new ISlotTracker.Noop();

	private int baseSlotLimit;
	private int slotLimit;
	private int maxStackSizeMultiplier;
	private boolean isInitializing;
	private final StackUpgradeConfig stackUpgradeConfig;
	private final InventoryPartitioner inventoryPartitioner;
	private Consumer<Set<Item>> filterItemsChangeListener = s -> {};
	private final Map<Item, Set<Integer>> filterItemSlots = new HashMap<>();
	private BooleanSupplier shouldInsertIntoEmpty = () -> true;
	private boolean slotLimitInitialized = false;

	protected InventoryHandler(int numberOfInventorySlots, IStorageWrapper storageWrapper, CompoundTag contentsNbt, Runnable saveHandler, int baseSlotLimit, StackUpgradeConfig stackUpgradeConfig) {
		super(numberOfInventorySlots);
		this.stackUpgradeConfig = stackUpgradeConfig;
		isInitializing = true;
		this.storageWrapper = storageWrapper;
		this.contentsNbt = contentsNbt;
		this.saveHandler = saveHandler;
		setBaseSlotLimit(baseSlotLimit);
		deserializeNBT(contentsNbt.getCompound(INVENTORY_TAG));
		inventoryPartitioner = new InventoryPartitioner(contentsNbt.getCompound(PARTITIONER_TAG), this, () -> storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class));
		filterItemSlots.putAll(inventoryPartitioner.getFilterItems());
		initStackNbts();

		isInitializing = false;
	}

	public ISlotTracker getSlotTracker() {
		initSlotTracker();
		return slotTracker;
	}

	@Override
	public void setSize(int size) {
		super.setSize(this.getSlots());
	}

	private void initStackNbts() {
		for (int slot = 0; slot < this.getSlots(); slot++) {
			ItemStack slotStack = this.getStackInSlot(slot);
			if (!slotStack.isEmpty()) {
				stackNbts.put(slot, getSlotsStackNbt(slot, slotStack));
			}
		}
	}

	@Override
	public void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		if (persistent && updateSlotNbt(slot)) {
			saveInventory();
			triggerOnChangeListeners(slot);
		}
	}

	public void triggerOnChangeListeners(int slot) {
		for (IntConsumer onContentsChangedListener : onContentsChangedListeners) {
			onContentsChangedListener.accept(slot);
		}
	}

	@SuppressWarnings("java:S3824")
	//compute use here would be difficult as then there's no way of telling that value was newly created vs different than the one that needs to be set
	private boolean updateSlotNbt(int slot) {
		ItemStack slotStack = getSlotStack(slot);
		if (slotStack.isEmpty()) {
			if (stackNbts.containsKey(slot)) {
				stackNbts.remove(slot);
				return true;
			}
		} else {
			CompoundTag itemTag = getSlotsStackNbt(slot, slotStack);
			if (!stackNbts.containsKey(slot) || !stackNbts.get(slot).equals(itemTag)) {
				stackNbts.put(slot, itemTag);
				return true;
			}
		}
		return false;
	}

	private CompoundTag getSlotsStackNbt(int slot, ItemStack slotStack) {
		CompoundTag itemTag = new CompoundTag();
		itemTag.putInt("Slot", slot);
		itemTag.putInt(REAL_COUNT_TAG, slotStack.getCount());
		slotStack.save(itemTag);
		return itemTag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		slotTracker.clear();
		setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : getSlots());
		ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag itemTags = tagList.getCompound(i);
			int slot = itemTags.getInt("Slot");

			if (slot >= 0 && slot < getSlots()) {
				ItemStack slotStack = ItemStack.of(itemTags);
				if (itemTags.contains(REAL_COUNT_TAG)) {
					slotStack.setCount(itemTags.getInt(REAL_COUNT_TAG));
				}
				contentsChangedInternal(slot, slotStack, null);
			}
		}
		slotTracker.refreshSlotIndexesFrom(this);
		onLoad();
	}

	public int getBaseSlotLimit() {
		return baseSlotLimit;
	}

	@Override
	public int getInternalSlotLimit(int slot) {
		return inventoryPartitioner.getPartBySlot(slot).getSlotLimit(slot);
	}

	@Override
	public int getSlotLimit(int slot) {
		if (!slotLimitInitialized) {
			slotLimitInitialized = true;
			updateSlotLimit();
			inventoryPartitioner.onSlotLimitChange();
		}

		return slotLimit > baseSlotLimit ? slotLimit : inventoryPartitioner.getPartBySlot(slot).getSlotLimit(slot);
	}

	public int getBaseStackLimit(ItemVariant resource) {
		if (!stackUpgradeConfig.canStackItem(resource.getItem())) {
			return resource.getItem().getMaxStackSize();
		}

		int limit = MathHelper.intMaxCappedMultiply(resource.getItem().getMaxStackSize(), (baseSlotLimit / 64));
		int remainder = baseSlotLimit % 64;
		if (remainder > 0) {
			limit = MathHelper.intMaxCappedAddition(limit, remainder * resource.getItem().getMaxStackSize() / 64);
		}
		return limit;
	}

/*	public int getBaseStackLimit(ItemStack stack) {
		if (!stackUpgradeConfig.canStackItem(stack.getItem())) {
			return stack.getMaxStackSize();
		}

		int limit = MathHelper.intMaxCappedMultiply(stack.getMaxStackSize(), (baseSlotLimit / 64));
		int remainder = baseSlotLimit % 64;
		if (remainder > 0) {
			limit = MathHelper.intMaxCappedAddition(limit, remainder * stack.getMaxStackSize() / 64);
		}
		return limit;
	}*/

	@Override
	public int getStackLimit(int slot, ItemVariant resource) {
		return inventoryPartitioner.getPartBySlot(slot).getStackLimit(slot, resource);
	}

/*	@Override
	public int getStackLimit(int slot, ItemStack stack) {
		return inventoryPartitioner.getPartBySlot(slot).getStackLimit(slot, stack);
	}*/

	public Item getFilterItem(int slot) {
		return inventoryPartitioner.getPartBySlot(slot).getFilterItem(slot);
	}

	public boolean isFilterItem(Item item) {
		return inventoryPartitioner.isFilterItem(item);
	}

	public void setBaseSlotLimit(int baseSlotLimit) {
		this.baseSlotLimit = baseSlotLimit;
		maxStackSizeMultiplier = baseSlotLimit / 64;

		if (inventoryPartitioner != null) {
			inventoryPartitioner.onSlotLimitChange();
		}

		if (!isInitializing) {
			slotTracker.refreshSlotIndexesFrom(this);
		}
	}

	private void updateSlotLimit() {
		AtomicInteger slotLimitOverride = new AtomicInteger(baseSlotLimit);
		storageWrapper.getUpgradeHandler().getWrappersThatImplement(ISlotLimitUpgrade.class).forEach(slu -> {
			if (slu.getSlotLimit() > slotLimitOverride.get()) {
				slotLimitOverride.set(slu.getSlotLimit());
			}
		});
		slotLimit = slotLimitOverride.get();
	}

	public long extractItemInternal(int slot, ItemVariant resource, long amount, TransactionContext ctx) {
		long extracted = super.extractSlot(slot, resource, amount, ctx);
		if (ctx != null) TransactionCallback.onSuccess(ctx, () -> {
			slotTracker.removeAndSetSlotIndexes(this, slot, getSlotStack(slot));
			onContentsChanged(slot);
		});
		return extracted;
	}

/*	public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
		if (amount == 0) {
			return ItemStack.EMPTY;
		}

		validateSlotIndex(slot);
		ItemStack existing = getSlotStack(slot);

		if (existing.isEmpty()) {
			return ItemStack.EMPTY;
		}

		if (existing.getCount() <= amount) {
			if (!simulate) {
				setSlotStack(slot, ItemStack.EMPTY);
				return existing;
			} else {
				return existing.copy();
			}
		} else {
			if (!simulate) {
				setSlotStack(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - amount));
			}

			return ItemHandlerHelper.copyStackWithSize(existing, amount);
		}
	}*/

	@Override
	@Nonnull
	public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext ctx) {
		return inventoryPartitioner.getPartBySlot(slot).extractItem(slot, resource, maxAmount, ctx);
	}

/*	@Override
	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return inventoryPartitioner.getPartBySlot(slot).extractItem(slot, amount, simulate);
	}*/

/*	@Override
	public void validateSlotIndex(int slot) {
		super.validateSlotIndex(slot);
	}*/

	public ItemVariant getSlotVariant(int slot) {
		return variants[slot];
	}

	public ItemStack getSlotStack(int slot) {
		return stacks[slot];
	}

	public void setSlotStack(int slot, ItemStack stack) {
		contentsChangedInternal(slot, stack, null);
		slotTracker.removeAndSetSlotIndexes(this, slot, stack);
		onContentsChanged(slot);
	}

	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx) {
		initSlotTracker();
		return slotTracker.insertItemIntoHandler(this, this::insertItemInternal, this::triggerOverflowUpgrades, slot, resource, maxAmount, ctx);
	}

/*	@Override
	@Nonnull
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		initSlotTracker();
		return slotTracker.insertItemIntoHandler(this, this::insertItemInternal, this::triggerOverflowUpgrades, slot, stack, simulate);
	}*/

	@Nonnull
	public long insertItemOnlyToSlot(int slot, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx) {
		initSlotTracker();
		if (ItemHandlerHelper.canItemStacksStack(getStackInSlot(slot), resource.toStack())) {
			return triggerOverflowUpgrades(resource.toStack((int) insertItemInternal(slot, resource, maxAmount, ctx))).getCount();
		}

		return maxAmount;
	}

/*	@Nonnull
	public ItemStack insertItemOnlyToSlot(int slot, ItemStack stack, boolean simulate) {
		initSlotTracker();
		if (ItemHandlerHelper.canItemStacksStack(getStackInSlot(slot), stack)) {
			return triggerOverflowUpgrades(insertItemInternal(slot, stack, simulate));
		}

		return stack;
	}*/

	private void initSlotTracker() {
		if (!(slotTracker instanceof InventoryHandlerSlotTracker)) {
			slotTracker = new InventoryHandlerSlotTracker(storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class), filterItemSlots);
			slotTracker.refreshSlotIndexesFrom(this);
			slotTracker.setShouldInsertIntoEmpty(shouldInsertIntoEmpty);
		}
	}

/*	private long insertItemInternal(int slot, ItemVariant resource, long maxAmount, TransactionContext ctx) {
		long inserted = super.insertSlot(slot, resource, maxAmount, ctx);
		if (ctx != null) TransactionCallback.onSuccess(ctx, () -> {
			slotTracker.removeAndSetSlotIndexes(this, slot, getSlotStack(slot));
			onContentsChanged(slot);
		});
		return inserted;
	}*/

	private long insertItemInternal(int slot, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx) {
		try (Transaction nested = Transaction.openNested(ctx)) {
			long remaining = runOnBeforeInsert(slot, resource, maxAmount, nested, this, storageWrapper);
			if (remaining <= 0) {
				nested.commit();
				return maxAmount;
			}

			remaining -= inventoryPartitioner.getPartBySlot(slot).insertItem(slot, resource, maxAmount, nested, super::insertSlot);
			slotTracker.removeAndSetSlotIndexes(this, slot, getStackInSlot(slot));
			TransactionCallback.onFail(nested, () -> slotTracker.removeAndSetSlotIndexes(this, slot, getStackInSlot(slot)));

			if (remaining == maxAmount) {
				return 0;
			}

			runOnAfterInsert(slot, nested, this, storageWrapper);
			nested.commit();
			return maxAmount - remaining;
		}
	}

/*	private ItemStack insertItemInternal(int slot, ItemStack stack, boolean simulate) {
		ItemStack ret = runOnBeforeInsert(slot, stack, simulate, this, storageWrapper);
		if (ret.isEmpty()) {
			return ret;
		}

		ret = inventoryPartitioner.getPartBySlot(slot).insertItem(slot, ret, simulate, super::insertItem);

		if (!simulate) {
			slotTracker.removeAndSetSlotIndexes(this, slot, getStackInSlot(slot));
		}

		if (ret == stack) {
			return ret;
		}

		runOnAfterInsert(slot, simulate, this, storageWrapper);

		return ret;
	}*/

	private ItemStack triggerOverflowUpgrades(ItemStack ret) {
		for (IOverflowResponseUpgrade overflowUpgrade : storageWrapper.getUpgradeHandler().getWrappersThatImplement(IOverflowResponseUpgrade.class)) {
			ret = overflowUpgrade.onOverflow(ret);
			if (ret.isEmpty()) {
				break;
			}
		}
		return ret;
	}

	private void runOnAfterInsert(int slot, @Nullable TransactionContext ctx, IItemHandlerSimpleInserter handler, IStorageWrapper storageWrapper) {
		storageWrapper.getUpgradeHandler().getWrappersThatImplementFromMainStorage(IInsertResponseUpgrade.class).forEach(u -> u.onAfterInsert(handler, slot, ctx));
	}

/*	private void runOnAfterInsert(int slot, boolean simulate, IItemHandlerSimpleInserter handler, IStorageWrapper storageWrapper) {
		if (!simulate) {
			storageWrapper.getUpgradeHandler().getWrappersThatImplementFromMainStorage(IInsertResponseUpgrade.class).forEach(u -> u.onAfterInsert(handler, slot));
		}
	}*/

	private long runOnBeforeInsert(int slot, ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx, IItemHandlerSimpleInserter handler, IStorageWrapper storageWrapper) {
		List<IInsertResponseUpgrade> wrappers = storageWrapper.getUpgradeHandler().getWrappersThatImplementFromMainStorage(IInsertResponseUpgrade.class);
		long toInsert = maxAmount;
		for (IInsertResponseUpgrade upgrade : wrappers) {
			toInsert = upgrade.onBeforeInsert(handler, slot, resource, toInsert, ctx);
			if (toInsert <= 0) {
				return 0;
			}
		}
		return toInsert;
	}

/*	private ItemStack runOnBeforeInsert(int slot, ItemStack stack, boolean simulate, IItemHandlerSimpleInserter handler, IStorageWrapper storageWrapper) {
		List<IInsertResponseUpgrade> wrappers = storageWrapper.getUpgradeHandler().getWrappersThatImplementFromMainStorage(IInsertResponseUpgrade.class);
		ItemStack remaining = stack;
		for (IInsertResponseUpgrade upgrade : wrappers) {
			remaining = upgrade.onBeforeInsert(handler, slot, remaining, simulate);
			if (remaining.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}
		return remaining;
	}*/

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		inventoryPartitioner.getPartBySlot(slot).setStackInSlot(slot, stack, super::setStackInSlot);
		slotTracker.removeAndSetSlotIndexes(this, slot, stack);
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, long amount) {
		return inventoryPartitioner.getPartBySlot(slot).isItemValid(slot, resource, amount) && isAllowed(resource, amount) && storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).matchesFilter(slot, resource, amount);
	}

/*	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return inventoryPartitioner.getPartBySlot(slot).isItemValid(slot, stack) && isAllowed(stack) && storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).matchesFilter(slot, stack);
	}*/

	@Nonnull
	@Override
	public ItemVariant getVariantInSlot(int slot) {
		return inventoryPartitioner.getPartBySlot(slot).getVariantInSlot(slot, super::getVariantInSlot);
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventoryPartitioner.getPartBySlot(slot).getStackInSlot(slot, super::getStackInSlot);
	}

	protected abstract boolean isAllowed(ItemVariant resource, long amount);
	//protected abstract boolean isAllowed(ItemStack stack);

	public void saveInventory() {
		contentsNbt.put(INVENTORY_TAG, serializeNBT());
		if (inventoryPartitioner != null) {
			//inventory parts may affect inventory slots during their initialization in Inventory Partitioner deserialize,
			// but there's no reason to serialize partitioner at that point as its nbt can't during init/deserialization.
			contentsNbt.put(PARTITIONER_TAG, inventoryPartitioner.serializeNBT());
		}
		saveHandler.run();
	}

	@Nullable
	public Pair<ResourceLocation, ResourceLocation> getNoItemIcon(int slotIndex) {
		return inventoryPartitioner.getNoItemIcon(slotIndex);
	}

	public void copyStacksTo(InventoryHandler otherHandler) {
		InventoryHelper.copyTo(this, otherHandler);
	}

	public void addListener(IntConsumer onContentsChanged) {
		onContentsChangedListeners.add(onContentsChanged);
	}

	public void clearListeners() {
		onContentsChangedListeners.clear();
	}

	@Override
	public CompoundTag serializeNBT() {
		ListTag nbtTagList = new ListTag();
		nbtTagList.addAll(stackNbts.values());
		CompoundTag nbt = new CompoundTag();
		nbt.put("Items", nbtTagList);
		nbt.putInt("Size", getSlots());
		return nbt;
	}

	public int getStackSizeMultiplier() {
		return maxStackSizeMultiplier;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, @Nullable TransactionContext ctx) {
		initSlotTracker();
		return slotTracker.insertItemIntoHandler(this, this::insertItemInternal, this::triggerOverflowUpgrades, resource, maxAmount, ctx);
	}

/*	@Override
	public ItemStack insertItem(ItemStack stack, boolean simulate) {
		initSlotTracker();
		return slotTracker.insertItemIntoHandler(this, this::insertItemInternal, this::triggerOverflowUpgrades, stack, simulate);
	}*/

	public void changeSlots(int diff) {
		var previousStacks = stacks.clone();

		setSize(previousStacks.length + diff);
		for (int slot = 0; slot < previousStacks.length && slot < getSlots(); slot++) {
			contentsChangedInternal(slot, previousStacks[slot], null);
		}

		initStackNbts();
		saveInventory();
		slotTracker.refreshSlotIndexesFrom(this);
	}

	@Override
	public Set<ItemStackKey> getTrackedStacks() {
		initSlotTracker();
		HashSet<ItemStackKey> ret = new HashSet<>(slotTracker.getFullStacks());
		ret.addAll(slotTracker.getPartialStacks());
		return ret;
	}

	@Override
	public void registerTrackingListeners(Consumer<ItemStackKey> onAddStackKey, Consumer<ItemStackKey> onRemoveStackKey, Runnable onAddFirstEmptySlot, Runnable onRemoveLastEmptySlot) {
		initSlotTracker();
		slotTracker.registerListeners(onAddStackKey, onRemoveStackKey, onAddFirstEmptySlot, onRemoveLastEmptySlot);
	}

	@Override
	public void unregisterStackKeyListeners() {
		slotTracker.unregisterStackKeyListeners();
	}

	@Override
	public boolean hasEmptySlots() {
		return slotTracker.hasEmptySlots();
	}

	public InventoryPartitioner getInventoryPartitioner() {
		return inventoryPartitioner;
	}

	public boolean isSlotAccessible(int slot) {
		return inventoryPartitioner.getPartBySlot(slot).isSlotAccessible(slot);
	}

	public Set<Integer> getNoSortSlots() {
		return inventoryPartitioner.getNoSortSlots();
	}

	public void onSlotFilterChanged(int slot) {
		inventoryPartitioner.getPartBySlot(slot).onSlotFilterChanged(slot);
	}

	public void registerFilterItemsChangeListener(Consumer<Set<Item>> listener) {
		filterItemsChangeListener = listener;
	}

	public void unregisterFilterItemsChangeListener() {
		filterItemsChangeListener = s -> {};
	}

	public void onFilterItemsChanged() {
		if (inventoryPartitioner == null) {
			return;
		}
		filterItemSlots.clear();
		filterItemSlots.putAll(inventoryPartitioner.getFilterItems());

		filterItemsChangeListener.accept(filterItemSlots.keySet());
	}

	public Set<Item> getFilterItems() {
		return filterItemSlots.keySet();
	}

	public void onInit() {
		if (inventoryPartitioner == null) {
			return;
		}
		inventoryPartitioner.onInit();
		slotTracker = new ISlotTracker.Noop();
	}

	public void setShouldInsertIntoEmpty(BooleanSupplier shouldInsertIntoEmpty) {
		this.shouldInsertIntoEmpty = shouldInsertIntoEmpty;
		slotTracker.setShouldInsertIntoEmpty(shouldInsertIntoEmpty);
	}
}
