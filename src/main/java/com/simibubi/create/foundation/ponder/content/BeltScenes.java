package com.simibubi.create.foundation.ponder.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour.Mode;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutBlockEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlockEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltPart;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmBlockEntity.Phase;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement.FaceCursorPose;
import com.simibubi.create.foundation.ponder.element.ParrotElement.FacePointOfInterestPose;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BeltScenes {

	public static void beltConnector(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("belt_connector", "Using Mechanical Belts");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.world.showSection(util.select.position(3, 0, 5), Direction.UP);
		scene.idle(5);

		scene.world.showSection(util.select.fromTo(4, 1, 3, 4, 1, 5), Direction.DOWN);
		ElementLink<WorldSectionElement> shafts =
			scene.world.showIndependentSection(util.select.fromTo(0, 1, 3, 4, 1, 3), Direction.DOWN);
		scene.world.moveSection(shafts, util.vector.of(0, 0, -1), 0);
		scene.world.setKineticSpeed(util.select.position(0, 1, 3), 0);
		scene.idle(20);

		BlockPos backEnd = util.grid.at(4, 1, 2);
		BlockPos frontEnd = util.grid.at(0, 1, 2);
		ItemStack beltItem = AllItems.BELT_CONNECTOR.asStack();
		Vec3 backEndCenter = util.vector.centerOf(backEnd);
		AABB connectBB = new AABB(backEndCenter, backEndCenter);
		AABB shaftBB = AllBlocks.SHAFT.getDefaultState()
			.setValue(ShaftBlock.AXIS, Axis.Z)
			.getShape(null, null)
			.bounds();

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(backEnd), Pointing.DOWN).rightClick()
			.withItem(beltItem), 57);
		scene.idle(7);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, backEnd, shaftBB.move(backEnd), 42);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.BLACK, backEndCenter, connectBB, 50);
		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(frontEnd), Pointing.DOWN).rightClick()
			.withItem(beltItem), 37);
		scene.idle(7);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, frontEnd, shaftBB.move(frontEnd), 17);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.BLACK, backEndCenter, connectBB.expandTowards(-4, 0, 0),
			20);
		scene.idle(20);

		scene.world.moveSection(shafts, util.vector.of(0, -2, 0), 0);
		scene.world.showSection(util.select.fromTo(0, 1, 2, 4, 1, 2), Direction.SOUTH);
		scene.idle(20);

		scene.overlay.showText(80)
			.text("Right-Clicking two shafts with a belt item will connect them together")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(2, 1, 2));
		scene.idle(90);

		Vec3 falseSelection = util.vector.topOf(backEnd.south(1));
		scene.overlay.showControls(new InputWindowElement(falseSelection, Pointing.DOWN).rightClick()
			.withItem(beltItem), 37);
		scene.idle(7);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, backEnd, shaftBB.move(backEnd.south(1)), 50);

		scene.overlay.showText(80)
			.colored(PonderPalette.RED)
			.text("Accidental selections can be canceled with Right-Click while Sneaking")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.centerOf(backEnd.south(1)));
		scene.idle(43);

		scene.overlay.showControls(new InputWindowElement(falseSelection, Pointing.DOWN).rightClick()
			.withItem(beltItem)
			.whileSneaking(), 20);
		scene.idle(60);

		BlockPos shaftLocation = frontEnd.east();
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(shaftLocation), Pointing.DOWN).rightClick()
			.withItem(AllBlocks.SHAFT.asStack()), 50);
		scene.idle(7);
		scene.world.modifyBlock(shaftLocation, s -> s.setValue(BeltBlock.PART, BeltPart.PULLEY), true);
		scene.idle(10);

		scene.overlay.showText(43)
			.text("Additional Shafts can be added throughout the Belt")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(shaftLocation, Direction.NORTH));
		scene.idle(50);

		Selection attachedShafts = util.select.fromTo(0, 1, 1, 1, 1, 1);
		scene.world.showSection(attachedShafts, Direction.SOUTH);
		scene.world.setKineticSpeed(attachedShafts, 32);
		scene.idle(10);
		scene.effects.rotationDirectionIndicator(util.grid.at(0, 1, 1));
		scene.effects.rotationDirectionIndicator(util.grid.at(1, 1, 1));
		scene.idle(20);

		scene.overlay.showText(50)
			.text("Shafts connected via Belts will rotate with Identical Speed and Direction")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(0, 1, 1), Direction.NORTH));
		scene.idle(60);

		scene.world.hideSection(attachedShafts, Direction.NORTH);
		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(shaftLocation), Pointing.DOWN).rightClick()
			.withWrench(), 50);
		scene.idle(7);
		scene.world.modifyBlock(shaftLocation, s -> s.setValue(BeltBlock.PART, BeltPart.MIDDLE), true);
		scene.idle(10);
		scene.overlay.showText(50)
			.text("Added shafts can be removed using the wrench")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(shaftLocation, Direction.NORTH));
		scene.idle(70);

		scene.overlay
			.showControls(new InputWindowElement(util.vector.topOf(shaftLocation.east()), Pointing.DOWN).rightClick()
				.withItem(new ItemStack(Items.BLUE_DYE)), 50);
		scene.idle(7);
		scene.world.modifyBlockEntityNBT(util.select.fromTo(0, 1, 2, 4, 1, 2), BeltBlockEntity.class,
			nbt -> NBTHelper.writeEnum(nbt, "Dye", DyeColor.BLUE));
		scene.idle(20);
		scene.overlay.showText(80)
			.colored(PonderPalette.BLUE)
			.text("Mechanical Belts can be dyed for aesthetic purposes")
			.placeNearTarget()
			.pointAt(util.vector.topOf(shaftLocation.east()));
		scene.idle(50);
	}

	public static void directions(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("belt_directions", "Valid Orientations for Mechanical Belts");
		scene.configureBasePlate(0, 0, 5);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();
		scene.idle(5);

		ElementLink<WorldSectionElement> leftShaft =
			scene.world.showIndependentSection(util.select.position(4, 1, 0), Direction.DOWN);
		ElementLink<WorldSectionElement> rightShaft =
			scene.world.showIndependentSection(util.select.position(0, 1, 0), Direction.DOWN);

		scene.world.moveSection(leftShaft, util.vector.of(0, 0, 2), 0);
		scene.world.moveSection(rightShaft, util.vector.of(0, 0, 2), 0);
		scene.idle(1);
		scene.world.moveSection(leftShaft, util.vector.of(-1, 0, 0), 10);
		scene.world.moveSection(rightShaft, util.vector.of(1, 1, 0), 10);

		scene.idle(20);

		Vec3 from = util.vector.centerOf(3, 1, 2);
		Vec3 to = util.vector.centerOf(1, 2, 2);

		scene.overlay.showLine(PonderPalette.RED, from, to, 70);
		scene.idle(10);
		scene.overlay.showLine(PonderPalette.GREEN, to.add(-1, -1, 0), from, 60);
		scene.overlay.showLine(PonderPalette.GREEN, from.add(0, 3, 0), from, 60);

		scene.idle(20);
		scene.overlay.showText(60)
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.pointAt(to)
			.text("Belts cannot connect in arbitrary directions");
		scene.idle(70);

		from = util.vector.centerOf(4, 1, 2);
		to = util.vector.centerOf(0, 1, 2);

		scene.world.moveSection(leftShaft, util.vector.of(1, 0, 0), 10);
		scene.world.moveSection(rightShaft, util.vector.of(-1, -1, 0), 10);
		scene.idle(10);
		scene.overlay.showLine(PonderPalette.GREEN, from, to, 40);
		scene.idle(10);
		scene.overlay.showText(40)
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(to)
			.attachKeyFrame()
			.text("1. They can connect horizontally");

		scene.idle(20);
		Selection firstBelt = util.select.fromTo(4, 1, 1, 0, 1, 1);
		ElementLink<WorldSectionElement> belt = scene.world.showIndependentSection(firstBelt, Direction.SOUTH);
		scene.world.moveSection(belt, util.vector.of(0, 0, 1), 0);
		scene.idle(20);
		scene.world.hideIndependentSection(belt, Direction.SOUTH);
		scene.idle(15);

		from = util.vector.centerOf(3, 3, 2);
		to = util.vector.centerOf(1, 1, 2);

		scene.world.moveSection(leftShaft, util.vector.of(-1, 2, 0), 10);
		scene.world.moveSection(rightShaft, util.vector.of(1, 0, 0), 10);
		scene.idle(10);
		scene.world.rotateSection(leftShaft, 0, 0, 25, 5);
		scene.world.rotateSection(rightShaft, 0, 0, 25, 5);
		scene.overlay.showLine(PonderPalette.GREEN, from, to, 40);
		scene.idle(10);
		scene.overlay.showText(40)
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(to)
			.attachKeyFrame()
			.text("2. They can connect diagonally");

		scene.idle(20);
		Selection secondBelt = util.select.fromTo(3, 3, 2, 1, 1, 2);
		belt = scene.world.showIndependentSection(secondBelt, Direction.SOUTH);
		scene.idle(20);
		scene.world.hideIndependentSection(belt, Direction.SOUTH);
		scene.idle(15);

		from = util.vector.centerOf(2, 4, 2);
		to = util.vector.centerOf(2, 1, 2);

		scene.world.moveSection(leftShaft, util.vector.of(-1, 1, 0), 10);
		scene.world.moveSection(rightShaft, util.vector.of(1, 0, 0), 10);
		scene.idle(10);
		scene.world.rotateSection(rightShaft, 0, 0, -25, 5);
		scene.overlay.showLine(PonderPalette.GREEN, from, to, 40);
		scene.idle(10);
		scene.overlay.showText(40)
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(to)
			.attachKeyFrame()
			.text("3. They can connect vertically");

		scene.idle(20);
		Selection thirdBelt = util.select.fromTo(2, 1, 3, 2, 4, 3);
		belt = scene.world.showIndependentSection(thirdBelt, Direction.SOUTH);
		scene.world.moveSection(belt, util.vector.of(0, 0, -1), 0);
		scene.idle(20);
		scene.world.hideIndependentSection(belt, Direction.SOUTH);
		scene.idle(15);

		from = util.vector.centerOf(4, 1, 2);
		to = util.vector.centerOf(0, 1, 2);

		scene.world.moveSection(leftShaft, util.vector.of(2, -3, 0), 10);
		scene.world.moveSection(rightShaft, util.vector.of(-2, 0, 0), 10);
		scene.idle(10);
		scene.world.rotateSection(rightShaft, 90, 0, -25, 5);
		scene.world.rotateSection(leftShaft, 90, 0, -50, 5);
		scene.overlay.showLine(PonderPalette.GREEN, from, to, 60);
		scene.idle(10);
		scene.overlay.showText(60)
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(to)
			.attachKeyFrame()
			.text("4. And they can connect vertical shafts horizontally");

		scene.idle(20);
		Selection fourthBelt = util.select.fromTo(4, 1, 4, 0, 1, 4);
		belt = scene.world.showIndependentSection(fourthBelt, Direction.DOWN);
		scene.world.moveSection(belt, util.vector.of(0, 1 / 512f, -2), 0);
		scene.idle(40);
		scene.world.hideIndependentSection(belt, Direction.UP);
		scene.idle(15);
		scene.world.hideIndependentSection(leftShaft, Direction.UP);
		scene.world.hideIndependentSection(rightShaft, Direction.UP);
		scene.idle(15);

		scene.world.showSection(firstBelt, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(secondBelt, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(thirdBelt, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(fourthBelt, Direction.DOWN);
		scene.idle(10);

		scene.overlay.showText(160)
			.text("These are all possible directions. Belts can span any Length between 2 and 20 blocks");
		scene.markAsFinished();
	}

	public static void transport(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("belt_transport", "Using Mechanical Belts for Logistics");
		scene.configureBasePlate(0, 0, 5);
		scene.setSceneOffsetY(-1);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -.6f * f);
		scene.showBasePlate();
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 1, 3, 2, 1, 5), Direction.DOWN);
		scene.idle(20);
		scene.world.showSection(util.select.fromTo(2, 1, 2, 4, 3, 2), Direction.SOUTH);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 1, 2, 0, 1, 2), Direction.SOUTH);
		scene.idle(10);
		scene.special.movePointOfInterest(util.grid.at(2, 2, 0));

		ItemStack stack = new ItemStack(Items.COPPER_BLOCK);
		ElementLink<EntityElement> item =
			scene.world.createItemEntity(util.vector.centerOf(0, 4, 2), util.vector.of(0, 0, 0), stack);
		scene.idle(13);
		scene.world.modifyEntity(item, Entity::discard);
		BlockPos beltEnd = util.grid.at(0, 1, 2);
		scene.world.createItemOnBelt(beltEnd, Direction.DOWN, stack);

		scene.idle(20);

		ElementLink<ParrotElement> parrot = scene.special.createBirb(util.vector.topOf(0, 1, 2)
			.add(0, -3 / 16f, 0), FacePointOfInterestPose::new);
		scene.special.moveParrot(parrot, util.vector.of(1.78, 0, 0), 40);
		scene.special.movePointOfInterest(util.grid.at(1, 1, 3));

		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(util.vector.topOf(beltEnd))
			.text("Moving belts will transport Items and other Entities");

		scene.idle(20);
		item = scene.world.createItemEntity(util.vector.centerOf(0, 4, 2), util.vector.of(0, 0, 0), stack);
		scene.special.movePointOfInterest(util.grid.at(0, 3, 2));
		scene.idle(10);
		scene.special.movePointOfInterest(beltEnd);
		scene.idle(3);
		scene.world.modifyEntity(item, Entity::discard);
		scene.world.createItemOnBelt(beltEnd, Direction.DOWN, stack);
		scene.idle(8);

		scene.special.movePointOfInterest(util.grid.at(3, 2, 1));
		scene.special.moveParrot(parrot, util.vector.of(2.1, 2.1, 0), 60);
		scene.idle(20);
		scene.special.movePointOfInterest(util.grid.at(5, 5, 2));
		scene.idle(30);
		scene.special.movePointOfInterest(util.grid.at(2, 1, 5));
		scene.idle(10);
		scene.special.moveParrot(parrot, util.vector.of(.23, 0, 0), 5);
		scene.idle(5);
		scene.world.setKineticSpeed(util.select.everywhere(), 0f);
		scene.idle(10);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.special.movePointOfInterest(util.grid.at(2, 5, 4));

		Vec3 topOf = util.vector.topOf(util.grid.at(3, 2, 2))
			.add(-0.1, 0.3, 0);
		scene.overlay.showControls(new InputWindowElement(topOf, Pointing.DOWN).rightClick(), 60);
		scene.idle(10);
		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(topOf.subtract(0, 0.1, 0))
			.attachKeyFrame()
			.text("Right-Click with an empty hand to take items off a belt");
		scene.idle(20);
		scene.world.removeItemsFromBelt(util.grid.at(3, 2, 2));
		scene.effects.indicateSuccess(util.grid.at(3, 2, 2));
		scene.idle(20);

		scene.special.changeBirbPose(parrot, FaceCursorPose::new);
	}

	public static void beltsCanBeEncased(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("belt_casing", "Encasing Belts");
		scene.configureBasePlate(0, 0, 5);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		scene.idle(20);

		ItemStack brassCasingItem = AllBlocks.BRASS_CASING.asStack();
		ItemStack andesiteCasingItem = AllBlocks.ANDESITE_CASING.asStack();

		BlockPos beltPos = util.grid.at(3, 1, 0);
		BlockPos beltPos2 = util.grid.at(0, 2, 3);
		BlockPos beltPos3 = util.grid.at(1, 4, 4);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(beltPos), Pointing.DOWN).rightClick()
			.withItem(brassCasingItem), 20);
		scene.idle(7);
		scene.world.modifyBlock(beltPos, s -> s.setValue(BeltBlock.CASING, true), true);
		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(beltPos2), Pointing.DOWN).rightClick()
			.withItem(andesiteCasingItem), 20);
		scene.idle(7);
		scene.world.modifyBlock(beltPos2, s -> s.setValue(BeltBlock.CASING, true), true);
		scene.world.modifyBlockEntityNBT(util.select.position(beltPos2), BeltBlockEntity.class, nbt -> {
			NBTHelper.writeEnum(nbt, "Casing", BeltBlockEntity.CasingType.ANDESITE);
		});
		scene.idle(20);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(beltPos3, Direction.EAST), Pointing.RIGHT).rightClick()
				.withItem(brassCasingItem),
			20);
		scene.idle(7);
		scene.world.modifyBlock(beltPos3, s -> s.setValue(BeltBlock.CASING, true), true);
		scene.idle(20);

		scene.overlay.showText(80)
			.text("Brass or Andesite Casing can be used to decorate Mechanical Belts")
			.attachKeyFrame()
			.pointAt(util.vector.centerOf(beltPos2));

		scene.idle(40);

		List<BlockPos> brassBelts = new ArrayList<>();
		List<BlockPos> andesiteBelts = new ArrayList<>();

		for (int z = 1; z <= 3; z++)
			brassBelts.add(beltPos.south(z));
		for (int x = 1; x <= 3; x++)
			brassBelts.add(beltPos3.east(x)
				.below(x));
		for (int x = 1; x <= 3; x++)
			andesiteBelts.add(beltPos2.east(x));

		Collections.shuffle(andesiteBelts);
		Collections.shuffle(brassBelts);

		for (BlockPos pos : andesiteBelts) {
			scene.idle(4);
			scene.world.modifyBlock(pos, s -> s.setValue(BeltBlock.CASING, true), true);
			scene.world.modifyBlockEntityNBT(util.select.position(pos), BeltBlockEntity.class, nbt -> {
				NBTHelper.writeEnum(nbt, "Casing", BeltBlockEntity.CasingType.ANDESITE);
			});
		}
		for (BlockPos pos : brassBelts) {
			scene.idle(4);
			scene.world.modifyBlock(pos, s -> s.setValue(BeltBlock.CASING, true), true);
		}
		scene.idle(30);
		scene.addKeyframe();

		scene.overlay
			.showControls(new InputWindowElement(util.vector.topOf(beltPos.south()), Pointing.DOWN).rightClick()
				.withWrench(), 40);
		scene.idle(7);
		scene.world.modifyBlock(beltPos.south(), s -> s.setValue(BeltBlock.CASING, false), true);
		scene.overlay.showText(80)
			.text("A wrench can be used to remove the casing")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(beltPos.south(), Direction.WEST));
	}

	public static void depot(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("depot", "Using Depots");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);
		scene.world.setBlock(util.grid.at(3, 2, 2), Blocks.WATER.defaultBlockState(), false);

		BlockPos depotPos = util.grid.at(2, 1, 2);
		scene.world.showSection(util.select.position(2, 1, 2), Direction.DOWN);
		Vec3 topOf = util.vector.topOf(depotPos);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("Depots can serve as 'stationary' belt elements")
			.placeNearTarget()
			.pointAt(topOf);
		scene.idle(70);

		scene.overlay.showControls(new InputWindowElement(topOf, Pointing.DOWN).rightClick()
			.withItem(new ItemStack(Items.COPPER_BLOCK)), 20);
		scene.idle(7);
		scene.world.createItemOnBeltLike(depotPos, Direction.NORTH, new ItemStack(Items.COPPER_BLOCK));
		scene.idle(10);
		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("Right-Click to manually place or remove Items from it")
			.placeNearTarget()
			.pointAt(topOf);
		scene.idle(80);

		scene.overlay.showControls(new InputWindowElement(topOf, Pointing.DOWN).rightClick(), 20);
		scene.idle(7);
		scene.world.removeItemsFromBelt(depotPos);
		scene.effects.indicateSuccess(depotPos);
		scene.idle(20);

		scene.world.showSection(util.select.position(depotPos.above(2)), Direction.SOUTH);
		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("Just like Mechanical Belts, it can provide items to processing")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(depotPos.above(2), Direction.WEST));
		ItemStack bottle = new ItemStack(Items.BUCKET);
		scene.world.createItemOnBeltLike(depotPos, Direction.NORTH, bottle);
		scene.idle(20);
		scene.world.modifyBlockEntityNBT(util.select.position(depotPos.above(2)), SpoutBlockEntity.class,
			nbt -> nbt.putInt("ProcessingTicks", 20));
		scene.idle(20);
		scene.world.removeItemsFromBelt(depotPos);
		scene.world.createItemOnBeltLike(depotPos, Direction.UP, new ItemStack(Items.WATER_BUCKET));
		scene.world.modifyBlockEntityNBT(util.select.position(depotPos.above(2)), SpoutBlockEntity.class,
			nbt -> nbt.putBoolean("Splash", true));
		scene.idle(30);
		scene.world.removeItemsFromBelt(depotPos);
		scene.world.hideSection(util.select.position(depotPos.above(2)), Direction.SOUTH);
		scene.idle(20);
		ElementLink<WorldSectionElement> spout =
			scene.world.showIndependentSection(util.select.position(depotPos.above(2)
				.west()), Direction.SOUTH);
		scene.world.moveSection(spout, util.vector.of(1, 0, 0), 0);

		BlockPos pressPos = depotPos.above(2)
			.west();
		ItemStack copper = new ItemStack(Items.COPPER_INGOT);
		scene.world.createItemOnBeltLike(depotPos, Direction.NORTH, copper);
		Vec3 depotCenter = util.vector.centerOf(depotPos);
		scene.idle(10);

		Class<MechanicalPressBlockEntity> type = MechanicalPressBlockEntity.class;
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.start(Mode.BELT));
		scene.idle(15);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.makePressingParticleEffect(depotCenter.add(0, 8 / 16f, 0), copper));
		scene.world.removeItemsFromBelt(depotPos);
		ItemStack sheet = AllItems.COPPER_SHEET.asStack();
		scene.world.createItemOnBeltLike(depotPos, Direction.UP, sheet);

		scene.idle(20);
		scene.world.hideIndependentSection(spout, Direction.SOUTH);
		scene.idle(10);

		Selection fanSelect = util.select.fromTo(4, 1, 3, 5, 2, 2)
			.add(util.select.position(3, 1, 2))
			.add(util.select.position(5, 0, 2));
		scene.world.showSection(fanSelect, Direction.SOUTH);
		ElementLink<WorldSectionElement> water =
			scene.world.showIndependentSection(util.select.position(3, 1, 0), Direction.SOUTH);
		scene.world.moveSection(water, util.vector.of(0, 1, 2), 0);
		scene.idle(30);

		scene.world.hideSection(fanSelect, Direction.SOUTH);
		scene.world.hideIndependentSection(water, Direction.SOUTH);
		scene.idle(30);

		scene.world.showSection(util.select.fromTo(2, 1, 4, 2, 1, 5)
			.add(util.select.position(2, 0, 5)), Direction.DOWN);
		BlockPos armPos = util.grid.at(2, 1, 4);
		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("...as well as provide Items to Mechanical Arms")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(armPos, Direction.WEST));
		scene.idle(20);

		scene.world.instructArm(armPos, Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
		scene.idle(37);
		scene.world.removeItemsFromBelt(depotPos);
		scene.world.instructArm(armPos, Phase.SEARCH_OUTPUTS, sheet, -1);
	}

}
