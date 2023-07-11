package net.p3pp3rf1y.sophisticatedcore.upgrades.cooking;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SlotSuppliedHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CookingLogicContainer<T extends AbstractCookingRecipe> {
	private final Supplier<CookingLogic<T>> supplyCookingLogic;

	private final List<Slot> smeltingSlots = new ArrayList<>();

	public CookingLogicContainer(Supplier<CookingLogic<T>> supplyCookingLogic, Consumer<Slot> addSlot) {
		this.supplyCookingLogic = supplyCookingLogic;

		addSmeltingSlot(addSlot, new SlotSuppliedHandler(() -> supplyCookingLogic.get().getCookingInventory(), CookingLogic.COOK_INPUT_SLOT, -100, -100));
		addSmeltingSlot(addSlot, new SlotSuppliedHandler(() -> supplyCookingLogic.get().getCookingInventory(), CookingLogic.FUEL_SLOT, -100, -100));
		addSmeltingSlot(addSlot, new SlotSuppliedHandler(() -> supplyCookingLogic.get().getCookingInventory(), CookingLogic.COOK_OUTPUT_SLOT, -100, -100) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false; //needs to not allow player putting anything in
			}
		});
	}

	private void addSmeltingSlot(Consumer<Slot> addSlot, Slot slot) {
		addSlot.accept(slot);
		smeltingSlots.add(slot);
	}

	public int getBurnTimeTotal() {
		return supplyCookingLogic.get().getBurnTimeTotal();
	}

	public long getBurnTimeFinish() {
		return supplyCookingLogic.get().getBurnTimeFinish();
	}

	public long getCookTimeFinish() {
		return supplyCookingLogic.get().getCookTimeFinish();
	}

	public int getCookTimeTotal() {
		return supplyCookingLogic.get().getCookTimeTotal();
	}

	public boolean isCooking() {
		return supplyCookingLogic.get().isCooking();
	}

	public boolean isBurning(Level world) {
		return supplyCookingLogic.get().isBurning(world);
	}

	public List<Slot> getCookingSlots() {
		return smeltingSlots;
	}
}
