package com.simibubi.create.infrastructure.ponder.scenes.fluid;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instruction.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.utility.Pointing;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public class SpoutScenes {

	public static void filling(SceneBuilder scene, SceneBuildingUtil util) {
		RandomSource random = RandomSource.create();

		scene.title("spout_filling", "Filling Items using a Spout");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		ElementLink<WorldSectionElement> depot =
			scene.world.showIndependentSection(util.select.position(2, 1, 1), Direction.DOWN);
		scene.world.moveSection(depot, util.vector.of(0, 0, 1), 0);
		scene.idle(10);
		
		scene.world.modifyBlock(util.grid.at(2, 3, 3), s -> s.setValue(PumpBlock.FACING, Direction.NORTH), false);

		Selection largeCog = util.select.position(3, 0, 5);
		Selection kinetics = util.select.fromTo(2, 1, 5, 2, 2, 3);
		Selection tank = util.select.fromTo(1, 1, 4, 1, 2, 4);
		Selection pipes = util.select.fromTo(1, 3, 4, 2, 3, 3);

		Selection spoutS = util.select.position(2, 3, 2);
		BlockPos spoutPos = util.grid.at(2, 3, 2);
		BlockPos depotPos = util.grid.at(2, 1, 1);
		scene.world.showSection(spoutS, Direction.DOWN);
		scene.idle(10);

		Vec3 spoutSide = util.vector.blockSurface(spoutPos, Direction.WEST);
		scene.overlay.showText(60)
			.pointAt(spoutSide)
			.placeNearTarget()
			.attachKeyFrame()
			.text("The Spout can fill fluid holding items provided beneath it");

		scene.idle(50);

		scene.world.showSection(tank, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(largeCog, Direction.UP);
		scene.world.showSection(kinetics, Direction.NORTH);
		scene.world.showSection(pipes, Direction.NORTH);

		scene.idle(20);
		FluidStack honey = new FluidStack(FluidHelper.convertToStill(AllFluids.HONEY.get()), 1000);
		ItemStack bucket = AllFluids.HONEY.get()
			.getFluidType()
			.getBucket(honey);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.NORTH), Pointing.RIGHT)
				.showing(AllIcons.I_MTD_CLOSE)
				.withItem(bucket),
			40);
		scene.idle(7);
		scene.overlay.showSelectionWithText(util.select.position(2, 3, 2), 50)
			.pointAt(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.WEST))
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.text("The content of a Spout cannot be accessed manually");
		scene.idle(60);
		scene.overlay.showText(70)
			.pointAt(util.vector.blockSurface(util.grid.at(2, 3, 3), Direction.WEST))
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.text("Instead, Pipes can be used to supply it with fluids");

		scene.idle(90);
		scene.overlay.showText(60)
			.pointAt(spoutSide.subtract(0, 2, 0))
			.attachKeyFrame()
			.placeNearTarget()
			.text("The Input items can be placed on a Depot under the Spout");
		scene.idle(50);
		ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
		scene.world.createItemOnBeltLike(depotPos, Direction.NORTH, bottle);
		Vec3 depotCenter = util.vector.centerOf(depotPos.south());
		scene.overlay.showControls(new InputWindowElement(depotCenter, Pointing.UP).withItem(bottle), 30);
		scene.idle(10);

		scene.idle(20);
		scene.world.modifyBlockEntityNBT(spoutS, SpoutBlockEntity.class, nbt -> nbt.putInt("ProcessingTicks", 20));
		scene.idle(20);
		scene.world.removeItemsFromBelt(depotPos);
		ItemStack potion = new ItemStack(Items.HONEY_BOTTLE);
		scene.world.createItemOnBeltLike(depotPos, Direction.UP, potion);
		ParticleOptions fluidParticle = FluidFX.getFluidParticle(new FluidStack(AllFluids.HONEY.get(), 1000));
		for (int i = 0; i < 10; i++) {
			scene.effects.emitParticles(util.vector.topOf(depotPos.south())
				.add(0, 1 / 16f, 0),
				Emitter.simple(fluidParticle, VecHelper.offsetRandomly(Vec3.ZERO, random, .1f)), 1, 1);
		}
		scene.idle(10);
		scene.overlay.showControls(new InputWindowElement(depotCenter, Pointing.UP).withItem(potion), 50);
		scene.idle(60);

		scene.world.hideIndependentSection(depot, Direction.NORTH);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(0, 1, 3, 0, 2, 3), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.fromTo(4, 1, 2, 0, 2, 2), Direction.SOUTH);
		scene.idle(20);
		BlockPos beltPos = util.grid.at(0, 1, 2);
		scene.overlay.showText(40)
			.pointAt(util.vector.blockSurface(beltPos, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("When items are provided on a belt...");
		scene.idle(30);

		ElementLink<BeltItemElement> ingot = scene.world.createItemOnBelt(beltPos, Direction.SOUTH, bottle);
		scene.idle(15);
		ElementLink<BeltItemElement> ingot2 = scene.world.createItemOnBelt(beltPos, Direction.SOUTH, bottle);
		scene.idle(15);
		scene.world.stallBeltItem(ingot, true);
		scene.world.modifyBlockEntityNBT(spoutS, SpoutBlockEntity.class, nbt -> nbt.putInt("ProcessingTicks", 20));

		scene.overlay.showText(50)
			.pointAt(spoutSide)
			.placeNearTarget()
			.attachKeyFrame()
			.text("The Spout will hold and process them automatically");

		scene.idle(20);
		for (int i = 0; i < 10; i++) {
			scene.effects.emitParticles(util.vector.topOf(depotPos.south())
				.add(0, 1 / 16f, 0),
				Emitter.simple(fluidParticle, VecHelper.offsetRandomly(Vec3.ZERO, random, .1f)), 1, 1);
		}
		scene.world.removeItemsFromBelt(spoutPos.below(2));
		ingot = scene.world.createItemOnBelt(spoutPos.below(2), Direction.UP, potion);
		scene.world.stallBeltItem(ingot, true);
		scene.idle(5);
		scene.world.stallBeltItem(ingot, false);
		scene.idle(15);
		scene.world.stallBeltItem(ingot2, true);
		scene.world.modifyBlockEntityNBT(spoutS, SpoutBlockEntity.class, nbt -> nbt.putInt("ProcessingTicks", 20));
		scene.idle(20);
		for (int i = 0; i < 10; i++) {
			scene.effects.emitParticles(util.vector.topOf(depotPos.south())
				.add(0, 1 / 16f, 0),
				Emitter.simple(fluidParticle, VecHelper.offsetRandomly(Vec3.ZERO, random, .1f)), 1, 1);
		}
		scene.world.removeItemsFromBelt(spoutPos.below(2));
		ingot2 = scene.world.createItemOnBelt(spoutPos.below(2), Direction.UP, potion);
		scene.world.stallBeltItem(ingot2, true);
		scene.idle(5);
		scene.world.stallBeltItem(ingot2, false);

	}

}
