package net.p3pp3rf1y.sophisticatedcore.client.gui.controls;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.upgrades.PrimaryMatch;

import java.util.Map;

import static net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.DEFAULT_BUTTON_BACKGROUND;
import static net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.DEFAULT_BUTTON_HOVERED_BACKGROUND;
import static net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.ICONS;
import static net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.SMALL_BUTTON_BACKGROUND;
import static net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.SMALL_BUTTON_HOVERED_BACKGROUND;
import static net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.getButtonStateData;

public class ButtonDefinitions {
	private ButtonDefinitions() {}

	public static Map<Boolean, ToggleButton.StateData> getBooleanStateData(ToggleButton.StateData onStateData, ToggleButton.StateData offStateData) {
		return Map.of(
				true, onStateData,
				false, offStateData
		);
	}

	public static <T extends Comparable<T>> ButtonDefinition.Toggle<T> createToggleButtonDefinition(Map<T, ToggleButton.StateData> stateData) {
		return new ButtonDefinition.Toggle<>(Dimension.SQUARE_18, DEFAULT_BUTTON_BACKGROUND, stateData, DEFAULT_BUTTON_HOVERED_BACKGROUND);
	}

	public static <T extends Comparable<T>> ButtonDefinition.Toggle<T> createSmallToggleButtonDefinition(Map<T, ToggleButton.StateData> stateData) {
		return new ButtonDefinition.Toggle<>(Dimension.SQUARE_12, SMALL_BUTTON_BACKGROUND, stateData, SMALL_BUTTON_HOVERED_BACKGROUND);
	}

	public static final ButtonDefinition.Toggle<Boolean> ALLOW_LIST = createToggleButtonDefinition(
			getBooleanStateData(
					getButtonStateData(new UV(0, 0), TranslationHelper.INSTANCE.translUpgradeButton("allow"), Dimension.SQUARE_16, new Position(1, 1)),
					getButtonStateData(new UV(16, 0), TranslationHelper.INSTANCE.translUpgradeButton("block"), Dimension.SQUARE_16, new Position(1, 1))
			));

	public static final ButtonDefinition.Toggle<Boolean> MATCH_DURABILITY = createToggleButtonDefinition(
			getBooleanStateData(
					getButtonStateData(new UV(0, 16), TranslationHelper.INSTANCE.translUpgradeButton("match_durability"), Dimension.SQUARE_16, new Position(1, 1)),
					getButtonStateData(new UV(16, 16), TranslationHelper.INSTANCE.translUpgradeButton("ignore_durability"), Dimension.SQUARE_16, new Position(1, 1))
			));

	public static final ButtonDefinition.Toggle<Boolean> MATCH_NBT = createToggleButtonDefinition(
			getBooleanStateData(
					getButtonStateData(new UV(32, 0), TranslationHelper.INSTANCE.translUpgradeButton("match_nbt"), Dimension.SQUARE_16, new Position(1, 1)),
					getButtonStateData(new UV(48, 0), TranslationHelper.INSTANCE.translUpgradeButton("ignore_nbt"), Dimension.SQUARE_16, new Position(1, 1))
			));

	public static final ButtonDefinition.Toggle<PrimaryMatch> PRIMARY_MATCH = createToggleButtonDefinition(
			Map.of(
					PrimaryMatch.ITEM, getButtonStateData(new UV(48, 16), TranslationHelper.INSTANCE.translUpgradeButton("match_item"), Dimension.SQUARE_16, new Position(1, 1)),
					PrimaryMatch.MOD, getButtonStateData(new UV(32, 16), TranslationHelper.INSTANCE.translUpgradeButton("match_mod"), Dimension.SQUARE_16, new Position(1, 1)),
					PrimaryMatch.TAGS, getButtonStateData(new UV(64, 0), TranslationHelper.INSTANCE.translUpgradeButton("match_tags"), Dimension.SQUARE_16, new Position(1, 1))
			));

	public static final ButtonDefinition.Toggle<SortBy> SORT_BY = createSmallToggleButtonDefinition(
			Map.of(
					SortBy.NAME, getButtonStateData(new UV(24, 144), TranslationHelper.INSTANCE.translButton("sort_by_name"), Dimension.SQUARE_12),
					SortBy.COUNT, getButtonStateData(new UV(36, 144), TranslationHelper.INSTANCE.translButton("sort_by_count"), Dimension.SQUARE_12),
					SortBy.TAGS, getButtonStateData(new UV(12, 144), TranslationHelper.INSTANCE.translButton("sort_by_tags"), Dimension.SQUARE_12)
			));

	private static final TextureBlitData SORT_BUTTON_FOREGROUND = new TextureBlitData(ICONS, Dimension.SQUARE_256, new UV(0, 144), Dimension.SQUARE_12);
	public static final ButtonDefinition SORT = new ButtonDefinition(Dimension.SQUARE_12, SMALL_BUTTON_BACKGROUND, SMALL_BUTTON_HOVERED_BACKGROUND, SORT_BUTTON_FOREGROUND,
			Component.translatable(TranslationHelper.INSTANCE.translButton("sort_action")));

	private static final TextureBlitData UPGRADE_SWITCH_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(65, 0), Dimension.RECTANGLE_6_12);
	private static final TextureBlitData UPGRADE_SWITCH_HOVERED_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(71, 0), Dimension.RECTANGLE_6_12);
	public static final ButtonDefinition.Toggle<Boolean> UPGRADE_SWITCH = new ButtonDefinition.Toggle<>(Dimension.RECTANGLE_6_12, UPGRADE_SWITCH_BACKGROUND, Map.of(
			true, getButtonStateData(new UV(4, 128), Dimension.RECTANGLE_4_10, new Position(1, 1), TranslationHelper.INSTANCE.translColoredButton("upgrade_switch_enabled", ChatFormatting.GREEN)),
			false, getButtonStateData(new UV(0, 128), Dimension.RECTANGLE_4_10, new Position(1, 1), TranslationHelper.INSTANCE.translColoredButton("upgrade_switch_disabled", ChatFormatting.RED))
	), UPGRADE_SWITCH_HOVERED_BACKGROUND);

	public static final ButtonDefinition.Toggle<Boolean> WORK_IN_GUI = createToggleButtonDefinition(
			Map.of(
					true, getButtonStateData(new UV(0, 48), TranslationHelper.INSTANCE.translUpgradeButton("works_in_gui"), Dimension.SQUARE_16, new Position(1, 1)),
					false, getButtonStateData(new UV(16, 48), TranslationHelper.INSTANCE.translUpgradeButton("only_automatic"), Dimension.SQUARE_16, new Position(1, 1))
			));

	public static final ButtonDefinition.Toggle<Boolean> MATCH_ANY_TAG = createToggleButtonDefinition(
			Map.of(
					true, getButtonStateData(new UV(0, 80), Dimension.SQUARE_16, new Position(1, 1),
							TranslationHelper.INSTANCE.getTranslatedLines(TranslationHelper.INSTANCE.translUpgradeButton("match_any_tag"))),
					false, getButtonStateData(new UV(16, 80), Dimension.SQUARE_16, new Position(1, 1),
							TranslationHelper.INSTANCE.getTranslatedLines(TranslationHelper.INSTANCE.translUpgradeButton("match_all_tags")))
			));

	private static final TextureBlitData ADD_TAG_FOREGROUND = new TextureBlitData(ICONS, new Position(1, 1), Dimension.SQUARE_256, new UV(96, 32), Dimension.SQUARE_16);
	public static final ButtonDefinition ADD_TAG = new ButtonDefinition(Dimension.SQUARE_18, DEFAULT_BUTTON_BACKGROUND, DEFAULT_BUTTON_HOVERED_BACKGROUND, ADD_TAG_FOREGROUND, Component.literal(""));

	private static final TextureBlitData REMOVE_TAG_FOREGROUND = new TextureBlitData(ICONS, new Position(1, 1), Dimension.SQUARE_256, new UV(112, 32), Dimension.SQUARE_16);
	public static final ButtonDefinition REMOVE_TAG = new ButtonDefinition(Dimension.SQUARE_18, DEFAULT_BUTTON_BACKGROUND, DEFAULT_BUTTON_HOVERED_BACKGROUND, REMOVE_TAG_FOREGROUND, Component.literal(""));
}
