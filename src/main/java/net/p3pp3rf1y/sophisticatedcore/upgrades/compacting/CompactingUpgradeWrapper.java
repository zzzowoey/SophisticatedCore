package net.p3pp3rf1y.sophisticatedcore.upgrades.compacting;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.api.ISlotChangeResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IFilteredUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IInsertResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper.CompactingShape;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class CompactingUpgradeWrapper extends UpgradeWrapperBase<CompactingUpgradeWrapper, CompactingUpgradeItem>
		implements IInsertResponseUpgrade, IFilteredUpgrade, ISlotChangeResponseUpgrade, ITickableUpgrade {
	private final FilterLogic filterLogic;
	private final Set<Integer> slotsToCompact = new HashSet<>();

	public CompactingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(storageWrapper, upgrade, upgradeSaveHandler);

		filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount(),
				stack -> !stack.hasTag() && !RecipeHelper.getItemCompactingShapes(stack.getItem()).isEmpty());
	}

	@Override
	public long onBeforeInsert(IItemHandlerSimpleInserter inventoryHandler, int slot, ItemVariant resource, long maxAmount, TransactionContext ctx) {
		return maxAmount;
	}

/*	@Override
	public ItemStack onBeforeInsert(IItemHandlerSimpleInserter inventoryHandler, int slot, ItemStack stack, boolean simulate) {
		return stack;
	}*/

	@Override
	public void onAfterInsert(IItemHandlerSimpleInserter inventoryHandler, int slot, TransactionContext ctx) {
		compactSlot(inventoryHandler, slot, ctx);
	}

	private void compactSlot(IItemHandlerSimpleInserter inventoryHandler, int slot, TransactionContext ctx) {
		ItemStack slotStack = inventoryHandler.getStackInSlot(slot);

		if (slotStack.isEmpty() || slotStack.hasTag() || !filterLogic.matchesFilter(slotStack)) {
			return;
		}

		Item item = slotStack.getItem();

		Set<CompactingShape> shapes = RecipeHelper.getItemCompactingShapes(item);

		if (upgradeItem.shouldCompactThreeByThree() && (shapes.contains(CompactingShape.THREE_BY_THREE_UNCRAFTABLE) || (shouldCompactNonUncraftable() && shapes.contains(CompactingShape.THREE_BY_THREE)))) {
			tryCompacting(inventoryHandler, item, 3, 3, ctx);
		} else if (shapes.contains(CompactingShape.TWO_BY_TWO_UNCRAFTABLE) || (shouldCompactNonUncraftable() && shapes.contains(CompactingShape.TWO_BY_TWO))) {
			tryCompacting(inventoryHandler, item, 2, 2, ctx);
		}
	}

	private void tryCompacting(IItemHandlerSimpleInserter inventoryHandler, Item item, int width, int height, TransactionContext ctx) {
		int totalCount = width * height;
		RecipeHelper.CompactingResult compactingResult = RecipeHelper.getCompactingResult(item, width, height);
		if (compactingResult.getCount() > 0) {
			long maxExtracted;
			try (Transaction nested = Transaction.openNested(ctx)) {
				maxExtracted = InventoryHelper.extractFromInventory(item, totalCount, inventoryHandler, nested);
			}

			if (maxExtracted != totalCount) {
				return;
			}

			ItemVariant resultCopy = compactingResult.getResult();
			while (maxExtracted == totalCount) {
				List<ItemStack> remainingItemsCopy = compactingResult.getRemainingItems().isEmpty() ? Collections.emptyList() : compactingResult.getRemainingItems().stream().map(ItemStack::copy).toList();

				if (!fitsResultAndRemainingItems(inventoryHandler, remainingItemsCopy, resultCopy, compactingResult.getCount(), ctx)) {
					break;
				}

				InventoryHelper.extractFromInventory(item, totalCount, inventoryHandler, ctx);
				inventoryHandler.insert(resultCopy, compactingResult.getCount(), ctx);
				InventoryHelper.insertIntoInventory(remainingItemsCopy, inventoryHandler, ctx);

				try (Transaction nested = Transaction.openNested(ctx)) {
					maxExtracted = InventoryHelper.extractFromInventory(item, totalCount, inventoryHandler, nested);
				}
			}
		}
	}

	private boolean fitsResultAndRemainingItems(SlotExposedStorage inventoryHandler, List<ItemStack> remainingItems, ItemVariant result, long count, @Nullable TransactionContext ctx) {
		if (!remainingItems.isEmpty()) {
			SlotExposedStorage clonedHandler = InventoryHelper.cloneInventory(inventoryHandler);
			return InventoryHelper.insertIntoInventory(result, count, clonedHandler, ctx) == count && InventoryHelper.insertIntoInventory(remainingItems, clonedHandler, ctx).isEmpty();
		}

		long ret;
		try (Transaction insertionTransaction = Transaction.openNested(ctx)) {
			ret = InventoryHelper.insertIntoInventory(result, count, inventoryHandler, insertionTransaction);
		}
		return ret == 0;
	}

	@Override
	public FilterLogic getFilterLogic() {
		return filterLogic;
	}

	public boolean shouldCompactNonUncraftable() {
		return NBTHelper.getBoolean(upgrade, "compactNonUncraftable").orElse(false);
	}

	public void setCompactNonUncraftable(boolean shouldCompactNonUncraftable) {
		NBTHelper.setBoolean(upgrade, "compactNonUncraftable", shouldCompactNonUncraftable);
		save();
	}

	@Override
	public void onSlotChange(SlotExposedStorage inventoryHandler, int slot) {
		if (shouldWorkInGUI()) {
			slotsToCompact.add(slot);
		}
	}

	public void setShouldWorkdInGUI(boolean shouldWorkdInGUI) {
		NBTHelper.setBoolean(upgrade, "shouldWorkInGUI", shouldWorkdInGUI);
		save();
	}

	public boolean shouldWorkInGUI() {
		return NBTHelper.getBoolean(upgrade, "shouldWorkInGUI").orElse(false);
	}

	@Override
	public void tick(@Nullable LivingEntity entity, Level world, BlockPos pos) {
		if (slotsToCompact.isEmpty()) {
			return;
		}

		try (Transaction ctx = Transaction.openOuter()) {
			for (int slot : slotsToCompact) {
				compactSlot(storageWrapper.getInventoryHandler(), slot, ctx);
			}
			ctx.commit();
		}

		slotsToCompact.clear();
	}
}
