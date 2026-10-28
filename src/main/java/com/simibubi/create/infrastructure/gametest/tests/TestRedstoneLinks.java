package com.simibubi.create.infrastructure.gametest.tests;

import com.simibubi.create.content.redstone.diodes.BrassDiodeBlock;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;

@GameTestGroup(path = "redstone_links")
public class TestRedstoneLinks {

	@GameTest(template = "delay", timeoutTicks = 1, setupTicks = 1)
	public static void delay(CreateGameTestHelper helper) {
		final BlockPos lever = new BlockPos(4, 2, 1);
		final BlockPos link1 = new BlockPos(3, 2, 1);
		final BlockPos link2 = new BlockPos(1, 2, 1);
		final boolean[][] expected = {{false, false}, {true, true}};
		final boolean[][] powered = new boolean[2][2];

		helper.pullLever(lever);

		helper.succeedWhen(() -> {
			if (helper.getTick() < expected.length) {
				powered[(int) helper.getTick()][0] = helper.getBlockState(link1).getValue(RedstoneLinkBlock.POWERED);
				powered[(int) helper.getTick()][1] = helper.getBlockState(link2).getValue(RedstoneLinkBlock.POWERED);
			}
			if (helper.getTick() >= expected.length - 1) {
				compare(expected, powered, helper);
				helper.pullLever(lever);
			} else {
				helper.fail("Waiting");
			}
		});
	}

	@GameTest(template = "pulsing", timeoutTicks = 5, setupTicks = 1)
	public static void pulsing(CreateGameTestHelper helper) {
		final BlockPos lever = new BlockPos(5, 2, 1);
		final BlockPos pulseTimer = new BlockPos(4, 2, 1);
		final BlockPos link1 = new BlockPos(3, 2, 1);
		final BlockPos link2 = new BlockPos(1, 2, 1);
		final boolean[][] expected = {{false, false, false}, {false, false, false}, {false, false, false}, {true, false, false}, {false, true, true}, {true, false, false}};
		final boolean[][] powered = new boolean[6][3];

		helper.pullLever(lever);

		helper.succeedWhen(() -> {
			if (helper.getTick() < expected.length) {
				powered[(int) helper.getTick()][0] = helper.getBlockState(pulseTimer).getValue(BrassDiodeBlock.POWERING);
				powered[(int) helper.getTick()][1] = helper.getBlockState(link1).getValue(RedstoneLinkBlock.POWERED);
				powered[(int) helper.getTick()][2] = helper.getBlockState(link2).getValue(RedstoneLinkBlock.POWERED);
			}
			if (helper.getTick() >= expected.length - 1) {
				compare(expected, powered, helper);
				helper.pullLever(lever);
			} else {
				helper.fail("Waiting");
			}
		});
	}

	@GameTest(template = "channels", timeoutTicks = 5, setupTicks = 1)
	public static void channels(CreateGameTestHelper helper) {
		final BlockPos lever = new BlockPos(6, 2, 1);
		final BlockPos link1 = new BlockPos(5, 2, 1);
		final BlockPos link2 = new BlockPos(3, 2, 1);
		final BlockPos link3 = new BlockPos(1, 2, 1);
		final boolean[][] expected = {{false, false, false}, {true, true, false}};
		final boolean[][] powered = new boolean[2][3];

		helper.pullLever(lever);

		helper.succeedWhen(() -> {
			if (helper.getTick() < expected.length) {
				powered[(int) helper.getTick()][0] = helper.getBlockState(link1).getValue(RedstoneLinkBlock.POWERED);
				powered[(int) helper.getTick()][1] = helper.getBlockState(link2).getValue(RedstoneLinkBlock.POWERED);
				powered[(int) helper.getTick()][2] = helper.getBlockState(link3).getValue(RedstoneLinkBlock.POWERED);
			}
			if (helper.getTick() >= expected.length - 1) {
				compare(expected, powered, helper);
				helper.pullLever(lever);
			} else {
				helper.fail("Waiting");
			}
		});
	}

	private static void compare(boolean[][] expected, boolean[][] actual, CreateGameTestHelper helper) {
		for (int i = 0; i < expected.length; i++) {
			for (int j = 0; j < expected[i].length; j++) {
				if (expected[i][j] != actual[i][j]) {
					helper.fail("Expected: " + stringify(expected) + " Got: " + stringify(actual));
				}
			}
		}
		helper.succeed();
	}

	private static String stringify(boolean[][] data) {
		StringBuilder string = new StringBuilder("[");
		for (boolean[] outer : data) {
			string.append("[");
			for (boolean inner : outer) {
				string.append(inner).append(",");
			}
			string.append("]");
		}
		string.append("]");

		return string.toString();
	}
}
