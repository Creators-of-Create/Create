package com.simibubi.create.infrastructure.gametest.tests;

import static com.simibubi.create.infrastructure.gametest.CreateGameTestHelper.TEN_SECONDS;

import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

@GameTestGroup(path = "regressions")
public class TestRegressions {
	// https://github.com/Creators-of-Create/Create/issues/9615
	@GameTest(template = "issue9615_efficient_deployers", timeoutTicks = TEN_SECONDS)
	public static void issue9615_efficientDeployers(CreateGameTestHelper helper) {
		final BlockPos lever = new BlockPos(2, 5, 0);
		final BlockPos goal = new BlockPos(1, 3, 4);
		helper.unpowerLever(lever);
		helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.LIME_STAINED_GLASS, goal));
	}

	// Tests to make sure items don't go under funnels when an inventory is full
	// https://github.com/Creators-of-Create/Create/issues/9334
	@GameTest(template = "issue9334_belt_funnel_interactions", timeoutTicks = TEN_SECONDS)
	public static void issue9334_beltFunnelInteractions(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(6, 3, 1);

		BlockPos[] startBarrels = CreateGameTestHelper.rowAlongAxis(Axis.X, new BlockPos(1, 3, 1), 6, 2);
		BlockPos[] startBelts = CreateGameTestHelper.rowAlongAxis(Axis.X, new BlockPos(1, 2, 2), 6, 2);
		BlockPos[] endStorages = CreateGameTestHelper.rowAlongAxis(Axis.X, new BlockPos(3, 3, 5), 4, 2);

		helper.unpowerLever(lever);
		helper.succeedWhen(() -> {
			helper.assertItemEntityNotPresent(Items.DIAMOND);

			for (BlockPos storage : endStorages) {
				helper.assertContainerContains(storage, Items.DIAMOND);
			}

			for (BlockPos barrel : startBarrels) {
				Object2LongMap<Item> items = helper.getItemContent(barrel);
				helper.assertValueEqual(
					(int) items.getOrDefault(Items.DIAMOND, -1),
					64 * 5,
					"amount of items left in initial storage"
				);
			}

			for (BlockPos belt : startBelts) {
				helper.assertContainerContains(belt, Items.DIAMOND);
			}
		});
	}
}
