package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.content.contraptions.components.actors.HarvesterBlockEntity;
import com.simibubi.create.content.contraptions.components.actors.PortableItemInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.components.actors.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.LinearChassisBlock;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement.FlappyPose;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MovementActorScenes {

	public static void psiTransfer(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("portable_storage_interface", "Contraption Storage Exchange");
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(0.95f);
		scene.setSceneOffsetY(-1);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		BlockPos bearing = util.grid.at(5, 1, 2);
		scene.world.showSection(util.select.position(bearing), Direction.DOWN);
		scene.idle(5);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.fromTo(5, 2, 2, 6, 3, 2), Direction.DOWN);
		scene.world.configureCenterOfRotation(contraption, util.vector.centerOf(bearing));
		scene.idle(10);
		scene.world.rotateBearing(bearing, 360, 70);
		scene.world.rotateSection(contraption, 0, 360, 0, 70);
		scene.overlay.showText(60)
			.pointAt(util.vector.topOf(bearing.above(2)))
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Moving inventories can be tricky to access with automation.");

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
			.text("This component can interact with storage without the need to stop the contraption.");
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
		scene.idle(38);

		Selection both = util.select.fromTo(2, 2, 2, 4, 2, 2);
		Class<PortableItemInterfaceBlockEntity> psiClass = PortableItemInterfaceBlockEntity.class;

		scene.world.modifyBlockEntityNBT(both, psiClass, nbt -> {
			nbt.putFloat("Distance", 1);
			nbt.putFloat("Timer", 12);
		});

		scene.idle(17);
		scene.overlay.showOutline(PonderPalette.GREEN, psi, util.select.fromTo(5, 3, 2, 6, 3, 2), 80);
		scene.idle(10);

		scene.overlay.showSelectionWithText(util.select.position(psi2), 70)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.text("While engaged, the stationary interface will represent ALL inventories on the contraption");

		scene.idle(80);

		BlockPos hopper = util.grid.at(2, 3, 2);
		scene.world.showSection(util.select.position(hopper), Direction.DOWN);
		scene.overlay.showText(70)
			.placeNearTarget()
			.pointAt(util.vector.topOf(hopper))
			.attachKeyFrame()
			.text("Items can now be inserted...");

		ItemStack itemStack = new ItemStack(Items.COPPER_INGOT);
		Vec3 entitySpawn = util.vector.topOf(hopper.above(3));

		ElementLink<EntityElement> entity1 =
			scene.world.createItemEntity(entitySpawn, util.vector.of(0, 0.2, 0), itemStack);
		scene.idle(10);
		ElementLink<EntityElement> entity2 =
			scene.world.createItemEntity(entitySpawn, util.vector.of(0, 0.2, 0), itemStack);
		scene.idle(10);
		scene.world.modifyEntity(entity1, Entity::discard);
		scene.idle(10);
		scene.world.modifyEntity(entity2, Entity::discard);

		scene.overlay
			.showControls(new InputWindowElement(util.vector.topOf(5, 3, 2), Pointing.DOWN).withItem(itemStack), 40);

		scene.idle(30);
		scene.world.hideSection(util.select.position(hopper), Direction.UP);
		scene.idle(15);

		BlockPos beltPos = util.grid.at(1, 1, 2);
		scene.world.showSection(util.select.fromTo(0, 1, 0, 1, 2, 6), Direction.DOWN);
		scene.idle(10);
		scene.world.createItemOnBelt(beltPos, Direction.EAST, itemStack.copy());
		scene.overlay.showText(40)
			.placeNearTarget()
			.pointAt(util.vector.topOf(beltPos.above()))
			.text("...or extracted from the contraption");
		scene.idle(15);
		scene.world.createItemOnBelt(beltPos, Direction.EAST, itemStack);

		scene.idle(20);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.idle(15);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);

		scene.overlay.showText(120)
			.placeNearTarget()
			.pointAt(util.vector.topOf(psi2))
			.text("After no items have been exchanged for a while, the contraption will continue on its way");
		scene.world.modifyBlockEntityNBT(both, psiClass, nbt -> nbt.putFloat("Timer", 2));

		scene.idle(15);
		scene.markAsFinished();
		scene.world.rotateBearing(bearing, 270, 120);
		scene.world.rotateSection(contraption, 0, 270, 0, 120);
	}

	public static void psiRedstone(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("portable_storage_interface_redstone", "Redstone Control");
		scene.configureBasePlate(0, 0, 5);
		scene.setSceneOffsetY(-1);

		Class<PortableStorageInterfaceBlockEntity> psiClass = PortableStorageInterfaceBlockEntity.class;
		Selection psis = util.select.fromTo(1, 1, 3, 1, 3, 3);
		scene.world.modifyBlockEntityNBT(psis, psiClass, nbt -> {
			nbt.putFloat("Distance", 1);
			nbt.putFloat("Timer", 12);
		});

		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layer(1), Direction.DOWN);
		scene.idle(5);

		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.layersFrom(2), Direction.DOWN);
		BlockPos bearing = util.grid.at(3, 1, 3);
		scene.world.configureCenterOfRotation(contraption, util.vector.topOf(bearing));
		scene.idle(20);
		scene.world.modifyBlockEntityNBT(psis, psiClass, nbt -> nbt.putFloat("Timer", 2));
		scene.world.rotateBearing(bearing, 360 * 3 + 270, 240 + 60);
		scene.world.rotateSection(contraption, 0, 360 * 3 + 270, 0, 240 + 60);
		scene.idle(20);

		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 1, 1, 1, 2));
		scene.effects.indicateRedstone(util.grid.at(1, 1, 1));

		scene.idle(10);

		scene.overlay.showSelectionWithText(util.select.position(1, 1, 3), 120)
			.colored(PonderPalette.RED)
			.text("Redstone power will prevent the stationary interface from engaging");

		scene.idle(20);
		scene.markAsFinished();
	}

	public static void harvester(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_harvester", "Using Mechanical Harvesters on Contraptions");
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(0.9f);

		Selection crops = util.select.fromTo(4, 1, 2, 3, 1, 2)
			.add(util.select.fromTo(3, 1, 1, 2, 1, 1)
				.add(util.select.position(2, 1, 3))
				.add(util.select.position(1, 1, 2)));

		scene.world.setBlocks(crops, Blocks.WHEAT.defaultBlockState()
			.setValue(CropBlock.AGE, 7), false);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos bearingPos = util.grid.at(4, 1, 4);

		scene.idle(5);
		scene.world.showSection(crops, Direction.UP);
		scene.world.showSection(util.select.position(bearingPos), Direction.DOWN);
		scene.idle(5);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.fromTo(4, 2, 4, 2, 2, 5)
				.add(util.select.fromTo(2, 1, 5, 0, 1, 5)), Direction.DOWN);
		scene.world.configureCenterOfRotation(contraption, util.vector.centerOf(bearingPos));
		scene.idle(10);

		for (int i = 0; i < 3; i++) {
			scene.world.showSectionAndMerge(util.select.position(i, 1, 4), Direction.SOUTH, contraption);
			scene.idle(5);
		}

		scene.overlay.showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 4), Direction.SOUTH))
			.text("Whenever Harvesters are moved as part of an animated Contraption...");
		scene.idle(70);

		for (int i = 0; i < 3; i++)
			scene.world.modifyBlockEntity(util.grid.at(i, 1, 4), HarvesterBlockEntity.class,
				hte -> hte.setAnimatedSpeed(-150));
		scene.world.rotateBearing(bearingPos, -360, 140);
		scene.world.rotateSection(contraption, 0, -360, 0, 140);

		BlockState harvested = Blocks.WHEAT.defaultBlockState();
		ItemStack wheatItem = new ItemStack(Items.WHEAT);

		scene.idle(5);
		BlockPos current = util.grid.at(2, 1, 3);
		scene.world.setBlock(current, harvested, true);
		scene.world.createItemEntity(util.vector.centerOf(current), util.vector.of(0, 0.3, -.2), wheatItem);
		scene.idle(5);
		current = util.grid.at(1, 1, 2);
		scene.world.setBlock(current, harvested, true);
		scene.world.createItemEntity(util.vector.centerOf(current), util.vector.of(0, 0.3, -.2), wheatItem);
		scene.idle(5);
		current = util.grid.at(3, 1, 2);
		scene.world.setBlock(current, harvested, true);
		scene.world.createItemEntity(util.vector.centerOf(current), util.vector.of(.1, 0.3, -.1), wheatItem);
		current = util.grid.at(2, 1, 1);
		scene.world.setBlock(current, harvested, true);
		scene.world.createItemEntity(util.vector.centerOf(current), util.vector.of(.1, 0.3, -.1), wheatItem);
		scene.idle(5);
		current = util.grid.at(3, 1, 1);
		scene.world.setBlock(current, harvested, true);
		scene.world.createItemEntity(util.vector.centerOf(current), util.vector.of(.1, 0.3, -.1), wheatItem);
		scene.idle(5);
		current = util.grid.at(4, 1, 2);
		scene.world.setBlock(current, harvested, true);
		scene.world.createItemEntity(util.vector.centerOf(current), util.vector.of(.2, 0.3, 0), wheatItem);

		scene.overlay.showText(80)
			.pointAt(util.vector.topOf(1, 0, 2))
			.text("They will harvest and reset any mature crops on their way")
			.placeNearTarget();

		scene.idle(101);
		scene.world.hideSection(crops, Direction.DOWN);
		scene.idle(15);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.world.setBlocks(crops, Blocks.WHEAT.defaultBlockState()
			.setValue(CropBlock.AGE, 7), false);
		scene.world.showSection(crops, Direction.UP);

		for (int i = 0; i < 3; i++)
			scene.world.modifyBlockEntity(util.grid.at(i, 1, 4), HarvesterBlockEntity.class,
				hte -> hte.setAnimatedSpeed(0));
		scene.idle(10);

		scene.world.cycleBlockProperty(util.grid.at(1, 1, 5), LinearChassisBlock.STICKY_TOP);
		scene.world.glueBlockOnto(util.grid.at(1, 2, 5), Direction.DOWN, contraption);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 5), Direction.WEST))
			.sharedText("storage_on_contraption");
		scene.idle(70);

		for (int i = 0; i < 3; i++)
			scene.world.modifyBlockEntity(util.grid.at(i, 1, 4), HarvesterBlockEntity.class,
				hte -> hte.setAnimatedSpeed(-150));
		scene.world.rotateBearing(bearingPos, -360, 140);
		scene.world.rotateSection(contraption, 0, -360, 0, 140);

		scene.idle(5);
		current = util.grid.at(2, 1, 3);
		scene.world.setBlock(current, harvested, true);
		scene.idle(5);
		current = util.grid.at(1, 1, 2);
		scene.world.setBlock(current, harvested, true);
		scene.idle(5);
		current = util.grid.at(3, 1, 2);
		scene.world.setBlock(current, harvested, true);
		current = util.grid.at(2, 1, 1);
		scene.world.setBlock(current, harvested, true);
		scene.idle(5);
		current = util.grid.at(3, 1, 1);
		scene.world.setBlock(current, harvested, true);
		scene.idle(5);
		current = util.grid.at(4, 1, 2);
		scene.world.setBlock(current, harvested, true);

		scene.idle(116);
		scene.overlay
			.showControls(new InputWindowElement(util.vector.topOf(1, 2, 5), Pointing.DOWN).withItem(wheatItem), 50);
		for (int i = 0; i < 3; i++)
			scene.world.modifyBlockEntity(util.grid.at(i, 1, 4), HarvesterBlockEntity.class,
				hte -> hte.setAnimatedSpeed(0));
	}

	public static void plough(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_plough", "Using Mechanical Ploughs on Contraptions");
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(0.9f);

		Selection garbage = util.select.fromTo(2, 1, 3, 1, 1, 2);
		Selection kinetics = util.select.fromTo(5, 1, 6, 5, 1, 2);
		Selection dynamic = util.select.fromTo(4, 0, 6, 5, 1, 6);

		scene.showBasePlate();
		ElementLink<WorldSectionElement> cogs =
			scene.world.showIndependentSection(util.select.fromTo(4, 0, 6, 5, 1, 6), Direction.UP);
		scene.idle(5);
		scene.world.showSection(kinetics.substract(dynamic), Direction.DOWN);
		ElementLink<WorldSectionElement> pistonHead =
			scene.world.showIndependentSection(util.select.fromTo(5, 1, 1, 7, 1, 1), Direction.DOWN);
		scene.world.moveSection(pistonHead, util.vector.of(0, 0, 1), 0);
		scene.idle(5);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.fromTo(4, 1, 3, 4, 1, 2), Direction.DOWN);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.position(3, 1, 3), Direction.EAST, contraption);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(3, 1, 2), Direction.EAST, contraption);
		scene.idle(20);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 3), Direction.EAST))
			.text("Whenever Ploughs are moved as part of an animated Contraption...");
		scene.idle(50);
		scene.world.showSection(garbage, Direction.EAST);
		scene.idle(20);

		scene.world.setKineticSpeed(util.select.position(4, 0, 6), -8);
		scene.world.setKineticSpeed(kinetics, 16);
		scene.world.moveSection(pistonHead, util.vector.of(-2, 0, 0), 60);
		scene.world.moveSection(contraption, util.vector.of(-2, 0, 0), 60);
		scene.idle(15);

		Vec3 m = util.vector.of(-0.1, .2, 0);
		scene.world.destroyBlock(util.grid.at(2, 1, 3));
		scene.world.createItemEntity(util.vector.centerOf(2, 1, 3), m, new ItemStack(Items.LEVER));
		scene.world.destroyBlock(util.grid.at(2, 1, 2));
		scene.world.createItemEntity(util.vector.centerOf(2, 1, 2), m, new ItemStack(Items.TORCH));

		scene.idle(30);

		scene.world.destroyBlock(util.grid.at(1, 1, 3));
		scene.world.createItemEntity(util.vector.centerOf(1, 1, 3), m, new ItemStack(Items.RAIL));
		scene.world.destroyBlock(util.grid.at(1, 1, 2));
		scene.world.createItemEntity(util.vector.centerOf(1, 1, 2), m, new ItemStack(Items.REDSTONE));

		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 3), Direction.EAST))
			.text("...they will break blocks without a solid collision hitbox");
		scene.idle(50);

		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -f);
		scene.world.moveSection(pistonHead, util.vector.of(2, 0, 0), 40);
		scene.world.moveSection(contraption, util.vector.of(2, 0, 0), 40);
		scene.world.hideSection(garbage, Direction.UP);
		scene.idle(40);
		scene.world.setBlocks(garbage, Blocks.SNOW.defaultBlockState(), false);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		ElementLink<WorldSectionElement> chest =
			scene.world.showIndependentSection(util.select.position(4, 2, 2), Direction.DOWN);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(4, 2, 2), Direction.WEST))
			.sharedText("storage_on_contraption");
		scene.idle(15);
		scene.effects.superGlue(util.grid.at(4, 2, 2), Direction.DOWN, true);
		scene.idle(45);
		scene.world.showSection(garbage, Direction.EAST);
		scene.idle(20);

		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -f);
		scene.world.moveSection(pistonHead, util.vector.of(-2, 0, 0), 60);
		scene.world.moveSection(contraption, util.vector.of(-2, 0, 0), 60);
		scene.world.moveSection(chest, util.vector.of(-2, 0, 0), 60);
		scene.idle(15);
		scene.world.destroyBlock(util.grid.at(2, 1, 3));
		scene.world.destroyBlock(util.grid.at(2, 1, 2));
		scene.idle(30);
		scene.world.destroyBlock(util.grid.at(1, 1, 3));
		scene.world.destroyBlock(util.grid.at(1, 1, 2));
		scene.idle(15);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.topOf(2, 2, 2), Pointing.DOWN).withItem(new ItemStack(Items.SNOWBALL)),
			40);
		scene.idle(40);
		scene.world.hideIndependentSection(chest, Direction.UP);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -f);
		scene.world.moveSection(pistonHead, util.vector.of(2, 0, 0), 40);
		scene.world.moveSection(contraption, util.vector.of(2, 0, 0), 40);
		scene.idle(40);

		Selection dirt = util.select.fromTo(2, 0, 3, 1, 0, 2);
		scene.world.hideSection(dirt, Direction.DOWN);
		scene.idle(15);
		scene.world.setBlocks(dirt, Blocks.GRASS_BLOCK.defaultBlockState(), false);
		scene.world.showSection(dirt, Direction.UP);
		scene.overlay.showText(60)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 3), Direction.EAST))
			.text("Additionally, ploughs can create farmland");
		scene.idle(30);

		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -f);
		scene.world.moveSection(pistonHead, util.vector.of(-2, 0, 0), 60);
		scene.world.moveSection(contraption, util.vector.of(-2, 0, 0), 60);
		scene.world.moveSection(chest, util.vector.of(-2, 0, 0), 60);
		scene.idle(15);
		scene.world.setBlocks(util.select.fromTo(2, 0, 2, 2, 0, 3), Blocks.FARMLAND.defaultBlockState(), true);
		scene.idle(30);
		scene.world.setBlocks(util.select.fromTo(1, 0, 2, 1, 0, 3), Blocks.FARMLAND.defaultBlockState(), true);
		scene.idle(20);

		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -f);
		scene.world.moveSection(pistonHead, util.vector.of(2, 0, 0), 40);
		scene.world.moveSection(contraption, util.vector.of(2, 0, 0), 40);

		scene.idle(50);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		scene.world.hideSection(kinetics.substract(dynamic), Direction.EAST);
		scene.world.hideSection(dirt, Direction.DOWN);
		scene.world.hideIndependentSection(pistonHead, Direction.EAST);
		scene.world.moveSection(cogs, util.vector.of(-1, 0, 0), 15);
		scene.idle(15);
		scene.world.restoreBlocks(dirt);
		scene.world.showSection(dirt, Direction.UP);
		scene.world.showSection(util.select.fromTo(4, 1, 6, 4, 3, 4), Direction.NORTH);
		scene.idle(15);
		scene.world.showSectionAndMerge(util.select.fromTo(4, 3, 3, 4, 2, 3), Direction.DOWN, contraption);
		scene.idle(15);
		
		BlockPos bearingPos = util.grid.at(4, 3, 4);
		scene.addKeyframe();
		
		scene.world.setKineticSpeed(util.select.position(4, 0, 6), 8);
		scene.world.setKineticSpeed(util.select.position(5, 1, 6), -16);
		scene.world.setKineticSpeed(util.select.position(4, 3, 5), -16);
		scene.world.setKineticSpeed(util.select.position(4, 1, 5), -16);
		scene.world.setKineticSpeed(util.select.position(4, 2, 5), 16);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -2 * f);
		scene.world.configureCenterOfRotation(contraption, util.vector.centerOf(bearingPos));
		scene.world.rotateSection(contraption, 0, 0, 90, 20);
		scene.world.rotateBearing(bearingPos, 90, 20);

		scene.idle(10);
		ElementLink<ParrotElement> birb = scene.special.createBirb(util.vector.topOf(3, 0, 2)
			.add(0, 0, 0.5), FlappyPose::new);
		scene.idle(11);

		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -2 * f);
		scene.world.rotateSection(contraption, 0, 0, -135, 10);
		scene.world.rotateBearing(bearingPos, -135, 10);
		scene.idle(7);
		scene.special.moveParrot(birb, util.vector.of(-20, 15, 0), 20);
		scene.special.rotateParrot(birb, 0, 360, 0, 20);
		scene.idle(3);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		scene.idle(20);

		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(util.vector.centerOf(util.grid.at(1, 3, 2)))
			.text("...they can also launch entities without hurting them");
		scene.idle(30);
	}

}
