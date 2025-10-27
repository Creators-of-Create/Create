package com.simibubi.create.infrastructure.ponder.scenes.highLogistics;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.math.Pointing;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FactoryGaugeScenes {

	public static void restocker(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("factory_gauge_restocker", "Restocking with Factory Gauges");
		scene.configureBasePlate(0, 0, 7);
		scene.scaleSceneView(0.925f);
		scene.setSceneOffsetY(-0.5f);
		scene.showBasePlate();

		Selection vault = util.select()
			.fromTo(5, 1, 4, 6, 3, 6);
		Selection packager = util.select()
			.fromTo(4, 1, 5, 4, 2, 5);
		BlockPos pack = util.grid()
			.at(4, 2, 5);
		BlockPos link = util.grid()
			.at(4, 3, 5);
		Selection linkS = util.select()
			.position(4, 3, 5);
		Selection funnel = util.select()
			.position(3, 2, 5);
		Selection funnel2 = util.select()
			.position(1, 2, 2);
		Selection belt1 = util.select()
			.fromTo(1, 1, 2, 1, 1, 6)
			.add(util.select()
				.fromTo(2, 1, 6, 2, 1, 7));
		Selection largeCog = util.select()
			.position(1, 0, 7);
		Selection belt2 = util.select()
			.fromTo(3, 1, 5, 2, 1, 5);
		Selection chest = util.select()
			.fromTo(3, 2, 1, 2, 2, 1);
		Selection chestScaff = util.select()
			.fromTo(3, 1, 1, 2, 1, 1);
		Selection packScaff = util.select()
			.position(1, 1, 1);
		BlockPos pack2 = util.grid()
			.at(1, 2, 1);
		Selection pack2S = util.select()
			.position(1, 2, 1);
		BlockPos gauge = util.grid()
			.at(1, 2, 0);
		Selection gaugeS = util.select()
			.position(1, 2, 0);

		scene.idle(10);

		ElementLink<WorldSectionElement> linkL = scene.world()
			.showIndependentSection(linkS, Direction.DOWN);
		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, -2, 0), 0);

		ElementLink<WorldSectionElement> chestL = scene.world()
			.showIndependentSection(chest, Direction.DOWN);
		scene.world()
			.moveSection(chestL, util.vector()
				.of(0, -1, 0), 0);
		scene.idle(5);
		scene.world()
			.showSectionAndMerge(pack2S, Direction.EAST, chestL);

		scene.idle(15);

		ItemStack linkItem = AllBlocks.FACTORY_GAUGE.asStack();
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

		scene.idle(60);
		scene.world()
			.showSectionAndMerge(gaugeS, Direction.SOUTH, chestL);
		scene.idle(50);

		Vec3 gaugeMiddle = util.vector()
			.of(1.25, 1.75, 1);
		scene.overlay()
			.showText(100)
			.text("When placed on a packager, factory gauges can monitor items inside the inventory")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(gaugeMiddle);
		scene.idle(30);

		scene.overlay()
			.showOutline(PonderPalette.BLUE, gauge, util.select()
				.fromTo(3, 1, 1, 2, 1, 1), 70);
		scene.idle(80);

		ItemStack monitorItem = new ItemStack(Items.CHARCOAL);
		scene.overlay()
			.showControls(gaugeMiddle, Pointing.DOWN, 50)
			.withItem(monitorItem)
			.rightClick();
		scene.idle(7);
		setPanelItem(builder, gauge, PanelSlot.TOP_RIGHT, monitorItem);

		scene.world()
			.modifyBlockEntity(gauge, FactoryPanelBlockEntity.class, be -> {
				FactoryPanelBehaviour pb = be.panels.get(PanelSlot.BOTTOM_LEFT);
				pb.setFilter(monitorItem);
			});

		scene.overlay()
			.showText(80)
			.text("Right-click it with the item that should be monitored")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(gaugeMiddle);
		scene.idle(90);

		scene.overlay()
			.showOutline(PonderPalette.BLUE, gauge, util.select()
				.fromTo(3, 1, 1, 2, 1, 1), 70);
		scene.overlay()
			.showText(70)
			.text("It will now display the amount present in the inventory")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(gaugeMiddle);

		scene.idle(80);

		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, 2.25, 0), 15);
		scene.idle(5);
		scene.idle(8);
		scene.world()
			.showSection(vault, Direction.NORTH);
		scene.idle(5);
		scene.world()
			.showSection(packager, Direction.EAST);
		scene.idle(2);
		scene.world()
			.moveSection(linkL, util.vector()
				.of(0, -.25, 0), 10);
		scene.idle(10);

		scene.overlay()
			.showText(90)
			.text("The gauge can refill this inventory from the logistics network")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.of(3, 2, 1.5));
		scene.idle(100);

		scene.overlay()
			.showControls(gaugeMiddle, Pointing.DOWN, 50)
			.rightClick();
		scene.idle(7);
		AABB boundingBox = new AABB(gaugeMiddle, gaugeMiddle).inflate(0.19, 0.19, 0);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, gauge, boundingBox, 150);
		scene.overlay()
			.showText(70)
			.text("Right-click it again to open its configuration UI")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(gaugeMiddle);
		scene.idle(80);

		scene.overlay()
			.showText(70)
			.text("Set an address that should be used for the requested items")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(gaugeMiddle);
		scene.idle(80);

		scene.world()
			.moveSection(chestL, util.vector()
				.of(0, 1, 0), 10);
		scene.idle(10);
		scene.world()
			.showSection(chestScaff, Direction.NORTH);
		scene.world()
			.showSection(packScaff, Direction.NORTH);
		scene.idle(10);
		scene.world()
			.showSection(largeCog, Direction.UP);
		scene.world()
			.showSection(belt1, Direction.EAST);
		scene.idle(5);
		scene.world()
			.showSection(belt2, Direction.SOUTH);
		scene.idle(15);

		gaugeMiddle = gaugeMiddle.add(0, 1, 0);

		scene.overlay()
			.showControls(gaugeMiddle, Pointing.DOWN, 100)
			.rightClick();
		scene.idle(7);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, gauge, boundingBox.move(0, 1, 0), 100);
		scene.overlay()
			.showText(100)
			.text("The target amount to maintain can now be set by holding Right-click on the gauge")
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(gaugeMiddle);
		scene.idle(40);

		setPanelNotSatisfied(builder, gauge, PanelSlot.TOP_RIGHT);

		scene.idle(70);
		scene.overlay()
			.showText(70)
			.text("Whenever the chest has fewer items than this amount...")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.of(3, 3, 1.5));
		scene.idle(50);

		PonderHilo.linkEffect(scene, link);
		ItemStack box = PackageItem.containing(List.of());
		PonderHilo.packagerCreate(scene, pack, box);
		scene.idle(30);

		scene.overlay()
			.showText(70)
			.text("...the logistics network sends more, with the specified address")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.of(4, 2.5, 5.5));
		scene.idle(50);

		scene.world()
			.showSection(funnel, Direction.DOWN);
		scene.idle(10);
		scene.world()
			.showSection(funnel2, Direction.DOWN);
		scene.idle(5);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(3, 1, 5), Direction.EAST, box);
		PonderHilo.packagerClear(scene, pack);
		scene.idle(40);

		scene.overlay()
			.showText(70)
			.text("From there, they can be routed to the packager")
			.placeNearTarget()
			.pointAt(util.vector()
				.of(1, 2.5, 3.5));

		scene.idle(30);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(1, 1, 2));
		PonderHilo.packagerUnpack(scene, pack2, box);
		scene.idle(15);

		setPanelSatisfied(builder, gauge, PanelSlot.TOP_RIGHT);
	}

	public static void recipe(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("factory_gauge_recipe", "Automated Recipes with Factory Gauges");
		scene.configureBasePlate(0, 0, 9);
		scene.scaleSceneView(0.925f);
		scene.setSceneOffsetY(-0.5f);

		Selection fakeFloor = util.select()
			.fromTo(8, 6, 0, 0, 6, 8);
		Selection belt1 = util.select()
			.fromTo(1, 0, 7, 1, 0, 2);
		Selection belt2 = util.select()
			.fromTo(1, 0, 1, 3, 0, 1);
		Selection saw = util.select()
			.position(4, 0, 1);
		Selection belt3 = util.select()
			.fromTo(5, 0, 1, 6, 0, 1);
		Selection belt4 = util.select()
			.fromTo(7, 0, 7, 7, 0, 5);
		Selection funnel1 = util.select()
			.position(1, 1, 7);
		Selection funnel2 = util.select()
			.position(6, 1, 1);
		Selection funnel3 = util.select()
			.position(7, 1, 5);
		Selection pack2S = util.select()
			.fromTo(7, 1, 7, 6, 1, 7);
		Selection basin = util.select()
			.position(7, 1, 1);
		Selection mixer = util.select()
			.fromTo(7, 3, 1, 9, 3, 1)
			.add(util.select()
				.fromTo(9, 2, 1, 9, 0, 1));
		Selection basinOut = util.select()
			.fromTo(7, 0, 2, 7, 1, 2);
		Selection barrelAndPackager = util.select()
			.fromTo(7, 2, 4, 7, 1, 3);
		BlockPos pack = util.grid()
			.at(2, 1, 7);
		Selection packS = util.select()
			.fromTo(2, 1, 7, 2, 2, 7);
		BlockPos link = util.grid()
			.at(2, 2, 7);
		Selection vault = util.select()
			.fromTo(5, 2, 7, 3, 1, 6);
		Selection scaff1 = util.select()
			.fromTo(5, 1, 4, 3, 1, 4);
		Selection scaff2 = util.select()
			.position(2, 1, 4);
		Selection board1 = util.select()
			.fromTo(5, 2, 4, 3, 3, 4);
		Selection board2 = util.select()
			.fromTo(6, 5, 4, 2, 2, 4)
			.substract(board1);

		BlockPos alloyG = util.grid()
			.at(3, 3, 3);
		BlockPos andeG = util.grid()
			.at(4, 3, 3);
		BlockPos nuggG = util.grid()
			.at(4, 2, 3);
		BlockPos ironG = util.grid()
			.at(5, 2, 3);
		BlockPos rawIronG = util.grid()
			.at(6, 3, 3);
		BlockPos dioriteG = util.grid()
			.at(5, 3, 3);
		BlockPos planksG = util.grid()
			.at(3, 4, 3);
		BlockPos logsG = util.grid()
			.at(4, 4, 3);
		BlockPos cogG = util.grid()
			.at(2, 2, 3);
		BlockPos quartzG = util.grid()
			.at(6, 4, 3);
		BlockPos cobbleG = util.grid()
			.at(5, 4, 3);

		ElementLink<WorldSectionElement> floorL = scene.world()
			.showIndependentSection(fakeFloor, Direction.UP);
		scene.world()
			.moveSection(floorL, util.vector()
				.of(0, -6, 0), 0);
		scene.idle(10);

		setPanelVisible(builder, alloyG, PanelSlot.TOP_RIGHT, false);
		setPanelPassive(builder, alloyG, PanelSlot.BOTTOM_LEFT);
		removePanelConnections(builder, alloyG, PanelSlot.BOTTOM_LEFT);
		setPanelItem(builder, alloyG, PanelSlot.BOTTOM_LEFT, ItemStack.EMPTY);

		scene.world()
			.showSection(scaff1, Direction.DOWN);
		scene.idle(5);
		scene.world()
			.showSection(board1, Direction.DOWN);
		scene.idle(10);
		scene.world()
			.showSection(util.select()
				.position(alloyG), Direction.SOUTH);
		scene.idle(25);

		Vec3 panelM = util.vector()
			.of(3.75, 3.25, 4);
		scene.overlay()
			.showText(60)
			.text("Whenever gauges are not placed on a packager...")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(panelM);
		scene.idle(50);

		scene.world()
			.showSection(vault, Direction.NORTH);
		scene.idle(5);
		scene.world()
			.showSection(packS, Direction.EAST);
		scene.idle(15);

		scene.overlay()
			.showOutlineWithText(vault, 100)
			.text("They will instead monitor stock levels of all linked inventories")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.of(4, 3, 7));
		scene.idle(110);

		ItemStack monitorItem = AllItems.ANDESITE_ALLOY.asStack();
		scene.overlay()
			.showControls(panelM, Pointing.DOWN, 50)
			.withItem(monitorItem)
			.rightClick();
		scene.idle(7);

		setPanelItem(builder, alloyG, PanelSlot.BOTTOM_LEFT, monitorItem);

		scene.overlay()
			.showText(80)
			.text("Right-click it with the item that should be monitored")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(panelM);
		scene.idle(90);

		scene.overlay()
			.showOutlineWithText(vault, 100)
			.text("It will now display the total amount present on the network")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(panelM);

		scene.idle(100);

		scene.world()
			.showSection(mixer, Direction.WEST);
		scene.world()
			.showSection(basin, Direction.WEST);
		scene.idle(20);

		scene.overlay()
			.showText(110)
			.text("The gauge can replenish stock levels by sending other items to be processed")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(7, 1, 1), Direction.WEST));
		scene.idle(120);

		setPanelPassive(builder, andeG, PanelSlot.BOTTOM_LEFT);
		removePanelConnections(builder, andeG, PanelSlot.BOTTOM_LEFT);
		setPanelPassive(builder, nuggG, PanelSlot.TOP_LEFT);
		removePanelConnections(builder, nuggG, PanelSlot.TOP_LEFT);

		scene.world()
			.showSection(util.select()
				.position(andeG), Direction.SOUTH);
		scene.idle(5);
		scene.world()
			.showSection(util.select()
				.position(nuggG), Direction.SOUTH);
		scene.idle(20);

		scene.overlay()
			.showText(70)
			.text("First, add the required ingredients as new factory gauges")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(panelM.add(1, -0.5, 0));
		scene.idle(80);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.fromTo(6, 0, 1, 1, 0, 7), 2);

		scene.overlay()
			.showControls(panelM, Pointing.DOWN, 40)
			.rightClick();
		scene.idle(7);
		AABB boundingBox = new AABB(panelM, panelM).inflate(0.19, 0.19, 0);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, alloyG, boundingBox, 100);
		scene.overlay()
			.showText(70)
			.text("From the target's UI, new connections can be made")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(panelM);
		scene.idle(40);
		scene.overlay()
			.showControls(panelM, Pointing.DOWN, 40)
			.showing(AllIcons.I_ADD);
		scene.idle(50);

		scene.overlay()
			.showControls(panelM.add(1, -0.5, 0), Pointing.DOWN, 50)
			.rightClick();
		scene.idle(7);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, nuggG, boundingBox.move(1, -.5, 0), 40);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, alloyG, boundingBox, 40);
		scene.idle(10);

		addPanelConnection(builder, alloyG, PanelSlot.BOTTOM_LEFT, nuggG, PanelSlot.TOP_LEFT);
		setArrowMode(builder, alloyG, PanelSlot.BOTTOM_LEFT, nuggG, PanelSlot.TOP_LEFT, 1);
		scene.idle(45);

		scene.overlay()
			.showControls(panelM, Pointing.DOWN, 40)
			.showing(AllIcons.I_ADD);
		scene.idle(50);
		scene.overlay()
			.showControls(panelM.add(1, 0, 0), Pointing.DOWN, 50)
			.rightClick();
		scene.idle(7);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, andeG, boundingBox.move(1, 0, 0), 40);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, alloyG, boundingBox, 40);
		scene.idle(10);

		addPanelConnection(builder, alloyG, PanelSlot.BOTTOM_LEFT, andeG, PanelSlot.BOTTOM_LEFT);
		scene.idle(45);

		scene.overlay()
			.showText(70)
			.text("For aesthetics, input panels can be wrenched to change the pathing")
			.colored(PonderPalette.BLUE)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(panelM.add(1, -0.5, 0));
		scene.idle(80);

		scene.overlay()
			.showControls(panelM.add(1.125, -0.5, 0), Pointing.RIGHT, 50)
			.rightClick()
			.withItem(AllItems.WRENCH.asStack());
		scene.idle(7);
		setArrowMode(builder, alloyG, PanelSlot.BOTTOM_LEFT, nuggG, PanelSlot.TOP_LEFT, 2);
		scene.idle(60);

		scene.overlay()
			.showControls(panelM, Pointing.DOWN, 100)
			.rightClick();
		scene.idle(7);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, alloyG, boundingBox, 100);
		scene.overlay()
			.showText(110)
			.text("In the UI, review the inputs and specify how much of the output gets made per batch")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(panelM);
		scene.idle(120);
		scene.overlay()
			.showText(80)
			.text("Specify the address that ingredients should be sent to")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(panelM);
		scene.idle(70);

		scene.world()
			.showSection(belt1, Direction.DOWN);
		scene.idle(1);
		scene.world()
			.setBlocks(util.select()
				.fromTo(1, 6, 7, 1, 6, 2), Blocks.AIR.defaultBlockState(), false);
		scene.idle(3);
		scene.world()
			.showSection(belt2, Direction.DOWN);
		scene.idle(1);
		scene.world()
			.setBlocks(util.select()
				.fromTo(1, 6, 1, 3, 6, 1), Blocks.AIR.defaultBlockState(), false);
		scene.idle(3);
		scene.world()
			.showSection(saw, Direction.DOWN);
		scene.idle(1);
		scene.world()
			.setBlocks(util.select()
				.position(4, 6, 1), Blocks.AIR.defaultBlockState(), false);
		scene.idle(3);
		scene.world()
			.showSection(belt3, Direction.DOWN);
		scene.idle(1);
		scene.world()
			.setBlocks(util.select()
				.fromTo(5, 6, 1, 6, 6, 1), Blocks.AIR.defaultBlockState(), false);
		scene.idle(20);

		scene.overlay()
			.showControls(panelM, Pointing.DOWN, 100)
			.rightClick();
		scene.idle(7);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, alloyG, boundingBox, 100);
		scene.overlay()
			.showText(100)
			.text("The target amount to maintain can now be set by holding Right-click on the gauge")
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(panelM);
		scene.idle(110);

		setPanelNotSatisfied(builder, alloyG, PanelSlot.BOTTOM_LEFT);
		scene.idle(20);

		scene.overlay()
			.showOutlineWithText(vault, 80)
			.text("Whenever the network has fewer items than the amount...")
			.colored(PonderPalette.BLUE)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.of(4, 3, 7));

		scene.idle(90);
		PonderHilo.linkEffect(scene, link);
		ItemStack andesiteItem = new ItemStack(Items.ANDESITE);
		ItemStack nuggetItem = new ItemStack(Items.IRON_NUGGET);
		ItemStack box = PackageItem.containing(List.of(andesiteItem, nuggetItem));
		PonderHilo.packagerCreate(scene, pack, box);
		flash(builder, alloyG, PanelSlot.BOTTOM_LEFT);
		scene.idle(20);

		scene.overlay()
			.showText(70)
			.text("...it will send new ingredients to the specified address")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector()
				.blockSurface(pack, Direction.WEST));
		scene.idle(80);

		scene.world()
			.showSection(funnel1, Direction.DOWN);
		scene.idle(5);
		scene.world()
			.showSection(funnel2, Direction.DOWN);
		scene.idle(10);

		scene.world()
			.createItemOnBelt(util.grid()
				.at(1, 0, 7), Direction.EAST, box);
		PonderHilo.packagerClear(scene, pack);
		scene.idle(75);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(6, 0, 1));
		scene.world()
			.flapFunnel(util.grid()
				.at(6, 1, 1), false);
		scene.idle(8);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(6, 0, 1));
		scene.world()
			.flapFunnel(util.grid()
				.at(6, 1, 1), false);

		scene.idle(5);
		Class<MechanicalMixerBlockEntity> type = MechanicalMixerBlockEntity.class;
		scene.world()
			.modifyBlockEntity(util.grid()
				.at(7, 3, 1), type, pte -> pte.startProcessingBasin());
		scene.world()
			.createItemOnBeltLike(util.grid()
				.at(7, 1, 1), Direction.UP, andesiteItem);
		scene.world()
			.createItemOnBeltLike(util.grid()
				.at(7, 1, 1), Direction.UP, nuggetItem);
		scene.idle(20);

		scene.world()
			.showSection(basinOut, Direction.DOWN);
		scene.idle(1);
		scene.world()
			.setBlocks(util.select()
				.fromTo(7, 6, 2, 7, 6, 3), Blocks.AIR.defaultBlockState(), false);
		scene.world()
			.showSection(barrelAndPackager, Direction.DOWN);

		scene.idle(20);
		scene.world()
			.modifyBlockEntityNBT(basin, BasinBlockEntity.class, nbt -> {
				nbt.put("VisualizedItems",
					NBTHelper.writeCompoundList(
						ImmutableList.of(IntAttached.with(1, AllItems.ANDESITE_ALLOY.asStack())), ia -> (CompoundTag) ia.getValue().saveOptional(builder.world().getHolderLookupProvider())));
			});
		scene.idle(4);
		scene.rotateCameraY(90);
		scene.idle(40);

		scene.overlay()
			.showText(90)
			.text("The outputs then need to return to any of the linked inventories")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(7, 1, 3), Direction.UP));
		scene.idle(70);

		scene.world()
			.showSection(belt4, Direction.DOWN);
		scene.idle(1);
		scene.world()
			.setBlocks(util.select()
				.fromTo(7, 6, 5, 7, 6, 7), Blocks.AIR.defaultBlockState(), false);
		scene.world()
			.showSection(pack2S, Direction.DOWN);
		scene.idle(5);
		scene.world()
			.showSection(funnel3, Direction.DOWN);
		scene.idle(10);
		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(7, 1, 4, 7, 2, 4));
		scene.effects()
			.indicateRedstone(util.grid()
				.at(7, 2, 4));
		ItemStack box2 = PackageItem.containing(List.of());
		PonderHilo.packagerCreate(scene, util.grid()
			.at(7, 1, 4), box2);
		scene.idle(20);
		PonderHilo.packagerClear(scene, util.grid()
			.at(7, 1, 4));
		scene.world()
			.createItemOnBelt(util.grid()
				.at(7, 0, 5), Direction.NORTH, box2);
		scene.idle(35);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(7, 0, 7));
		scene.world()
			.flapFunnel(util.grid()
				.at(7, 1, 7), false);
		PonderHilo.packagerUnpack(scene, util.grid()
			.at(6, 1, 7), box2);
		scene.idle(20);
		setPanelSatisfied(scene, alloyG, PanelSlot.BOTTOM_LEFT);

		scene.rotateCameraY(-90);
		scene.idle(40);

		scene.overlay()
			.showText(110)
			.text("Green connections indicate that the target amount has been reached")
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(panelM.add(0.5, 0, 0));
		scene.idle(120);

		scene.world()
			.hideSection(mixer, Direction.EAST);
		scene.world()
			.hideSection(basin, Direction.EAST);
		scene.world()
			.hideSection(basinOut, Direction.UP);
		scene.idle(3);
		scene.world()
			.hideSection(barrelAndPackager, Direction.EAST);
		scene.world()
			.hideSection(funnel1, Direction.UP);
		scene.world()
			.hideSection(util.select()
				.position(6, 1, 1), Direction.UP);
		scene.world()
			.hideSection(util.select()
				.position(7, 1, 5), Direction.UP);
		scene.world()
			.hideSection(util.select()
				.fromTo(6, 0, 1, 1, 0, 1), Direction.DOWN);
		scene.world()
			.hideSection(util.select()
				.fromTo(1, 0, 7, 1, 0, 2), Direction.DOWN);
		scene.idle(5);
		scene.world()
			.restoreBlocks(util.select()
				.fromTo(6, 6, 1, 1, 6, 1));
		scene.world()
			.restoreBlocks(util.select()
				.fromTo(1, 6, 7, 1, 6, 2));
		scene.world()
			.restoreBlocks(util.select()
				.fromTo(7, 6, 4, 7, 6, 1));
		scene.idle(15);

		scene.world()
			.showSection(scaff2, Direction.DOWN);
		ElementLink<WorldSectionElement> scaffL = scene.world()
			.showIndependentSection(scaff2, Direction.DOWN);
		scene.world()
			.moveSection(scaffL, util.vector()
				.of(4, 0, 0), 0);
		scene.idle(10);
		scene.world()
			.showSection(board2, Direction.DOWN);
		scene.idle(10);

		setPanelPassive(builder, ironG, PanelSlot.TOP_LEFT);
		removePanelConnections(builder, ironG, PanelSlot.TOP_LEFT);
		scene.world()
			.showSection(util.select()
				.position(ironG), Direction.SOUTH);
		scene.idle(15);

		addPanelConnection(builder, nuggG, PanelSlot.TOP_LEFT, ironG, PanelSlot.TOP_LEFT);
		setPanelNotSatisfied(builder, nuggG, PanelSlot.TOP_LEFT);
		scene.idle(15);

		scene.overlay()
			.showText(110)
			.text("The board of gauges can expand to include more recipe steps")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(panelM.add(1, -0.5, 0));
		scene.idle(120);

		setPanelPassive(builder, dioriteG, PanelSlot.TOP_LEFT);
		removePanelConnections(builder, dioriteG, PanelSlot.TOP_LEFT);
		scene.world()
			.showSection(util.select()
				.position(dioriteG), Direction.SOUTH);
		scene.idle(5);
		setPanelPassive(builder, cobbleG, PanelSlot.TOP_LEFT);
		removePanelConnections(builder, cobbleG, PanelSlot.TOP_LEFT);
		scene.world()
			.showSection(util.select()
				.position(cobbleG), Direction.SOUTH);
		scene.idle(10);

		addPanelConnection(builder, andeG, PanelSlot.BOTTOM_LEFT, dioriteG, PanelSlot.TOP_LEFT);
		setPanelNotSatisfied(builder, andeG, PanelSlot.BOTTOM_LEFT);
		scene.idle(5);
		addPanelConnection(builder, andeG, PanelSlot.BOTTOM_LEFT, cobbleG, PanelSlot.TOP_LEFT);
		setArrowMode(builder, andeG, PanelSlot.BOTTOM_LEFT, cobbleG, PanelSlot.TOP_LEFT, 2);
		setPanelNotSatisfied(builder, andeG, PanelSlot.BOTTOM_LEFT);
		scene.idle(15);

		removePanelConnections(builder, quartzG, PanelSlot.BOTTOM_RIGHT);
		removePanelConnections(builder, logsG, PanelSlot.TOP_LEFT);
		removePanelConnections(builder, alloyG, PanelSlot.TOP_RIGHT);
		removePanelConnections(builder, logsG, PanelSlot.BOTTOM_RIGHT);
		removePanelConnections(builder, planksG, PanelSlot.BOTTOM_RIGHT);
		removePanelConnections(builder, rawIronG, PanelSlot.BOTTOM_RIGHT);
		removePanelConnections(builder, cogG, PanelSlot.TOP_LEFT);

		scene.overlay()
			.showText(110)
			.text("Each gauge maintains the stock level of its item independently")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(panelM.add(1, 0, 0));
		scene.idle(120);

		setPanelPassive(builder, quartzG, PanelSlot.BOTTOM_RIGHT);
		scene.world()
			.showSection(util.select()
				.position(quartzG), Direction.SOUTH);
		scene.idle(15);

		addPanelConnection(builder, dioriteG, PanelSlot.TOP_LEFT, quartzG, PanelSlot.BOTTOM_RIGHT);
		addPanelConnection(builder, dioriteG, PanelSlot.TOP_LEFT, cobbleG, PanelSlot.TOP_LEFT);
		setPanelNotSatisfied(builder, dioriteG, PanelSlot.TOP_LEFT);
		scene.idle(5);

		scene.world()
			.showSection(util.select()
				.position(logsG), Direction.SOUTH);
		scene.idle(10);

		scene.world()
			.showSection(util.select()
				.position(rawIronG), Direction.SOUTH);

		scene.idle(4);
		setPanelVisible(builder, alloyG, PanelSlot.TOP_RIGHT, true);
		scene.idle(1);
		addPanelConnection(builder, logsG, PanelSlot.BOTTOM_RIGHT, logsG, PanelSlot.TOP_LEFT);
		setPanelNotSatisfied(builder, logsG, PanelSlot.BOTTOM_RIGHT);
		scene.world()
			.showSection(util.select()
				.position(planksG), Direction.SOUTH);
		scene.idle(5);
		scene.world()
			.showSection(util.select()
				.position(cogG), Direction.SOUTH);
		addPanelConnection(builder, alloyG, PanelSlot.TOP_RIGHT, alloyG, PanelSlot.BOTTOM_LEFT);
		setPanelNotSatisfied(builder, alloyG, PanelSlot.TOP_RIGHT);
		scene.idle(5);
		addPanelConnection(builder, ironG, PanelSlot.TOP_LEFT, rawIronG, PanelSlot.BOTTOM_RIGHT);
		setPanelNotSatisfied(builder, ironG, PanelSlot.TOP_LEFT);
		scene.idle(5);
		addPanelConnection(builder, planksG, PanelSlot.BOTTOM_RIGHT, logsG, PanelSlot.BOTTOM_RIGHT);
		setPanelNotSatisfied(builder, planksG, PanelSlot.BOTTOM_RIGHT);
		scene.idle(5);
		addPanelConnection(builder, cogG, PanelSlot.TOP_LEFT, planksG, PanelSlot.BOTTOM_RIGHT);
		addPanelConnection(builder, cogG, PanelSlot.TOP_LEFT, alloyG, PanelSlot.TOP_RIGHT);
		setPanelNotSatisfied(builder, cogG, PanelSlot.TOP_LEFT);

	}

	public static void crafting(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("factory_gauge_crafting", "Automatic Crafting with Factory Gauges");
		scene.configureBasePlate(0, 0, 9);
		scene.scaleSceneView(0.925f);
		scene.setSceneOffsetY(-0.5f);
		scene.showBasePlate();

		Selection scaff = util.select()
			.fromTo(3, 1, 5, 1, 1, 5);
		Selection board = util.select()
			.fromTo(3, 2, 5, 1, 3, 5);
		FactoryPanelPosition pickG = new FactoryPanelPosition(util.grid()
			.at(1, 3, 4), PanelSlot.BOTTOM_LEFT);
		FactoryPanelPosition stickG = new FactoryPanelPosition(util.grid()
			.at(2, 2, 4), PanelSlot.TOP_LEFT);
		FactoryPanelPosition diaG = new FactoryPanelPosition(util.grid()
			.at(3, 3, 4), PanelSlot.BOTTOM_RIGHT);

		BlockPos funnelToDelete = util.grid()
			.at(1, 2, 1);
		scene.world()
			.setBlock(funnelToDelete, Blocks.AIR.defaultBlockState(), false);

		Selection belt1 = util.select()
			.fromTo(0, 1, 1, 7, 1, 1);
		Selection belt2 = util.select()
			.fromTo(7, 1, 7, 0, 1, 7);
		Selection repacker = util.select()
			.fromTo(1, 2, 1, 4, 3, 1);
		Selection crafterBits = util.select()
			.fromTo(6, 1, 3, 7, 1, 3);
		Selection crafter = util.select()
			.fromTo(6, 1, 2, 6, 4, 4)
			.substract(crafterBits);
		BlockPos pack = util.grid()
			.at(6, 2, 1);
		Selection packS = util.select()
			.position(6, 2, 1);
		Selection cogs1 = util.select()
			.fromTo(7, 1, 2, 8, 1, 2);
		Selection cogs2 = util.select()
			.fromTo(7, 1, 6, 8, 1, 6);
		Selection largeCog1 = util.select()
			.position(9, 0, 2);
		Selection largeCog2 = util.select()
			.position(9, 0, 6);
		Selection barrel = util.select()
			.fromTo(6, 1, 5, 6, 2, 5);
		Selection outPacker = util.select()
			.fromTo(6, 1, 6, 6, 3, 6)
			.add(util.select()
				.position(5, 2, 6));
		Selection outFunnel = util.select()
			.position(6, 2, 7);

		scene.idle(10);
		scene.world()
			.showSection(scaff, Direction.NORTH);
		scene.world()
			.showSection(board, Direction.DOWN);
		scene.idle(10);
		scene.world()
			.showSection(crafterBits, Direction.WEST);
		scene.world()
			.showSection(largeCog1, Direction.UP);
		scene.world()
			.showSection(cogs1, Direction.SOUTH);
		scene.idle(5);
		scene.world()
			.showSection(crafter, Direction.DOWN);
		scene.idle(15);

		scene.overlay()
			.showText(100)
			.text("Factory gauges provide auto-arrangement for crafting table recipes")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(6, 3, 3), Direction.WEST));
		scene.idle(110);

		removePanelConnections(builder, pickG.pos(), pickG.slot());
		setPanelPassive(builder, pickG.pos(), pickG.slot());

		scene.world()
			.showSection(util.select()
				.position(diaG.pos()), Direction.SOUTH);
		scene.idle(5);
		scene.world()
			.showSection(util.select()
				.position(stickG.pos()), Direction.SOUTH);
		scene.idle(5);
		scene.world()
			.showSection(util.select()
				.position(pickG.pos()), Direction.SOUTH);
		scene.idle(15);
		addPanelConnection(builder, pickG.pos(), pickG.slot(), diaG.pos(), diaG.slot());
		scene.idle(5);
		addPanelConnection(builder, pickG.pos(), pickG.slot(), stickG.pos(), stickG.slot());
		scene.idle(5);

		Vec3 midl = util.vector()
			.of(1.75, 3.25, 5);
		scene.overlay()
			.showText(60)
			.text("Connect the required ingredients as before")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(midl);
		scene.idle(80);

		scene.overlay()
			.showControls(midl, Pointing.DOWN, 120)
			.showing(AllIcons.I_3x3);
		scene.idle(7);
		AABB boundingBox = new AABB(midl, midl).inflate(0.19, 0.19, 0);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, pickG, boundingBox, 100);
		scene.idle(10);
		scene.overlay()
			.showText(90)
			.text("When a valid recipe is detected, a new button appears in the UI")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(midl);
		scene.idle(100);

		scene.world()
			.showSection(belt1, Direction.SOUTH);
		scene.world()
			.showSection(packS, Direction.DOWN);
		scene.idle(30);

		scene.overlay()
			.showText(120)
			.text("With auto-arrangement active, the boxes can be unwrapped into crafters directly")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(6, 2, 1), Direction.WEST));
		scene.idle(130);
		scene.rotateCameraY(90);
		scene.idle(40);

		scene.overlay()
			.showControls(util.vector()
				.blockSurface(util.grid()
					.at(6, 3, 4), Direction.EAST),
				Pointing.RIGHT, 120)
			.rightClick()
			.withItem(AllItems.WRENCH.asStack());
		scene.idle(7);

		scene.world()
			.connectCrafterInvs(util.grid()
				.at(6, 4, 4),
				util.grid()
					.at(6, 4, 3));
		scene.world()
			.connectCrafterInvs(util.grid()
				.at(6, 4, 3),
				util.grid()
					.at(6, 4, 2));
		scene.world()
			.connectCrafterInvs(util.grid()
				.at(6, 4, 4),
				util.grid()
					.at(6, 3, 4));
		scene.world()
			.connectCrafterInvs(util.grid()
				.at(6, 4, 3),
				util.grid()
					.at(6, 3, 3));
		scene.world()
			.connectCrafterInvs(util.grid()
				.at(6, 4, 2),
				util.grid()
					.at(6, 3, 2));
		scene.world()
			.connectCrafterInvs(util.grid()
				.at(6, 3, 4),
				util.grid()
					.at(6, 2, 4));
		scene.world()
			.connectCrafterInvs(util.grid()
				.at(6, 3, 3),
				util.grid()
					.at(6, 2, 3));
		scene.world()
			.connectCrafterInvs(util.grid()
				.at(6, 3, 2),
				util.grid()
					.at(6, 2, 2));
		for (int y = 0; y < 3; y++)
			for (int z = 0; z < 3; z++)
				scene.effects()
					.indicateSuccess(util.grid()
						.at(6, 2 + y, 2 + z));
		scene.idle(20);

		scene.overlay()
			.showOutlineWithText(util.select()
				.fromTo(6, 2, 2, 6, 4, 4), 100)
			.text("The setup must be 3x3 and the crafters have to be connected via wrench")
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(6, 3, 2), Direction.NORTH));

		scene.idle(100);
		scene.rotateCameraY(-90);
		scene.idle(20);
		scene.world()
			.showSection(util.select()
				.position(5, 2, 1), Direction.DOWN);
		scene.idle(10);
		scene.world()
			.showSection(barrel, Direction.NORTH);
		scene.idle(20);

		scene.overlay()
			.showControls(midl, Pointing.DOWN, 60)
			.rightClick();
		scene.idle(7);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, pickG, boundingBox, 100);
		scene.overlay()
			.showText(60)
			.text("Hold Right-click on the gauge to set the target amount")
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(midl);

		scene.idle(70);
		setPanelNotSatisfied(builder, pickG.pos(), pickG.slot());
		scene.idle(10);
		ItemStack box = PackageItem.containing(List.of());
		scene.world()
			.createItemOnBelt(util.grid()
				.at(0, 1, 1), Direction.WEST, box);
		scene.idle(40);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(5, 1, 1));
		scene.world()
			.flapFunnel(util.grid()
				.at(5, 2, 1), false);
		PonderHilo.packagerUnpack(scene, pack, box);
		scene.rotateCameraY(-15);
		scene.idle(15);

		insertItemsIntoCrafter(scene, util.grid()
			.at(6, 4, 2), new ItemStack(Items.DIAMOND));
		insertItemsIntoCrafter(scene, util.grid()
			.at(6, 4, 3), new ItemStack(Items.DIAMOND));
		insertItemsIntoCrafter(scene, util.grid()
			.at(6, 4, 4), new ItemStack(Items.DIAMOND));
		insertItemsIntoCrafter(scene, util.grid()
			.at(6, 3, 3), new ItemStack(Items.STICK));
		insertItemsIntoCrafter(scene, util.grid()
			.at(6, 2, 3), new ItemStack(Items.STICK));
		scene.world()
			.setCraftingResult(util.grid()
				.at(6, 2, 4), new ItemStack(Items.DIAMOND_PICKAXE));
		scene.world()
			.modifyBlockEntity(util.grid()
				.at(6, 3, 2), MechanicalCrafterBlockEntity.class, be -> be.checkCompletedRecipe(true));

		scene.idle(60);
		scene.overlay()
			.showText(120)
			.text("This crafter can now be used universally, by more gauges with different recipes")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(6, 3, 4), Direction.WEST));

		scene.idle(120);

		scene.world()
			.showSection(outPacker, Direction.NORTH);
		scene.idle(20);
		scene.rotateCameraY(15);
		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(6, 2, 6, 6, 3, 6));
		scene.effects()
			.indicateRedstone(util.grid()
				.at(6, 3, 6));
		scene.world()
			.showSection(belt2, Direction.NORTH);
		scene.world()
			.showSection(outFunnel, Direction.DOWN);
		scene.world()
			.showSection(largeCog2, Direction.UP);
		scene.world()
			.showSection(cogs2, Direction.WEST);
		scene.world()
			.restoreBlocks(util.select()
				.position(funnelToDelete));
		scene.idle(15);
		ItemStack box2 = PackageItem.containing(List.of());
		PonderHilo.packagerCreate(scene, util.grid()
			.at(6, 2, 6), box2);
		scene.idle(20);
		PonderHilo.packagerClear(scene, util.grid()
			.at(6, 2, 6));
		scene.world()
			.createItemOnBelt(util.grid()
				.at(6, 1, 7), Direction.NORTH, box2);

		scene.overlay()
			.showText(100)
			.text("Outputs should be sent back to a linked inventory to close the loop")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(6, 2, 6), Direction.WEST));

		scene.idle(45);
		PonderHilo.packageHopsOffBelt(scene, util.grid()
			.at(0, 1, 7), Direction.WEST, box2);
		scene.idle(50);

		scene.world()
			.showSection(repacker, Direction.DOWN);
		scene.idle(20);

		scene.overlay()
			.showText(120)
			.text("Using a Re-packager is recommended to prevent fragmentation of input packages")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(3, 2, 1), Direction.NORTH));
		scene.idle(120);
	}

	public static void links(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("factory_gauge_links", "Connecting Gauges to other Blocks");
		scene.setSceneOffsetY(-1f);
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		Selection scaff = util.select()
			.fromTo(3, 1, 2, 1, 1, 2);
		Selection board = util.select()
			.fromTo(3, 3, 2, 1, 2, 2);
		BlockPos link = util.grid()
			.at(3, 2, 1);
		BlockPos display = util.grid()
			.at(1, 2, 1);
		BlockPos gauge = util.grid()
			.at(2, 3, 1);
		Selection linkS = util.select()
			.position(3, 2, 1);
		Selection displayS = util.select()
			.position(1, 2, 1);
		Selection gaugeS = util.select()
			.position(2, 3, 1);
		PanelSlot slot = PanelSlot.TOP_LEFT;

		removePanelConnections(scene, gauge, slot);
		scene.idle(10);
		scene.world()
			.showSection(scaff, Direction.NORTH);
		scene.world()
			.showSection(board, Direction.DOWN);
		scene.idle(10);
		scene.world()
			.showSection(gaugeS, Direction.SOUTH);
		scene.idle(20);

		Vec3 midl = util.vector()
			.of(2.75, 3.75, 2);
		AABB boundingBox = new AABB(midl, midl).inflate(0.19, 0.19, 0);
		scene.overlay()
			.showControls(midl, Pointing.DOWN, 60)
			.showing(AllIcons.I_ADD);
		scene.idle(7);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, gauge, boundingBox, 100);
		scene.idle(10);
		scene.overlay()
			.showText(70)
			.text("When adding a new connection from the UI...")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(midl);
		scene.idle(50);

		scene.world()
			.showSection(linkS, Direction.SOUTH);
		scene.idle(5);
		scene.world()
			.showSection(displayS, Direction.SOUTH);

		scene.idle(30);
		scene.overlay()
			.showControls(util.vector()
				.of(4, 2.5, 2), Pointing.RIGHT, 60)
			.rightClick();

		scene.idle(7);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, link, boundingBox.move(0.75, -1.25, 0)
				.inflate(0.15, 0.25, 0), 40);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, gauge, boundingBox, 40);

		addPanelConnection(builder, gauge, slot, link, PanelSlot.TOP_RIGHT);
		setArrowMode(builder, gauge, slot, link, PanelSlot.TOP_RIGHT, 0);
		scene.idle(20);

		scene.overlay()
			.showText(70)
			.text("...the gauge also accepts Redstone and Display Links")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(midl.add(0.5, -1.5, 0));
		scene.idle(80);

		setPanelSatisfied(builder, gauge, slot);
		scene.world()
			.toggleRedstonePower(util.select()
				.position(3, 2, 1));
		scene.effects()
			.indicateRedstone(util.grid()
				.at(3, 2, 1));
		scene.idle(40);

		scene.overlay()
			.showText(120)
			.text("Redstone links will be powered when the stock level is at or above the target amount")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(midl);
		scene.idle(130);

		scene.world()
			.toggleRedstonePower(util.select()
				.position(3, 2, 1));
		setPanelNotSatisfied(builder, gauge, slot);

		scene.idle(30);
		scene.overlay()
			.showControls(util.vector()
				.of(4, 2.5, 2), Pointing.RIGHT, 60)
			.withItem(AllItems.WRENCH.asStack())
			.rightClick();
		scene.idle(7);
		scene.world()
			.cycleBlockProperty(util.grid()
				.at(3, 2, 1), RedstoneLinkBlock.RECEIVER);
		scene.idle(30);

		scene.world()
			.toggleRedstonePower(util.select()
				.position(3, 2, 1));
		scene.effects()
			.indicateRedstone(util.grid()
				.at(3, 2, 1));
		setPanelPowered(builder, gauge, slot, true);
		scene.idle(30);

		scene.overlay()
			.showText(100)
			.text("In receiver mode, links can stop the gauge from sending requests")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(midl);
		scene.idle(130);

		addPanelConnection(builder, gauge, slot, display, slot);
		setArrowMode(builder, gauge, slot, display, slot, 2);

		scene.overlay()
			.showText(100)
			.text("Display links can provide a status overview of connected gauges")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.of(1, 3, 1));
		scene.idle(100);

	}

	private static void setPanelItem(SceneBuilder builder, BlockPos gauge, PanelSlot slot, ItemStack item) {
		withGaugeDo(builder, gauge, slot, pb -> pb.setFilter(item));
	}

	private static void setPanelPowered(SceneBuilder builder, BlockPos gauge, PanelSlot slot, boolean power) {
		withGaugeDo(builder, gauge, slot, pb -> pb.redstonePowered = power);
	}

	private static void setPanelVisible(SceneBuilder builder, BlockPos gauge, PanelSlot slot, boolean visible) {
		withGaugeDo(builder, gauge, slot, pb -> pb.active = visible);
	}

	private static void setPanelNotSatisfied(SceneBuilder builder, BlockPos gauge, PanelSlot slot) {
		withGaugeDo(builder, gauge, slot, pb -> pb.count = 2);
	}

	private static void flash(SceneBuilder builder, BlockPos gauge, PanelSlot slot) {
		withGaugeDo(builder, gauge, slot, pb -> pb.bulb.setValue(1));
	}

	private static void setPanelSatisfied(SceneBuilder builder, BlockPos gauge, PanelSlot slot) {
		withGaugeDo(builder, gauge, slot, pb -> pb.count = 1);
	}

	private static void setPanelPassive(SceneBuilder builder, BlockPos gauge, PanelSlot slot) {
		withGaugeDo(builder, gauge, slot, pb -> pb.count = 0);
	}

	private static void removePanelConnections(SceneBuilder builder, BlockPos gauge, PanelSlot slot) {
		withGaugeDo(builder, gauge, slot, pb -> pb.disconnectAll());
	}

	private static void setArrowMode(SceneBuilder builder, BlockPos gauge, PanelSlot slot, BlockPos fromGauge,
		PanelSlot fromSlot, int mode) {
		withGaugeDo(builder, gauge, slot, pb -> {
			FactoryPanelConnection connection = pb.targetedBy.get(new FactoryPanelPosition(fromGauge, fromSlot));
			if (connection == null) {
				connection = pb.targetedByLinks.get(fromGauge);
				if (connection == null)
					return;
			}
			connection.arrowBendMode = mode;
		});
	}

	private static void addPanelConnection(SceneBuilder builder, BlockPos gauge, PanelSlot slot, BlockPos fromGauge,
		PanelSlot fromSlot) {
		withGaugeDo(builder, gauge, slot, pb -> pb.addConnection(new FactoryPanelPosition(fromGauge, fromSlot)));
	}

	private static void insertItemsIntoCrafter(CreateSceneBuilder scene, BlockPos pos, ItemStack stack) {
		scene.world()
			.modifyBlockEntity(pos, MechanicalCrafterBlockEntity.class, be -> be.getInventory()
				.setItem(0, stack));
	}

	private static void withGaugeDo(SceneBuilder builder, BlockPos gauge, PanelSlot slot,
		Consumer<FactoryPanelBehaviour> call) {
		builder.world()
			.modifyBlockEntity(gauge, FactoryPanelBlockEntity.class, be -> call.accept(be.panels.get(slot)));
	}

}
