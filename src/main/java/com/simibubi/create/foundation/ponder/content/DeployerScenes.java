package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.EntityElement;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class DeployerScenes {

	public static void filter(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("deployer", "Using the Deployer");
		scene.configureBasePlate(0, 0, 5);

		BlockPos potPosition = util.grid.at(1, 1, 2);
		BlockPos deployerPos = util.grid.at(3, 1, 2);
		Selection deployerSelection = util.select.position(deployerPos);

		scene.world.setBlock(potPosition, Blocks.AIR.getDefaultState(), false);
		scene.world.showSection(util.select.layer(0)
			.add(util.select.position(1, 1, 2)), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 3, 3, 1, 5), Direction.DOWN);
		scene.idle(10);

		scene.world.showSection(deployerSelection, Direction.SOUTH);
		scene.idle(10);

		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(util.vector.topOf(deployerPos))
			.text("Given Rotational Force, a Deployer can imitate player interactions");
		scene.world.moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world.moveDeployer(deployerPos, -1, 25);
		scene.idle(44);

		scene.overlay.showSelectionWithText(util.select.position(deployerPos.west(2)), 60)
			.text("It will always interact with the position 2 blocks in front of itself")
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.attachKeyFrame();
		scene.world.moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world.moveDeployer(deployerPos, -1, 25);
		scene.idle(20);
		scene.world.showSection(util.select.fromTo(2, 1, 3, 2, 1, 1), Direction.DOWN);
		scene.idle(24);

		scene.overlay.showText(50)
			.pointAt(util.vector.topOf(deployerPos.west()))
			.text("Blocks directly in front will not obstruct it")
			.placeNearTarget();
		scene.world.moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world.moveDeployer(deployerPos, -1, 25);
		scene.idle(34);
		scene.world.hideSection(util.select.fromTo(2, 1, 3, 2, 1, 1), Direction.UP);
		scene.idle(20);

		String[] actions =
			new String[] { "Place Blocks,", "Use Items,", "Activate Blocks,", "Harvest blocks", "and Attack Mobs" };

		scene.overlay.showText(80)
			.attachKeyFrame()
			.independent(40)
			.placeNearTarget()
			.text("Deployers can:");

		int y = 60;
		for (String s : actions) {
			scene.idle(15);
			scene.overlay.showText(50)
				.colored(PonderPalette.MEDIUM)
				.placeNearTarget()
				.independent(y)
				.text(s);
			y += 16;
		}
		scene.idle(50);

		ItemStack pot = new ItemStack(Items.FLOWER_POT);
		Vec3d frontVec = util.vector.blockSurface(deployerPos, Direction.WEST)
			.add(-.125, 0, 0);

		scene.overlay.showControls(new InputWindowElement(frontVec, Pointing.DOWN).rightClick()
			.withItem(pot), 40);
		scene.idle(7);
		Class<DeployerTileEntity> teType = DeployerTileEntity.class;
		scene.world.modifyTileNBT(deployerSelection, teType, nbt -> nbt.put("HeldItem", pot.serializeNBT()));
		scene.idle(10);

		scene.overlay.showText(40)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(frontVec)
			.text("Right-click the front to give it an Item to use");
		scene.idle(40);
		scene.world.moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world.restoreBlocks(util.select.position(potPosition));
		scene.world.modifyTileNBT(deployerSelection, teType,
			nbt -> nbt.put("HeldItem", ItemStack.EMPTY.serializeNBT()));
		scene.world.moveDeployer(deployerPos, -1, 25);
		scene.idle(20);

		scene.world.showSection(util.select.position(deployerPos.up()), Direction.DOWN);

		ItemStack tulip = new ItemStack(Items.RED_TULIP);
		Vec3d entitySpawn = util.vector.topOf(deployerPos.up(3));

		ElementLink<EntityElement> entity1 =
			scene.world.createItemEntity(entitySpawn, util.vector.of(0, 0.2, 0), tulip);
		scene.idle(17);
		scene.world.modifyEntity(entity1, Entity::remove);
		scene.world.modifyTileNBT(deployerSelection, teType, nbt -> nbt.put("HeldItem", tulip.serializeNBT()));
		scene.idle(10);
		scene.overlay.showText(40)
			.placeNearTarget()
			.pointAt(util.vector.of(3, 2.5, 3))
			.text("Items can also be inserted automatically");
		scene.idle(30);
		scene.world.moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world.setBlock(potPosition, Blocks.POTTED_RED_TULIP.getDefaultState(), false);
		scene.world.modifyTileNBT(deployerSelection, teType,
			nbt -> nbt.put("HeldItem", ItemStack.EMPTY.serializeNBT()));
		scene.world.moveDeployer(deployerPos, -1, 25);
		scene.idle(25);
		scene.world.hideSection(util.select.position(potPosition), Direction.UP);
		scene.world.hideSection(util.select.position(deployerPos.up()), Direction.EAST);
		scene.idle(20);

		Vec3d filterSlot = frontVec.add(0.375, 0.25, 0);
		scene.overlay.showFilterSlotInput(filterSlot, 80);
		scene.overlay.showText(40)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(filterSlot)
			.text("Deployers carry a filter slot");
		scene.idle(50);

		ItemStack shears = new ItemStack(Items.SHEARS);

		scene.overlay.showControls(new InputWindowElement(filterSlot, Pointing.DOWN).rightClick()
			.withItem(shears), 40);
		scene.idle(7);
		scene.world.setFilterData(deployerSelection, teType, shears);
		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(filterSlot)
			.text("When a filter is set, it activates only while holding a matching item");
		scene.idle(70);

		ElementLink<EntityElement> sheep = scene.world.createEntity(w -> {
			SheepEntity entity = EntityType.SHEEP.create(w);
			entity.setFleeceColor(DyeColor.PINK);
			Vec3d p = util.vector.topOf(util.grid.at(1, 0, 2));
			entity.setPosition(p.x, p.y, p.z);
			entity.prevPosX = p.x;
			entity.prevPosY = p.y;
			entity.prevPosZ = p.z;
			entity.limbSwing = 0;
			entity.prevRotationYaw = 210;
			entity.rotationYaw = 210;
			entity.prevRotationYawHead = 210;
			entity.rotationYawHead = 210;
			return entity;
		});
		scene.idle(20);
		scene.world.showSection(util.select.position(deployerPos.up()), Direction.WEST);
		entity1 = scene.world.createItemEntity(entitySpawn, util.vector.of(0, 0.2, 0), shears);
		scene.idle(17);
		scene.world.modifyEntity(entity1, Entity::remove);
		scene.world.modifyTileNBT(deployerSelection, teType, nbt -> nbt.put("HeldItem", shears.serializeNBT()));
		scene.idle(10);

		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(util.vector.of(3, 2.5, 3))
			.text("Only items matching the filter can now be inserted...");

		scene.idle(70);
		scene.world.moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world.modifyEntity(sheep, e -> ((SheepEntity) e).setSheared(true));
		scene.effects.emitParticles(util.vector.topOf(deployerPos.west(2))
			.add(0, -.25, 0),
			Emitter.withinBlockSpace(new BlockParticleData(ParticleTypes.BLOCK, Blocks.PINK_WOOL.getDefaultState()),
				util.vector.of(0, 0, 0)),
			25, 1);
		scene.world.moveDeployer(deployerPos, -1, 25);
		scene.world.showSection(util.select.position(deployerPos.north()), Direction.SOUTH);
		scene.idle(25);

		scene.overlay.showText(80)
			.placeNearTarget()
			.pointAt(util.vector.of(3.5, 1.25, 1.25))
			.text("...and only non-matching items will be extracted");
		scene.world.flapFunnel(deployerPos.north(), true);
		scene.world.createItemEntity(util.vector.centerOf(deployerPos.north())
			.subtract(0, .45, 0), util.vector.of(0, 0, -0.1), new ItemStack(Items.PINK_WOOL));

		scene.markAsFinished();
		for (int i = 0; i < 10; i++) {
			scene.idle(26);
			scene.world.moveDeployer(deployerPos, 1, 25);
			scene.idle(26);
			scene.world.moveDeployer(deployerPos, -1, 25);
			scene.idle(26);
		}
	}

	public static void modes(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("deployer_modes", "Modes of the Deployer");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 3, 3, 1, 5), Direction.DOWN);
		scene.idle(10);

		BlockPos deployerPos = util.grid.at(3, 1, 2);
		Vec3d frontVec = util.vector.blockSurface(deployerPos, Direction.WEST)
			.add(-.125, 0, 0);
		Selection grassBlock = util.select.position(1, 1, 2);

		Selection deployerSelection = util.select.position(deployerPos);
		scene.world.showSection(deployerSelection, Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(grassBlock, Direction.DOWN);
		scene.idle(10);

		ItemStack tool = new ItemStack(Items.GOLDEN_HOE);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(deployerPos), Pointing.DOWN).withItem(tool),
			30);
		scene.idle(7);
		scene.world.modifyTileNBT(deployerSelection, DeployerTileEntity.class,
			nbt -> nbt.put("HeldItem", tool.serializeNBT()));
		scene.idle(45);

		scene.world.setKineticSpeed(util.select.position(2, 0, 5), 16);
		scene.world.setKineticSpeed(util.select.layer(1), -32);
		scene.world.moveDeployer(deployerPos, 1, 25);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(1, 1, 2))
			.text("By default, a Deployer imitates a Right-click interaction");

		scene.idle(26);
		scene.world.replaceBlocks(grassBlock, Blocks.FARMLAND.getDefaultState(), false);
		scene.world.moveDeployer(deployerPos, -1, 25);
		scene.idle(46);

		scene.overlay.showControls(new InputWindowElement(frontVec, Pointing.LEFT).rightClick()
			.withWrench(), 40);
		scene.idle(7);
		scene.world.modifyTileNBT(deployerSelection, DeployerTileEntity.class, nbt -> nbt.putString("Mode", "PUNCH"));
		scene.idle(45);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(1, 1, 2))
			.text("Using a Wrench, it can be set to imitate a Left-click instead");

		BlockPos breakingPos = deployerPos.west(2);
		for (int i = 0; i < 4; i++) {
			scene.idle(26);
			scene.world.moveDeployer(deployerPos, 1, 25);
			scene.idle(26);
			scene.world.incrementBlockBreakingProgress(breakingPos);
			scene.world.incrementBlockBreakingProgress(breakingPos);
			scene.world.incrementBlockBreakingProgress(breakingPos);
			scene.world.moveDeployer(deployerPos, -1, 25);
			if (i == 3)
				scene.world.createItemEntity(util.vector.centerOf(breakingPos), util.vector.of(0, 0, 0),
					new ItemStack(Blocks.DIRT));
			scene.idle(26);

			if (i == 0)
				scene.markAsFinished();
		}
	}

	public static void redstone(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("deployer_redstone", "Controlling Deployers with Redstone");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 5, 3, 1, 3), Direction.DOWN);

		BlockPos deployerPos = util.grid.at(3, 1, 3);
		Selection redstone = util.select.fromTo(3, 1, 1, 3, 1, 2);
		BlockPos leverPos = util.grid.at(3, 1, 1);

		scene.world.toggleRedstonePower(redstone);

		scene.idle(26);
		scene.world.moveDeployer(deployerPos, 1, 30);
		scene.idle(31);
		scene.world.moveDeployer(deployerPos, -1, 30);
		scene.world.showSection(redstone, Direction.SOUTH);
		scene.idle(31);

		scene.world.toggleRedstonePower(redstone);
		scene.effects.indicateRedstone(leverPos);
		scene.idle(10);

		scene.overlay.showText(60)
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(deployerPos))
			.placeNearTarget()
			.text("When powered by Redstone, Deployers will not activate");
		scene.idle(70);

		scene.world.toggleRedstonePower(redstone);
		scene.idle(10);
		scene.world.moveDeployer(deployerPos, 1f, 30);
		scene.idle(10);

		scene.world.toggleRedstonePower(redstone);
		scene.effects.indicateRedstone(leverPos);
		scene.idle(21);

		scene.overlay.showText(60)
			.pointAt(util.vector.topOf(deployerPos))
			.placeNearTarget()
			.text("Before stopping, the Deployer will finish any started cycles");

		scene.world.moveDeployer(deployerPos, -1f, 30);
		scene.idle(70);

		scene.world.toggleRedstonePower(redstone);
		scene.idle(3);
		scene.world.toggleRedstonePower(redstone);
		scene.effects.indicateRedstone(leverPos);
		scene.world.moveDeployer(deployerPos, 1, 30);
		scene.overlay.showText(100)
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(deployerPos))
			.placeNearTarget()
			.text("Thus, a negative pulse can be used to trigger exactly one activation cycle");
		scene.idle(31);
		scene.world.moveDeployer(deployerPos, -1, 30);

	}

	public static void contraption(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("deployer_contraption", "Using Deployers on Contraptions");
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(.9f);
		Selection flowers = util.select.fromTo(4, 1, 1, 1, 1, 1);
		scene.world.replaceBlocks(flowers, Blocks.AIR.getDefaultState(), false);

		Selection kinetics = util.select.fromTo(5, 1, 6, 5, 1, 3);
		BlockPos deployerPos = util.grid.at(4, 1, 3);
		Selection deployerSelection = util.select.position(deployerPos);

		scene.world.showSection(util.select.layer(0)
			.add(flowers), Direction.UP);
		scene.idle(5);

		ElementLink<WorldSectionElement> pistonHead =
			scene.world.showIndependentSection(util.select.fromTo(5, 1, 2, 8, 1, 2), Direction.DOWN);
		scene.world.moveSection(pistonHead, util.vector.of(0, 0, 1), 0);
		scene.world.showSection(kinetics, Direction.DOWN);
		scene.idle(5);

		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(deployerSelection, Direction.DOWN);
		scene.idle(5);
		scene.world.glueBlockOnto(util.grid.at(4, 2, 3), Direction.DOWN, contraption);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(deployerPos, Direction.WEST))
			.text("Whenever Deployers are moved as part of an animated Contraption...");
		scene.idle(70);

		scene.world.setKineticSpeed(util.select.position(4, 0, 6), -8);
		scene.world.setKineticSpeed(kinetics, 16);
		scene.world.moveSection(pistonHead, util.vector.of(-3, 0, 0), 100);
		scene.world.moveSection(contraption, util.vector.of(-3, 0, 0), 100);

		for (int x = 0; x < 4; x++) {
			scene.world.moveDeployer(deployerPos, 1, 9);
			scene.idle(10);
			scene.world.moveDeployer(deployerPos, -1, 9);
			scene.world.restoreBlocks(util.select.position(4 - x, 1, 1));
			scene.idle(18);
		}

		scene.overlay.showSelectionWithText(flowers, 90)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("They activate at each visited location, using items from inventories anywhere on the contraption");
		scene.idle(100);

		scene.world.hideSection(flowers, Direction.UP);
		scene.idle(15);
		scene.world.replaceBlocks(flowers, Blocks.AIR.getDefaultState(), false);
		scene.world.showSection(flowers, Direction.UP);

		Vec3d frontVec = util.vector.blockSurface(deployerPos.west(3), Direction.NORTH)
			.add(0, 0, -.125);
		Vec3d filterSlot = frontVec.add(0, 0.25, 0.375);
		scene.overlay.showFilterSlotInput(filterSlot, 80);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(filterSlot)
			.text("The Filter slot can be used to specify which items to pull");
		scene.idle(70);

		ItemStack poppy = new ItemStack(Items.POPPY);
		scene.overlay.showControls(new InputWindowElement(filterSlot, Pointing.DOWN).withItem(poppy), 30);
		scene.idle(7);
		scene.world.setFilterData(deployerSelection, DeployerTileEntity.class, poppy);
		scene.idle(25);

		scene.world.setKineticSpeed(util.select.position(4, 0, 6), 8);
		scene.world.setKineticSpeed(kinetics, -16);
		scene.world.moveSection(pistonHead, util.vector.of(3, 0, 0), 100);
		scene.world.moveSection(contraption, util.vector.of(3, 0, 0), 100);

		for (int x = 0; x < 4; x++) {
			scene.world.moveDeployer(deployerPos, 1, 9);
			scene.idle(10);
			scene.world.moveDeployer(deployerPos, -1, 9);
			scene.world.setBlock(util.grid.at(1 + x, 1, 1), Blocks.POPPY.getDefaultState(), false);
			scene.idle(18);
		}

	}

}
