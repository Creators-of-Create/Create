package com.simibubi.create.foundation.gui;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.Create;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum AllGuiTextures implements IScreenRenderable {

	// Inventories
	PLAYER_INVENTORY("player_inventory.png", 176, 108),
	WAND_OF_SYMMETRY("curiosities.png", 0, 131, 188, 101),
	BLOCKZAPPER("curiosities.png", 0, 99, 214, 97),
	TERRAINZAPPER("curiosities.png", 234, 103),
	TERRAINZAPPER_INACTIVE_PARAM("curiosities.png", 238, 0, 18, 18),

	LOGO("logo.png", 256, 256),
	
	SCHEMATIC("schematics.png", 192, 121),
	SCHEMATIC_SLOT("widgets.png", 54, 0, 16, 16),
	SCHEMATIC_PROMPT("schematics_2.png", 213, 77),
	HUD_BACKGROUND("overlay.png", 0, 0, 16, 16),

	SCHEMATIC_TABLE("schematics.png", 0, 121, 214, 83),
	SCHEMATIC_TABLE_PROGRESS("schematics.png", 0, 204, 84, 16),

	SCHEMATICANNON_TOP("schematics_2.png", 0, 77, 213, 42),
	SCHEMATICANNON_BOTTOM("schematics_2.png", 0, 119, 213, 99),
	SCHEMATICANNON_PROGRESS("schematics_2.png", 76, 239, 114, 16),
	SCHEMATICANNON_CHECKLIST_PROGRESS("schematics_2.png", 191, 240, 16, 14),
	SCHEMATICANNON_HIGHLIGHT("schematics_2.png", 1, 229, 26, 26),
	SCHEMATICANNON_FUEL("schematics_2.png", 28, 222, 47, 16),
	SCHEMATICANNON_FUEL_CREATIVE("schematics_2.png", 28, 239, 47, 16),

	STOCKSWITCH("logistics.png", 182, 93),
	STOCKSWITCH_ARROW_UP("logistics.png", 191, 0, 7, 24),
	STOCKSWITCH_ARROW_DOWN("logistics.png", 198, 0, 7, 24),
	STOCKSWITCH_CURSOR("logistics.png", 206, 0, 7, 16),
	STOCKSWITCH_INTERVAL("logistics.png", 0, 93, 100, 18),
	STOCKSWITCH_UNPOWERED_LANE("logistics.png", 36, 18, 102, 18),
	STOCKSWITCH_POWERED_LANE("logistics.png", 36, 40, 102, 18),

	ADJUSTABLE_CRATE("logistics_2.png", 124, 127),
	ADJUSTABLE_DOUBLE_CRATE("logistics_2.png", 0, 127, 196, 127),
	ADJUSTABLE_CRATE_LOCKED_SLOT("logistics_2.png", 125, 109, 18, 18),

	FILTER("filters.png", 214, 97),
	ATTRIBUTE_FILTER("filters.png", 0, 97, 241, 83),

	SEQUENCER("sequencer.png", 173, 159),
	SEQUENCER_INSTRUCTION("sequencer.png", 0, 14, 162, 22),
	SEQUENCER_DELAY("sequencer.png", 0, 58, 162, 22),
	SEQUENCER_END("sequencer.png", 0, 80, 162, 22),
	SEQUENCER_EMPTY("sequencer.png", 0, 102, 162, 22),
	SEQUENCER_AWAIT("sequencer.png", 0, 160, 162, 22),
	
	LINKED_CONTROLLER("curiosities2.png", 179, 109),
	BLUEPRINT("curiosities2.png", 0, 109, 179, 109),

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
	JEI_HEAT_BAR("jei/widgets.png", 0, 201, 169, 19),
	JEI_NO_HEAT_BAR("jei/widgets.png", 0, 221, 169, 19),

	// Widgets
	BUTTON("widgets.png", 18, 18),
	BUTTON_HOVER("widgets.png", 18, 0, 18, 18),
	BUTTON_DOWN("widgets.png", 36, 0, 18, 18),
	INDICATOR("widgets.png", 0, 18, 18, 6),
	INDICATOR_WHITE("widgets.png", 18, 18, 18, 6),
	INDICATOR_GREEN("widgets.png", 36, 18, 18, 6),
	INDICATOR_YELLOW("widgets.png", 54, 18, 18, 6),
	INDICATOR_RED("widgets.png", 72, 18, 18, 6),
	
	HOTSLOT_ARROW("widgets.png", 24, 51, 20, 12),
	HOTSLOT("widgets.png", 0, 68, 22, 22),
	HOTSLOT_ACTIVE("widgets.png", 0, 46, 22, 22),
	HOTSLOT_SUPER_ACTIVE("widgets.png", 27, 67, 24, 24),

	SPEECH_TOOLTIP_BACKGROUND("widgets.png", 0, 24, 8, 8),
	SPEECH_TOOLTIP_COLOR("widgets.png", 8, 24, 8, 8),

	// PlacementIndicator
	PLACEMENT_INDICATOR_SHEET("placement_indicator.png", 0, 0, 16, 256);

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

	@Override
	@OnlyIn(Dist.CLIENT)
	public void draw(MatrixStack ms, AbstractGui screen, int x, int y) {
		bind();
		screen.drawTexture(ms, x, y, startX, startY, width, height);
	}

	public void draw(MatrixStack ms, int x, int y, Color c) {
		bind();
		UIRenderHelper.drawColoredTexture(ms, c, x, y, startX, startY, width, height);
	}
}
