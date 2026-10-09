package com.simibubi.create.infrastructure.gametest.tests;

import static com.simibubi.create.infrastructure.gametest.CreateGameTestHelper.TEN_SECONDS;
import static com.simibubi.create.infrastructure.gametest.CreateGameTestHelper.TWENTY_SECONDS;

import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;

@GameTestGroup(path = "regressions")
public class TestRegressions {
	@GameTest(template = "issue9615_efficient_deployers", timeoutTicks = TEN_SECONDS)
	public static void issue9615_efficientDeployers(CreateGameTestHelper helper) {
		final BlockPos lever = new BlockPos(2, 5, 0);
		final BlockPos goal = new BlockPos(1, 3, 4);
		helper.unpowerLever(lever);
		helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.LIME_STAINED_GLASS, goal));
	}

	@GameTest(template = "issue9381_turtle_eggs_deployers", timeoutTicks = TWENTY_SECONDS)
	public static void deployerTurtleEggs(CreateGameTestHelper helper) {
		final BlockPos hopperSilk = new BlockPos(1, 2, 1);
		final BlockPos hopperNormal = new BlockPos(1, 2, 3);
		final BlockPos lever = new BlockPos(3, 4, 2);
		final BlockPos eggPosSilk = new BlockPos(4, 3, 1);
		final BlockPos eggPosNormal = new BlockPos(4, 3, 3);

		helper.unpowerLever(lever);

		for (int i = 1; i <= 4; i++) {
			final int breakNumber = i;
			// normal deployer breaks one egg per cycle, drops nothing
			helper.runAfterDelay(59 + 65 * (i - 1), () -> {
				BlockState eggsStateNormal = helper.getBlockState(eggPosNormal);

				if (breakNumber < 4) {
					helper.assertTrue(eggsStateNormal.is(Blocks.TURTLE_EGG),
						"Normal egg cluster should still exist after break " + breakNumber);
					helper.assertTrue(eggsStateNormal.getValue(TurtleEggBlock.EGGS) == 4 - breakNumber,
						"Normal egg cluster should have " + (4 - breakNumber) + " eggs remaining, got " + eggsStateNormal.getValue(TurtleEggBlock.EGGS));
				} else {
					helper.assertBlockNotPresent(Blocks.TURTLE_EGG, eggPosNormal);
				}
			});

			// silk touch deployer breaks one egg per cycle, collecting each into the hopper
			// takes slightly longer per cycle than normal due to storing the collected egg
			helper.runAfterDelay(69 + 76 * (i - 1), () -> {
				BlockState eggsStateSilk = helper.getBlockState(eggPosSilk);
				long eggsInHopper = helper.getItemContent(hopperSilk).getLong(Items.TURTLE_EGG);

				if (breakNumber < 4) {
					helper.assertTrue(eggsStateSilk.is(Blocks.TURTLE_EGG),
						"Silk touch egg cluster should still exist after break " + breakNumber);
					helper.assertTrue(eggsStateSilk.getValue(TurtleEggBlock.EGGS) == 4 - breakNumber,
						"Silk touch egg cluster should have " + (4 - breakNumber) + " eggs remaining, got " + eggsStateSilk.getValue(TurtleEggBlock.EGGS));
				} else {
					helper.assertBlockNotPresent(Blocks.TURTLE_EGG, eggPosSilk);
				}
				helper.assertTrue(eggsInHopper == breakNumber,
						"Silk touch hopper should have " + breakNumber + " eggs, got " + eggsInHopper);
			});
		}

		helper.succeedWhen(() -> {
			helper.assertBlockNotPresent(Blocks.TURTLE_EGG, eggPosNormal);
			helper.assertBlockNotPresent(Blocks.TURTLE_EGG, eggPosSilk);
			helper.assertContainerEmpty(hopperNormal);
			long eggCount = helper.getItemContent(hopperSilk).getLong(Items.TURTLE_EGG);
			helper.assertTrue(eggCount == 4, "Expected 4 eggs in hopper, got " + eggCount);
		});
	}
}
