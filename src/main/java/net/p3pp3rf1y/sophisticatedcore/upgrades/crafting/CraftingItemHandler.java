package net.p3pp3rf1y.sophisticatedcore.upgrades.crafting;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CraftingItemHandler extends CraftingContainer {
	private final Supplier<SlotExposedStorage> supplyInventory;
	private final Consumer<Container> onCraftingMatrixChanged;

	public CraftingItemHandler(Supplier<SlotExposedStorage> supplyInventory, Consumer<Container> onCraftingMatrixChanged) {
		super(new AbstractContainerMenu(null, -1) {
			@Override
			public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
				return ItemStack.EMPTY;
			}

			@Override
			public boolean stillValid(Player playerIn) {
				return false;
			}
		}, 3, 3);
		this.supplyInventory = supplyInventory;
		this.onCraftingMatrixChanged = onCraftingMatrixChanged;
	}

	@Override
	public int getContainerSize() {
		return supplyInventory.get().getSlots();
	}

	@Override
	public boolean isEmpty() {
		return InventoryHelper.isEmpty(supplyInventory.get());
	}

	@Override
	public ItemStack getItem(int index) {
		SlotExposedStorage itemHandler = supplyInventory.get();
		return index >= itemHandler.getSlots() ? ItemStack.EMPTY : itemHandler.getStackInSlot(index);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return InventoryHelper.getAndRemove(supplyInventory.get(), index);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		ItemVariant resource = ItemVariant.of(supplyInventory.get().getStackInSlot(index));

		long amount = supplyInventory.get().extractSlot(index, resource, count, null);
		if (amount > 0) {
			onCraftingMatrixChanged.accept(this);
		}

		return resource.toStack((int) amount);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		supplyInventory.get().setStackInSlot(index, stack);
		onCraftingMatrixChanged.accept(this);
	}

	@Override
	public void fillStackedContents(StackedContents helper) {
		InventoryHelper.iterate(supplyInventory.get(), (slot, stack) -> helper.accountSimpleStack(stack));
	}

}
