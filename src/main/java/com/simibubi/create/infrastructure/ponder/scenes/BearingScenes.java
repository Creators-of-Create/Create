package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterBlockEntity;
import com.simibubi.create.content.contraptions.bearing.SailBlock;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BearingScenes {

	public static void windmillsAsSource(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("windmill_source", "Generating Rotational Force using Windmill Bearings");
		scene.configureBasePlate(1, 1, 5);
		scene.setSceneOffsetY(-1);
		scene.scaleSceneView(.9f);

		scene.world.showSection(util.select.fromTo(1, 0, 1, 5, 0, 5), Direction.UP);
		scene.world.setBlock(util.grid.at(2, -1, 0), AllBlocks.SAIL.getDefaultState()
			.setValue(SailBlock.FACING, Direction.NORTH), false);
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
			.attachKeyFrame()
			.placeNearTarget()
			.text("Windmill Bearings attach to the block in front of them");
		scene.idle(50);

		ElementLink<WorldSectionElement> structure =
			scene.world.showIndependentSection(util.select.position(anchorPos), Direction.SOUTH);
		scene.idle(10);
		for (Direction d : Iterate.directions)
			if (d.getAxis() != Axis.Z)
				scene.world.showSectionAndMerge(util.select.fromTo(anchorPos.relative(d, 1), anchorPos.relative(d, 2)),
					d.getOpposite(), structure);
		scene.idle(10);

		scene.world.showSectionAndMerge(util.select.fromTo(anchorPos.above()
			.east(),
			anchorPos.above(3)
				.east()),
			Direction.WEST, structure);
		scene.world.showSectionAndMerge(util.select.fromTo(anchorPos.below()
			.west(),
			anchorPos.below(3)
				.west()),
			Direction.EAST, structure);
		scene.world.showSectionAndMerge(util.select.fromTo(anchorPos.east()
			.below(),
			anchorPos.east(3)
				.below()),
			Direction.UP, structure);
		scene.world.showSectionAndMerge(util.select.fromTo(anchorPos.west()
			.above(),
			anchorPos.west(3)
				.above()),
			Direction.DOWN, structure);

		scene.idle(5);
		for (Direction d : Iterate.directions)
			if (d.getAxis() != Axis.Z)
				scene.effects.superGlue(anchorPos.relative(d, 1), d.getOpposite(), false);
		scene.idle(10);

		AABB bb1 = new AABB(util.grid.at(5, 2, 0));
		AABB bb2 = new AABB(util.grid.at(3, 4, 0));
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb1, bb1, 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb1, bb1.expandTowards(-4, 0, 0), 75);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb2, bb2, 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb2, bb2.expandTowards(0, -4, 0), 80);
		scene.idle(10);
		scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(util.grid.at(5, 2, 0)), Pointing.RIGHT)
			.withItem(AllItems.SUPER_GLUE.asStack()), 40);

		scene.idle(15);
		scene.overlay.showText(60)
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 0), Direction.NORTH))
			.attachKeyFrame()
			.placeNearTarget()
			.text("Create a movable structure with the help of Super Glue");
		scene.idle(70);

		scene.overlay.showText(80)
			.pointAt(util.vector.centerOf(1, 3, 0))
			.attachKeyFrame()
			.placeNearTarget()
			.text("If enough Sail-like blocks are included, this can act as a Windmill");
		scene.idle(70);

		scene.rotateCameraY(-90);
		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(windmill)
			.subtract(.5, 0, 0), Pointing.DOWN).rightClick(), 60);
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
			.pointAt(util.vector.topOf(windmill)
				.subtract(.5, 0, 0))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Activated with Right-Click, the Windmill Bearing will start providing Rotational Force");
		scene.idle(70);

		scene.overlay.showText(60)
			.pointAt(util.vector.blockSurface(gaugePos, Direction.WEST))
			.colored(PonderPalette.SLOW)
			.placeNearTarget()
			.text("The Amount of Sail Blocks determine its Rotation Speed");
		scene.idle(90);

		Vec3 surface = util.vector.blockSurface(windmill, Direction.WEST)
			.add(0, 0, 2 / 16f);
		scene.overlay.showControls(new InputWindowElement(surface, Pointing.DOWN).rightClick(), 60);
		scene.overlay.showFilterSlotInput(surface, Direction.WEST, 50);
		scene.overlay.showText(60)
			.pointAt(surface)
			.attachKeyFrame()
			.placeNearTarget()
			.text("Use the value panel to configure its rotation direction");
		scene.idle(36);

		scene.world.rotateBearing(windmill, -90 - 45, 75);
		scene.world.rotateSection(structure, 0, 0, -90 - 45, 75);
		scene.world.modifyKineticSpeed(largeCog, f -> -f);
		scene.world.modifyKineticSpeed(kinetics, f -> -f);
		scene.effects.rotationDirectionIndicator(windmill.south());
		scene.idle(69);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(windmill)
			.subtract(.5, 0, 0), Pointing.DOWN).rightClick(), 60);
		scene.idle(7);
		scene.world.rotateBearing(windmill, -45, 0);
		scene.world.rotateSection(structure, 0, 0, -45, 0);
		scene.world.setKineticSpeed(largeCog, 0);
		scene.world.setKineticSpeed(kinetics, 0);
		scene.idle(10);
		scene.overlay.showText(60)
			.pointAt(util.vector.topOf(windmill)
				.subtract(.5, 0, 0))
			.placeNearTarget()
			.text("Right-click the Bearing anytime to stop and edit the Structure again");
		scene.idle(30);

	}

	public static void windmillsAnyStructure(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("windmill_structure", "Windmill Contraptions");
		scene.configureBasePlate(1, 1, 5);
		scene.setSceneOffsetY(-1);
		scene.world.modifyEntities(SuperGlueEntity.class, Entity::discard);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		BlockPos bearingPos = util.grid.at(3, 1, 3);
		scene.world.showSection(util.select.position(bearingPos), Direction.DOWN);
		scene.idle(10);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.position(bearingPos.above()), Direction.DOWN);
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
		scene.effects.superGlue(bearingPos.above(), Direction.SOUTH, true);
		scene.effects.superGlue(bearingPos.above(), Direction.NORTH, true);
		scene.idle(5);
		scene.effects.superGlue(util.grid.at(3, 1, 5), Direction.UP, true);
		scene.idle(5);
		scene.effects.superGlue(util.grid.at(3, 3, 3), Direction.DOWN, true);
		scene.idle(10);

		scene.overlay.showOutline(PonderPalette.BLUE, bearingPos, util.select.fromTo(3, 2, 1, 3, 3, 2), 80);
		scene.overlay.showSelectionWithText(util.select.fromTo(3, 2, 4, 3, 3, 5), 80)
			.colored(PonderPalette.BLUE)
			.attachKeyFrame()
			.text("Any Structure can count as a valid Windmill, as long as it contains at least 8 sail-like Blocks.");

		scene.idle(90);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(bearingPos, Direction.WEST), Pointing.LEFT).rightClick(),
			40);
		scene.idle(7);
		scene.markAsFinished();
		scene.world.rotateBearing(bearingPos, -720, 400);
		scene.world.rotateSection(contraption, 0, -720, 0, 400);
		scene.world.modifyBlockEntity(util.grid.at(2, 1, 5), HarvesterBlockEntity.class,
			hte -> hte.setAnimatedSpeed(-150));
		scene.idle(400);
		scene.world.modifyBlockEntity(util.grid.at(2, 1, 5), HarvesterBlockEntity.class,
			hte -> hte.setAnimatedSpeed(0));
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
		scene.overlay.showSelectionWithText(util.select.position(bearingPos.above()), 60)
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(bearingPos, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Mechanical Bearings attach to the block in front of them");
		scene.idle(50);

		ElementLink<WorldSectionElement> plank =
			scene.world.showIndependentSection(util.select.position(bearingPos.above()
				.east()
				.north()), Direction.DOWN);
		scene.world.moveSection(plank, util.vector.of(-1, 0, 1), 0);
		scene.idle(20);

		scene.world.setKineticSpeed(cog1, -8);
		scene.world.setKineticSpeed(cog2, 8);
		scene.world.setKineticSpeed(cog3, -16);
		scene.world.setKineticSpeed(cog4, 16);
		scene.effects.rotationSpeedIndicator(bearingPos.below());
		scene.world.rotateBearing(bearingPos, 360, 37 * 2);
		scene.world.rotateSection(plank, 0, 360, 0, 37 * 2);

		scene.overlay.showText(80)
			.pointAt(util.vector.topOf(bearingPos.above()))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Upon receiving Rotational Force, it will assemble it into a Rotating Contraption");
		scene.idle(37 * 2);
		scene.world.setKineticSpeed(all, 0);
		scene.idle(20);

		scene.world.hideIndependentSection(plank, Direction.UP);
		scene.idle(15);
		Selection plank2 = util.select.position(4, 3, 2);
		ElementLink<WorldSectionElement> contraption = scene.world.showIndependentSection(util.select.layersFrom(3)
			.substract(plank2), Direction.DOWN);
		scene.world.replaceBlocks(util.select.fromTo(2, 4, 3, 4, 3, 3), Blocks.OAK_PLANKS.defaultBlockState(), false);
		scene.idle(10);

		scene.overlay.showOutline(PonderPalette.GREEN, "glue", util.select.position(2, 4, 3)
			.add(util.select.fromTo(4, 3, 3, 2, 3, 3))
			.add(util.select.position(4, 3, 2)), 40);
		scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(util.grid.at(4, 3, 3)), Pointing.RIGHT)
			.withItem(AllItems.SUPER_GLUE.asStack()), 40);

		scene.idle(10);
		scene.world.showSectionAndMerge(plank2, Direction.SOUTH, contraption);
		scene.idle(15);
		scene.effects.superGlue(util.grid.at(4, 3, 2), Direction.SOUTH, true);
		scene.overlay.showText(120)
			.pointAt(util.vector.topOf(bearingPos.above()))
			.placeNearTarget()
			.attachKeyFrame()
			.sharedText("movement_anchors");
		scene.idle(25);

		scene.world.configureCenterOfRotation(contraption, util.vector.topOf(bearingPos));
		scene.world.setKineticSpeed(cog1, -8);
		scene.world.setKineticSpeed(cog2, 8);
		scene.world.setKineticSpeed(cog3, -16);
		scene.world.setKineticSpeed(cog4, 16);
		scene.effects.rotationSpeedIndicator(bearingPos.below());
		scene.world.rotateBearing(bearingPos, 360 * 2, 37 * 4);
		scene.world.rotateSection(contraption, 0, 360 * 2, 0, 37 * 4);

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

		Vec3 target = util.vector.topOf(bearingPos.below());
		scene.overlay.showLine(PonderPalette.RED, target.add(-2.5, 0, 3.5), target, 50);
		scene.overlay.showLine(PonderPalette.GREEN, target.add(0, 0, 4.5), target, 50);

		scene.idle(50);

		scene.overlay.showText(100)
			.pointAt(util.vector.topOf(util.grid.at(5, 0, 4)))
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.text("When Stopped, the Bearing will place the structure at the nearest grid-aligned Angle");
		scene.idle(110);

		Vec3 blockSurface = util.vector.blockSurface(bearingPos, Direction.NORTH)
			.add(0, 2 / 16f, 0);
		scene.overlay.showFilterSlotInput(blockSurface, Direction.NORTH, 60);
		scene.overlay.showControls(new InputWindowElement(blockSurface, Pointing.DOWN).scroll()
			.withWrench(), 60);
		scene.idle(10);
		scene.overlay.showText(60)
			.pointAt(blockSurface)
			.placeNearTarget()
			.attachKeyFrame()
			.sharedText("behaviour_modify_value_panel");
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
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 3), Direction.UP))
			.text("It can be configured never to revert to solid blocks, or only near the angle it started at");
		scene.idle(90);

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
			.attachKeyFrame()
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
			.attachKeyFrame()
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
			.attachKeyFrame()
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
			.attachKeyFrame()
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
			.attachKeyFrame()
			.text("Right-Click the bearing to start or stop animating the structure");
		scene.idle(70);

		scene.world.hideIndependentSection(plank, Direction.NORTH);
		scene.idle(15);

		scene.world.replaceBlocks(util.select.fromTo(3, 3, 1, 3, 4, 2), Blocks.OAK_PLANKS.defaultBlockState(), false);
		ElementLink<WorldSectionElement> hourHand =
			scene.world.showIndependentSection(util.select.fromTo(3, 3, 1, 3, 5, 2), Direction.SOUTH);
		scene.world.configureCenterOfRotation(hourHand, util.vector.centerOf(bearingPos));
		scene.idle(15);
		scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(util.grid.at(3, 4, 1)), Pointing.RIGHT)
			.withItem(AllItems.SUPER_GLUE.asStack()), 40);
		scene.overlay.showSelectionWithText(util.select.fromTo(3, 3, 1, 3, 4, 2), 80)
			.placeNearTarget()
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
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
			.attachKeyFrame()
			.text("In front of the Hour Hand, a second structure can be added");
		scene.idle(90);
		scene.overlay.showControls(clickTheBearingSide.rightClick(), 20);
		scene.idle(7);
		scene.world.rotateSection(hourHand, 0, 0, -120, 0);
		scene.world.rotateBearing(bearingPos, -120, 0);
		scene.idle(10);

		scene.world.setBlock(util.grid.at(3, 3, 0), Blocks.STONE_BRICK_WALL.defaultBlockState()
			.setValue(WallBlock.SOUTH_WALL, WallSide.TALL), false);
		ElementLink<WorldSectionElement> minuteHand =
			scene.world.showIndependentSection(util.select.fromTo(3, 3, 0, 3, 6, 0), Direction.SOUTH);
		scene.world.configureCenterOfRotation(minuteHand, util.vector.centerOf(bearingPos));
		scene.idle(30);

		scene.overlay.showOutline(PonderPalette.BLUE, minuteHand, util.select.fromTo(3, 3, 0, 3, 6, 0), 85);
		scene.overlay.showSelectionWithText(util.select.fromTo(3, 3, 1, 3, 4, 2), 80)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.text("Ensure that the two Structures are not glued to each other");
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

	public static void sail(SceneBuilder scene, SceneBuildingUtil util) {
		sails(scene, util, false);
	}

	public static void sailFrame(SceneBuilder scene, SceneBuildingUtil util) {
		sails(scene, util, true);
	}

	private static void sails(SceneBuilder scene, SceneBuildingUtil util, boolean frame) {
		String plural = frame ? "Sail Frames" : "Sails";
		scene.title(frame ? "sail_frame" : "sail", "Assembling Windmills using " + plural);
		scene.configureBasePlate(0, 0, 5);
		scene.scaleSceneView(0.9f);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		BlockPos bearingPos = util.grid.at(2, 1, 2);
		scene.world.showSection(util.select.position(bearingPos), Direction.DOWN);
		scene.idle(5);
		ElementLink<WorldSectionElement> plank =
			scene.world.showIndependentSection(util.select.position(bearingPos.above()), Direction.DOWN);
		scene.idle(10);

		for (int i = 0; i < 3; i++) {
			for (Direction d : Iterate.horizontalDirections) {
				BlockPos location = bearingPos.above(i + 1)
					.relative(d);
				if (frame)
					scene.world.modifyBlock(location, s -> AllBlocks.SAIL_FRAME.getDefaultState()
						.setValue(SailBlock.FACING, s.getValue(SailBlock.FACING)), false);
				scene.world.showSectionAndMerge(util.select.position(location), d.getOpposite(), plank);
				scene.idle(2);
			}
		}

		scene.overlay.showText(70)
			.text(plural + " are handy blocks to create Windmills with")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 3, 2), Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame();
		scene.idle(80);

		scene.overlay.showSelectionWithText(util.select.position(bearingPos.above()), 80)
			.colored(PonderPalette.GREEN)
			.text("They will attach to blocks and each other without the need of Super Glue or Chassis Blocks")
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(40);
		scene.world.configureCenterOfRotation(plank, util.vector.centerOf(bearingPos));

		if (!frame) {
			scene.world.rotateBearing(bearingPos, 180, 75);
			scene.world.rotateSection(plank, 0, 180, 0, 75);
			scene.idle(76);
			scene.world.rotateBearing(bearingPos, 180, 0);
			scene.world.rotateSection(plank, 0, 180, 0, 0);
			scene.rotateCameraY(-30);
			scene.idle(10);
			InputWindowElement input =
				new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 3, 1), Direction.NORTH), Pointing.RIGHT)
					.withItem(new ItemStack(Items.BLUE_DYE));
			scene.overlay.showControls(input, 30);
			scene.idle(7);
			scene.world.setBlock(util.grid.at(2, 3, 1), AllBlocks.DYED_SAILS.get(DyeColor.BLUE)
				.getDefaultState()
				.setValue(SailBlock.FACING, Direction.WEST), false);
			scene.idle(10);
			scene.overlay.showText(40)
				.colored(PonderPalette.BLUE)
				.text("Right-Click with Dye to paint them")
				.attachKeyFrame()
				.pointAt(util.vector.blockSurface(util.grid.at(2, 3, 1), Direction.WEST))
				.placeNearTarget();
			scene.idle(20);
			scene.overlay.showControls(input, 30);
			scene.idle(7);
			scene.world.replaceBlocks(util.select.fromTo(2, 2, 1, 2, 4, 1), AllBlocks.DYED_SAILS.get(DyeColor.BLUE)
				.getDefaultState()
				.setValue(SailBlock.FACING, Direction.WEST), false);

			scene.idle(20);
			scene.world.rotateBearing(bearingPos, 90, 33);
			scene.world.rotateSection(plank, 0, 90, 0, 33);
			scene.idle(40);

			input =
				new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 3, 1), Direction.NORTH), Pointing.RIGHT)
					.withItem(new ItemStack(Items.SHEARS));

			scene.overlay.showControls(input, 30);
			scene.idle(7);
			scene.world.setBlock(util.grid.at(3, 3, 2), AllBlocks.SAIL_FRAME.getDefaultState()
				.setValue(SailBlock.FACING, Direction.NORTH), false);
			scene.idle(10);
			scene.overlay.showText(40)
				.text("Right-Click with Shears to turn them back into frames")
				.attachKeyFrame()
				.pointAt(util.vector.blockSurface(util.grid.at(2, 3, 1), Direction.WEST))
				.placeNearTarget();
			scene.idle(20);
			scene.overlay.showControls(input, 30);
			scene.idle(7);
			scene.world.replaceBlocks(util.select.fromTo(3, 2, 2, 3, 4, 2), AllBlocks.SAIL_FRAME.getDefaultState()
				.setValue(SailBlock.FACING, Direction.NORTH), false);
			scene.idle(20);
		}

		scene.world.rotateBearing(bearingPos, 720, 300);
		scene.world.rotateSection(plank, 0, 720, 0, 300);

	}

}
