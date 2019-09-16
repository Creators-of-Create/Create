package com.simibubi.create;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
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
	
	// JEI
	CRUSHING_RECIPE("recipes1.png", 177, 109),
	FAN_RECIPE("recipes1.png", 0, 128, 177, 109),
	BLOCKZAPPER_UPGRADE_RECIPE("recipes2.png", 144, 66),
	PRESSER_RECIPE("recipes2.png", 0, 108, 177, 109),
	
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
	ICON_NONE("icons.png", 16, 16, 16, 16),
	ICON_ADD("icons.png", 16, 16),
	ICON_TRASH("icons.png", 16, 0, 16, 16),
	ICON_3x3("icons.png", 32, 0, 16, 16),
	ICON_TARGET("icons.png", 48, 0, 16, 16),
	ICON_CONFIRM("icons.png", 0, 16, 16, 16),
	
	ICON_OPEN_FOLDER("icons.png", 32, 16, 16, 16),
	ICON_REFRESH("icons.png", 48, 16, 16, 16),
	
	ICON_DONT_REPLACE("icons.png", 0, 32, 16, 16),
	ICON_REPLACE_SOLID("icons.png", 16, 32, 16, 16),
	ICON_REPLACE_ANY("icons.png", 32, 32, 16, 16),
	ICON_REPLACE_EMPTY("icons.png", 48, 32, 16, 16),
	
	ICON_TOOL_DEPLOY("icons.png", 0, 48, 16, 16),
	ICON_SKIP_MISSING("icons.png", 16, 48, 16, 16),
	ICON_SKIP_TILES("icons.png", 32, 48, 16, 16),
	
	ICON_TOOL_MOVE_XZ("icons.png", 0, 64, 16, 16),
	ICON_TOOL_MOVE_Y("icons.png", 16, 64, 16, 16),
	ICON_TOOL_ROTATE("icons.png", 32, 64, 16, 16),
	ICON_TOOL_MIRROR("icons.png", 48, 64, 16, 16),
	
	ICON_PLAY("icons.png", 0, 80, 16, 16),
	ICON_PAUSE("icons.png", 16, 80, 16, 16),
	ICON_STOP("icons.png", 32, 80, 16, 16),
	
	ICON_PATTERN_SOLID("icons.png", 0, 96, 16, 16),
	ICON_PATTERN_CHECKERED("icons.png", 16, 96, 16, 16),
	ICON_PATTERN_CHECKERED_INVERSED("icons.png", 32, 96, 16, 16),
	ICON_PATTERN_CHANCE_25("icons.png", 48, 96, 16, 16),
	ICON_PATTERN_CHANCE_50("icons.png", 0, 112, 16, 16),
	ICON_PATTERN_CHANCE_75("icons.png", 16, 112, 16, 16),
	ICON_FOLLOW_DIAGONAL("icons.png", 32, 112, 16, 16),
	ICON_FOLLOW_MATERIAL("icons.png", 48, 112, 16, 16),
	
	;
	
	public static final int FONT_COLOR = 0x575F7A;
	
	public final ResourceLocation location;
	public int width, height;
	public int startX, startY;
	
	private ScreenResources(String location, int width, int height) {
		this(location, 0, 0, width, height);
	}
	
	private ScreenResources(String location, int startX, int startY, int width, int height) {
		this.location = new ResourceLocation(Create.ID, "textures/gui/" + location);
		this.width = width; this.height = height;
		this.startX = startX; this.startY = startY;
	}
	
	public void bind() {
		Minecraft.getInstance().getTextureManager().bindTexture(location);
	}
	
	public void draw(AbstractGui screen, int i, int j) {
		bind();
		screen.blit(i, j, startX, startY, width, height);
	}

}
