package com.simibubi.create.infrastructure.ponder.scenes.fluid;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.ElementLink;
import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class PumpScenes {

	public static void flow(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("mechanical_pump_flow", "Fluid Transportation using Mechanical Pumps");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.world().multiplyKineticSpeed(util.select().everywhere(), -1);
		scene.idle(5);

		BlockPos pumpPos = util.grid().at(2, 1, 1);
		Selection tank1 = util.select().fromTo(0, 2, 3, 0, 1, 3);
		Selection tank2 = util.select().fromTo(4, 2, 3, 4, 1, 3);
		Selection pipes = util.select().fromTo(3, 1, 3, 1, 1, 1);
		Selection largeCog = util.select().position(5, 0, 1);
		Selection kinetics = util.select().fromTo(5, 1, 0, 2, 1, 0);
		BlockPos leverPos = util.grid().at(4, 2, 0);
		Selection pump = util.select().position(pumpPos);

		scene.world().setBlock(pumpPos, AllBlocks.FLUID_PIPE.get()
			.getAxisState(Axis.X), false);

		scene.world().showSection(tank1, Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(tank2, Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(pipes, Direction.NORTH);
		scene.idle(15);

		scene.world().destroyBlock(pumpPos);
		scene.world().restoreBlocks(pump);
		scene.world().setKineticSpeed(pump, 0);

		scene.idle(15);

		scene.overlay().showText(60)
			.text("Mechanical Pumps govern the flow of their attached pipe networks")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().topOf(pumpPos));

		scene.idle(70);
		scene.world().showSection(largeCog, Direction.UP);
		scene.idle(5);
		scene.world().showSection(kinetics, Direction.SOUTH);
		scene.world().showSection(util.select().position(leverPos), Direction.SOUTH);
		scene.idle(10);
		scene.world().setKineticSpeed(pump, 64);
		scene.world().propagatePipeChange(pumpPos);
		scene.effects().rotationDirectionIndicator(pumpPos.north());
		scene.idle(15);

		scene.overlay().showText(60)
			.text("Their arrow indicates the direction of flow")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().topOf(pumpPos)
				.subtract(0.5f, 0.125f, 0));

		AABB bb1 = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(.25, .25, 0)
			.move(0, 0, .25);
		AABB bb2 = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(.25, .25, 1.25);
		scene.idle(65);

		Object in = new Object();
		Object out = new Object();

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, in, bb1.move(util.vector().centerOf(3, 1, 3)), 3);
		scene.idle(2);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, in, bb2.move(util.vector().centerOf(3, 1, 2)), 50);
		scene.idle(10);

		scene.overlay().showText(50)
			.text("The network behind is now pulling fluids...")
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.INPUT)
			.pointAt(util.vector().centerOf(3, 1, 2));

		scene.idle(60);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, out, bb1.move(util.vector().centerOf(1, 1, 1)
			.add(0, 0, -.5)), 3);
		scene.idle(2);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, out, bb2.move(util.vector().centerOf(1, 1, 2)), 50);
		scene.idle(10);

		scene.overlay().showText(50)
			.text("...while the network in front is transferring it outward")
			.placeNearTarget()
			.colored(PonderPalette.OUTPUT)
			.pointAt(util.vector().centerOf(1, 1, 2));

		scene.idle(70);
		scene.world().toggleRedstonePower(util.select().fromTo(4, 2, 0, 4, 1, 0));
		scene.effects().indicateRedstone(leverPos);
		scene.world().multiplyKineticSpeed(util.select().fromTo(3, 1, 0, 2, 1, 1), -1);
		scene.effects().rotationDirectionIndicator(pumpPos.north());
		scene.world().propagatePipeChange(pumpPos);
		scene.idle(15);

		scene.overlay().showText(60)
			.text("The pumps direction is unaffected by the input rotation")
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector().topOf(pumpPos)
				.subtract(0.5f, 0.125f, 0));

		scene.idle(25);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, in, new AABB(leverPos.below()), 30);
		scene.idle(45);

		scene.overlay().showControls(new InputWindowElement(util.vector().topOf(pumpPos), Pointing.DOWN).rightClick()
			.withItem(AllItems.WRENCH.asStack()), 40);
		scene.idle(7);
		scene.world().modifyBlock(pumpPos, s -> s.setValue(PumpBlock.FACING, Direction.EAST), true);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.pointAt(util.vector().centerOf(2, 1, 1))
			.placeNearTarget()
			.text("Instead, a Wrench can be used to reverse the direction");
		scene.world().propagatePipeChange(pumpPos);
		scene.idle(40);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, in, bb1.move(util.vector().centerOf(3, 1, 3)), 3);
		scene.idle(2);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, in, bb2.move(util.vector().centerOf(3, 1, 2)), 30);
		scene.idle(15);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, out, bb1.move(util.vector().centerOf(1, 1, 1)
			.add(0, 0, -.5)), 3);
		scene.idle(2);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, out, bb2.move(util.vector().centerOf(1, 1, 2)), 30);
		scene.idle(25);

	}

	public static void speed(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("mechanical_pump_speed", "Throughput of Mechanical Pumps");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);
//		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);

		Selection largeCog = util.select().position(5, 0, 3);
		Selection cogs = util.select().fromTo(5, 1, 4, 2, 1, 4)
			.add(util.select().position(2, 1, 3));
		BlockPos pumpPos = util.grid().at(2, 1, 2);
		Selection pump = util.select().position(pumpPos);
		Selection tank1 = util.select().fromTo(4, 1, 2, 4, 2, 2);
		Selection tank2 = util.select().fromTo(0, 1, 2, 0, 2, 2);
		Selection megapipe1 = util.select().fromTo(0, 3, 5, 1, 4, 2);
		Selection megapipe2 = util.select().fromTo(3, 3, 1, 5, 6, 2);

		scene.world().modifyBlock(pumpPos, s -> s.setValue(PumpBlock.FACING, Direction.WEST), false);
		scene.world().modifyBlockEntity(util.grid().at(0, 1, 2), FluidTankBlockEntity.class, be -> be.getTankInventory()
			.drain(3000, FluidAction.EXECUTE));
		scene.world().multiplyKineticSpeed(util.select().everywhere(), 0.125f);

		BlockPos east = pumpPos.east();
		scene.world().setBlock(east, Blocks.AIR.defaultBlockState(), false);
		scene.world().setBlock(east, AllBlocks.GLASS_FLUID_PIPE.getDefaultState()
			.setValue(GlassFluidPipeBlock.AXIS, Axis.X), false);

		scene.world().setBlock(pumpPos.south(), AllBlocks.COGWHEEL.getDefaultState()
			.setValue(CogWheelBlock.AXIS, Axis.X), false);
		Selection southPump = util.select().position(pumpPos.south());
		scene.world().setKineticSpeed(southPump, 4);

		scene.world().setKineticSpeed(pump, 0);
		scene.world().showSection(pump, Direction.DOWN);
		scene.idle(10);
		ElementLink<WorldSectionElement> mp1 = scene.world().showIndependentSection(megapipe1, Direction.EAST);
		scene.world().moveSection(mp1, util.vector().of(0, -3, 0), 0);
		scene.idle(5);
		ElementLink<WorldSectionElement> mp2 = scene.world().showIndependentSection(megapipe2, Direction.WEST);
		scene.world().moveSection(mp2, util.vector().of(0, -3, 0), 0);
		scene.idle(15);

		scene.overlay().showText(70)
			.attachKeyFrame()
			.pointAt(util.vector().topOf(pumpPos))
			.placeNearTarget()
			.text("Regardless of speed, Mechanical Pumps affect pipes connected up to 16 blocks away");
		scene.idle(75);

		scene.world().hideIndependentSection(mp1, Direction.WEST);
		scene.idle(5);
		scene.world().hideIndependentSection(mp2, Direction.EAST);
		scene.idle(15);

		scene.world().showSection(tank1, Direction.DOWN);
		scene.idle(2);
		scene.world().showSection(util.select().position(east), Direction.DOWN);
		scene.idle(5);
		BlockPos west = pumpPos.west();
		scene.world().showSection(util.select().position(west), Direction.DOWN);
		scene.idle(2);
		scene.world().showSection(tank2, Direction.DOWN);
		scene.idle(5);

		scene.world().showSection(largeCog, Direction.UP);
		scene.world().showSection(cogs, Direction.SOUTH);
		scene.idle(10);
		scene.world().setKineticSpeed(util.select().position(pumpPos), -4);
		scene.effects().rotationSpeedIndicator(pumpPos);
		scene.world().propagatePipeChange(pumpPos);
		scene.idle(40);

		scene.world().multiplyKineticSpeed(util.select().everywhere(), 8);
		scene.effects().rotationSpeedIndicator(pumpPos);
		scene.world().propagatePipeChange(pumpPos);
		scene.idle(20);

		scene.overlay().showText(60)
			.attachKeyFrame()
			.pointAt(util.vector().topOf(pumpPos))
			.placeNearTarget()
			.text("Speeding up the input rotation changes the speed of flow propagation...");
		scene.idle(70);

		scene.overlay().showText(50)
			.pointAt(util.vector().blockSurface(util.grid().at(0, 1, 2), Direction.WEST))
			.placeNearTarget()
			.text("...and the speed at which fluids are transferred");
		scene.idle(60);

		BlockState pipeState = AllBlocks.FLUID_PIPE.getDefaultState()
			.setValue(FluidPipeBlock.DOWN, false)
			.setValue(FluidPipeBlock.UP, false);
		scene.world().setKineticSpeed(util.select().everywhere(), 0);
		scene.idle(10);

		scene.world().setBlock(east, pipeState, true);
		scene.world().setBlock(west, pipeState, true);

		scene.world().setBlock(east.north(), pipeState.setValue(FluidPipeBlock.NORTH, false)
			.setValue(FluidPipeBlock.EAST, false), false);
		scene.world().setBlock(east.south(), pipeState.setValue(FluidPipeBlock.SOUTH, false)
			.setValue(FluidPipeBlock.EAST, false), false);
		scene.world().showSection(util.select().position(east.north()), Direction.DOWN);
		scene.world().showSection(util.select().position(east.south()), Direction.DOWN);
		Selection northPump = util.select().position(pumpPos.north());

		scene.world().setBlock(west.north(), pipeState.setValue(FluidPipeBlock.NORTH, false)
			.setValue(FluidPipeBlock.WEST, false), false);
		scene.world().setBlock(west.south(), pipeState.setValue(FluidPipeBlock.SOUTH, false)
			.setValue(FluidPipeBlock.WEST, false), false);
		scene.world().showSection(util.select().position(west.north()), Direction.DOWN);
		scene.world().showSection(util.select().position(west.south()), Direction.DOWN);

		scene.world().restoreBlocks(southPump);
		scene.world().modifyBlock(pumpPos.south(), s -> s.setValue(PumpBlock.FACING, Direction.WEST), true);
		scene.world().setKineticSpeed(util.select().everywhere(), 0);
		scene.world().showSection(northPump, Direction.DOWN);
		scene.world().modifyBlock(pumpPos.north(), s -> s.setValue(PumpBlock.FACING, Direction.EAST), false);
		scene.idle(4);

		scene.world().setKineticSpeed(util.select().everywhere(), -16);
		scene.world().setKineticSpeed(northPump, 16);
		scene.world().setKineticSpeed(southPump, 16);
		scene.world().setKineticSpeed(largeCog, 8);
		scene.idle(20);

		scene.overlay().showSelectionWithText(util.select().fromTo(2, 1, 1, 2, 1, 3), 60)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector().topOf(pumpPos))
			.placeNearTarget()
			.text("Pumps can combine their throughputs within shared pipe networks");
		scene.idle(70);

		scene.idle(30);
		scene.overlay()
			.showControls(new InputWindowElement(util.vector().topOf(pumpPos.north()), Pointing.DOWN).rightClick()
				.withItem(AllItems.WRENCH.asStack()), 30);
		scene.idle(7);
		scene.world().modifyBlock(pumpPos.north(), s -> s.setValue(PumpBlock.FACING, Direction.WEST), true);
		scene.idle(30);

		scene.overlay().showText(70)
			.attachKeyFrame()
			.pointAt(util.vector().topOf(pumpPos.north())
				.subtract(0.5f, 0.125f, 0))
			.placeNearTarget()
			.text("Ensure that all of them are facing in the same direction");
		scene.idle(40);

		scene.world().multiplyKineticSpeed(util.select().everywhere(), 4);
		scene.effects().rotationSpeedIndicator(pumpPos);
		scene.effects().rotationSpeedIndicator(pumpPos.north());
		scene.effects().rotationSpeedIndicator(pumpPos.south());
		scene.world().propagatePipeChange(pumpPos);
		scene.world().propagatePipeChange(pumpPos.north());
		scene.world().propagatePipeChange(pumpPos.south());
		scene.idle(100);

		scene.world().multiplyKineticSpeed(util.select().everywhere(), -1);
		scene.effects().rotationSpeedIndicator(pumpPos);
		scene.effects().rotationSpeedIndicator(pumpPos.north());
		scene.effects().rotationSpeedIndicator(pumpPos.south());
		scene.world().propagatePipeChange(pumpPos);
		scene.world().propagatePipeChange(pumpPos.north());
		scene.world().propagatePipeChange(pumpPos.south());
	}

}
