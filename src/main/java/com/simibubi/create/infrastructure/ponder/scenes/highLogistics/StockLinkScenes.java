package com.simibubi.create.infrastructure.ponder.scenes.highLogistics;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlockEntity;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public class StockLinkScenes {

	public static void stockLink(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("stock_link", "Logistics Networks and the Stock Link");
		scene.configureBasePlate(0, 0, 7);
		scene.world()
			.showSection(util.select()
				.layer(0), Direction.UP);

		Selection vault = util.select()
			.fromTo(4, 1, 4, 5, 2, 5);
		Selection chest = util.select()
			.fromTo(2, 1, 4, 2, 1, 5);
		BlockPos packager1 = util.grid()
			.at(4, 1, 3);
		BlockPos packager2 = util.grid()
			.at(2, 1, 3);
		BlockPos link1 = util.grid()
			.at(4, 2, 3);
		BlockPos link2 = util.grid()
			.at(2, 2, 3);
		BlockPos lever = util.grid()
			.at(1, 2, 2);
		BlockPos analog = util.grid()
			.at(1, 2, 3);
		Selection casing = util.select()
			.position(1, 1, 3);
		Selection requester = util.select()
			.fromTo(5, 1, 1, 4, 1, 1);
		BlockPos seat = util.grid()
			.at(5, 1, 1);
		BlockPos ticker = util.grid()
			.at(4, 1, 1);
		Selection packager1S = util.select()
			.position(packager1);
		Selection packager2S = util.select()
			.position(packager2);
		Selection link1S = util.select()
			.position(link1);
		Selection link2S = util.select()
			.position(link2);

		scene.idle(10);
		ElementLink<WorldSectionElement> linkL = scene.world()
			.showIndependentSection(link2S, Direction.DOWN);
		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, -1, 0), 0);
		scene.idle(25);

		scene.overlay()
			.showText(70)
			.attachKeyFrame()
			.text("When placed, Stock Links create a new stock network")
			.pointAt(util.vector()
				.centerOf(link2.below()))
			.placeNearTarget();
		scene.idle(80);

		ItemStack linkItem = AllBlocks.STOCK_LINK.asStack();
		scene.overlay()
			.showControls(util.vector()
				.topOf(link2.below()), Pointing.DOWN, 50)
			.rightClick()
			.withItem(linkItem);
		scene.idle(5);

		AABB bb1 = new AABB(link2.below());
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, link2, bb1.deflate(0.45), 10);
		scene.idle(1);
		bb1 = bb1.deflate(1 / 16f)
			.contract(0, 8 / 16f, 0);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, link2, bb1, 50);
		scene.idle(26);

		scene.overlay()
			.showText(80)
			.text("Right-click an existing link before placing it to bind them")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(link2.below()));

		scene.idle(40);

		scene.world()
			.showSectionAndMerge(link1S, Direction.DOWN, linkL);
		scene.idle(20);

		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, link1, bb1.move(util.vector()
				.of(2, 0, 0)), 40);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, link2, bb1, 40);
		scene.overlay()
			.showLine(PonderPalette.GREEN, util.vector()
				.centerOf(link1.below())
				.subtract(0, 1 / 4f, 0),
				util.vector()
					.centerOf(link2.below())
					.subtract(0, 1 / 4f, 0),
				40);
		scene.idle(60);

		scene.world()
			.cycleBlockProperty(packager1, PackagerBlock.LINKED);
		scene.world()
			.cycleBlockProperty(packager2, PackagerBlock.LINKED);

		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, 1.25, 0), 15);
		scene.idle(10);
		scene.world()
			.showSection(packager1S, Direction.NORTH);
		scene.idle(2);
		scene.world()
			.showSection(packager2S, Direction.NORTH);
		scene.idle(5);
		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, -.25, 0), 10);
		scene.idle(10);
		scene.world()
			.cycleBlockProperty(packager1, PackagerBlock.LINKED);
		scene.world()
			.cycleBlockProperty(packager2, PackagerBlock.LINKED);
		scene.effects()
			.indicateSuccess(packager1);
		scene.effects()
			.indicateSuccess(packager2);
		scene.idle(40);

		scene.overlay()
			.showText(100)
			.text("Stock-linked packagers make their inventory available to the network")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.of(2, 1.5, 3));
		scene.idle(40);

		scene.world()
			.showSection(chest, Direction.NORTH);
		scene.idle(5);
		scene.world()
			.showSection(vault, Direction.NORTH);
		scene.idle(20);

		scene.overlay()
			.showOutline(PonderPalette.BLUE, link1, chest, 120);
		scene.overlay()
			.showOutline(PonderPalette.BLUE, link2, vault, 120);
		scene.idle(30);

		scene.world()
			.showSection(requester, Direction.DOWN);
		scene.idle(3);
		scene.special()
			.createBirb(util.vector()
				.centerOf(seat), FacePointOfInterestPose::new);
		scene.idle(20);

		scene.overlay()
			.showOutline(PonderPalette.BLUE, seat, util.select()
				.position(4, 1, 1), 100);

		scene.overlay()
			.showText(100)
			.text("Other components on the network can now find and request their items")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(util.grid()
					.at(4, 1, 1)));
		scene.idle(110);

		scene.effects()
			.indicateSuccess(ticker);
		scene.idle(5);
		PonderHilo.linkEffect(scene, link1);
		PonderHilo.packagerCreate(scene, packager1, PackageItem.containing(List.of()));
		scene.idle(3);
		PonderHilo.linkEffect(scene, link2);
		PonderHilo.packagerCreate(scene, packager2, PackageItem.containing(List.of()));
		scene.idle(30);

		scene.overlay()
			.showText(100)
			.text("On request, items from the inventories will be placed into packages")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.of(2, 1.5, 3));
		scene.idle(120);

		scene.overlay()
			.showText(100)
			.text("Stock Link signals have unlimited range, but packages require transportation")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(util.grid()
					.at(2, 2, 3)));
		scene.idle(110);

		scene.world()
			.showSection(casing, Direction.EAST);
		scene.idle(10);
		ElementLink<WorldSectionElement> leverL = scene.world()
			.showIndependentSection(util.select()
				.position(lever), Direction.DOWN);
		scene.world()
			.moveSection(leverL, util.vector()
				.of(0, 0, 1), 0);
		scene.idle(20);

		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(1, 2, 2, 2, 2, 3));
		scene.effects()
			.indicateRedstone(link2.west());
		scene.idle(10);
		scene.overlay()
			.showControls(util.vector()
				.centerOf(link2), Pointing.DOWN, 40)
			.withItem(new ItemStack(Items.BARRIER));

		scene.idle(20);
		scene.overlay()
			.showText(80)
			.text("Full redstone power will stop a link from broadcasting")
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(util.grid()
					.at(2, 2, 3))
				.add(-0.25, 0, 0));
		scene.idle(70);

		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(1, 2, 2, 2, 2, 3));
		scene.idle(10);
		scene.world()
			.hideIndependentSection(leverL, Direction.SOUTH);

		scene.idle(10);
		Selection leverSelection = util.select()
			.position(analog);
		scene.world()
			.showSection(leverSelection, Direction.DOWN);

		scene.idle(20);
		scene.effects()
			.indicateRedstone(analog);
		scene.world()
			.toggleRedstonePower(util.select()
				.position(2, 2, 3));
		for (int i = 0; i < 10; i++) {
			final int state = i + 1;
			scene.world()
				.modifyBlockEntityNBT(leverSelection, AnalogLeverBlockEntity.class, nbt -> nbt.putInt("State", state));
			scene.idle(2);
		}
		scene.idle(20);

		scene.overlay()
			.showText(100)
			.text("Analog power lowers the priority of a link, causing others to act first")
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(util.grid()
					.at(2, 2, 3))
				.add(-0.25, 0, 0));
		scene.idle(80);
	}

}
