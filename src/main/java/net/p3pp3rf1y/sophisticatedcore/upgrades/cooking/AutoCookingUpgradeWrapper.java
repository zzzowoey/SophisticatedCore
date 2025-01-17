package net.p3pp3rf1y.sophisticatedcore.upgrades.cooking;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public class AutoCookingUpgradeWrapper<W extends AutoCookingUpgradeWrapper<W, U, R>, U extends UpgradeItemBase<W> & IAutoCookingUpgradeItem, R extends AbstractCookingRecipe>
		extends UpgradeWrapperBase<W, U>
		implements ITickableUpgrade, ICookingUpgrade<R> {
	private static final int NOTHING_TO_DO_COOLDOWN = 10;
	private static final int NO_INVENTORY_SPACE_COOLDOWN = 60;

	private final FilterLogic inputFilterLogic;
	private final FilterLogic fuelFilterLogic;
	private final CookingLogic<R> cookingLogic;
	private final Predicate<ItemStack> isValidInput;
	private final Predicate<ItemStack> isValidFuel;
	private int outputCooldown = 0;
	private int fuelCooldown = 0;
	private int inputCooldown = 0;

	public AutoCookingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler, RecipeType<R> recipeType, float burnTimeModifier) {
		super(storageWrapper, upgrade, upgradeSaveHandler);
		AutoCookingUpgradeConfig autoCookingUpgradeConfig = upgradeItem.getAutoCookingUpgradeConfig();
		inputFilterLogic = new FilterLogic(upgrade, upgradeSaveHandler, autoCookingUpgradeConfig.inputFilterSlots.get(),
				s -> RecipeHelper.getCookingRecipe(s, recipeType).isPresent(), "inputFilter");
		fuelFilterLogic = new FilterLogic(upgrade, upgradeSaveHandler, autoCookingUpgradeConfig.fuelFilterSlots.get(),
				s -> Objects.requireNonNullElse(FuelRegistry.INSTANCE.get(s.getItem()), 0) > 0, "fuelFilter");
		fuelFilterLogic.setAllowByDefault(true);
		fuelFilterLogic.setEmptyAllowListMatchesEverything();

		isValidInput = s -> RecipeHelper.getCookingRecipe(s, recipeType).isPresent() && inputFilterLogic.matchesFilter(s);
		isValidFuel = s -> Objects.requireNonNullElse(FuelRegistry.INSTANCE.get(s.getItem()), 0) > 0 && fuelFilterLogic.matchesFilter(s);
		cookingLogic = new CookingLogic<>(upgrade, upgradeSaveHandler, isValidFuel, isValidInput, autoCookingUpgradeConfig, recipeType, burnTimeModifier);
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (!enabled) {
			pauseAndRemoveRenderInfo();
		}
		super.setEnabled(enabled);
	}

	private void pauseAndRemoveRenderInfo() {
		cookingLogic.pause();
		RenderInfo renderInfo = storageWrapper.getRenderInfo();
		renderInfo.removeUpgradeRenderData(CookingUpgradeRenderData.TYPE);
	}

	@Override
	public void onBeforeRemoved() {
		pauseAndRemoveRenderInfo();
	}

	private void tryPushingOutput() {
		if (outputCooldown > 0) {
			outputCooldown--;
			return;
		}

		try (Transaction ctx = Transaction.openOuter()) {
			ItemStack output = cookingLogic.getCookOutput();
			ItemVariant outputResource = ItemVariant.of(output);
			IItemHandlerSimpleInserter inventory = storageWrapper.getInventoryForUpgradeProcessing();
			if (!output.isEmpty() && StorageUtil.simulateInsert(inventory, outputResource, output.getCount(), ctx) > 0) {
				long ret = inventory.insert(outputResource, output.getCount(), ctx);
				cookingLogic.getCookingInventory().extractSlot(CookingLogic.COOK_OUTPUT_SLOT, outputResource, ret, ctx);
			} else {
				outputCooldown = NO_INVENTORY_SPACE_COOLDOWN;
			}

			ItemStack fuel = cookingLogic.getFuel();
			ItemVariant fuelResource = ItemVariant.of(fuel);
			if (!fuel.isEmpty() && Objects.requireNonNullElse(FuelRegistry.INSTANCE.get(fuelResource.getItem()), 0) <= 0 && StorageUtil.simulateInsert(inventory, fuelResource, fuel.getCount(), ctx) > 0) {
				long ret = inventory.insert(fuelResource, fuel.getCount(), ctx);
				cookingLogic.getCookingInventory().extractSlot(CookingLogic.FUEL_SLOT, fuelResource, ret, ctx);
			}

			ctx.commit();
		}
	}

	@Override
	public void tick(@Nullable LivingEntity entity, Level world, BlockPos pos) {
		if (isInCooldown(world)) {
			return;
		}
		tryPushingOutput();
		tryPullingFuel();
		tryPullingInput();

		if (!cookingLogic.tick(world) && outputCooldown <= 0 && fuelCooldown <= 0 && inputCooldown <= 0) {
			setCooldown(world, NOTHING_TO_DO_COOLDOWN);
		}
		boolean isBurning = cookingLogic.isBurning(world);
		RenderInfo renderInfo = storageWrapper.getRenderInfo();
		if (renderInfo.getUpgradeRenderData(CookingUpgradeRenderData.TYPE).map(CookingUpgradeRenderData::isBurning).orElse(false) != isBurning) {
			if (isBurning) {
				renderInfo.setUpgradeRenderData(CookingUpgradeRenderData.TYPE, new CookingUpgradeRenderData(true));
			} else {
				renderInfo.removeUpgradeRenderData(CookingUpgradeRenderData.TYPE);
			}
		}
	}

	private void tryPullingInput() {
		if (inputCooldown > 0) {
			inputCooldown--;
			return;
		}

		if (tryPullingGetUnsucessful(cookingLogic.getCookInput(), cookingLogic::setCookInput, isValidInput)) {
			inputCooldown = NO_INVENTORY_SPACE_COOLDOWN;
		}
	}

	private void tryPullingFuel() {
		if (fuelCooldown > 0) {
			fuelCooldown--;
			return;
		}

		if (tryPullingGetUnsucessful(cookingLogic.getFuel(), cookingLogic::setFuel, isValidFuel)) {
			fuelCooldown = NO_INVENTORY_SPACE_COOLDOWN;
		}
	}

	private boolean tryPullingGetUnsucessful(ItemStack stack, Consumer<ItemStack> setSlot, Predicate<ItemStack> isItemValid) {
		ResourceAmount<ItemVariant> toExtract = null;
		Storage<ItemVariant> inventory = storageWrapper.getInventoryForUpgradeProcessing();
		if (stack.isEmpty()) {
			for (var view : inventory.nonEmptyViews()) {
				ItemStack ret = view.getResource().toStack((int) view.getAmount());
				if (isItemValid.test(ret)) {
					toExtract = new ResourceAmount<>(view.getResource(), ret.getMaxStackSize());
					break;
				}
			}

			if (toExtract == null) {
				return true;
			}
		} else if (stack.getCount() == stack.getMaxStackSize() || !isItemValid.test(stack)) {
			return true;
		} else {
			toExtract = new ResourceAmount<>(ItemVariant.of(stack), stack.getMaxStackSize() - stack.getCount());
		}

		try (Transaction ctx = Transaction.openOuter()) {
			long extracted = inventory.extract(toExtract.resource(), toExtract.amount(), ctx);
			if (extracted > 0) {
				ItemStack toSet = toExtract.resource().toStack((int) extracted);
				toSet.grow(stack.getCount());
				setSlot.accept(toSet);
				ctx.commit();
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public CookingLogic<R> getCookingLogic() {
		return cookingLogic;
	}

	public FilterLogic getInputFilterLogic() {
		return inputFilterLogic;
	}

	public FilterLogic getFuelFilterLogic() {
		return fuelFilterLogic;
	}

	public static class AutoSmeltingUpgradeWrapper extends AutoCookingUpgradeWrapper<AutoSmeltingUpgradeWrapper, AutoSmeltingUpgradeItem, SmeltingRecipe> {
		public AutoSmeltingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
			super(storageWrapper, upgrade, upgradeSaveHandler, RecipeType.SMELTING, 1);
		}
	}

	public static class AutoSmokingUpgradeWrapper extends AutoCookingUpgradeWrapper<AutoSmokingUpgradeWrapper, AutoSmokingUpgradeItem, SmokingRecipe> {
		public AutoSmokingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
			super(storageWrapper, upgrade, upgradeSaveHandler, RecipeType.SMOKING, 0.5f);
		}
	}

	public static class AutoBlastingUpgradeWrapper extends AutoCookingUpgradeWrapper<AutoBlastingUpgradeWrapper, AutoBlastingUpgradeItem, BlastingRecipe> {
		public AutoBlastingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
			super(storageWrapper, upgrade, upgradeSaveHandler, RecipeType.BLASTING, 0.5f);
		}
	}
}
