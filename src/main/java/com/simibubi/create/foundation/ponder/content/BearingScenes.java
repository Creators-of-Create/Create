package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.actors.HarvesterTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.SailBlock;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class BearingScenes {

	public static void windmillsAsSource(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("windmill_source", "Generating Rotational Force using Windmill Bearings");
		scene.configureBasePlate(1, 1, 5);
		scene.setSceneOffsetY(-1);
		
		scene.world.showSection(util.select.fromTo(1, 0, 1, 5, 0, 5), Direction.UP);
		scene.world.setBlock(util.grid.at(2, -1, 0), AllBlocks.SAIL.getDefaultState()
			.with(SailBlock.FACING, Direction.NORTH), false);
		scene.idle(5);
		Selection kinetics = util.select.fromTo(3, 1, 1, 4, 1, 4);
		Selection largeCog = util.select.position(3, 2, 2);
		BlockPos windmill = util.grid.at(3, 2, 1);
		scene.world.showSection(kinetics.add(largeCog), Direction.DOWN);
		scene.idle(10);

		scene.world.showSection(util.select.position(windmill), Direction.DOWN);
		scene.idle(10);

		BlockPos anchorPos = windmill.north();
		scene.overlay.showSelectionWithText(util.select.position(anchorPos), 60)
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(windmill, Direction.WEST))
			.placeNearTarget()
			.text("Windmill Bearings attach to the block in front of them");
		scene.idle(50);

		ElementLink<WorldSectionElement> structure =
			scene.world.showIndependentSection(util.select.position(anchorPos), Direction.SOUTH);
		scene.idle(10);
		for (Direction d : Iterate.directions)
			if (d.getAxis() != Axis.Z)
				scene.world.showSectionAndMerge(util.select.fromTo(anchorPos.offset(d, 1), anchorPos.offset(d, 2)),
					d.getOpposite(), structure);
		scene.idle(10);

		scene.world.showSectionAndMerge(util.select.fromTo(anchorPos.up()
			.east(),
			anchorPos.up(3)
				.east()),
			Direction.WEST, structure);
		scene.world.showSectionAndMerge(util.select.fromTo(anchorPos.down()
			.west(),
			anchorPos.down(3)
				.west()),
			Direction.EAST, structure);
		scene.world.showSectionAndMerge(util.select.fromTo(anchorPos.east()
			.down(),
			anchorPos.east(3)
				.down()),
			Direction.UP, structure);
		scene.world.showSectionAndMerge(util.select.fromTo(anchorPos.west()
			.up(),
			anchorPos.west(3)
				.up()),
			Direction.DOWN, structure);

		scene.idle(5);
		for (Direction d : Iterate.directions)
			if (d.getAxis() != Axis.Z)
				scene.effects.superGlue(anchorPos.offset(d, 1), d.getOpposite(), false);
		scene.idle(10);

		scene.overlay.showText(60)
			.pointAt(util.vector.blockSurface(anchorPos, Direction.NORTH))
			.placeNearTarget()
			.text("If enough Sail-like blocks are attached to the block, it can act as a Windmill");
		scene.idle(70);

		scene.rotateCameraY(-90);
		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(windmill), Pointing.DOWN).rightClick(), 60);
		scene.idle(7);
		scene.world.rotateBearing(windmill, 360, 200);
		scene.world.rotateSection(structure, 0, 0, 360, 200);
		scene.world.setKineticSpeed(largeCog, 4);
		scene.world.setKineticSpeed(kinetics, -8);
		scene.effects.rotationDirectionIndicator(windmill.south());
		BlockPos gaugePos = util.grid.at(4, 1, 4);
		scene.effects.indicateSuccess(gaugePos);
		scene.idle(10);

		scene.overlay.showText(60)
			.pointAt(util.vector.topOf(windmill))
			.placeNearTarget()
			.text("Activated with Right-Click, the Windmill Bearing will start providing Rotational Force");
		scene.idle(70);

		scene.overlay.showText(60)
			.pointAt(util.vector.blockSurface(gaugePos, Direction.WEST))
			.colored(PonderPalette.SLOW)
			.placeNearTarget()
			.text("The Amount of Sail Blocks determine its Rotation Speed");
		scene.idle(90);

		Vector3d surface = util.vector.blockSurface(windmill, Direction.WEST);
		scene.overlay.showControls(new InputWindowElement(surface, Pointing.DOWN).scroll()
			.withWrench(), 60);
		scene.overlay.showCenteredScrollInput(windmill, Direction.WEST, 50);
		scene.overlay.showText(60)
			.pointAt(surface)
			.placeNearTarget()
			.text("Use a Wrench to configure its rotation direction");
		scene.idle(36);

		scene.world.rotateBearing(windmill, -90 - 45, 75);
		scene.world.rotateSection(structure, 0, 0, -90 - 45, 75);
		scene.world.modifyKineticSpeed(largeCog, f -> -f);
		scene.world.modifyKineticSpeed(kinetics, f -> -f);
		scene.effects.rotationDirectionIndicator(windmill.south());
		scene.idle(69);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(windmill), Pointing.DOWN).rightClick(), 60);
		scene.idle(7);
		scene.world.rotateBearing(windmill, -45, 0);
		scene.world.rotateSection(structure, 0, 0, -45, 0);
		scene.world.setKineticSpeed(largeCog, 0);
		scene.world.setKineticSpeed(kinetics, 0);
		scene.idle(10);
		scene.overlay.showText(60)
			.pointAt(util.vector.topOf(windmill))
			.placeNearTarget()
			.text("Right-click the Bearing anytime to stop and edit the Structure again");

	}

	public static void windmillsAnyStructure(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("windmill_structure", "Windmill Contraptions");
		scene.configureBasePlate(1, 1, 5);
		scene.setSceneOffsetY(-1);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		BlockPos bearingPos = util.grid.at(3, 1, 3);
		scene.world.showSection(util.select.position(bearingPos), Direction.DOWN);
		scene.idle(10);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.position(bearingPos.up()), Direction.DOWN);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.fromTo(3, 2, 2, 3, 3, 1), Direction.SOUTH, contraption);
		scene.world.showSectionAndMerge(util.select.fromTo(3, 2, 4, 3, 3, 5), Direction.NORTH, contraption);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(3, 1, 5), Direction.NORTH, contraption);
		scene.world.showSectionAndMerge(util.select.position(3, 4, 2), Direction.DOWN, contraption);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(2, 1, 5), Direction.EAST, contraption);
		scene.world.showSectionAndMerge(util.select.position(3, 3, 3), Direction.DOWN, contraption);
		scene.idle(5);
		scene.effects.superGlue(bearingPos.up(), Direction.SOUTH, true);
		scene.effects.superGlue(bearingPos.up(), Direction.NORTH, true);
		scene.idle(5);
		scene.effects.superGlue(util.grid.at(3, 1, 5), Direction.UP, true);
		scene.idle(5);
		scene.effects.superGlue(util.grid.at(3, 3, 3), Direction.DOWN, true);
		scene.idle(10);

		scene.overlay.showOutline(PonderPalette.BLUE, bearingPos, util.select.fromTo(3, 2, 1, 3, 3, 2), 80);
		scene.overlay.showSelectionWithText(util.select.fromTo(3, 2, 4, 3, 3, 5), 80)
			.colored(PonderPalette.BLUE)
			.text("Any Structure can count as a valid Windmill, as long as it contains at least 8 sail-like Blocks.");

		scene.idle(90);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(bearingPos, Direction.WEST), Pointing.LEFT).rightClick(),
			40);
		scene.idle(7);
		scene.markAsFinished();
		scene.world.rotateBearing(bearingPos, -720, 400);
		scene.world.rotateSection(contraption, 0, -720, 0, 400);
		scene.world.modifyTileEntity(util.grid.at(2, 1, 5), HarvesterTileEntity.class,
			hte -> hte.setAnimatedSpeed(-150));
		scene.idle(400);
		scene.world.modifyTileEntity(util.grid.at(2, 1, 5), HarvesterTileEntity.class, hte -> hte.setAnimatedSpeed(0));
	}

	public static void mechanicalBearing(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_bearing", "Movings Structures using the Mechanical Bearing");
		scene.configureBasePlate(1, 1, 5);
		scene.setSceneOffsetY(-1);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layer(1), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.layer(2), Direction.DOWN);
		scene.idle(10);

		Selection cog1 = util.select.position(6, 0, 4);
		Selection cog2 = util.select.position(5, 1, 4);
		Selection cog3 = util.select.position(4, 1, 3);
		Selection cog4 = util.select.position(3, 1, 3);
		Selection all = cog1.copy()
			.add(cog2)
			.add(cog3)
			.add(cog4);

		BlockPos bearingPos = util.grid.at(3, 2, 3);
		scene.overlay.showSelectionWithText(util.select.position(bearingPos.up()), 60)
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(bearingPos, Direction.WEST))
			.placeNearTarget()
			.text("Mechanical Bearings attach to the block in front of them");
		scene.idle(50);

		ElementLink<WorldSectionElement> plank = scene.world.showIndependentSection(util.select.position(bearingPos.up()
			.east()
			.north()), Direction.DOWN);
		scene.world.moveSection(plank, util.vector.of(-1, 0, 1), 0);
		scene.idle(20);

		scene.world.setKineticSpeed(cog1, -8);
		scene.world.setKineticSpeed(cog2, 8);
		scene.world.setKineticSpeed(cog3, -16);
		scene.world.setKineticSpeed(cog4, 16);
		scene.effects.rotationSpeedIndicator(bearingPos.down());
		scene.world.rotateBearing(bearingPos, 360, 37 * 2);
		scene.world.rotateSection(plank, 0, 360, 0, 37 * 2);

		scene.overlay.showText(80)
			.pointAt(util.vector.topOf(bearingPos.up()))
			.placeNearTarget()
			.text("Upon receiving Rotational Force, it will assemble it into a Rotating Contraption");
		scene.idle(37 * 2);
		scene.world.setKineticSpeed(all, 0);
		scene.idle(20);

		scene.world.hideIndependentSection(plank, Direction.UP);
		scene.idle(15);
		Selection plank2 = util.select.position(4, 3, 2);
		ElementLink<WorldSectionElement> contraption = scene.world.showIndependentSection(util.select.layersFrom(3)
			.substract(plank2), Direction.DOWN);
		scene.idle(10);
		scene.world.showSectionAndMerge(plank2, Direction.SOUTH, contraption);
		scene.idle(15);
		scene.effects.superGlue(util.grid.at(4, 3, 2), Direction.SOUTH, true);
		scene.idle(5);

		scene.world.configureCenterOfRotation(contraption, util.vector.topOf(bearingPos));
		scene.world.setKineticSpeed(cog1, -8);
		scene.world.setKineticSpeed(cog2, 8);
		scene.world.setKineticSpeed(cog3, -16);
		scene.world.setKineticSpeed(cog4, 16);
		scene.effects.rotationSpeedIndicator(bearingPos.down());
		scene.world.rotateBearing(bearingPos, 360 * 2, 37 * 4);
		scene.world.rotateSection(contraption, 0, 360 * 2, 0, 37 * 4);

		scene.overlay.showText(120)
			.pointAt(util.vector.topOf(bearingPos.up()))
			.placeNearTarget()
			.sharedText("movement_anchors");

		scene.idle(37 * 4);
		scene.world.setKineticSpeed(all, 0);
	}

	public static void bearingModes(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("bearing_modes", "Movement Modes of the Mechanical Bearing");
		scene.configureBasePlate(1, 1, 6);
		scene.setSceneOffsetY(-1);
		
		Selection sideCog = util.select.position(util.grid.at(7, 0, 3));
		Selection cogColumn = util.select.fromTo(6, 1, 3, 6, 4, 3);
		Selection cogAndClutch = util.select.fromTo(5, 3, 1, 5, 4, 2);
		BlockPos leverPos = util.grid.at(5, 3, 1);

		scene.world.setKineticSpeed(sideCog, 4);
		scene.world.setKineticSpeed(cogColumn, -4);
		scene.world.setKineticSpeed(cogAndClutch, 8);
		scene.world.toggleRedstonePower(cogAndClutch);

		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(cogColumn, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(cogAndClutch, Direction.DOWN);
		scene.idle(10);

		BlockPos bearingPos = util.grid.at(5, 2, 2);
		scene.world.showSection(util.select.position(bearingPos), Direction.UP);
		scene.idle(10);

		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.fromTo(5, 1, 2, 2, 1, 2), Direction.EAST);
		scene.world.configureCenterOfRotation(contraption, util.vector.centerOf(bearingPos));
		scene.idle(20);

		scene.world.toggleRedstonePower(cogAndClutch);
		scene.effects.indicateRedstone(leverPos);
		scene.world.rotateSection(contraption, 0, 55, 0, 23);
		scene.world.rotateBearing(bearingPos, 55, 23);
		scene.idle(24);

		scene.world.toggleRedstonePower(cogAndClutch);
		scene.effects.indicateRedstone(leverPos);
		scene.world.rotateSection(contraption, 0, 35, 0, 0);
		scene.world.rotateBearing(bearingPos, 35, 0);

		Vector3d target = util.vector.topOf(bearingPos.down());
		scene.overlay.showLine(PonderPalette.RED, target.add(-2.5, 0, 3.5), target, 50);
		scene.overlay.showLine(PonderPalette.GREEN, target.add(0, 0, 4.5), target, 50);

		scene.idle(50);

		scene.overlay.showText(100)
			.pointAt(util.vector.blockSurface(bearingPos, Direction.WEST))
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.text("When Stopped, the Bearing will place the structure at the nearest grid-aligned Angle");
		scene.idle(110);

		scene.overlay.showCenteredScrollInput(bearingPos, Direction.NORTH, 60);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(bearingPos, Direction.NORTH), Pointing.DOWN).scroll()
				.withWrench(),
			60);
		scene.idle(10);
		scene.overlay.showText(60)
			.pointAt(util.vector.blockSurface(bearingPos, Direction.WEST))
			.placeNearTarget()
			.sharedText("behaviour_modify_wrench");
		scene.idle(70);

		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -f);
		scene.world.toggleRedstonePower(cogAndClutch);
		scene.effects.indicateRedstone(leverPos);
		scene.world.rotateSection(contraption, 0, -55, 0, 23);
		scene.world.rotateBearing(bearingPos, -55, 23);
		scene.idle(24);

		scene.world.toggleRedstonePower(cogAndClutch);
		scene.effects.indicateRedstone(leverPos);
		scene.idle(40);

		scene.overlay.showText(120)
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 3), Direction.UP))
			.text("It can be configured never to revert to solid blocks, or only near the angle it started at");

	}

	public static void stabilizedBearings(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("stabilized_bearings", "Stabilized Contraptions");
		scene.configureBasePlate(1, 1, 5);
		scene.setSceneOffsetY(-1);

		Selection beltAndBearing = util.select.fromTo(3, 3, 4, 3, 1, 6);
		Selection largeCog = util.select.position(2, 0, 6);
		BlockPos parentBearingPos = util.grid.at(3, 3, 4);
		BlockPos bearingPos = util.grid.at(3, 4, 2);

		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(beltAndBearing, Direction.DOWN);
		scene.idle(10);

		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.fromTo(3, 3, 3, 3, 4, 3), Direction.SOUTH);
		scene.world.configureCenterOfRotation(contraption, util.vector.centerOf(parentBearingPos));
		scene.idle(20);
		scene.world.glueBlockOnto(bearingPos, Direction.SOUTH, contraption);

		scene.idle(15);

		scene.overlay.showSelectionWithText(util.select.position(bearingPos), 60)
			.text("Whenever Mechanical Bearings are themselves part of a moving Structure..")
			.placeNearTarget();
		scene.idle(70);

		scene.world.setKineticSpeed(largeCog, -8);
		scene.world.setKineticSpeed(beltAndBearing, 16);
		scene.world.rotateBearing(parentBearingPos, 360, 74);
		scene.world.rotateSection(contraption, 0, 0, 360, 74);
		scene.world.rotateBearing(bearingPos, -360, 74);
		scene.idle(74);

		scene.world.setKineticSpeed(largeCog, 0);
		scene.world.setKineticSpeed(beltAndBearing, 0);
		scene.overlay.showText(60)
			.text("..they will attempt to keep themselves upright")
			.pointAt(util.vector.blockSurface(bearingPos, Direction.NORTH))
			.placeNearTarget();
		scene.idle(70);

		scene.overlay.showSelectionWithText(util.select.position(bearingPos.north()), 60)
			.colored(PonderPalette.GREEN)
			.text("Once again, the bearing will attach to the block in front of it")
			.placeNearTarget();
		scene.idle(70);

		ElementLink<WorldSectionElement> subContraption =
			scene.world.showIndependentSection(util.select.fromTo(4, 4, 1, 2, 4, 1), Direction.SOUTH);
		scene.world.configureCenterOfRotation(subContraption, util.vector.centerOf(parentBearingPos));
		scene.world.configureStabilization(subContraption, util.vector.centerOf(bearingPos));
		scene.idle(20);

		scene.overlay.showText(80)
			.text("As a result, the entire sub-Contraption will stay upright");

		scene.world.setKineticSpeed(largeCog, -8);
		scene.world.setKineticSpeed(beltAndBearing, 16);
		scene.world.rotateBearing(parentBearingPos, 360 * 2, 74 * 2);
		scene.world.rotateSection(contraption, 0, 0, 360 * 2, 74 * 2);
		scene.world.rotateBearing(bearingPos, -360 * 2, 74 * 2);
		scene.world.rotateSection(subContraption, 0, 0, 360 * 2, 74 * 2);

		scene.markAsFinished();
		scene.idle(74 * 2);
		scene.world.setKineticSpeed(largeCog, 0);
		scene.world.setKineticSpeed(beltAndBearing, 0);
	}

	public static void clockwork(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("clockwork_bearing", "Animating Structures using Clockwork Bearings");
		scene.configureBasePlate(1, 1, 5);
		scene.setSceneOffsetY(-1);

		Selection kinetics = util.select.fromTo(3, 3, 4, 3, 1, 6);
		Selection largeCog = util.select.position(2, 0, 6);
		BlockPos bearingPos = util.grid.at(3, 3, 3);

		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(kinetics, Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(bearingPos), Direction.DOWN);
		scene.idle(10);

		scene.overlay.showSelectionWithText(util.select.position(bearingPos.north()), 60)
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(bearingPos, Direction.WEST))
			.placeNearTarget()
			.text("Clockwork Bearings attach to blocks in front of them");
		scene.idle(50);

		ElementLink<WorldSectionElement> plank =
			scene.world.showIndependentSection(util.select.position(2, 3, 2), Direction.SOUTH);
		scene.world.moveSection(plank, util.vector.of(1, 0, 0), 0);
		scene.idle(20);

		scene.world.rotateSection(plank, 0, 0, 60, 25);
		scene.world.rotateBearing(bearingPos, 60, 25);
		scene.world.setKineticSpeed(kinetics, 8);
		scene.world.setKineticSpeed(largeCog, -4);

		scene.idle(25);
		scene.overlay.showText(80)
			.pointAt(util.vector.blockSurface(bearingPos.north(), Direction.NORTH))
			.placeNearTarget()
			.text("Upon receiving Rotational Force, the structure will be rotated according to the hour of the day");
		scene.idle(90);

		scene.overlay.showText(30)
			.pointAt(util.vector.blockSurface(bearingPos.north(), Direction.NORTH))
			.placeNearTarget()
			.text("3:00");
		scene.world.rotateSection(plank, 0, 0, 30, 12);
		scene.world.rotateBearing(bearingPos, 30, 12);
		scene.idle(42);
		scene.overlay.showText(30)
			.pointAt(util.vector.blockSurface(bearingPos.north(), Direction.NORTH))
			.placeNearTarget()
			.text("4:00");
		scene.world.rotateSection(plank, 0, 0, 30, 12);
		scene.world.rotateBearing(bearingPos, 30, 12);
		scene.idle(42);

		InputWindowElement clickTheBearing = new InputWindowElement(util.vector.topOf(bearingPos), Pointing.DOWN);
		InputWindowElement clickTheBearingSide =
			new InputWindowElement(util.vector.blockSurface(bearingPos, Direction.WEST), Pointing.LEFT);

		scene.overlay.showControls(clickTheBearing.rightClick(), 60);
		scene.idle(7);
		scene.world.rotateSection(plank, 0, 0, -120, 0);
		scene.world.rotateBearing(bearingPos, -120, 0);
		scene.overlay.showText(60)
			.pointAt(util.vector.blockSurface(bearingPos, Direction.WEST))
			.placeNearTarget()
			.text("Right-Click the bearing to start or stop animating the structure");
		scene.idle(70);

		scene.world.hideIndependentSection(plank, Direction.NORTH);
		scene.idle(15);

		ElementLink<WorldSectionElement> hourHand =
			scene.world.showIndependentSection(util.select.fromTo(3, 3, 1, 3, 5, 2), Direction.SOUTH);
		scene.world.configureCenterOfRotation(hourHand, util.vector.centerOf(bearingPos));
		scene.idle(15);
		scene.overlay.showSelectionWithText(util.select.fromTo(3, 3, 1, 3, 4, 2), 80)
			.placeNearTarget()
			.sharedText("movement_anchors");
		scene.idle(90);

		scene.overlay.showControls(clickTheBearingSide.rightClick(), 20);
		scene.idle(7);
		scene.world.rotateSection(hourHand, 0, 0, 120, 50);
		scene.world.rotateBearing(bearingPos, 120, 50);
		scene.idle(60);

		scene.overlay.showSelectionWithText(util.select.position(bearingPos.north(3)), 80)
			.placeNearTarget()
			.colored(PonderPalette.BLUE)
			.text("In front of the Hour Hand, a second structure can be added");
		scene.idle(90);
		scene.overlay.showControls(clickTheBearingSide.rightClick(), 20);
		scene.idle(7);
		scene.world.rotateSection(hourHand, 0, 0, -120, 0);
		scene.world.rotateBearing(bearingPos, -120, 0);
		scene.idle(10);

		ElementLink<WorldSectionElement> minuteHand =
			scene.world.showIndependentSection(util.select.fromTo(3, 3, 0, 3, 6, 0), Direction.SOUTH);
		scene.world.configureCenterOfRotation(minuteHand, util.vector.centerOf(bearingPos));
		scene.idle(30);

		scene.overlay.showOutline(PonderPalette.BLUE, minuteHand, util.select.fromTo(3, 3, 0, 3, 6, 0), 85);
		scene.overlay.showSelectionWithText(util.select.fromTo(3, 3, 1, 3, 4, 2), 80)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.text("Ensure the two Structures are not attached to each other through super glue or similar");
		scene.idle(90);

		scene.overlay.showControls(clickTheBearingSide.rightClick(), 20);
		scene.idle(7);

		scene.world.rotateSection(hourHand, 0, 0, 120, 50);
		scene.world.rotateSection(minuteHand, 0, 0, 180, 75);
		scene.world.rotateBearing(bearingPos, 120, 50);
		scene.idle(90);
		scene.world.rotateSection(minuteHand, 0, 0, 6, 3);

		scene.overlay.showText(80)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(bearingPos.north(3), Direction.NORTH))
			.colored(PonderPalette.GREEN)
			.text("The Second Structure will now rotate as the Minute Hand");
		scene.markAsFinished();

		for (int i = 0; i < 40; i++) {
			scene.idle(23);
			scene.world.rotateSection(minuteHand, 0, 0, 6, 3);
			if (i == 29)
				scene.world.rotateSection(hourHand, 0, 0, 30, 20);
		}
	}

}
