package net.p3pp3rf1y.sophisticatedcore.init;

import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import io.github.fabricators_of_create.porting_lib.util.SimpleFlowableFluid;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalFluidTags;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;

import java.util.function.Consumer;

public class ModFluids {
	private ModFluids() {}

	private static SimpleFlowableFluid.Properties fluidProperties() {
		return new SimpleFlowableFluid.Properties(XP_STILL, XP_FLOWING);
	}

	public static final ResourceLocation EXPERIENCE_TAG_NAME = new ResourceLocation("forge:experience");

	public static final TagKey<Fluid> EXPERIENCE_TAG = TagKey.create(Registries.FLUID, EXPERIENCE_TAG_NAME);
	public static final LazyRegistrar<Fluid> FLUIDS = LazyRegistrar.create(BuiltInRegistries.FLUID, SophisticatedCore.ID);

	public static final RegistryObject<FlowingFluid> XP_STILL = FLUIDS.register("xp_still", () -> new SimpleFlowableFluid.Still(fluidProperties()));

	public static final RegistryObject<FlowingFluid> XP_FLOWING = FLUIDS.register("xp_flowing", () -> new SimpleFlowableFluid.Flowing(fluidProperties()));
	/*public static final RegistryObject<FluidType> XP_FLUID_TYPE = FLUID_TYPES.register("experience", () -> new FluidType(FluidType.Properties.create().lightLevel(10).density(800).viscosity(1500)) {
		@Override
		public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
			consumer.accept(new IClientFluidTypeExtensions() {
				private static final ResourceLocation XP_STILL_TEXTURE = new ResourceLocation(SophisticatedCore.ID, "fluids/xp_still");
				private static final ResourceLocation XP_FLOWING_TEXTURE = new ResourceLocation(SophisticatedCore.ID, "fluids/xp_flowing");

				@Override
				public ResourceLocation getStillTexture() {
					return XP_STILL_TEXTURE;
				}

				@Override
				public ResourceLocation getFlowingTexture() {
					return XP_FLOWING_TEXTURE;
				}
			});
		}
	});*/
	
	public static void registerHandlers() {
		FLUIDS.register();
		//FLUID_TYPES.register(modBus);
	}
}
