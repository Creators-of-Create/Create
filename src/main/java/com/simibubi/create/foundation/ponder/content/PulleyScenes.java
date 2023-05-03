package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

public class PulleyScenes {

	public static void movement(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("rope_pulley", "Moving Structures using Rope Pulleys");
		scene.configureBasePlate(0, 0, 5);
		scene.scaleSceneView(0.95f);
		scene.setSceneOffsetY(-1);

		Selection reversable = util.select.fromTo(2, 3, 4, 2, 4, 2);
		BlockPos leverPos = util.grid.at(1, 2, 4);
		BlockPos pulleyPos = util.grid.at(2, 4, 2);
		Selection redstoneStuff = util.select.fromTo(leverPos, leverPos.east());

		scene.world.showSection(util.select.layer(0), Direction.UP);
		ElementLink<WorldSectionElement> plank =
			scene.world.showIndependentSection(util.select.position(2, 1, 2), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 4, 3, 2, 1, 4), Direction.DOWN);
		scene.idle(10);

		scene.world.showSection(util.select.position(pulleyPos), Direction.SOUTH);
		scene.idle(20);

		scene.world.toggleRedstonePower(redstoneStuff);
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(reversable, f -> -f);
		scene.effects.rotationDirectionIndicator(pulleyPos.south());
		scene.world.movePulley(pulleyPos, 2, 40);

		scene.idle(45);
		scene.overlay.showText(60)
			.pointAt(util.vector.blockSurface(pulleyPos, Direction.WEST))
			.attachKeyFrame()
			.text("Rope Pulleys can move blocks vertically when given Rotational Force")
			.placeNearTarget();
		scene.idle(70);

		scene.world.toggleRedstonePower(redstoneStuff);
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(reversable, f -> -f);
		scene.effects.rotationDirectionIndicator(pulleyPos.south());
		scene.world.movePulley(pulleyPos, -2, 40);
		scene.world.moveSection(plank, util.vector.of(0, 2, 0), 40);
		scene.idle(60);

		scene.overlay.showText(60)
			.pointAt(util.vector.blockSurface(pulleyPos, Direction.SOUTH))
			.text("Direction and Speed of movement depend on the Rotational Input")
			.placeNearTarget();

		scene.world.toggleRedstonePower(redstoneStuff);
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(reversable, f -> -f);
		scene.effects.rotationDirectionIndicator(pulleyPos.south());
		scene.world.movePulley(pulleyPos, 2, 40);
		scene.world.moveSection(plank, util.vector.of(0, -2, 0), 40);
		scene.idle(50);

		scene.world.hideIndependentSection(plank, Direction.NORTH);
		scene.idle(15);
		ElementLink<WorldSectionElement> chassis =
			scene.world.showIndependentSection(util.select.fromTo(2, 1, 1, 0, 2, 1), Direction.SOUTH);
		scene.world.moveSection(chassis, util.vector.of(1, 0, 1), 0);
		scene.world.replaceBlocks(util.select.fromTo(0, 2, 1, 2, 1, 1), Blocks.OAK_PLANKS.defaultBlockState(), false);

		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(2, 1, 0), Direction.SOUTH, chassis);
		scene.overlay.showOutline(PonderPalette.GREEN, "glue", util.select.position(3, 1, 1)
			.add(util.select.fromTo(1, 1, 2, 3, 1, 2))
			.add(util.select.position(1, 2, 2)), 40);
		scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(util.grid.at(2, 2, 0)), Pointing.RIGHT)
			.withItem(AllItems.SUPER_GLUE.asStack()), 40);
		scene.idle(15);
		scene.effects.superGlue(util.grid.at(3, 1, 1), Direction.SOUTH, true);
		scene.overlay.showText(80)
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 2), Direction.NORTH))
			.placeNearTarget()
			.attachKeyFrame()
			.sharedText("movement_anchors");
		scene.idle(90);

		scene.world.toggleRedstonePower(redstoneStuff);
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(reversable, f -> -f);
		scene.effects.rotationDirectionIndicator(pulleyPos.south());
		scene.world.movePulley(pulleyPos, -2, 40);
		scene.world.moveSection(chassis, util.vector.of(0, 2, 0), 40);
		scene.idle(50);

		scene.world.toggleRedstonePower(redstoneStuff);
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(reversable, f -> -f);
		scene.effects.rotationDirectionIndicator(pulleyPos.south());
		scene.world.movePulley(pulleyPos, 2, 40);
		scene.world.moveSection(chassis, util.vector.of(0, -2, 0), 40);
		scene.idle(50);
	}

	public static void movementModes(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("rope_pulley_modes", "Movement Modes of the Rope Pulley");
		scene.configureBasePlate(0, 0, 5);
		scene.scaleSceneView(0.95f);
		scene.setSceneOffsetY(-1);

		Selection reversable = util.select.fromTo(2, 3, 4, 2, 4, 2);
		BlockPos leverPos = util.grid.at(1, 2, 4);
		BlockPos pulleyPos = util.grid.at(2, 4, 2);
		Selection redstoneStuff = util.select.fromTo(leverPos, leverPos.east());
		BlockPos flowerPos = util.grid.at(2, 1, 2);

		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.world.showSection(util.select.position(flowerPos), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 4, 3, 2, 1, 4), Direction.DOWN);
		scene.idle(10);

		scene.world.showSection(util.select.position(pulleyPos), Direction.SOUTH);
		ElementLink<WorldSectionElement> glass =
			scene.world.showIndependentSection(util.select.position(pulleyPos.below()), Direction.UP);
		scene.idle(20);

		scene.world.toggleRedstonePower(redstoneStuff);
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(reversable, f -> -f);
		scene.effects.rotationDirectionIndicator(pulleyPos.south());
		scene.world.movePulley(pulleyPos, 2, 40);
		scene.world.moveSection(glass, util.vector.of(0, -2, 0), 40);
		scene.idle(40);

		scene.world.destroyBlock(flowerPos);
		scene.idle(10);
		scene.overlay.showSelectionWithText(util.select.position(flowerPos), 70)
			.text("Whenever Pulleys stop moving, the moved structure reverts to blocks")
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.RED);
		scene.idle(80);

		scene.world.toggleRedstonePower(redstoneStuff);
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(reversable, f -> -f);
		scene.effects.rotationDirectionIndicator(pulleyPos.south());
		scene.world.movePulley(pulleyPos, -2, 40);
		scene.world.moveSection(glass, util.vector.of(0, 2, 0), 40);
		scene.world.hideSection(util.select.position(flowerPos), Direction.DOWN);
		scene.idle(40);

		scene.world.setBlock(flowerPos, Blocks.BLUE_ORCHID.defaultBlockState(), false);
		scene.world.showSection(util.select.position(flowerPos), Direction.DOWN);
		scene.overlay.showCenteredScrollInput(pulleyPos, Direction.UP, 60);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(pulleyPos), Pointing.DOWN).rightClick(),
			60);	
		scene.overlay.showText(70)
			.pointAt(util.vector.topOf(pulleyPos))
			.placeNearTarget()
			.attachKeyFrame()
			.sharedText("behaviour_modify_value_panel");
		scene.idle(80);

		scene.world.toggleRedstonePower(redstoneStuff);
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(reversable, f -> -f);
		scene.effects.rotationDirectionIndicator(pulleyPos.south());
		scene.world.movePulley(pulleyPos, 2, 40);
		scene.world.moveSection(glass, util.vector.of(0, -2, 0), 40);
		scene.idle(50);
		scene.overlay.showText(120)
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(flowerPos, Direction.WEST))
			.placeNearTarget()
			.text("It can be configured never to revert to solid blocks, or only at the location it started at");
		scene.idle(90);
	}

	public static void attachment(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("rope_pulley_attachment", "Moving Pulleys as part of a Contraption");
		scene.configureBasePlate(0, 0, 5);
		scene.scaleSceneView(0.95f);
		scene.setSceneOffsetY(-1);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		Selection kinetics = util.select.fromTo(4, 3, 2, 4, 1, 5);
		Selection largeCog = util.select.position(3, 0, 5);

		scene.world.showSection(kinetics, Direction.DOWN);
		ElementLink<WorldSectionElement> poles =
			scene.world.showIndependentSection(util.select.fromTo(4, 4, 2, 6, 4, 2), Direction.DOWN);
		scene.world.moveSection(poles, util.vector.of(0, -1, 0), 0);
		scene.idle(10);

		BlockPos pulleyPos = util.grid.at(3, 3, 2);
		ElementLink<WorldSectionElement> pulley =
			scene.world.showIndependentSection(util.select.position(pulleyPos), Direction.EAST);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.fromTo(3, 1, 1, 3, 1, 2)
			.add(util.select.position(3, 2, 1)), Direction.SOUTH, pulley);

		scene.idle(10);
		scene.overlay.showText(50)
			.pointAt(util.vector.blockSurface(pulleyPos, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Whenever Pulleys are themselves being moved by a Contraption...");
		scene.idle(60);

		scene.world.setKineticSpeed(largeCog, -16);
		scene.world.setKineticSpeed(kinetics, 32);
		scene.effects.rotationDirectionIndicator(util.grid.at(4, 1, 5));
		scene.world.moveSection(poles, util.vector.of(-2, 0, 0), 40);
		scene.world.moveSection(pulley, util.vector.of(-2, 0, 0), 40);
		scene.idle(40);

		scene.overlay.showSelectionWithText(util.select.fromTo(1, 1, 1, 1, 1, 2), 50)
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.attachKeyFrame()
			.text("...its attached structure will be dragged with it");
		scene.idle(60);
		scene.overlay.showText(80)
			.colored(PonderPalette.RED)
			.pointAt(util.vector.topOf(pulleyPos.west(2)))
			.placeNearTarget()
			.text("Mind that pulleys are only movable while stopped");
		scene.idle(50);
	}

}
