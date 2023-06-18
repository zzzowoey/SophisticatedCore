package net.p3pp3rf1y.sophisticatedcore.common.gui;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class SlotSuppliedHandler extends SlotItemHandler {
	private final Supplier<SlotExposedStorage> itemHandlerSupplier;
	private final int slot;

	public SlotSuppliedHandler(Supplier<SlotExposedStorage> itemHandlerSupplier, int slot, int xPosition, int yPosition) {
		super(itemHandlerSupplier.get(), slot, xPosition, yPosition);

		this.itemHandlerSupplier = itemHandlerSupplier;
		this.slot = slot;
	}

	@Override
	public SlotExposedStorage getItemHandler() {
		return itemHandlerSupplier.get();
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return itemHandlerSupplier.get().isItemValid(slot, ItemVariant.of(stack), stack.getCount());
	}

	@Override
	public int getMaxStackSize() {
		return itemHandlerSupplier.get().getSlotLimit(slot);
	}
}
