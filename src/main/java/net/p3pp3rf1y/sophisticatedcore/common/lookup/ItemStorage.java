package net.p3pp3rf1y.sophisticatedcore.common.lookup;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.core.Direction;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import org.jetbrains.annotations.Nullable;

public class ItemStorage {
    public static final BlockApiLookup<SlotExposedStorage, @Nullable Direction> SIDED = BlockApiLookup.get(SophisticatedCore.getRL("sided_item_handler"), SlotExposedStorage.class, Direction.class);
    public static final ItemApiLookup<SlotExposedStorage, ContainerItemContext> ITEM = ItemApiLookup.get(SophisticatedCore.getRL("item_handler"), SlotExposedStorage.class, ContainerItemContext.class);
}
