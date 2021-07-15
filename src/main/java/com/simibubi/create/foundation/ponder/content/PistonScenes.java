package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonHeadBlock;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.ParrotElement;
import com.simibubi.create.foundation.ponder.elements.ParrotElement.FaceCursorPose;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.state.properties.PistonType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class PistonScenes {

	public static void movement(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_piston", "Moving Structures using Mechanical Pistons");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0)
			.add(util.select.position(0, 1, 2)), Direction.UP);

		Selection kinetics = util.select.fromTo(3, 1, 3, 3, 1, 2);
		BlockPos piston = util.grid.at(3, 1, 2);
		BlockPos leverPos = util.grid.at(3, 2, 4);
		BlockPos shaft = util.grid.at(3, 1, 3);

		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 3, 3, 2, 5), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(piston), Direction.DOWN);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.position(3, 1, 1), Direction.DOWN);
		scene.world.moveSection(contraption, util.vector.of(0, 0, 1), 0);
		scene.idle(20);
		scene.world.showSectionAndMerge(util.select.position(piston.north()
			.east()), Direction.DOWN, contraption);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(piston.north()
			.east(2)), Direction.DOWN, contraption);
		scene.world.showSectionAndMerge(util.select.position(piston.north()
			.west()), Direction.DOWN, contraption);
		scene.idle(15);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.world.modifyKineticSpeed(kinetics, f -> -f);
		scene.effects.rotationDirectionIndicator(shaft);
		scene.world.moveSection(contraption, util.vector.of(-2, 0, 0), 40);
		scene.overlay.showText(55)
			.pointAt(util.vector.topOf(piston))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Mechanical Pistons can move blocks in front of them");
		scene.idle(65);

		scene.overlay.showText(45)
			.pointAt(util.vector.blockSurface(shaft, Direction.SOUTH))
			.placeNearTarget()
			.text("Speed and direction of movement depend on the Rotational Input");
		scene.world.setBlock(util.grid.at(2, 1, 1), Blocks.AIR.defaultBlockState(), false);
		scene.world.setBlock(util.grid.at(0, 1, 2), Blocks.OAK_PLANKS.defaultBlockState(), false);
		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.world.modifyKineticSpeed(kinetics, f -> -f);
		scene.effects.rotationDirectionIndicator(shaft);
		scene.world.moveSection(contraption, util.vector.of(2, 0, 0), 40);
		scene.idle(60);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(piston, Direction.WEST), Pointing.DOWN).rightClick()
				.withItem(new ItemStack(Items.SLIME_BALL)),
			30);
		scene.idle(7);
		scene.world.modifyBlock(piston.north(), s -> s.setValue(MechanicalPistonHeadBlock.TYPE, PistonType.STICKY), false);
		scene.effects.superGlue(piston, Direction.WEST, true);

		scene.idle(33);
		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.world.modifyKineticSpeed(kinetics, f -> -f);
		scene.effects.rotationDirectionIndicator(shaft);
		scene.world.moveSection(contraption, util.vector.of(-2, 0, 0), 40);

		scene.idle(25);
		scene.overlay.showText(60)
			.pointAt(util.vector.topOf(piston))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Sticky Mechanical Pistons can pull the attached blocks back");
		scene.idle(20);
		scene.world.setBlock(util.grid.at(2, 1, 1), Blocks.OAK_PLANKS.defaultBlockState(), false);
		scene.world.setBlock(util.grid.at(0, 1, 2), Blocks.AIR.defaultBlockState(), false);
		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.world.modifyKineticSpeed(kinetics, f -> -f);
		scene.effects.rotationDirectionIndicator(shaft);
		scene.world.moveSection(contraption, util.vector.of(2, 0, 0), 40);

		scene.idle(50);
		scene.world.setBlock(util.grid.at(2, 1, 1), Blocks.AIR.defaultBlockState(), false);
		ElementLink<WorldSectionElement> chassis =
			scene.world.showIndependentSection(util.select.fromTo(2, 2, 0, 2, 3, 2), Direction.DOWN);
		scene.world.moveSection(chassis, util.vector.of(0, -1, 1), 0);
		scene.addKeyframe();
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(1, 2, 0), Direction.EAST, chassis);
		scene.idle(15);
		scene.effects.superGlue(piston.west()
			.north(), Direction.WEST, true);
		scene.overlay.showText(80)
			.pointAt(util.vector.topOf(piston.west()))
			.placeNearTarget()
			.sharedText("movement_anchors");

		scene.idle(90);
		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.world.modifyKineticSpeed(kinetics, f -> -f);
		scene.effects.rotationDirectionIndicator(shaft);
		scene.world.moveSection(contraption, util.vector.of(-2, 0, 0), 40);
		scene.world.moveSection(chassis, util.vector.of(-2, 0, 0), 40);
	}

	public static void poles(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("piston_pole", "Piston Extension Poles");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -f);

		Selection kinetics = util.select.fromTo(3, 1, 3, 3, 1, 2);
		BlockPos piston = util.grid.at(3, 1, 2);

		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 3, 3, 2, 5), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(piston), Direction.DOWN);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.position(3, 1, 1), Direction.DOWN);
		scene.world.moveSection(contraption, util.vector.of(0, 0, 1), 0);
		scene.idle(20);

		BlockPos leverPos = util.grid.at(3, 2, 4);
		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.world.setKineticSpeed(kinetics, 16);
		scene.idle(10);

		scene.overlay.showSelectionWithText(util.select.position(piston), 50)
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Without attached Poles, a Mechanical Piston cannot move");
		scene.idle(60);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.world.setKineticSpeed(kinetics, 0);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(piston.north()
			.east()), Direction.DOWN, contraption);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(piston.north()
			.east(2)), Direction.DOWN, contraption);
		scene.idle(10);

		scene.overlay.showOutline(PonderPalette.RED, new Object(), util.select.fromTo(piston.east(), piston.east(2)),
			100);
		scene.overlay.showSelectionWithText(util.select.fromTo(piston.west(), piston.west(2)), 100)
			.text("The Length of pole added at its back determines the Extension Range")
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.GREEN);
		scene.idle(110);

		scene.world.showSectionAndMerge(util.select.position(piston.north()
			.west()), Direction.EAST, contraption);
		scene.idle(10);
		ElementLink<ParrotElement> birb =
			scene.special.createBirb(util.vector.topOf(piston.west()), FaceCursorPose::new);
		scene.idle(15);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.world.setKineticSpeed(kinetics, 16);
		scene.world.moveSection(contraption, util.vector.of(-2, 0, 0), 40);
		scene.special.moveParrot(birb, util.vector.of(-2, 0, 0), 40);

	}

	public static void movementModes(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_piston_modes", "Movement Modes of the Mechanical Piston");
		scene.configureBasePlate(0, 0, 5);
		Selection rose = util.select.fromTo(0, 2, 2, 0, 1, 2);
		scene.world.showSection(util.select.layer(0)
			.add(rose), Direction.UP);

		Selection kinetics = util.select.fromTo(3, 1, 3, 3, 1, 2);
		BlockPos piston = util.grid.at(3, 1, 2);
		BlockPos leverPos = util.grid.at(3, 2, 4);
		BlockPos shaft = util.grid.at(3, 1, 3);

		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 3, 3, 2, 5), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(piston), Direction.DOWN);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.position(3, 1, 1), Direction.DOWN);
		scene.world.moveSection(contraption, util.vector.of(0, 0, 1), 0);
		scene.idle(20);
		scene.world.showSectionAndMerge(util.select.position(piston.north()
			.east()), Direction.DOWN, contraption);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(piston.north()
			.east(2)), Direction.DOWN, contraption);
		scene.world.showSectionAndMerge(util.select.position(piston.north()
			.west()), Direction.DOWN, contraption);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(piston.north()
			.west()
			.above()), Direction.DOWN, contraption);
		scene.idle(15);
		scene.effects.superGlue(piston.west(), Direction.UP, true);
		scene.idle(10);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.world.modifyKineticSpeed(kinetics, f -> -f);
		scene.effects.rotationDirectionIndicator(shaft);
		scene.world.moveSection(contraption, util.vector.of(-2, 0, 0), 40);
		scene.idle(40);

		scene.world.destroyBlock(util.grid.at(0, 1, 2));
		scene.world.destroyBlock(util.grid.at(0, 2, 2));
		scene.idle(10);
		scene.overlay.showSelectionWithText(rose, 70)
			.text("Whenever Pistons stop moving, the moved structure reverts to blocks")
			.attachKeyFrame()
			.colored(PonderPalette.RED);
		scene.idle(80);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.world.modifyKineticSpeed(kinetics, f -> -f);
		scene.effects.rotationDirectionIndicator(shaft);
		scene.world.moveSection(contraption, util.vector.of(2, 0, 0), 40);
		scene.world.hideSection(rose, Direction.UP);
		scene.idle(50);

		scene.world.setBlock(util.grid.at(0, 1, 2), Blocks.ROSE_BUSH.defaultBlockState(), false);
		scene.world.setBlock(util.grid.at(0, 2, 2), Blocks.ROSE_BUSH.defaultBlockState()
			.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), false);
		scene.world.showIndependentSection(rose, Direction.DOWN);
		scene.overlay.showCenteredScrollInput(piston, Direction.UP, 60);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(piston), Pointing.DOWN).scroll()
			.withWrench(), 60);
		scene.overlay.showText(70)
			.pointAt(util.vector.topOf(piston))
			.placeNearTarget()
			.attachKeyFrame()
			.sharedText("behaviour_modify_wrench");
		scene.idle(80);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.world.modifyKineticSpeed(kinetics, f -> -f);
		scene.effects.rotationDirectionIndicator(shaft);
		scene.world.moveSection(contraption, util.vector.of(-2, 0, 0), 40);
		scene.idle(50);
		scene.overlay.showText(120)
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(util.grid.at(0, 1, 2), Direction.WEST))
			.placeNearTarget()
			.text("It can be configured never to revert to solid blocks, or only at the location it started at");

	}

}
