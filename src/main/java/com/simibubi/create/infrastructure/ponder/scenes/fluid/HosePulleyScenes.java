package com.simibubi.create.infrastructure.ponder.scenes.fluid;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyFluidHandler;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.ElementLink;
import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class HosePulleyScenes {

	public static void intro(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("hose_pulley", "Source Filling and Draining using Hose Pulleys");
		scene.configureBasePlate(0, 0, 3);
		scene.setSceneOffsetY(-1);
		scene.scaleSceneView(.9f);
		scene.showBasePlate();
		scene.idle(5);

		Selection cogs = util.select().fromTo(3, 1, 2, 3, 2, 2);
		Selection pipes = util.select().fromTo(3, 1, 1, 3, 5, 1)
			.add(util.select().position(2, 5, 1));
		BlockPos hosePos = util.grid().at(1, 5, 1);
		Selection hose = util.select().position(1, 5, 1);
		Selection crank = util.select().position(0, 5, 1);

		ElementLink<WorldSectionElement> hoselink = scene.world().showIndependentSection(hose, Direction.UP);
		scene.world().moveSection(hoselink, util.vector().of(0, -1, 0), 0);
		scene.idle(10);

		Vec3 shaftInput = util.vector().blockSurface(hosePos.below(), Direction.WEST);
		scene.overlay().showText(70)
			.text("Hose Pulleys can be used to fill or drain large bodies of Fluid")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().topOf(hosePos.below()));
		scene.idle(80);

		scene.overlay().showText(80)
			.text("With the Kinetic Input, the height of the pulleys' hose can be controlled")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(shaftInput);
		scene.idle(40);

		scene.world().showSectionAndMerge(crank, Direction.EAST, hoselink);
		scene.idle(20);

		Selection kinetics = util.select().fromTo(1, 5, 1, 0, 5, 1);
		scene.world().setKineticSpeed(kinetics, 32);
		scene.idle(50);

		scene.world().setKineticSpeed(kinetics, 0);
		scene.overlay().showText(80)
			.text("The Pulley retracts while the input rotation is inverted")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().centerOf(hosePos.below(3)));
		scene.idle(30);

		scene.world().setKineticSpeed(kinetics, -32);
		scene.idle(16);
		scene.world().setKineticSpeed(kinetics, 0);
		scene.idle(10);
		scene.rotateCameraY(70);
		scene.idle(40);

		scene.overlay().showText(60)
			.text("On the opposite side, pipes can be connected")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().blockSurface(hosePos.below(), Direction.EAST));
		scene.idle(70);

		scene.rotateCameraY(-70);
		scene.idle(10);

		scene.world().showSectionAndMerge(cogs, Direction.NORTH, hoselink);
		scene.world().showSectionAndMerge(pipes, Direction.WEST, hoselink);
		scene.world().showSection(util.select().fromTo(0, 1, 0, 2, 2, 2), Direction.UP);
		scene.idle(10);

		scene.overlay().showText(70)
			.text("Attached pipe networks can either provide fluid to the hose...")
			.attachKeyFrame()
			.pointAt(util.vector().centerOf(util.grid().at(3, 1, 1)));
		scene.idle(40);

		List<BlockPos> blocks = new LinkedList<>();
		for (int y = 1; y < 3; y++) {
			blocks.add(util.grid().at(1, y, 1));
			blocks.add(util.grid().at(0, y, 1));
			blocks.add(util.grid().at(1, y, 0));
			blocks.add(util.grid().at(2, y, 1));
			blocks.add(util.grid().at(1, y, 2));
			blocks.add(util.grid().at(0, y, 0));
			blocks.add(util.grid().at(2, y, 0));
			blocks.add(util.grid().at(2, y, 2));
			blocks.add(util.grid().at(0, y, 2));
		}

		for (BlockPos blockPos : blocks) {
			scene.world().setBlock(blockPos, Blocks.WATER.defaultBlockState(), false);
			scene.idle(3);
		}

		scene.world().modifyBlockEntity(util.grid().at(1, 5, 1), HosePulleyBlockEntity.class, be -> be
			.getCapability(ForgeCapabilities.FLUID_HANDLER)
			.ifPresent(
				ifh -> ((HosePulleyFluidHandler) ifh).fill(new FluidStack(Fluids.WATER, 100), FluidAction.EXECUTE)));

		scene.idle(20);
		scene.world().modifyBlock(util.grid().at(3, 2, 1), s -> s.setValue(PumpBlock.FACING, Direction.DOWN), true);
		scene.world().propagatePipeChange(util.grid().at(3, 2, 1));
		scene.idle(20);
		scene.world().setKineticSpeed(kinetics, 32);
		scene.idle(16);
		scene.world().setKineticSpeed(kinetics, 0);
		scene.idle(5);
		scene.overlay().showText(70)
			.text("...or pull from it, draining the pool instead")
			.attachKeyFrame()
			.pointAt(util.vector().centerOf(util.grid().at(3, 1, 1)));
		scene.idle(40);

		Collections.reverse(blocks);
		for (BlockPos blockPos : blocks) {
			scene.world().destroyBlock(blockPos);
			scene.idle(3);
		}

		scene.idle(20);
		scene.overlay().showText(120)
			.text("Fill and Drain speed of the pulley depends entirely on the fluid networks' throughput")
			.placeNearTarget()
			.colored(PonderPalette.MEDIUM)
			.attachKeyFrame()
			.pointAt(util.vector().centerOf(util.grid().at(3, 1, 1)));
		scene.idle(40);
		scene.markAsFinished();

	}

	public static void level(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("hose_pulley_level", "Fill and Drain level of Hose Pulleys");
		scene.configureBasePlate(0, 0, 3);
		scene.setSceneOffsetY(-1.5f);
		scene.scaleSceneView(.9f);
		scene.showBasePlate();

		List<BlockPos> blocks = new LinkedList<>();
		for (int y = 1; y < 4; y++) {
			blocks.add(util.grid().at(1, y, 1));
			blocks.add(util.grid().at(0, y, 1));
			blocks.add(util.grid().at(1, y, 0));
			blocks.add(util.grid().at(2, y, 1));
			blocks.add(util.grid().at(1, y, 2));
			blocks.add(util.grid().at(0, y, 0));
			blocks.add(util.grid().at(2, y, 0));
			blocks.add(util.grid().at(2, y, 2));
			blocks.add(util.grid().at(0, y, 2));
		}

		for (BlockPos blockPos : blocks)
			scene.world().setBlock(blockPos, Blocks.WATER.defaultBlockState(), false);
		scene.idle(5);

		Selection water = util.select().fromTo(2, 1, 0, 0, 4, 2);
		scene.world().showSection(water, Direction.UP);
		scene.idle(10);

		Selection cogs = util.select().fromTo(3, 1, 2, 3, 2, 2);
		Selection pipes = util.select().fromTo(3, 1, 1, 3, 6, 1)
			.add(util.select().position(2, 6, 1));
		BlockPos hosePos = util.grid().at(1, 6, 1);
		Selection hose = util.select().position(1, 6, 1);
		Selection crank = util.select().position(0, 6, 1);

		ElementLink<WorldSectionElement> hoselink = scene.world().showIndependentSection(hose, Direction.DOWN);
		scene.world().moveSection(hoselink, util.vector().of(0, -1, 0), 0);
		scene.idle(10);

		scene.world().showSectionAndMerge(crank, Direction.EAST, hoselink);
		scene.idle(20);

		scene.overlay().showSelectionWithText(util.select().position(hosePos.below()), 50)
			.text("While fully retracted, the Hose Pulley cannot operate")
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.pointAt(util.vector().blockSurface(hosePos.below(), Direction.UP));
		scene.idle(55);

		scene.world().modifyBlock(util.grid().at(3, 2, 1), s -> s.setValue(PumpBlock.FACING, Direction.DOWN), false);
		Selection kinetics = util.select().fromTo(1, 6, 1, 0, 6, 1);
		scene.world().setKineticSpeed(kinetics, 32);
		scene.idle(50);

		scene.world().setKineticSpeed(kinetics, 0);
		scene.overlay().showText(40)
			.text("Draining runs from top to bottom")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().centerOf(hosePos.below(3)));
		scene.idle(10);

		scene.world().showSectionAndMerge(cogs, Direction.NORTH, hoselink);
		scene.world().showSectionAndMerge(pipes, Direction.WEST, hoselink);
		scene.world().modifyBlockEntity(util.grid().at(1, 6, 1), HosePulleyBlockEntity.class,
			be -> be.getCapability(ForgeCapabilities.FLUID_HANDLER)
				.ifPresent(
					fh -> ((HosePulleyFluidHandler) fh).fill(new FluidStack(Fluids.WATER, 100), FluidAction.EXECUTE)));
		scene.world().propagatePipeChange(util.grid().at(3, 2, 1));

		Vec3 surface = util.vector().topOf(1, 3, 1)
			.subtract(0, 2 / 8f, 0);
		AABB bb = new AABB(surface, surface).inflate(1.5, 0, 1.5);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.MEDIUM, bb, bb, 3);
		scene.idle(3);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.MEDIUM, bb, bb.expandTowards(0, -2, 0), 70);
		scene.idle(20);

		Collections.reverse(blocks);
		int i = 0;
		for (BlockPos blockPos : blocks) {
			if (i++ == 18)
				break;
			scene.world().destroyBlock(blockPos);
			scene.idle(3);
		}

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.WHITE, bb, bb.move(0, -2, 0), 60);
		scene.overlay().showText(60)
			.text("The surface level will end up just below where the hose ends")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().of(0, 2 - 1 / 8f, 1.5f));
		scene.idle(30);

		scene.idle(30);
		scene.world().hideSection(water, Direction.SOUTH);
		scene.idle(15);
		for (BlockPos blockPos : blocks)
			scene.world().destroyBlock(blockPos);
		scene.world().showSection(water, Direction.UP);
		scene.idle(15);
		scene.world().setKineticSpeed(kinetics, -32);
		scene.world().modifyBlock(util.grid().at(3, 2, 1), s -> s.setValue(PumpBlock.FACING, Direction.UP), true);
		scene.world().propagatePipeChange(util.grid().at(3, 2, 1));
		scene.idle(16);
		scene.world().setKineticSpeed(kinetics, 0);

		scene.overlay().showText(40)
			.text("Filling runs from bottom to top")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().centerOf(hosePos.below(3)));
		scene.idle(10);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.MEDIUM, bb, bb.move(0, -3 + 2 / 8f, 0), 3);
		scene.idle(3);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.MEDIUM, bb, bb.expandTowards(0, -3 + 2 / 8f, 0), 120);
		scene.idle(20);

		scene.world().setBlock(util.grid().at(1, 3, 1), Blocks.WATER.defaultBlockState(), false);
		scene.idle(3);
		scene.world().setBlock(util.grid().at(1, 2, 1), Blocks.WATER.defaultBlockState(), false);
		scene.idle(3);

		Collections.reverse(blocks);
		for (BlockPos blockPos : blocks) {
			scene.world().setBlock(blockPos, Blocks.WATER.defaultBlockState(), false);
			scene.idle(3);
		}

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.WHITE, bb, bb, 100);
		scene.overlay().showText(100)
			.text("The filled pool will not grow beyond the layer above the hose end")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().of(0, 4 - 1 / 8f, 1.5f));
		scene.idle(80);
	}

	public static void infinite(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("hose_pulley_infinite", "Passively Filling and Draining large bodies of Fluid");
		scene.configureBasePlate(0, 0, 5);
		scene.setSceneOffsetY(-.5f);
		scene.scaleSceneView(.9f);
		scene.showBasePlate();
		scene.idle(5);

		Selection tank = util.select().fromTo(4, 1, 1, 4, 3, 1);
		Selection pipes = util.select().fromTo(3, 1, 1, 2, 3, 2);
		Selection kinetics = util.select().fromTo(5, 1, 2, 4, 2, 2)
			.add(util.select().position(5, 0, 2));
		Selection hose = util.select().fromTo(1, 3, 2, 0, 3, 2);
		BlockPos pumpPos = util.grid().at(3, 2, 2);

		scene.world().multiplyKineticSpeed(kinetics, 0.25f);
		scene.world().multiplyKineticSpeed(util.select().position(pumpPos), 0.25f);

		scene.world().showSection(hose, Direction.UP);
		scene.idle(5);
		scene.world().showSection(tank, Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(pipes, Direction.NORTH);
		scene.idle(5);
		scene.world().showSection(kinetics, Direction.DOWN);
		scene.idle(10);

		scene.world().setKineticSpeed(hose, 32);
		scene.idle(10);

		Vec3 entryPoint = util.vector().topOf(1, 0, 2);
		scene.overlay().showText(60)
			.text("When deploying the Hose Pulley into a large enough ocean...")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(entryPoint);

		scene.idle(40);
		scene.world().modifyBlockEntity(util.grid().at(1, 3, 2), HosePulleyBlockEntity.class,
			be -> be.getCapability(ForgeCapabilities.FLUID_HANDLER)
				.ifPresent(
					fh -> ((HosePulleyFluidHandler) fh).fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE)));
		scene.world().setKineticSpeed(hose, 0);
		scene.world().modifyBlock(pumpPos, s -> s.setValue(PumpBlock.FACING, Direction.DOWN), true);
		scene.world().propagatePipeChange(pumpPos);
		scene.idle(30);

		Selection pulleyPos = util.select().position(1, 3, 2);
		scene.overlay().showSelectionWithText(pulleyPos, 60)
			.text("It will provide/dispose fluids without affecting the source")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector().topOf(util.grid().at(1, 3, 2)));

		scene.idle(60);

		scene.world().modifyBlockEntity(util.grid().at(4, 1, 1), FluidTankBlockEntity.class, be -> be.getTankInventory()
			.fill(new FluidStack(Fluids.WATER, 24000), FluidAction.EXECUTE));

		scene.idle(20);

		scene.overlay().showText(60)
			.text("Pipe networks can limitlessly take fluids from/to such pulleys")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().blockSurface(util.grid().at(3, 2, 2), Direction.WEST));
		scene.idle(40);
	}

}
