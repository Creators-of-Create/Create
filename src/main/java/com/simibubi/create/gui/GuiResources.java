package com.simibubi.create.gui;

import com.simibubi.create.Create;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;

public enum GuiResources {
	
	// Inventories
	PLAYER_INVENTORY("player_inventory.png", 176, 108),
	WAND_SYMMETRY("wand_symmetry.png", 207, 58),
	SCHEMATIC_TABLE("schematic_table.png", 207, 89),
	SCHEMATIC_TABLE_PROGRESS("schematic_table.png", 209, 0, 24, 17),

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
	
	ICON_NORMAL_ROOF("icons.png", 32, 16, 16, 16),
	ICON_FLAT_ROOF("icons.png", 48, 16, 16, 16),
	ICON_NO_ROOF("icons.png", 0, 32, 16, 16),
	
	ICON_TOWER_NO_ROOF("icons.png", 16, 32, 16, 16),
	ICON_TOWER_ROOF("icons.png", 32, 32, 16, 16),
	ICON_TOWER_FLAT_ROOF("icons.png", 48, 32, 16, 16),
	
	ICON_LAYER_REGULAR("icons.png", 0, 48, 16, 16),
	ICON_LAYER_OPEN("icons.png", 16, 48, 16, 16),
	ICON_LAYER_FOUNDATION("icons.png", 32, 48, 16, 16),
	ICON_LAYER_SPECIAL("icons.png", 48, 48, 16, 16),
	
	ICON_TOOL_RESHAPE("icons.png", 0, 64, 16, 16),
	ICON_TOOL_ROOM("icons.png", 16, 64, 16, 16),
	ICON_TOOL_TOWER("icons.png", 32, 64, 16, 16),
	ICON_TOOL_STACK("icons.png", 48, 64, 16, 16),
	
	ICON_TOOL_HEIGHT("icons.png", 0, 80, 16, 16),
	ICON_TOOL_REROLL("icons.png", 16, 80, 16, 16),
	ICON_TOOL_REROLL_TARGET("icons.png", 32, 80, 16, 16),
	ICON_TOOL_PALETTE("icons.png", 48, 80, 16, 16);
	
	public static final int FONT_COLOR = 0x575F7A;
	
	public final ResourceLocation location;
	public int width, height;
	public int startX, startY;
	
	private GuiResources(String location, int width, int height) {
		this(location, 0, 0, width, height);
	}
	
	private GuiResources(String location, int startX, int startY, int width, int height) {
		this.location = new ResourceLocation(Create.ID, "textures/gui/" + location);
		this.width = width; this.height = height;
		this.startX = startX; this.startY = startY;
	}
	
	public void draw(AbstractGui screen, int i, int j) {
		Minecraft.getInstance().getTextureManager().bindTexture(location);
		screen.blit(i, j, startX, startY, width, height);
	}

}
