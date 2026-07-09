package com.simibubi.create.infrastructure.ponder.scenes.highLogistics;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.api.math.Pointing;
import net.createmod.ponder.api.client.PonderPalette;
import net.createmod.ponder.api.client.element.ElementLink;
import net.createmod.ponder.api.client.element.ParrotPose.FacePointOfInterestPose;
import net.createmod.ponder.api.client.element.WorldSectionElement;
import net.createmod.ponder.api.client.scene.SceneBuilder;
import net.createmod.ponder.api.client.scene.SceneBuildingUtil;
import net.createmod.ponder.api.client.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class StockTickerScenes {

	public static void stockTicker(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("stock_ticker", "Ordering items with Stock tickers");
		scene.configureBasePlate(0, 0, 7);
		scene.scaleSceneView(0.925f);
		scene.setSceneOffsetY(-0.5f);
		scene.showBasePlate();

		BlockPos link1 = util.grid()
			.at(3, 3, 5);
		BlockPos link2 = util.grid()
			.at(5, 3, 5);
		BlockPos pack1 = util.grid()
			.at(3, 2, 5);
		BlockPos pack2 = util.grid()
			.at(5, 2, 5);
		BlockPos fun1 = util.grid()
			.at(3, 2, 4);
		BlockPos fun2 = util.grid()
			.at(5, 2, 4);
		Selection link1S = util.select()
			.position(link1);
		Selection link2S = util.select()
			.position(link2);
		Selection pack1S = util.select()
			.position(pack1);
		Selection pack2S = util.select()
			.position(pack2);
		Selection fun1S = util.select()
			.position(fun1);
		Selection fun2S = util.select()
			.position(fun2);
		Selection vault = util.select()
			.fromTo(4, 1, 6, 5, 3, 6);
		Selection chest = util.select()
			.fromTo(3, 1, 6, 3, 2, 6);
		Selection scaff2 = util.select()
			.position(5, 1, 5);
		Selection scaff1 = util.select()
			.position(3, 1, 5);
		Selection belt1 = util.select()
			.fromTo(6, 1, 4, 2, 1, 4);
		Selection cog1 = util.select()
			.position(6, 1, 3);
		Selection largeCog1 = util.select()
			.position(7, 0, 3);
		Selection cog2 = util.select()
			.position(2, 1, 6);
		Selection largeCog2 = util.select()
			.position(2, 0, 7);
		Selection belt2 = util.select()
			.fromTo(1, 1, 6, 1, 1, 1);
		Selection trapdoor = util.select()
			.position(1, 1, 0);
		Selection cannon = util.select()
			.position(2, 1, 3);
		BlockPos ticker = util.grid()
			.at(3, 1, 1);
		Selection tickerS = util.select()
			.position(3, 1, 1);
		Selection seat = util.select()
			.position(4, 1, 1);

		scene.idle(10);

		ElementLink<WorldSectionElement> linkL = scene.world()
			.showIndependentSection(link1S, Direction.DOWN);
		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, -2, 0), 0);
		scene.idle(15);

		ItemStack linkItem = AllBlocks.STOCK_TICKER.asStack();
		scene.overlay()
			.showControls(util.vector()
				.topOf(link1.below(2)), Pointing.DOWN, 50)
			.rightClick()
			.withItem(linkItem);
		scene.idle(5);

		AABB bb1 = new AABB(link1.below(2));
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, link1, bb1.deflate(0.45), 10);
		scene.idle(1);
		bb1 = bb1.deflate(1 / 16f)
			.contract(0, 8 / 16f, 0);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, link1, bb1, 50);
		scene.idle(26);

		scene.overlay()
			.showText(100)
			.text("Right-click a Stock link before placement to connect to its network")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(link1.below(2)));

		scene.idle(40);

		scene.world()
			.showSection(tickerS, Direction.DOWN);
		scene.idle(20);

		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, ticker, new AABB(ticker), 40);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, link1, bb1, 40);
		scene.overlay()
			.showLine(PonderPalette.GREEN, util.vector()
				.centerOf(ticker)
				.subtract(0, 1 / 4f, 0),
				util.vector()
					.centerOf(link1.below(2))
					.subtract(0, 1 / 4f, 0),
				40);
		scene.idle(60);

		scene.world()
			.cycleBlockProperty(pack1, PackagerBlock.LINKED);
		scene.world()
			.cycleBlockProperty(pack2, PackagerBlock.LINKED);

		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, 2.25, 0), 15);
		scene.idle(5);
		scene.world()
			.showSection(scaff1, Direction.NORTH);
		scene.world()
			.showSection(scaff2, Direction.NORTH);
		scene.idle(8);
		scene.world()
			.showSection(pack1S, Direction.NORTH);
		scene.idle(2);
		scene.world()
			.showSection(pack2S, Direction.NORTH);
		scene.world()
			.showSection(link2S, Direction.DOWN);
		scene.idle(2);
		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, -.25, 0), 10);
		scene.idle(10);
		scene.world()
			.cycleBlockProperty(pack1, PackagerBlock.LINKED);
		scene.world()
			.cycleBlockProperty(pack2, PackagerBlock.LINKED);
		scene.effects()
			.indicateSuccess(pack1);
		scene.effects()
			.indicateSuccess(pack2);
		scene.idle(15);
		scene.world()
			.showSection(chest, Direction.NORTH);
		scene.idle(2);
		scene.world()
			.showSection(vault, Direction.NORTH);
		scene.idle(10);

		scene.overlay()
			.showOutline(PonderPalette.BLUE, link1, util.select()
				.position(3, 2, 6), 120);
		scene.overlay()
			.showOutline(PonderPalette.BLUE, link2, util.select()
				.fromTo(4, 2, 6, 5, 3, 6), 120);
		scene.idle(30);

		scene.overlay()
			.showText(100)
			.text("Stock-linked packagers make their attached inventory available to the network")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(3, 2, 5), Direction.WEST));
		scene.idle(110);

		scene.overlay()
			.showOutline(PonderPalette.BLUE, ticker, tickerS, 40);

		scene.overlay()
			.showText(80)
			.text("Stock tickers can order items from these inventories")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(ticker));
		scene.idle(90);

		scene.world()
			.showSection(seat, Direction.WEST);
		scene.idle(10);
		scene.special()
			.createBirb(util.vector()
				.centerOf(ticker.east()), FacePointOfInterestPose::new);

		Vec3 keeper = util.vector()
			.blockSurface(ticker.east(), Direction.WEST)
			.add(0, 0.5, 0);
		scene.overlay()
			.showText(80)
			.text("Seated mobs or blaze burners in front of it act as the Stock Keeper")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(keeper);
		scene.idle(90);

		scene.overlay()
			.showControls(util.vector()
				.topOf(ticker.east()), Pointing.DOWN, 50)
			.rightClick();
		scene.idle(10);

		scene.overlay()
			.showText(80)
			.text("Right-click the keeper to start ordering items")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(keeper);
		scene.idle(60);

		scene.effects()
			.indicateSuccess(ticker);
		scene.idle(5);
		PonderHilo.linkEffect(scene, link1);
		ItemStack box1 = PackageItem.containing(List.of());
		ItemStack box2 = PackageItem.containing(List.of());
		PonderHilo.packagerCreate(scene, pack1, box1);
		scene.idle(3);
		PonderHilo.linkEffect(scene, link2);
		PonderHilo.packagerCreate(scene, pack2, box2);
		scene.idle(30);

		scene.overlay()
			.showText(100)
			.text("When an order is submitted, the items will be placed into packages")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(util.grid()
					.at(3, 2, 5)));
		scene.idle(80);

		scene.world()
			.showSection(largeCog1, Direction.UP);
		scene.world()
			.showSection(largeCog2, Direction.UP);
		scene.idle(5);
		scene.world()
			.showSection(cog1, Direction.DOWN);
		scene.world()
			.showSection(cog2, Direction.DOWN);
		scene.idle(5);
		scene.world()
			.showSection(belt1, Direction.DOWN);
		scene.world()
			.showSection(belt2, Direction.EAST);
		scene.idle(10);
		scene.world()
			.showSection(trapdoor, Direction.SOUTH);
		scene.idle(5);
		scene.world()
			.showSection(fun1S, Direction.DOWN);
		scene.idle(5);
		scene.world()
			.showSection(fun2S, Direction.DOWN);
		scene.idle(10);

		scene.world()
			.createItemOnBelt(util.grid()
				.at(3, 1, 4), Direction.SOUTH, box1);
		PonderHilo.packagerClear(scene, pack1);
		scene.idle(5);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(5, 1, 4), Direction.SOUTH, box2);
		PonderHilo.packagerClear(scene, pack2);
		scene.idle(20);

		scene.overlay()
			.showText(80)
			.text("From there, they can be transported to the request point")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(util.grid()
					.at(1, 1, 3)));
		scene.idle(110);

		scene.world()
			.hideSection(largeCog2, Direction.DOWN);
		scene.world()
			.hideSection(largeCog1, Direction.DOWN);
		scene.idle(2);
		scene.world()
			.hideSection(belt1, Direction.NORTH);
		scene.world()
			.hideSection(belt2, Direction.WEST);
		scene.world()
			.hideSection(cog2, Direction.WEST);
		scene.world()
			.hideSection(cog1, Direction.NORTH);
		scene.world()
			.hideSection(trapdoor, Direction.WEST);
		scene.world()
			.hideSection(fun1S, Direction.NORTH);
		scene.world()
			.hideSection(fun2S, Direction.NORTH);
		scene.idle(30);

		ItemStack filterItem = AllItems.ATTRIBUTE_FILTER.asStack();
		scene.overlay()
			.showControls(util.vector()
				.topOf(ticker), Pointing.DOWN, 100)
			.rightClick()
			.withItem(filterItem);
		scene.idle(10);

		scene.overlay()
			.showText(100)
			.text("Using attribute or list filters, categories can be added to the item listings")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(ticker));
		scene.idle(110);

		scene.overlay()
			.showText(80)
			.text("Right-click the Stock ticker to open the category editor")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(ticker));
		scene.idle(100);

		scene.world()
			.showSection(cannon, Direction.DOWN);

		scene.overlay()
			.showText(80)
			.text("Stock tickers can also order blocks required for the schematicannon")
			.colored(PonderPalette.BLUE)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(util.grid()
					.at(2, 1, 3)));
		scene.idle(100);

		ItemStack clipboardItem = AllBlocks.CLIPBOARD.asStack();
		scene.overlay()
			.showControls(keeper, Pointing.DOWN, 100)
			.rightClick()
			.withItem(clipboardItem);
		scene.idle(10);

		scene.overlay()
			.showText(80)
			.text("Simply hand the printed clipboard it generated to the Stock keeper")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(keeper);
		scene.idle(50);

	}

	public static void stockTickerAddress(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("stock_ticker_address", "Addressing a Stock ticker order");
		scene.configureBasePlate(0, 0, 9);
		scene.scaleSceneView(.875f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();

		Selection vault = util.select()
			.fromTo(6, 1, 6, 8, 3, 8);
		BlockPos pack = util.grid()
			.at(7, 2, 5);
		BlockPos link = util.grid()
			.at(7, 3, 5);
		Selection belt1 = util.select()
			.fromTo(8, 1, 4, 2, 1, 4)
			.add(util.select()
				.position(4, 2, 4));
		Selection belt2 = util.select()
			.fromTo(4, 1, 0, 3, 1, 3);
		Selection largeCog = util.select()
			.position(2, 0, 9);
		Selection cog = util.select()
			.fromTo(3, 1, 5, 3, 1, 9);
		Selection frog = util.select()
			.position(7, 1, 1);
		Selection postBox = util.select()
			.fromTo(6, 1, 2, 6, 2, 2);
		BlockPos seat = util.grid()
			.at(1, 1, 6);
		Selection tickerS = util.select()
			.fromTo(2, 1, 6, 2, 2, 6);
		BlockPos ticker = util.grid()
			.at(2, 2, 6);
		Selection funnel = util.select()
			.position(7, 2, 4);
		Selection linkAndPackager = util.select()
			.fromTo(7, 1, 5, 7, 3, 5);
		Selection seatS = util.select()
			.position(1, 1, 6);

		scene.idle(10);
		scene.world()
			.showSection(vault, Direction.DOWN);
		scene.world()
			.showSection(linkAndPackager, Direction.SOUTH);
		scene.idle(5);
		scene.world()
			.showSection(tickerS, Direction.DOWN);
		scene.world()
			.showSection(seatS, Direction.EAST);
		scene.idle(10);
		scene.special()
			.createBirb(util.vector()
				.centerOf(seat), FacePointOfInterestPose::new);

		scene.idle(20);

		scene.overlay()
			.showText(90)
			.text("When ordering items, a target address can be set in the request")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(ticker));
		scene.idle(100);

		scene.effects()
			.indicateSuccess(ticker);
		PonderHilo.linkEffect(scene, link);

		scene.overlay()
			.showText(40)
			.colored(PonderPalette.GREEN)
			.text("\u2192 Workshop")
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(ticker));

		scene.idle(10);
		ItemStack box1 = PackageItem.containing(List.of());
		PackageItem.addAddress(box1, "Workshop");
		PonderHilo.packagerCreate(scene, pack, box1);
		scene.idle(50);
		scene.overlay()
			.showText(90)
			.text("This address will be on all packages created for the request")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(pack, Direction.WEST));
		scene.idle(50);

		scene.world()
			.showSection(belt1, Direction.WEST);
		scene.world()
			.showSection(cog, Direction.WEST);
		scene.world()
			.showSection(largeCog, Direction.UP);
		scene.idle(5);
		scene.world()
			.showSection(belt2, Direction.SOUTH);
		scene.idle(45);
		scene.rotateCameraY(-15);
		scene.idle(15);

		scene.overlay()
			.showControls(util.vector()
				.of(4, 2.825, 4.5), Pointing.DOWN, 60)
			.withItem(AllItems.PACKAGE_FILTER.asStack());
		scene.idle(10);
		scene.overlay()
			.showFilterSlotInput(util.vector()
				.of(4.1, 2.825, 4.5), 50);
		scene.idle(30);

		scene.overlay()
			.showText(70)
			.text("Using package filters, this can control where the packages will go")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.of(4, 2.825, 4.5));
		scene.idle(60);
		scene.world()
			.showSection(funnel, Direction.DOWN);
		scene.idle(15);

		scene.world()
			.createItemOnBelt(util.grid()
				.at(7, 1, 4), Direction.SOUTH, box1);
		PonderHilo.packagerClear(scene, pack);
		scene.idle(15);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 1 / 32f);
		scene.idle(10);

		scene.overlay()
			.showText(50)
			.text("\u2192 Workshop")
			.colored(PonderPalette.OUTPUT)
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(6, 2, 4), Direction.WEST));
		scene.overlay()
			.showText(50)
			.colored(PonderPalette.BLUE)
			.text("Workshop")
			.placeNearTarget()
			.pointAt(util.vector()
				.of(4, 2.825, 4.5));
		scene.idle(60);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 32f);

		scene.idle(60);
		PonderHilo.packageHopsOffBelt(scene, util.grid()
			.at(2, 1, 4), Direction.WEST, box1);
		scene.idle(40);

		scene.effects()
			.indicateSuccess(ticker);
		PonderHilo.linkEffect(scene, link);

		scene.overlay()
			.showText(40)
			.colored(PonderPalette.GREEN)
			.text("\u2192 Factory")
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(ticker));

		scene.idle(10);
		ItemStack box2 = PackageItem.containing(List.of());
		PackageItem.addAddress(box2, "Factory");
		PonderHilo.packagerCreate(scene, pack, box2);
		scene.idle(20);

		scene.world()
			.createItemOnBelt(util.grid()
				.at(7, 1, 4), Direction.SOUTH, box2);
		PonderHilo.packagerClear(scene, pack);
		scene.idle(15);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 1 / 32f);
		scene.idle(10);

		scene.overlay()
			.showText(30)
			.text("\u2192 Factory")
			.colored(PonderPalette.OUTPUT)
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(6, 2, 4), Direction.WEST));
		scene.overlay()
			.showText(30)
			.colored(PonderPalette.BLUE)
			.text("Workshop")
			.placeNearTarget()
			.pointAt(util.vector()
				.of(4, 2.825, 4.5));
		scene.idle(40);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 32f);

		scene.idle(90);
		PonderHilo.packageHopsOffBelt(scene, util.grid()
			.at(4, 1, 0), Direction.NORTH, box2);
		scene.idle(5);
		scene.world()
			.hideSection(belt1, Direction.WEST);
		scene.world()
			.hideSection(cog, Direction.WEST);
		scene.world()
			.hideSection(largeCog, Direction.DOWN);
		scene.world()
			.hideSection(funnel, Direction.UP);
		scene.idle(5);
		scene.world()
			.hideSection(belt2, Direction.NORTH);
		scene.rotateCameraY(15);
		scene.idle(15);

		ElementLink<WorldSectionElement> extrasL = scene.world()
			.showIndependentSection(postBox, Direction.DOWN);
		scene.world()
			.moveSection(extrasL, util.vector()
				.of(-3, 0, 2), 0);
		scene.idle(5);
		scene.world()
			.showSectionAndMerge(frog, Direction.DOWN, extrasL);
		scene.idle(20);

		scene.overlay()
			.showText(100)
			.text("Aside from filters, Frogports and Postboxes have package routing abilities")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(3, 2, 4), Direction.NORTH));
		scene.idle(110);

		scene.overlay()
			.showText(80)
			.text("Inspect them to find out more about their behaviour")
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(3, 2, 4), Direction.NORTH));
		scene.idle(90);
	}

}
