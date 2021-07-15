package com.simibubi.create.foundation.ponder.content.fluid;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity.CreativeSmartFluidTank;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeTileEntity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.content.PonderPalette;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.utility.Pointing;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidTankScenes {

	public static void storage(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("fluid_tank_storage", "Storing Fluids in Fluid Tanks");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos tankPos = util.grid.at(3, 1, 2);
		Selection chocolate = util.select.fromTo(1, 5, 0, 0, 8, 1);
		Selection tank = util.select.fromTo(3, 1, 2, 3, 2, 2);
		Selection largeCog1 = util.select.position(3, 0, 5);
		Selection kinetics1 = util.select.fromTo(2, 1, 5, 2, 1, 3)
			.add(util.select.position(2, 2, 4));
		Selection largeCog2 = util.select.position(6, 0, 1);
		Selection comparatorStuff = util.select.fromTo(2, 1, 1, 2, 1, 0);
		Selection pump = util.select.position(1, 1, 3);
		BlockPos pumpPos = util.grid.at(1, 1, 3);
		Selection spoutstuff = util.select.fromTo(3, 1, 0, 5, 3, 2)
			.substract(tank);
		Selection pipe = util.select.fromTo(1, 1, 2, 1, 1, 5)
			.add(util.select.position(1, 0, 5))
			.add(util.select.position(2, 1, 2));

		ElementLink<WorldSectionElement> tankLink = scene.world.showIndependentSection(tank, Direction.NORTH);
		scene.world.moveSection(tankLink, util.vector.of(0, 0, -1), 0);
		scene.idle(5);
		ElementLink<WorldSectionElement> chocLink = scene.world.showIndependentSection(chocolate, Direction.NORTH);
		scene.world.moveSection(chocLink, util.vector.of(2, -4, 3), 0);
		scene.idle(10);

		scene.overlay.showOutline(PonderPalette.GREEN, chocLink, util.select.fromTo(3, 1, 3, 2, 4, 4), 40);
		scene.idle(10);
		scene.overlay.showLine(PonderPalette.GREEN, util.vector.of(3, 1, 2), util.vector.of(2, 1, 3), 30);
		scene.overlay.showLine(PonderPalette.GREEN, util.vector.of(3, 3, 2), util.vector.of(2, 5, 3), 30);
		scene.overlay.showLine(PonderPalette.GREEN, util.vector.of(4, 3, 2), util.vector.of(4, 5, 3), 30);
		scene.overlay.showOutline(PonderPalette.GREEN, tankLink, util.select.fromTo(3, 1, 1, 3, 2, 1), 40);
		scene.idle(10);

		scene.overlay.showText(40)
			.text("Fluid Tanks can be used to store large amounts of fluid")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 2, 1), Direction.WEST));
		scene.idle(50);

		scene.world.hideIndependentSection(chocLink, Direction.DOWN);
		scene.idle(5);
		FluidStack content = new FluidStack(AllFluids.CHOCOLATE.get()
			.getSource(), 16000);
		scene.world.modifyTileEntity(tankPos, FluidTankTileEntity.class, te -> te.getTankInventory()
			.fill(content, FluidAction.EXECUTE));
		scene.idle(25);

		scene.world.moveSection(tankLink, util.vector.of(0, 0, 1), 10);
		scene.idle(5);
		scene.world.setKineticSpeed(pump, 0);
		scene.world.showSection(pipe, Direction.EAST);
		scene.idle(10);
		scene.world.showSection(largeCog1, Direction.UP);
		scene.world.showSection(kinetics1, Direction.WEST);
		scene.idle(10);
		scene.world.setBlock(util.grid.at(1, -1, 5), AllBlocks.FLUID_TANK.getDefaultState(), false);
		scene.world.setKineticSpeed(pump, 128);

		scene.idle(5);
		Selection pumpRedstone = util.select.fromTo(2, 1, 4, 2, 2, 4);
		Selection pumpCogs = util.select.fromTo(2, 1, 3, 1, 1, 3);
		scene.world.toggleRedstonePower(pumpRedstone);
		scene.world.multiplyKineticSpeed(pumpCogs, -1);
		scene.world.propagatePipeChange(pumpPos);
		scene.effects.rotationDirectionIndicator(pumpPos);
		scene.world.modifyTileEntity(util.grid.at(2, 0, 5), FluidTankTileEntity.class, te -> te.getTankInventory()
			.fill(content, FluidAction.EXECUTE));
		scene.idle(20);

		for (int i = 0; i < 4; i++) {
			scene.world.modifyTileEntity(tankPos, FluidTankTileEntity.class, te -> te.getTankInventory()
				.drain(2000, FluidAction.EXECUTE));
			scene.idle(5);
		}

		scene.overlay.showText(60)
			.text("Pipe networks can push and pull fluids from any side")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.centerOf(1, 1, 2));
		scene.idle(40);

		scene.world.toggleRedstonePower(pumpRedstone);
		scene.world.multiplyKineticSpeed(pumpCogs, -1);
		scene.world.propagatePipeChange(pumpPos);
		scene.effects.rotationDirectionIndicator(pumpPos);
		for (int i = 0; i < 4; i++) {
			scene.world.modifyTileEntity(tankPos, FluidTankTileEntity.class, te -> te.getTankInventory()
				.fill(FluidHelper.copyStackWithAmount(content, 2000), FluidAction.EXECUTE));
			scene.idle(5);
		}
		scene.idle(40);

		scene.world.hideSection(largeCog1, Direction.DOWN);
		scene.world.hideSection(kinetics1, Direction.SOUTH);
		scene.world.hideSection(pipe, Direction.WEST);
		scene.idle(10);
		scene.world.showSection(comparatorStuff, Direction.SOUTH);
		scene.idle(5);
		scene.world.moveSection(tankLink, util.vector.of(-1, 0, 0), 10);
		scene.idle(10);
		scene.world.toggleRedstonePower(comparatorStuff);
		scene.world.modifyTileNBT(util.select.position(2, 1, 0), NixieTubeTileEntity.class,
			nbt -> nbt.putInt("RedstoneStrength", 15));

		scene.overlay.showText(50)
			.text("The contained fluid can be measured by a Comparator")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 1, 1), Direction.DOWN)
				.add(0, 1 / 8f, 0));
		scene.idle(50);

		scene.world.hideSection(comparatorStuff, Direction.EAST);
		scene.idle(20);

		ItemStack bucket = new ItemStack(Items.BUCKET, 1);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.NORTH), Pointing.RIGHT)
				.showing(AllIcons.I_MTD_CLOSE)
				.withItem(bucket),
			40);
		scene.idle(7);
		scene.overlay.showSelectionWithText(util.select.fromTo(2, 1, 2, 2, 2, 2), 70)
			.text("However, in Survival Mode Fluids cannot be added or taken manually")
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.WEST));
		scene.idle(80);
		scene.world.modifyTileEntity(util.grid.at(4, 3, 0), SpoutTileEntity.class,
			te -> te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
				.ifPresent(ifh -> ifh.fill(content, FluidAction.EXECUTE)));

		scene.world.moveSection(tankLink, util.vector.of(0, 0, 1), 7);
		scene.world.multiplyKineticSpeed(spoutstuff, -1);
		scene.world.multiplyKineticSpeed(largeCog2, -1);
		scene.idle(7);
		ElementLink<WorldSectionElement> spoutLink = scene.world.showIndependentSection(spoutstuff, Direction.SOUTH);
		ElementLink<WorldSectionElement> largeCogLink = scene.world.showIndependentSection(largeCog2, Direction.UP);
		scene.world.moveSection(spoutLink, util.vector.of(-1, 0, 1), 0);
		scene.world.moveSection(largeCogLink, util.vector.of(-1, 0, 1), 0);
		scene.idle(20);
		scene.overlay.showOutline(PonderPalette.GREEN, new Object(), util.select.position(2, 1, 1), 50);
		scene.idle(5);
		scene.overlay.showOutline(PonderPalette.GREEN, new Object(), util.select.position(3, 3, 1), 50);
		scene.idle(5);

		scene.overlay.showText(80)
			.text("You can use Basins, Item Drains and Spouts to drain or fill fluid containing items")
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(util.vector.topOf(2, 1, 1));
		scene.idle(90);

		ItemStack chocBucket = AllFluids.CHOCOLATE.get()
			.getAttributes()
			.getBucket(new FluidStack(FluidHelper.convertToStill(AllFluids.CHOCOLATE.get()), 1000));
		scene.world.createItemOnBeltLike(util.grid.at(3, 1, 0), Direction.WEST, chocBucket);
		scene.idle(40);
		scene.world.modifyTileNBT(util.select.position(util.grid.at(4, 3, 0)), SpoutTileEntity.class,
			nbt -> nbt.putInt("ProcessingTicks", 20));
		scene.idle(20);
		scene.world.removeItemsFromBelt(util.grid.at(4, 1, 0));
		scene.world.createItemOnBeltLike(util.grid.at(4, 1, 0), Direction.UP, chocBucket);
		for (int i = 0; i < 10; i++) {
			scene.effects.emitParticles(util.vector.topOf(3, 1, 1)
				.add(0, 1 / 16f, 0),
				Emitter.simple(FluidFX.getFluidParticle(content),
					VecHelper.offsetRandomly(Vector3d.ZERO, Create.RANDOM, .1f)),
				1, 1);
		}

	}

	public static void sizes(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("fluid_tank_sizes", "Dimensions of a Fluid tank");
		scene.configureBasePlate(0, 0, 6);
		scene.showBasePlate();
		scene.idle(5);

		Selection single = util.select.position(0, 3, 0);
		Selection single2 = util.select.fromTo(1, 2, 1, 0, 2, 0);
		Selection single3 = util.select.fromTo(2, 1, 0, 0, 1, 2);

		ElementLink<WorldSectionElement> s1 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s1, util.vector.of(2, -2, 2), 0);
		scene.idle(10);

		scene.overlay.showText(60)
			.text("Fluid Tanks can be combined to increase the total capacity")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(2, 1, 2));
		scene.idle(40);

		ElementLink<WorldSectionElement> s2 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s2, util.vector.of(2, -2, 3), 0);
		scene.idle(5);
		ElementLink<WorldSectionElement> s3 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s3, util.vector.of(3, -2, 3), 0);
		scene.idle(5);
		ElementLink<WorldSectionElement> s4 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s4, util.vector.of(3, -2, 2), 0);
		scene.idle(10);

		scene.world.moveSection(s1, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s2, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s3, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s4, util.vector.of(0, -100, 0), 0);

		ElementLink<WorldSectionElement> d = scene.world.showIndependentSectionImmediately(single2);
		scene.world.moveSection(d, util.vector.of(2, -1, 2), 0);
		scene.effects.indicateSuccess(util.grid.at(2, 1, 2));
		scene.effects.indicateSuccess(util.grid.at(3, 1, 2));
		scene.effects.indicateSuccess(util.grid.at(2, 1, 3));
		scene.effects.indicateSuccess(util.grid.at(3, 1, 3));
		scene.world.hideIndependentSection(s1, Direction.DOWN);
		scene.world.hideIndependentSection(s2, Direction.DOWN);
		scene.world.hideIndependentSection(s3, Direction.DOWN);
		scene.world.hideIndependentSection(s4, Direction.DOWN);
		scene.idle(25);

		scene.overlay.showText(60)
			.text("Their base square can be up to 3 blocks wide...")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(2, 1, 2));
		scene.idle(40);

		s1 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s1, util.vector.of(2, -2, 4), 0);
		scene.idle(3);
		s2 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s2, util.vector.of(3, -2, 4), 0);
		scene.idle(3);
		s3 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s3, util.vector.of(4, -2, 4), 0);
		scene.idle(3);
		s4 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s4, util.vector.of(4, -2, 3), 0);
		scene.idle(3);
		ElementLink<WorldSectionElement> s5 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s5, util.vector.of(4, -2, 2), 0);
		scene.idle(10);

		scene.world.moveSection(d, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s1, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s2, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s3, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s4, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s5, util.vector.of(0, -100, 0), 0);

		ElementLink<WorldSectionElement> t = scene.world.showIndependentSectionImmediately(single3);
		scene.world.moveSection(t, util.vector.of(2, 0, 2), 0);

		for (int i = 2; i < 5; i++)
			for (int j = 2; j < 5; j++)
				scene.effects.indicateSuccess(util.grid.at(i, 1, j));

		scene.world.hideIndependentSection(d, Direction.DOWN);
		scene.world.hideIndependentSection(s1, Direction.DOWN);
		scene.world.hideIndependentSection(s2, Direction.DOWN);
		scene.world.hideIndependentSection(s3, Direction.DOWN);
		scene.world.hideIndependentSection(s4, Direction.DOWN);
		scene.world.hideIndependentSection(s5, Direction.DOWN);
		scene.idle(25);

		scene.world.hideIndependentSection(t, Direction.DOWN);
		scene.idle(10);

		Selection full1 = util.select.fromTo(5, 1, 0, 5, 6, 0);
		Selection full2 = util.select.fromTo(4, 1, 1, 3, 6, 2);
		Selection full3 = util.select.fromTo(0, 6, 5, 2, 1, 3);

		scene.world.showSection(full1, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(full2, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(full3, Direction.DOWN);
		scene.idle(10);

		Vector3d blockSurface = util.vector.blockSurface(util.grid.at(3, 3, 1), Direction.WEST);
		scene.overlay.showText(60)
			.text("...and grow in height by more than 30 additional layers")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(blockSurface);
		scene.idle(70);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(3, 3, 1), Direction.NORTH), Pointing.RIGHT)
				.rightClick()
				.withWrench(),
			60);
		scene.idle(7);
		scene.world.modifyBlocks(full2, s -> s.setValue(FluidTankBlock.SHAPE, FluidTankBlock.Shape.PLAIN), false);
		scene.idle(30);

		scene.overlay.showText(60)
			.text("Using a Wrench, a tanks' window can be toggled")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(blockSurface);
		scene.idle(50);
	}

	public static void creative(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("creative_fluid_tank", "Creative Fluid Tanks");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		Selection largeCog = util.select.position(5, 0, 2);
		Selection cTank = util.select.fromTo(3, 1, 1, 3, 2, 1);
		Selection tank = util.select.fromTo(1, 1, 3, 1, 3, 3);
		Selection pipes = util.select.fromTo(3, 1, 2, 2, 1, 3);
		Selection cog = util.select.position(4, 1, 2);
		BlockPos cTankPos = util.grid.at(3, 1, 1);
		BlockPos pumpPos = util.grid.at(3, 1, 2);

		ElementLink<WorldSectionElement> cTankLink = scene.world.showIndependentSection(cTank, Direction.DOWN);
		scene.world.moveSection(cTankLink, util.vector.of(-1, 0, 1), 0);

		scene.overlay.showText(70)
			.text("Creative Fluid Tanks can be used to provide a bottomless supply of fluid")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.WEST));
		scene.idle(80);

		ItemStack bucket = new ItemStack(Items.LAVA_BUCKET);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.NORTH), Pointing.RIGHT)
				.rightClick()
				.withItem(bucket),
			40);
		scene.idle(7);
		scene.world.modifyTileEntity(cTankPos, CreativeFluidTankTileEntity.class,
			te -> ((CreativeSmartFluidTank) te.getTankInventory())
				.setContainedFluid(new FluidStack(Fluids.FLOWING_LAVA, 1000)));
		scene.idle(5);

		scene.overlay.showText(50)
			.text("Right-Click with a fluid containing item to configure it")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.WEST));
		scene.idle(60);
		scene.world.moveSection(cTankLink, util.vector.of(1, 0, -1), 6);
		scene.idle(7);
		scene.world.showSection(tank, Direction.DOWN);
		scene.idle(5);

		scene.world.showSection(largeCog, Direction.UP);
		scene.world.showSection(cog, Direction.NORTH);
		scene.world.showSection(pipes, Direction.NORTH);
		scene.world.multiplyKineticSpeed(util.select.everywhere(), -1);
		scene.world.propagatePipeChange(pumpPos);
		scene.effects.rotationDirectionIndicator(pumpPos);
		scene.idle(40);

		scene.overlay.showText(70)
			.text("Pipe Networks can now endlessly draw the assigned fluid from the tank")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 2), Direction.WEST));
		scene.idle(120);

		scene.world.multiplyKineticSpeed(util.select.everywhere(), -1);
		scene.world.propagatePipeChange(pumpPos);
		scene.effects.rotationDirectionIndicator(pumpPos);
		scene.idle(40);

		scene.overlay.showText(70)
			.text("Any Fluids pushed back into a Creative Fluid Tank will be voided")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 2), Direction.WEST));
		scene.idle(40);

	}

}
