package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.depot.DepotBlockEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement.FlappyPose;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instruction.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FanScenes {

	public static void direction(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("fan_direction", "Air flow of Encased Fans");
		scene.configureBasePlate(0, 1, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 0, 3, 1, 5)
			.add(util.select.position(3, 2, 4)), Direction.DOWN);
		scene.world.showSection(util.select.fromTo(2, 1, 5, 1, 1, 5), Direction.DOWN);
		scene.idle(10);

		BlockPos fanPos = util.grid.at(1, 1, 4);
		scene.world.showSection(util.select.position(fanPos), Direction.SOUTH);

		scene.idle(40);
		scene.effects.rotationDirectionIndicator(fanPos.south());

		ElementLink<ParrotElement> flappyBirb = scene.special.createBirb(util.vector.topOf(1, 0, 3), FlappyPose::new);
		scene.idle(2);
		scene.special.rotateParrot(flappyBirb, 0, 235, 0, 30);
		scene.special.moveParrot(flappyBirb, util.vector.of(0, 0, -2.5), 30);
		scene.idle(20);

		scene.overlay.showText(80)
			.text("Encased Fans use Rotational Force to create an Air Current")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(fanPos));
		scene.idle(90);

		BlockPos leverPos = util.grid.at(3, 2, 4);
		Selection reverse = util.select.fromTo(3, 1, 5, 1, 1, 4);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.effects.indicateRedstone(leverPos);
		scene.addKeyframe();
		scene.world.modifyKineticSpeed(reverse, f -> -f);
		scene.effects.rotationDirectionIndicator(fanPos.south());
		scene.special.rotateParrot(flappyBirb, 0, 215 * 2, 0, 30);
		scene.special.moveParrot(flappyBirb, util.vector.of(0, 0, 2.5), 30);
		scene.idle(31);

		scene.overlay.showText(60)
			.text("Strength and Direction of Flow depends on the Rotational Input")
			.placeNearTarget()
			.pointAt(util.vector.topOf(fanPos));
		scene.markAsFinished();
		scene.idle(70);

		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.below()));
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(reverse, f -> -f);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> 4 * f);
		scene.effects.rotationSpeedIndicator(fanPos.south());
		scene.special.rotateParrot(flappyBirb, 0, 245 * 4, 0, 30);
		scene.special.moveParrot(flappyBirb, util.vector.of(0, 0, -20), 30);

	}

	public static void processing(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("fan_processing", "Processing Items using Encased Fans");
		scene.configureBasePlate(1, 0, 5);
		scene.world.showSection(util.select.layer(0)
			.substract(util.select.position(0, 0, 4)), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(6, 1, 2, 5, 1, 2)
			.add(util.select.position(1, 1, 2)), Direction.DOWN);
		scene.idle(25);

		BlockPos blockPos = util.grid.at(4, 1, 2);

		// blasting start

		ElementLink<WorldSectionElement> blockInFront =
			scene.world.showIndependentSection(util.select.position(3, 1, 0), Direction.SOUTH);
		scene.world.moveSection(blockInFront, util.vector.of(1, 0, 2), 0);
		scene.world.setBlock(blockPos, Blocks.LAVA.defaultBlockState(), false);
		scene.idle(10);

		scene.overlay.showSelectionWithText(util.select.fromTo(blockPos, blockPos.west(2)), 80)
			.colored(PonderPalette.RED)
			.text("When passing through lava, the Air Flow becomes Heated");
		scene.idle(80);

		ItemStack stack = new ItemStack(Items.GOLD_ORE);
		ItemStack smelted = new ItemStack(Items.GOLD_INGOT);

		ElementLink<EntityElement> entityLink = scene.world.createItemEntity(util.vector.centerOf(blockPos.west(2)
			.above(2)), util.vector.of(0, 0.1, 0), stack);
		scene.idle(15);
		scene.world.modifyEntity(entityLink, e -> e.setDeltaMovement(-0.2f, 0, 0));
		Vec3 itemVec = util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.EAST)
			.add(0.1, 0, 0);
		scene.overlay.showControls(new InputWindowElement(itemVec, Pointing.DOWN).withItem(stack), 20);
		scene.idle(20);
		scene.effects.emitParticles(itemVec.add(0, 0.2f, 0), Emitter.simple(ParticleTypes.LARGE_SMOKE, Vec3.ZERO), 1,
			60);

		scene.overlay.showText(80)
			.colored(PonderPalette.WHITE)
			.pointAt(itemVec)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Items caught in the area will be smelted");

		scene.idle(60);
		scene.world.modifyEntities(ItemEntity.class, ie -> ie.setItem(smelted));
		scene.idle(40);
		scene.overlay.showControls(new InputWindowElement(itemVec, Pointing.DOWN).withItem(smelted), 20);
		scene.idle(20);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.idle(20);

		scene.overlay.showText(80)
			.colored(PonderPalette.RED)
			.pointAt(itemVec)
			.placeNearTarget()
			.text("Food items thrown here would be incinerated");
		scene.idle(40);

		// smoking start

		BlockState campfire = Blocks.FIRE.defaultBlockState();
		scene.world.hideIndependentSection(blockInFront, Direction.NORTH);
		scene.idle(15);
		scene.world.setBlock(util.grid.at(3, 1, 0), campfire, false);
		scene.world.setBlock(blockPos, campfire, true);
		blockInFront = scene.world.showIndependentSection(util.select.position(3, 1, 0), Direction.NORTH);
		scene.world.moveSection(blockInFront, util.vector.of(1, 0, 2), 0);
		scene.idle(50);

		scene.overlay.showSelectionWithText(util.select.fromTo(blockPos, blockPos.west(2)), 60)
			.colored(PonderPalette.BLACK)
			.attachKeyFrame()
			.text("Instead, a setup for Smoking using Fire should be used for them");
		scene.idle(80);

		// washing start

		BlockState water = Blocks.WATER.defaultBlockState();
		scene.world.hideIndependentSection(blockInFront, Direction.NORTH);
		scene.idle(15);
		scene.world.setBlock(util.grid.at(3, 1, 0), water, false);
		scene.world.setBlock(blockPos, water, true);
		blockInFront = scene.world.showIndependentSection(util.select.position(3, 1, 0), Direction.NORTH);
		scene.world.moveSection(blockInFront, util.vector.of(1, 0, 2), 0);
		scene.idle(20);

		scene.overlay.showSelectionWithText(util.select.fromTo(blockPos, blockPos.west(2)), 60)
			.colored(PonderPalette.MEDIUM)
			.attachKeyFrame()
			.text("Air Flows passing through water create a Washing Setup");
		scene.idle(70);

		stack = new ItemStack(Items.RED_SAND, 16);
		ItemStack washed = new ItemStack(Items.GOLD_NUGGET, 16);

		entityLink = scene.world.createItemEntity(util.vector.centerOf(blockPos.west(2)
			.above(2)), util.vector.of(0, 0.1, 0), stack);
		scene.idle(15);
		scene.world.modifyEntity(entityLink, e -> e.setDeltaMovement(-0.2f, 0, 0));
		scene.overlay.showControls(new InputWindowElement(itemVec, Pointing.DOWN).withItem(stack), 20);
		scene.idle(20);
		scene.effects.emitParticles(itemVec.add(0, 0.2f, 0), Emitter.simple(ParticleTypes.SPIT, Vec3.ZERO), 1, 60);

		scene.overlay.showText(50)
			.colored(PonderPalette.WHITE)
			.pointAt(itemVec)
			.placeNearTarget()
			.text("Some interesting new processing can be done with it");

		scene.idle(60);
		scene.world.modifyEntities(ItemEntity.class, ie -> ie.setItem(washed));
		scene.overlay.showControls(new InputWindowElement(itemVec, Pointing.DOWN).withItem(washed), 20);
		scene.idle(20);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.idle(20);

		scene.overlay.showText(100)
			.colored(PonderPalette.RED)
			.pointAt(util.vector.topOf(blockPos.east()))
			.placeNearTarget()
			.attachKeyFrame()
			.text("The Speed of the Fan does NOT affect the processing speed, only its range");
		scene.world.destroyBlock(util.grid.at(1, 1, 2));
		scene.idle(110);

		ElementLink<WorldSectionElement> cogs = scene.world.makeSectionIndependent(util.select.fromTo(6, 1, 2, 6, 0, 3)
			.add(util.select.fromTo(4, 0, 2, 5, 0, 2)));
		scene.world.modifyKineticSpeed(util.select.position(5, 2, 2), f -> f / 3f);
		scene.world.moveSection(cogs, util.vector.of(0, 1, 0), 15);
		scene.world.moveSection(blockInFront, util.vector.of(0, 1, 0), 15);
		scene.world.destroyBlock(blockPos.east());
		scene.world.showSection(util.select.position(blockPos.east()
			.above()), Direction.DOWN);
		scene.world.setBlock(blockPos.above(), Blocks.WATER.defaultBlockState(), false);

		ItemStack sand = new ItemStack(Items.SAND);
		ItemStack clay = new ItemStack(Items.CLAY_BALL);

		scene.idle(20);
		BlockPos depos = util.grid.at(3, 4, 2);
		ElementLink<WorldSectionElement> depot =
			scene.world.showIndependentSection(util.select.position(depos), Direction.DOWN);
		scene.world.moveSection(depot, util.vector.of(-1, -3, 0), 0);
		scene.world.createItemOnBeltLike(depos, Direction.NORTH, sand);
		scene.idle(10);
		Vec3 depotTop = util.vector.topOf(2, 1, 2)
			.add(0, 0.25, 0);
		scene.effects.emitParticles(depotTop, Emitter.simple(ParticleTypes.SPIT, Vec3.ZERO), .5f, 30);
		scene.idle(30);
		scene.world.modifyBlockEntityNBT(util.select.position(depos), DepotBlockEntity.class,
			nbt -> nbt.put("HeldItem", new TransportedItemStack(clay).serializeNBT()));
		scene.effects.emitParticles(depotTop, Emitter.simple(ParticleTypes.SPIT, Vec3.ZERO), .5f, 30);
		scene.overlay.showText(90)
			.pointAt(depotTop)
			.attachKeyFrame()
			.text("Fan Processing can also be applied to Items on Depots and Belts");

		scene.idle(100);
		scene.world.moveSection(depot, util.vector.of(-1, 0, 0), 15);
		scene.idle(15);
		ElementLink<WorldSectionElement> largeCog =
			scene.world.showIndependentSection(util.select.position(1, 2, 4), Direction.UP);
		ElementLink<WorldSectionElement> belt =
			scene.world.showIndependentSection(util.select.fromTo(3, 3, 1, 1, 3, 3), Direction.DOWN);
		scene.world.moveSection(largeCog, util.vector.of(-1, -2, 0), 0);
		scene.world.moveSection(belt, util.vector.of(-1, -2, 0), 0);
		ElementLink<BeltItemElement> transported =
			scene.world.createItemOnBelt(util.grid.at(3, 3, 3), Direction.SOUTH, sand);
		scene.idle(60);
		scene.effects.emitParticles(depotTop, Emitter.simple(ParticleTypes.SPIT, Vec3.ZERO), .5f, 25);
		scene.idle(25);
		scene.world.changeBeltItemTo(transported, new ItemStack(Items.CLAY_BALL));
		scene.effects.emitParticles(depotTop, Emitter.simple(ParticleTypes.SPIT, Vec3.ZERO), .5f, 25);
		scene.idle(60);

		scene.world.setKineticSpeed(util.select.position(1, 2, 4)
			.add(util.select.fromTo(3, 3, 1, 1, 3, 3)), 0);

	}

}
