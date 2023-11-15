package com.simibubi.create.infrastructure.ponder.scenes.trains;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
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
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TrackScenes {

	public static void placement(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("track_placement", "Placing Train Tracks");
		scene.configureBasePlate(0, 0, 15);
		scene.scaleSceneView(.5f);
		scene.showBasePlate();
		scene.idle(10);

		ElementLink<WorldSectionElement> bgTrack =
			scene.world().showIndependentSection(util.select().position(11, 4, 9), Direction.DOWN);
		scene.world().moveSection(bgTrack, util.vector().of(0, -2, 0), 0);

		for (int i = 11; i >= 2; i--) {
			scene.world().showSectionAndMerge(util.select().position(i, 3, 9), Direction.DOWN, bgTrack);
			if (i == 5)
				scene.world().showSectionAndMerge(util.select().position(7, 4, 9), Direction.DOWN, bgTrack);
			scene.idle(2);
		}

		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(5, 0, 9))
			.placeNearTarget()
			.text("A new type of rail designed for Train Contraptions");
		scene.idle(50);

		ElementLink<WorldSectionElement> fgTrack =
			scene.world().showIndependentSection(util.select().position(3, 3, 5), Direction.DOWN);
		scene.world().moveSection(fgTrack, util.vector().of(0, -2, 0), 0);
		scene.idle(20);

		Vec3 startTrack = util.vector().topOf(3, 0, 5);
		scene.overlay().showText(70)
			.pointAt(startTrack)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.text("To place rows of track in bulk, click on an existing track");
		scene.idle(30);

		ItemStack trackStack = AllBlocks.TRACK.asStack();

		scene.overlay().showControls(startTrack, Pointing.DOWN, 40)
				.rightClick()
				.withItem(trackStack);

		scene.overlay().showControls(startTrack, Pointing.DOWN, 40).rightClick()
			.withItem(trackStack);
		scene.idle(6);
		AABB bb = new AABB(util.grid().at(3, 1, 5)).contract(0, .75f, 0)
			.inflate(0, 0, .85f);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, startTrack, bb, 32);
		scene.idle(45);

		scene.overlay().showControls(startTrack.add(9, 0, 0), Pointing.DOWN, 40).rightClick()
			.withItem(trackStack);
		scene.idle(6);
		scene.overlay().showText(40)
			.pointAt(util.vector().topOf(12, 0, 5))
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.text("Then place or select a second track");
		scene.idle(20);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, startTrack, bb.expandTowards(9, 0, 0), 30);

		scene.world().showSectionAndMerge(util.select().fromTo(12, 3, 5, 4, 3, 5), Direction.WEST, fgTrack);
		scene.idle(55);

		scene.world().hideIndependentSection(bgTrack, Direction.UP);
		scene.idle(7);
		scene.world().hideIndependentSection(fgTrack, Direction.UP);
		scene.idle(25);

		scene.world().showSection(util.select().position(8, 1, 2), Direction.SOUTH);
		scene.idle(10);
		scene.addKeyframe();

		scene.overlay().showControls(util.vector().topOf(8, 0, 2), Pointing.DOWN, 15).rightClick()
			.withItem(trackStack);
		scene.idle(15);
		scene.overlay().showControls(util.vector().topOf(2, 0, 8), Pointing.DOWN, 15).rightClick()
			.withItem(trackStack);
		scene.idle(7);
		scene.world().showSection(util.select().position(2, 1, 8), Direction.DOWN);
		scene.idle(25);

		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(7, 0, 7))
			.placeNearTarget()
			.text("Tracks can also be placed as turns or slopes");
		scene.idle(40);

		scene.world().showSection(util.select().position(12, 1, 2), Direction.SOUTH);
		scene.idle(10);

		scene.overlay().showControls(util.vector().topOf(12, 0, 2), Pointing.DOWN, 10).rightClick()
			.withItem(trackStack);
		scene.idle(15);
		scene.overlay().showControls(util.vector().topOf(2, 0, 12), Pointing.DOWN, 10).rightClick()
			.withItem(trackStack);
		scene.idle(7);
		scene.world().showSection(util.select().fromTo(12, 1, 3, 12, 1, 5), Direction.DOWN);
		scene.idle(3);
		scene.world().showSection(util.select().fromTo(12, 1, 6, 6, 1, 12), Direction.DOWN);
		scene.idle(3);
		scene.world().showSection(util.select().fromTo(5, 1, 12, 2, 1, 12), Direction.DOWN);
		scene.idle(25);

		scene.overlay().showText(70)
			.pointAt(util.vector().topOf(11, 0, 11))
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.placeNearTarget()
			.text("When connecting, tracks will try to make each turn equally sized");
		scene.idle(70);

		scene.world().hideSection(util.select().fromTo(12, 1, 2, 12, 1, 5), Direction.NORTH);
		scene.world().hideSection(util.select().fromTo(5, 1, 12, 2, 1, 12), Direction.WEST);

		bb = new AABB(util.grid().at(5, 1, 5)).contract(0, .75f, 0)
			.inflate(3, 0, 3)
			.expandTowards(.85f, 0, .85f);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, startTrack, bb, 32);
		scene.idle(20);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, startTrack, bb.move(4, 0, 4), 32);
		scene.idle(30);

		scene.world().hideSection(util.select().fromTo(12, 1, 6, 6, 1, 12), Direction.UP);
		scene.idle(5);
		scene.world().showSection(util.select().position(12, 1, 2), Direction.SOUTH);
		scene.idle(20);

		scene.overlay().showControls(util.vector().topOf(12, 0, 2), Pointing.DOWN, 10).rightClick()
			.withItem(trackStack);
		scene.idle(10);

		scene.overlay().showControls(util.vector().topOf(2, 0, 12), Pointing.DOWN, 60).rightClick()
			.withItem(trackStack)
			.whileCTRL();
		scene.idle(10);

		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(2, 0, 12))
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.placeNearTarget()
			.text("Holding the sprint key while connecting...");
		scene.idle(50);

		ElementLink<WorldSectionElement> longBend =
			scene.world().showIndependentSection(util.select().position(2, 2, 12), Direction.DOWN);
		scene.world().moveSection(longBend, util.vector().of(0, -1, 0), 0);
		scene.idle(30);

		scene.overlay().showText(60)
			.pointAt(util.vector().centerOf(9, 1, 9))
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.text("...will create the longest fitting bend instead");
		scene.idle(70);

		scene.world().hideIndependentSection(longBend, Direction.UP);
		scene.world().hideSection(util.select().position(12, 1, 2), Direction.UP);
		scene.idle(5);
		scene.world().hideSection(util.select().fromTo(8, 1, 2, 2, 1, 8), Direction.UP);
		scene.idle(25);

		ElementLink<WorldSectionElement> slopeStart =
			scene.world().showIndependentSection(util.select().fromTo(12, 6, 2, 12, 9, 12), Direction.DOWN);
		scene.world().moveSection(slopeStart, util.vector().of(0, -5, 0), 0);
		scene.idle(10);
		scene.world().showSectionAndMerge(util.select().fromTo(2, 6, 2, 2, 7, 4), Direction.DOWN, slopeStart);
		scene.world().showSectionAndMerge(util.select().fromTo(2, 6, 6, 2, 9, 8), Direction.DOWN, slopeStart);
		scene.world().showSectionAndMerge(util.select().fromTo(2, 6, 10, 2, 11, 12), Direction.DOWN, slopeStart);
		scene.idle(20);

		scene.overlay().showControls(util.vector().topOf(12, 3, 11), Pointing.LEFT, 30).withItem(trackStack);
		scene.idle(4);
		ItemStack smoothStone = new ItemStack(Blocks.SMOOTH_STONE);
		scene.overlay().showControls(util.vector().topOf(12, 3, 11), Pointing.RIGHT, 26).withItem(smoothStone);
		scene.idle(30);

		scene.overlay().showControls(util.vector().topOf(2, 6, 11), Pointing.LEFT, 30).withItem(trackStack);
		scene.idle(4);
		scene.overlay().showControls(util.vector().topOf(2, 6, 11), Pointing.RIGHT, 26).withItem(smoothStone);
		scene.idle(10);

		scene.world().showSectionAndMerge(util.select().position(2, 12, 11), Direction.DOWN, slopeStart);
		scene.idle(2);
		scene.world().showSectionAndMerge(util.select().fromTo(11, 8, 10, 3, 11, 12), Direction.UP, slopeStart);
		scene.idle(20);

		scene.overlay().showText(100)
			.pointAt(util.vector().blockSurface(util.grid().at(9, 3, 10), Direction.NORTH))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Materials in the off-hand will be paved under tracks automatically");
		scene.idle(80);

		scene.overlay().showControls(util.vector().topOf(12, 2, 7), Pointing.LEFT, 30).withItem(trackStack);
		scene.idle(4);
		smoothStone = new ItemStack(Blocks.SMOOTH_STONE_SLAB);
		scene.overlay().showControls(util.vector().topOf(12, 2, 7), Pointing.RIGHT, 26).withItem(smoothStone);
		scene.idle(30);

		scene.overlay().showControls(util.vector().topOf(2, 4, 7), Pointing.LEFT, 30).withItem(trackStack);
		scene.idle(4);
		scene.overlay().showControls(util.vector().topOf(2, 4, 7), Pointing.RIGHT, 26).withItem(smoothStone);
		scene.idle(10);

		scene.world().showSectionAndMerge(util.select().position(2, 10, 7), Direction.DOWN, slopeStart);
		scene.idle(2);
		scene.world().showSectionAndMerge(util.select().fromTo(11, 7, 6, 3, 11, 8), Direction.UP, slopeStart);
		scene.idle(20);

		scene.overlay().showControls(util.vector().topOf(12, 1, 3), Pointing.LEFT, 30).withItem(trackStack);
		scene.idle(4);
		smoothStone = AllBlocks.METAL_GIRDER.asStack();
		scene.overlay().showControls(util.vector().topOf(12, 1, 3), Pointing.RIGHT, 26).withItem(smoothStone);
		scene.idle(30);

		scene.overlay().showControls(util.vector().topOf(2, 2, 3), Pointing.LEFT, 30).withItem(trackStack);
		scene.idle(4);
		scene.overlay().showControls(util.vector().topOf(2, 2, 3), Pointing.RIGHT, 26).withItem(smoothStone);
		scene.idle(10);

		scene.world().showSectionAndMerge(util.select().position(2, 8, 3), Direction.DOWN, slopeStart);
	}

	public static void portal(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("track_portal", "Tracks and the Nether");
		scene.configureBasePlate(0, 0, 9);
		scene.scaleSceneView(.65f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();
		scene.world().showSection(util.select().fromTo(2, 1, 7, 6, 6, 7), Direction.UP);
		scene.idle(10);

		for (int i = 1; i <= 5; i++) {
			scene.world().showSection(util.select().position(4, 1, i), Direction.DOWN);
			scene.idle(2);
		}

		scene.world().toggleControls(util.grid().at(4, 3, 3));

		scene.idle(15);
		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(4, 0, 6))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Tracks placed up against a nether portal...");
		scene.idle(50);

		scene.world().showSection(util.select().position(4, 1, 6), Direction.DOWN);

		scene.idle(20);
		scene.overlay().showText(70)
			.pointAt(util.vector().topOf(4, 0, 6))
			.placeNearTarget()
			.text("...will attempt to create a paired track on the other side");
		scene.idle(40);

		ElementLink<WorldSectionElement> t1 =
			scene.world().showIndependentSection(util.select().fromTo(5, 2, 1, 3, 3, 2), Direction.DOWN);
		ElementLink<WorldSectionElement> t2 =
			scene.world().showIndependentSection(util.select().fromTo(5, 2, 3, 3, 3, 3), Direction.DOWN);
		ElementLink<WorldSectionElement> t3 =
			scene.world().showIndependentSection(util.select().fromTo(5, 2, 4, 3, 3, 5), Direction.DOWN);

		ElementLink<ParrotElement> birb =
			scene.special().createBirb(util.vector().centerOf(4, 3, 2), ParrotPose.FacePointOfInterestPose::new);
		scene.special().conductorBirb(birb, true);
		scene.special().movePointOfInterest(util.grid().at(4, 4, 10));

		scene.addKeyframe();
		scene.idle(30);

		for (ElementLink<WorldSectionElement> e : List.of(t1, t2, t3))
			scene.world().moveSection(e, util.vector().of(0, 0, 6), 30);
		scene.special().moveParrot(birb, util.vector().of(0, 0, 5.6), 28);
		scene.world().animateBogey(util.grid().at(4, 2, 3), -6, 30);

		for (ElementLink<WorldSectionElement> e : List.of(t3, t2, t1)) {
			scene.idle(2);
			scene.world().hideIndependentSection(e, Direction.SOUTH);
		}

		scene.world().hideSection(util.select().layers(0, 1), Direction.UP);
		scene.rotateCameraY(360);
		scene.idle(15);
		scene.special().movePointOfInterest(util.grid().at(4, 4, 0));
		ElementLink<WorldSectionElement> nether =
			scene.world().showIndependentSection(util.select().layers(7, 1), Direction.UP);
		scene.world().moveSection(nether, util.vector().of(0, -7, 0), 0);
		scene.special().moveParrot(birb, util.vector().of(0, 0, -.1f), 1);
		scene.idle(25);

		ElementLink<WorldSectionElement> s1 =
			scene.world().showIndependentSection(util.select().fromTo(5, 2, 1, 3, 3, 5), null);
		scene.world().rotateSection(s1, 0, 180, 0, 0);
		scene.world().moveSection(s1, util.vector().of(0, 0, 3.5f), 0);
		scene.world().moveSection(s1, util.vector().of(0, 0, -3.5f), 18);
		scene.special().moveParrot(birb, util.vector().of(0, 0, -3.5f), 18);
		scene.world().animateBogey(util.grid().at(4, 2, 3), -3.5f, 18);
		scene.idle(30);

		scene.overlay().showText(70)
			.pointAt(util.vector().topOf(util.grid().at(3, 2, 3)))
			.attachKeyFrame()
			.placeNearTarget()
			.text("Trains on this track are now able to travel across dimensions");
		scene.idle(40);

	}

	public static void chunks(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("track_chunks", "Traversing unloaded Chunks");
		scene.configureBasePlate(0, 0, 9);
		scene.scaleSceneView(.65f);
		scene.setSceneOffsetY(-1);

		scene.world().cycleBlockProperty(util.grid().at(5, 3, 4), BlazeBurnerBlock.HEAT_LEVEL);

		ElementLink<WorldSectionElement> stationElement =
			scene.world().showIndependentSection(util.select().fromTo(0, 0, 0, 8, 0, 8), Direction.UP);
		ElementLink<WorldSectionElement> stationTrackElement =
			scene.world().showIndependentSection(util.select().position(0, 2, 0), Direction.UP);
		scene.world().showSection(util.select().position(0, 3, 0), Direction.DOWN);
		scene.idle(10);

		Selection vPlatform = util.select().fromTo(7, 1, 6, 1, 2, 8);
		Selection vStation = util.select().position(7, 1, 1);
		Selection dPlatform = util.select().fromTo(7, 3, 6, 1, 4, 8);
		Selection dStation = util.select().position(1, 1, 1);
		Selection train = util.select().fromTo(6, 2, 3, 2, 3, 5);
		Selection track = util.select().fromTo(7, 1, 4, 37, 1, 4);

		scene.world().animateTrainStation(util.grid().at(7, 1, 1), true);
		scene.world().toggleControls(util.grid().at(4, 3, 4));

		for (int i = 6; i >= 2; i--) {
			scene.world().showSectionAndMerge(util.select().position(i, 1, 4), Direction.DOWN, stationTrackElement);
			scene.idle(2);
		}

		scene.world().showSectionAndMerge(vPlatform, Direction.NORTH, stationElement);
		scene.idle(5);
		scene.world().showSectionAndMerge(vStation, Direction.DOWN, stationElement);
		ElementLink<ParrotElement> birb =
			scene.special().createBirb(util.vector().centerOf(2, 2, 7), ParrotPose.FacePointOfInterestPose::new);
		scene.special().movePointOfInterest(util.grid().at(4, 3, 4));
		scene.idle(5);
		ElementLink<WorldSectionElement> trainElement = scene.world().showIndependentSection(train, Direction.DOWN);

		scene.idle(10);
		ElementLink<WorldSectionElement> trackElement = scene.world().showIndependentSection(track, Direction.EAST);
		scene.world().moveSection(trackElement, util.vector().of(-36, 0, 0), 0);

		scene.idle(15);
		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(1, 0, 4))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Tracks stay functional outside of loaded chunks");
		scene.idle(60);

		scene.idle(30);
		scene.world().animateTrainStation(util.grid().at(7, 1, 1), false);
		scene.world().moveSection(trackElement, util.vector().of(12, 0, 0), 120);
		scene.world().moveSection(stationElement, util.vector().of(12, 0, 0), 120);
		scene.world().moveSection(stationTrackElement, util.vector().of(12, 0, 0), 120);
		scene.world().animateBogey(util.grid().at(4, 2, 4), 12f, 120);
		scene.special().moveParrot(birb, util.vector().of(12, 0, 0), 120);
		scene.idle(15);
		scene.world().hideIndependentSection(stationElement, null);
		scene.special().hideElement(birb, null);
		scene.idle(10);
		scene.world().hideIndependentSection(trainElement, null);
		scene.idle(5);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, trainElement,
												new AABB(util.grid().at(4, 2, 4)).inflate(1, .75f, .5f), 280);

		scene.idle(15);
		scene.overlay().showText(60)
			.pointAt(util.vector().blockSurface(util.grid().at(3, 2, 4), Direction.WEST))
			.placeNearTarget()
			.colored(PonderPalette.BLUE)
			.attachKeyFrame()
			.text("Trains will travel through inactive sections of the world without issue");
		scene.idle(70);

		scene.overlay().showText(60)
			.pointAt(util.vector().blockSurface(util.grid().at(3, 2, 4), Direction.WEST))
			.placeNearTarget()
			.colored(PonderPalette.BLUE)
			.attachKeyFrame()
			.text("They will still stop at stations or red signals");
		scene.idle(50);

		scene.world().moveSection(trackElement, util.vector().of(19, 0, 0), 190);
		scene.world().moveSection(stationTrackElement, util.vector().of(19, 0, 0), 190);
		scene.world().animateBogey(util.grid().at(4, 2, 4), 19f, 190);

		scene.idle(30);
		scene.overlay().showText(90)
			.pointAt(util.vector().blockSurface(util.grid().at(3, 2, 4), Direction.WEST))
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.text("However, Drills and other on-board machines will not operate");
		scene.idle(80);

		stationElement = scene.world().showIndependentSection(util.select().fromTo(0, 0, 0, 8, 0, 8)
			.add(dStation), null);
		ElementLink<WorldSectionElement> dPlatformElement =
			scene.world().showIndependentSection(dPlatform, null);
		birb = scene.special().createBirb(util.vector().centerOf(-2, 2, 7), ParrotPose.FacePointOfInterestPose::new);
		scene.world().moveSection(dPlatformElement, util.vector().of(-8, -2, 0), 0);
		scene.world().moveSection(stationElement, util.vector().of(-8, 0, 0), 0);
		scene.world().moveSection(stationElement, util.vector().of(8, 0, 0), 80);
		scene.world().moveSection(dPlatformElement, util.vector().of(8, 0, 0), 80);
		scene.special().moveParrot(birb, util.vector().of(8, 0, 0), 80);

		scene.idle(30);
		trainElement = scene.world().showIndependentSection(train, null);
		scene.idle(20);

		scene.overlay().showText(90)
			.pointAt(util.vector().blockSurface(util.grid().at(3, 2, 4), Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Once near a Player, the train will re-appear");
		scene.idle(30);
		scene.world().animateTrainStation(util.grid().at(1, 1, 1), true);
		scene.idle(30);
	}

}
