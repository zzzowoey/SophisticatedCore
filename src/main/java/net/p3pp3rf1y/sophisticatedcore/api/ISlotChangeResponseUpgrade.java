package net.p3pp3rf1y.sophisticatedcore.api;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage;

public interface ISlotChangeResponseUpgrade {
	void onSlotChange(SlotExposedStorage inventoryHandler, int slot);
}
