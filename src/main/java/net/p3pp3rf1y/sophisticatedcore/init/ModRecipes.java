package net.p3pp3rf1y.sophisticatedcore.init;

import io.github.fabricators_of_create.porting_lib.util.IdentifiableSimplePreparableReloadListener;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.crafting.ItemEnabledCondition;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeClearRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe;

public class ModRecipes {
	private static final LazyRegistrar<RecipeSerializer<?>> RECIPE_SERIALIZERS = LazyRegistrar.create(Registries.RECIPE_SERIALIZER, SophisticatedCore.ID);
	public static final RegistryObject<RecipeSerializer<?>> UPGRADE_NEXT_TIER_SERIALIZER = RECIPE_SERIALIZERS.register("upgrade_next_tier", UpgradeNextTierRecipe.Serializer::new);
	public static final RegistryObject<SimpleCraftingRecipeSerializer<?>> UPGRADE_CLEAR_SERIALIZER = RECIPE_SERIALIZERS.register("upgrade_clear", () -> new SimpleCraftingRecipeSerializer<>(UpgradeClearRecipe::new));

	public static void registerHandlers() {
		RECIPE_SERIALIZERS.register();

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableSimplePreparableReloadListener(SophisticatedCore.getRL("modrecipes")) {
			@Override
			protected Object prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
				return null;
			}

			@Override
			protected void apply(Object object, ResourceManager resourceManager, ProfilerFiller profiler) {
				UpgradeNextTierRecipe.REGISTERED_RECIPES.clear();
			}
		});

		ResourceConditions.register(ItemEnabledCondition.ID, ItemEnabledCondition::test);
	}
}
