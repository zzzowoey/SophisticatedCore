package net.p3pp3rf1y.sophisticatedcore.crafting;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
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
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static net.minecraft.data.recipes.RecipeBuilder.ROOT_RECIPE_ADVANCEMENT;

public class ShapeBasedRecipeBuilder {
	private final RecipeCategory category;
	private final Item itemResult;
	@Nullable
	private final CompoundTag nbt;
	private final int count;
	private final List<ConditionJsonProvider> conditions = new ArrayList<>();
	private final List<String> pattern = new ArrayList<>();
	private final Map<Character, Ingredient> keyIngredients = Maps.newLinkedHashMap();
	private final RecipeSerializer<?> serializer;
	private final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();
	@Nullable
	private String group;

	public ShapeBasedRecipeBuilder(RecipeCategory category, ItemLike itemResult, int count, @Nullable CompoundTag nbt, RecipeSerializer<?> serializer) {
		this.category = category;
		this.itemResult = itemResult.asItem();
		this.count = count;
		this.nbt = nbt;
		this.serializer = serializer;
	}

	public static ShapeBasedRecipeBuilder shaped(RecipeCategory category, ItemLike itemResult) {
		return shaped(category, itemResult, 1, RecipeSerializer.SHAPED_RECIPE);
	}

	public static ShapeBasedRecipeBuilder shaped(RecipeCategory category, ItemLike itemResult, RecipeSerializer<?> serializer) {
		return shaped(category, itemResult, 1, serializer);
	}

	public static ShapeBasedRecipeBuilder shaped(RecipeCategory category, ItemLike itemResult, int count, RecipeSerializer<?> serializer) {
		return shaped(category, itemResult, count, null, serializer);
	}

	public static ShapeBasedRecipeBuilder shaped(RecipeCategory category, ItemLike itemResult, int count, @Nullable CompoundTag nbt, RecipeSerializer<?> serializer) {
		return new ShapeBasedRecipeBuilder(category, itemResult, count, nbt,serializer);
	}

	public static ShapeBasedRecipeBuilder shaped(RecipeCategory category, ItemStack stack) {
		return shaped(category, stack.getItem(), 1, stack.getTag(), RecipeSerializer.SHAPED_RECIPE);
	}

	public ShapeBasedRecipeBuilder define(Character symbol, TagKey<Item> tagIn) {
		return define(symbol, Ingredient.of(tagIn));
	}

	public ShapeBasedRecipeBuilder define(Character symbol, ItemLike itemIn) {
		return define(symbol, Ingredient.of(itemIn));
	}

	public ShapeBasedRecipeBuilder define(Character symbol, Ingredient ingredientIn) {
		if (keyIngredients.containsKey(symbol)) {
			throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
		} else if (symbol == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			keyIngredients.put(symbol, ingredientIn);
			return this;
		}
	}

	public ShapeBasedRecipeBuilder pattern(String patternIn) {
		if (!pattern.isEmpty() && patternIn.length() != pattern.get(0).length()) {
			throw new IllegalArgumentException("Pattern must be the same width on every line!");
		} else {
			pattern.add(patternIn);
			return this;
		}
	}

	public ShapeBasedRecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterion) {
		advancementBuilder.addCriterion(name, criterion);
		return this;
	}

	public ShapeBasedRecipeBuilder group(@org.jetbrains.annotations.Nullable String groupName) {
		this.group = groupName;
		return this;
	}

	public void save(Consumer<FinishedRecipe> consumerIn) {
		save(consumerIn, RegistryHelper.getItemKey(itemResult));
	}

	public void save(Consumer<FinishedRecipe> consumerIn, ResourceLocation id) {
		ensureValid(id);
		advancementBuilder.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);
		consumerIn.accept(new Result(id, itemResult, conditions, nbt, count, group == null ? "" : group, determineBookCategory(this.category), pattern, keyIngredients, advancementBuilder, new ResourceLocation(id.getNamespace(), "recipes/" + this.category.getFolderName() + "/" + id.getPath()), serializer));
	}

	private void ensureValid(ResourceLocation id) {
		if (pattern.isEmpty()) {
			throw new IllegalStateException("No pattern is defined for shaped recipe " + id + "!");
		}

		Set<Character> set = Sets.newHashSet(keyIngredients.keySet());
		set.remove(' ');

		for (String s : pattern) {
			for (int i = 0; i < s.length(); ++i) {
				char c0 = s.charAt(i);
				if (!keyIngredients.containsKey(c0) && c0 != ' ') {
					throw new IllegalStateException("Pattern in recipe " + id + " uses undefined symbol '" + c0 + "'");
				}

				set.remove(c0);
			}
		}

		if (!set.isEmpty()) {
			throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + id);
		} else if (pattern.size() == 1 && pattern.get(0).length() == 1) {
			throw new IllegalStateException("Shaped recipe " + id + " only takes in a single item - should it be a shapeless recipe instead?");
		}
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
		private final ResourceLocation id;
		private final List<ConditionJsonProvider> conditions;
		private final Item itemResult;
		@Nullable
		private final CompoundTag nbt;
		private final int count;
		private final String group;
		private final CraftingBookCategory category;
		private final List<String> pattern;
		private final Map<Character, Ingredient> key;
		private final ResourceLocation advancementId;
		private final Advancement.Builder advancementBuilder;
		private final RecipeSerializer<?> serializer;

		@SuppressWarnings("java:S107") //the only way of reducing number of parameters here means adding pretty much unnecessary object parameter
		public Result(ResourceLocation id, Item itemResult, List<ConditionJsonProvider> conditions, @Nullable CompoundTag nbt,
					  int count, String group, CraftingBookCategory craftingBookCategory, List<String> pattern, Map<Character, Ingredient> keyIngredients, Advancement.Builder advancementBuilder, ResourceLocation advancementId, RecipeSerializer<?> serializer) {
			this.id = id;
			this.conditions = conditions;
			this.itemResult = itemResult;
			this.nbt = nbt;
			this.count = count;
			this.group = group;
			this.category = craftingBookCategory;
			this.pattern = pattern;
			this.key = keyIngredients;
			this.advancementId = advancementId;
			this.advancementBuilder = advancementBuilder;
			this.serializer = serializer;

			conditions.add(new ItemEnabledCondition(itemResult));
		}

		public void serializeRecipeData(JsonObject json) {
			json.addProperty("category", this.category.getSerializedName());

			if (!this.group.isEmpty()) {
				json.addProperty("group", this.group);
			}

			JsonArray conditionsArray = new JsonArray();
			conditions.forEach(c -> conditionsArray.add(c.toJson()));
			json.add(ResourceConditions.CONDITIONS_KEY, conditionsArray);

			JsonArray jsonarray = new JsonArray();
			for (String s : pattern) {
				jsonarray.add(s);
			}
			json.add("pattern", jsonarray);
			JsonObject jsonobject = new JsonObject();

			for (Map.Entry<Character, Ingredient> entry : key.entrySet()) {
				jsonobject.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
			}

			json.add("key", jsonobject);
			JsonObject jsonobject1 = new JsonObject();
			jsonobject1.addProperty("item", RegistryHelper.getItemKey(itemResult).toString());
			if (count > 1) {
				jsonobject1.addProperty("count", count);
			}
			if (this.nbt != null) {
				jsonobject1.addProperty("nbt", nbt.toString());
			}

			json.add("result", jsonobject1);
		}

		public RecipeSerializer<?> getType() {
			return serializer;
		}

		public ResourceLocation getId() {
			return id;
		}

		@Nullable
		public JsonObject serializeAdvancement() {
			return advancementBuilder.serializeToJson();
		}

		@Nullable
		public ResourceLocation getAdvancementId() {
			return advancementId;
		}
	}
}
