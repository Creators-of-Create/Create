package com.simibubi.create.infrastructure.gametest.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyFluidHandler;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlock;
import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestGroup(path = "fluids")
public class TestFluids {
	@GameTest(template = "hose_pulley_transfer", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
	public static void hosePulleyTransfer(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(7, 7, 5);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(15);
			// check filled
			BlockPos filledLowerCorner = new BlockPos(2, 3, 2);
			BlockPos filledUpperCorner = new BlockPos(4, 5, 4);
			BlockPos.betweenClosed(filledLowerCorner, filledUpperCorner)
					.forEach(pos -> helper.assertBlockPresent(Blocks.WATER, pos));
			// check emptied
			BlockPos emptiedLowerCorner = new BlockPos(8, 3, 2);
			BlockPos emptiedUpperCorner = new BlockPos(10, 5, 4);
			BlockPos.betweenClosed(emptiedLowerCorner, emptiedUpperCorner)
					.forEach(pos -> helper.assertBlockPresent(Blocks.AIR, pos));
			// check nothing left in pulley
			BlockPos pulleyPos = new BlockPos(4, 7, 3);
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
	public static void inWorldPumpingOut(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(4, 3, 3);
		BlockPos basin = new BlockPos(5, 2, 2);
		BlockPos output = new BlockPos(2, 2, 2);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertBlockPresent(Blocks.WATER, output);
			helper.assertTankEmpty(basin);
		});
	}

	@GameTest(template = "in_world_pumping_in")
	public static void inWorldPumpingIn(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(4, 3, 3);
		BlockPos basin = new BlockPos(5, 2, 2);
		BlockPos water = new BlockPos(2, 2, 2);
		FluidStack expectedResult = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertBlockPresent(Blocks.AIR, water);
			helper.assertFluidPresent(expectedResult, basin);
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

	@GameTest(template = "large_waterwheel", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void largeWaterwheel(CreateGameTestHelper helper) {
		BlockPos wheel = new BlockPos(4, 3, 2);
		BlockPos leftEnd = new BlockPos(6, 2, 2);
		BlockPos rightEnd = new BlockPos(2, 2, 2);
		List<BlockPos> edges = List.of(new BlockPos(4, 5, 1), new BlockPos(4, 5, 3));
		BlockPos openLever = new BlockPos(3, 8, 1);
		BlockPos leftLever = new BlockPos(5, 7, 1);
		waterwheel(helper, wheel, 4, 512, leftEnd, rightEnd, edges, openLever, leftLever);
	}

	@GameTest(template = "small_waterwheel", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void smallWaterwheel(CreateGameTestHelper helper) {
		BlockPos wheel = new BlockPos(3, 2, 2);
		BlockPos leftEnd = new BlockPos(4, 2, 2);
		BlockPos rightEnd = new BlockPos(2, 2, 2);
		List<BlockPos> edges = List.of(new BlockPos(3, 3, 1), new BlockPos(3, 3, 3));
		BlockPos openLever = new BlockPos(2, 6, 1);
		BlockPos leftLever = new BlockPos(4, 5, 1);
		waterwheel(helper, wheel, 8, 128, leftEnd, rightEnd, edges, openLever, leftLever);
	}

	private static void waterwheel(CreateGameTestHelper helper,
								   BlockPos wheel, float expectedRpm, float expectedSU,
								   BlockPos leftEnd, BlockPos rightEnd, List<BlockPos> edges,
								   BlockPos openLever, BlockPos leftLever) {
		BlockPos speedometer = wheel.north();
		BlockPos stressometer = wheel.south();
		helper.pullLever(openLever);
		helper.succeedWhen(() -> {
			// must always be true
			edges.forEach(pos -> helper.assertBlockNotPresent(Blocks.WATER, pos));
			helper.assertBlockPresent(Blocks.WATER, rightEnd);
			// first step: expect water on left end while flow is allowed
			if (!helper.getBlockState(leftLever).getValue(LeverBlock.POWERED)) {
				helper.assertBlockPresent(Blocks.WATER, leftEnd);
				// water is present. both sides should cancel.
				helper.assertSpeedometerSpeed(speedometer, 0);
				helper.assertStressometerCapacity(stressometer, 0);
				// success, pull the lever, enter step 2
				helper.powerLever(leftLever);
				helper.fail("Entering step 2");
			} else {
				// lever is pulled, flow should stop
				helper.assertBlockNotPresent(Blocks.WATER, leftEnd);
				// 1-sided flow, should be spinning
				helper.assertSpeedometerSpeed(speedometer, expectedRpm);
				helper.assertStressometerCapacity(stressometer, expectedSU);
			}
		});
	}

	@GameTest(template = "waterwheel_materials", timeoutTicks = CreateGameTestHelper.FIFTEEN_SECONDS)
	public static void waterwheelMaterials(CreateGameTestHelper helper) {
		List<Item> planks = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.PLANKS).stream()
				.map(ItemLike::asItem).collect(Collectors.toCollection(ArrayList::new));
		List<BlockPos> chests = List.of(new BlockPos(6, 4, 2), new BlockPos(6, 4, 3));
		List<BlockPos> deployers = chests.stream().map(pos -> pos.below(2)).toList();
		helper.runAfterDelay(3, () -> chests.forEach(chest ->
				planks.forEach(plank -> ItemHandlerHelper.insertItem(helper.itemStorageAt(chest), new ItemStack(plank), false))
		));

		BlockPos smallWheel = new BlockPos(4, 2, 2);
		BlockPos largeWheel = new BlockPos(3, 3, 3);
		BlockPos lever = new BlockPos(5, 3, 1);
		helper.pullLever(lever);

		helper.succeedWhen(() -> {
			Item plank = planks.get(0);
			if (!(plank instanceof BlockItem blockItem))
				throw new GameTestAssertException(ForgeRegistries.ITEMS.getKey(plank) + " is not a BlockItem");
			Block block = blockItem.getBlock();

			WaterWheelBlockEntity smallWheelBe = helper.getBlockEntity(AllBlockEntityTypes.WATER_WHEEL.get(), smallWheel);
			if (!smallWheelBe.material.is(block))
				helper.fail("Small waterwheel has not consumed " + ForgeRegistries.ITEMS.getKey(plank));

			WaterWheelBlockEntity largeWheelBe = helper.getBlockEntity(AllBlockEntityTypes.LARGE_WATER_WHEEL.get(), largeWheel);
			if (!largeWheelBe.material.is(block))
				helper.fail("Large waterwheel has not consumed " + ForgeRegistries.ITEMS.getKey(plank));

			// next item
			planks.remove(0);
			deployers.forEach(pos -> {
				IItemHandler handler = helper.itemStorageAt(pos);
				for (int i = 0; i < handler.getSlots(); i++) {
					handler.extractItem(i, Integer.MAX_VALUE, false);
				}
			});
			if (!planks.isEmpty())
				helper.fail("Not all planks have been consumed");
		});
	}

	@GameTest(template = "smart_observer_pipes")
	public static void smartObserverPipes(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(3, 3, 1);
		BlockPos output = new BlockPos(3, 4, 4);
		BlockPos tankOutput = new BlockPos(1, 2, 4);
		FluidStack expected = new FluidStack(Fluids.WATER, 2 * FluidAttributes.BUCKET_VOLUME);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertFluidPresent(expected, tankOutput);
			helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, output);
		});
	}

	@GameTest(template = "threshold_switch", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
	public static void thresholdSwitch(CreateGameTestHelper helper) {
		BlockPos leftHandle = new BlockPos(4, 2, 4);
		BlockPos leftValve = new BlockPos(4, 2, 3);
		BlockPos leftTank = new BlockPos(5, 2, 3);

		BlockPos rightHandle = new BlockPos(2, 2, 4);
		BlockPos rightValve = new BlockPos(2, 2, 3);
		BlockPos rightTank = new BlockPos(1, 2, 3);

		BlockPos drainHandle = new BlockPos(3, 3, 2);
		BlockPos drainValve = new BlockPos(3, 3, 1);
		BlockPos lamp = new BlockPos(1, 3, 1);
		BlockPos tank = new BlockPos(2, 2, 1);
		helper.succeedWhen(() -> {
			if (!helper.getBlockState(leftValve).getValue(FluidValveBlock.ENABLED)) { // step 1
				helper.getBlockEntity(AllBlockEntityTypes.VALVE_HANDLE.get(), leftHandle)
						.activate(false); // open the valve, fill 4 buckets
				helper.fail("Entering step 2");
			} else if (!helper.getBlockState(rightValve).getValue(FluidValveBlock.ENABLED)) { // step 2
				helper.assertFluidPresent(FluidStack.EMPTY, leftTank); // wait for left tank to drain
				helper.assertBlockProperty(lamp, RedstoneLampBlock.LIT, false); // should not be on yet
				helper.getBlockEntity(AllBlockEntityTypes.VALVE_HANDLE.get(), rightHandle)
						.activate(false); // fill another 4 buckets
				helper.fail("Entering step 3");
			} else if (!helper.getBlockState(drainValve).getValue(FluidValveBlock.ENABLED)) { // step 3
				helper.assertFluidPresent(FluidStack.EMPTY, rightTank); // wait for right tank to drain
				// 16 buckets inserted. tank full, lamp on.
				helper.assertBlockProperty(lamp, RedstoneLampBlock.LIT, true);
				// drain what's filled so far
				helper.getBlockEntity(AllBlockEntityTypes.VALVE_HANDLE.get(), drainHandle)
						.activate(false); // drain all 8 buckets
				helper.fail("Entering step 4");
			} else {
				helper.assertTankEmpty(tank); // wait for it to empty
				helper.assertBlockProperty(lamp, RedstoneLampBlock.LIT, false); // should be off now
			}
		});
	}
}
