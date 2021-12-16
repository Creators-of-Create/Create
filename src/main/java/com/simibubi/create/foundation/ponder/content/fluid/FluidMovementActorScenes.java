package com.simibubi.create.foundation.ponder.content.fluid;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.components.actors.PortableFluidInterfaceTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import com.simibubi.create.lib.transfer.TransferUtil;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import com.simibubi.create.lib.transfer.fluid.FluidStack;
import com.simibubi.create.lib.transfer.fluid.IFluidHandler;

public class FluidMovementActorScenes {

	public static void transfer(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("portable_fluid_interface", "Contraption Fluid Exchange");
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(0.95f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();
		scene.idle(5);

		Selection pipes = util.select.fromTo(2, 1, 3, 0, 1, 3)
			.add(util.select.position(0, 1, 4));
		BlockPos pumpPos = util.grid.at(0, 1, 4);
		Selection kinetics = util.select.fromTo(1, 1, 7, 1, 1, 4);
		Selection tank = util.select.fromTo(0, 1, 5, 0, 3, 5);
		Selection largeCog = util.select.position(2, 0, 7);
		FluidStack chocolate = new FluidStack(FluidHelper.convertToStill(AllFluids.CHOCOLATE.get()), FluidConstants.BUCKET);
		BlockPos ct1 = util.grid.at(5, 3, 2);
		BlockPos ct2 = util.grid.at(6, 3, 2);
		BlockPos st = util.grid.at(0, 1, 5);
		Class<FluidTankTileEntity> type = FluidTankTileEntity.class;
		ItemStack bucket = AllFluids.CHOCOLATE.get().getBucket().getDefaultInstance();

		scene.world.modifyTileEntity(st, type, te -> TransferUtil.getFluidHandler(te)
			.ifPresent(ifh -> ifh.fill(FluidHelper.copyStackWithAmount(chocolate, FluidConstants.BUCKET * 10), false)));

		BlockPos bearing = util.grid.at(5, 1, 2);
		scene.world.showSection(util.select.position(bearing), Direction.DOWN);
		scene.idle(5);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.fromTo(5, 2, 2, 6, 4, 2), Direction.DOWN);
		scene.world.configureCenterOfRotation(contraption, util.vector.centerOf(bearing));
		scene.idle(10);
		scene.world.rotateBearing(bearing, 360, 70);
		scene.world.rotateSection(contraption, 0, 360, 0, 70);
		scene.overlay.showText(60)
			.pointAt(util.vector.topOf(bearing.above(2)))
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Fluid Tanks on moving contraptions cannot be accessed by any pipes");

		scene.idle(70);
		BlockPos psi = util.grid.at(4, 2, 2);
		scene.world.showSectionAndMerge(util.select.position(psi), Direction.EAST, contraption);
		scene.idle(13);
		scene.effects.superGlue(psi, Direction.EAST, true);

		scene.overlay.showText(80)
			.pointAt(util.vector.topOf(psi))
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.attachKeyFrame()
			.text("This component can interact with fluid tanks without the need to stop the contraption");
		scene.idle(90);

		BlockPos psi2 = psi.west(2);
		scene.world.showSection(util.select.position(psi2), Direction.DOWN);
		scene.overlay.showSelectionWithText(util.select.position(psi.west()), 50)
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Place a second one with a gap of 1 or 2 blocks inbetween");
		scene.idle(55);

		scene.world.rotateBearing(bearing, 360, 60);
		scene.world.rotateSection(contraption, 0, 360, 0, 60);
		scene.idle(20);

		scene.overlay.showText(40)
			.placeNearTarget()
			.pointAt(util.vector.of(3, 3, 2.5))
			.text("Whenever they pass by each other, they will engage in a connection");
		scene.idle(35);

		Selection both = util.select.fromTo(2, 2, 2, 4, 2, 2);
		Class<PortableFluidInterfaceTileEntity> psiClass = PortableFluidInterfaceTileEntity.class;

		scene.world.modifyTileNBT(both, psiClass, nbt -> {
			nbt.putFloat("Distance", 1);
			nbt.putFloat("Timer", 40);
		});

		scene.idle(20);
		scene.overlay.showOutline(PonderPalette.GREEN, psi, util.select.fromTo(5, 3, 2, 6, 4, 2), 80);
		scene.idle(10);

		scene.overlay.showSelectionWithText(util.select.position(psi2), 70)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.text("While engaged, the stationary interface will represent ALL Tanks on the contraption");
		scene.idle(80);

		ElementLink<WorldSectionElement> p = scene.world.showIndependentSection(tank, Direction.DOWN);
		scene.world.moveSection(p, util.vector.of(0, 0, -1), 0);
		scene.idle(5);
		scene.world.showSectionAndMerge(pipes, Direction.EAST, p);
		scene.idle(5);
		scene.world.showSectionAndMerge(largeCog, Direction.UP, p);
		scene.world.showSectionAndMerge(kinetics, Direction.NORTH, p);
		scene.idle(10);

		scene.overlay.showText(70)
			.placeNearTarget()
			.pointAt(util.vector.topOf(pumpPos))
			.attachKeyFrame()
			.text("Fluid can now be inserted...");
		scene.idle(30);

		for (int i = 0; i < 16; i++) {
			if (i == 8)
				scene.overlay
					.showControls(new InputWindowElement(util.vector.blockSurface(util.grid.at(5, 3, 2), Direction.WEST)
						.add(0, 0.5, 0), Pointing.LEFT).withItem(bucket), 30);
			scene.world.modifyTileEntity(st, type, te -> TransferUtil.getFluidHandler(te)
				.ifPresent(ifh -> ifh.drain(FluidConstants.BUCKET, false)));
			scene.world.modifyTileEntity(ct1, type, te -> TransferUtil.getFluidHandler(te)
				.ifPresent(ifh -> ifh.fill(chocolate, false)));
			scene.idle(2);
		}
		for (int i = 0; i < 8; i++) {
			scene.world.modifyTileEntity(st, type, te -> TransferUtil.getFluidHandler(te)
				.ifPresent(ifh -> ifh.drain(FluidConstants.BUCKET, false)));
			scene.world.modifyTileEntity(ct2, type, te -> TransferUtil.getFluidHandler(te)
				.ifPresent(ifh -> ifh.fill(chocolate, false)));
			scene.idle(2);
		}

		scene.idle(50);

		scene.overlay.showText(40)
			.placeNearTarget()
			.pointAt(util.vector.topOf(pumpPos))
			.text("...or extracted from the contraption");
		scene.world.multiplyKineticSpeed(util.select.everywhere(), -1);
		scene.world.propagatePipeChange(pumpPos);
		scene.idle(30);

		for (int i = 0; i < 8; i++) {
			scene.world.modifyTileEntity(ct2, type, te -> TransferUtil.getFluidHandler(te)
				.ifPresent(ifh -> ifh.drain(FluidConstants.BUCKET, false)));
			scene.world.modifyTileEntity(st, type, te -> TransferUtil.getFluidHandler(te)
				.ifPresent(ifh -> ifh.fill(chocolate, false)));
			scene.idle(2);
		}
		for (int i = 0; i < 16; i++) {
			scene.world.modifyTileEntity(ct1, type, te -> TransferUtil.getFluidHandler(te)
				.ifPresent(ifh -> ifh.drain(FluidConstants.BUCKET, false)));
			scene.world.modifyTileEntity(st, type, te -> TransferUtil.getFluidHandler(te)
				.ifPresent(ifh -> ifh.fill(chocolate, false)));
			scene.idle(2);
		}

		scene.world.modifyTileEntity(util.grid.at(2, 2, 3), type, te -> TransferUtil.getFluidHandler(te)
			.ifPresent(ifh -> ifh.drain(FluidConstants.BUCKET * 8, false)));
		scene.idle(50);

		scene.overlay.showText(120)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.topOf(psi2))
			.text("After no contents have been exchanged for a while, the contraption will continue on its way");
		scene.world.modifyTileNBT(both, psiClass, nbt -> nbt.putFloat("Timer", 9));

		scene.idle(15);
		scene.markAsFinished();
		scene.world.rotateBearing(bearing, 270, 120);
		scene.world.rotateSection(contraption, 0, 270, 0, 120);
	}

}
