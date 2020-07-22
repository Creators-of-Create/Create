package com.simibubi.create.foundation.gui;

import com.simibubi.create.Create;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum AllGuiTextures {

	// Inventories
	PLAYER_INVENTORY("player_inventory.png", 176, 108),
	WAND_SYMMETRY("wand_symmetry.png", 207, 58),
	BLOCKZAPPER("zapper.png", 217, 70),
	TERRAINZAPPER("zapper.png", 0, 70, 217, 105),
	TERRAINZAPPER_INACTIVE_PARAM("zapper.png", 0, 175, 14, 14),

	SCHEMATIC_TABLE("schematic_table.png", 207, 89),
	SCHEMATIC_TABLE_PROGRESS("schematic_table.png", 209, 0, 24, 17),
	SCHEMATIC("schematic.png", 207, 95),

	SCHEMATICANNON_BG("schematicannon.png", 247, 161),
	SCHEMATICANNON_BG_FUEL("schematicannon.png", 247, 161),
	SCHEMATICANNON_PROGRESS("schematicannon.png", 0, 161, 121, 16),
	SCHEMATICANNON_PROGRESS_2("schematicannon.png", 122, 161, 16, 15),
	SCHEMATICANNON_HIGHLIGHT("schematicannon.png", 0, 182, 28, 28),
	SCHEMATICANNON_FUEL("schematicannon.png", 0, 215, 82, 4),
	SCHEMATICANNON_FUEL_CREATIVE("schematicannon.png", 0, 219, 82, 4),

	FLEXCRATE("flex_crate_and_stockpile_switch.png", 125, 129),
	FLEXCRATE_DOUBLE("double_flexcrate.png", 197, 129),
	FLEXCRATE_LOCKED_SLOT("flex_crate_and_stockpile_switch.png", 138, 0, 18, 18),

	STOCKSWITCH("flex_crate_and_stockpile_switch.png", 0, 129, 205, 93),
	STOCKSWITCH_INTERVAL("flex_crate_and_stockpile_switch.png", 0, 222, 198, 17),
	STOCKSWITCH_INTERVAL_END("flex_crate_and_stockpile_switch.png", 0, 239, 198, 17),
	STOCKSWITCH_CURSOR_ON("flex_crate_and_stockpile_switch.png", 218, 129, 8, 21),
	STOCKSWITCH_CURSOR_OFF("flex_crate_and_stockpile_switch.png", 226, 129, 8, 21),
	STOCKSWITCH_BOUND_LEFT("flex_crate_and_stockpile_switch.png", 234, 129, 7, 21),
	STOCKSWITCH_BOUND_RIGHT("flex_crate_and_stockpile_switch.png", 241, 129, 7, 21),

	FILTER("filter.png", 200, 100),
	ATTRIBUTE_FILTER("filter.png", 0, 100, 200, 86),

	SEQUENCER("sequencer.png", 156, 128),
	SEQUENCER_INSTRUCTION("sequencer.png", 14, 47, 131, 18),
	SEQUENCER_WAIT("sequencer.png", 14, 65, 131, 18),
	SEQUENCER_END("sequencer.png", 14, 83, 131, 18),
	SEQUENCER_EMPTY("sequencer.png", 14, 101, 131, 18),

	// JEI
	JEI_SLOT("jei/widgets.png", 18, 18),
	JEI_CHANCE_SLOT("jei/widgets.png", 20, 156, 18, 18),
	JEI_CATALYST_SLOT("jei/widgets.png", 0, 156, 18, 18),
	JEI_ARROW("jei/widgets.png", 19, 10, 42, 10),
	JEI_LONG_ARROW("jei/widgets.png", 19, 0, 71, 10),
	JEI_DOWN_ARROW("jei/widgets.png", 0, 21, 18, 14),
	JEI_LIGHT("jei/widgets.png", 0, 42, 52, 11),
	JEI_QUESTION_MARK("jei/widgets.png", 0, 178, 12, 16),
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

	;

	public static final int FONT_COLOR = 0x575F7A;

	public final ResourceLocation location;
	public int width, height;
	public int startX, startY;

	private AllGuiTextures(String location, int width, int height) {
		this(location, 0, 0, width, height);
	}

	private AllGuiTextures(int startX, int startY) {
		this("icons.png", startX * 16, startY * 16, 16, 16);
	}

	private AllGuiTextures(String location, int startX, int startY, int width, int height) {
		this.location = new ResourceLocation(Create.ID, "textures/gui/" + location);
		this.width = width;
		this.height = height;
		this.startX = startX;
		this.startY = startY;
	}

	@OnlyIn(Dist.CLIENT)
	public void bind() {
		Minecraft.getInstance()
			.getTextureManager()
			.bindTexture(location);
	}

	@OnlyIn(Dist.CLIENT)
	public void draw(AbstractGui screen, int x, int y) {
		bind();
		screen.drawTexture(x, y, startX, startY, width, height);
	}

	@OnlyIn(Dist.CLIENT)
	public void draw(int x, int y) {
		draw(new Screen(null) {
		}, x, y);
	}

}
