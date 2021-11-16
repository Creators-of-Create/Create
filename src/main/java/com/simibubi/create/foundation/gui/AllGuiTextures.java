package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.element.ScreenElement;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public enum AllGuiTextures implements ScreenElement {

	// Inventories
	PLAYER_INVENTORY("player_inventory", 176, 108),
	WAND_OF_SYMMETRY("curiosities", 0, 131, 188, 101),
	BLOCKZAPPER("curiosities", 0, 99, 214, 97),
	TERRAINZAPPER("curiosities", 234, 103),
	TERRAINZAPPER_INACTIVE_PARAM("curiosities", 238, 0, 18, 18),

	LOGO("logo", 256, 256),

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

	STOCKSWITCH("logistics", 182, 93),
	STOCKSWITCH_ARROW_UP("logistics", 191, 0, 7, 24),
	STOCKSWITCH_ARROW_DOWN("logistics", 198, 0, 7, 24),
	STOCKSWITCH_CURSOR("logistics", 206, 0, 7, 16),
	STOCKSWITCH_INTERVAL("logistics", 0, 93, 100, 18),
	STOCKSWITCH_UNPOWERED_LANE("logistics", 36, 18, 102, 18),
	STOCKSWITCH_POWERED_LANE("logistics", 36, 40, 102, 18),

	ADJUSTABLE_CRATE("logistics_2", 124, 127),
	ADJUSTABLE_DOUBLE_CRATE("logistics_2", 0, 127, 196, 127),
	ADJUSTABLE_CRATE_LOCKED_SLOT("logistics_2", 125, 109, 18, 18),

	FILTER("filters", 214, 97),
	ATTRIBUTE_FILTER("filters", 0, 97, 241, 83),

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

	SEQUENCER("sequencer", 173, 159),
	SEQUENCER_INSTRUCTION("sequencer", 0, 14, 162, 22),
	SEQUENCER_DELAY("sequencer", 0, 58, 162, 22),
	SEQUENCER_END("sequencer", 0, 80, 162, 22),
	SEQUENCER_EMPTY("sequencer", 0, 102, 162, 22),
	SEQUENCER_AWAIT("sequencer", 0, 160, 162, 22),

	LINKED_CONTROLLER("curiosities_2", 179, 109),
	BLUEPRINT("curiosities_2", 0, 109, 179, 109),

	PROJECTOR("projector", 235, 185),
	PROJECTOR_FILTER_STRENGTH("projector", 0, 14, 162, 22),
	PROJECTOR_FILTER("projector", 0, 36, 162, 22),
	PROJECTOR_END("projector", 0, 58, 162, 22),
	PROJECTOR_EMPTY("projector", 0, 80, 162, 22),

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

	// PlacementIndicator
	PLACEMENT_INDICATOR_SHEET("placement_indicator", 0, 0, 16, 256);

	;

	public static final int FONT_COLOR = 0x575F7A;

	public final ResourceLocation location;
	public int width, height;
	public int startX, startY;

	private AllGuiTextures(String location, int width, int height) {
		this(location, 0, 0, width, height);
	}

	private AllGuiTextures(int startX, int startY) {
		this("icons", startX * 16, startY * 16, 16, 16);
	}

	private AllGuiTextures(String location, int startX, int startY, int width, int height) {
		this(Create.ID, location, startX, startY, width, height);
	}

	private AllGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
		this.location = new ResourceLocation(namespace, "textures/gui/" + location + ".png");
		this.width = width;
		this.height = height;
		this.startX = startX;
		this.startY = startY;
	}

	@Environment(EnvType.CLIENT)
	public void bind() {
		RenderSystem.setShaderTexture(0, location);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void render(PoseStack ms, int x, int y) {
		bind();
		GuiComponent.blit(ms, x, y, 0, startX, startY, width, height, 256, 256);
	}

	@Environment(EnvType.CLIENT)
	public void render(PoseStack ms, int x, int y, GuiComponent component) {
		bind();
		component.blit(ms, x, y, startX, startY, width, height);
	}

	@Environment(EnvType.CLIENT)
	public void render(PoseStack ms, int x, int y, Color c) {
		bind();
		UIRenderHelper.drawColoredTexture(ms, c, x, y, startX, startY, width, height);
	}

}
