package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
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
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FunnelScenes {

	public static void intro(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Using funnels");
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
		Selection exitFunnel = util.select.position(exitBeltPos.up());

		for (int i = 0; i < 8; i++) {
			scene.idle(8);
			scene.world.removeItemsFromBelt(exitBeltPos);
			scene.world.flapFunnels(exitFunnel, false);
			if (i == 2)
				scene.rotateCameraY(70);
			if (i < 6)
				scene.world.createItemOnBelt(entryBeltPos, Direction.EAST, itemStack);
		}

		scene.rotateCameraY(-70);
		scene.idle(10);

		Selection outputFunnel = util.select.position(1, 2, 4);
		scene.world.setBlocks(outputFunnel, Blocks.AIR.getDefaultState(), false);
		scene.world.setBlocks(util.select.fromTo(2, -1, 4, 2, 0, 4), AllBlocks.ANDESITE_CASING.getDefaultState(), true);
		ElementLink<WorldSectionElement> independentSection =
			scene.world.showIndependentSection(verticalFunnel, Direction.UP);

		Vec3d topItemSpawn = util.vector.centerOf(2, 6, 4);
		Vec3d sideItemSpawn = util.vector.centerOf(1, 3, 4)
			.add(0.15f, -0.45f, 0);
		ElementLink<EntityElement> lastItemEntity = null;

		for (int i = 0; i < 4; i++) {
			if (lastItemEntity != null)
				scene.world.modifyEntity(lastItemEntity, Entity::remove);
			if (i < 3)
				lastItemEntity = scene.world.createItemEntity(topItemSpawn, util.vector.of(0, -0.4, 0), itemStack);
			scene.idle(8);
		}

		scene.world.moveSection(independentSection, util.vector.of(0, 1, 0), 15);
		scene.idle(10);
		scene.world.setBlocks(outputFunnel, AllBlocks.ANDESITE_FUNNEL.getDefaultState()
			.with(FunnelBlock.FACING, Direction.WEST)
			.with(FunnelBlock.EXTRACTING, true), false);

		for (int i = 0; i < 3; i++) {
			scene.idle(8);
			scene.world.flapFunnels(outputFunnel, false);
			scene.world.createItemEntity(sideItemSpawn, util.vector.of(-.05, 0, 0), itemStack);
		}

		scene.idle(8);
		scene.overlay.showText(PonderPalette.WHITE, 0, "funnels_transfer",
			"Funnels are ideal for transferring items from and to inventories.", 360);
		scene.markAsFinished();
	}

	public static void directionality(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Direction of Transfer");
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
		Vec3d topCenter = util.vector.centerOf(topFunnel);
		Vec3d topSide = util.vector.blockSurface(topFunnel, Direction.EAST);

		InputWindowElement controlsSneak = new InputWindowElement(topCenter, Pointing.DOWN).rightClick()
			.whileSneaking();

		// Placing funnels without sneak
		scene.world.showSection(topFunnelSelection, Direction.DOWN);
		scene.overlay.showTargetedTextNearScene(PonderPalette.WHITE, topCenter, "regular_place",
			"Placed normally, it pull items from the inventory.", 80);
		scene.idle(45);

		ElementLink<EntityElement> itemLink =
			scene.world.createItemEntity(topCenter, util.vector.of(0, 4 / 16f, 0), itemStack);
		scene.idle(40);

		scene.world.modifyEntity(itemLink, Entity::remove);
		scene.world.hideSection(topFunnelSelection, Direction.UP);
		scene.idle(20);

		// Placing funnels with sneak
		scene.world.modifyBlock(topFunnel, s -> s.with(FunnelBlock.EXTRACTING, false), false);
		scene.idle(5);

		scene.world.showSection(topFunnelSelection, Direction.DOWN);
		scene.overlay.showControls(controlsSneak, 35);
		scene.overlay.showTargetedTextNearScene(PonderPalette.WHITE, topCenter, "sneak_place",
			"Placed while sneaking, it will put items into the inventory.", 80);
		scene.idle(45);

		itemLink = scene.world.createItemEntity(topCenter.add(0, 3, 0), util.vector.of(0, -0.2, 0), itemStack);
		scene.idle(10);

		scene.world.modifyEntity(itemLink, Entity::remove);
		scene.idle(45);

		// Wrench interaction
		InputWindowElement wrenchControls = new InputWindowElement(topSide, Pointing.RIGHT).rightClick()
			.withWrench();
		scene.overlay.showControls(wrenchControls, 40);
		scene.idle(10);
		scene.world.modifyBlock(topFunnel, s -> s.cycle(FunnelBlock.EXTRACTING), true);
		scene.idle(10);
		scene.overlay.showTargetedTextNearScene(PonderPalette.WHITE, topCenter, "wrench_reverse",
			"Using a wrench, the funnel can be flipped after placement.", 80);

		itemLink = scene.world.createItemEntity(topCenter, util.vector.of(0, 4 / 16f, 0), itemStack);
		scene.idle(30);

		scene.overlay.showControls(wrenchControls, 40);
		scene.idle(10);
		scene.world.modifyBlock(topFunnel, s -> s.cycle(FunnelBlock.EXTRACTING), true);
		scene.idle(10);
		scene.world.modifyEntity(itemLink, Entity::remove);

		scene.idle(20);

		// Side funnel
		BlockPos sideFunnel = util.grid.at(3, 2, 1);
		Selection sideFunnelSelection = util.select.fromTo(sideFunnel.down(), sideFunnel);
		Vec3d sideCenter = util.vector.centerOf(sideFunnel);

		scene.world.modifyBlock(sideFunnel, s -> s.cycle(FunnelBlock.EXTRACTING), false);
		scene.world.showSection(sideFunnelSelection, Direction.DOWN);
		scene.overlay.showTargetedTextNearScene(PonderPalette.WHITE, sideCenter, "same_for_other",
			"Same rules will apply for most orientations.", 70);

		scene.idle(20);

		scene.world.flapFunnels(sideFunnelSelection, true);
		itemLink = scene.world.createItemEntity(sideCenter.subtract(0, .45, 0), util.vector.of(0, 0, -0.1), itemStack);
		scene.idle(60);
		scene.world.hideSection(sideFunnelSelection, Direction.UP);
		scene.world.hideSection(topFunnelSelection, Direction.UP);
		scene.world.modifyEntity(itemLink, Entity::remove);
		scene.idle(20);

		// Belt funnel
		Selection beltFunnelSetup = util.select.fromTo(0, 1, 0, 2, 2, 5);
		Selection gearshiftAndLever = util.select.fromTo(1, 1, 4, 1, 2, 4);
		Selection gearshiftedKinetics = util.select.fromTo(1, 1, 2, 2, 1, 4);
		Vec3d topOfBeltFunnel = util.vector.topOf(2, 2, 2);
		BlockPos beltPos = util.grid.at(2, 1, 2);
		BlockPos cogPos = util.grid.at(1, 1, 3);

		scene.world.showSection(beltFunnelSetup, Direction.DOWN);
		scene.overlay.showTargetedText(PonderPalette.WHITE, topOfBeltFunnel, "belt_funnel",
			"Funnels on belts will extract/insert depending on its movement direction.", 140);
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
			scene.world.flapFunnels(beltFunnelSetup, false);

			if (i == 0) {
				scene.idle(50);
				scene.world.modifyBlocks(gearshiftAndLever, s -> s.cycle(BlockStateProperties.POWERED), false);
				scene.world.modifyKineticSpeed(gearshiftedKinetics, f -> -f);
				scene.effects.indicateRedstone(util.grid.at(1, 2, 4));
			}
		}
	}

	public static void mounting(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Funnel compatibility");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

	}

}
