package net.p3pp3rf1y.sophisticatedcore.util;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IPickupResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class InventoryHelper {
	private InventoryHelper() {}

	public static Optional<ItemStack> getItemFromEitherHand(Player player, Item item) {
		ItemStack mainHandItem = player.getMainHandItem();
		if (mainHandItem.getItem() == item) {
			return Optional.of(mainHandItem);
		}
		ItemStack offhandItem = player.getOffhandItem();
		if (offhandItem.getItem() == item) {
			return Optional.of(offhandItem);
		}
		return Optional.empty();
	}

	public static boolean hasItem(SlotExposedStorage inventory, Predicate<ItemStack> matches) {
		AtomicBoolean result = new AtomicBoolean(false);
		iterate(inventory, (slot, stack) -> {
			if (!stack.isEmpty() && matches.test(stack)) {
				result.set(true);
			}
		}, result::get);
		return result.get();
	}

	public static Set<Integer> getItemSlots(SlotExposedStorage inventory, Predicate<ItemStack> matches) {
		Set<Integer> slots = new HashSet<>();
		iterate(inventory, (slot, stack) -> {
			if (!stack.isEmpty() && matches.test(stack)) {
				slots.add(slot);
			}
		});
		return slots;
	}

	public static void copyTo(SlotExposedStorage handlerA, SlotExposedStorage handlerB) {
		int slotsA = handlerA.getSlots();
		int slotsB = handlerB.getSlots();
		for (int slot = 0; slot < slotsA && slot < slotsB; slot++) {
			ItemStack slotStack = handlerA.getStackInSlot(slot);
			if (!slotStack.isEmpty()) {
				handlerB.setStackInSlot(slot, slotStack);
			}
		}
	}

	public static List<ItemStack> insertIntoInventory(List<ItemStack> stacks, SlotExposedStorage inventory, @Nullable TransactionContext ctx) {
		if (stacks.isEmpty()) {
			return stacks;
		}
		List<ItemStack> remainingStacks = new ArrayList<>();
		for (ItemStack stack : stacks) {
			ItemVariant resource = ItemVariant.of(stack);

			long remaining = stack.getCount() - insertIntoInventory(resource, stack.getCount(), inventory, ctx);
			if (remaining > 0) {
				remainingStacks.add(resource.toStack((int) remaining));
			}
		}
		return remainingStacks;
	}

	public static SlotExposedStorage cloneInventory(SlotExposedStorage inventory) {
		SlotExposedStorage cloned = new ItemStackHandler(inventory.getSlots());
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			cloned.insertSlot(slot, ItemVariant.of(stack), stack.getCount(), null);
		}
		return cloned;
	}

	public static long insertIntoInventory(ItemVariant resource, long maxAmount, SlotExposedStorage inventory, @Nullable TransactionContext ctx) {
		if (inventory instanceof IItemHandlerSimpleInserter itemHandlerSimpleInserter) {
			return maxAmount - itemHandlerSimpleInserter.insert(resource, maxAmount, ctx);
		}

		long remaining = maxAmount;
		int slots = inventory.getSlots();
		for (int slot = 0; slot < slots && remaining > 0; slot++) {
			remaining -= inventory.insertSlot(slot, resource, remaining, ctx);
		}
		return maxAmount - remaining;
	}

	public static long extractFromInventory(Item item, int count, SlotExposedStorage inventory, TransactionContext ctx) {
		long ret = 0;
		int slots = inventory.getSlots();
		for (int slot = 0; slot < slots && ret < count; slot++) {
			ItemStack slotStack = inventory.getStackInSlot(slot);
			ItemVariant resource = ItemVariant.of(slotStack);
			if (slotStack.getItem() == item && (ret == 0 || ItemHandlerHelper.canItemStacksStack(resource.toStack((int) ret), slotStack))) {
				long toExtract = Math.min(slotStack.getCount(), count - ret);
				ret += inventory.extractSlot(slot, resource, toExtract, ctx);
			}
		}

		return ret;
	}

	public static ItemStack simulateExtractFromInventory(ItemStack stack, SlotExposedStorage inventory, @Nullable TransactionContext ctx) {
		try (Transaction transaction = Transaction.openNested(ctx)) {
			return extractFromInventory(stack, inventory, transaction);
		}
	}

	public static ItemStack extractFromInventory(ItemStack stack, SlotExposedStorage inventory, @Nullable TransactionContext ctx) {
		long extractedCount = 0;
		int slots = inventory.getSlots();
		for (int slot = 0; slot < slots && extractedCount < stack.getCount(); slot++) {
			ItemStack slotStack = inventory.getStackInSlot(slot);
			ItemVariant resource = ItemVariant.of(slotStack);
			if (ItemHandlerHelper.canItemStacksStack(stack, slotStack)) {
				long toExtract = Math.min(slotStack.getCount(), stack.getCount() - extractedCount);
				extractedCount += inventory.extractSlot(slot, resource, toExtract, ctx);
			}
		}

		if (extractedCount == 0) {
			return ItemStack.EMPTY;
		}

		ItemStack result = stack.copy();
		result.setCount((int) extractedCount);

		return result;
	}

	public static ItemStack runPickupOnPickupResponseUpgrades(Level world, UpgradeHandler upgradeHandler, ItemStack remainingStack, @Nullable TransactionContext ctx) {
		return runPickupOnPickupResponseUpgrades(world, null, upgradeHandler, remainingStack, ctx);
	}

	public static ItemStack runPickupOnPickupResponseUpgrades(Level world,
			@Nullable Player player, UpgradeHandler upgradeHandler, ItemStack remainingStack, @Nullable TransactionContext ctx) {
		List<IPickupResponseUpgrade> pickupUpgrades = upgradeHandler.getWrappersThatImplement(IPickupResponseUpgrade.class);

		for (IPickupResponseUpgrade pickupUpgrade : pickupUpgrades) {
			int countBeforePickup = remainingStack.getCount();
			try (Transaction pickupTransaction = Transaction.openNested(ctx)) {
				remainingStack = pickupUpgrade.pickup(world, remainingStack, pickupTransaction);

				ItemStack finalRemainingStack = remainingStack;
				TransactionCallback.onSuccess(pickupTransaction, () -> {
					if (player != null && finalRemainingStack.getCount() != countBeforePickup) {
						playPickupSound(world, player);
					}
				});

				pickupTransaction.commit();
			}

			if (remainingStack.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}

		return remainingStack;
	}

	private static void playPickupSound(Level level, @Nonnull Player player) {
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, RandHelper.getRandomMinusOneToOne(level.random) * 1.4F + 2.0F);
	}

	public static void iterate(SlotExposedStorage handler, BiConsumer<Integer, ItemStack> actOn) {
		iterate(handler, actOn, () -> false);
	}

	public static void iterate(SlotExposedStorage handler, BiConsumer<Integer, ItemStack> actOn, BooleanSupplier shouldExit) {
		int slots = handler.getSlots();
		for (int slot = 0; slot < slots; slot++) {
			ItemStack stack = handler.getStackInSlot(slot);
			actOn.accept(slot, stack);
			if (shouldExit.getAsBoolean()) {
				break;
			}
		}
	}

	public static int getCountMissingInHandler(SlotExposedStorage itemHandler, ItemStack filter, int expectedCount) {
		MutableInt missingCount = new MutableInt(expectedCount);
		iterate(itemHandler, (slot, stack) -> {
			if (ItemHandlerHelper.canItemStacksStack(stack, filter)) {
				missingCount.subtract(Math.min(stack.getCount(), missingCount.getValue()));
			}
		}, () -> missingCount.getValue() == 0);
		return missingCount.getValue();
	}

	public static <T> T iterate(SlotExposedStorage handler, BiFunction<Integer, ItemStack, T> getFromSlotStack, Supplier<T> supplyDefault, Predicate<T> shouldExit) {
		T ret = supplyDefault.get();
		int slots = handler.getSlots();
		for (int slot = 0; slot < slots; slot++) {
			ItemStack stack = handler.getStackInSlot(slot);
			ret = getFromSlotStack.apply(slot, stack);
			if (shouldExit.test(ret)) {
				break;
			}
		}
		return ret;
	}

	public static void transfer(SlotExposedStorage handlerA, SlotExposedStorage handlerB, Consumer<Supplier<ItemStack>> onInserted) {
		if (handlerA == null || handlerB == null) {
			return;
		}

		try (Transaction iterationTransaction = Transaction.openNested(null)) {
			for (StorageView<ItemVariant> view : handlerA.nonEmptyViews()) {
				ItemVariant resource = view.getResource();
				long maxExtracted;

				// check how much can be extracted
				try (Transaction extractionTestTransaction = iterationTransaction.openNested()) {
					maxExtracted = view.extract(resource, view.getAmount(), extractionTestTransaction);
					extractionTestTransaction.abort();
				}

				try (Transaction transferTransaction = iterationTransaction.openNested()) {
					// check how much can be inserted
					long accepted = handlerB.insert(resource, maxExtracted, transferTransaction);

					// extract it, or rollback if the amounts don't match
					if (view.extract(resource, accepted, transferTransaction) == accepted) {
						transferTransaction.commit();
						TransactionCallback.onSuccess(transferTransaction, () -> onInserted.accept(() -> resource.toStack((int) accepted)));
					}
				}
			}

			iterationTransaction.commit();
		} catch (Exception e) {
			CrashReport report = CrashReport.forThrowable(e, "Moving resources between storages");
			report.addCategory("Move details")
					.setDetail("Input storage", handlerA::toString)
					.setDetail("Output storage", handlerB::toString);
			throw new ReportedException(report);
		}
	}

	public static boolean isEmpty(SlotExposedStorage itemHandler) {
		int slots = itemHandler.getSlots();
		for (int slot = 0; slot < slots; slot++) {
			if (!itemHandler.getStackInSlot(slot).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public static ItemStack getAndRemove(SlotExposedStorage itemHandler, int slot) {
		if (slot >= itemHandler.getSlots()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = itemHandler.getStackInSlot(slot);
		ItemVariant resource = ItemVariant.of(stack);
		long extracted = itemHandler.extractSlot(slot, resource, stack.getCount(), null);
		return resource.toStack((int) extracted);
	}

	public static void insertOrDropItem(Player player, ItemStack stack, SlotExposedStorage... inventories) {
		ItemVariant resource = ItemVariant.of(stack);
		long toInsert = stack.getCount();
		for (SlotExposedStorage inventory : inventories) {
			toInsert -= insertIntoInventory(resource, toInsert, inventory, null);
			if (toInsert == 0) {
				return;
			}
		}

		if (toInsert > 0) {
			player.drop(resource.toStack((int) toInsert), true);
		}
	}

	static Map<ItemStackKey, Integer> getCompactedStacks(SlotExposedStorage handler) {
		return getCompactedStacks(handler, new HashSet<>());
	}

	static Map<ItemStackKey, Integer> getCompactedStacks(SlotExposedStorage handler, Set<Integer> ignoreSlots) {
		Map<ItemStackKey, Integer> ret = new HashMap<>();
		iterate(handler, (slot, stack) -> {
			if (stack.isEmpty() || ignoreSlots.contains(slot)) {
				return;
			}
			ItemStackKey itemStackKey = new ItemStackKey(stack);
			ret.put(itemStackKey, ret.computeIfAbsent(itemStackKey, fs -> 0) + stack.getCount());
		});
		return ret;
	}

	public static List<ItemStack> getCompactedStacksSortedByCount(SlotExposedStorage handler) {
		Map<ItemStackKey, Integer> compactedStacks = getCompactedStacks(handler);
		List<Map.Entry<ItemStackKey, Integer>> sortedList = new ArrayList<>(compactedStacks.entrySet());
		sortedList.sort(InventorySorter.BY_COUNT);

		List<ItemStack> ret = new ArrayList<>();
		sortedList.forEach(e -> {
			ItemStack stackCopy = e.getKey().getStack().copy();
			stackCopy.setCount(e.getValue());
			ret.add(stackCopy);
		});
		return ret;
	}

	public static Set<ItemStackKey> getUniqueStacks(SlotExposedStorage handler) {
		Set<ItemStackKey> uniqueStacks = new HashSet<>();
		iterate(handler, (slot, stack) -> {
			if (stack.isEmpty()) {
				return;
			}
			ItemStackKey itemStackKey = new ItemStackKey(stack);
			uniqueStacks.add(itemStackKey);
		});
		return uniqueStacks;
	}

	public static List<Integer> getEmptySlotsRandomized(SlotExposedStorage inventory) {
		List<Integer> list = Lists.newArrayList();

		for (int i = 0; i < inventory.getSlots(); ++i) {
			if (inventory.getStackInSlot(i).isEmpty()) {
				list.add(i);
			}
		}

		Collections.shuffle(list, new Random());
		return list;
	}

	public static void shuffleItems(List<ItemStack> stacks, int emptySlotsCount, RandomSource rand) {
		List<ItemStack> list = Lists.newArrayList();
		Iterator<ItemStack> iterator = stacks.iterator();

		while (iterator.hasNext()) {
			ItemStack itemstack = iterator.next();
			if (itemstack.isEmpty()) {
				iterator.remove();
			} else if (itemstack.getCount() > 1) {
				list.add(itemstack);
				iterator.remove();
			}
		}

		while (emptySlotsCount - stacks.size() - list.size() > 0 && !list.isEmpty()) {
			ItemStack itemstack2 = list.remove(Mth.nextInt(rand, 0, list.size() - 1));
			int i = Mth.nextInt(rand, 1, itemstack2.getCount() / 2);
			ItemStack itemstack1 = itemstack2.split(i);
			if (itemstack2.getCount() > 1 && rand.nextBoolean()) {
				list.add(itemstack2);
			} else {
				stacks.add(itemstack2);
			}

			if (itemstack1.getCount() > 1 && rand.nextBoolean()) {
				list.add(itemstack1);
			} else {
				stacks.add(itemstack1);
			}
		}

		stacks.addAll(list);
		Collections.shuffle(stacks, new Random());
	}

	public static void dropItems(ItemStackHandler inventoryHandler, Level level, BlockPos pos) {
		dropItems(inventoryHandler, level, pos.getX(), pos.getY(), pos.getZ());
	}

	public static void dropItems(ItemStackHandler inventoryHandler, Level level, double x, double y, double z) {
		iterate(inventoryHandler, (slot, stack) -> {
			if (stack.isEmpty()) {
				return;
			}

			ItemVariant resource = ItemVariant.of(stack);
			long extracted = inventoryHandler.extractSlot(slot, resource, stack.getCount(), null);
			ItemStack extractedStack = resource.toStack((int) extracted);
			while (!extractedStack.isEmpty()) {
				Containers.dropItemStack(level, x, y, z, extractedStack.split(Math.min(extractedStack.getCount(), extractedStack.getMaxStackSize())));
				inventoryHandler.setStackInSlot(slot, ItemStack.EMPTY);
			}
		});
	}

	public static int getAnalogOutputSignal(ITrackedContentsItemHandler handler) {
		AtomicDouble totalFilled = new AtomicDouble(0);
		AtomicBoolean isEmpty = new AtomicBoolean(true);
		iterate(handler, (slot, stack) -> {
			if (!stack.isEmpty()) {
				int slotLimit = handler.getInternalSlotLimit(slot);
				totalFilled.addAndGet(stack.getCount() / (slotLimit / ((float) 64 / stack.getMaxStackSize())));
				isEmpty.set(false);
			}
		});
		double percentFilled = totalFilled.get() / handler.getSlots();
		return Mth.floor(percentFilled * 14.0F) + (isEmpty.get() ? 0 : 1);
	}
}
