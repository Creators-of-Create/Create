package com.simibubi.create.infrastructure.gametest.tests;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlock;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import it.unimi.dsi.fastutil.objects.Object2LongMap;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraftforge.fluids.FluidStack;

@GameTestGroup(path = "contraptions")
public class TestContraptions {
	@GameTest(template = "arrow_dispenser", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void arrowDispenser(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(2, 3, 1);
		helper.pullLever(lever);
		BlockPos pos1 = new BlockPos(0, 5, 0);
		BlockPos pos2 = new BlockPos(4, 5, 4);
		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(7);
			List<Arrow> arrows = helper.getEntitiesBetween(EntityType.ARROW, pos1, pos2);
			if (arrows.size() != 4)
				helper.fail("Expected 4 arrows");
			helper.powerLever(lever); // disassemble contraption
			BlockPos dispenser = new BlockPos(2, 5, 2);
			// there should be 1 left over
			helper.assertContainerContains(dispenser, Items.ARROW);
		});
	}

	@GameTest(template = "crop_farming", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void cropFarming(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(4, 3, 1);
		helper.pullLever(lever);
		BlockPos output = new BlockPos(1, 3, 12);
		helper.succeedWhen(() -> helper.assertAnyContained(output, Items.WHEAT, Items.POTATO, Items.CARROT));
	}

	@GameTest(template = "mounted_item_extract", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
	public static void mountedItemExtract(CreateGameTestHelper helper) {
		BlockPos barrel = new BlockPos(1, 3, 2);
		Object2LongMap<Item> content = helper.getItemContent(barrel);
		BlockPos lever = new BlockPos(1, 5, 1);
		helper.pullLever(lever);
		BlockPos outputPos = new BlockPos(4, 2, 1);
		helper.succeedWhen(() -> {
			helper.assertContentPresent(content, outputPos); // verify all extracted
			helper.powerLever(lever);
			helper.assertContainerEmpty(barrel); // verify nothing left
		});
	}

	@GameTest(template = "mounted_fluid_drain", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void mountedFluidDrain(CreateGameTestHelper helper) {
		BlockPos tank = new BlockPos(1, 3, 2);
		FluidStack fluid = helper.getTankContents(tank);
		if (fluid.isEmpty())
			helper.fail("Tank empty");
		BlockPos lever = new BlockPos(1, 5, 1);
		helper.pullLever(lever);
		BlockPos output = new BlockPos(4, 2, 1);
		helper.succeedWhen(() -> {
			helper.assertFluidPresent(fluid, output); // verify all extracted
			helper.powerLever(lever); // disassemble contraption
			helper.assertTankEmpty(tank); // verify nothing left
		});
	}

	@GameTest(template = "ploughing")
	public static void ploughing(CreateGameTestHelper helper) {
		BlockPos dirt = new BlockPos(4, 2, 1);
		BlockPos lever = new BlockPos(3, 3, 2);
		helper.pullLever(lever);
		helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.FARMLAND, dirt));
	}

	@GameTest(template = "redstone_contacts")
	public static void redstoneContacts(CreateGameTestHelper helper) {
		BlockPos end = new BlockPos(5, 10, 1);
		BlockPos lever = new BlockPos(1, 3, 2);
		helper.pullLever(lever);
		helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, end));
	}

	@GameTest(template = "controls", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void controls(CreateGameTestHelper helper) {
		BlockPos button = new BlockPos(5, 5, 4);
		BlockPos gearshift = new BlockPos(4, 5, 4);
		BlockPos bearingPos = new BlockPos(4, 4, 4);
		AtomicInteger step = new AtomicInteger(1);

		List<BlockPos> dirt = List.of(new BlockPos(4, 2, 6), new BlockPos(2, 2, 4), new BlockPos(4, 2, 2));
		List<BlockPos> wheat = List.of(new BlockPos(4, 3, 7), new BlockPos(1, 3, 4), new BlockPos(4, 3, 1));

		helper.pressButton(button);
		helper.succeedWhen(() -> {
			// wait for gearshift to reset
			helper.assertBlockProperty(gearshift, SequencedGearshiftBlock.STATE, 0);
			if (step.get() == 4)
				return; // step 4: all done!
			MechanicalBearingBlockEntity bearing = helper.getBlockEntity(AllBlockEntityTypes.MECHANICAL_BEARING.get(), bearingPos);
			if (bearing.getMovedContraption() == null)
				helper.fail("Contraption not assembled");
			Contraption contraption = bearing.getMovedContraption().getContraption();
			switch (step.get()) {
				case 1 -> { // step 1: both should be active
					helper.assertBlockPresent(Blocks.FARMLAND, dirt.get(0));
					helper.assertBlockProperty(wheat.get(0), CropBlock.AGE, 0);
					// now disable harvester
					helper.toggleActorsOfType(contraption, AllBlocks.MECHANICAL_HARVESTER.get());
					helper.pressButton(button);
					step.incrementAndGet();
					helper.fail("Entering step 2");
				}
				case 2 -> { // step 2: harvester disabled
					helper.assertBlockPresent(Blocks.FARMLAND, dirt.get(1));
					helper.assertBlockProperty(wheat.get(1), CropBlock.AGE, 7);
					// now disable plough
					helper.toggleActorsOfType(contraption, AllBlocks.MECHANICAL_PLOUGH.get());
					helper.pressButton(button);
					step.incrementAndGet();
					helper.fail("Entering step 3");
				}
				case 3 -> { // step 3: both disabled
					helper.assertBlockPresent(Blocks.DIRT, dirt.get(2));
					helper.assertBlockProperty(wheat.get(2), CropBlock.AGE, 7);
					// successful!
					helper.pressButton(button);
					step.incrementAndGet();
					helper.fail("Entering step 4");
				}
			}
		});
	}

	@GameTest(template = "elevator")
	public static void elevator(CreateGameTestHelper helper) {
		BlockPos pulley = new BlockPos(5, 12, 3);
		BlockPos secondaryPulley = new BlockPos(5, 12, 1);
		BlockPos bottomLamp = new BlockPos(2, 3, 2);
		BlockPos topLamp = new BlockPos(2, 12, 2);
		BlockPos lever = new BlockPos(1, 11, 2);
		BlockPos elevatorStart = new BlockPos(4, 2, 2);
		BlockPos cowSpawn = new BlockPos(4, 4, 2);
		BlockPos cowEnd = new BlockPos(4, 13, 2);

		helper.runAtTickTime(1, () -> helper.spawn(EntityType.COW, cowSpawn));
		helper.runAtTickTime(
				15, () -> helper.getBlockEntity(AllBlockEntityTypes.ELEVATOR_PULLEY.get(), pulley).clicked()
		);
		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(1);
			if (!helper.getBlockState(lever).getValue(LeverBlock.POWERED)) { // step 1: check entity, lamps, and secondary, then move up
				helper.getFirstEntity(AllEntityTypes.CONTROLLED_CONTRAPTION.get(), elevatorStart); // make sure entity exists

				helper.assertBlockProperty(topLamp, RedstoneLampBlock.LIT, false);
				helper.assertBlockProperty(bottomLamp, RedstoneLampBlock.LIT, true);

				ElevatorPulleyBlockEntity secondary = helper.getBlockEntity(AllBlockEntityTypes.ELEVATOR_PULLEY.get(), secondaryPulley);
				if (secondary.getMirrorParent() == null)
					helper.fail("Secondary pulley has no parent");

				helper.pullLever(lever);
				helper.fail("Entering step 2");
			} else { // step 2: wait for top lamp and cow passenger
				helper.assertBlockProperty(topLamp, RedstoneLampBlock.LIT, true);
				helper.assertBlockProperty(bottomLamp, RedstoneLampBlock.LIT, false);
				helper.assertEntityPresent(EntityType.COW, cowEnd);
				// all done, disassemble
				helper.getBlockEntity(AllBlockEntityTypes.ELEVATOR_PULLEY.get(), pulley).clicked();
			}
		});
	}

	@GameTest(template = "roller_filling")
	public static void rollerFilling(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(7, 6, 1);
		BlockPos barrelEnd = new BlockPos(2, 5, 2);
		List<BlockPos> existing = BlockPos.betweenClosedStream(new BlockPos(1, 3, 2), new BlockPos(4, 2, 2)).toList();
		List<BlockPos> filled = BlockPos.betweenClosedStream(new BlockPos(1, 2, 1), new BlockPos(4, 3, 3))
				.filter(pos -> !existing.contains(pos)).toList();
		List<BlockPos> tracks = BlockPos.betweenClosedStream(new BlockPos(1, 4, 2), new BlockPos(4, 4, 2)).toList();
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(4);
			existing.forEach(pos -> helper.assertBlockPresent(AllBlocks.RAILWAY_CASING.get(), pos));
			filled.forEach(pos -> helper.assertBlockPresent(AllBlocks.ANDESITE_CASING.get(), pos));
			tracks.forEach(pos -> helper.assertBlockPresent(AllBlocks.TRACK.get(), pos));
			helper.assertContainerEmpty(barrelEnd);
		});
	}

	@GameTest(template = "roller_paving_and_clearing", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void rollerPavingAndClearing(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(8, 5, 1);
		List<BlockPos> paved = BlockPos.betweenClosedStream(new BlockPos(1, 2, 1), new BlockPos(4, 2, 1)).toList();
		// block above will be cleared too, but will later be replaced by the contraption's barrel
		BlockPos cleared = new BlockPos(2, 3, 1);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(9);
			paved.forEach(pos -> helper.assertBlockPresent(AllBlocks.ANDESITE_CASING.get(), pos));
			helper.assertBlockPresent(Blocks.AIR, cleared);
		});
	}

	// FIXME: trains do not enjoy being loaded in structures
	// https://gist.github.com/TropheusJ/f2d0a7df48360d2e078d0987c115c6ef
//	@GameTest(template = "train_observer")
//	public static void trainObserver(CreateGameTestHelper helper) {
//		helper.fail("NYI");
//	}
}
