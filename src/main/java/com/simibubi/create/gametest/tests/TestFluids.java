package com.simibubi.create.gametest.tests;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.fluids.actors.HosePulleyFluidHandler;
import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeBlockEntity;
import com.simibubi.create.gametest.infrastructure.CreateGameTestHelper;
import com.simibubi.create.gametest.infrastructure.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

@GameTestGroup(path = "fluids")
public class TestFluids {
	@GameTest(template = "hose_pulley_transfer", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
	public static void hosePulleyTransfer(CreateGameTestHelper helper) {
		// there was supposed to be redstone here built in, but it kept popping off, so put it there manually
		BlockPos brokenRedstone = new BlockPos(4, 8, 3);
		BlockState redstone = Blocks.REDSTONE_WIRE.defaultBlockState()
				.setValue(RedStoneWireBlock.NORTH, RedstoneSide.NONE)
				.setValue(RedStoneWireBlock.SOUTH, RedstoneSide.NONE)
				.setValue(RedStoneWireBlock.EAST, RedstoneSide.UP)
				.setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE)
				.setValue(RedStoneWireBlock.POWER, 14);
		helper.setBlock(brokenRedstone, redstone);
		// pump
		BlockPos lever = new BlockPos(6, 9, 3);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(15);
			// check filled
			BlockPos filledLowerCorner = new BlockPos(8, 3, 2);
			BlockPos filledUpperCorner = new BlockPos(10, 5, 4);
			BlockPos.betweenClosed(filledLowerCorner, filledUpperCorner)
					.forEach(pos -> helper.assertBlockPresent(Blocks.WATER, pos));
			// check emptied
			BlockPos emptiedLowerCorner = new BlockPos(2, 3, 2);
			BlockPos emptiedUpperCorner = new BlockPos(4, 5, 4);
			BlockPos.betweenClosed(emptiedLowerCorner, emptiedUpperCorner)
					.forEach(pos -> helper.assertBlockPresent(Blocks.AIR, pos));
			// check nothing left in pulley
			BlockPos pulleyPos = new BlockPos(8, 7, 4);
			IFluidHandler storage = helper.fluidStorageAt(pulleyPos);
			if (storage instanceof HosePulleyFluidHandler hose) {
				IFluidHandler internalTank = hose.getInternalTank();
				if (!internalTank.drain(1, FluidAction.SIMULATE).isEmpty())
					helper.fail("Pulley not empty");
			} else {
				helper.fail("Not a pulley");
			}
		});
	}

	@GameTest(template = "in_world_pumping_out")
	public static void inWorldPumpingOutput(CreateGameTestHelper helper) {
		BlockPos pumpPos = new BlockPos(3, 2, 2);
		BlockPos waterPos = pumpPos.west();
		BlockPos basinPos = pumpPos.east();
		helper.flipBlock(pumpPos);
		helper.succeedWhen(() -> {
			helper.assertBlockPresent(Blocks.WATER, waterPos);
			helper.assertTankEmpty(basinPos);
		});
	}

	@GameTest(template = "in_world_pumping_in")
	public static void inWorldPumpingPickup(CreateGameTestHelper helper) {
		BlockPos pumpPos = new BlockPos(3, 2, 2);
		BlockPos basinPos = pumpPos.east();
		BlockPos waterPos = pumpPos.west();
		FluidStack expectedResult = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
		helper.flipBlock(pumpPos);
		helper.succeedWhen(() -> {
			helper.assertBlockPresent(Blocks.AIR, waterPos);
			helper.assertFluidPresent(expectedResult, basinPos);
		});
	}

	@GameTest(template = "steam_engine")
	public static void steamEngine(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(4, 3, 3);
		helper.pullLever(lever);
		BlockPos stressometer = new BlockPos(5, 2, 5);
		BlockPos speedometer = new BlockPos(4, 2, 5);
		helper.succeedWhen(() -> {
			StressGaugeBlockEntity stress = helper.getBlockEntity(AllBlockEntityTypes.STRESSOMETER.get(), stressometer);
			SpeedGaugeBlockEntity speed = helper.getBlockEntity(AllBlockEntityTypes.SPEEDOMETER.get(), speedometer);
			float capacity = stress.getNetworkCapacity();
			helper.assertCloseEnoughTo(capacity, 2048);
			float rotationSpeed = Mth.abs(speed.getSpeed());
			helper.assertCloseEnoughTo(rotationSpeed, 16);
		});
	}

	@GameTest(template = "3_pipe_combine", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
	public static void threePipeCombine(CreateGameTestHelper helper) {
		BlockPos tank1Pos = new BlockPos(5, 2, 1);
		BlockPos tank2Pos = tank1Pos.south();
		BlockPos tank3Pos = tank2Pos.south();
		long initialContents = helper.getFluidInTanks(tank1Pos, tank2Pos, tank3Pos);

		BlockPos pumpPos = new BlockPos(2, 2, 2);
		helper.flipBlock(pumpPos);
		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(13);
			// make sure fully drained
			helper.assertTanksEmpty(tank1Pos, tank2Pos, tank3Pos);
			// and fully moved
			BlockPos outputTankPos = new BlockPos(1, 2, 2);
			long moved = helper.getFluidInTanks(outputTankPos);
			if (moved != initialContents)
				helper.fail("Wrong amount of fluid amount. expected [%s], got [%s]".formatted(initialContents, moved));
			// verify nothing was duped or deleted
		});
	}

	@GameTest(template = "3_pipe_split", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void threePipeSplit(CreateGameTestHelper helper) {
		BlockPos pumpPos = new BlockPos(2, 2, 2);
		BlockPos tank1Pos = new BlockPos(5, 2, 1);
		BlockPos tank2Pos = tank1Pos.south();
		BlockPos tank3Pos = tank2Pos.south();
		BlockPos outputTankPos = new BlockPos(1, 2, 2);

		long totalContents = helper.getFluidInTanks(tank1Pos, tank2Pos, tank3Pos, outputTankPos);
		helper.flipBlock(pumpPos);

		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(7);
			FluidStack contents = helper.getTankContents(outputTankPos);
			if (!contents.isEmpty()) {
				helper.fail("Tank not empty: " + contents.getAmount());
			}
			long newTotalContents = helper.getFluidInTanks(tank1Pos, tank2Pos, tank3Pos);
			if (newTotalContents != totalContents) {
				helper.fail("Wrong total fluid amount. expected [%s], got [%s]".formatted(totalContents, newTotalContents));
			}
		});
	}
}
