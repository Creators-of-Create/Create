package com.simibubi.create.infrastructure.ponder.scenes.highLogistics;

import java.util.List;

import com.simibubi.create.AllBlocks;
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

public class RequesterAndShopScenes {

	public static void requester(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("redstone_requester", "Automated orders with Redstone Requesters");
		scene.configureBasePlate(0, 0, 7);
		scene.showBasePlate();

		Selection vault = util.select()
			.fromTo(4, 1, 3, 5, 2, 5);
		Selection packS = util.select()
			.fromTo(3, 1, 4, 3, 2, 4);
		BlockPos pack = util.grid()
			.at(3, 2, 4);
		BlockPos link = util.grid()
			.at(3, 3, 4);
		Selection linkS = util.select()
			.position(3, 3, 4);
		Selection funnel = util.select()
			.position(2, 2, 4);
		Selection belt = util.select()
			.fromTo(2, 1, 5, 2, 1, 2);
		Selection cogs = util.select()
			.fromTo(3, 1, 6, 3, 1, 5);
		Selection largeCog = util.select()
			.position(3, 0, 7);
		BlockPos req = util.grid()
			.at(3, 1, 1);
		Selection reqS = util.select()
			.position(3, 1, 1);
		Selection buttonAndRedstone = util.select()
			.fromTo(1, 1, 1, 2, 1, 1);
		Selection ticker = util.select()
			.fromTo(3, 1, 0, 4, 1, 0);

		scene.idle(10);

		ElementLink<WorldSectionElement> linkL = scene.world()
			.showIndependentSection(linkS, Direction.DOWN);
		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, -2, 0), 0);
		scene.idle(15);

		ItemStack linkItem = AllBlocks.REDSTONE_REQUESTER.asStack();
		scene.overlay()
			.showControls(util.vector()
				.topOf(link.below(2)), Pointing.DOWN, 50)
			.rightClick()
			.withItem(linkItem);
		scene.idle(5);

		AABB bb1 = new AABB(link.below(2));
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, link, bb1.deflate(0.45), 10);
		scene.idle(1);
		bb1 = bb1.deflate(1 / 16f)
			.contract(0, 8 / 16f, 0);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, link, bb1, 50);
		scene.idle(26);

		scene.overlay()
			.showText(100)
			.text("Right-click a Stock link before placement to connect to its network")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(link.below(2)));

		scene.idle(40);

		scene.world()
			.showSection(reqS, Direction.DOWN);
		scene.idle(20);

		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, ticker, new AABB(req), 40);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, link, bb1, 40);
		scene.overlay()
			.showLine(PonderPalette.GREEN, util.vector()
				.centerOf(req)
				.subtract(0, 1 / 4f, 0),
				util.vector()
					.centerOf(link.below(2))
					.subtract(0, 1 / 4f, 0),
				40);
		scene.idle(60);

		scene.world()
			.cycleBlockProperty(pack, PackagerBlock.LINKED);

		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, 2.25, 0), 10);
		scene.idle(8);
		scene.world()
			.showSection(packS, Direction.NORTH);
		scene.idle(3);
		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, -.25, 0), 10);
		scene.idle(10);
		scene.world()
			.cycleBlockProperty(pack, PackagerBlock.LINKED);
		scene.effects()
			.indicateSuccess(pack);
		scene.idle(5);
		scene.world()
			.showSection(vault, Direction.WEST);
		scene.idle(30);

		scene.overlay()
			.showText(110)
			.text("Just like Stock tickers, Redstone requesters can order items from the logistics network")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(req, Direction.WEST));
		scene.idle(120);

		scene.overlay()
			.showControls(util.vector()
				.topOf(req), Pointing.DOWN, 80)
			.rightClick();
		scene.idle(10);

		scene.overlay()
			.showOutlineWithText(reqS, 80)
			.text("Right-click the requester to open its configuration UI")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(req, Direction.WEST));

		scene.idle(90);
		scene.world()
			.showSection(buttonAndRedstone, Direction.EAST);
		scene.idle(5);
		scene.world()
			.showSection(belt, Direction.EAST);
		scene.world()
			.showSection(cogs, Direction.NORTH);
		scene.world()
			.showSection(largeCog, Direction.UP);
		scene.world()
			.showSection(funnel, Direction.DOWN);
		scene.idle(30);

		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(1, 1, 1, 3, 1, 1));
		scene.effects()
			.indicateRedstone(util.grid()
				.at(1, 1, 1));
		PonderHilo.requesterEffect(scene, req);
		scene.idle(5);
		ItemStack box = PackageItem.containing(List.of());
		PonderHilo.packagerCreate(scene, pack, box);
		PonderHilo.linkEffect(scene, link);
		scene.idle(20);

		PonderHilo.packagerClear(scene, pack);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(2, 1, 4), Direction.EAST, box);
		scene.idle(15);

		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(1, 1, 1, 3, 1, 1));

		scene.overlay()
			.showText(120)
			.text("The order set in the UI will be requested on every redstone pulse")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(3, 2, 4), Direction.WEST));
		scene.idle(60);

		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(1, 1, 1, 3, 1, 1));
		scene.effects()
			.indicateRedstone(util.grid()
				.at(1, 1, 1));
		PonderHilo.requesterEffect(scene, req);
		scene.idle(5);
		ItemStack box2 = box.copy();
		PonderHilo.packagerCreate(scene, pack, box2);
		PonderHilo.linkEffect(scene, link);
		scene.idle(20);

		PonderHilo.packagerClear(scene, pack);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(2, 1, 4), Direction.EAST, box2);
		scene.idle(15);

		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(1, 1, 1, 3, 1, 1));
		scene.idle(60);

		scene.world()
			.hideSection(reqS, Direction.UP);
		scene.world()
			.hideSection(buttonAndRedstone, Direction.WEST);
		scene.idle(15);
		ElementLink<WorldSectionElement> tickerL = scene.world()
			.showIndependentSection(ticker, Direction.DOWN);
		scene.world()
			.moveSection(tickerL, util.vector()
				.of(0, 0, 1), 0);
		scene.idle(5);
		scene.special()
			.createBirb(util.vector()
				.centerOf(util.grid()
					.at(4, 1, 1)),
				FacePointOfInterestPose::new);
		scene.idle(20);

		Vec3 keeper = util.vector()
			.blockSurface(util.grid()
				.at(4, 1, 1), Direction.WEST)
			.add(0, 0.5, 0);
		scene.overlay()
			.showControls(util.vector()
				.topOf(util.grid()
					.at(4, 1, 1)),
				Pointing.DOWN, 50)
			.rightClick()
			.withItem(linkItem);
		scene.idle(10);

		scene.overlay()
			.showText(80)
			.text("Alternatively, the requester can be fully configured before placement")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(keeper);
		scene.idle(90);

		scene.overlay()
			.showText(100)
			.text("Right-click a Stock keeper with it and set the desired order there")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(keeper);
		scene.idle(90);

	}

}
