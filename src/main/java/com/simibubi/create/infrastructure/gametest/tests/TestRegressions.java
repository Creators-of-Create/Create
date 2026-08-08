package com.simibubi.create.infrastructure.gametest.tests;

import static com.simibubi.create.infrastructure.gametest.CreateGameTestHelper.TEN_SECONDS;

import java.util.ArrayList;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.block.state.BlockState;
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

	@GameTest(template = "issue9615_efficient_deployers")
	public static void issue909_diagonalSidewaysBelts(CreateGameTestHelper helper) {
		BlockPos start = new BlockPos(0, 2, 0);
		BlockPos middle = new BlockPos(1, 2, 1);
		BlockPos end = new BlockPos(2, 2, 2);
		BlockState verticalShaft = AllBlocks.SHAFT.getDefaultState()
			.setValue(AbstractSimpleShaftBlock.AXIS, Axis.Y);

		helper.setBlock(start, verticalShaft);
		helper.setBlock(middle, Blocks.AIR);
		helper.setBlock(end, verticalShaft);

		BlockPos absoluteStart = helper.absolutePos(start);
		BlockPos absoluteEnd = helper.absolutePos(end);
		helper.assertTrue(BeltConnectorItem.canConnect(helper.getLevel(), absoluteStart, absoluteEnd),
			"Vertical shafts should accept a diagonal belt connection");
		BeltConnectorItem.createBelts(helper.getLevel(), absoluteStart, absoluteEnd);

		helper.assertBlockProperty(start, BeltBlock.SLOPE, BeltSlope.DIAGONAL_SIDEWAYS);
		helper.assertBlockProperty(middle, BeltBlock.SLOPE, BeltSlope.DIAGONAL_SIDEWAYS);
		helper.assertBlockProperty(end, BeltBlock.SLOPE, BeltSlope.DIAGONAL_SIDEWAYS);
		helper.assertBlockProperty(start, BeltBlock.HORIZONTAL_FACING, Direction.EAST);
		helper.assertBlockProperty(middle, BeltBlock.HORIZONTAL_FACING, Direction.EAST);
		helper.assertBlockProperty(end, BeltBlock.HORIZONTAL_FACING, Direction.EAST);
		helper.assertBlockProperty(start, BeltBlock.PART, BeltPart.START);
		helper.assertBlockProperty(middle, BeltBlock.PART, BeltPart.MIDDLE);
		helper.assertBlockProperty(end, BeltBlock.PART, BeltPart.END);
		BeltBlockEntity startBelt = helper.getBlockEntity(AllBlockEntityTypes.BELT.get(), start);
		BlockState startState = startBelt.getBlockState();
		ArrayList<BlockPos> propagationLocations = new ArrayList<>();
		startBelt.addPropagationLocations((IRotate) startState.getBlock(), startState, propagationLocations);
		helper.assertTrue(propagationLocations.contains(absoluteStart.offset(1, 0, 1)),
			"Diagonal belt segments should propagate rotation to one another");

		assertDiagonalDirection(helper, 1, 1, Direction.EAST);
		assertDiagonalDirection(helper, 1, -1, Direction.NORTH);
		assertDiagonalDirection(helper, -1, 1, Direction.SOUTH);
		assertDiagonalDirection(helper, -1, -1, Direction.WEST);
		helper.succeed();
	}

	private static void assertDiagonalDirection(CreateGameTestHelper helper, int x, int z, Direction expectedFacing) {
		Direction facing = BeltHelper.getDiagonalFacing(x, z);
		helper.assertTrue(facing == expectedFacing, "Diagonal offset should select the expected facing");
		helper.assertTrue(BeltHelper.getBeltVector(facing, BeltSlope.DIAGONAL_SIDEWAYS)
			.equals(new BlockPos(x, 0, z)), "Diagonal facing should reproduce the original offset");
	}
}
