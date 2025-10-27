package com.simibubi.create;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.target.DisplayBoardTarget;
import com.simibubi.create.content.redstone.displayLink.target.LecternDisplayTarget;
import com.simibubi.create.content.redstone.displayLink.target.NixieTubeDisplayTarget;
import com.simibubi.create.content.redstone.displayLink.target.SignDisplayTarget;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Map;
import java.util.function.Supplier;

public class AllDisplayTargets {
	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static final RegistryEntry<DisplayTarget, SignDisplayTarget> SIGN = REGISTRATE.displayTarget("sign", SignDisplayTarget::new)
		.associate(BlockEntityType.SIGN)
		.register();
	public static final RegistryEntry<DisplayTarget, LecternDisplayTarget> LECTERN = REGISTRATE.displayTarget("lectern", LecternDisplayTarget::new)
		.associate(BlockEntityType.LECTERN)
		.register();

	public static final RegistryEntry<DisplayTarget, DisplayBoardTarget> DISPLAY_BOARD = simple("display_board", DisplayBoardTarget::new);
	public static final RegistryEntry<DisplayTarget, NixieTubeDisplayTarget> NIXIE_TUBE = simple("nixie_tube", NixieTubeDisplayTarget::new);

	public static final Map<String, RegistryEntry<DisplayTarget, ? extends DisplayTarget>> LEGACY_NAMES = Map.of(
		"sign_display_target", SIGN,
		"lectern_display_target", LECTERN,
		"display_board_target", DISPLAY_BOARD,
		"nixie_tube_target", NIXIE_TUBE
	);

	private static <T extends DisplayTarget> RegistryEntry<DisplayTarget, T> simple(String name, Supplier<T> supplier) {
		return REGISTRATE.displayTarget(name, supplier).register();
	}

	public static void register() {
	}
}
