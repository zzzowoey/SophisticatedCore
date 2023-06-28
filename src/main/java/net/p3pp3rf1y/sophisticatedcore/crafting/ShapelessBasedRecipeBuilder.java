package net.p3pp3rf1y.sophisticatedcore.crafting;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.p3pp3rf1y.sophisticatedcore.util.RegistryHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ShapelessBasedRecipeBuilder implements RecipeBuilder {
	private final RecipeCategory category;
	private final Item result;
	@Nullable
	private final CompoundTag nbt;
	private final int count;
	private final List<Ingredient> ingredients = Lists.newArrayList();
	private final Advancement.Builder advancement = Advancement.Builder.advancement();
	private final List<ConditionJsonProvider> conditions = new ArrayList<>();
	@Nullable
	private String group;
	private boolean showNotification = true;

	public ShapelessBasedRecipeBuilder(RecipeCategory category, ItemLike result, int count, @Nullable CompoundTag nbt) {
		this.category = category;
		this.result = result.asItem();
		this.count = count;
		this.nbt = nbt;
	}

	public static ShapelessBasedRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
		return shapeless(category, result, 1);
	}

	public static ShapelessBasedRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
		return new ShapelessBasedRecipeBuilder(category, result, count, null);
	}

	public static ShapelessBasedRecipeBuilder shapeless(RecipeCategory category, ItemStack stack) {
		return new ShapelessBasedRecipeBuilder(category, stack.getItem(), 1, stack.getTag());
	}

	public ShapelessBasedRecipeBuilder condition(ConditionJsonProvider condition) {
		conditions.add(condition);
		return this;
	}

	public ShapelessBasedRecipeBuilder requires(TagKey<Item> tag) {
		return requires(Ingredient.of(tag));
	}

	public ShapelessBasedRecipeBuilder requires(ItemLike item) {
		return requires(item, 1);
	}

	public ShapelessBasedRecipeBuilder requires(ItemLike item, int quantity) {
		for (int i = 0; i < quantity; ++i) {
			requires(Ingredient.of(item));
		}

		return this;
	}

	public ShapelessBasedRecipeBuilder requires(Ingredient ingredient) {
		return requires(ingredient, 1);
	}

	public ShapelessBasedRecipeBuilder requires(Ingredient ingredient, int quantity) {
		for (int i = 0; i < quantity; ++i) {
			ingredients.add(ingredient);
		}

		return this;
	}

	public ShapelessBasedRecipeBuilder unlockedBy(String criterionName, CriterionTriggerInstance criterionTrigger) {
		advancement.addCriterion(criterionName, criterionTrigger);
		return this;
	}

	public ShapelessBasedRecipeBuilder group(@Nullable String groupName) {
		group = groupName;
		return this;
	}

	public ShapelessBasedRecipeBuilder showNotification(boolean bl) {
		this.showNotification = bl;
		return this;
	}

	public Item getResult() {
		return result;
	}

	public void save(Consumer<FinishedRecipe> finishedRecipeConsumer, ResourceLocation recipeId) {
		advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).requirements(RequirementsStrategy.OR);
		finishedRecipeConsumer.accept(new Result(recipeId, result, conditions, nbt, count, group == null ? "" : group, determineBookCategory(this.category), ingredients, advancement, new ResourceLocation(recipeId.getNamespace(), "recipes/" + this.category.getFolderName() + "/" + recipeId.getPath()), this.showNotification));
	}

	protected static CraftingBookCategory determineBookCategory(RecipeCategory category) {
		CraftingBookCategory var10000;
		switch (category) {
			case BUILDING_BLOCKS:
				var10000 = CraftingBookCategory.BUILDING;
				break;
			case TOOLS:
			case COMBAT:
				var10000 = CraftingBookCategory.EQUIPMENT;
				break;
			case REDSTONE:
				var10000 = CraftingBookCategory.REDSTONE;
				break;
			default:
				var10000 = CraftingBookCategory.MISC;
		}

		return var10000;
	}
	public static class Result implements FinishedRecipe {
		private final List<ConditionJsonProvider> conditions;
		private final ResourceLocation id;
		private final Item itemResult;
		@Nullable
		private final CompoundTag nbt;
		private final int count;
		private final String group;
		private final CraftingBookCategory category;
		private final List<Ingredient> ingredients;
		private final Advancement.Builder advancement;
		private final ResourceLocation advancementId;
		private final boolean showNotification;

		@SuppressWarnings("java:S107") //the only way of reducing number of parameters here means adding pretty much unnecessary object parameter
		public Result(ResourceLocation id, Item itemResult, List<ConditionJsonProvider> conditions, @Nullable CompoundTag nbt,
				int count, String group, CraftingBookCategory craftingBookCategory, List<Ingredient> ingredients, Advancement.Builder advancement, ResourceLocation advancementId, boolean showNotification) {
			this.id = id;
			this.itemResult = itemResult;
			this.conditions = conditions;
			this.nbt = nbt;
			this.count = count;
			this.group = group;
			this.category = craftingBookCategory;
			this.ingredients = ingredients;
			this.advancement = advancement;
			this.advancementId = advancementId;
			this.showNotification = showNotification;

			conditions.add(new ItemEnabledCondition(this.itemResult));
		}

		public void serializeRecipeData(JsonObject json) {
			json.addProperty("category", this.category.getSerializedName());

			if (!group.isEmpty()) {
				json.addProperty("group", group);
			}

			JsonArray conditionsArray = new JsonArray();
			conditions.forEach(c -> conditionsArray.add(c.toJson()));
			json.add("conditions", conditionsArray);

			JsonArray jsonarray = new JsonArray();

			for (Ingredient ingredient : ingredients) {
				jsonarray.add(ingredient.toJson());
			}

			json.add("ingredients", jsonarray);
			JsonObject jsonobject = new JsonObject();
			jsonobject.addProperty("item", RegistryHelper.getItemKey(itemResult).toString());
			if (count > 1) {
				jsonobject.addProperty("count", count);
			}
			if (nbt != null) {
				jsonobject.addProperty("nbt", nbt.toString());
			}

			json.add("result", jsonobject);
			json.addProperty("show_notification", this.showNotification);
		}

		public RecipeSerializer<?> getType() {
			return RecipeSerializer.SHAPELESS_RECIPE;
		}

		public ResourceLocation getId() {
			return id;
		}

		@Nullable
		public JsonObject serializeAdvancement() {
			return advancement.serializeToJson();
		}

		@Nullable
		public ResourceLocation getAdvancementId() {
			return advancementId;
		}
	}
}