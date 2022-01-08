package com.simibubi.create;

import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.getCT;
import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType.HORIZONTAL;
import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType.OMNIDIRECTIONAL;
import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType.VERTICAL;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.WoodType;

public class AllSpriteShifts {

	private static final Map<WoodType, CTSpriteShiftEntry> WOODEN_WINDOWS = new IdentityHashMap<>();

	public static final Map<DyeColor, SpriteShiftEntry> DYED_BELTS = new IdentityHashMap<>(),
		DYED_OFFSET_BELTS = new IdentityHashMap<>(), DYED_DIAGONAL_BELTS = new IdentityHashMap<>();

	public static final CTSpriteShiftEntry FRAMED_GLASS =
		getCT(OMNIDIRECTIONAL, "palettes/framed_glass", "palettes/framed_glass"),
		HORIZONTAL_FRAMED_GLASS = getCT(CTType.HORIZONTAL_KRYPPERS, "palettes/framed_glass", "palettes/horizontal_framed_glass"),
		VERTICAL_FRAMED_GLASS = getCT(VERTICAL, "palettes/framed_glass", "palettes/vertical_framed_glass"),
		ORNATE_IRON_WINDOW = vertical("palettes/ornate_iron_window");

	public static final CTSpriteShiftEntry CRAFTER_FRONT = getCT(CTType.OMNIDIRECTIONAL, "crafter_top", "brass_casing"),
		CRAFTER_SIDE = vertical("crafter_side"),
		CRAFTER_OTHERSIDE = horizontal("crafter_side"),
		ANDESITE_ENCASED_COGWHEEL_SIDE = vertical("andesite_encased_cogwheel_side"),
		ANDESITE_ENCASED_COGWHEEL_OTHERSIDE = horizontal("andesite_encased_cogwheel_side"),
		BRASS_ENCASED_COGWHEEL_SIDE = vertical("brass_encased_cogwheel_side"),
		BRASS_ENCASED_COGWHEEL_OTHERSIDE = horizontal("brass_encased_cogwheel_side");

	public static final CTSpriteShiftEntry ANDESITE_CASING = omni("andesite_casing"),
		BRASS_CASING = omni("brass_casing"), COPPER_CASING = omni("copper_casing"),
		SHADOW_STEEL_CASING = omni("shadow_steel_casing"), REFINED_RADIANCE_CASING = omni("refined_radiance_casing"),
		CREATIVE_CASING = getCT(CTType.CROSS, "creative_casing");

	public static final CTSpriteShiftEntry CHASSIS_SIDE = omni("linear_chassis_side"),
		SECONDARY_CHASSIS_SIDE = omni("secondary_linear_chassis_side"),
		CHASSIS = omni("linear_chassis_end"),
		CHASSIS_STICKY = omni("linear_chassis_end_sticky");

	public static final CTSpriteShiftEntry BRASS_TUNNEL_TOP = vertical("brass_tunnel_top"),
		FLUID_TANK = getCT(CTType.CROSS, "fluid_tank"),
		CREATIVE_FLUID_TANK = getCT(CTType.CROSS, "creative_fluid_tank");

	public static final Couple<CTSpriteShiftEntry> VAULT_TOP = vault("top"), VAULT_FRONT = vault("front"),
		VAULT_SIDE = vault("side"), VAULT_BOTTOM = vault("bottom");

	public static final SpriteShiftEntry BELT = SpriteShifter.get("block/belt", "block/belt_scroll"),
		BELT_OFFSET = SpriteShifter.get("block/belt_offset", "block/belt_scroll"),
		BELT_DIAGONAL = SpriteShifter.get("block/belt_diagonal", "block/belt_diagonal_scroll"),
		ANDESIDE_BELT_CASING = SpriteShifter.get("block/brass_casing_belt", "block/andesite_casing_belt"),
		CRAFTER_THINGIES = SpriteShifter.get("block/crafter_thingies", "block/crafter_thingies");

	static {
		populateMaps();
	}

	private static void populateMaps() {
		WoodType[] supportedWoodTypes = new WoodType[] { WoodType.OAK, WoodType.SPRUCE, WoodType.BIRCH, WoodType.ACACIA,
			WoodType.JUNGLE, WoodType.DARK_OAK, WoodType.CRIMSON, WoodType.WARPED };
		Arrays.stream(supportedWoodTypes)
			.forEach(woodType -> WOODEN_WINDOWS.put(woodType, vertical("palettes/" + woodType.name() + "_window")));

		for (DyeColor color : DyeColor.values()) {
			String id = color.getSerializedName();
			DYED_BELTS.put(color, SpriteShifter.get("block/belt", "block/belt/" + id + "_scroll"));
			DYED_OFFSET_BELTS.put(color, SpriteShifter.get("block/belt_offset", "block/belt/" + id + "_scroll"));
			DYED_DIAGONAL_BELTS.put(color,
				SpriteShifter.get("block/belt_diagonal", "block/belt/" + id + "_diagonal_scroll"));
		}
	}

	private static Couple<CTSpriteShiftEntry> vault(String name) {
		final String prefixed = "vault_" + name;
		return Couple
			.createWithContext(b -> getCT(CTType.CROSS, prefixed, b ? prefixed : prefixed + "_large"));
	}

	//

	private static CTSpriteShiftEntry omni(String name) {
		return getCT(OMNIDIRECTIONAL, name);
	}

	private static CTSpriteShiftEntry horizontal(String name) {
		return getCT(HORIZONTAL, name);
	}

	private static CTSpriteShiftEntry vertical(String name) {
		return getCT(VERTICAL, name);
	}

	//

	public static CTSpriteShiftEntry getWoodenWindow(WoodType woodType) {
		return WOODEN_WINDOWS.get(woodType);
	}

}
