package net.p3pp3rf1y.sophisticatedcore.upgrades.tank;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeSlotChangeResult;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TankUpgradeItem extends UpgradeItemBase<TankUpgradeWrapper> {
	public static final UpgradeType<TankUpgradeWrapper> TYPE = new UpgradeType<>(TankUpgradeWrapper::new);

	private final TankUpgradeConfig tankUpgradeConfig;

	public TankUpgradeItem(TankUpgradeConfig tankUpgradeConfig) {
		super();
		this.tankUpgradeConfig = tankUpgradeConfig;
	}

	public long getBaseCapacity(IStorageWrapper storageWrapper) {
		// Config values are in millibuckets so we devide the bucket constant by 1000 to get the correct multiplier
		return (long) tankUpgradeConfig.capacityPerSlotRow.get() * storageWrapper.getNumberOfSlotRows() * (FluidConstants.BUCKET / 1000);
	}

	public int getAdjustedStackMultiplier(IStorageWrapper storageWrapper) {
		return 1 + (int) (tankUpgradeConfig.stackMultiplierRatio.get() * (storageWrapper.getInventoryHandler().getStackSizeMultiplier() - 1));
	}

	public long getTankCapacity(IStorageWrapper storageWrapper) {
		int stackMultiplier = getAdjustedStackMultiplier(storageWrapper);
		long baseCapacity = getBaseCapacity(storageWrapper);
		return Long.MAX_VALUE / stackMultiplier < baseCapacity ? Integer.MAX_VALUE : baseCapacity * stackMultiplier;
	}

	public TankUpgradeConfig getTankUpgradeConfig() {
		return tankUpgradeConfig;
	}

	@Override
	public UpgradeType<TankUpgradeWrapper> getType() {
		return TYPE;
	}

	@Override
	public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack, boolean firstLevelStorage, boolean isClientSide) {
		Set<Integer> errorUpgradeSlots = new HashSet<>();
		storageWrapper.getUpgradeHandler().getSlotWrappers().forEach((slot, wrapper) -> {
			if (wrapper instanceof TankUpgradeWrapper) {
				errorUpgradeSlots.add(slot);
			}
		});

		if (errorUpgradeSlots.size() >= 2) {
			return new UpgradeSlotChangeResult.Fail(TranslationHelper.INSTANCE.translError("add.two_tank_upgrades_present"), errorUpgradeSlots, Collections.emptySet(), Collections.emptySet());
		}

		int multiplierRequired = (int) Math.ceil((float) TankUpgradeWrapper.getContents(upgradeStack).getAmount() / getTankCapacity(storageWrapper));
		if (multiplierRequired > 1) {
			return new UpgradeSlotChangeResult.Fail(TranslationHelper.INSTANCE.translError("add.tank_capacity_high", multiplierRequired), Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
		}

		return new UpgradeSlotChangeResult.Success();
	}

	@Override
	public int getInventoryColumnsTaken() {
		return 2;
	}
}