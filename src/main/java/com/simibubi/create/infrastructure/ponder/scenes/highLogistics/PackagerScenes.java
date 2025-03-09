package com.simibubi.create.infrastructure.ponder.scenes.highLogistics;

import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PackagerScenes {

	public static void packager(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("packager", "Creating and unwrapping packages");
		scene.configureBasePlate(0, 0, 7);
		scene.showBasePlate();

		Selection chest1 = util.select()
			.fromTo(5, 2, 3, 5, 2, 4);
		BlockPos funnel1 = util.grid()
			.at(4, 2, 2);
		BlockPos funnel2 = util.grid()
			.at(1, 2, 2);
		Selection funnel1S = util.select()
			.position(funnel1);
		Selection funnel2S = util.select()
			.position(funnel2);
		BlockPos packager1 = util.grid()
			.at(5, 2, 2);
		BlockPos packager2 = util.grid()
			.at(1, 2, 3);
		Selection packager1S = util.select()
			.position(packager1);
		Selection packager2S = util.select()
			.position(packager2);
		Selection largeCog = util.select()
			.position(7, 0, 3);
		Selection cogNBelt = util.select()
			.fromTo(6, 1, 2, 0, 1, 2)
			.add(util.select()
				.position(6, 1, 3));
		BlockPos lever = util.grid()
			.at(5, 3, 2);
		Selection scaff1 = util.select()
			.fromTo(5, 1, 3, 5, 1, 4);
		Selection scaff2 = util.select()
			.fromTo(1, 1, 3, 1, 1, 4);
		scene.idle(5);

		ElementLink<WorldSectionElement> chestL = scene.world()
			.showIndependentSection(chest1, Direction.DOWN);
		scene.world()
			.moveSection(chestL, util.vector()
				.of(-2, -1, 0), 0);
		scene.idle(10);
		ElementLink<WorldSectionElement> packagerL = scene.world()
			.showIndependentSection(packager1S, Direction.SOUTH);
		scene.world()
			.moveSection(packagerL, util.vector()
				.of(-2, -1, 0), 0);
		scene.idle(20);

		ItemStack dirt = new ItemStack(Items.DIRT);
		scene.overlay()
			.showControls(util.vector()
				.of(2.5, 3, 2.5), Pointing.DOWN, 40)
			.withItem(dirt);
		scene.idle(20);

		scene.overlay()
			.showText(80)
			.text("Attach packagers to the inventory they should target")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.of(2, 2.5, 2.5));
		scene.idle(60);

		ElementLink<WorldSectionElement> leverL = scene.world()
			.showIndependentSection(util.select()
				.position(lever), Direction.DOWN);
		scene.world()
			.moveSection(leverL, util.vector()
				.of(-2, -1, 0), 0);
		scene.idle(30);

		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(lever, packager1));
		scene.effects()
			.indicateRedstone(lever.west(2)
				.below());

		scene.idle(10);
		ItemStack box = PackageStyles.getDefaultBox()
			.copy();
		PackageItem.addAddress(box, "Warehouse");
		PonderHilo.packagerCreate(scene, packager1, box);
		scene.idle(30);

		scene.overlay()
			.showText(80)
			.text("Given redstone power, it will pack items from the inventory into a package")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(3, 1, 2), Direction.UP));
		scene.idle(30);

		scene.idle(80);

		scene.world()
			.moveSection(leverL, util.vector()
				.of(2, 1, 0), 10);
		scene.world()
			.moveSection(packagerL, util.vector()
				.of(2, 1, 0), 10);
		scene.world()
			.moveSection(chestL, util.vector()
				.of(2, 1, 0), 10);
		scene.world()
			.showSection(scaff1, Direction.UP);
		scene.idle(10);
		scene.world()
			.showSection(largeCog, Direction.UP);
		scene.world()
			.showSection(cogNBelt, Direction.SOUTH);
		scene.idle(10);
		scene.world()
			.showSection(funnel1S, Direction.DOWN);
		scene.idle(15);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(4, 1, 2), Direction.EAST, box);
		PonderHilo.packagerClear(scene, packager1);
		scene.idle(20);
		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(5, 2, 2, 5, 3, 2));
		scene.idle(10);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 1 / 16f);

		scene.overlay()
			.showText(70)
			.text("These can be picked up and transported like any other item")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(3, 2, 2), Direction.EAST));

		scene.idle(80);
		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 16f);
		scene.idle(10);

		scene.world()
			.showSection(scaff2, Direction.DOWN);
		scene.idle(5);
		scene.world()
			.showSection(packager2S, Direction.DOWN);
		scene.world()
			.showSection(util.select()
				.position(1, 2, 4), Direction.DOWN);
		scene.idle(10);
		scene.world()
			.showSection(funnel2S, Direction.SOUTH);
		scene.rotateCameraY(-15);
		scene.idle(40);

		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(1, 1, 2));
		scene.world()
			.flapFunnel(util.grid()
				.at(1, 2, 2), false);
		PonderHilo.packagerUnpack(scene, packager2, box);

		scene.idle(20);
		scene.overlay()
			.showControls(util.vector()
				.topOf(util.grid()
					.at(1, 2, 4)),
				Pointing.DOWN, 40)
			.withItem(dirt);
		scene.idle(20);

		scene.overlay()
			.showText(90)
			.text("Packages inserted will be destroyed, unpacking the contents into the inventory")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(1, 2, 3), Direction.WEST));
		scene.idle(100);

		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(5, 2, 2, 5, 3, 2));
		scene.effects()
			.indicateRedstone(util.grid()
				.at(5, 3, 2));
		PonderHilo.packagerCreate(scene, packager1, box);
		scene.idle(25);

		scene.world()
			.createItemOnBelt(util.grid()
				.at(4, 1, 2), Direction.EAST, box);
		PonderHilo.packagerClear(scene, packager1);
		scene.idle(30);

		scene.overlay()
			.showText(60)
			.text("Full")
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.pointAt(util.vector()
				.topOf(util.grid()
					.at(1, 2, 4)));
		scene.idle(80);

		scene.overlay()
			.showOutlineWithText(util.select()
				.fromTo(1, 2, 3, 1, 2, 4), 90)
			.text("Packagers will not accept packages they cannot fully unpack")
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(1, 2, 3), Direction.WEST));
		scene.idle(40);

		PonderHilo.packageHopsOffBelt(scene, util.grid()
			.at(0, 1, 2), Direction.WEST, box);
		scene.idle(40);

	}

	public static void packagerAddress(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("packager_address", "Routing packages with an address");
		scene.configureBasePlate(0, 0, 9);
		scene.scaleSceneView(.875f);
		scene.showBasePlate();

		Selection frogport = util.select()
			.position(7, 1, 1);
		Selection postbox = util.select()
			.fromTo(6, 1, 2, 6, 2, 2);
		Selection northBelt = util.select()
			.fromTo(3, 1, 3, 4, 1, 0);
		Selection initialKinetics = util.select()
			.fromTo(3, 1, 5, 3, 1, 9);
		Selection largeCog = util.select()
			.position(2, 0, 9);
		Selection saw = util.select()
			.fromTo(2, 1, 5, 0, 1, 4);
		Selection eastBelt = util.select()
			.fromTo(3, 1, 4, 8, 1, 4);
		Selection tunnelS = util.select()
			.position(4, 2, 4);
		Selection chest = util.select()
			.fromTo(7, 2, 8, 7, 2, 7);
		Selection scaffold = util.select()
			.fromTo(7, 1, 8, 7, 1, 7);
		BlockPos packager = util.grid()
			.at(7, 2, 6);
		Selection packagerAndLever = util.select()
			.fromTo(7, 2, 6, 7, 3, 6);
		Selection packagerBelt = util.select()
			.fromTo(7, 1, 6, 4, 1, 6);
		BlockPos funnel = util.grid()
			.at(6, 2, 6);
		Selection signS = util.select()
			.position(7, 2, 5);

		scene.idle(10);
		ElementLink<WorldSectionElement> chestL = scene.world()
			.showIndependentSection(chest, Direction.DOWN);
		scene.world()
			.moveSection(chestL, util.vector()
				.of(-2, -1, -2), 0);
		scene.idle(5);
		scene.world()
			.showSectionAndMerge(packagerAndLever, Direction.SOUTH, chestL);
		scene.idle(20);

		scene.world()
			.showSectionAndMerge(signS, Direction.SOUTH, chestL);
		scene.idle(15);

		scene.overlay()
			.showText(40)
			.text("Warehouse")
			.colored(PonderPalette.OUTPUT)
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(5, 1, 4), Direction.NORTH)
				.add(-0.5, 0, 0));
		scene.idle(50);

		scene.overlay()
			.showText(60)
			.text("When a sign is placed on a packager..")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(5, 1, 4), Direction.NORTH)
				.add(-0.5, 0, 0));
		scene.idle(50);

		scene.world()
			.toggleRedstonePower(packagerAndLever);
		scene.effects()
			.indicateRedstone(util.grid()
				.at(5, 1, 4));
		ItemStack box = PackageStyles.getDefaultBox()
			.copy();
		PonderHilo.packagerCreate(scene, packager, box);

		scene.idle(20);
		scene.world()
			.moveSection(chestL, util.vector()
				.of(0, 1, 0), 10);
		scene.idle(10);
		scene.world()
			.showSectionAndMerge(scaffold, Direction.NORTH, chestL);
		scene.world()
			.showSection(largeCog, Direction.UP);
		scene.world()
			.showSection(initialKinetics, Direction.NORTH);
		scene.world()
			.showSectionAndMerge(packagerBelt, Direction.SOUTH, chestL);
		scene.idle(5);
		scene.world()
			.showSectionAndMerge(util.select()
				.position(funnel), Direction.DOWN, chestL);
		scene.idle(15);

		PonderHilo.packagerClear(scene, packager);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(6, 1, 6), Direction.EAST, box);
		scene.idle(20);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 1 / 32f);
		scene.overlay()
			.showText(40)
			.text("\u2192 Warehouse")
			.colored(PonderPalette.OUTPUT)
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(3, 2, 4), Direction.NORTH));
		scene.idle(50);

		scene.overlay()
			.showText(100)
			.text("Created packages will carry the written lines of text as their address")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(3, 2, 4), Direction.NORTH)
				.add(-0.5, 0, 0));
		scene.idle(120);

		scene.world()
			.hideIndependentSection(chestL, Direction.NORTH);
		scene.idle(15);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(5, 1, 6));
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(4, 1, 6));
		scene.idle(15);

		scene.world()
			.showSection(eastBelt, Direction.WEST);
		scene.idle(5);
		scene.world()
			.showSection(tunnelS, Direction.DOWN);
		scene.idle(5);
		scene.world()
			.showSection(saw, Direction.EAST);
		scene.idle(5);
		scene.world()
			.showSection(northBelt, Direction.SOUTH);
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
			.text("Package filters route packages based on their address")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.of(4, 2.825, 4.5));
		scene.idle(70);

		ItemStack warehouseBox = PackageStyles.getDefaultBox()
			.copy();
		ItemStack factoryBox = PackageItem.containing(List.of(new ItemStack(Items.IRON_INGOT)));
		PackageItem.addAddress(warehouseBox, "Warehouse");
		PackageItem.addAddress(factoryBox, "Factory");

		scene.world()
			.createItemOnBelt(util.grid()
				.at(6, 1, 4), Direction.EAST, warehouseBox);
		scene.idle(10);

		scene.overlay()
			.showText(50)
			.text("\u2192 Warehouse")
			.colored(PonderPalette.OUTPUT)
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(7, 2, 4), Direction.WEST));
		scene.overlay()
			.showText(50)
			.colored(PonderPalette.BLUE)
			.text("Factory")
			.placeNearTarget()
			.pointAt(util.vector()
				.of(4, 2.825, 4.5));
		scene.idle(60);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 32f);

		scene.idle(60);

		scene.world()
			.createItemOnBelt(util.grid()
				.at(6, 1, 4), Direction.EAST, factoryBox);
		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 1 / 32f);
		scene.idle(10);

		scene.overlay()
			.showText(50)
			.text("\u2192 Factory")
			.colored(PonderPalette.OUTPUT)
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(7, 2, 4), Direction.WEST));
		scene.overlay()
			.showText(50)
			.colored(PonderPalette.BLUE)
			.text("Factory")
			.placeNearTarget()
			.pointAt(util.vector()
				.of(4, 2.825, 4.5));
		scene.idle(60);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 32f);

		scene.idle(40);
		PonderHilo.packageHopsOffBelt(scene, util.grid()
			.at(4, 1, 0), Direction.NORTH, warehouseBox);
		scene.idle(40);
		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 1 / 32f);
		scene.overlay()
			.showText(100)
			.text("For compactness, mechanical saws can unpack straight onto a belt")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.topOf(util.grid()
					.at(2, 1, 4)));
		scene.idle(110);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 32f);

		scene.idle(20);
		scene.world()
			.hideSection(eastBelt, Direction.EAST);
		scene.idle(5);
		scene.world()
			.hideSection(tunnelS, Direction.UP);
		scene.idle(5);
		scene.world()
			.hideSection(saw, Direction.WEST);
		scene.idle(5);
		scene.world()
			.hideSection(initialKinetics, Direction.UP);
		scene.world()
			.hideSection(largeCog, Direction.DOWN);
		scene.world()
			.hideSection(northBelt, Direction.NORTH);
		scene.rotateCameraY(15);
		scene.idle(15);

		ElementLink<WorldSectionElement> extrasL = scene.world()
			.showIndependentSection(postbox, Direction.DOWN);
		scene.world()
			.moveSection(extrasL, util.vector()
				.of(-3, 0, 2), 0);
		scene.idle(5);
		scene.world()
			.showSectionAndMerge(frogport, Direction.DOWN, extrasL);
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
