package com.simibubi.create.infrastructure.ponder.scenes.trains;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.trains.signal.SignalBlock;
import com.simibubi.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TrainSignalScenes {

	public static void placement(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("train_signal_placement", "Placing Train Signals");
		scene.configureBasePlate(1, 0, 12);
		scene.scaleSceneView(.65f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();

		for (int i = 13; i >= 0; i--) {
			scene.world().showSection(util.select().position(i, 1, 6), Direction.DOWN);
			scene.idle(1);
		}

		scene.idle(10);

		BlockPos stationPos = util.grid().at(11, 1, 3);
		Selection station = util.select().position(stationPos);
		BlockPos signalPos = util.grid().at(8, 1, 3);
		Selection firstSignal = util.select().position(signalPos);
		Selection fakeSignal = util.select().position(7, 1, 3);
		Selection secondSignal = util.select().position(8, 1, 9);
		Selection thirdSignal = util.select().fromTo(9, 1, 9, 9, 4, 9);
		Selection firstNixie = util.select().position(8, 2, 3);
		Selection secondNixie = util.select().position(8, 2, 9);
		Selection thirdNixie = util.select().position(9, 4, 8);
		Selection train = util.select().fromTo(5, 2, 5, 1, 3, 7);

		scene.world().toggleControls(util.grid().at(3, 3, 6));

		Vec3 marker = util.vector().topOf(8, 0, 6)
			.add(0, 3 / 16f, 0);

		AABB bb = new AABB(marker, marker);
		AABB bb3 = bb.move(3, 0, 0);

		scene.overlay().showControls(marker, Pointing.DOWN, 40).rightClick()
			.withItem(AllBlocks.TRACK_SIGNAL.asStack());
		scene.idle(6);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.move(0, -1 / 16f, 0), 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.move(0, -1 / 16f, 0)
			.inflate(.45f, 1 / 16f, .45f), 100);
		scene.idle(10);

		scene.overlay().showText(70)
			.pointAt(marker)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.text("Select a Train Track then place the Signal nearby");
		scene.idle(60);

		ElementLink<WorldSectionElement> signalElement = scene.world().showIndependentSection(fakeSignal, Direction.DOWN);
		scene.world().moveSection(signalElement, util.vector().of(1, 0, 0), 0);
		scene.idle(15);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, new AABB(signalPos), 20);
		scene.idle(25);

		scene.overlay().showText(80)
			.pointAt(util.vector().blockSurface(signalPos, Direction.WEST))
			.attachKeyFrame()
			.placeNearTarget()
			.text("Signals control the flow of Trains not driven by players");
		scene.idle(70);

		ElementLink<WorldSectionElement> trainElement = scene.world().showIndependentSection(train, Direction.DOWN);
		Vec3 birbVec = util.vector().centerOf(util.grid().at(2, 3, 6));
		ElementLink<ParrotElement> birb = scene.special().createBirb(birbVec, ParrotPose.FacePointOfInterestPose::new);

		scene.idle(10);
		scene.world().showSection(station, Direction.DOWN);

		scene.idle(10);

		scene.overlay().showText(80)
			.pointAt(marker.add(-.45, 0, 0))
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.text("Scheduled Trains will never cross signals in the opposite direction");
		scene.idle(90);

		scene.overlay().showControls(birbVec.add(0, 0.5, 0), Pointing.DOWN, 40).withItem(AllItems.SCHEDULE.asStack());
		scene.idle(6);
		scene.special().movePointOfInterest(util.grid().at(19, 4, 6));

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb3, bb3, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb3, bb3.inflate(.45f, 0, .45f), 40);
		scene.idle(15);

		AABB bb2 = new AABB(marker, marker).move(-.45, 0, 0);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.move(-4, 0, 0), 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.expandTowards(-4, 0, 0), 20);
		scene.idle(15);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb, bb, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb, bb.inflate(.45f, 0, .45f), 40);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb3, bb3.inflate(.45f, 0, .45f), 45);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2.expandTowards(-4, 0, 0), 45);
		scene.idle(20);
		scene.special().movePointOfInterest(util.grid().at(5, 1, 4));
		scene.idle(20);

		scene.overlay().showControls(marker, Pointing.DOWN, 40).rightClick()
			.withItem(AllBlocks.TRACK_SIGNAL.asStack());
		scene.idle(6);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.move(0, -1 / 16f, 0), 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.move(0, -1 / 16f, 0)
			.inflate(.45f, 1 / 16f, .45f), 70);
		scene.idle(30);
		scene.world().showSection(secondSignal, Direction.DOWN);
		scene.idle(10);
		scene.world().moveSection(signalElement, util.vector().of(0, -1000, 0), 0);
		scene.world().showIndependentSectionImmediately(firstSignal);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, new AABB(util.grid().at(8, 1, 9)), 40);

		scene.overlay().showText(80)
			.pointAt(util.vector().blockSurface(util.grid().at(8, 1, 9), Direction.WEST))
			.attachKeyFrame()
			.placeNearTarget()
			.text("...unless a second signal is added facing the opposite way.");
		scene.idle(90);
		scene.world().hideIndependentSection(signalElement, null);

		scene.overlay().showControls(birbVec.add(0, 0.5, 0), Pointing.DOWN, 40).withItem(AllItems.SCHEDULE.asStack());
		scene.idle(6);
		scene.special().movePointOfInterest(util.grid().at(19, 4, 6));

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb3, bb3, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb3, bb3.inflate(.45f, 0, .45f), 40);
		scene.idle(15);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.move(-4, 0, 0), 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.expandTowards(-4, 0, 0), 40);
		scene.idle(5);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb.inflate(.45f, 0, .45f), 30);
		scene.idle(5);

		AABB bb4 = new AABB(marker, marker).move(.45, 0, 0);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb4, bb4, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb4, bb4.expandTowards(2, 0, 0), 20);
		scene.idle(15);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb4, bb4.expandTowards(2, 0, 0), 25);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb3, bb3.inflate(.45f, 0, .45f), 25);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb2, bb2.expandTowards(-4, 0, 0), 25);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.inflate(.45f, 0, .45f), 25);
		scene.idle(20);

		scene.world().moveSection(trainElement, util.vector().of(7, 0, 0), 25);
		scene.world().animateBogey(util.grid().at(3, 2, 6), -7f, 25);
		scene.special().moveParrot(birb, util.vector().of(7, 0, 0), 25);
		scene.idle(25);

		scene.world().animateTrainStation(stationPos, true);
		scene.world().changeSignalState(util.grid().at(8, 1, 9), SignalState.RED);
		scene.world().changeSignalState(util.grid().at(9, 4, 9), SignalState.RED);
		scene.idle(25);

		scene.world().showSection(thirdSignal, Direction.DOWN);
		scene.rotateCameraY(-90);
		scene.special().movePointOfInterest(util.grid().at(8, 2, 9));
		scene.idle(5);
		scene.world().showSection(firstNixie, Direction.DOWN);
		scene.idle(3);
		scene.world().showSection(secondNixie, Direction.DOWN);
		scene.idle(3);
		scene.world().showSection(thirdNixie, Direction.SOUTH);
		scene.idle(15);

		scene.overlay().showText(100)
			.pointAt(util.vector().blockSurface(util.grid().at(8, 2, 9), Direction.SOUTH))
			.attachKeyFrame()
			.placeNearTarget()
			.text("Nixie tubes can be attached to make a signal's lights more visible");
		scene.idle(70);
	}

	public static void signaling(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("train_signal_signaling", "Collision Prevention with Signals");
		scene.configureBasePlate(1, 0, 15);
		scene.scaleSceneView(.5f);
		scene.showBasePlate();
		scene.rotateCameraY(55);

		for (int i = 16; i >= 0; i--) {
			scene.world().showSection(util.select().position(i, 1, 7), Direction.DOWN);
			scene.world().showSection(util.select().position(i, 1, 15 - i), Direction.DOWN);
			scene.idle(1);
		}

		scene.world().toggleControls(util.grid().at(13, 3, 7));
		scene.world().toggleControls(util.grid().at(13, 3, 1));
		scene.world().toggleControls(util.grid().at(13, 3, 4));

		Selection train1 = util.select().fromTo(11, 2, 6, 15, 3, 8);
		Selection train2a = util.select().fromTo(15, 2, 3, 11, 3, 5);
		Selection train2b = util.select().fromTo(19, 2, 3, 16, 3, 5);
		Selection train3 = util.select().fromTo(11, 2, 0, 15, 3, 2);
		BlockPos s1Pos = util.grid().at(11, 3, 9);
		Selection s1 = util.select().fromTo(11, 1, 9, 11, 4, 9);
		BlockPos s2Pos = util.grid().at(5, 1, 5);
		Selection s2 = util.select().fromTo(5, 1, 5, 5, 2, 5);
		BlockPos s3Pos = util.grid().at(9, 1, 2);
		Selection s3 = util.select().fromTo(9, 1, 2, 10, 1, 2);
		BlockPos s4Pos = util.grid().at(7, 1, 12);
		Selection s4 = util.select().fromTo(7, 1, 12, 6, 1, 12);

		float pY = 3 / 16f;
		Vec3 m1 = util.vector().topOf(11, 0, 7)
			.add(0, pY, 0);
		Vec3 m2 = util.vector().topOf(5, 0, 7)
			.add(0, pY, 0);
		Vec3 m3 = util.vector().topOf(12, 0, 3)
			.add(0, pY, 0);
		Vec3 m4 = util.vector().topOf(4, 0, 11)
			.add(0, pY, 0);

		scene.idle(10);

		scene.world().showSection(s1, Direction.DOWN);
		scene.idle(8);

		Vec3 x1 = util.vector().of(17, 1 + pY, 7.5);
		Vec3 x2 = util.vector().of(0, 1 + pY, 7.5);
		Vec3 xz1 = util.vector().of(1, 1 + pY, 15);
		Vec3 xz2 = util.vector().of(16, 1 + pY, 0);

		scene.overlay().showBigLine(PonderPalette.OUTPUT, x1, m1.add(.45, 0, 0), 100);
		scene.overlay().showBigLine(PonderPalette.RED, x2, m1.add(-.45, 0, 0), 100);
		scene.overlay().showBigLine(PonderPalette.RED, xz1, xz2, 100);
		scene.idle(35);

		scene.overlay().showText(60)
			.pointAt(m1.add(-.45, 0, 0))
			.attachKeyFrame()
			.placeNearTarget()
			.text("Train Signals divide a track into segments");
		scene.idle(50);

		scene.world().showSection(s2, Direction.DOWN);
		scene.idle(8);

		scene.overlay().showBigLine(PonderPalette.OUTPUT, x1, m1.add(.45, 0, 0), 80);
		scene.overlay().showBigLine(PonderPalette.BLUE, x2, m2.add(-.45, 0, 0), 75);
		scene.overlay().showBigLine(PonderPalette.RED, m2.add(.45, 0, 0), m1.add(-.45, 0, 0), 75);
		scene.overlay().showBigLine(PonderPalette.RED, xz1, xz2, 75);
		scene.idle(25);

		scene.world().showSection(s3, Direction.DOWN);
		scene.world().showSection(s4, Direction.DOWN);
		scene.idle(8);

		scene.overlay().showBigLine(PonderPalette.OUTPUT, x1, m1.add(.45, 0, 0), 50);
		scene.overlay().showBigLine(PonderPalette.BLUE, m2.add(-.45, 0, 0), x2, 50);
		scene.overlay().showBigLine(PonderPalette.FAST, xz1, m4.add(-.45, 0, .45), 50);
		scene.overlay().showBigLine(PonderPalette.RED, m2.add(.45, 0, 0), m1.add(-.45, 0, 0), 50);
		scene.overlay().showBigLine(PonderPalette.RED, m4.add(.45, 0, -.45), m3.add(-.45, 0, .45), 50);
		scene.overlay().showBigLine(PonderPalette.GREEN, m3.add(.45, 0, -.45), xz2, 50);
		scene.idle(40);

		ElementLink<WorldSectionElement> trainElement = scene.world().showIndependentSection(train1, null);
		ElementLink<ParrotElement> birb1 =
			scene.special().createBirb(util.vector().centerOf(18, 3, 7), ParrotPose.FacePointOfInterestPose::new);
		scene.world().moveSection(trainElement, util.vector().of(4, 0, 0), 0);
		scene.world().moveSection(trainElement, util.vector().of(-9, 0, 0), 45);
		scene.world().animateBogey(util.grid().at(13, 2, 7), 9f, 45);
		scene.special().moveParrot(birb1, util.vector().of(-9, 0, 0), 45);
		scene.idle(20);

		scene.world().changeSignalState(s1Pos, SignalState.RED);
		scene.effects().indicateRedstone(s1Pos.above());
		scene.world().changeSignalState(s3Pos, SignalState.RED);
		scene.effects().indicateRedstone(s3Pos.east());
		scene.overlay().showBigLine(PonderPalette.RED, m2.add(.45, 0, 0), m1.add(-.45, 0, 0), 220);
		scene.overlay().showBigLine(PonderPalette.RED, m4.add(.45, 0, -.45), m3.add(-.45, 0, .45), 220);
		scene.idle(25);

		scene.overlay().showText(80)
			.pointAt(util.vector().blockSurface(s1Pos.above(), Direction.WEST))
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.text("If a Segment is occupied, no other Trains will be allowed entry");
		scene.idle(50);

		ElementLink<WorldSectionElement> trainElement2 = scene.world().showIndependentSection(train3, null);
		ElementLink<ParrotElement> birb2 =
			scene.special().createBirb(util.vector().centerOf(18, 3, 7), ParrotPose.FacePointOfInterestPose::new);
		scene.world().moveSection(trainElement2, util.vector().of(4, 0, 6), 0);
		scene.world().moveSection(trainElement2, util.vector().of(-4.5, 0, 0), 35);
		scene.world().animateBogey(util.grid().at(13, 2, 1), 4.5f, 35);
		scene.special().moveParrot(birb2, util.vector().of(-4.5, 0, 0), 35);
		scene.idle(40);
		scene.special().movePointOfInterest(s1Pos.above(2));
		scene.idle(10);

		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(util.grid().at(9, 0, 6)))
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.text("Thus, each Segment will contain only one Train at a time");
		scene.idle(90);

		scene.world().hideIndependentSection(trainElement, Direction.UP);
		scene.special().hideElement(birb1, Direction.UP);
		scene.idle(3);
		scene.world().hideIndependentSection(trainElement2, Direction.UP);
		scene.special().hideElement(birb2, Direction.UP);
		scene.idle(3);
		scene.world().changeSignalState(s1Pos, SignalState.GREEN);
		scene.world().changeSignalState(s3Pos, SignalState.GREEN);
		scene.idle(20);

		scene.overlay().showControls(util.vector().blockSurface(s1Pos, Direction.EAST), Pointing.RIGHT, 80).rightClick()
				.withItem(AllItems.WRENCH.asStack());
		scene.idle(6);
		scene.world().cycleBlockProperty(s1Pos, SignalBlock.TYPE);
		scene.idle(15);

		scene.overlay().showText(60)
			.pointAt(util.vector().blockSurface(s1Pos, Direction.WEST))
			.attachKeyFrame()
			.placeNearTarget()
			.text("A second Signal mode is available via the Wrench");
		scene.idle(70);

		AABB bb = new AABB(m1, m1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb.inflate(.45f, 0, .45f), 140);
		scene.idle(10);

		AABB bb2 = bb.move(-.45, 0, 0);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.expandTowards(-5, 0, 0), 130);
		scene.idle(10);

		AABB bb3 = bb.move(-6, 0, 0);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb3, bb3, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb3, bb3.inflate(.45f, 0, .45f), 120);
		scene.idle(10);

		scene.overlay().showText(60)
			.pointAt(util.vector().blockSurface(s2Pos, Direction.WEST))
			.placeNearTarget()
			.colored(PonderPalette.BLUE)
			.text("Segments of a brass signal usually lead into standard signals");
		scene.idle(70);

		scene.overlay().showText(60)
			.pointAt(util.vector().blockSurface(s1Pos, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("This special Signal can stop trains under a second condition");
		scene.idle(60);

		trainElement = scene.world().showIndependentSection(train1, Direction.DOWN);
		scene.world().moveSection(trainElement, util.vector().of(-10.5, 0, 0), 0);
		birb1 = scene.special().createBirb(util.vector().centerOf(3, 3, 7)
			.add(.5, 0, 0), ParrotPose.DancePose::new);
		scene.idle(10);
		scene.world().changeSignalState(s2Pos, SignalState.RED);
		scene.effects().indicateRedstone(s2Pos.above());
		scene.overlay().showBigLine(PonderPalette.RED, m2.add(-.45, 0, 0), x2, 220);
		scene.idle(15);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb.inflate(.45f, 0, .45f), 140);
		scene.idle(10);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.expandTowards(-5, 0, 0), 130);
		scene.idle(10);

		trainElement2 = scene.world().showIndependentSection(train3, null);
		birb2 = scene.special().createBirb(util.vector().centerOf(18, 3, 7), ParrotPose.FacePointOfInterestPose::new);

		scene.world().moveSection(trainElement2, util.vector().of(4, 0, 6), 0);
		scene.world().moveSection(trainElement2, util.vector().of(-4.5, 0, 0), 35);
		scene.world().animateBogey(util.grid().at(13, 2, 1), 4.5f, 35);
		scene.special().moveParrot(birb2, util.vector().of(-4.5, 0, 0), 35);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb, bb.inflate(.45f, 0, .45f), 140);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2.expandTowards(-5, 0, 0), 130);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb3, bb3, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb3, bb3.inflate(.45f, 0, .45f), 120);
		scene.idle(5);
		scene.world().changeSignalState(s1Pos, SignalState.RED);
		scene.effects().indicateRedstone(s1Pos.above());
		scene.idle(15);

		scene.overlay().showText(50)
			.pointAt(util.vector().blockSurface(s1Pos, Direction.WEST))
			.placeNearTarget()
			.text("It will stop Trains, which, upon entering...");
		scene.idle(50);

		AABB trainBB = new AABB(util.grid().at(13, 2, 7)).inflate(1, 1, .25f);
		for (int i = 1; i < 14; i++) {
			scene.idle(2);
			scene.overlay().chaseBoundingBoxOutline(i == 13 ? PonderPalette.RED : PonderPalette.OUTPUT, trainBB,
													trainBB.move(-i * .5, 0, 0), i == 13 ? 100 : 5);
		}

		scene.special().movePointOfInterest(util.grid().at(5, 3, 7));
		scene.idle(20);

		scene.overlay().showText(80)
			.pointAt(util.vector().of(7, 4, 7))
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.text("...would not be able to leave the Segment immediately");
		scene.idle(40);
		scene.idle(50);

		ElementLink<WorldSectionElement> trainElement3a = scene.world().showIndependentSection(train2a, null);
		scene.world().rotateSection(trainElement3a, 0, 45, 0, 0);
		scene.world().moveSection(trainElement3a, util.vector().of(4, 0, -6), 0);
		scene.world().moveSection(trainElement3a, util.vector().of(-20, 0, 20), 40);
		scene.world().animateBogey(util.grid().at(13, 2, 4), -20f, 40);
		ElementLink<ParrotElement> birb3 =
			scene.special().createBirb(util.vector().of(18, 3.5, -2), ParrotPose.FacePointOfInterestPose::new);
		scene.special().moveParrot(birb3, util.vector().of(-20, 0, 20), 40);
		scene.idle(5);

		scene.effects().indicateRedstone(s3Pos.east());
		scene.world().changeSignalState(s3Pos, SignalState.RED);

		ElementLink<WorldSectionElement> trainElement3b = scene.world().showIndependentSection(train2b, null);
		scene.world().rotateSection(trainElement3b, 0, 45, 0, 0);
		scene.world().moveSection(trainElement3b, util.vector().of(0.5, 0, -7), 0);
		scene.world().moveSection(trainElement3b, util.vector().of(-20, 0, 20), 40);
		scene.world().animateBogey(util.grid().at(17, 2, 4), -20f, 40);
		scene.idle(10);

		scene.effects().indicateRedstone(s4Pos.west());
		scene.world().changeSignalState(s4Pos, SignalState.RED);

		scene.idle(5);
		scene.world().changeSignalState(s3Pos, SignalState.GREEN);
		scene.world().hideIndependentSection(trainElement3a, null);
		scene.special().hideElement(birb3, null);
		scene.idle(5);
		scene.world().hideIndependentSection(trainElement3b, null);
		scene.idle(15);
		scene.world().changeSignalState(s4Pos, SignalState.GREEN);
		scene.idle(15);

		scene.overlay().showBigLine(PonderPalette.GREEN, m2.add(.45, 0, 0), m1.add(-.45, 0, 0), 100);
		scene.overlay().showBigLine(PonderPalette.GREEN, m4.add(.45, 0, -.45), m3.add(-.45, 0, .45), 100);
		scene.idle(15);
		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(util.grid().at(9, 0, 6)))
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.text("This helps keeping queued Trains out of a busy Segment");
		scene.idle(60);
	}

	public static void redstone(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("train_signal_redstone", "Signals & Redstone");
		scene.configureBasePlate(0, 0, 9);
		scene.scaleSceneView(.75f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();

		for (int i = 1; i <= 7; i++) {
			scene.world().showSection(util.select().position(6, 1, i), Direction.DOWN);
			scene.idle(2);
		}

		scene.idle(10);

		Selection train = util.select().fromTo(5, 2, 3, 7, 3, 7);
		Selection lever = util.select().fromTo(2, 1, 3, 1, 1, 3);
		Selection comparator = util.select().fromTo(2, 1, 1, 1, 1, 1);
		Selection signal = util.select().fromTo(3, 1, 3, 3, 2, 3);
		BlockPos signalPos = util.grid().at(3, 1, 3);

		scene.world().showSection(signal, Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(lever, Direction.EAST);
		scene.idle(15);

		scene.overlay().showText(80)
			.pointAt(util.vector().blockSurface(util.grid().at(3, 2, 3), Direction.WEST))
			.attachKeyFrame()
			.placeNearTarget()
			.text("Signals can be forced red by a redstone signal");
		scene.idle(40);

		scene.world().toggleRedstonePower(lever);
		scene.effects().indicateRedstone(util.grid().at(1, 1, 3));
		scene.world().changeSignalState(signalPos, SignalState.RED);
		scene.idle(40);

		scene.world().toggleRedstonePower(lever);
		scene.effects().indicateRedstone(util.grid().at(1, 1, 3));
		scene.world().changeSignalState(signalPos, SignalState.GREEN);
		scene.idle(40);

		scene.world().hideSection(lever, Direction.SOUTH);
		scene.idle(15);
		ElementLink<WorldSectionElement> comparatorElement =
			scene.world().showIndependentSection(comparator, Direction.SOUTH);
		scene.world().moveSection(comparatorElement, util.vector().of(0, 0, 2), 0);
		scene.idle(15);

		scene.overlay().showText(80)
			.pointAt(util.vector().blockSurface(util.grid().at(3, 2, 3), Direction.WEST))
			.attachKeyFrame()
			.placeNearTarget()
			.text("Conversely, red signals emit a comparator output");
		scene.idle(40);

		scene.world().toggleControls(util.grid().at(6, 3, 5));
		scene.world().showSection(train, Direction.DOWN);
		scene.special().createBirb(util.vector().centerOf(util.grid().at(6, 3, 4)), ParrotPose.DancePose::new);
		scene.idle(10);
		scene.world().toggleRedstonePower(comparator);
		scene.effects().indicateRedstone(signalPos);
		scene.world().changeSignalState(signalPos, SignalState.RED);
	}

}
