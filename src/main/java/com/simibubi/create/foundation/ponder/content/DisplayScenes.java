package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.block.display.DisplayLinkTileEntity;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayTileEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DisplayScenes {

	public static void link(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("display_link", "Setting up Display Links");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		ItemStack linkItem = AllBlocks.DISPLAY_LINK.asStack();
		BlockPos invalidLinkPos = util.grid.at(2, 1, 2);
		Selection invalidLinkSel = util.select.position(invalidLinkPos);
		BlockPos linkPos = util.grid.at(2, 1, 1);
		Selection linkSel = util.select.position(linkPos);

		BlockPos signPos = util.grid.at(2, 1, 4);
		BlockPos signTarget = util.grid.at(2, 2, 4);

		scene.idle(20);

		scene.world.showSection(invalidLinkSel, Direction.DOWN);

		scene.idle(10);
		scene.effects.indicateRedstone(invalidLinkPos);
		scene.overlay.showSelectionWithText(invalidLinkSel, 60)
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.text("Display Links should be assigned a target before they are placed")
			.pointAt(util.vector.blockSurface(invalidLinkPos, Direction.WEST))
			.placeNearTarget();

		scene.idle(60);

		ElementLink<WorldSectionElement> signSection =
			scene.world.showIndependentSection(util.select.position(signPos), Direction.DOWN);
		scene.world.moveSection(signSection, util.vector.of(0, 1, 0), 0);

		ElementLink<WorldSectionElement> concreteSection =
			scene.world.showIndependentSection(util.select.position(signPos.below()), Direction.UP);
		scene.world.moveSection(concreteSection, util.vector.of(0, 1, 0), 0);

		scene.world.hideSection(invalidLinkSel, Direction.UP);

		scene.idle(20);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(signTarget, Direction.UP), Pointing.DOWN).rightClick()
				.withItem(linkItem),
			50);

		scene.idle(5);
		AABB signBounds = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D)
			.bounds();
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, new Object(), signBounds.move(signTarget), 60);

		scene.overlay.showText(70)
			.colored(PonderPalette.OUTPUT)
			.text("First select a target by Right- Clicking while holding the Link ...")
			.pointAt(util.vector.topOf(signTarget))
			.placeNearTarget();

		scene.idle(50);

		BlockPos observerPos = util.grid.at(3, 1, 1);

		scene.world.showSection(util.select.fromTo(observerPos, observerPos.relative(Direction.SOUTH)), Direction.DOWN);

		scene.idle(10);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(observerPos, Direction.WEST), Pointing.RIGHT).rightClick()
				.withItem(linkItem),
			50);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.colored(PonderPalette.INPUT)
			.text("... then select the source by placing the Link against it")
			.pointAt(util.vector.blockSurface(observerPos, Direction.UP))
			.placeNearTarget();

		scene.idle(55);

		scene.world.showSection(linkSel, Direction.EAST);

		scene.idle(20);

		scene.world.modifyTileEntity(linkPos, DisplayLinkTileEntity.class, linkTile -> linkTile.glow.setValue(2));
		scene.world.modifyTileEntity(signPos, SignBlockEntity.class,
			signTile -> signTile.setMessage(1, new TextComponent("42 Cinder Flour")));

		scene.idle(60);

		scene.world.hideIndependentSection(signSection, Direction.UP);
		scene.world.hideIndependentSection(concreteSection, Direction.DOWN);
		scene.idle(25);

		Selection boards = util.select.fromTo(4, 1, 4, 1, 2, 4)
			.substract(util.select.position(signPos));
		Selection cogs = util.select.position(5, 2, 4)
			.add(util.select.position(5, 2, 5))
			.add(util.select.position(5, 1, 5))
			.add(util.select.position(4, 0, 5));

		scene.world.showSection(boards, Direction.DOWN);
		scene.world.showSection(cogs, Direction.DOWN);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.colored(PonderPalette.WHITE)
			.text("Replacing Targets or Sources at a later time is possible as well")
			.placeNearTarget();

		scene.idle(7);

		scene.world.modifyTileEntity(linkPos, DisplayLinkTileEntity.class, linkTile -> linkTile.glow.setValue(2));

		scene.idle(43);

		scene.world.hideSection(util.select.fromTo(observerPos, observerPos.relative(Direction.SOUTH)),
			Direction.SOUTH);

		scene.idle(15);

		ElementLink<WorldSectionElement> seatSection =
			scene.world.showIndependentSection(util.select.position(3, 1, 0), Direction.SOUTH);
		scene.world.moveSection(seatSection, util.vector.of(0, 0, 1), 0);

		scene.world.modifyTileEntity(util.grid.at(4, 2, 4), FlapDisplayTileEntity.class, displayTile -> {
			displayTile.applyTextManually(0, "");
			displayTile.applyTextManually(1, "");
		});

		scene.idle(30);
		scene.special.createBirb(util.vector.of(3.5, 1.4, 1.5), ParrotElement.DancePose::new);

		scene.idle(10);

		// scene.world.modifyTileEntity(util.grid.at(4, 2, 4),
		// FlapDisplayTileEntity.class, displayTile -> {
		// displayTile.applyTextManually(0, "Sitting here:");
		// displayTile.applyTextManually(1, "Party Parrot");
		// });

		ElementLink<WorldSectionElement> replacementBoards =
			scene.world.showIndependentSectionImmediately(util.select.fromTo(4, 2, 3, 1, 2, 3));
		scene.world.moveSection(replacementBoards, util.vector.of(0, 0, 1), 0);

		scene.world.modifyTileEntity(linkPos, DisplayLinkTileEntity.class, linkTile -> linkTile.glow.setValue(2));

		scene.idle(10);

	}

	public static void board(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("display_board", "Using Display Boards");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		Selection largeCog = util.select.position(5, 0, 1);
		Selection cogs = util.select.fromTo(4, 1, 1, 4, 1, 3);
		BlockPos depotPos = util.grid.at(3, 1, 1);
		Selection depot = util.select.position(3, 1, 1);
		BlockPos linkPos = util.grid.at(2, 1, 1);
		Selection link = util.select.position(linkPos);
		BlockPos board = util.grid.at(3, 2, 3);
		Selection fullBoard = util.select.fromTo(3, 2, 3, 1, 1, 3);

		scene.world.setKineticSpeed(fullBoard, 0);
		scene.idle(15);

		for (int y = 1; y <= 2; y++) {
			for (int x = 3; x >= 1; x--) {
				scene.world.showSection(util.select.position(x, y, 3), Direction.DOWN);
				scene.idle(2);
			}
			scene.idle(2);
		}

		scene.idle(10);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("Display Boards are a scalable alternative to the sign")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 3), Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		scene.rotateCameraY(60);
		scene.idle(20);
		scene.world.showSection(cogs, Direction.DOWN);
		scene.world.showSection(largeCog, Direction.UP);
		scene.idle(10);
		scene.world.setKineticSpeed(fullBoard, 32);
		scene.world.multiplyKineticSpeed(util.select.position(3, 1, 3), -1);
		scene.world.multiplyKineticSpeed(util.select.position(2, 2, 3), -1);
		scene.world.multiplyKineticSpeed(util.select.position(1, 1, 3), -1);

		scene.overlay.showText(50)
			.text("They require Rotational Force to operate")
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 3), Direction.EAST))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(40);
		scene.rotateCameraY(-60);
		scene.idle(20);

		scene.world.showSection(util.select.position(0, 1, 2), Direction.DOWN);
		scene.idle(15);

		Vec3 target = util.vector.of(3.95, 2.75, 3.25);
		scene.overlay
			.showControls(new InputWindowElement(target, Pointing.RIGHT).withItem(new ItemStack(Items.NAME_TAG))
				.rightClick(), 40);
		scene.idle(6);
		scene.world.setDisplayBoardText(board, 0, new TextComponent("Create"));
		scene.idle(25);

		scene.overlay.showText(50)
			.text("Text can be displayed using Name Tags...")
			.pointAt(target.add(-2, 0, 0))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(40);
		scene.world.hideSection(util.select.position(0, 1, 2), Direction.WEST);
		scene.idle(20);

		scene.world.showSection(depot, Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(link, Direction.EAST);
		scene.idle(15);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, depot, new AABB(linkPos).contract(-.5f, 0, 0), 60);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, link, new AABB(board).expandTowards(-2, -1, 0)
			.deflate(0, 0, 3 / 16f), 60);
		scene.idle(20);

		scene.overlay.showText(50)
			.text("...or through the use of Display Links")
			.pointAt(target.add(-2, 0, 0))
			.attachKeyFrame()
			.colored(PonderPalette.OUTPUT)
			.placeNearTarget();
		scene.idle(50);

		ItemStack item = AllItems.PROPELLER.asStack();
		scene.world.createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.world.setDisplayBoardText(board, 1, item.getHoverName());
		scene.world.flashDisplayLink(linkPos);
		scene.idle(50);

		scene.world.removeItemsFromBelt(depotPos);
		item = AllItems.BLAZE_CAKE.asStack();
		scene.world.createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.world.setDisplayBoardText(board, 1, item.getHoverName());
		scene.world.flashDisplayLink(linkPos);
		scene.idle(50);

		scene.world.removeItemsFromBelt(depotPos);
		item = AllBlocks.DISPLAY_BOARD.asStack();
		scene.world.createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.world.setDisplayBoardText(board, 1, item.getHoverName());
		scene.world.flashDisplayLink(linkPos);
		scene.idle(50);

		scene.overlay
			.showControls(new InputWindowElement(target, Pointing.RIGHT).withItem(new ItemStack(Items.PINK_DYE))
				.rightClick(), 40);
		scene.idle(6);
		scene.world.dyeDisplayBoard(board, 0, DyeColor.PINK);
		scene.idle(25);

		scene.overlay.showText(70)
			.text("Dyes can be applied to individual lines of the board")
			.pointAt(target.add(-2, 0, 0))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(25);

		scene.overlay.showControls(
			new InputWindowElement(target.add(0, -.5f, 0), Pointing.RIGHT).withItem(new ItemStack(Items.LIME_DYE))
				.rightClick(),
			40);
		scene.idle(6);
		scene.world.dyeDisplayBoard(board, 1, DyeColor.LIME);
		scene.idle(55);

		scene.overlay.showControls(new InputWindowElement(target, Pointing.RIGHT).rightClick(), 40);
		scene.idle(6);
		scene.world.setDisplayBoardText(board, 0, new TextComponent(""));
		scene.idle(25);

		scene.overlay.showText(70)
			.text("Lines can be reset by clicking them with an empty hand")
			.pointAt(target.add(-2, 0, 0))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(40);

	}

}
