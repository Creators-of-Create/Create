package com.simibubi.create;

import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType.HORIZONTAL;
import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType.OMNIDIRECTIONAL;
import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType.VERTICAL;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;

import net.minecraft.util.ResourceLocation;

public enum AllCTs {

	FRAMED_GLASS(custom(OMNIDIRECTIONAL, "palettes/framed_glass", "framed_glass")),
	HORIZONTAL_FRAMED_GLASS(custom(HORIZONTAL, "palettes/framed_glass", "horizontal_framed_glass")),
	VERTICAL_FRAMED_GLASS(custom(VERTICAL, "palettes/framed_glass", "vertical_framed_glass")),

	OAK_GLASS(vertical("oak_window")),
	SPRUCE_GLASS(vertical("spruce_window")),
	BIRCH_GLASS(vertical("birch_window")),
	JUNGLE_GLASS(vertical("jungle_window")),
	DARK_OAK_GLASS(vertical("dark_oak_window")),
	ACACIA_GLASS(vertical("acacia_window")),
	ACACIA_GLASS_DENSE(vertical("acacia_window_dense")),
	IRON_GLASS(vertical("iron_window")),

	GRANITE_LAYERS(layers("granite")),
	DIORITE_LAYERS(layers("diorite")),
	ANDESITE_LAYERS(layers("andesite")),
	GABBRO_LAYERS(layers("gabbro")),
	DOLOMITE_LAYERS(layers("dolomite")),
	LIMESTONE_LAYERS(layers("limestone")),
	WEATHERED_LIMESTONE_LAYERS(layers("weathered_limestone")),
	SCORIA_LAYERS(layers("scoria")),

	POLISHED_GRANITE(polishedVanilla("granite")),
	POLISHED_DIORITE(polishedVanilla("diorite")),
	POLISHED_ANDESITE(polishedVanilla("andesite")),
	POLISHED_GABBRO(polished("gabbro")),
	POLISHED_DOLOMITE(polished("dolomite")),
	POLISHED_LIMESTONE(polished("limestone")),
	POLISHED_WEATHERED_LIMESTONE(polished("weathered_limestone")),
	POLISHED_SCORIA(polished("scoria")),

	;

	private CTSpriteShiftEntry entry;

	private AllCTs(CTSpriteShiftEntry entry) {
		this.entry = entry;
	}

	public CTSpriteShiftEntry get() {
		return entry;
	}

	static CTSpriteShiftEntry omni(String name) {
		return CTSpriteShifter.get(OMNIDIRECTIONAL, name);
	}

	static CTSpriteShiftEntry custom(CTType type, String from, String to) {
		return CTSpriteShifter.get(type, from, to);
	}

	static CTSpriteShiftEntry vertical(String blockname) {
		return CTSpriteShifter.get(VERTICAL, blockname);
	}

	static CTSpriteShiftEntry layers(String prefix) {
		return CTSpriteShifter.get(HORIZONTAL, prefix + "_layers");
	}

	static CTSpriteShiftEntry polished(String blockname) {
		return omni("polished_" + blockname);
	}

	static CTSpriteShiftEntry polishedVanilla(String blockname) {
		return CTSpriteShifter.get(OMNIDIRECTIONAL, new ResourceLocation("block/polished_" + blockname),
			"polished_" + blockname);
	}

}
