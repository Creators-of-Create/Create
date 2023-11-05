package com.simibubi.create.infrastructure.ponder.scenes.fluid;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidMovementActorScenes {

	public static void transfer(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("portable_fluid_interface", "Contraption Fluid Exchange");
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(0.95f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();
		scene.idle(5);

		Selection pipes = util.select().fromTo(2, 1, 3, 0, 1, 3)
			.add(util.select().position(0, 1, 4));
		BlockPos pumpPos = util.grid().at(0, 1, 4);
		Selection kinetics = util.select().fromTo(1, 1, 7, 1, 1, 4);
		Selection tank = util.select().fromTo(0, 1, 5, 0, 3, 5);
		Selection largeCog = util.select().position(2, 0, 7);
		FluidStack chocolate = new FluidStack(FluidHelper.convertToStill(AllFluids.CHOCOLATE.get()), 1000);
		BlockPos ct1 = util.grid().at(5, 3, 2);
		BlockPos ct2 = util.grid().at(6, 3, 2);
		BlockPos st = util.grid().at(0, 1, 5);
		Capability<IFluidHandler> fhc = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
		Class<FluidTankBlockEntity> type = FluidTankBlockEntity.class;
		ItemStack bucket = AllFluids.CHOCOLATE.get()
			.getFluidType()
			.getBucket(chocolate);

		scene.world().modifyBlock(pumpPos, s -> s.setValue(PumpBlock.FACING, Direction.NORTH), false);

		scene.world().modifyBlockEntity(st, type, be -> be.getCapability(fhc)
			.ifPresent(ifh -> ifh.fill(FluidHelper.copyStackWithAmount(chocolate, 10000), FluidAction.EXECUTE)));

		BlockPos bearing = util.grid().at(5, 1, 2);
		scene.world().showSection(util.select().position(bearing), Direction.DOWN);
		scene.idle(5);
		ElementLink<WorldSectionElement> contraption =
			scene.world().showIndependentSection(util.select().fromTo(5, 2, 2, 6, 4, 2), Direction.DOWN);
		scene.world().configureCenterOfRotation(contraption, util.vector().centerOf(bearing));
		scene.idle(10);
		scene.world().rotateBearing(bearing, 360, 70);
		scene.world().rotateSection(contraption, 0, 360, 0, 70);
		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(bearing.above(2)))
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Fluid Tanks on moving contraptions cannot be accessed by any pipes");

		scene.idle(70);
		BlockPos psi = util.grid().at(4, 2, 2);
		scene.world().showSectionAndMerge(util.select().position(psi), Direction.EAST, contraption);
		scene.idle(13);
		scene.effects().superGlue(psi, Direction.EAST, true);

		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(psi))
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.attachKeyFrame()
			.text("This component can interact with fluid tanks without the need to stop the contraption");
		scene.idle(90);

		BlockPos psi2 = psi.west(2);
		scene.world().showSection(util.select().position(psi2), Direction.DOWN);
		scene.overlay().showOutlineWithText(util.select().position(psi.west()), 50)
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Place a second one with a gap of 1 or 2 blocks inbetween");
		scene.idle(55);

		scene.world().rotateBearing(bearing, 360, 60);
		scene.world().rotateSection(contraption, 0, 360, 0, 60);
		scene.idle(20);

		scene.overlay().showText(40)
			.placeNearTarget()
			.pointAt(util.vector().of(3, 3, 2.5))
			.text("Whenever they pass by each other, they will engage in a connection");
		scene.idle(38);

		Selection both = util.select().fromTo(2, 2, 2, 4, 2, 2);
		Class<PortableFluidInterfaceBlockEntity> psiClass = PortableFluidInterfaceBlockEntity.class;

		scene.world().modifyBlockEntityNBT(both, psiClass, nbt -> {
			nbt.putFloat("Distance", 1);
			nbt.putFloat("Timer", 14);
		});

		scene.idle(17);
		scene.overlay().showOutline(PonderPalette.GREEN, psi, util.select().fromTo(5, 3, 2, 6, 4, 2), 80);
		scene.idle(10);

		scene.overlay().showOutlineWithText(util.select().position(psi2), 70)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.text("While engaged, the stationary interface will represent ALL Tanks on the contraption");
		scene.idle(80);

		ElementLink<WorldSectionElement> p = scene.world().showIndependentSection(tank, Direction.DOWN);
		scene.world().moveSection(p, util.vector().of(0, 0, -1), 0);
		scene.idle(5);
		scene.world().showSectionAndMerge(pipes, Direction.EAST, p);
		scene.idle(5);
		scene.world().showSectionAndMerge(largeCog, Direction.UP, p);
		scene.world().showSectionAndMerge(kinetics, Direction.NORTH, p);
		scene.idle(10);

		scene.overlay().showText(70)
			.placeNearTarget()
			.pointAt(util.vector().topOf(pumpPos))
			.attachKeyFrame()
			.text("Fluid can now be inserted...");
		scene.idle(30);

		for (int i = 0; i < 16; i++) {
			if (i == 8)
				scene.overlay().showControls(util.vector().blockSurface(
										util.grid().at(5, 3, 2), Direction.WEST)
								.add(0, 0.5, 0), Pointing.LEFT, 30)
						.withItem(bucket);

			scene.world().modifyBlockEntity(st, type, be -> be.getCapability(fhc)
				.ifPresent(ifh -> ifh.drain(1000, FluidAction.EXECUTE)));
			scene.world().modifyBlockEntity(ct1, type, be -> be.getCapability(fhc)
				.ifPresent(ifh -> ifh.fill(chocolate, FluidAction.EXECUTE)));
			scene.idle(2);
		}
		for (int i = 0; i < 8; i++) {
			scene.world().modifyBlockEntity(st, type, be -> be.getCapability(fhc)
				.ifPresent(ifh -> ifh.drain(1000, FluidAction.EXECUTE)));
			scene.world().modifyBlockEntity(ct2, type, be -> be.getCapability(fhc)
				.ifPresent(ifh -> ifh.fill(chocolate, FluidAction.EXECUTE)));
			scene.idle(2);
		}

		scene.idle(50);

		scene.overlay().showText(40)
			.placeNearTarget()
			.pointAt(util.vector().topOf(pumpPos))
			.text("...or extracted from the contraption");
		scene.world().modifyBlock(pumpPos, s -> s.setValue(PumpBlock.FACING, Direction.SOUTH), true);
		scene.world().propagatePipeChange(pumpPos);
		scene.idle(30);

		for (int i = 0; i < 8; i++) {
			scene.world().modifyBlockEntity(ct2, type, be -> be.getCapability(fhc)
				.ifPresent(ifh -> ifh.drain(1000, FluidAction.EXECUTE)));
			scene.world().modifyBlockEntity(st, type, be -> be.getCapability(fhc)
				.ifPresent(ifh -> ifh.fill(chocolate, FluidAction.EXECUTE)));
			scene.idle(2);
		}
		for (int i = 0; i < 16; i++) {
			scene.world().modifyBlockEntity(ct1, type, be -> be.getCapability(fhc)
				.ifPresent(ifh -> ifh.drain(1000, FluidAction.EXECUTE)));
			scene.world().modifyBlockEntity(st, type, be -> be.getCapability(fhc)
				.ifPresent(ifh -> ifh.fill(chocolate, FluidAction.EXECUTE)));
			scene.idle(2);
		}

		scene.world().modifyBlockEntity(util.grid().at(2, 2, 3), type, be -> be.getCapability(fhc)
			.ifPresent(ifh -> ifh.drain(8000, FluidAction.EXECUTE)));
		scene.idle(50);

		scene.overlay().showText(120)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector().topOf(psi2))
			.text("After no contents have been exchanged for a while, the contraption will continue on its way");
		scene.world().modifyBlockEntityNBT(both, psiClass, nbt -> nbt.putFloat("Timer", 2));

		scene.idle(15);
		scene.world().rotateBearing(bearing, 270, 120);
		scene.world().rotateSection(contraption, 0, 270, 0, 120);

		scene.idle(100);
		scene.markAsFinished();
	}

}
