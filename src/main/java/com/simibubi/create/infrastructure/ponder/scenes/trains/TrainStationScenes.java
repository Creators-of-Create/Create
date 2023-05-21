package com.simibubi.create.infrastructure.ponder.scenes.trains;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.trains.station.StationBlock;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement.FacePointOfInterestPose;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TrainStationScenes {

	public static void assembly(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("train_assembly", "Assembling Trains");
		scene.configureBasePlate(1, 0, 12);
		scene.scaleSceneView(.65f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();

		for (int i = 13; i >= 0; i--) {
			scene.world.showSection(util.select.position(i, 1, 6), Direction.DOWN);
			scene.idle(1);
		}

		BlockState air = Blocks.AIR.defaultBlockState();
		scene.world.setBlock(util.grid.at(10, 2, 6), air, false);
		scene.world.setBlock(util.grid.at(6, 2, 6), air, false);
		scene.world.setBlock(util.grid.at(3, 2, 6), air, false);

		scene.idle(10);

		Selection station = util.select.position(11, 1, 3);
		Selection controls = util.select.fromTo(9, 3, 6, 10, 3, 6);
		Selection train1 = util.select.fromTo(12, 2, 5, 8, 2, 7)
			.substract(util.select.position(10, 2, 6));
		Selection train2 = util.select.fromTo(7, 2, 5, 2, 2, 7)
			.substract(util.select.position(6, 2, 6))
			.substract(util.select.position(3, 2, 6));
		Selection train3 = util.select.fromTo(7, 2, 1, 3, 3, 3);

		BlockPos stationPos = util.grid.at(11, 1, 3);
		Vec3 marker = util.vector.topOf(11, 0, 6)
			.add(0, 3 / 16f, 0);
		Vec3 stationTop = util.vector.topOf(stationPos);

		AABB bb = new AABB(util.vector.topOf(11, 0, 6), util.vector.topOf(11, 0, 6)).move(0, 2 / 16f, 0);

		scene.overlay.showControls(new InputWindowElement(marker, Pointing.DOWN).rightClick()
			.withItem(AllBlocks.TRACK_STATION.asStack()), 40);
		scene.idle(6);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb, 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.inflate(.45f, 1 / 16f, .45f), 100);
		scene.idle(10);

		scene.overlay.showText(70)
			.pointAt(marker)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.text("Select a Train Track then place the Station nearby");
		scene.idle(60);

		scene.world.showSection(station, Direction.DOWN);
		scene.idle(15);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb,
			new AABB(stationPos).contract(1 / 16f, 2 / 16f, 1 / 16f), 20);
		scene.idle(25);

		scene.overlay.showText(80)
			.pointAt(marker)
			.attachKeyFrame()
			.placeNearTarget()
			.text("Stations are the Waypoints of your Track Network");
		scene.idle(90);

		scene.overlay.showControls(new InputWindowElement(stationTop, Pointing.DOWN).rightClick(), 50);
		scene.idle(16);
		scene.overlay.showText(70)
			.pointAt(stationTop)
			.placeNearTarget()
			.attachKeyFrame()
			.text("To create a new Train, open the UI and switch to Assembly Mode");
		scene.idle(50);

		scene.world.cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.effects.indicateSuccess(stationPos);

		scene.idle(20);
		scene.overlay.showControls(new InputWindowElement(marker, Pointing.DOWN).withItem(new ItemStack(Items.BARRIER)),
			60);
		scene.idle(6);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb, bb, 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb, bb.inflate(.45f, 1 / 16f, .45f), 80);
		scene.idle(10);
		scene.overlay.showText(70)
			.pointAt(marker)
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.text("During Assembly no scheduled trains will approach this station");
		scene.idle(85);

		ItemStack casing = AllBlocks.RAILWAY_CASING.asStack();
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(11, 0, 6), Pointing.RIGHT).rightClick()
			.withItem(casing), 80);
		scene.idle(6);
		scene.world.restoreBlocks(util.select.position(10, 2, 6));
		ElementLink<WorldSectionElement> trainElement1 =
			scene.world.showIndependentSection(util.select.position(10, 2, 6), Direction.DOWN);
		scene.idle(20);
		scene.overlay.showText(70)
			.pointAt(util.vector.blockSurface(util.grid.at(10, 2, 6), Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.text("Create new bogeys by using Train Casing on Tracks");
		scene.idle(55);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(4, 0, 6), Pointing.RIGHT).rightClick()
			.withItem(casing), 15);
		scene.idle(6);
		scene.world.setBlock(util.grid.at(3, 2, 6), AllBlocks.SMALL_BOGEY.getDefaultState(), false);
		ElementLink<WorldSectionElement> trainElement2 =
			scene.world.showIndependentSection(util.select.position(3, 2, 6), Direction.DOWN);
		scene.idle(20);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(7, 0, 6), Pointing.RIGHT).rightClick()
			.withItem(casing), 15);
		scene.idle(6);
		scene.world.setBlock(util.grid.at(6, 2, 6), AllBlocks.SMALL_BOGEY.getDefaultState(), false);
		scene.world.showSectionAndMerge(util.select.position(6, 2, 6), Direction.DOWN, trainElement2);
		scene.idle(30);

		scene.overlay.showText(50)
			.pointAt(util.vector.topOf(3, 0, 6))
			.placeNearTarget()
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.text("Click the track again to cycle between bogey designs");
		scene.idle(35);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(4, 0, 6), Pointing.RIGHT).rightClick(), 15);
		scene.idle(6);
		scene.world.restoreBlocks(util.select.position(3, 2, 6));
		scene.idle(20);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(7, 0, 6), Pointing.RIGHT).rightClick(), 15);
		scene.idle(6);
		scene.world.restoreBlocks(util.select.position(6, 2, 6));
		scene.idle(30);

		scene.overlay.showOutline(PonderPalette.GREEN, casing, util.select.position(10, 2, 6)
			.add(util.select.position(6, 2, 6))
			.add(util.select.position(3, 2, 6)), 40);

		scene.overlay.showText(70)
			.pointAt(util.vector.topOf(3, 2, 6))
			.placeNearTarget()
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("Attach blocks with the help of Super Glue");
		scene.idle(35);

		scene.world.showSectionAndMerge(train1, Direction.DOWN, trainElement1);
		scene.idle(10);
		scene.world.showSectionAndMerge(train2, Direction.DOWN, trainElement2);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.fromTo(6, 4, 5, 6, 3, 7), Direction.WEST, trainElement2);
		scene.idle(3);
		scene.world.showSectionAndMerge(util.select.fromTo(5, 3, 6, 4, 4, 7), Direction.NORTH, trainElement2);
		scene.idle(3);
		scene.world.showSectionAndMerge(util.select.fromTo(3, 3, 6, 3, 5, 6), Direction.DOWN, trainElement2);
		scene.idle(3);
		scene.world.showSectionAndMerge(util.select.fromTo(3, 5, 5, 3, 6, 5), Direction.SOUTH, trainElement2);
		scene.idle(3);
		scene.world.showSectionAndMerge(util.select.position(3, 3, 5), Direction.EAST, trainElement2);
		scene.idle(3);
		scene.world.showSectionAndMerge(util.select.position(5, 3, 5), Direction.SOUTH, trainElement2);
		scene.idle(10);

		AABB glue1 = new AABB(util.grid.at(10, 2, 6));
		AABB glue2 = new AABB(util.grid.at(4, 2, 6));
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, glue2, glue2, 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, glue2, glue2.inflate(2, 0, 1)
			.expandTowards(1, 3, 0), 60);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, glue1, glue1, 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, glue1, glue1.inflate(1.25, 0, .25)
			.expandTowards(0, 1, 0), 60);
		scene.idle(15);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(util.grid.at(4, 2, 6)), Pointing.UP)
			.withItem(AllItems.SUPER_GLUE.asStack()), 40);
		scene.idle(5);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(util.grid.at(10, 2, 6)), Pointing.UP)
			.withItem(AllItems.SUPER_GLUE.asStack()), 40);
		scene.idle(55);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(util.grid.at(5, 3, 5)), Pointing.DOWN)
			.withItem(new ItemStack(Items.CHARCOAL)), 40);
		scene.idle(10);
		scene.overlay.showText(90)
			.pointAt(util.vector.blockSurface(util.grid.at(5, 3, 5), Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.text("Assembled Trains will move faster if they can find fuel in assembled chests or barrels");
		scene.idle(100);
		scene.overlay.showSelectionWithText(util.select.fromTo(4, 3, 6, 5, 4, 7), 60)
			.pointAt(util.vector.blockSurface(util.grid.at(5, 4, 6), Direction.UP))
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.text("Fuel stored in Vaults will not be consumed by the train");
		scene.idle(75);

		ElementLink<WorldSectionElement> controlsElement = scene.world.showIndependentSection(controls, Direction.DOWN);
		scene.idle(15);
		scene.overlay.showText(60)
			.pointAt(util.vector.topOf(10, 3, 6))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Every Train requires Train Controls on board");
		scene.idle(55);
		scene.world.showSectionAndMerge(util.select.position(8, 3, 6), Direction.DOWN, controlsElement);
		scene.idle(15);
		scene.world.moveSection(controlsElement, util.vector.of(1, 0, 0), 10);
		scene.idle(15);

		scene.overlay.showText(80)
			.pointAt(util.vector.topOf(9, 3, 6))
			.placeNearTarget()
			.text("An optional second one allows departure from Stations in both directions");
		scene.idle(75);

		scene.overlay.showControls(new InputWindowElement(stationTop, Pointing.DOWN).rightClick(), 50);
		scene.idle(16);
		scene.overlay.showText(60)
			.pointAt(stationTop)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Open the Station UI and confirm the Assembly process");
		scene.idle(50);

		scene.world.toggleControls(util.grid.at(10, 3, 6));
		scene.world.toggleControls(util.grid.at(8, 3, 6));
		scene.world.cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.effects.indicateSuccess(stationPos);
		scene.world.animateTrainStation(stationPos, true);
		scene.idle(20);

		ElementLink<ParrotElement> birb =
			scene.special.createBirb(util.vector.centerOf(10, 3, 6), FacePointOfInterestPose::new);
		scene.idle(15);
		scene.special.movePointOfInterest(util.grid.at(18, 3, 6));
		scene.idle(15);
		scene.world.animateTrainStation(stationPos, false);
		scene.world.moveSection(controlsElement, util.vector.of(18, 0, 0), 70);
		scene.world.moveSection(trainElement1, util.vector.of(18, 0, 0), 70);
		scene.world.moveSection(trainElement2, util.vector.of(18, 0, 0), 70);
		scene.world.animateBogey(util.grid.at(10, 2, 6), -18f, 70);
		scene.world.animateBogey(util.grid.at(6, 2, 6), -18f, 70);
		scene.world.animateBogey(util.grid.at(3, 2, 6), -18f, 70);
		scene.special.moveParrot(birb, util.vector.of(18, 0, 0), 70);

		scene.idle(10);
		scene.world.hideIndependentSection(controlsElement, null);
		scene.world.hideIndependentSection(trainElement1, null);
		scene.special.hideElement(birb, null);
		scene.idle(20);
		scene.world.hideIndependentSection(trainElement2, null);
		scene.idle(20);

		scene.overlay.showText(70)
			.pointAt(stationTop)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Trains can be disassembled back into blocks at stations only");
		scene.idle(85);

		scene.overlay.showControls(new InputWindowElement(stationTop, Pointing.DOWN).rightClick()
			.withItem(new ItemStack(Items.FILLED_MAP)), 75);
		scene.idle(15);
		scene.overlay.showText(70)
			.pointAt(stationTop)
			.placeNearTarget()
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.text("When used on a station, maps will add a labeled marker at the location");
		scene.idle(70);

		for (int i = 8; i >= 3; i--) {
			scene.world.showSection(util.select.position(i, 1, 2), Direction.DOWN);
			scene.idle(1);
		}

		scene.world.toggleControls(util.grid.at(5, 3, 2));
		scene.idle(10);
		ElementLink<WorldSectionElement> trainElement3 = scene.world.showIndependentSection(train3, Direction.DOWN);
		scene.world.moveSection(trainElement3, util.vector.of(0, 0, 4), 0);
		scene.idle(15);

		Vec3 target = util.vector.topOf(util.grid.at(5, 3, 6));
		scene.overlay.showControls(new InputWindowElement(target, Pointing.DOWN).rightClick()
			.withWrench(), 75);
		scene.idle(15);

		scene.overlay.showText(70)
			.pointAt(target)
			.placeNearTarget()
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.text("Assembled Trains can be relocated to nearby Tracks using the Wrench");
		scene.idle(60);

		scene.overlay
			.showControls(new InputWindowElement(util.vector.topOf(util.grid.at(6, 0, 2)), Pointing.DOWN).rightClick()
				.withWrench(), 15);
		scene.idle(15);
		scene.world.moveSection(trainElement3, util.vector.of(0, 0, -4), 5);

	}

	public static void autoSchedule(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("auto_schedule", "Stations & Scheduling");
		scene.configureBasePlate(1, 0, 12);
		scene.scaleSceneView(.65f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();

		for (int i = 13; i >= 0; i--) {
			scene.world.showSection(util.select.position(i, 1, 6), Direction.DOWN);
			scene.idle(1);
		}

		scene.world.toggleControls(util.grid.at(7, 3, 6));

		scene.idle(4);
		Selection redstone = util.select.fromTo(8, 1, 2, 8, 1, 1);
		Selection belt = util.select.fromTo(9, 1, 3, 12, 1, 3);
		Selection largeCog = util.select.position(13, 0, 4);
		Selection cog = util.select.position(12, 1, 4);
		BlockPos stationPos = util.grid.at(8, 1, 3);
		Selection train = util.select.fromTo(9, 2, 5, 5, 3, 7);
		Selection station = util.select.position(stationPos);

		scene.world.showSection(station, Direction.DOWN);
		scene.idle(10);

		ElementLink<WorldSectionElement> trainElement = scene.world.showIndependentSection(train, Direction.DOWN);
		scene.world.moveSection(trainElement, util.vector.of(-4, 0, 0), 0);
		Vec3 target = util.vector.centerOf(2, 3, 6);
		ElementLink<ParrotElement> birb = scene.special.createBirb(target, FacePointOfInterestPose::new);
		scene.idle(10);

		scene.overlay.showText(90)
			.pointAt(target)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Schedules can be used to provide drivers with a destination");
		scene.idle(70);
		scene.overlay.showControls(
			new InputWindowElement(target.add(0, 0.5, 0), Pointing.DOWN).withItem(AllItems.SCHEDULE.asStack()), 40);
		scene.idle(6);
		scene.special.movePointOfInterest(util.grid.at(9, 4, 6));

		Vec3 marker = util.vector.topOf(8, 0, 6)
			.add(0, 3 / 16f, 0);
		AABB bb = new AABB(marker, marker);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb, 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb.inflate(.45f, 0, .45f), 40);
		scene.idle(15);

		AABB bb2 = new AABB(marker, marker).move(-.45, 0, 0);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.move(-4, 0, 0), 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.expandTowards(-4, 0, 0), 20);
		scene.idle(15);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.inflate(.45f, 0, .45f), 25);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb2, bb2.expandTowards(-4, 0, 0), 25);
		scene.idle(20);

		scene.world.showSection(redstone, Direction.SOUTH);

		scene.world.moveSection(trainElement, util.vector.of(4, 0, 0), 20);
		scene.world.animateBogey(util.grid.at(7, 2, 6), -4f, 20);
		scene.special.moveParrot(birb, util.vector.of(4, 0, 0), 20);
		scene.idle(20);

		scene.world.animateTrainStation(stationPos, true);
		scene.world.toggleRedstonePower(redstone);
		scene.effects.indicateRedstone(stationPos);
		scene.idle(25);
		scene.overlay.showText(80)
			.pointAt(util.vector.topOf(stationPos.north())
				.subtract(0, 14 / 16f, 0))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Comparators will receive a signal whenever a Train is present");
		scene.idle(90);

		scene.world.hideSection(redstone, Direction.NORTH);
		scene.world.animateTrainStation(stationPos, false);
		scene.world.moveSection(trainElement, util.vector.of(0, 1, 0), 5);
		scene.special.moveParrot(birb, util.vector.of(0, 2, 0), 5);
		scene.idle(7);
		scene.world.moveSection(trainElement, util.vector.of(4, 0, 0), 10);
		scene.world.rotateSection(trainElement, 0, 180, 0, 10);
		scene.special.moveParrot(birb, util.vector.of(6, 0, 0), 10);
		scene.idle(12);
		scene.world.moveSection(trainElement, util.vector.of(0, -1, 0), 5);
		scene.special.moveParrot(birb, util.vector.of(0, -2, 0), 5);
		scene.idle(25);

		target = target.add(10, 0, 0);
		scene.overlay.showText(90)
			.pointAt(marker.add(-.45, 0, 0))
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Mind that a Station can only be approached from the indicated direction");
		scene.idle(70);
		scene.overlay.showControls(
			new InputWindowElement(target.add(0, 0.5, 0), Pointing.DOWN).withItem(AllItems.SCHEDULE.asStack()), 40);
		scene.idle(6);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb, 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb.inflate(.45f, 0, .45f), 40);
		scene.idle(15);

		bb2 = new AABB(marker, marker).move(.45, 0, 0);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.move(4, 0, 0), 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.expandTowards(4, 0, 0), 20);
		scene.idle(15);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb, bb.inflate(.45f, 0, .45f), 45);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2.expandTowards(4, 0, 0), 45);
		scene.idle(20);

		scene.special.movePointOfInterest(util.grid.at(11, 2, 5));
		scene.idle(25);

		scene.world.hideIndependentSection(trainElement, Direction.EAST);
		scene.special.hideElement(birb, Direction.EAST);
		scene.idle(20);

		scene.world.multiplyKineticSpeed(util.select.everywhere(), .75f);

		scene.world.showSection(largeCog, Direction.UP);
		scene.world.showSection(cog, Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(belt, Direction.SOUTH);
		scene.idle(15);

		scene.overlay.showText(70)
			.pointAt(util.vector.topOf(stationPos)
				.add(-.5, 0, 0))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Stations can also be used to assign new Schedules automatically");
		scene.idle(40);

		trainElement = scene.world.showIndependentSection(train, Direction.DOWN);
		scene.world.moveSection(trainElement, util.vector.of(-4, 0, 0), 0);
		target = util.vector.centerOf(2, 3, 6);
		birb = scene.special.createBirb(target, FacePointOfInterestPose::new);
		scene.idle(10);
		scene.world.moveSection(trainElement, util.vector.of(4, 0, 0), 20);
		scene.world.animateBogey(util.grid.at(7, 2, 6), -4f, 20);
		scene.special.moveParrot(birb, util.vector.of(4, 0, 0), 20);
		scene.idle(10);

		scene.world.createItemOnBelt(util.grid.at(11, 1, 3), Direction.DOWN, AllItems.SCHEDULE.asStack());
		scene.idle(10);
		scene.world.animateTrainStation(stationPos, true);
		scene.idle(10);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.topOf(stationPos), Pointing.DOWN).withItem(AllItems.SCHEDULE.asStack()),
			20);
		scene.idle(30);
		scene.effects.indicateSuccess(stationPos);
		scene.idle(10);
		target = util.vector.centerOf(6, 3, 6);
		scene.overlay.showLine(PonderPalette.GREEN, util.vector.topOf(stationPos)
			.add(0, -.125, .45), target.add(0, 0, -.5), 40);
		scene.idle(10);
		scene.effects.indicateSuccess(util.grid.at(6, 3, 6));
		scene.special.conductorBirb(birb, true);
		scene.special.movePointOfInterest(util.grid.at(18, 4, 6));
		scene.idle(25);

		scene.overlay.showText(80)
			.pointAt(target)
			.placeNearTarget()
			.attachKeyFrame()
			.text("The Schedule placed on a station will automatically copy itself to present trains");
		scene.idle(90);

		scene.world.moveSection(trainElement, util.vector.of(8, 0, 0), 30);
		scene.special.moveParrot(birb, util.vector.of(8, 0, 0), 30);
		scene.world.animateBogey(util.grid.at(7, 2, 6), -8f, 30);
		scene.world.animateTrainStation(stationPos, false);
		scene.idle(10);
		scene.world.hideIndependentSection(trainElement, null);
		scene.special.hideElement(birb, null);
		scene.idle(25);

		scene.overlay.showText(100)
			.pointAt(util.vector.topOf(stationPos)
				.add(-.5, 0, 0))
			.placeNearTarget()
			.colored(PonderPalette.BLUE)
			.text("As opposed to manual scheduling, drivers will not take the item with them");
		scene.idle(60);
	}

}
