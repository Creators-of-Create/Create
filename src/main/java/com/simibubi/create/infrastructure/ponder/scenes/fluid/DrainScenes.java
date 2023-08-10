package com.simibubi.create.infrastructure.ponder.scenes.fluid;

import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.foundation.ElementLink;
import net.createmod.ponder.foundation.SceneBuilder;
import net.createmod.ponder.foundation.SceneBuildingUtil;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class DrainScenes {

	public static void emptying(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("item_drain", "Emptying Fluid Containers using Item Drains");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		Selection drain = util.select.position(3, 1, 2);
		BlockPos drainPos = util.grid.at(3, 1, 2);
		Selection pipes = util.select.fromTo(3, 1, 3, 3, 1, 4)
			.add(util.select.fromTo(3, 2, 4, 2, 2, 4));
		Selection tank = util.select.fromTo(1, 1, 4, 1, 3, 4);
		Selection largeCog = util.select.position(1, 0, 5);
		Selection kinetics = util.select.fromTo(2, 1, 3, 2, 1, 5);
		Selection belt = util.select.fromTo(2, 1, 2, 1, 1, 2);
		BlockPos beltPos = util.grid.at(1, 1, 2);

		ElementLink<WorldSectionElement> drainLink = scene.world.showIndependentSection(drain, Direction.DOWN);
		scene.world.moveSection(drainLink, util.vector.of(-1, 0, 0), 0);
		scene.idle(10);

		scene.overlay.showText(40)
			.text("Item Drains can extract fluids from items")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(drainPos.west(), Direction.UP));
		scene.idle(50);

		ItemStack lavaBucket = new ItemStack(Items.LAVA_BUCKET);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(drainPos.west(), Direction.UP), Pointing.DOWN).rightClick()
				.withItem(lavaBucket),
			40);
		scene.idle(7);
		scene.world.modifyBlockEntity(drainPos, ItemDrainBlockEntity.class, be -> {
			be.getBehaviour(SmartFluidTankBehaviour.TYPE)
				.allowInsertion();
			be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
				.ifPresent(fh -> fh.fill(new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE));
		});
		scene.idle(10);

		scene.overlay.showText(50)
			.text("Right-click it to pour fluids from your held item into it")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(drainPos.west(), Direction.WEST));
		scene.idle(60);

		scene.world.modifyBlockEntity(drainPos, ItemDrainBlockEntity.class,
			be -> be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
				.ifPresent(fh -> fh.drain(500, FluidAction.EXECUTE)));

		scene.world.moveSection(drainLink, util.vector.of(1, 0, 0), 7);
		scene.world.showSection(largeCog, Direction.UP);
		scene.idle(3);
		scene.world.showSection(kinetics, Direction.NORTH);
		scene.idle(4);
		scene.world.showSection(belt, Direction.SOUTH);
		scene.idle(10);

		scene.overlay.showText(40)
			.text("When items are inserted from the side...")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(util.grid.at(2, 1, 2)));
		scene.idle(40);

		scene.overlay
			.showControls(new InputWindowElement(util.vector.topOf(beltPos), Pointing.DOWN).withItem(lavaBucket), 20);
		scene.idle(7);
		scene.world.createItemOnBelt(beltPos, Direction.NORTH, lavaBucket);
		scene.idle(30);

		scene.overlay.showText(60)
			.text("...they roll across, emptying out their contained fluid")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(drainPos));
		scene.idle(40);

		scene.world.showSection(tank, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(pipes, Direction.NORTH);
		scene.idle(20);

		scene.overlay.showText(90)
			.text("Pipe Networks can now pull the fluid from the drains' internal buffer")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(util.grid.at(3, 1, 3)));
		scene.idle(50);
		scene.markAsFinished();
		scene.idle(50);

		for (int i = 0; i < 5; i++) {
			scene.world.createItemOnBelt(beltPos, Direction.NORTH, lavaBucket);
			scene.idle(30);
		}

	}

}
