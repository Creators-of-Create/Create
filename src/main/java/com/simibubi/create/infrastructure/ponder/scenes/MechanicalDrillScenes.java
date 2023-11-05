package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class MechanicalDrillScenes {

	public static void breaker(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("mechanical_drill", "Breaking Blocks with the Mechanical Drill");
		scene.configureBasePlate(0, 0, 5);

		scene.world().setKineticSpeed(util.select().layer(0), -8);
		scene.world().setKineticSpeed(util.select().layer(1), 16);

		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);
		scene.world().showSection(util.select().fromTo(4, 1, 2, 5, 1, 2), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(3, 1, 2), Direction.EAST);
		scene.idle(20);

		BlockPos breakingPos = util.grid().at(2, 1, 2);

		scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
		scene.idle(5);
		for (int i = 0; i < 10; i++) {
			scene.idle(10);
			scene.world().incrementBlockBreakingProgress(breakingPos);
			if (i == 1) {
				scene.overlay().showText(80)
					.attachKeyFrame()
					.placeNearTarget()
					.pointAt(util.vector().topOf(breakingPos))
					.text("When given Rotational Force, a Mechanical Drill will break blocks directly in front of it");
			}
		}

		scene.world().hideSection(util.select().position(breakingPos), Direction.UP);
		ElementLink<EntityElement> plankEntity = scene.world().createItemEntity(util.vector().centerOf(breakingPos),
																				util.vector().of(0, .1f, 0), new ItemStack(Items.OAK_PLANKS));
		scene.idle(20);
		scene.idle(15);

		scene.world().modifyEntity(plankEntity, Entity::discard);
		scene.world().modifyKineticSpeed(util.select().everywhere(), f -> 4 * f);
		scene.effects().rotationSpeedIndicator(breakingPos.east(3));
		scene.idle(5);
		scene.world().setBlock(breakingPos, Blocks.OAK_PLANKS.defaultBlockState(), false);
		scene.world().showSection(util.select().position(breakingPos), Direction.DOWN);

		scene.idle(5);
		for (int i = 0; i < 10; i++) {
			scene.idle(3);
			scene.world().incrementBlockBreakingProgress(breakingPos);
			if (i == 2) {
				scene.overlay().showText(80)
					.attachKeyFrame()
					.placeNearTarget()
					.pointAt(util.vector().topOf(breakingPos.east()))
					.text("Its mining speed depends on the Rotational Input");
			}
		}

		scene.world().createItemEntity(util.vector().centerOf(breakingPos), util.vector().of(0, .1f, 0),
									   new ItemStack(Items.OAK_PLANKS));
		scene.idle(50);
	}

	public static void contraption(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("mechanical_drill_contraption", "Using Mechanical Drills on Contraptions");
		scene.configureBasePlate(0, 0, 6);
		scene.world().showSection(util.select().layer(0), Direction.UP);

		Selection kinetics = util.select().fromTo(5, 1, 2, 5, 1, 6);

		scene.idle(5);
		ElementLink<WorldSectionElement> pistonHead =
			scene.world().showIndependentSection(util.select().fromTo(5, 1, 1, 7, 1, 1), Direction.DOWN);
		scene.world().moveSection(pistonHead, util.vector().of(0, 0, 1), 0);
		scene.world().showSection(kinetics, Direction.DOWN);
		scene.idle(5);
		ElementLink<WorldSectionElement> contraption =
			scene.world().showIndependentSection(util.select().fromTo(4, 2, 3, 4, 1, 2), Direction.DOWN);
		scene.idle(5);
		scene.world().showSectionAndMerge(util.select().position(3, 1, 3), Direction.EAST, contraption);
		scene.idle(5);
		scene.world().showSectionAndMerge(util.select().position(3, 1, 2), Direction.EAST, contraption);
		scene.world().showSectionAndMerge(util.select().position(3, 2, 3), Direction.EAST, contraption);
		scene.idle(5);
		scene.world().showSectionAndMerge(util.select().position(3, 2, 2), Direction.EAST, contraption);

		scene.overlay().showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().topOf(util.grid().at(3, 2, 3)))
			.text("Whenever Drills are moved as part of an animated Contraption...");
		scene.idle(70);

		Selection drills = util.select().fromTo(3, 1, 2, 3, 2, 3);

		Selection planks = util.select().fromTo(1, 1, 2, 1, 2, 3);
		scene.world().showSection(planks, Direction.DOWN);
		scene.world().setKineticSpeed(util.select().position(4, 0, 6), -8);
		scene.world().setKineticSpeed(kinetics, 16);
		scene.world().setKineticSpeed(drills, 16);
		scene.world().moveSection(pistonHead, util.vector().of(-1, 0, 0), 20);
		scene.world().moveSection(contraption, util.vector().of(-1, 0, 0), 20);
		scene.idle(20);
		scene.world().setKineticSpeed(drills, 64);

		BlockPos p1 = util.grid().at(1, 1, 2);
		BlockPos p2 = util.grid().at(1, 1, 3);
		BlockPos p3 = util.grid().at(1, 2, 2);
		BlockPos p4 = util.grid().at(1, 2, 3);

		for (int i = 0; i < 10; i++) {
			scene.idle(3);
			scene.world().incrementBlockBreakingProgress(p1);
			scene.world().incrementBlockBreakingProgress(p2);
			scene.world().incrementBlockBreakingProgress(p3);
			scene.world().incrementBlockBreakingProgress(p4);
			if (i == 2) {
				scene.overlay().showText(80)
					.placeNearTarget()
					.pointAt(util.vector().topOf(p3))
					.text("...they will break blocks the contraption runs them into");
			}
		}

		Vec3 m = util.vector().of(-.1, 0, 0);
		ItemStack item = new ItemStack(Items.OAK_PLANKS);
		scene.world().createItemEntity(util.vector().centerOf(p1), m, item);
		scene.world().createItemEntity(util.vector().centerOf(p2), m, item);
		scene.world().createItemEntity(util.vector().centerOf(p3), m, item);
		scene.world().createItemEntity(util.vector().centerOf(p4), m, item);

		scene.world().setKineticSpeed(drills, 16);
		scene.world().moveSection(pistonHead, util.vector().of(-1, 0, 0), 20);
		scene.world().moveSection(contraption, util.vector().of(-1, 0, 0), 20);
		scene.idle(20);
		scene.world().setKineticSpeed(drills, 0);
		scene.idle(20);

		scene.world().modifyKineticSpeed(util.select().everywhere(), f -> -f);
		scene.world().moveSection(pistonHead, util.vector().of(2, 0, 0), 40);
		scene.world().moveSection(contraption, util.vector().of(2, 0, 0), 40);
		scene.world().hideSection(planks, Direction.UP);
		scene.idle(40);

		scene.world().setBlocks(planks, Blocks.OAK_PLANKS.defaultBlockState(), false);
		scene.world().modifyEntities(ItemEntity.class, Entity::discard);
		scene.world().glueBlockOnto(util.grid().at(4, 3, 2), Direction.DOWN, contraption);

		scene.overlay().showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().blockSurface(util.grid().at(4, 3, 2), Direction.WEST))
			.sharedText("storage_on_contraption");
		scene.idle(70);

		scene.world().showSection(planks, Direction.DOWN);
		scene.world().modifyKineticSpeed(util.select().everywhere(), f -> -f);
		scene.world().setKineticSpeed(drills, 16);
		scene.world().moveSection(pistonHead, util.vector().of(-1, 0, 0), 20);
		scene.world().moveSection(contraption, util.vector().of(-1, 0, 0), 20);

		scene.idle(20);
		scene.world().setKineticSpeed(drills, 64);

		for (int i = 0; i < 10; i++) {
			scene.idle(3);
			scene.world().incrementBlockBreakingProgress(p1);
			scene.world().incrementBlockBreakingProgress(p2);
			scene.world().incrementBlockBreakingProgress(p3);
			scene.world().incrementBlockBreakingProgress(p4);
		}

		scene.world().setKineticSpeed(drills, 16);
		scene.world().moveSection(pistonHead, util.vector().of(-1, 0, 0), 20);
		scene.world().moveSection(contraption, util.vector().of(-1, 0, 0), 20);
		scene.idle(20);
		scene.world().setKineticSpeed(drills, 0);
		scene.idle(10);
		scene.overlay().showControls(util.vector().topOf(2, 3, 2), Pointing.DOWN, 60)
			.withItem(new ItemStack(Blocks.OAK_PLANKS));
		scene.idle(20);
	}

}
