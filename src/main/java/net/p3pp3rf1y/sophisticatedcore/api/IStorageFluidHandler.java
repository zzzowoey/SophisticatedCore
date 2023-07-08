package net.p3pp3rf1y.sophisticatedcore.api;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public interface IStorageFluidHandler extends Storage<FluidVariant> {
	/*default long insert(TagKey<Fluid> fluidTag, int maxFill, Fluid fallbackFluid, TransactionContext ctx) {
		return insert(fluidTag, maxFill, fallbackFluid, ctx, false);
	}

	default long insert(TagKey<Fluid> fluidTag, int maxFill, Fluid fallbackFluid, TransactionContext ctx, boolean ignoreInOutLimit) {
        for (StorageView<FluidVariant> view : TransferUtil.getNonEmpty(this)) {
            if (view.getResource().getFluid().defaultFluidState().is(fluidTag)) {
                return insert(view.getResource(), maxFill, ctx, ignoreInOutLimit);
            }
        }

		return insert(FluidVariant.of(fallbackFluid), maxFill, ctx, ignoreInOutLimit);
	}*/

	long insert(FluidVariant resource, long maxFill, TransactionContext ctx, boolean ignoreInOutLimit);

	long extract(FluidVariant resource, long maxDrain, TransactionContext ctx, boolean ignoreInOutLimit);

	/*FluidStack extract(TagKey<Fluid> resourceTag, long maxDrain, TransactionContext ctx, boolean ignoreInOutLimit);*/

	FluidStack extract(int maxDrain, TransactionContext ctx, boolean ignoreInOutLimit);

	FluidStack extract(FluidStack resource, TransactionContext ctx, boolean ignoreInOutLimit);
}
