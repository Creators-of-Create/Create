package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.Pointing;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.ElementLink;
import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DisplayScenes {

	public static void link(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("display_link", "Setting up Display Links");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		BlockPos depotPos = util.grid().at(3, 1, 1);
		Selection depot = util.select().position(3, 1, 1);
		BlockPos linkPos = util.grid().at(2, 1, 1);
		Selection link = util.select().position(linkPos);
		BlockPos board = util.grid().at(3, 2, 3);
		Selection fullBoard = util.select().fromTo(3, 2, 3, 1, 1, 3);
		Selection largeCog = util.select().position(3, 0, 5);
		Selection smallCog = util.select().fromTo(4, 1, 5, 4, 1, 3);

		Selection cuckoo = util.select().position(3, 2, 1);
		Selection stresso = util.select().position(3, 3, 1);
		Selection content = util.select().fromTo(4, 4, 1, 3, 4, 1);
		Selection dirt = util.select().position(3, 5, 1);

		Selection lectern = util.select().position(2, 2, 4);
		Selection sign = util.select().position(2, 1, 4);
		Selection nixies = util.select().fromTo(3, 3, 4, 1, 3, 4);

		scene.idle(15);

		scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);

		scene.idle(10);

		scene.overlay().showText(70)
			.attachKeyFrame()
			.text("Display Links can be used to visualise dynamic information")
			.pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.WEST))
			.placeNearTarget();
		scene.idle(60);

		scene.world().hideSection(util.select().position(2, 1, 2), Direction.UP);
		scene.idle(5);
		scene.world().showSection(largeCog, Direction.UP);
		scene.world().showSection(smallCog, Direction.WEST);
		scene.idle(5);
		scene.world().showSection(fullBoard, Direction.NORTH);
		scene.idle(25);

		Vec3 target = util.vector().of(3.5, 2.75, 3.25);
		scene.overlay()
			.showControls(new InputWindowElement(target, Pointing.RIGHT).withItem(AllBlocks.DISPLAY_LINK.asStack())
				.rightClick(), 60);
		scene.idle(6);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, link, new AABB(board).expandTowards(-2, -1, 0)
			.deflate(0, 0, 3 / 16f), 60);
		scene.idle(35);
		scene.overlay().showText(70)
			.text("First, right-click the target display...")
			.pointAt(target.add(-1, 0, 0))
			.colored(PonderPalette.OUTPUT)
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(60);

		scene.world().showSection(depot, Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(link, Direction.EAST);
		scene.idle(20);
		scene.overlay().showSelectionWithText(depot, 70)
			.text("...then attach it to the block to read from")
			.pointAt(util.vector().centerOf(linkPos))
			.colored(PonderPalette.INPUT)
			.placeNearTarget();
		scene.idle(60);

		ItemStack item = AllItems.PROPELLER.asStack();
		scene.world().createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.idle(20);

		scene.overlay().showControls(new InputWindowElement(util.vector().topOf(linkPos), Pointing.DOWN).rightClick(), 60);
		scene.idle(20);
		scene.overlay().showText(80)
			.text("Open the Interface to select and configure what is sent")
			.pointAt(util.vector().centerOf(linkPos))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(80);
		scene.effects().indicateSuccess(linkPos);
		scene.world().setDisplayBoardText(board, 1, item.getHoverName());
		scene.world().flashDisplayLink(linkPos);
		scene.idle(50);

		scene.world().removeItemsFromBelt(depotPos);
		item = AllItems.BLAZE_CAKE.asStack();
		scene.world().createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.world().setDisplayBoardText(board, 1, item.getHoverName());
		scene.world().flashDisplayLink(linkPos);
		scene.idle(20);

		scene.overlay().showText(80)
			.text("The display will now receive information from the link")
			.pointAt(target.add(-2.45f, -.5f, 0))
			.attachKeyFrame()
			.placeNearTarget();

		scene.idle(30);
		scene.world().removeItemsFromBelt(depotPos);
		item = AllBlocks.DISPLAY_BOARD.asStack();
		scene.world().createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.world().setDisplayBoardText(board, 1, item.getHoverName());
		scene.world().flashDisplayLink(linkPos);
		scene.idle(50);

		scene.world().hideSection(depot, Direction.SOUTH);
		scene.idle(5);
		scene.world().setDisplayBoardText(board, 1, Components.immutableEmpty());
		scene.world().flashDisplayLink(linkPos);
		scene.idle(5);
		ElementLink<WorldSectionElement> dirtElement = scene.world().showIndependentSection(dirt, Direction.SOUTH);
		scene.world().moveSection(dirtElement, util.vector().of(0, -4, 0), 0);

		scene.idle(25);
		scene.overlay().showSelectionWithText(depot, 50)
			.text("Not every block can act as a source")
			.pointAt(util.vector().topOf(depotPos))
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.placeNearTarget();
		scene.idle(60);
		scene.world().hideIndependentSection(dirtElement, Direction.SOUTH);
		scene.idle(10);

		ElementLink<WorldSectionElement> stressElement = scene.world().showIndependentSection(stresso, Direction.SOUTH);
		scene.world().moveSection(stressElement, util.vector().of(0, -2, 0), 0);
		scene.idle(10);
		scene.world().setDisplayBoardText(board, 1,
										  Components.literal(1024 + " ").append(CreateLang.translateDirect("generic.unit.stress")));
		scene.world().flashDisplayLink(linkPos);
		scene.idle(40);
		scene.world().hideIndependentSection(stressElement, Direction.SOUTH);
		scene.idle(10);

		ElementLink<WorldSectionElement> chestElement = scene.world().showIndependentSection(content, Direction.SOUTH);
		scene.world().moveSection(chestElement, util.vector().of(0, -3, 0), 0);
		scene.idle(10);
		scene.world().setDisplayBoardText(board, 1,
										  Components.literal(418 + " ").append(new ItemStack(Items.DEEPSLATE).getHoverName()));
		scene.world().setDisplayBoardText(board, 2, Components.literal(14 + " ").append(AllBlocks.COGWHEEL.asStack()
			.getHoverName()));
		scene.world().flashDisplayLink(linkPos);
		scene.idle(40);
		scene.world().hideIndependentSection(chestElement, Direction.SOUTH);
		scene.idle(10);

		scene.overlay().showText(90)
			.text("Each compatible block provides unique information")
			.pointAt(util.vector().topOf(depotPos))
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.placeNearTarget();

		ElementLink<WorldSectionElement> cuckooElement = scene.world().showIndependentSection(cuckoo, Direction.SOUTH);
		scene.world().moveSection(cuckooElement, util.vector().of(0, -1, 0), 0);
		scene.idle(10);
		scene.world().setDisplayBoardText(board, 1,
										  Components.literal("6:00 ").append(CreateLang.translateDirect("generic.daytime.pm")));
		scene.world().setDisplayBoardText(board, 2, Components.immutableEmpty());
		scene.world().flashDisplayLink(linkPos);
		scene.idle(90);

		scene.world().hideSection(fullBoard, Direction.SOUTH);
		scene.world().hideSection(largeCog, Direction.DOWN);
		scene.world().hideSection(smallCog, Direction.EAST);
		scene.idle(10);

		ElementLink<WorldSectionElement> signElement = scene.world().showIndependentSection(sign, Direction.SOUTH);
		scene.world().moveSection(signElement, util.vector().of(0, 0, -1), 0);
		scene.idle(10);
		scene.world().flashDisplayLink(linkPos);
		scene.idle(5);
		scene.world().hideIndependentSection(signElement, Direction.SOUTH);
		scene.idle(10);

		ElementLink<WorldSectionElement> lecternElement = scene.world().showIndependentSection(lectern, Direction.SOUTH);
		scene.world().moveSection(lecternElement, util.vector().of(0, -1, -1), 0);
		scene.idle(10);
		scene.world().flashDisplayLink(linkPos);
		scene.idle(5);
		scene.world().hideIndependentSection(lecternElement, Direction.SOUTH);
		scene.idle(10);

		scene.overlay().showText(90)
			.text("The Display Link can work with several different displays")
			.pointAt(util.vector().blockSurface(util.grid().at(1, 1, 3), Direction.WEST))
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.placeNearTarget();

		ElementLink<WorldSectionElement> nixieElement = scene.world().showIndependentSection(nixies, Direction.SOUTH);
		scene.world().moveSection(nixieElement, util.vector().of(0, -2, -1), 0);
		scene.idle(10);
		scene.world().flashDisplayLink(linkPos);
		scene.idle(60);
	}

	public static void board(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("display_board", "Using Display Boards");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		Selection largeCog = util.select().position(5, 0, 1);
		Selection cogs = util.select().fromTo(4, 1, 1, 4, 1, 3);
		BlockPos depotPos = util.grid().at(3, 1, 1);
		Selection depot = util.select().position(3, 1, 1);
		BlockPos linkPos = util.grid().at(2, 1, 1);
		Selection link = util.select().position(linkPos);
		BlockPos board = util.grid().at(3, 2, 3);
		Selection fullBoard = util.select().fromTo(3, 2, 3, 1, 1, 3);

		scene.world().setKineticSpeed(fullBoard, 0);
		scene.idle(15);

		for (int y = 1; y <= 2; y++) {
			for (int x = 3; x >= 1; x--) {
				scene.world().showSection(util.select().position(x, y, 3), Direction.DOWN);
				scene.idle(2);
			}
			scene.idle(2);
		}

		scene.idle(10);

		scene.overlay().showText(70)
			.attachKeyFrame()
			.text("Display Boards are a scalable alternative to the sign")
			.pointAt(util.vector().blockSurface(util.grid().at(1, 2, 3), Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		scene.rotateCameraY(60);
		scene.idle(20);
		scene.world().showSection(cogs, Direction.DOWN);
		scene.world().showSection(largeCog, Direction.UP);
		scene.idle(10);
		scene.world().setKineticSpeed(fullBoard, 32);
		scene.world().multiplyKineticSpeed(util.select().position(3, 1, 3), -1);
		scene.world().multiplyKineticSpeed(util.select().position(2, 2, 3), -1);
		scene.world().multiplyKineticSpeed(util.select().position(1, 1, 3), -1);

		scene.overlay().showText(50)
			.text("They require Rotational Force to operate")
			.pointAt(util.vector().blockSurface(util.grid().at(3, 1, 3), Direction.EAST))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(40);
		scene.rotateCameraY(-60);
		scene.idle(20);

		Vec3 target = util.vector().of(3.95, 2.75, 3.25);
		ItemStack clipboard = AllBlocks.CLIPBOARD.asStack();
		ClipboardOverrides.switchTo(ClipboardType.WRITTEN, clipboard);
		scene.overlay().showControls(new InputWindowElement(target, Pointing.RIGHT).withItem(clipboard)
			.rightClick(), 40);
		scene.idle(6);
		scene.world().setDisplayBoardText(board, 0, Components.literal("Create"));
		scene.idle(25);

		scene.overlay().showText(50)
			.text("Static text can be applied using written Clipboards")
			.pointAt(target.add(-2, 0, 0))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(80);

		scene.world().showSection(depot, Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(link, Direction.EAST);
		scene.idle(15);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, depot, new AABB(linkPos).contract(-.5f, 0, 0), 60);
		scene.idle(5);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, link, new AABB(board).expandTowards(-2, -1, 0)
			.deflate(0, 0, 3 / 16f), 60);
		scene.idle(20);

		scene.overlay().showText(70)
			.text("And dynamic text through the use of Display Links")
			.pointAt(target.add(-2, 0, 0))
			.attachKeyFrame()
			.colored(PonderPalette.OUTPUT)
			.placeNearTarget();
		scene.idle(50);

		ItemStack item = AllItems.PROPELLER.asStack();
		scene.world().createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.world().setDisplayBoardText(board, 1, item.getHoverName());
		scene.world().flashDisplayLink(linkPos);
		scene.idle(50);

		scene.world().removeItemsFromBelt(depotPos);
		item = AllItems.BLAZE_CAKE.asStack();
		scene.world().createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.world().setDisplayBoardText(board, 1, item.getHoverName());
		scene.world().flashDisplayLink(linkPos);
		scene.idle(50);

		scene.world().removeItemsFromBelt(depotPos);
		item = AllBlocks.DISPLAY_BOARD.asStack();
		scene.world().createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.world().setDisplayBoardText(board, 1, item.getHoverName());
		scene.world().flashDisplayLink(linkPos);
		scene.idle(50);

		scene.overlay()
			.showControls(new InputWindowElement(target, Pointing.RIGHT).withItem(new ItemStack(Items.PINK_DYE))
				.rightClick(), 40);
		scene.idle(6);
		scene.world().dyeDisplayBoard(board, 0, DyeColor.PINK);
		scene.idle(25);

		scene.overlay().showText(70)
			.text("Dyes can be applied to individual lines of the board")
			.pointAt(target.add(-2, 0, 0))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(25);

		scene.overlay().showControls(
			new InputWindowElement(target.add(0, -.5f, 0), Pointing.RIGHT).withItem(new ItemStack(Items.LIME_DYE))
				.rightClick(),
			40);
		scene.idle(6);
		scene.world().dyeDisplayBoard(board, 1, DyeColor.LIME);
		scene.idle(55);

		scene.overlay().showControls(new InputWindowElement(target, Pointing.RIGHT).rightClick(), 40);
		scene.idle(6);
		scene.world().setDisplayBoardText(board, 0, Components.immutableEmpty());
		scene.idle(25);

		scene.overlay().showText(70)
			.text("Lines can be reset by clicking them with an empty hand")
			.pointAt(target.add(-2, 0, 0))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(40);

	}

	public static void redstone(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("display_link_redstone", "Redstone Control");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		BlockPos depotPos = util.grid().at(3, 1, 1);
		Selection depot = util.select().position(3, 1, 1);
		BlockPos linkPos = util.grid().at(2, 1, 1);
		Selection link = util.select().position(linkPos);
		BlockPos board = util.grid().at(3, 2, 3);
		Selection fullBoard = util.select().fromTo(3, 2, 3, 1, 1, 3);
		Selection largeCog = util.select().position(3, 0, 5);
		Selection smallCog = util.select().fromTo(4, 1, 5, 4, 1, 3);
		Selection redstone = util.select().fromTo(1, 1, 1, 0, 1, 1);
		BlockPos leverPos = util.grid().at(0, 1, 1);

		scene.world().showSection(largeCog, Direction.UP);
		scene.world().showSection(smallCog, Direction.WEST);
		scene.idle(5);
		scene.world().showSection(fullBoard, Direction.NORTH);
		scene.idle(25);

		Vec3 target = util.vector().of(3.5, 2.75, 3.25);
		scene.world().showSection(depot, Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(link, Direction.EAST);
		scene.idle(10);
		scene.world().flashDisplayLink(linkPos);
		scene.world().setDisplayBoardText(board, 1, new ItemStack(Items.CALCITE).getHoverName());
		scene.idle(10);
		scene.world().showSection(redstone, Direction.EAST);
		scene.idle(20);
		scene.world().toggleRedstonePower(redstone);
		scene.world().toggleRedstonePower(link);
		scene.effects().indicateRedstone(leverPos);

		scene.idle(20);
		scene.overlay().showText(80)
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.pointAt(util.vector().topOf(linkPos))
			.placeNearTarget()
			.text("When powered by Redstone, Display Links stop sending updates");

		scene.idle(50);
		scene.world().removeItemsFromBelt(depotPos);
		ItemStack item = new ItemStack(Items.AMETHYST_BLOCK);
		scene.world().createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.idle(20);

		scene.idle(30);
		scene.world().removeItemsFromBelt(depotPos);
		item = new ItemStack(Items.GOLD_BLOCK);
		scene.world().createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.idle(50);

		scene.world().toggleRedstonePower(redstone);
		scene.world().toggleRedstonePower(link);
		scene.effects().indicateRedstone(leverPos);
		scene.idle(1);
		scene.world().flashDisplayLink(linkPos);
		scene.world().setDisplayBoardText(board, 1, item.getHoverName());

		scene.idle(15);
		scene.overlay().showText(100)
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.pointAt(target.add(-2.45, -0.5, 0))
			.placeNearTarget()
			.text("Once unpowered, the Timer is reset and new info is sent immediately");
		scene.idle(100);

		scene.world().hideSection(depot, Direction.SOUTH);
		scene.world().setBlock(util.grid().at(4, 1, 0), Blocks.REDSTONE_BLOCK.defaultBlockState(), false);
		scene.idle(10);
		ElementLink<WorldSectionElement> redstoneBlock =
			scene.world().showIndependentSection(util.select().position(4, 1, 0), Direction.SOUTH);
		scene.world().moveSection(redstoneBlock, util.vector().of(-1, 0, 1), 0);
		scene.idle(10);
		scene.world().flashDisplayLink(linkPos);
		scene.world().setDisplayBoardText(board, 1, Components.immutableEmpty());
		scene.idle(25);

		scene.overlay().showSelectionWithText(depot, 80)
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.pointAt(util.vector().topOf(linkPos))
			.placeNearTarget()
			.text("Signals emitted from the source do not affect the Link");
		scene.idle(70);

	}

}
