package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.funnel.FunnelBlock;
import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;

public class FunnelScenes {

	public static void intro(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("funnel_intro", "Using funnels");
		scene.configureBasePlate(0, 1, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> f / 2f);

		scene.idle(10);

		Selection verticalFunnel = util.select.fromTo(2, -1, 4, 2, 4, 4)
			.add(util.select.fromTo(1, 1, 4, 1, 4, 4));
		Selection beltFunnels = util.select.fromTo(1, 2, 2, 3, 2, 2);
		Selection beltFunnelEnv = util.select.fromTo(0, 1, 0, 5, 2, 2)
			.substract(beltFunnels);

		scene.world.showSection(beltFunnelEnv, Direction.DOWN);

		scene.idle(20);
		scene.world.showSection(beltFunnels, Direction.DOWN);

		BlockPos entryBeltPos = util.grid.at(3, 1, 2);
		BlockPos exitBeltPos = util.grid.at(1, 1, 2);
		ItemStack itemStack = AllBlocks.BRASS_BLOCK.asStack();

		for (int i = 0; i < 8; i++) {
			scene.idle(8);
			scene.world.removeItemsFromBelt(exitBeltPos);
			scene.world.flapFunnel(exitBeltPos.above(), false);
			if (i == 2)
				scene.rotateCameraY(70);
			if (i < 6)
				scene.world.createItemOnBelt(entryBeltPos, Direction.EAST, itemStack);
		}

		scene.rotateCameraY(-70);
		scene.idle(10);

		Selection outputFunnel = util.select.position(1, 2, 4);
		scene.world.setBlocks(outputFunnel, Blocks.AIR.defaultBlockState(), false);
		scene.world.setBlocks(util.select.fromTo(2, -1, 4, 2, 0, 4), AllBlocks.ANDESITE_CASING.getDefaultState(), true);
		ElementLink<WorldSectionElement> independentSection =
			scene.world.showIndependentSection(verticalFunnel, Direction.UP);

		Vec3 topItemSpawn = util.vector.centerOf(2, 6, 4);
		Vec3 sideItemSpawn = util.vector.centerOf(1, 3, 4)
			.add(0.15f, -0.45f, 0);
		ElementLink<EntityElement> lastItemEntity = null;

		for (int i = 0; i < 4; i++) {
			if (lastItemEntity != null)
				scene.world.modifyEntity(lastItemEntity, Entity::discard);
			if (i < 3)
				lastItemEntity = scene.world.createItemEntity(topItemSpawn, util.vector.of(0, -0.4, 0), itemStack);
			scene.idle(8);
		}

		scene.world.moveSection(independentSection, util.vector.of(0, 1, 0), 15);
		scene.idle(10);
		scene.world.setBlocks(outputFunnel, AllBlocks.ANDESITE_FUNNEL.getDefaultState()
			.setValue(FunnelBlock.FACING, Direction.WEST)
			.setValue(FunnelBlock.EXTRACTING, true), false);

		for (int i = 0; i < 3; i++) {
			scene.idle(8);
			scene.world.flapFunnel(util.grid.at(1, 2, 4), false);
			scene.world.createItemEntity(sideItemSpawn, util.vector.of(-.05, 0, 0), itemStack);
		}

		scene.idle(8);
		scene.overlay.showText(360)
			.text("Funnels are ideal for transferring items from and to inventories.")
			.independent();
		scene.markAsFinished();
	}

	public static void directionality(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("funnel_direction", "Direction of Transfer");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> f / 2f);
		scene.world.setBlocks(util.select.position(3, 1, 1), AllBlocks.ANDESITE_CASING.getDefaultState(), false);

		BlockPos topFunnel = util.grid.at(3, 3, 2);
		Selection topFunnelSelection = util.select.position(topFunnel);
		Selection firstShow = util.select.fromTo(3, 1, 2, 3, 2, 2);
		scene.idle(5);

		scene.world.showSection(firstShow, Direction.DOWN);
		scene.idle(15);

		ItemStack itemStack = AllBlocks.BRASS_BLOCK.asStack();
		Vec3 topCenter = util.vector.centerOf(topFunnel);
		Vec3 topSide = util.vector.blockSurface(topFunnel, Direction.EAST);

		InputWindowElement controlsSneak = new InputWindowElement(topCenter, Pointing.DOWN).rightClick()
			.whileSneaking();

		// Placing funnels without sneak
		scene.world.showSection(topFunnelSelection, Direction.DOWN);
		scene.overlay.showText(80)
			.text("Placed normally, it pulls items from the inventory.")
			.attachKeyFrame()
			.pointAt(topCenter)
			.placeNearTarget();
		scene.idle(45);

		ElementLink<EntityElement> itemLink =
			scene.world.createItemEntity(topCenter, util.vector.of(0, 4 / 16f, 0), itemStack);
		scene.idle(40);

		scene.world.modifyEntity(itemLink, Entity::discard);
		scene.world.hideSection(topFunnelSelection, Direction.UP);
		scene.idle(20);

		// Placing funnels with sneak
		scene.world.modifyBlock(topFunnel, s -> s.setValue(FunnelBlock.EXTRACTING, false), false);
		scene.idle(5);

		scene.world.showSection(topFunnelSelection, Direction.DOWN);
		scene.overlay.showControls(controlsSneak, 35);
		scene.overlay.showText(80)
			.text("Placed while sneaking, it puts items into the inventory.")
			.attachKeyFrame()
			.pointAt(topCenter)
			.placeNearTarget();
		scene.idle(45);

		itemLink = scene.world.createItemEntity(topCenter.add(0, 3, 0), util.vector.of(0, -0.2, 0), itemStack);
		scene.idle(10);

		scene.world.modifyEntity(itemLink, Entity::discard);
		scene.idle(45);

		// Wrench interaction
		InputWindowElement wrenchControls = new InputWindowElement(topSide, Pointing.RIGHT).rightClick()
			.withWrench();
		scene.overlay.showControls(wrenchControls, 40);
		scene.idle(10);
		scene.world.modifyBlock(topFunnel, s -> s.cycle(FunnelBlock.EXTRACTING), true);
		scene.idle(10);
		scene.overlay.showText(80)
			.text("Using a wrench, the funnel can be flipped after placement.")
			.attachKeyFrame()
			.pointAt(topCenter)
			.placeNearTarget();

		itemLink = scene.world.createItemEntity(topCenter, util.vector.of(0, 4 / 16f, 0), itemStack);
		scene.idle(30);

		scene.overlay.showControls(wrenchControls, 40);
		scene.idle(10);
		scene.world.modifyBlock(topFunnel, s -> s.cycle(FunnelBlock.EXTRACTING), true);
		scene.idle(10);
		scene.world.modifyEntity(itemLink, Entity::discard);

		scene.idle(20);

		// Side funnel
		BlockPos sideFunnel = util.grid.at(3, 2, 1);
		Selection sideFunnelSelection = util.select.fromTo(sideFunnel.below(), sideFunnel);
		Vec3 sideCenter = util.vector.centerOf(sideFunnel);

		scene.world.modifyBlock(sideFunnel, s -> s.cycle(FunnelBlock.EXTRACTING), false);
		scene.world.showSection(sideFunnelSelection, Direction.DOWN);
		scene.overlay.showText(70)
			.text("Same rules will apply for most orientations.")
			.pointAt(sideCenter)
			.placeNearTarget();

		scene.idle(20);

		scene.world.flapFunnel(sideFunnel, true);
		itemLink = scene.world.createItemEntity(sideCenter.subtract(0, .45, 0), util.vector.of(0, 0, -0.1), itemStack);
		scene.idle(60);
		scene.world.hideSection(sideFunnelSelection, Direction.UP);
		scene.world.hideSection(topFunnelSelection, Direction.UP);
		scene.world.modifyEntity(itemLink, Entity::discard);
		scene.idle(20);

		// Belt funnel
		Selection beltFunnelSetup = util.select.fromTo(0, 1, 0, 2, 2, 5);
		Selection gearshiftAndLever = util.select.fromTo(1, 1, 4, 1, 2, 4);
		Selection gearshiftedKinetics = util.select.fromTo(1, 1, 2, 2, 1, 4);
		Vec3 topOfBeltFunnel = util.vector.topOf(2, 2, 2);
		BlockPos beltPos = util.grid.at(2, 1, 2);
		BlockPos cogPos = util.grid.at(1, 1, 3);

		scene.world.showSection(beltFunnelSetup, Direction.DOWN);
		scene.overlay.showText(140)
			.text("Funnels on belts will extract/insert depending on its movement direction.")
			.attachKeyFrame()
			.pointAt(topOfBeltFunnel);

		scene.idle(15);

		for (int i = 0; i < 2; i++) {
			scene.world.createItemOnBelt(beltPos, Direction.EAST, itemStack);
			scene.effects.rotationDirectionIndicator(cogPos);
			scene.idle(50);

			scene.world.modifyBlocks(gearshiftAndLever, s -> s.cycle(BlockStateProperties.POWERED), false);
			scene.world.modifyKineticSpeed(gearshiftedKinetics, f -> -f);
			scene.effects.indicateRedstone(util.grid.at(1, 2, 4));
			scene.effects.rotationDirectionIndicator(cogPos);
			scene.idle(35);

			scene.world.removeItemsFromBelt(beltPos);
			scene.world.flapFunnel(util.grid.at(2, 2, 2), false);

			if (i == 0) {
				scene.idle(50);
				scene.world.modifyBlocks(gearshiftAndLever, s -> s.cycle(BlockStateProperties.POWERED), false);
				scene.world.modifyKineticSpeed(gearshiftedKinetics, f -> -f);
				scene.effects.indicateRedstone(util.grid.at(1, 2, 4));
			}
		}
	}

	public static void compat(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("funnel_compat", "Funnel compatibility");
		scene.configureBasePlate(0, 0, 5);

		BlockPos sawFunnel = util.grid.at(4, 2, 1);
		BlockPos depotFunnel = util.grid.at(2, 2, 2);
		BlockPos drainFunnel = util.grid.at(0, 2, 3);

		scene.world.showSection(util.select.layer(0), Direction.UP);
		Selection firstShow = util.select.layer(1)
			.add(util.select.position(sawFunnel.south()))
			.add(util.select.position(depotFunnel.south()))
			.add(util.select.position(drainFunnel.south()));
		scene.idle(5);

		scene.world.showSection(firstShow, Direction.DOWN);

		scene.idle(8);
		scene.overlay.showText(360)
			.text("Funnels should also interact nicely with a handful of other components.")
			.attachKeyFrame()
			.independent(0);
		scene.idle(40);

		scene.world.showSection(util.select.position(sawFunnel), Direction.DOWN);
		scene.overlay.showText(40)
			.text("Vertical Saws")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector.centerOf(sawFunnel.below()));
		scene.idle(8);
		scene.world.createItemOnBeltLike(sawFunnel.below(), Direction.SOUTH, new ItemStack(Blocks.OAK_LOG));
		scene.idle(40);

		scene.world.showSection(util.select.position(depotFunnel), Direction.DOWN);
		scene.overlay.showText(40)
			.text("Depots")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector.centerOf(depotFunnel.below()));
		scene.idle(8);
		scene.world.createItemOnBeltLike(depotFunnel.below(), Direction.SOUTH, new ItemStack(Items.GOLDEN_PICKAXE));
		scene.idle(40);

		scene.world.showSection(util.select.position(drainFunnel), Direction.DOWN);
		scene.overlay.showText(40)
			.text("Item Drains")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector.centerOf(drainFunnel.below()));
		scene.idle(8);
		scene.world.createItemOnBeltLike(drainFunnel.below(), Direction.SOUTH, new ItemStack(Items.WATER_BUCKET));
		scene.idle(40);

		scene.markAsFinished();
	}

	public static void redstone(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("funnel_redstone", "Redstone control");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);

		ItemStack itemStack = AllBlocks.BRASS_BLOCK.asStack();
		Vec3 topItemSpawn = util.vector.centerOf(3, 6, 2);
		ElementLink<EntityElement> lastItemEntity = null;

		BlockPos lever = util.grid.at(1, 2, 2);
		BlockPos redstone = util.grid.at(2, 2, 2);
		BlockPos funnel = util.grid.at(3, 2, 2);

		AABB redstoneBB = new AABB(funnel).inflate(-1 / 16f, -6 / 16f, -1 / 16f)
			.move(0, -5 / 16f, 0);

		for (int i = 0; i < 4; i++) {
			if (lastItemEntity != null)
				scene.world.modifyEntity(lastItemEntity, Entity::discard);
			lastItemEntity = scene.world.createItemEntity(topItemSpawn, util.vector.of(0, -0.2, 0), itemStack);
			scene.idle(8);

			if (i == 3) {
				scene.world.modifyBlock(lever, s -> s.cycle(LeverBlock.POWERED), false);
				scene.world.modifyBlock(redstone, s -> s.setValue(RedStoneWireBlock.POWER, 15), false);
				scene.world.modifyBlock(funnel, s -> s.cycle(FunnelBlock.POWERED), false);
				scene.effects.indicateRedstone(lever);
				scene.idle(4);
				scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, funnel, redstoneBB, 80);
				scene.overlay.showText(80)
					.colored(PonderPalette.RED)
					.text("Redstone power will prevent any funnel from acting")
					.pointAt(util.vector.blockSurface(funnel, Direction.DOWN));
			} else {
				scene.idle(4);
			}
		}

		scene.idle(60);
	}

	public static void brass(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("brass_funnel", "The Brass Funnel");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		BlockPos firstDepot = util.grid.at(3, 1, 1);
		BlockPos secondDepot = util.grid.at(1, 1, 1);
		Selection depots = util.select.fromTo(firstDepot, secondDepot);
		Selection beltAndStuff = util.select.fromTo(0, 1, 2, 4, 1, 2)
			.add(util.select.fromTo(0, 1, 3, 0, 2, 5));
		Selection withoutBelt = util.select.layersFrom(1)
			.substract(beltAndStuff)
			.substract(depots);

		scene.world.showSection(withoutBelt, Direction.DOWN);
		ElementLink<WorldSectionElement> independentSection =
			scene.world.showIndependentSection(depots, Direction.DOWN);
		scene.world.moveSection(independentSection, util.vector.of(0, 0, 1), 0);

		BlockPos andesiteFunnel = util.grid.at(3, 2, 2);
		BlockPos brassFunnel = util.grid.at(1, 2, 2);
		ItemStack itemStack = AllItems.BRASS_INGOT.asStack();
		scene.idle(10);

		scene.overlay.showText(60)
			.text("Andesite Funnels can only ever extract single items.")
			.attachKeyFrame()
			.pointAt(util.vector.topOf(andesiteFunnel))
			.placeNearTarget();
		scene.idle(10);
		scene.world.createItemOnBeltLike(andesiteFunnel.below()
			.north(), Direction.SOUTH, itemStack);
		scene.world.flapFunnel(andesiteFunnel, true);
		scene.idle(60);

		Vec3 filter = util.vector.topOf(brassFunnel);
		scene.overlay.showText(60)
			.text("Brass Funnels can extract up to a full stack.")
			.attachKeyFrame()
			.pointAt(filter)
			.placeNearTarget();
		scene.idle(10);
		scene.world.createItemOnBeltLike(brassFunnel.below()
			.north(), Direction.SOUTH, ItemHandlerHelper.copyStackWithSize(itemStack, 64));
		scene.world.flapFunnel(brassFunnel, true);
		scene.idle(60);

		filter = filter.add(0, -5 / 16f, -1.5 / 16f);
		scene.overlay.showFilterSlotInput(filter, Direction.NORTH, 80);
		scene.overlay.showControls(new InputWindowElement(filter, Pointing.DOWN).rightClick(), 60);
		scene.idle(10);
		scene.overlay.showText(80)
			.text("The value panel allows for precise control over the extracted stack size.")
			.attachKeyFrame()
			.pointAt(filter)
			.placeNearTarget();
		scene.idle(90);

		// belt
		scene.world.hideIndependentSection(independentSection, Direction.NORTH);
		scene.world.hideSection(util.select.position(brassFunnel), Direction.UP);
		scene.idle(20);

		scene.world.modifyBlock(brassFunnel, s -> s.cycle(BeltFunnelBlock.SHAPE), false);
		scene.world.showSection(util.select.position(brassFunnel), Direction.DOWN);
		scene.world.showSection(beltAndStuff, Direction.SOUTH);
		scene.idle(10);

		ItemStack dirt = new ItemStack(Items.DIRT);
		ItemStack gravel = new ItemStack(Items.GRAVEL);
		ItemStack emerald = new ItemStack(Items.EMERALD);

		for (int i = 0; i < 14; i++) {

			if (i < 12)
				scene.world.createItemOnBelt(andesiteFunnel.below(), Direction.SOUTH,
					i % 3 == 0 ? dirt : i % 3 == 1 ? gravel : emerald);
			scene.idle(10);

			if (i > 0 && (i < 3 || i % 3 == 0)) {
				scene.world.removeItemsFromBelt(brassFunnel.below());
				scene.world.flapFunnel(brassFunnel, false);
			}

			scene.world.modifyEntities(ItemEntity.class, e -> {
				if (e.getY() < 1)
					e.discard();
			});

			if (i == 2) {
				scene.overlay.showFilterSlotInput(filter, Direction.NORTH, 40);
				scene.overlay.showControls(new InputWindowElement(filter, Pointing.DOWN).rightClick()
					.withItem(emerald), 60);
				scene.idle(10);
				scene.overlay.showText(80)
					.text("Using items on the filter slot will restrict the funnel to only transfer matching stacks.")
					.attachKeyFrame()
					.pointAt(filter)
					.placeNearTarget();
				scene.world.setFilterData(util.select.position(brassFunnel), FunnelBlockEntity.class, emerald);
			} else
				scene.idle(10);

			if (i == 8)
				scene.markAsFinished();
		}
	}

	public static void transposer(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("funnel_transfer", "Direct transfer");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		BlockPos funnelPos = util.grid.at(2, 2, 2);
		Selection funnelSelect = util.select.position(funnelPos);

		ElementLink<WorldSectionElement> rightChest =
			scene.world.showIndependentSection(util.select.position(0, 2, 2), Direction.DOWN);
		ElementLink<WorldSectionElement> leftChest =
			scene.world.showIndependentSection(util.select.position(4, 2, 2), Direction.DOWN);
		scene.world.moveSection(rightChest, util.vector.of(2, 1, 0), 0);
		scene.world.moveSection(leftChest, util.vector.of(-2, -1, 0), 0);
		scene.idle(5);

		scene.world.showSection(funnelSelect, Direction.DOWN);
		scene.idle(20);

		scene.overlay.showSelectionWithText(funnelSelect, 40)
			.colored(PonderPalette.RED)
			.text("Funnels cannot ever transfer between closed inventories directly.")
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(50);

		scene.world.hideSection(funnelSelect, Direction.SOUTH);
		scene.idle(20);

		scene.world.setBlocks(funnelSelect, AllBlocks.CHUTE.getDefaultState(), false);
		scene.world.showSection(funnelSelect, Direction.NORTH);
		scene.idle(10);

		scene.overlay.showText(40)
			.colored(PonderPalette.GREEN)
			.text("Chutes or Smart chutes might be more suitable for such purposes.")
			.attachKeyFrame()
			.pointAt(util.vector.centerOf(funnelPos))
			.placeNearTarget();
		scene.idle(50);

		scene.world.hideSection(funnelSelect, Direction.UP);
		scene.world.hideIndependentSection(leftChest, Direction.UP);
		scene.world.hideIndependentSection(rightChest, Direction.UP);
		scene.idle(20);

		Selection belt = util.select.layer(1);
		scene.world.setBlocks(funnelSelect, Blocks.AIR.defaultBlockState(), false);
		scene.world.showSection(belt, Direction.DOWN);
		scene.world.showSection(util.select.fromTo(0, 2, 2, 4, 2, 2), Direction.DOWN);
		scene.overlay.showText(120)
			.colored(PonderPalette.GREEN)
			.text("Same applies for horizontal movement. A mechanical belt should help here.")
			.pointAt(util.vector.topOf(1, 2, 2))
			.placeNearTarget();

		scene.markAsFinished();
	}

}
