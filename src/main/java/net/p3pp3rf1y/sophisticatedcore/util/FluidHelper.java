package net.p3pp3rf1y.sophisticatedcore.util;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.world.item.ItemStack;

public class FluidHelper {
    public static boolean isFluidStorage(ItemStack stack) {
        return ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM) != null;
    }
}
