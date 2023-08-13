package net.p3pp3rf1y.sophisticatedcore.util;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
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

	public static boolean hasItem(SlottedStorage<ItemVariant> inventory, Predicate<ItemStack> matches) {
		AtomicBoolean result = new AtomicBoolean(false);
		iterate(inventory, (stack) -> {
			if (!stack.isEmpty() && matches.test(stack)) {
				result.set(true);
			}
		}, result::get);
		return result.get();
	}

	public static Set<Integer> getItemSlots(SlottedStorage<ItemVariant> inventory, Predicate<ItemStack> matches) {
		Set<Integer> slots = new HashSet<>();
		iterate(inventory, (slot, stack) -> {
			if (!stack.isEmpty() && matches.test(stack)) {
				slots.add(slot);
			}
		});
		return slots;
	}

	public static void copyTo(SlottedStorage<ItemVariant> handlerA, SlottedStorage<ItemVariant> handlerB) {
		int slotsA = handlerA.getSlotCount();
		int slotsB = handlerB.getSlotCount();
		try (Transaction ctx = Transaction.openOuter()) {
			for (int slot = 0; slot < slotsA && slot < slotsB; slot++) {
				SingleSlotStorage<ItemVariant> slotStorage = handlerA.getSlot(slot);
				if (!slotStorage.isResourceBlank()) {
					handlerB.getSlots().get(slot).insert(slotStorage.getResource(), slotStorage.getAmount(), ctx);
				}
			}
			ctx.commit();
		}
	}

	public static List<ItemStack> insertIntoInventory(List<ItemStack> stacks, Storage<ItemVariant> inventory, @Nullable TransactionContext ctx) {
		if (stacks.isEmpty()) {
			return stacks;
		}

		List<ItemStack> remainingStacks = new ArrayList<>();
		for (ItemStack stack : stacks) {
			ItemVariant resource = ItemVariant.of(stack);

			long remaining = stack.getCount() - inventory.insert(resource, stack.getCount(), ctx);
			if (remaining > 0) {
				remainingStacks.add(resource.toStack((int) remaining));
			}
		}
		return remainingStacks;
	}

	public static ItemStack runPickupOnPickupResponseUpgrades(Level world, UpgradeHandler upgradeHandler, ItemStack remainingStack, @Nullable TransactionContext ctx) {
		return runPickupOnPickupResponseUpgrades(world, null, upgradeHandler, remainingStack, ctx);
	}

	public static ItemStack runPickupOnPickupResponseUpgrades(Level world, @Nullable Player player, UpgradeHandler upgradeHandler, ItemStack remainingStack, @Nullable TransactionContext ctx) {
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

	public static void iterate(SlottedStorage<ItemVariant> handler, BiConsumer<Integer, ItemStack> actOn) {
		iterate(handler, actOn, () -> false);
	}

	public static void iterate(SlottedStorage<ItemVariant> handler, BiConsumer<Integer, ItemStack> actOn, BooleanSupplier shouldExit) {
		int slots = handler.getSlotCount();
		for (int slot = 0; slot < slots; slot++) {
			SingleSlotStorage<ItemVariant> storage = handler.getSlot(slot);
			ItemStack stack = storage.getResource().toStack((int) storage.getAmount());
			actOn.accept(slot, stack);
			if (shouldExit.getAsBoolean()) {
				break;
			}
		}
	}

	public static void iterate(SlottedStorage<ItemVariant> handler, Consumer<ItemStack> actOn) {
		iterate(handler, actOn, () -> false);
	}

	public static void iterate(Storage<ItemVariant> handler, Consumer<ItemStack> actOn, BooleanSupplier shouldExit) {
		for (StorageView<ItemVariant> view : handler.nonEmptyViews()) {
			ItemStack stack = view.getResource().toStack((int) view.getAmount());
			actOn.accept(stack);
			if (shouldExit.getAsBoolean()) {
				break;
			}
		}
	}

	public static <T> T iterate(SlottedStorage<ItemVariant> handler, BiFunction<Integer, ItemStack, T> getFromSlotStack, Supplier<T> supplyDefault, Predicate<T> shouldExit) {
		T ret = supplyDefault.get();
		int slots = handler.getSlotCount();
		for (int slot = 0; slot < slots; slot++) {
			SingleSlotStorage<ItemVariant> storage = handler.getSlot(slot);
			ItemStack stack = storage.getResource().toStack((int) storage.getAmount());
			ret = getFromSlotStack.apply(slot, stack);
			if (shouldExit.test(ret)) {
				break;
			}
		}
		return ret;
	}

	public static int getCountMissingInHandler(Storage<ItemVariant> itemHandler, ItemStack filter, int expectedCount) {
		MutableInt missingCount = new MutableInt(expectedCount);
		iterate(itemHandler, (stack) -> {
			if (ItemStackHelper.canItemStacksStack(stack, filter)) {
				missingCount.subtract(Math.min(stack.getCount(), missingCount.getValue()));
			}
		}, () -> missingCount.getValue() == 0);
		return missingCount.getValue();
	}

	public static void transfer(Storage<ItemVariant> handlerA, Storage<ItemVariant> handlerB, Consumer<Supplier<ItemStack>> onInserted, @Nullable TransactionContext ctx) {
		if (handlerA == null || handlerB == null) {
			return;
		}

		try (Transaction outer = Transaction.openNested(ctx)) {
			for (StorageView<ItemVariant> view : handlerA.nonEmptyViews()) {
				ItemVariant resource = view.getResource();
				long maxExtracted;

				// check how much can be extracted
				try (Transaction extractionTestTransaction = outer.openNested()) {
					maxExtracted = view.extract(resource, view.getAmount(), extractionTestTransaction);
				}

				try (Transaction transferTransaction = outer.openNested()) {
					// check how much can be inserted
					long accepted = handlerB.insert(resource, maxExtracted, transferTransaction);

					// extract it, or rollback if the amounts don't match
					if (accepted > 0 && view.extract(resource, accepted, transferTransaction) == accepted) {
						TransactionCallback.onSuccess(transferTransaction, () -> onInserted.accept(() -> resource.toStack((int) accepted)));
						transferTransaction.commit();
					}
				}
			}

			outer.commit();
		} catch (Exception e) {
			CrashReport report = CrashReport.forThrowable(e, "Moving resources between storages");
			report.addCategory("Move details")
					.setDetail("Input storage", handlerA::toString)
					.setDetail("Output storage", handlerB::toString);
			throw new ReportedException(report);
		}
	}

	public static <T> boolean isEmpty(Storage<T> itemHandler) {
		return itemHandler.nonEmptyIterator().hasNext();
		/*int slots = itemHandler.getSlotCount();
		for (int slot = 0; slot < slots; slot++) {
			if (!itemHandler.getSlot(slot).isResourceBlank()) {
				return false;
			}
		}
		return true;*/
	}

	public static ItemStack getAndRemove(SlottedStorage<ItemVariant> itemHandler, int slot) {
		if (slot >= itemHandler.getSlotCount()) {
			return ItemStack.EMPTY;
		}

		SingleSlotStorage<ItemVariant> slotStorage = itemHandler.getSlot(slot);
		ItemVariant resource = slotStorage.getResource();
		long extracted = slotStorage.extract(resource, Long.MAX_VALUE, null);
		return resource.toStack((int) extracted);
		/*ItemStack stack = itemHandler.getStackInSlot(slot);
		ItemVariant resource = ItemVariant.of(stack);
		long extracted = itemHandler.extractSlot(slot, resource, stack.getCount(), null);
		return resource.toStack((int) extracted);*/
	}

	public static void insertOrDropItem(Player player, ItemStack stack, Storage<ItemVariant>... inventories) {
		ItemVariant resource = ItemVariant.of(stack);
		long toInsert = stack.getCount();
		for (Storage<ItemVariant> inventory : inventories) {
			try (Transaction outer = Transaction.openOuter()) {
				toInsert -= inventory.insert(resource, toInsert, outer);
				outer.commit();
			}
			if (toInsert == 0) {
				return;
			}
		}

		if (toInsert > 0) {
			player.drop(resource.toStack((int) toInsert), true);
		}
	}

	static Map<ItemStackKey, Integer> getCompactedStacks(SlottedStorage<ItemVariant> handler) {
		return getCompactedStacks(handler, new HashSet<>());
	}

	static Map<ItemStackKey, Integer> getCompactedStacks(SlottedStorage<ItemVariant> handler, Set<Integer> ignoreSlots) {
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

	public static List<ItemStack> getCompactedStacksSortedByCount(SlottedStorage<ItemVariant> handler) {
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

	public static Set<ItemStackKey> getUniqueStacks(SlottedStorage<ItemVariant> handler) {
		Set<ItemStackKey> uniqueStacks = new HashSet<>();
		iterate(handler, stack -> {
			if (stack.isEmpty()) {
				return;
			}
			ItemStackKey itemStackKey = new ItemStackKey(stack);
			uniqueStacks.add(itemStackKey);
		});
		return uniqueStacks;
	}

	public static List<Integer> getEmptySlotsRandomized(SlottedStorage<ItemVariant> inventory) {
		List<Integer> list = Lists.newArrayList();

		for (int i = 0; i < inventory.getSlotCount(); ++i) {
			if (inventory.getSlot(i).isResourceBlank()) {
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
		double percentFilled = totalFilled.get() / handler.getSlotCount();
		return Mth.floor(percentFilled * 14.0F) + (isEmpty.get() ? 0 : 1);
	}
}
