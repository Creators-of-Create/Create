package com.simibubi.create;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.WoodType;

public class AllSpriteShifts {

	private static final Map<WoodType, CTSpriteShiftEntry> WOODEN_WINDOWS = new IdentityHashMap<>();

	public static final Map<DyeColor, SpriteShiftEntry> DYED_BELTS = new EnumMap<>(DyeColor.class),
		DYED_OFFSET_BELTS = new EnumMap<>(DyeColor.class), DYED_DIAGONAL_BELTS = new EnumMap<>(DyeColor.class);

	public static final SpriteShiftEntry BURNER_FLAME =
		get("block/blaze_burner_flame", "block/blaze_burner_flame_scroll"),
		SUPER_BURNER_FLAME = get("block/blaze_burner_flame", "block/blaze_burner_flame_superheated_scroll");

	public static final CTSpriteShiftEntry ANDESITE_SCAFFOLD = horizontal("scaffold/andesite_scaffold"),
		BRASS_SCAFFOLD = horizontal("scaffold/brass_scaffold"),
		COPPER_SCAFFOLD = horizontal("scaffold/copper_scaffold");

	public static final CTSpriteShiftEntry ANDESITE_SCAFFOLD_INSIDE = horizontal("scaffold/andesite_scaffold_inside"),
		BRASS_SCAFFOLD_INSIDE = horizontal("scaffold/brass_scaffold_inside"),
		COPPER_SCAFFOLD_INSIDE = horizontal("scaffold/copper_scaffold_inside");

	public static final CTSpriteShiftEntry FRAMED_GLASS =
		getCT(AllCTTypes.OMNIDIRECTIONAL, "palettes/framed_glass", "palettes/framed_glass"),
		HORIZONTAL_FRAMED_GLASS =
			getCT(AllCTTypes.HORIZONTAL_KRYPPERS, "palettes/framed_glass", "palettes/horizontal_framed_glass"),
		VERTICAL_FRAMED_GLASS = getCT(AllCTTypes.VERTICAL, "palettes/framed_glass", "palettes/vertical_framed_glass"),
		ORNATE_IRON_WINDOW = vertical("palettes/ornate_iron_window");

	public static final CTSpriteShiftEntry CRAFTER_SIDE = vertical("crafter_side"),
		CRAFTER_OTHERSIDE = horizontal("crafter_side"),
		ANDESITE_ENCASED_COGWHEEL_SIDE = vertical("andesite_encased_cogwheel_side"),
		ANDESITE_ENCASED_COGWHEEL_OTHERSIDE = horizontal("andesite_encased_cogwheel_side"),
		BRASS_ENCASED_COGWHEEL_SIDE = vertical("brass_encased_cogwheel_side"),
		BRASS_ENCASED_COGWHEEL_OTHERSIDE = horizontal("brass_encased_cogwheel_side"),
		GIRDER_POLE = vertical("girder_pole_side");

	public static final CTSpriteShiftEntry ANDESITE_CASING = omni("andesite_casing"),
		BRASS_CASING = omni("brass_casing"), COPPER_CASING = omni("copper_casing"),
		SHADOW_STEEL_CASING = omni("shadow_steel_casing"), REFINED_RADIANCE_CASING = omni("refined_radiance_casing"),
		RAILWAY_CASING = omni("railway_casing"), RAILWAY_CASING_SIDE = omni("railway_casing_side"),
		CREATIVE_CASING = getCT(AllCTTypes.RECTANGLE, "creative_casing");

	public static final CTSpriteShiftEntry CHASSIS_SIDE = omni("linear_chassis_side"),
		SECONDARY_CHASSIS_SIDE = omni("secondary_linear_chassis_side"), CHASSIS = omni("linear_chassis_end"),
		CHASSIS_STICKY = omni("linear_chassis_end_sticky");

	public static final CTSpriteShiftEntry BRASS_TUNNEL_TOP = vertical("tunnel/brass_tunnel_top"),
		FLUID_TANK = getCT(AllCTTypes.RECTANGLE, "fluid_tank"),
		FLUID_TANK_TOP = getCT(AllCTTypes.RECTANGLE, "fluid_tank_top"),
		FLUID_TANK_INNER = getCT(AllCTTypes.RECTANGLE, "fluid_tank_inner"),
		CREATIVE_FLUID_TANK = getCT(AllCTTypes.RECTANGLE, "creative_fluid_tank");

	public static final Couple<CTSpriteShiftEntry> VAULT_TOP = vault("top"), VAULT_FRONT = vault("front"),
		VAULT_SIDE = vault("side"), VAULT_BOTTOM = vault("bottom");

	public static final SpriteShiftEntry ELEVATOR_BELT =
		get("block/elevator_pulley_belt", "block/elevator_pulley_belt_scroll"),
		ELEVATOR_COIL = get("block/elevator_pulley_coil", "block/elevator_pulley_coil_scroll");

	public static final SpriteShiftEntry BELT = get("block/belt", "block/belt_scroll"),
		BELT_OFFSET = get("block/belt_offset", "block/belt_scroll"),
		BELT_DIAGONAL = get("block/belt_diagonal", "block/belt_diagonal_scroll"),
		ANDESIDE_BELT_CASING = get("block/belt/brass_belt_casing", "block/belt/andesite_belt_casing"),
		CRAFTER_THINGIES = get("block/crafter_thingies", "block/crafter_thingies");

	static {
		populateMaps();
	}

	private static void populateMaps() {
		WoodType[] supportedWoodTypes = new WoodType[] { WoodType.OAK, WoodType.SPRUCE, WoodType.BIRCH, WoodType.ACACIA,
			WoodType.JUNGLE, WoodType.DARK_OAK, WoodType.MANGROVE, WoodType.CRIMSON, WoodType.WARPED };
		Arrays.stream(supportedWoodTypes)
			.forEach(woodType -> WOODEN_WINDOWS.put(woodType, vertical("palettes/" + woodType.name() + "_window")));

		for (DyeColor color : DyeColor.values()) {
			String id = color.getSerializedName();
			DYED_BELTS.put(color, get("block/belt", "block/belt/" + id + "_scroll"));
			DYED_OFFSET_BELTS.put(color, get("block/belt_offset", "block/belt/" + id + "_scroll"));
			DYED_DIAGONAL_BELTS.put(color, get("block/belt_diagonal", "block/belt/" + id + "_diagonal_scroll"));
		}
	}

	private static Couple<CTSpriteShiftEntry> vault(String name) {
		final String prefixed = "block/vault/vault_" + name;
		return Couple.createWithContext(
			medium -> CTSpriteShifter.getCT(AllCTTypes.RECTANGLE, Create.asResource(prefixed + "_small"),
				Create.asResource(medium ? prefixed + "_medium" : prefixed + "_large")));
	}

	//

	private static CTSpriteShiftEntry omni(String name) {
		return getCT(AllCTTypes.OMNIDIRECTIONAL, name);
	}

	private static CTSpriteShiftEntry horizontal(String name) {
		return getCT(AllCTTypes.HORIZONTAL, name);
	}

	private static CTSpriteShiftEntry vertical(String name) {
		return getCT(AllCTTypes.VERTICAL, name);
	}

	//

	private static SpriteShiftEntry get(String originalLocation, String targetLocation) {
		return SpriteShifter.get(Create.asResource(originalLocation), Create.asResource(targetLocation));
	}

	private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName, String connectedTextureName) {
		return CTSpriteShifter.getCT(type, Create.asResource("block/" + blockTextureName),
			Create.asResource("block/" + connectedTextureName + "_connected"));
	}

	private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName) {
		return getCT(type, blockTextureName, blockTextureName);
	}

	//

	public static CTSpriteShiftEntry getWoodenWindow(WoodType woodType) {
		return WOODEN_WINDOWS.get(woodType);
	}

}
