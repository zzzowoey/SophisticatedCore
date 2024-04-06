package net.p3pp3rf1y.sophisticatedcore.upgrades;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public interface IRenderedTankUpgrade {
	void setTankRenderInfoUpdateCallback(Consumer<TankRenderInfo> updateTankRenderInfoCallback);

	void forceUpdateTankRenderInfo();

	class TankRenderInfo {
		private static final String FLUID_TAG = "fluid";
		private static final String FILL_RATIO_TAG = "fillRatio";

		public TankRenderInfo() {
			this(null, 0);
		}

		public TankRenderInfo(@Nullable FluidStack fluidStack, float fillRatio) {
			this.fluidStack = fluidStack;
			this.fillRatio = fillRatio;
		}

		@Nullable
		private FluidStack fluidStack;
		private float fillRatio;

		public CompoundTag serialize() {
			CompoundTag ret = new CompoundTag();
			if (fluidStack != null) {
				ret.put(FLUID_TAG, fluidStack.writeToNBT(new CompoundTag()));
				ret.putFloat(FILL_RATIO_TAG, fillRatio);
			}
			return ret;
		}

		public static TankRenderInfo deserialize(CompoundTag tag) {
			if (tag.contains(FLUID_TAG)) {
				return new TankRenderInfo( FluidStack.loadFluidStackFromNBT(tag.getCompound(FLUID_TAG)), tag.getFloat(FILL_RATIO_TAG));
			}

			return new TankRenderInfo();
		}

		public void setFluid(FluidStack fluidStack) {
			this.fluidStack = fluidStack.copy();
		}

		public Optional<FluidStack> getFluid() {
			return Optional.ofNullable(fluidStack);
		}

		public void setFillRatio(float fillRatio) {
			this.fillRatio = fillRatio;
		}

		public float getFillRatio() {
			return fillRatio;
		}
	}
}
