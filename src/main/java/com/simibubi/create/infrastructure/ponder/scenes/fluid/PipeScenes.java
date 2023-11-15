package com.simibubi.create.infrastructure.ponder.scenes.fluid;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import com.simibubi.create.content.fluids.pipes.AxisPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.SmartFluidPipeBlockEntity;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlock;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class PipeScenes {

	public static void flow(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("fluid_pipe_flow", "Moving Fluids using Copper Pipes");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockState pipeState = AllBlocks.FLUID_PIPE.getDefaultState()
			.setValue(FluidPipeBlock.UP, false)
			.setValue(FluidPipeBlock.DOWN, false);

		scene.world().setBlock(util.grid().at(2, 1, 1), pipeState.setValue(FluidPipeBlock.NORTH, false)
			.setValue(FluidPipeBlock.SOUTH, false), false);
		scene.world().setBlock(util.grid().at(1, 1, 2), pipeState.setValue(FluidPipeBlock.WEST, false)
			.setValue(FluidPipeBlock.EAST, false), false);

		Selection largeCog = util.select().position(5, 0, 1);
		Selection kinetics = util.select().fromTo(5, 1, 0, 3, 1, 0);
		Selection tank = util.select().fromTo(4, 1, 2, 4, 2, 2);
		Selection tank2 = util.select().fromTo(0, 1, 3, 0, 2, 3);

		Selection strayPipes = util.select().fromTo(2, 1, 2, 2, 2, 2)
			.add(util.select().fromTo(1, 2, 2, 1, 3, 2));

		scene.world().showSection(tank, Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(tank2, Direction.DOWN);
		FluidStack content = new FluidStack(Fluids.LAVA, 10000);
		scene.world().modifyBlockEntity(util.grid().at(4, 1, 2), FluidTankBlockEntity.class, be -> be.getTankInventory()
			.fill(content, FluidAction.EXECUTE));
		scene.idle(10);

		for (int i = 4; i >= 1; i--) {
			scene.world().showSection(util.select().position(i, 1, 1), i == 4 ? Direction.SOUTH : Direction.EAST);
			scene.idle(3);
		}

		scene.overlay().showText(60)
			.text("Fluid Pipes can connect two or more fluid sources and targets")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().topOf(1, 1, 1));

		for (int i = 2; i <= 3; i++) {
			scene.world().showSection(util.select().position(1, 1, i), Direction.NORTH);
			scene.idle(3);
		}

		scene.idle(60);

		scene.overlay().showControls(util.vector().centerOf(2, 1, 1), Pointing.DOWN, 40).rightClick()
			.withItem(AllItems.WRENCH.asStack());
		scene.idle(7);
		scene.world().restoreBlocks(util.select().position(2, 1, 1));
		scene.overlay().showText(70)
			.attachKeyFrame()
			.pointAt(util.vector().centerOf(2, 1, 1))
			.placeNearTarget()
			.text("Using a wrench, a straight pipe segment can be given a window");
		scene.idle(40);

		scene.overlay().showControls(util.vector().centerOf(1, 1, 2), Pointing.DOWN, 10).rightClick()
			.withItem(AllItems.WRENCH.asStack());
		scene.idle(7);
		scene.world().restoreBlocks(util.select().position(1, 1, 2));
		scene.idle(40);

		Vec3 center = util.vector().centerOf(2, 1, 2);
		AABB bb = new AABB(center, center).inflate(1 / 6f);
		AABB bb1 = bb.move(-0.5, 0, 0);
		AABB bb2 = bb.move(0, 0, -0.5);

		scene.world().showSection(strayPipes, Direction.DOWN);
		scene.idle(10);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb1, bb, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb, 1);
		scene.idle(1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb1, bb1, 50);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2, 50);
		scene.idle(10);
		scene.overlay().showText(55)
			.attachKeyFrame()
			.pointAt(util.vector().centerOf(2, 1, 2))
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.text("Windowed pipes will not connect to any other adjacent pipe segments");
		scene.idle(60);
		scene.world().hideSection(strayPipes, Direction.UP);
		scene.idle(10);

		BlockPos pumpPos = util.grid().at(3, 1, 1);
		scene.world().setBlock(pumpPos, AllBlocks.MECHANICAL_PUMP.getDefaultState()
			.setValue(PumpBlock.FACING, Direction.WEST), true);
		scene.idle(10);
		scene.world().showSection(largeCog, Direction.UP);
		scene.world().showSection(kinetics, Direction.SOUTH);
		scene.idle(10);
		scene.world().multiplyKineticSpeed(util.select().everywhere(), 0.25f);
		scene.world().setKineticSpeed(util.select().position(pumpPos), 8);
		scene.world().propagatePipeChange(pumpPos);

		scene.overlay().showText(70)
			.attachKeyFrame()
			.pointAt(util.vector().topOf(pumpPos))
			.placeNearTarget()
			.text("Powered by Mechanical Pumps, the Pipes can transport Fluids");
		scene.idle(85);
		scene.overlay().showOutlineWithText(tank, 40)
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.text("No fluid is being extracted at first");
		scene.idle(70);

		scene.overlay().showOutline(PonderPalette.GREEN, new Object(), tank, 100);
		scene.idle(5);
		scene.overlay().showOutline(PonderPalette.GREEN, new Object(), tank2, 100);
		scene.idle(5);
		scene.overlay().showText(100)
			.attachKeyFrame()
			.independent()
			.text("Once the flow connects them, the endpoints gradually transfer their contents");
		scene.overlay().showLine(PonderPalette.GREEN, util.vector().blockSurface(util.grid().at(4, 2, 2), Direction.WEST),
								 util.vector().blockSurface(util.grid().at(0, 2, 3), Direction.EAST), 80);

		scene.world().multiplyKineticSpeed(util.select().everywhere(), 2);
		scene.world().propagatePipeChange(pumpPos);
		scene.effects().rotationSpeedIndicator(pumpPos);

		scene.idle(120);

		scene.overlay().showText(60)
			.text("Thus, the Pipe blocks themselves never 'physically' contain any fluid")
			.placeNearTarget()
			.pointAt(util.vector().topOf(1, 1, 1));
		scene.idle(50);
	}

	public static void interaction(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("fluid_pipe_interaction", "Draining and Filling fluid containers");
		scene.configureBasePlate(0, 1, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos pumpPos = util.grid().at(2, 1, 4);
		Selection largeCog = util.select().position(5, 0, 4);
		Selection kinetics = util.select().fromTo(5, 1, 5, 2, 1, 5);
		Selection pipes = util.select().fromTo(1, 1, 4, 3, 1, 3)
			.add(util.select().position(3, 1, 2));
		Selection tank = util.select().fromTo(4, 1, 3, 4, 2, 3);
		Selection drain = util.select().position(1, 1, 2);
		Selection basin = util.select().position(3, 1, 1);

		Selection waterSourceS = util.select().position(1, 1, 1);
		Selection waterTargetS = util.select().position(4, 1, 1);
		Selection waterTarget2S = util.select().fromTo(4, 0, 0, 4, 1, 0);

		scene.world().setKineticSpeed(util.select().position(pumpPos), 0);

		scene.world().showSection(pipes, Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(basin, Direction.SOUTH);
		scene.idle(5);
		scene.world().showSection(drain, Direction.SOUTH);
		scene.idle(5);
		scene.world().showSection(tank, Direction.WEST);

		scene.overlay().showText(60)
			.text("Endpoints of a pipe network can interact with a variety of blocks")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().blockSurface(util.grid().at(1, 1, 3), Direction.NORTH));
		scene.idle(60);
		scene.world().showSection(largeCog, Direction.UP);
		scene.idle(5);
		scene.world().showSection(kinetics, Direction.NORTH);
		scene.idle(10);
		scene.world().multiplyKineticSpeed(util.select().everywhere(), 0.5f);
		scene.world().setKineticSpeed(util.select().position(pumpPos), 32);
		BlockPos drainPos = util.grid().at(1, 1, 2);
		scene.world().modifyBlockEntity(drainPos, ItemDrainBlockEntity.class,
			be -> be.getBehaviour(SmartFluidTankBehaviour.TYPE)
				.allowInsertion()
				.getPrimaryHandler()
				.fill(new FluidStack(Fluids.WATER, 1500), FluidAction.EXECUTE));

		scene.idle(50);
		scene.overlay().showOutline(PonderPalette.MEDIUM, new Object(), drain, 40);
		scene.idle(5);
		scene.overlay().showOutline(PonderPalette.MEDIUM, new Object(), tank, 40);
		scene.idle(5);
		scene.overlay().showOutline(PonderPalette.MEDIUM, new Object(), basin, 40);
		scene.idle(5);

		scene.overlay().showText(60)
			.text("Any block with fluid storage capabilities can be filled or drained")
			.attachKeyFrame()
			.colored(PonderPalette.MEDIUM)
			.placeNearTarget()
			.pointAt(util.vector().blockSurface(drainPos, Direction.UP));
		scene.idle(100);

		scene.world().hideSection(drain, Direction.NORTH);
		scene.idle(5);
		scene.world().hideSection(tank, Direction.EAST);
		scene.idle(5);
		scene.world().setBlock(drainPos, Blocks.AIR.defaultBlockState(), false);
		scene.world().propagatePipeChange(pumpPos);
		scene.world().hideSection(basin, Direction.NORTH);
		scene.idle(5);
		scene.world().setBlock(util.grid().at(3, 1, 1), Blocks.AIR.defaultBlockState(), false);
		scene.idle(5);
		scene.world().setBlock(util.grid().at(3, 1, 3), AllBlocks.GLASS_FLUID_PIPE.getDefaultState()
			.setValue(AxisPipeBlock.AXIS, Axis.Z), false);
		scene.idle(10);
//		scene.world.multiplyKineticSpeed(util.select.everywhere(), 2);
		scene.world().propagatePipeChange(pumpPos);
		ElementLink<WorldSectionElement> water = scene.world().showIndependentSection(waterSourceS, Direction.DOWN);
		scene.world().moveSection(water, util.vector().of(0, 0, 1), 0);
		scene.idle(10);
		scene.world().setBlock(drainPos, Blocks.WATER.defaultBlockState(), false);
		scene.idle(20);

		scene.overlay().showText(60)
			.text("Source blocks right in front of an open end can be picked up...")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().blockSurface(drainPos, Direction.SOUTH));

		scene.idle(40);
		scene.world().setBlock(drainPos.north(), Blocks.AIR.defaultBlockState(), false);
		scene.idle(40);
		ElementLink<WorldSectionElement> target = scene.world().showIndependentSection(waterTargetS, Direction.UP);
		scene.world().moveSection(target, util.vector().of(-1, 0, 0), 0);
		scene.idle(5);
		scene.world().showSectionAndMerge(waterTarget2S, Direction.UP, target);

		scene.overlay().showText(60)
			.text("...while spilling into empty spaces can create fluid sources")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().blockSurface(util.grid().at(3, 1, 2), Direction.NORTH));

		scene.idle(80);
		scene.world().hideIndependentSection(target, Direction.DOWN);
		scene.idle(5);
		scene.world().setBlock(drainPos, Blocks.BEE_NEST.defaultBlockState()
			.setValue(BeehiveBlock.HONEY_LEVEL, 5), false);
		scene.world().showSection(drain, Direction.DOWN);
		scene.world().setBlock(util.grid().at(3, 1, 2), AllBlocks.FLUID_TANK.getDefaultState(), false);
		scene.world().propagatePipeChange(pumpPos);
		scene.idle(15);

		scene.overlay().showText(60)
			.text("Pipes can also extract fluids from a handful of other blocks directly")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().topOf(drainPos));

		scene.idle(60);
		scene.world().setBlock(drainPos, Blocks.BEE_NEST.defaultBlockState()
			.setValue(BeehiveBlock.HONEY_LEVEL, 0), false);
	}

	public static void encasing(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("encased_fluid_pipe", "Encasing Fluid Pipes");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);
		scene.world().showSection(util.select().position(2, 0, 5), Direction.UP);
		scene.idle(5);
		scene.world().showSection(util.select().layer(1), Direction.DOWN);
		scene.idle(15);

		BlockState copperEncased = AllBlocks.ENCASED_FLUID_PIPE.getDefaultState()
			.setValue(FluidPipeBlock.SOUTH, true)
			.setValue(FluidPipeBlock.WEST, true);
		ItemStack casingItem = AllBlocks.COPPER_CASING.asStack();

		scene.overlay().showControls(util.vector().topOf(3, 1, 1), Pointing.DOWN, 60).rightClick()
			.withItem(casingItem);
		scene.idle(7);
		scene.world().setBlock(util.grid().at(3, 1, 1), copperEncased, true);
		scene.idle(10);

		scene.overlay().showText(60)
			.placeNearTarget()
			.text("Copper Casing can be used to decorate Fluid Pipes")
			.attachKeyFrame()
			.pointAt(util.vector().topOf(3, 1, 1));

		scene.idle(70);
		scene.world().destroyBlock(util.grid().at(2, 1, 1));
		scene.world().modifyBlock(util.grid().at(1, 1, 1), s -> s.setValue(FluidPipeBlock.EAST, false)
			.setValue(FluidPipeBlock.NORTH, true), false);
		scene.idle(5);

		scene.overlay().showLine(PonderPalette.RED, util.vector().of(1.5, 1.75, 1), util.vector().of(1.5, 1.75, 2), 80);
		scene.idle(5);
		scene.addKeyframe();
		scene.overlay().showLine(PonderPalette.GREEN, util.vector().of(3.5, 2, 1.5), util.vector().of(3.5, 2, 2), 80);
		scene.overlay().showLine(PonderPalette.GREEN, util.vector().of(3, 2, 1.5), util.vector().of(3.5, 2, 1.5), 80);

		scene.idle(25);
		scene.overlay().showText(60)
			.placeNearTarget()
			.text("Aside from being concealed, Encased Pipes are locked into their connectivity state")
			.pointAt(util.vector().blockSurface(util.grid().at(3, 1, 1), Direction.WEST));

		scene.idle(70);
		BlockState defaultState = AllBlocks.FLUID_PIPE.getDefaultState();
		for (BooleanProperty booleanProperty : FluidPipeBlock.PROPERTY_BY_DIRECTION.values())
			defaultState = defaultState.setValue(booleanProperty, false);

		scene.world().setBlock(util.grid().at(3, 2, 1), defaultState.setValue(FluidPipeBlock.EAST, true)
			.setValue(FluidPipeBlock.WEST, true), false);
		scene.world().setBlock(util.grid().at(1, 2, 1), defaultState.setValue(FluidPipeBlock.UP, true)
			.setValue(FluidPipeBlock.DOWN, true), false);
		scene.world().showSection(util.select().layer(2), Direction.DOWN);
		scene.idle(10);
		scene.world().modifyBlock(util.grid().at(1, 1, 1), s -> s.setValue(FluidPipeBlock.UP, true)
			.setValue(FluidPipeBlock.NORTH, false), false);
		scene.idle(20);

		scene.overlay().showText(60)
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.text("It will no longer react to any neighbouring blocks being added or removed")
			.attachKeyFrame()
			.pointAt(util.vector().centerOf(3, 2, 1));
		scene.idle(20);
	}

	public static void valve(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("valve_pipe", "Controlling Fluid flow using Valves");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		Selection cogs = util.select().fromTo(5, 0, 2, 5, 1, 2);
		Selection tank1 = util.select().fromTo(3, 1, 3, 3, 2, 3);
		Selection tank2 = util.select().fromTo(1, 1, 3, 1, 2, 3);
		BlockPos valvePos = util.grid().at(2, 1, 1);
		BlockPos handlePos = util.grid().at(2, 2, 1);
		BlockPos pumpPos = util.grid().at(4, 1, 2);
		Selection pipes1 = util.select().fromTo(4, 1, 3, 4, 1, 1);
		Selection pipes2 = util.select().fromTo(3, 1, 1, 1, 1, 1);
		Selection pipes3 = util.select().fromTo(0, 1, 1, 0, 1, 3);

		scene.world().setKineticSpeed(pipes1, 0);
		scene.world().propagatePipeChange(pumpPos);
		scene.world().setBlock(valvePos, AllBlocks.FLUID_PIPE.get()
			.getAxisState(Axis.X), false);
		scene.world().setBlock(util.grid().at(3, 1, 1), Blocks.AIR.defaultBlockState(), false);
		scene.world().setBlock(util.grid().at(3, 1, 1), AllBlocks.GLASS_FLUID_PIPE.getDefaultState()
			.setValue(GlassFluidPipeBlock.AXIS, Axis.X), false);

		scene.idle(5);
		scene.world().showSection(tank1, Direction.NORTH);
		scene.idle(5);
		scene.world().showSection(tank2, Direction.NORTH);
		scene.idle(10);
		scene.world().showSection(pipes1, Direction.WEST);
		scene.idle(5);
		scene.world().showSection(pipes2, Direction.SOUTH);
		scene.idle(5);
		scene.world().showSection(pipes3, Direction.EAST);
		scene.idle(15);

		scene.world().destroyBlock(valvePos);
		scene.world().restoreBlocks(util.select().position(valvePos));

		scene.overlay().showText(60)
			.placeNearTarget()
			.text("Valve pipes help control fluids propagating through pipe networks")
			.attachKeyFrame()
			.pointAt(util.vector().blockSurface(valvePos, Direction.WEST));
		scene.idle(75);

		scene.world().showSection(cogs, Direction.WEST);
		scene.idle(10);
		scene.world().setKineticSpeed(util.select().position(pumpPos), 64);
		scene.world().propagatePipeChange(pumpPos);

		scene.overlay().showText(60)
			.placeNearTarget()
			.text("Their shaft input controls whether fluid is currently allowed through")
			.attachKeyFrame()
			.pointAt(util.vector().topOf(valvePos));
		scene.idle(60);
		ElementLink<WorldSectionElement> handleLink =
			scene.world().showIndependentSection(util.select().position(handlePos), Direction.DOWN);
		scene.idle(15);

		Selection valveKinetics = util.select().fromTo(2, 1, 1, 2, 2, 1);
		scene.world().setKineticSpeed(valveKinetics, 16);
		scene.world().rotateSection(handleLink, 0, 90, 0, 22);
		scene.effects().rotationSpeedIndicator(handlePos);
		scene.world().modifyBlockEntity(valvePos, FluidValveBlockEntity.class, be -> be.onSpeedChanged(0));
		scene.idle(22);
		scene.world().modifyBlock(valvePos, s -> s.setValue(FluidValveBlock.ENABLED, true), false);
		scene.effects().indicateSuccess(valvePos);
		scene.idle(5);
		scene.world().setKineticSpeed(valveKinetics, 0);

		scene.overlay().showText(60)
			.placeNearTarget()
			.text("Given Rotational Force in the opening direction, the valve will open up")
			.attachKeyFrame()
			.pointAt(util.vector().blockSurface(valvePos, Direction.NORTH));
		scene.idle(90);

		scene.overlay().showText(50)
			.placeNearTarget()
			.text("It can be closed again by reversing the input rotation")
			.attachKeyFrame()
			.pointAt(util.vector().blockSurface(valvePos, Direction.NORTH));
		scene.idle(40);

		scene.world().setKineticSpeed(valveKinetics, -16);
		scene.world().rotateSection(handleLink, 0, -90, 0, 22);
		scene.effects().rotationSpeedIndicator(handlePos);
		scene.world().modifyBlockEntity(valvePos, FluidValveBlockEntity.class, be -> be.onSpeedChanged(0));
		scene.idle(22);
		scene.world().modifyBlock(valvePos, s -> s.setValue(FluidValveBlock.ENABLED, false), false);
		scene.effects().indicateRedstone(valvePos);
		scene.world().propagatePipeChange(pumpPos);
		scene.idle(5);
		scene.world().setKineticSpeed(valveKinetics, 0);
	}

	public static void smart(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("smart_pipe", "Controlling Fluid flow using Smart Pipes");
		scene.configureBasePlate(1, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		Selection tank1 = util.select().fromTo(4, 1, 3, 4, 2, 3);
		Selection tank2 = util.select().fromTo(4, 1, 4, 4, 2, 4);
		Selection additionalPipes = util.select().fromTo(3, 1, 4, 1, 1, 4);
		Selection mainPipes = util.select().fromTo(3, 1, 3, 1, 1, 1);
		Selection kinetics1 = util.select().fromTo(0, 0, 2, 0, 0, 5);
		Selection kinetics2 = util.select().position(1, 0, 5);
		BlockPos basinPos = util.grid().at(4, 1, 1);
		BlockPos pumpPos = util.grid().at(1, 1, 2);
		Selection pump = util.select().position(1, 1, 2);
		Selection basin = util.select().position(basinPos);
		BlockPos smartPos = util.grid().at(3, 1, 1);

		scene.world().modifyBlockEntity(basinPos, BasinBlockEntity.class,
			be -> be.getCapability(ForgeCapabilities.FLUID_HANDLER)
				.ifPresent(fh -> fh.fill(new FluidStack(ForgeMod.MILK.get(), 1000), FluidAction.EXECUTE)));

		scene.world().setBlock(util.grid().at(3, 1, 3), AllBlocks.FLUID_PIPE.get()
			.getAxisState(Axis.X), false);
		scene.world().setBlock(smartPos, AllBlocks.FLUID_PIPE.get()
			.getAxisState(Axis.X), false);
		scene.world().setBlock(util.grid().at(2, 1, 3), AllBlocks.GLASS_FLUID_PIPE.getDefaultState()
			.setValue(GlassFluidPipeBlock.AXIS, Axis.X), false);
		scene.world().setBlock(util.grid().at(1, 1, 3), AllBlocks.FLUID_PIPE.get()
			.getAxisState(Axis.X)
			.setValue(FluidPipeBlock.NORTH, true)
			.setValue(FluidPipeBlock.WEST, false), false);

		scene.world().showSection(basin, Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(tank1, Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(mainPipes, Direction.EAST);
		scene.idle(15);

		scene.world().destroyBlock(smartPos);
		scene.world().restoreBlocks(util.select().position(smartPos));

		Vec3 filterVec = util.vector().topOf(smartPos)
			.subtract(0.25, 0, 0);
		scene.overlay().showText(50)
			.placeNearTarget()
			.text("Smart pipes can help control flows by fluid type")
			.pointAt(filterVec);
		scene.idle(60);

		scene.overlay().showOutlineWithText(util.select().position(basinPos), 80)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.text("When placed directly at the source, they can specify the type of fluid to extract")
			.attachKeyFrame()
			.pointAt(filterVec);
		scene.idle(90);

		FluidStack chocolate = new FluidStack(FluidHelper.convertToStill(AllFluids.CHOCOLATE.get()), 1000);
		ItemStack bucket = AllFluids.CHOCOLATE.get()
			.getFluidType()
			.getBucket(chocolate);
		ItemStack milkBucket = new ItemStack(Items.MILK_BUCKET);
		scene.overlay().showControls(filterVec, Pointing.DOWN, 80).rightClick()
			.withItem(bucket);
		scene.idle(7);
		scene.world().setFilterData(util.select().position(3, 1, 1), SmartFluidPipeBlockEntity.class, bucket);
		scene.idle(10);
		scene.overlay().showText(60)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Simply Right-Click their filter slot with any item containing the desired fluid")
			.pointAt(filterVec);
		scene.idle(50);

		scene.world().showSection(kinetics2, Direction.WEST);
		scene.world().setKineticSpeed(kinetics2, 24);
		scene.idle(5);
		scene.world().showSection(kinetics1, Direction.EAST);
		scene.world().setKineticSpeed(kinetics1, -24);
		scene.idle(10);
		scene.world().setKineticSpeed(pump, 48);
		scene.world().propagatePipeChange(pumpPos);
		scene.idle(100);
		scene.world().setKineticSpeed(util.select().everywhere(), 0);
		scene.world().propagatePipeChange(pumpPos);
		scene.idle(15);
		scene.world().showSection(tank2, Direction.DOWN);
		scene.world().showSection(additionalPipes, Direction.NORTH);
		scene.world().setBlock(util.grid().at(3, 1, 1), AllBlocks.FLUID_PIPE.get()
			.getAxisState(Axis.X), true);
		scene.idle(10);
		for (int i = 0; i < 3; i++) {
			BlockPos pos = util.grid().at(1 + i, 1, 3);
			scene.world().destroyBlock(pos);
			scene.world().restoreBlocks(util.select().position(pos));
			scene.idle(2);
		}
		scene.idle(15);
		scene.world().modifyBlockEntity(basinPos, BasinBlockEntity.class,
			be -> be.getCapability(ForgeCapabilities.FLUID_HANDLER)
				.ifPresent(fh -> fh.fill(chocolate, FluidAction.EXECUTE)));
		scene.idle(10);

		scene.overlay().showText(80)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.text("When placed further down a pipe network, smart pipes will only let matching fluids continue")
			.attachKeyFrame()
			.pointAt(filterVec.add(-1, 0, 2));
		scene.idle(90);

		scene.overlay().showControls(filterVec.add(-1, 0, 3), Pointing.DOWN, 30).rightClick()
			.withItem(milkBucket);
		scene.idle(7);
		scene.world().setFilterData(util.select().position(2, 1, 4), SmartFluidPipeBlockEntity.class, milkBucket);
		scene.idle(30);

		scene.overlay().showControls(filterVec.add(-1, 0, 2), Pointing.DOWN, 30).rightClick()
			.withItem(bucket);
		scene.idle(7);
		scene.world().setFilterData(util.select().position(2, 1, 3), SmartFluidPipeBlockEntity.class, bucket);
		scene.idle(30);

		scene.world().setKineticSpeed(kinetics2, 24);
		scene.world().setKineticSpeed(kinetics1, -24);
		scene.world().setKineticSpeed(pump, 48);
		scene.world().propagatePipeChange(pumpPos);
		scene.effects().rotationSpeedIndicator(pumpPos);
		scene.idle(40);
	}

}
