package com.simibubi.create;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;

public enum ScreenResources {

	// Inventories
	PLAYER_INVENTORY("player_inventory.png", 176, 108),
	WAND_SYMMETRY("wand_symmetry.png", 207, 58),
	PLACEMENT_GUN("placement_handgun.png", 217, 70),

	SCHEMATIC_TABLE("schematic_table.png", 207, 89),
	SCHEMATIC_TABLE_PROGRESS("schematic_table.png", 209, 0, 24, 17),
	SCHEMATIC("schematic.png", 207, 95),

	SCHEMATICANNON("schematicannon.png", 247, 161),
	SCHEMATICANNON_PROGRESS("schematicannon.png", 0, 161, 121, 16),
	SCHEMATICANNON_PROGRESS_2("schematicannon.png", 122, 161, 16, 15),
	SCHEMATICANNON_HIGHLIGHT("schematicannon.png", 0, 182, 28, 28),
	SCHEMATICANNON_FUEL("schematicannon.png", 0, 215, 82, 4),

	FLEXCRATE("flex_crate_and_stockpile_switch.png", 125, 129),
	FLEXCRATE_LOCKED_SLOT("flex_crate_and_stockpile_switch.png", 138, 0, 18, 18),

	STOCKSWITCH("flex_crate_and_stockpile_switch.png", 0, 129, 205, 93),
	STOCKSWITCH_INTERVAL("flex_crate_and_stockpile_switch.png", 0, 222, 198, 17),
	STOCKSWITCH_INTERVAL_END("flex_crate_and_stockpile_switch.png", 0, 239, 198, 17),
	STOCKSWITCH_CURSOR_ON("flex_crate_and_stockpile_switch.png", 218, 129, 8, 21),
	STOCKSWITCH_CURSOR_OFF("flex_crate_and_stockpile_switch.png", 226, 129, 8, 21),
	STOCKSWITCH_BOUND_LEFT("flex_crate_and_stockpile_switch.png", 234, 129, 7, 21),
	STOCKSWITCH_BOUND_RIGHT("flex_crate_and_stockpile_switch.png", 241, 129, 7, 21),

	// Logistical Index
	INDEX_TOP("index.png", 41, 0, 174, 22),
	INDEX_TOP_TRIM("index.png", 41, 22, 174, 6),
	INDEX_MIDDLE("index.png", 41, 28, 183, 178),
	INDEX_BOTTOM_TRIM("index.png", 41, 206, 174, 6),
	INDEX_BOTTOM("index.png", 41, 212, 181, 44),
	INDEX_SCROLLER_TOP("index.png", 224, 31, 10, 6),
	INDEX_SCROLLER_MIDDLE("index.png", 224, 37, 10, 6),
	INDEX_SCROLLER_BOTTOM("index.png", 224, 43, 10, 6),
	INDEX_TAB("index.png", 0, 55, 22, 22),
	INDEX_TAB_ACTIVE("index.png", 0, 77, 22, 22),
	INDEX_SEARCH("index.png", 0, 99, 28, 19),
	INDEX_SEARCH_OVERLAY("widgets.png", 0, 81, 176, 20),

	LOGISTICAL_CONTROLLER_TRIM("controller.png", 178, 6),
	LOGISTICAL_CONTROLLER("controller.png", 0, 6, 185, 71),

	ITEM_COUNT_SCROLLAREA("controller.png", 62, 83, 22, 10),
	BIG_SLOT("controller.png", 0, 83, 26, 26),
	SHIPPING_SLOT("controller.png", 26, 83, 18, 21),
	RECEIVING_SLOT("controller.png", 44, 83, 18, 21),
	SLOT_FRAME("index.png", 0, 118, 18, 18),
	SLOT_INNER("index.png", 18, 118, 18, 18),
	DISABLED_SLOT_FRAME("index.png", 0, 136, 18, 18),
	DISABLED_SLOT_INNER("index.png", 18, 136, 18, 18),
	CRAFTY_SLOT_FRAME("index.png", 0, 154, 18, 18),
	CRAFTY_SLOT_INNER("index.png", 18, 154, 18, 18),
	SELECTED_SLOT_INNER("index.png", 18, 172, 18, 18),

	// JEI
	JEI_SLOT("jei/widgets.png", 18, 18),
	JEI_ARROW("jei/widgets.png", 19, 10, 42, 10),
	JEI_LONG_ARROW("jei/widgets.png", 19, 0, 71, 10),
	JEI_DOWN_ARROW("jei/widgets.png", 0, 21, 18, 14),
	JEI_LIGHT("jei/widgets.png", 0, 42, 52, 11),
	JEI_SHADOW("jei/widgets.png", 0, 56, 52, 11),
	BLOCKZAPPER_UPGRADE_RECIPE("jei/widgets.png", 0, 75, 144, 66),

	// Widgets
	PALETTE_BUTTON("palette_picker.png", 0, 236, 20, 20),
	TEXT_INPUT("widgets.png", 0, 28, 194, 47),
	BUTTON("widgets.png", 18, 18),
	BUTTON_HOVER("widgets.png", 18, 0, 18, 18),
	BUTTON_DOWN("widgets.png", 36, 0, 18, 18),
	INDICATOR("widgets.png", 0, 18, 18, 5),
	INDICATOR_WHITE("widgets.png", 18, 18, 18, 5),
	INDICATOR_GREEN("widgets.png", 0, 23, 18, 5),
	INDICATOR_YELLOW("widgets.png", 18, 23, 18, 5),
	INDICATOR_RED("widgets.png", 36, 23, 18, 5),
	GRAY("background.png", 0, 0, 16, 16),

	BLUEPRINT_SLOT("widgets.png", 90, 0, 24, 24),

	// Icons
	I_NONE(16, 16),
	I_ADD(0, 0),
	I_TRASH(16, 0),
	I_3x3(32, 0),
	I_TARGET(48, 0),
	I_CONFIRM(0, 16),

	I_OPEN_FOLDER(32, 16),
	I_REFRESH(48, 16),

	I_DONT_REPLACE(0, 32),
	I_REPLACE_SOLID(16, 32),
	I_REPLACE_ANY(32, 32),
	I_REPLACE_EMPTY(48, 32),

	I_TOOL_DEPLOY(0, 48),
	I_SKIP_MISSING(16, 48),
	I_SKIP_TILES(32, 48),

	I_TOOL_MOVE_XZ(0, 64),
	I_TOOL_MOVE_Y(16, 64),
	I_TOOL_ROTATE(32, 64),
	I_TOOL_MIRROR(48, 64),

	I_PLAY(0, 80),
	I_PAUSE(16, 80),
	I_STOP(32, 80),

	I_PATTERN_SOLID(0, 96),
	I_PATTERN_CHECKERED(16, 96),
	I_PATTERN_CHECKERED_INVERSED(32, 96),
	I_PATTERN_CHANCE_25(48, 96),
	I_PATTERN_CHANCE_50(0, 112),
	I_PATTERN_CHANCE_75(16, 112),
	I_FOLLOW_DIAGONAL(32, 112),
	I_FOLLOW_MATERIAL(48, 112),

	I_PRIORITY_VERY_LOW(64, 0),
	I_PRIORITY_LOW(80, 0),
	I_PRIORITY_HIGH(96, 0),
	I_PRIORITY_VERY_HIGH(112, 0),
	I_ACTIVE(64, 16),
	I_PASSIVE(80, 16),

	;

	public static final int FONT_COLOR = 0x575F7A;

	public final ResourceLocation location;
	public int width, height;
	public int startX, startY;
	static Screen renderer = new Screen(null) {
	};

	private ScreenResources(String location, int width, int height) {
		this(location, 0, 0, width, height);
	}

	private ScreenResources(int startX, int startY) {
		this("icons.png", startX, startY, 16, 16);
	}

	private ScreenResources(String location, int startX, int startY, int width, int height) {
		this.location = new ResourceLocation(Create.ID, "textures/gui/" + location);
		this.width = width;
		this.height = height;
		this.startX = startX;
		this.startY = startY;
	}

	public void bind() {
		Minecraft.getInstance().getTextureManager().bindTexture(location);
	}

	public void draw(AbstractGui screen, int x, int y) {
		bind();
		screen.blit(x, y, startX, startY, width, height);
	}

	public void draw(int x, int y) {
		draw(renderer, x, y);
	}

}
