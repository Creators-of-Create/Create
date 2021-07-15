package com.simibubi.create.foundation.ponder.content;

import static com.simibubi.create.content.logistics.block.chute.ChuteBlock.SHAPE;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.block.chute.ChuteBlock;
import com.simibubi.create.content.logistics.block.chute.ChuteBlock.Shape;
import com.simibubi.create.content.logistics.block.chute.SmartChuteTileEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.EntityElement;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class ChuteScenes {

	public static void downward(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("chute", "Transporting Items downward via Chutes");
		scene.configureBasePlate(0, 0, 5);
		scene.scaleSceneView(.9f);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		ElementLink<WorldSectionElement> top =
			scene.world.showIndependentSection(util.select.fromTo(3, 3, 3, 3, 4, 3), Direction.DOWN);
		ElementLink<WorldSectionElement> bottom =
			scene.world.showIndependentSection(util.select.fromTo(3, 2, 3, 3, 1, 3), Direction.DOWN);
		scene.world.moveSection(bottom, util.vector.of(-2, 0, -1), 0);
		scene.world.moveSection(top, util.vector.of(0, 0, -1), 0);
		scene.idle(20);

		ItemStack stack = AllBlocks.COPPER_BLOCK.asStack();
		scene.world.createItemEntity(util.vector.centerOf(util.grid.at(3, 3, 2)), util.vector.of(0, -0.1, 0), stack);
		scene.idle(20);
		ElementLink<EntityElement> remove =
			scene.world.createItemEntity(util.vector.centerOf(util.grid.at(1, 5, 2)), util.vector.of(0, 0.1, 0), stack);
		scene.idle(15);
		scene.world.modifyEntity(remove, Entity::remove);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(util.grid.at(1, 2, 2)))
			.placeNearTarget()
			.text("Chutes can transport items vertically from and to inventories");
		scene.idle(70);
		scene.world.modifyEntities(ItemEntity.class, Entity::remove);
		scene.world.moveSection(bottom, util.vector.of(1, 0, 0), 10);
		scene.world.moveSection(top, util.vector.of(-1, 0, 0), 10);
		scene.idle(20);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.NORTH), Pointing.RIGHT)
				.rightClick()
				.withWrench(),
			40);
		scene.idle(7);
		scene.world.modifyBlock(util.grid.at(3, 3, 3), s -> s.setValue(ChuteBlock.SHAPE, ChuteBlock.Shape.WINDOW), false);
		scene.overlay.showText(50)
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.WEST))
			.placeNearTarget()
			.text("Using the Wrench, a window can be created");

		scene.idle(10);
		scene.world.modifyBlock(util.grid.at(3, 2, 3), s -> s.setValue(SHAPE, Shape.WINDOW), false);

		for (int i = 0; i < 8; i++) {
			scene.idle(10);
			scene.world.createItemOnBeltLike(util.grid.at(3, 3, 3), Direction.UP, stack);
		}
		scene.idle(20);
		scene.world.hideIndependentSection(bottom, Direction.EAST);
		scene.world.hideIndependentSection(top, Direction.EAST);
		scene.idle(15);

		scene.rotateCameraY(-90);
		scene.world.modifyBlock(util.grid.at(2, 2, 1), s -> s.setValue(SHAPE, Shape.NORMAL), false);
		scene.world.showSection(util.select.fromTo(2, 1, 1, 2, 2, 1), Direction.DOWN);
		scene.idle(30);
		ItemStack chuteItem = AllBlocks.CHUTE.asStack();
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 2, 1), Direction.SOUTH), Pointing.LEFT)
				.rightClick()
				.withItem(chuteItem),
			30);
		scene.idle(7);
		scene.world.showSection(util.select.position(2, 3, 2), Direction.NORTH);
		scene.world.restoreBlocks(util.select.position(2, 2, 1));
		scene.idle(35);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.SOUTH), Pointing.LEFT)
				.rightClick()
				.withItem(chuteItem),
			30);
		scene.idle(7);
		scene.world.showSection(util.select.position(2, 4, 3), Direction.NORTH);
		scene.idle(35);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 4, 3), Direction.WEST))
			.placeNearTarget()
			.text("Placing chutes targeting the side faces of another will make it diagonal");
		scene.idle(15);
		scene.rotateCameraY(90);

		scene.idle(35);

		Direction offset = Direction.NORTH;
		for (int i = 0; i < 3; i++) {
			remove = scene.world.createItemEntity(util.vector.centerOf(util.grid.at(2, 6, 3)
				.relative(offset)), util.vector.of(0, 0.1, 0)
					.add(Vector3d.atLowerCornerOf(offset.getNormal()).scale(-.1)),
				stack);
			scene.idle(12);
			scene.world.createItemOnBeltLike(util.grid.at(2, 4, 3), Direction.UP, stack);
			scene.world.modifyEntity(remove, Entity::remove);
			scene.idle(3);
			offset = offset.getClockWise();
		}

		scene.idle(10);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 1, 1), Direction.NORTH), Pointing.RIGHT)
				.withItem(stack),
			50);
	}

	public static void upward(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("chute_upward", "Transporting Items upward via Chutes");
		scene.configureBasePlate(0, 0, 5);
		scene.scaleSceneView(.9f);
		scene.showBasePlate();
		Selection chute = util.select.fromTo(1, 2, 2, 1, 4, 2);
		scene.world.setBlocks(chute, Blocks.AIR.defaultBlockState(), false);
		scene.world.showSection(util.select.position(1, 1, 2), Direction.UP);
		scene.idle(20);

		scene.world.restoreBlocks(chute);
		scene.world.showSection(chute, Direction.DOWN);
		scene.idle(20);
		scene.world.setKineticSpeed(util.select.position(1, 1, 2), 0);
		Vector3d surface = util.vector.blockSurface(util.grid.at(1, 2, 2), Direction.WEST);
		scene.overlay.showText(70)
			.text("Using Encased Fans at the top or bottom, a Chute can move items upward")
			.attachKeyFrame()
			.pointAt(surface)
			.placeNearTarget();
		scene.idle(80);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(1, 2, 2), Direction.NORTH), Pointing.RIGHT)
				.withItem(AllItems.GOGGLES.asStack()),
			50);
		scene.overlay.showText(70)
			.text("Inspecting chutes with Engineers' Goggles reveals information about the movement direction")
			.attachKeyFrame()
			.pointAt(surface)
			.placeNearTarget();
		scene.idle(80);

		scene.world.showSection(util.select.fromTo(2, 2, 2, 4, 1, 5)
			.add(util.select.position(3, 0, 5)), Direction.DOWN);
		ItemStack stack = AllBlocks.COPPER_BLOCK.asStack();
		scene.world.createItemOnBelt(util.grid.at(4, 1, 2), Direction.EAST, stack);
		scene.idle(10);
		scene.rotateCameraY(60);
		scene.overlay.showText(70)
			.text("On the 'blocked' end, items will have to be inserted/taken from the sides")
			.attachKeyFrame()
			.pointAt(util.vector.centerOf(util.grid.at(3, 1, 2))
				.add(0, 3 / 16f, 0))
			.placeNearTarget();
		scene.idle(32);
		scene.world.flapFunnel(util.grid.at(2, 2, 2), false);
		scene.world.removeItemsFromBelt(util.grid.at(2, 1, 2));
		scene.world.createItemOnBeltLike(util.grid.at(1, 2, 2), Direction.EAST, stack);
	}

	public static void smart(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("smart_chute", "Filtering Items using Smart Chutes");
		scene.configureBasePlate(0, 0, 5);
		scene.scaleSceneView(.9f);

		Selection lever = util.select.fromTo(0, 1, 2, 1, 3, 2);
		BlockPos smarty = util.grid.at(2, 3, 2);

		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(2, 1, 2, 2, 2, 2), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(2, 3, 2), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(2, 4, 2), Direction.DOWN);

		scene.overlay.showText(60)
			.text("Smart Chutes are vertical chutes with additional control")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(smarty, Direction.WEST))
			.placeNearTarget();
		scene.idle(70);

		Vector3d filter = util.vector.blockSurface(smarty, Direction.NORTH)
			.add(0, 0.25, 0);
		scene.overlay.showFilterSlotInput(filter, 60);
		ItemStack copper = new ItemStack(Items.IRON_INGOT);
		scene.overlay.showControls(new InputWindowElement(filter, Pointing.DOWN).rightClick()
			.withItem(copper), 40);
		scene.idle(7);
		scene.world.setFilterData(util.select.position(smarty), SmartChuteTileEntity.class, copper);
		scene.idle(10);
		scene.rotateCameraY(20);
		scene.overlay.showText(60)
			.text("Items in the filter slot specify what exactly they can extract and transfer")
			.attachKeyFrame()
			.pointAt(filter)
			.placeNearTarget();
		scene.idle(10);

		for (int i = 0; i < 18; i++) {
			scene.idle(10);
			scene.world.createItemOnBeltLike(util.grid.at(2, 2, 2), Direction.UP, copper);
			if (i == 8) {
				scene.rotateCameraY(-20);
				scene.overlay.showControls(new InputWindowElement(filter, Pointing.DOWN).scroll(), 40);
				scene.overlay.showText(50)
					.text("Use the Mouse Wheel to specify the extracted stack size")
					.attachKeyFrame()
					.pointAt(filter)
					.placeNearTarget();
			}
			if (i == 13)
				scene.world.showSection(lever, Direction.NORTH);
		}

		scene.world.toggleRedstonePower(lever.add(util.select.position(smarty)));
		scene.effects.indicateRedstone(util.grid.at(0, 3, 2));
		scene.overlay.showText(50)
			.text("Redstone power will prevent Smart Chutes from acting.")
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.pointAt(util.vector.blockSurface(util.grid.at(0, 2, 2), Direction.UP))
			.placeNearTarget();
		scene.idle(70);

		scene.world.toggleRedstonePower(lever.add(util.select.position(smarty)));
		scene.markAsFinished();
		for (int i = 0; i < 8; i++) {
			scene.idle(10);
			scene.world.createItemOnBeltLike(util.grid.at(2, 2, 2), Direction.UP, copper);
		}

	}

}
