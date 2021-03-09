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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BearingScenes {

	public static void windmillsAsSource(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("windmill_source", "Generating Rotational Force using Windmill Bearings");
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
			.text("Once Activated, the Windmill Bearing will start providing Rotational Force");
		scene.idle(70);

		scene.overlay.showText(60)
			.pointAt(util.vector.blockSurface(gaugePos, Direction.WEST))
			.colored(PonderPalette.SLOW)
			.placeNearTarget()
			.text("The Amount of Sail Blocks determine its Rotation Speed");
		scene.idle(90);

		Vec3d surface = util.vector.blockSurface(windmill, Direction.WEST);
		AxisAlignedBB point = new AxisAlignedBB(surface, surface);
		AxisAlignedBB expanded = point.grow(1 / 16f, 1 / 4f, 1 / 4f);

		scene.overlay.showControls(new InputWindowElement(surface, Pointing.DOWN).scroll()
			.withWrench(), 60);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, point, point, 1);
		scene.idle(1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, point, expanded, 50);
		scene.overlay.showText(60)
			.pointAt(surface)
			.placeNearTarget()
			.text("Use a Wrench to configure its rotation direction");
		scene.idle(35);

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
		scene.world.rotateBearing(bearingPos, -720, 400);
		scene.world.rotateSection(contraption, 0, -720, 0, 400);
		scene.world.modifyTileEntity(util.grid.at(2, 1, 5), HarvesterTileEntity.class,
			hte -> hte.setAnimatedSpeed(-150));
		scene.markAsFinished();
		scene.idle(400);
		scene.world.modifyTileEntity(util.grid.at(2, 1, 5), HarvesterTileEntity.class, hte -> hte.setAnimatedSpeed(0));
	}

}
