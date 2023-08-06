package net.p3pp3rf1y.sophisticatedcore.inventory;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

// TODO: Temporary, remove once refactoring to InventoryStorage is done
public class SlottedStackStorageWrapper implements SlottedStackStorage {
    private InventoryStorage inventory;

    public SlottedStackStorageWrapper(InventoryStorage inventory) {
        this.inventory = inventory;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        SingleSlotStorage<ItemVariant> resource = this.inventory.getSlot(slot);
        return resource.getResource().toStack((int) resource.getAmount());
    }

    @Override
    public void setStackInSlot(int i, @NotNull ItemStack itemStack) {
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return this.inventory.getSlot(slot);
    }

    @Override
    public int getSlotCount() {
        return this.inventory.getSlotCount();
    }

    @Override
    public int getSlotLimit(int slot) {
        return getStackInSlot(slot).getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, ItemVariant resource) {
        return true;
    }

    @Override
    public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (isItemValid(slot, resource))
            return insert(resource, maxAmount, transaction);

        return 0;
    }
    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return this.inventory.insert(resource, maxAmount, transaction);
    }

    @Override
    public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return extract(resource, maxAmount, transaction);
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
