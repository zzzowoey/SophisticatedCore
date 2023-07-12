package net.p3pp3rf1y.sophisticatedcore.inventory;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

// TODO: Temporary, remove once refactoring to InventoryStorage is done
public class SlotExposedStorageWrapper implements SlotExposedStorage {
    private InventoryStorage inventory;

    public SlotExposedStorageWrapper(InventoryStorage inventory) {
        this.inventory = inventory;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        SingleSlotStorage<ItemVariant> resource = this.inventory.getSlot(slot);
        return resource.getResource().toStack((int) resource.getAmount());
    }

    @Override
    public int getSlots() {
        return this.inventory.getSlotCount();
    }

    @Override
    public int getSlotLimit(int slot) {
        return getStackInSlot(slot).getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, ItemVariant resource, long amount) {
        return true;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return this.inventory.insert(resource, maxAmount, transaction);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return this.inventory.extract(resource, maxAmount, transaction);
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return this.inventory.iterator();
    }
}
