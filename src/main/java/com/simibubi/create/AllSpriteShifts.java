package com.simibubi.create;

import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.getCT;
import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType.HORIZONTAL;
import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType.OMNIDIRECTIONAL;
import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType.VERTICAL;

import java.util.IdentityHashMap;
import java.util.Map;

import com.simibubi.create.content.palettes.PaletteBlockPatterns;
import com.simibubi.create.content.palettes.PaletteBlockPatterns.CTs;
import com.simibubi.create.content.palettes.PaletteStoneVariants;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.WoodType;
import net.minecraft.item.DyeColor;

public class AllSpriteShifts {

	static final Map<WoodType, CTSpriteShiftEntry> WOODEN_WINDOWS = new IdentityHashMap<>();
	static final Map<PaletteStoneVariants, Map<PaletteBlockPatterns.CTs, CTSpriteShiftEntry>> PALETTE_VARIANT_PATTERNS =
		new IdentityHashMap<>();

	public static final Map<DyeColor, SpriteShiftEntry> DYED_BELTS = new IdentityHashMap<>(),
		DYED_OFFSET_BELTS = new IdentityHashMap<>(), DYED_DIAGONAL_BELTS = new IdentityHashMap<>();

	public static final CTSpriteShiftEntry FRAMED_GLASS =
		getCT(OMNIDIRECTIONAL, "palettes/framed_glass", "palettes/framed_glass"),
		HORIZONTAL_FRAMED_GLASS = getCT(HORIZONTAL, "palettes/framed_glass", "palettes/horizontal_framed_glass"),
		VERTICAL_FRAMED_GLASS = getCT(VERTICAL, "palettes/framed_glass", "palettes/vertical_framed_glass"),
		ORNATE_IRON_WINDOW = vertical("palettes/ornate_iron_window");

	public static final CTSpriteShiftEntry CRAFTER_FRONT = getCT(CTType.OMNIDIRECTIONAL, "crafter_top", "brass_casing"),
		CRAFTER_SIDE = getCT(CTType.VERTICAL, "crafter_side"),
		CRAFTER_OTHERSIDE = getCT(CTType.HORIZONTAL, "crafter_side");

	public static final CTSpriteShiftEntry ANDESITE_CASING = omni("andesite_casing"),
		BRASS_CASING = omni("brass_casing"), COPPER_CASING = omni("copper_casing"),
		SHADOW_STEEL_CASING = omni("shadow_steel_casing"), REFINED_RADIANCE_CASING = omni("refined_radiance_casing"),
		CREATIVE_CASING = getCT(CTType.CROSS, "creative_casing");

	public static final CTSpriteShiftEntry CHASSIS = getCT(CTType.OMNIDIRECTIONAL, "linear_chassis_end"),
		CHASSIS_STICKY = getCT(CTType.OMNIDIRECTIONAL, "linear_chassis_end_sticky");

	public static final CTSpriteShiftEntry BRASS_TUNNEL_TOP = vertical("brass_tunnel_top"),
		FLUID_TANK = getCT(CTType.CROSS, "fluid_tank"),
		CREATIVE_FLUID_TANK = getCT(CTType.CROSS, "creative_fluid_tank");

	public static final SpriteShiftEntry BELT = SpriteShifter.get("block/belt", "block/belt_scroll"),
		BELT_OFFSET = SpriteShifter.get("block/belt_offset", "block/belt_offset_scroll"),
		BELT_DIAGONAL = SpriteShifter.get("block/belt_diagonal", "block/belt_diagonal_scroll"),
		ANDESIDE_BELT_CASING = SpriteShifter.get("block/brass_casing_belt", "block/andesite_casing_belt"),
		CRAFTER_THINGIES = SpriteShifter.get("block/crafter_thingies", "block/crafter_thingies");

	static {
		populateMaps();
	}

	//

	public static CTSpriteShiftEntry getWoodenWindow(WoodType woodType) {
		return WOODEN_WINDOWS.get(woodType);
	}

	public static CTSpriteShiftEntry getVariantPattern(PaletteStoneVariants variant, PaletteBlockPatterns.CTs texture) {
		return PALETTE_VARIANT_PATTERNS.get(variant)
			.get(texture);
	}

	//

	private static void populateMaps() {
		WoodType.stream()
			.forEach(woodType -> WOODEN_WINDOWS.put(woodType, vertical("palettes/" + woodType.getName() + "_window")));

		for (PaletteStoneVariants paletteStoneVariants : PaletteStoneVariants.values()) {
			String variantName = Lang.asId(paletteStoneVariants.name());
			IdentityHashMap<CTs, CTSpriteShiftEntry> map = new IdentityHashMap<>();
			PALETTE_VARIANT_PATTERNS.put(paletteStoneVariants, map);

			for (PaletteBlockPatterns.CTs texture : PaletteBlockPatterns.CTs.values()) {
				String textureName = Lang.asId(texture.name());
				String target = "palettes/" + variantName + "/" + textureName;
				map.put(texture, getCT(texture.type, target));
			}
		}

		for (DyeColor color : DyeColor.values()) {
			String id = color.getName();
			DYED_BELTS.put(color, SpriteShifter.get("block/belt", "block/belt/" + id));
			DYED_OFFSET_BELTS.put(color, SpriteShifter.get("block/belt_offset", "block/belt/" + id));
			DYED_DIAGONAL_BELTS.put(color, SpriteShifter.get("block/belt_diagonal", "block/belt/" + id + "_diagonal"));
		}
	}

	//

	static CTSpriteShiftEntry omni(String name) {
		return getCT(OMNIDIRECTIONAL, name);
	}

	static CTSpriteShiftEntry vertical(String name) {
		return getCT(VERTICAL, name);
	}

	static CTSpriteShiftEntry horizontal(String name) {
		return getCT(HORIZONTAL, name);
	}

}
