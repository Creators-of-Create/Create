package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;

import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum AllGuiTextures implements ScreenElement, TextureSheetSegment {

	// Inventories
	PLAYER_INVENTORY("player_inventory", 176, 108),
	WAND_OF_SYMMETRY("curiosities", 0, 131, 188, 101),
	BLOCKZAPPER("curiosities", 0, 99, 214, 97),
	TERRAINZAPPER("curiosities", 234, 103),
	TERRAINZAPPER_INACTIVE_PARAM("curiosities", 238, 0, 18, 18),

	LOGO("logo", 256, 256),
	CURSEFORGE_LOGO("platform_icons/curseforge", 256, 256),
	MODRINTH_LOGO("platform_icons/modrinth", 256, 256),

	SCHEMATIC("schematics", 192, 121),
	SCHEMATIC_SLOT("widgets", 54, 0, 16, 16),
	SCHEMATIC_PROMPT("schematics_2", 213, 77),
	HUD_BACKGROUND("overlay", 0, 0, 16, 16),

	SCHEMATIC_TABLE("schematics", 0, 121, 214, 83),
	SCHEMATIC_TABLE_PROGRESS("schematics", 0, 204, 84, 16),

	SCHEMATICANNON_TOP("schematics_2", 0, 77, 213, 42),
	SCHEMATICANNON_BOTTOM("schematics_2", 0, 119, 213, 99),
	SCHEMATICANNON_PROGRESS("schematics_2", 76, 239, 114, 16),
	SCHEMATICANNON_CHECKLIST_PROGRESS("schematics_2", 191, 240, 16, 14),
	SCHEMATICANNON_HIGHLIGHT("schematics_2", 1, 229, 26, 26),
	SCHEMATICANNON_FUEL("schematics_2", 28, 222, 47, 16),
	SCHEMATICANNON_FUEL_CREATIVE("schematics_2", 28, 239, 47, 16),

	STOCKSWITCH("logistics", 182, 95),
	STOCKSWITCH_ARROW_UP("logistics", 191, 0, 7, 24),
	STOCKSWITCH_ARROW_DOWN("logistics", 198, 0, 7, 24),
	STOCKSWITCH_CURSOR("logistics", 206, 0, 7, 16),
	STOCKSWITCH_INTERVAL("logistics", 0, 95, 100, 18),
	STOCKSWITCH_UNPOWERED_LANE("logistics", 37, 20, 100, 18),
	STOCKSWITCH_POWERED_LANE("logistics", 37, 42, 100, 18),

	FILTER("filters", 214, 99),
	ATTRIBUTE_FILTER("filters", 0, 99, 241, 85),

	TOOLBOX("toolbox", 188, 171),
	TOOLBELT_SLOT("minecraft", "widgets", 24, 23, 22, 22),
	TOOLBELT_SLOT_HIGHLIGHT("minecraft", "widgets", 0, 22, 24, 24),
	TOOLBELT_MAIN_SLOT("widgets", 0, 97, 24, 24),
	TOOLBELT_EMPTY_SLOT("widgets", 27, 98, 22, 22),
	TOOLBELT_INACTIVE_SLOT("widgets", 52, 98, 22, 22),

	TOOLBELT_HOTBAR_OFF("widgets", 0, 130, 20, 24),
	TOOLBELT_HOTBAR_ON("widgets", 20, 130, 20, 24),
	TOOLBELT_SELECTED_OFF("widgets", 0, 155, 22, 22),
	TOOLBELT_SELECTED_ON("widgets", 22, 155, 22, 22),

	SEQUENCER("sequencer", 173, 161),
	SEQUENCER_INSTRUCTION("sequencer", 0, 16, 162, 22),
	SEQUENCER_DELAY("sequencer", 0, 60, 162, 22),
	SEQUENCER_END("sequencer", 0, 82, 162, 22),
	SEQUENCER_EMPTY("sequencer", 0, 104, 162, 22),
	SEQUENCER_AWAIT("sequencer", 0, 162, 162, 22),

	LINKED_CONTROLLER("curiosities_2", 179, 109),
	BLUEPRINT("curiosities_2", 0, 109, 179, 109),

	CLIPBOARD("clipboard", 0, 0, 256, 256),

	DATA_GATHERER("display_link", 235, 162),
	DATA_AREA_START("display_link", 0, 163, 2, 18),
	DATA_AREA_SPEECH("display_link", 8, 163, 5, 18),
	DATA_AREA("display_link", 3, 163, 1, 18),
	DATA_AREA_END("display_link", 5, 163, 2, 18),

	SCHEDULE("schedule", 256, 226),
	SCHEDULE_CARD_DARK("schedule", 5, 233, 1, 1),
	SCHEDULE_CARD_MEDIUM("schedule", 6, 233, 1, 1),
	SCHEDULE_CARD_LIGHT("schedule", 7, 233, 1, 1),
	SCHEDULE_CARD_MOVE_UP("schedule", 51, 230, 12, 12),
	SCHEDULE_CARD_MOVE_DOWN("schedule", 65, 230, 12, 12),
	SCHEDULE_CARD_REMOVE("schedule", 51, 243, 12, 12),
	SCHEDULE_CARD_DUPLICATE("schedule", 65, 243, 12, 12),
	SCHEDULE_CARD_NEW("schedule", 79, 239, 16, 16),
	SCHEDULE_CONDITION_NEW("schedule", 96, 239, 19, 16),
	SCHEDULE_CONDITION_LEFT("schedule", 116, 239, 6, 16),
	SCHEDULE_CONDITION_LEFT_CLEAN("schedule", 147, 239, 2, 16),
	SCHEDULE_CONDITION_MIDDLE("schedule", 123, 239, 1, 16),
	SCHEDULE_CONDITION_ITEM("schedule", 125, 239, 18, 16),
	SCHEDULE_CONDITION_RIGHT("schedule", 144, 239, 2, 16),
	SCHEDULE_CONDITION_APPEND("schedule", 150, 245, 10, 10),
	SCHEDULE_SCROLL_LEFT("schedule", 161, 247, 4, 8),
	SCHEDULE_SCROLL_RIGHT("schedule", 166, 247, 4, 8),
	SCHEDULE_STRIP_DARK("schedule", 5, 235, 3, 1),
	SCHEDULE_STRIP_LIGHT("schedule", 5, 237, 3, 1),
	SCHEDULE_STRIP_WAIT("schedule", 1, 239, 11, 16),
	SCHEDULE_STRIP_TRAVEL("schedule", 12, 239, 11, 16),
	SCHEDULE_STRIP_DOTTED("schedule", 23, 239, 11, 16),
	SCHEDULE_STRIP_END("schedule", 34, 239, 11, 16),
	SCHEDULE_STRIP_ACTION("schedule", 209, 239, 11, 16),
	SCHEDULE_EDITOR("schedule_2", 256, 89),
	SCHEDULE_EDITOR_ADDITIONAL_SLOT("schedule_2", 55, 47, 32, 18),
	SCHEDULE_EDITOR_INACTIVE_SLOT("schedule_2", 0, 91, 18, 18),
	SCHEDULE_POINTER("schedule", 185, 239, 21, 16),
	SCHEDULE_POINTER_OFFSCREEN("schedule", 171, 239, 13, 16),

	STATION("schedule_2", 0, 111, 200, 127),
	STATION_ASSEMBLING("assemble", 200, 178),
	STATION_TEXTBOX_TOP("assemble", 1, 179, 150, 18),
	STATION_TEXTBOX_MIDDLE("assemble", 1, 198, 150, 1),
	STATION_TEXTBOX_BOTTOM("assemble", 1, 200, 150, 4),
	STATION_TEXTBOX_SPEECH("assemble", 152, 179, 8, 6),
	STATION_EDIT_NAME("schedule_2", 0, 239, 13, 13),
	STATION_EDIT_TRAIN_NAME("schedule_2", 89, 239, 13, 13),
	I_NEW_TRAIN("schedule_2", 14, 239, 24, 16),
	I_DISASSEMBLE_TRAIN("schedule_2", 39, 239, 24, 16),
	I_ASSEMBLE_TRAIN("schedule_2", 64, 239, 24, 16),

	ELEVATOR_CONTACT("display_link", 20, 172, 233, 82),

	BRASS_FRAME_TL("value_settings", 65, 9, 4, 4),
	BRASS_FRAME_TR("value_settings", 70, 9, 4, 4),
	BRASS_FRAME_BL("value_settings", 65, 19, 4, 4),
	BRASS_FRAME_BR("value_settings", 70, 19, 4, 4),
	BRASS_FRAME_LEFT("value_settings", 65, 14, 3, 4),
	BRASS_FRAME_RIGHT("value_settings", 71, 14, 3, 4),
	BRASS_FRAME_TOP("value_settings", 0, 24, 256, 3),
	BRASS_FRAME_BOTTOM("value_settings", 0, 27, 256, 3),

	VALUE_SETTINGS_MILESTONE("value_settings", 0, 0, 7, 8),
	VALUE_SETTINGS_WIDE_MILESTONE("value_settings", 75, 14, 13, 8),
	VALUE_SETTINGS_BAR("value_settings", 7, 0, 249, 8),
	VALUE_SETTINGS_BAR_BG("value_settings", 75, 9, 1, 1),
	VALUE_SETTINGS_OUTER_BG("value_settings", 80, 9, 1, 1),
	VALUE_SETTINGS_CURSOR_LEFT("value_settings", 0, 9, 3, 14),
	VALUE_SETTINGS_CURSOR("value_settings", 4, 9, 56, 14),
	VALUE_SETTINGS_CURSOR_RIGHT("value_settings", 61, 9, 3, 14),
	VALUE_SETTINGS_CURSOR_ICON("value_settings", 0, 44, 22, 20),
	VALUE_SETTINGS_LABEL_BG("value_settings", 0, 31, 81, 11),

	// JEI
	JEI_SLOT("jei/widgets", 18, 18),
	JEI_CHANCE_SLOT("jei/widgets", 20, 156, 18, 18),
	JEI_CATALYST_SLOT("jei/widgets", 0, 156, 18, 18),
	JEI_ARROW("jei/widgets", 19, 10, 42, 10),
	JEI_LONG_ARROW("jei/widgets", 19, 0, 71, 10),
	JEI_DOWN_ARROW("jei/widgets", 0, 21, 18, 14),
	JEI_LIGHT("jei/widgets", 0, 42, 52, 11),
	JEI_QUESTION_MARK("jei/widgets", 0, 178, 12, 16),
	JEI_SHADOW("jei/widgets", 0, 56, 52, 11),
	BLOCKZAPPER_UPGRADE_RECIPE("jei/widgets", 0, 75, 144, 66),
	JEI_HEAT_BAR("jei/widgets", 0, 201, 169, 19),
	JEI_NO_HEAT_BAR("jei/widgets", 0, 221, 169, 19),

	// Widgets
	BUTTON("widgets", 18, 18),
	BUTTON_HOVER("widgets", 18, 0, 18, 18),
	BUTTON_DOWN("widgets", 36, 0, 18, 18),
	INDICATOR("widgets", 0, 18, 18, 6),
	INDICATOR_WHITE("widgets", 18, 18, 18, 6),
	INDICATOR_GREEN("widgets", 36, 18, 18, 6),
	INDICATOR_YELLOW("widgets", 54, 18, 18, 6),
	INDICATOR_RED("widgets", 72, 18, 18, 6),

	HOTSLOT_ARROW("widgets", 24, 51, 20, 12),
	HOTSLOT("widgets", 0, 68, 22, 22),
	HOTSLOT_ACTIVE("widgets", 0, 46, 22, 22),
	HOTSLOT_SUPER_ACTIVE("widgets", 27, 67, 24, 24),

	SPEECH_TOOLTIP_BACKGROUND("widgets", 0, 24, 8, 8),
	SPEECH_TOOLTIP_COLOR("widgets", 8, 24, 8, 8),

	TRAIN_HUD_SPEED_BG("widgets", 0, 190, 182, 5),
	TRAIN_HUD_SPEED("widgets", 0, 185, 182, 5),
	TRAIN_HUD_THROTTLE("widgets", 0, 195, 182, 5),
	TRAIN_HUD_THROTTLE_POINTER("widgets", 0, 209, 6, 9),
	TRAIN_HUD_FRAME("widgets", 0, 200, 186, 7),
	TRAIN_HUD_DIRECTION("widgets", 77, 165, 28, 20),
	TRAIN_PROMPT_L("widgets", 8, 209, 3, 16),
	TRAIN_PROMPT_R("widgets", 11, 209, 3, 16),
	TRAIN_PROMPT("widgets", 0, 230, 256, 16),

	// ComputerCraft
	COMPUTER("computer", 200, 102);

	;

	public static final int FONT_COLOR = 0x575F7A;

	public final ResourceLocation location;
	private final int width;
	private final int height;
	private final int startX;
	private final int startY;

	AllGuiTextures(String location, int width, int height) {
		this(location, 0, 0, width, height);
	}
	AllGuiTextures(String location, int startX, int startY, int width, int height) {
		this(Create.ID, location, startX, startY, width, height);
	}

	AllGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
		this.location = new ResourceLocation(namespace, "textures/gui/" + location + ".png");
		this.width = width;
		this.height = height;
		this.startX = startX;
		this.startY = startY;
	}

	@Override
	public ResourceLocation getLocation() {
		return location;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void render(PoseStack ms, int x, int y) {
		bind();
		GuiComponent.blit(ms, x, y, 0, startX, startY, width, height, 256, 256);
	}

	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack ms, int x, int y, GuiComponent component) {
		bind();
		component.blit(ms, x, y, startX, startY, width, height);
	}

	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack ms, int x, int y, Color c) {
		bind();
		UIRenderHelper.drawColoredTexture(ms, c, x, y, startX, startY, width, height);
	}

	@Override
	public int getStartX() {
		return startX;
	}

	@Override
	public int getStartY() {
		return startY;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
