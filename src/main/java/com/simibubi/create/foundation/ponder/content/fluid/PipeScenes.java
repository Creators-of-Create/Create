package com.simibubi.create.foundation.ponder.content.fluid;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.fluids.PumpBlock;
import com.simibubi.create.content.contraptions.fluids.actors.ItemDrainTileEntity;
import com.simibubi.create.content.contraptions.fluids.pipes.AxisPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.content.PonderPalette;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class PipeScenes {

	public static void flow(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("fluid_pipe_flow", "Moving Fluids using Copper Pipes");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockState pipeState = AllBlocks.FLUID_PIPE.getDefaultState()
			.with(FluidPipeBlock.UP, false)
			.with(FluidPipeBlock.DOWN, false);

		scene.world.setBlock(util.grid.at(2, 1, 1), pipeState.with(FluidPipeBlock.NORTH, false)
			.with(FluidPipeBlock.SOUTH, false), false);
		scene.world.setBlock(util.grid.at(1, 1, 2), pipeState.with(FluidPipeBlock.WEST, false)
			.with(FluidPipeBlock.EAST, false), false);

		Selection largeCog = util.select.position(5, 0, 1);
		Selection kinetics = util.select.fromTo(5, 1, 0, 3, 1, 0);
		Selection tank = util.select.fromTo(4, 1, 2, 4, 2, 2);
		Selection tank2 = util.select.fromTo(0, 1, 3, 0, 2, 3);

		Selection strayPipes = util.select.fromTo(2, 1, 2, 2, 2, 2)
			.add(util.select.fromTo(1, 2, 2, 1, 3, 2));

		scene.world.showSection(tank, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(tank2, Direction.DOWN);
		FluidStack content = new FluidStack(Fluids.LAVA, 10000);
		scene.world.modifyTileEntity(util.grid.at(4, 1, 2), FluidTankTileEntity.class, te -> te.getTankInventory()
			.fill(content, FluidAction.EXECUTE));
		scene.idle(10);

		for (int i = 4; i >= 1; i--) {
			scene.world.showSection(util.select.position(i, 1, 1), i == 4 ? Direction.SOUTH : Direction.EAST);
			scene.idle(3);
		}

		scene.overlay.showText(60)
			.text("Fluid Pipes can connect two or more fluid sources and targets")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(1, 1, 1));

		for (int i = 2; i <= 3; i++) {
			scene.world.showSection(util.select.position(1, 1, i), Direction.NORTH);
			scene.idle(3);
		}

		scene.idle(60);

		scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(2, 1, 1), Pointing.DOWN).rightClick()
			.withWrench(), 40);
		scene.idle(7);
		scene.world.restoreBlocks(util.select.position(2, 1, 1));
		scene.overlay.showText(70)
			.attachKeyFrame()
			.pointAt(util.vector.centerOf(2, 1, 1))
			.placeNearTarget()
			.text("Using a wrench, a straight pipe segment can be given a window");
		scene.idle(40);

		scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(1, 1, 2), Pointing.DOWN).rightClick()
			.withWrench(), 10);
		scene.idle(7);
		scene.world.restoreBlocks(util.select.position(1, 1, 2));
		scene.idle(40);

		Vector3d center = util.vector.centerOf(2, 1, 2);
		AxisAlignedBB bb = new AxisAlignedBB(center, center).grow(1 / 6f);
		AxisAlignedBB bb1 = bb.offset(-0.5, 0, 0);
		AxisAlignedBB bb2 = bb.offset(0, 0, -0.5);

		scene.world.showSection(strayPipes, Direction.DOWN);
		scene.idle(10);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb1, bb, 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb, 1);
		scene.idle(1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb1, bb1, 50);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2, 50);
		scene.idle(10);
		scene.overlay.showText(55)
			.attachKeyFrame()
			.pointAt(util.vector.centerOf(2, 1, 2))
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.text("Windowed pipes will not connect to any other adjacent pipe segments");
		scene.idle(60);
		scene.world.hideSection(strayPipes, Direction.UP);
		scene.idle(10);

		BlockPos pumpPos = util.grid.at(3, 1, 1);
		scene.world.setBlock(pumpPos, AllBlocks.MECHANICAL_PUMP.getDefaultState()
			.with(PumpBlock.FACING, Direction.WEST), true);
		scene.idle(10);
		scene.world.showSection(largeCog, Direction.UP);
		scene.world.showSection(kinetics, Direction.SOUTH);
		scene.idle(10);
		scene.world.setKineticSpeed(util.select.position(pumpPos), 32);
		scene.world.propagatePipeChange(pumpPos);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(pumpPos))
			.placeNearTarget()
			.text("Powered by Mechanical Pumps, the Pipes can transport Fluids");
		scene.idle(85);
		scene.overlay.showSelectionWithText(tank, 40)
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.text("No fluid is being extracted at first");
		scene.idle(90);

		scene.overlay.showOutline(PonderPalette.GREEN, new Object(), tank, 100);
		scene.idle(5);
		scene.overlay.showOutline(PonderPalette.GREEN, new Object(), tank2, 100);
		scene.idle(5);
		scene.overlay.showText(100)
			.attachKeyFrame()
			.independent()
			.text("Once the flow connects them, the endpoints gradually transfer their contents");
		scene.overlay.showLine(PonderPalette.GREEN, util.vector.blockSurface(util.grid.at(4, 2, 2), Direction.WEST),
			util.vector.blockSurface(util.grid.at(0, 2, 3), Direction.EAST), 80);

		scene.world.multiplyKineticSpeed(util.select.everywhere(), 2);
		scene.world.propagatePipeChange(pumpPos);
		scene.effects.rotationSpeedIndicator(pumpPos);

		scene.idle(120);

		scene.overlay.showText(60)
			.text("Thus, the Pipe blocks themselves never 'physically' contain any fluid")
			.placeNearTarget()
			.pointAt(util.vector.topOf(1, 1, 1));
		scene.idle(50);
	}

	public static void interaction(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("fluid_pipe_interaction", "Draining and Filling fluid containers");
		scene.configureBasePlate(0, 1, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos pumpPos = util.grid.at(2, 1, 4);
		Selection largeCog = util.select.position(5, 0, 4);
		Selection kinetics = util.select.fromTo(5, 1, 5, 2, 1, 5);
		Selection pipes = util.select.fromTo(1, 1, 4, 3, 1, 3)
			.add(util.select.position(3, 1, 2));
		Selection tank = util.select.fromTo(4, 1, 3, 4, 2, 3);
		Selection drain = util.select.position(1, 1, 2);
		Selection basin = util.select.position(3, 1, 1);

		Selection waterSourceS = util.select.position(1, 1, 1);
		Selection waterTargetS = util.select.position(4, 1, 1);
		Selection waterTarget2S = util.select.fromTo(4, 0, 0, 4, 1, 0);

		scene.world.setKineticSpeed(util.select.position(pumpPos), 0);

		scene.world.showSection(pipes, Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(basin, Direction.SOUTH);
		scene.idle(5);
		scene.world.showSection(drain, Direction.SOUTH);
		scene.idle(5);
		scene.world.showSection(tank, Direction.WEST);

		scene.overlay.showText(60)
			.text("Endpoints of a pipe network can interact with a variety of blocks")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 3), Direction.NORTH));
		scene.idle(60);
		scene.world.showSection(largeCog, Direction.UP);
		scene.idle(5);
		scene.world.showSection(kinetics, Direction.NORTH);
		scene.idle(10);
		scene.world.setKineticSpeed(util.select.position(pumpPos), 64);
		BlockPos drainPos = util.grid.at(1, 1, 2);
		scene.world.modifyTileEntity(drainPos, ItemDrainTileEntity.class,
			te -> te.getBehaviour(SmartFluidTankBehaviour.TYPE)
				.allowInsertion()
				.getPrimaryHandler()
				.fill(new FluidStack(Fluids.WATER, 1500), FluidAction.EXECUTE));

		scene.idle(50);
		scene.overlay.showOutline(PonderPalette.MEDIUM, new Object(), drain, 40);
		scene.idle(5);
		scene.overlay.showOutline(PonderPalette.MEDIUM, new Object(), tank, 40);
		scene.idle(5);
		scene.overlay.showOutline(PonderPalette.MEDIUM, new Object(), basin, 40);
		scene.idle(5);

		scene.overlay.showText(60)
			.text("Any block with fluid storage capabilities can be filled or drained")
			.attachKeyFrame()
			.colored(PonderPalette.MEDIUM)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(drainPos, Direction.UP));
		scene.idle(100);

		scene.world.hideSection(drain, Direction.NORTH);
		scene.idle(5);
		scene.world.hideSection(tank, Direction.EAST);
		scene.idle(5);
		scene.world.setBlock(drainPos, Blocks.AIR.getDefaultState(), false);
		scene.world.propagatePipeChange(pumpPos);
		scene.world.hideSection(basin, Direction.NORTH);
		scene.idle(5);
		scene.world.setBlock(util.grid.at(3, 1, 1), Blocks.AIR.getDefaultState(), false);
		scene.idle(5);
		scene.world.setBlock(util.grid.at(3, 1, 3), AllBlocks.GLASS_FLUID_PIPE.getDefaultState()
			.with(AxisPipeBlock.AXIS, Axis.Z), false);
		scene.idle(10);
		scene.world.multiplyKineticSpeed(util.select.everywhere(), 2);
		scene.world.propagatePipeChange(pumpPos);
		ElementLink<WorldSectionElement> water = scene.world.showIndependentSection(waterSourceS, Direction.DOWN);
		scene.world.moveSection(water, util.vector.of(0, 0, 1), 0);
		scene.idle(10);
		scene.world.setBlock(drainPos, Blocks.WATER.getDefaultState(), false);
		scene.idle(20);

		scene.overlay.showText(60)
			.text("Source blocks right in front of an open end can be picked up...")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(drainPos, Direction.SOUTH));

		scene.idle(40);
		scene.world.setBlock(drainPos.north(), Blocks.AIR.getDefaultState(), false);
		scene.idle(40);
		ElementLink<WorldSectionElement> target = scene.world.showIndependentSection(waterTargetS, Direction.UP);
		scene.world.moveSection(target, util.vector.of(-1, 0, 0), 0);
		scene.idle(5);
		scene.world.showSectionAndMerge(waterTarget2S, Direction.UP, target);

		scene.overlay.showText(60)
			.text("...while spilling into empty spaces can create fluid sources")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 2), Direction.NORTH));

		scene.idle(80);
		scene.world.hideIndependentSection(target, Direction.DOWN);
		scene.idle(5);
		scene.world.setBlock(drainPos, Blocks.BEE_NEST.getDefaultState()
			.with(BeehiveBlock.HONEY_LEVEL, 5), false);
		scene.world.showSection(drain, Direction.DOWN);
		scene.world.setBlock(util.grid.at(3, 1, 2), AllBlocks.FLUID_TANK.getDefaultState(), false);
		scene.world.propagatePipeChange(pumpPos);
		scene.idle(15);

		scene.overlay.showText(60)
			.text("Pipes can also extract fluids from a handful of other blocks directly")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(drainPos));

		scene.idle(60);
		scene.world.setBlock(drainPos, Blocks.BEE_NEST.getDefaultState()
			.with(BeehiveBlock.HONEY_LEVEL, 0), false);
	}

	public static void encasing(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("encased_fluid_pipe", "Encasing Fluid Pipes");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);
		scene.world.showSection(util.select.position(2, 0, 5), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layer(1), Direction.DOWN);
		scene.idle(15);

		BlockState copperEncased = AllBlocks.ENCASED_FLUID_PIPE.getDefaultState()
			.with(FluidPipeBlock.SOUTH, true)
			.with(FluidPipeBlock.WEST, true);
		ItemStack casingItem = AllBlocks.COPPER_CASING.asStack();

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(3, 1, 1), Pointing.DOWN).rightClick()
			.withItem(casingItem), 60);
		scene.idle(7);
		scene.world.setBlock(util.grid.at(3, 1, 1), copperEncased, true);
		scene.idle(10);

		scene.overlay.showText(60)
			.placeNearTarget()
			.text("Copper Casing can be used to decorate Fluid Pipes")
			.attachKeyFrame()
			.pointAt(util.vector.topOf(3, 1, 1));

		scene.idle(70);
		scene.world.destroyBlock(util.grid.at(2, 1, 1));
		scene.world.modifyBlock(util.grid.at(1, 1, 1), s -> s.with(FluidPipeBlock.EAST, false)
			.with(FluidPipeBlock.NORTH, true), false);
		scene.idle(5);

		scene.overlay.showLine(PonderPalette.RED, util.vector.of(1.5, 1.75, 1), util.vector.of(1.5, 1.75, 2), 80);
		scene.idle(5);
		scene.addKeyframe();
		scene.overlay.showLine(PonderPalette.GREEN, util.vector.of(3.5, 2, 1.5), util.vector.of(3.5, 2, 2), 80);
		scene.overlay.showLine(PonderPalette.GREEN, util.vector.of(3, 2, 1.5), util.vector.of(3.5, 2, 1.5), 80);

		scene.idle(25);
		scene.overlay.showText(60)
			.placeNearTarget()
			.text("Aside from being conceiled, Encased Pipes are locked into their connectivity state")
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 1), Direction.WEST));

		scene.idle(70);
		BlockState defaultState = AllBlocks.FLUID_PIPE.getDefaultState();
		for (BooleanProperty booleanProperty : FluidPipeBlock.FACING_TO_PROPERTY_MAP.values())
			defaultState = defaultState.with(booleanProperty, false);

		scene.world.setBlock(util.grid.at(3, 2, 1), defaultState.with(FluidPipeBlock.EAST, true)
			.with(FluidPipeBlock.WEST, true), false);
		scene.world.setBlock(util.grid.at(1, 2, 1), defaultState.with(FluidPipeBlock.UP, true)
			.with(FluidPipeBlock.DOWN, true), false);
		scene.world.showSection(util.select.layer(2), Direction.DOWN);
		scene.idle(10);
		scene.world.modifyBlock(util.grid.at(1, 1, 1), s -> s.with(FluidPipeBlock.UP, true)
			.with(FluidPipeBlock.NORTH, false), false);
		scene.idle(20);

		scene.overlay.showText(60)
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.text("It will no longer react to any neighbouring blocks being added or removed")
			.attachKeyFrame()
			.pointAt(util.vector.centerOf(3, 2, 1));
		scene.idle(20);
	}

	public static void valve(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("valve_pipe", "Controlling Fluid flow using Valves");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);

		/*
		 * Valve pipes propagate flows in a straight line
		 * 
		 * When given Rotational Force in the closing direction, the valve will stop the
		 * fluid flow
		 * 
		 * It can be re-opened by reversing the input rotation
		 */
	}

	public static void smart(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("smart_pipe", "Controlling Fluid flow using Smart Pipes");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);

		/*
		 * Smart pipes propagate flows in a straight line
		 * 
		 * When placed directly at the source, they can specify the type of fluid to
		 * extract
		 * 
		 * Simply Right-Click their filter slot with any item containing the desired
		 * fluid
		 * 
		 * When placed further down a pipe network, smart pipes will only let matching
		 * fluids continue past
		 * 
		 * In this configuration, their filter has no impact on whether a fluid can
		 * enter the pipe network
		 */
	}

}
