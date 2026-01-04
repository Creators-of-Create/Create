package com.simibubi.create.infrastructure.gametest.tests;

import static com.simibubi.create.infrastructure.gametest.CreateGameTestHelper.TEN_SECONDS;

import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.block.Blocks;

@GameTestGroup(path = "regressions")
public class TestRegressions {
	@GameTest(template = "issue9615_efficient_deployers", timeoutTicks = TEN_SECONDS)
	public static void issue9615_efficientDeployers(CreateGameTestHelper helper) {
		final BlockPos lever = new BlockPos(2, 5, 0);
		final BlockPos goal = new BlockPos(1, 3, 4);
		helper.unpowerLever(lever);
		helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.LIME_STAINED_GLASS, goal));
	}
}
