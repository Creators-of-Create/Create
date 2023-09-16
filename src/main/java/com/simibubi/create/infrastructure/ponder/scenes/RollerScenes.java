package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.content.contraptions.actors.roller.RollerBlockEntity;
import com.simibubi.create.content.trains.station.StationBlock;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.ElementLink;
import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.element.ParrotElement;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class RollerScenes {

	public static void clearAndPave(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("mechanical_roller_pave", "Clearing and Paving with the Roller");
		scene.configureBasePlate(0, 0, 9);
		scene.scaleSceneView(.75f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos stationPos = util.grid().at(7, 1, 1);
		Selection gantryPole = util.select().fromTo(9, 5, 4, 1, 5, 4);
		Selection cogs = util.select().fromTo(9, 0, 4, 9, 4, 4);
		Selection gantryCar = util.select().fromTo(7, 2, 8, 7, 4, 8);
		BlockPos bogeyPos = util.grid().at(7, 2, 4);
		Selection someRubble = util.select().fromTo(2, 1, 3, 3, 2, 5)
			.substract(util.select().fromTo(3, 1, 4, 2, 1, 4));
		Selection chest = util.select().fromTo(7, 2, 5, 8, 3, 5);
		Selection rollers = util.select().fromTo(6, 2, 3, 6, 2, 5);
		Selection train = util.select().fromTo(8, 3, 4, 7, 2, 4);
		BlockPos controlsPos = util.grid().at(7, 3, 4);

		for (int i = 8; i >= 0; i--) {
			scene.world().showSection(util.select().position(i, 1, 4), Direction.DOWN);
			scene.idle(1);
		}

		scene.special().movePointOfInterest(util.grid().at(0, 3, 4));
		scene.idle(5);
		scene.world().showSection(util.select().position(stationPos), Direction.DOWN);
		scene.idle(5);
		ElementLink<ParrotElement> birbLink =
			scene.special().createBirb(util.vector().centerOf(8, 3, 4), ParrotElement.FacePointOfInterestPose::new);
		ElementLink<WorldSectionElement> trainLink = scene.world().showIndependentSection(train, Direction.DOWN);
		scene.idle(5);
		scene.world().showSectionAndMerge(rollers, Direction.EAST, trainLink);
		scene.idle(15);
		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.world().animateTrainStation(stationPos, true);

		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(util.grid().at(6, 2, 4)))
			.attachKeyFrame()
			.text("Mechanical rollers help to clean up terrain around tracks or paths")
			.placeNearTarget();
		scene.idle(70);

		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.world().animateTrainStation(stationPos, false);
		scene.world().showSection(someRubble, Direction.DOWN);
		scene.world().toggleControls(controlsPos);

		scene.world().moveSection(trainLink, util.vector().of(-1.5, 0, 0), 30);
		scene.special().moveParrot(birbLink, util.vector().of(-1.5, 0, 0), 30);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(-100));
		scene.world().animateBogey(bogeyPos, 1.5f, 30);
		scene.idle(30);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(0));

		scene.overlay().showControls(new InputWindowElement(util.vector().topOf(util.grid().at(2, 2, 4)), Pointing.DOWN)
			.showing(AllIcons.I_ROLLER_PAVE), 70);

		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(util.grid().at(2, 2, 4)))
			.attachKeyFrame()
			.text("In its default mode, without a material set, it will simply clear blocks like a Drill")
			.placeNearTarget();

		for (int i = 0; i < 10; i++) {
			scene.idle(3);
			scene.world().incrementBlockBreakingProgress(util.grid().at(3, 1, 5));
			scene.world().incrementBlockBreakingProgress(util.grid().at(3, 1, 3));
			scene.world().incrementBlockBreakingProgress(util.grid().at(3, 2, 5));
		}

		scene.world().moveSection(trainLink, util.vector().of(-1, 0, 0), 20);
		scene.special().moveParrot(birbLink, util.vector().of(-1, 0, 0), 20);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(-100));
		scene.world().animateBogey(bogeyPos, 1f, 20);
		scene.idle(20);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(0));

		for (int i = 0; i < 10; i++) {
			scene.idle(3);
			scene.world().incrementBlockBreakingProgress(util.grid().at(2, 2, 4));
			scene.world().incrementBlockBreakingProgress(util.grid().at(2, 1, 3));
		}

		scene.world().moveSection(trainLink, util.vector().of(-2, 0, 0), 40);
		scene.special().moveParrot(birbLink, util.vector().of(-2, 0, 0), 40);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(-100));
		scene.world().animateBogey(bogeyPos, 2f, 40);
		scene.idle(40);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(0));

		scene.special().hideElement(birbLink, Direction.UP);
		scene.world().hideIndependentSection(trainLink, Direction.UP);
		scene.idle(15);

		birbLink = scene.special().createBirb(util.vector().centerOf(8, 3, 4), ParrotElement.FacePointOfInterestPose::new);
		trainLink = scene.world().showIndependentSection(train, Direction.DOWN);
		scene.world().toggleControls(controlsPos);
		scene.idle(5);
		scene.world().showSectionAndMerge(rollers, Direction.EAST, trainLink);
		scene.idle(15);
		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.world().animateTrainStation(stationPos, true);
		scene.world().hideSection(someRubble, Direction.DOWN);

		Vec3 filterSlot = util.vector().of(6.75 - 1 / 16f, 3, 3.25 + 1 / 16f);
		scene.overlay().showFilterSlotInput(filterSlot, Direction.UP, 60);
		scene.overlay().showText(60)
			.pointAt(filterSlot.add(-.125, 0, 0))
			.attachKeyFrame()
			.text("While disassembled, a suitable paving material can be specified")
			.placeNearTarget();
		scene.idle(50);

		Block paveMaterial = Blocks.TUFF;
		ItemStack paveItem = new ItemStack(paveMaterial);
		scene.overlay().showControls(new InputWindowElement(filterSlot, Pointing.DOWN).withItem(paveItem), 40);
		scene.idle(7);
		scene.world().setFilterData(rollers, RollerBlockEntity.class, paveItem);
		scene.idle(20);

		scene.world().showSectionAndMerge(chest, Direction.DOWN, trainLink);
		scene.idle(15);
		scene.overlay().showText(70)
			.pointAt(util.vector().topOf(util.grid().at(7, 3, 5)))
			.text("Materials can be supplied via chests or barrels attached to the structure")
			.placeNearTarget();
		scene.idle(60);

		scene.world().restoreBlocks(someRubble);
		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.world().animateTrainStation(stationPos, false);
		scene.world().showSection(someRubble, Direction.DOWN);
		scene.world().toggleControls(controlsPos);
		scene.world().showSection(someRubble, Direction.DOWN);

		scene.world().moveSection(trainLink, util.vector().of(-1.5, 0, 0), 30);
		scene.special().moveParrot(birbLink, util.vector().of(-1.5, 0, 0), 30);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(-100));
		scene.world().animateBogey(bogeyPos, 1.5f, 30);

		scene.world().replaceBlocks(util.select().fromTo(5, 0, 3, 5, 0, 5), paveMaterial.defaultBlockState(), true);
		scene.idle(20);
		scene.world().replaceBlocks(util.select().fromTo(4, 0, 3, 4, 0, 5), paveMaterial.defaultBlockState(), true);
		scene.idle(10);

		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(0));

		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(util.grid().at(5, 0, 3)))
			.attachKeyFrame()
			.text("In addition to breaking blocks, it will now replace the layer beneath them")
			.placeNearTarget();

		for (int i = 0; i < 10; i++) {
			scene.idle(3);
			scene.world().incrementBlockBreakingProgress(util.grid().at(3, 1, 5));
			scene.world().incrementBlockBreakingProgress(util.grid().at(3, 1, 3));
			scene.world().incrementBlockBreakingProgress(util.grid().at(3, 2, 5));
		}

		scene.world().moveSection(trainLink, util.vector().of(-1, 0, 0), 20);
		scene.special().moveParrot(birbLink, util.vector().of(-1, 0, 0), 20);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(-100));
		scene.world().animateBogey(bogeyPos, 1f, 20);
		scene.idle(10);
		scene.world().replaceBlocks(util.select().fromTo(3, 0, 3, 3, 0, 5), paveMaterial.defaultBlockState(), true);
		scene.idle(10);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(0));

		for (int i = 0; i < 10; i++) {
			scene.idle(3);
			scene.world().incrementBlockBreakingProgress(util.grid().at(2, 2, 4));
			scene.world().incrementBlockBreakingProgress(util.grid().at(2, 1, 3));
		}

		scene.world().moveSection(trainLink, util.vector().of(-3, 0, 0), 60);
		scene.special().moveParrot(birbLink, util.vector().of(-3, 0, 0), 60);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(-100));
		scene.world().animateBogey(bogeyPos, 3f, 60);
		scene.idle(10);
		scene.world().replaceBlocks(util.select().fromTo(2, 0, 3, 2, 0, 5), paveMaterial.defaultBlockState(), true);
		scene.idle(20);
		scene.world().replaceBlocks(util.select().fromTo(1, 0, 3, 1, 0, 5), paveMaterial.defaultBlockState(), true);
		scene.idle(20);
		scene.world().replaceBlocks(util.select().fromTo(0, 0, 3, 0, 0, 5), paveMaterial.defaultBlockState(), true);
		scene.idle(10);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(0));

		scene.special().hideElement(birbLink, Direction.UP);
		scene.world().hideIndependentSection(trainLink, Direction.UP);
		scene.idle(5);
		scene.world().hideSection(util.select().fromTo(8, 1, 4, 0, 1, 4), Direction.SOUTH);
		scene.world().hideSection(util.select().position(stationPos), Direction.UP);
		scene.idle(10);

		scene.overlay().showSelectionWithText(util.select().fromTo(5, 0, 3, 0, 0, 5), 90)
			.pointAt(util.vector().topOf(util.grid().at(3, 0, 4)))
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.text("Note that any block destroyed by a roller has a chance not to yield drops")
			.placeNearTarget();
		scene.idle(100);

		scene.world().showSection(gantryPole, Direction.DOWN);
		scene.world().showSection(cogs, Direction.WEST);
		scene.idle(10);
		ElementLink<WorldSectionElement> gantryLink = scene.world().showIndependentSection(gantryCar, Direction.UP);
		scene.world().moveSection(gantryLink, util.vector().of(0, 0, -4), 0);
		scene.idle(10);
		ElementLink<WorldSectionElement> gantryLink2 = scene.world().showIndependentSection(rollers, Direction.EAST);
		scene.idle(5);
		ElementLink<WorldSectionElement> gantryLink3 = scene.world().showIndependentSection(chest, Direction.SOUTH);
		scene.world().moveSection(gantryLink3, util.vector().of(0, 0, -2), 0);
		scene.idle(15);

		paveMaterial = Blocks.GRASS_BLOCK;
		paveItem = new ItemStack(paveMaterial);
		scene.overlay().showControls(new InputWindowElement(filterSlot, Pointing.DOWN).withItem(paveItem), 40);
		scene.idle(7);
		scene.world().setFilterData(rollers, RollerBlockEntity.class, paveItem);
		scene.idle(20);

		scene.overlay().showText(110)
			.independent()
			.attachKeyFrame()
			.text(
				"Rollers are especially useful on Trains, but can also be used on most other types of moving contraptions");
		scene.idle(20);

		scene.world().moveSection(gantryLink, util.vector().of(-5.5, 0, 0), 110);
		scene.world().moveSection(gantryLink2, util.vector().of(-5.5, 0, 0), 110);
		scene.world().moveSection(gantryLink3, util.vector().of(-5.5, 0, 0), 110);
		scene.world().setKineticSpeed(gantryPole, 48);

		for (int i = 0; i < 5; i++)
			scene.world().setKineticSpeed(util.select().position(9, i, 4), i % 2 == 0 ? -48 : 48);
		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(-100));
		for (int i = 0; i < 5; i++) {
			scene.world().replaceBlocks(util.select().fromTo(5 - i, 0, 3, 5 - i, 0, 5), paveMaterial.defaultBlockState(),
										true);
			scene.idle(20);
		}
		scene.world().replaceBlocks(util.select().fromTo(0, 0, 3, 0, 0, 5), paveMaterial.defaultBlockState(), true);
		scene.idle(10);

		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 2, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(0));
		scene.world().setKineticSpeed(util.select().everywhere(), 0);

	}

	public static void fill(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("mechanical_roller_fill", "Filling terrain with the Roller");
		scene.configureBasePlate(0, 0, 9);
		scene.scaleSceneView(.625f);
		scene.setSceneOffsetY(-3);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos stationPos = util.grid().at(7, 5, 1);
		BlockPos controlsPos = util.grid().at(7, 7, 4);
		BlockPos bogeyPos = util.grid().at(7, 6, 4);
		Selection train = util.select().fromTo(8, 6, 4, 7, 7, 5);
		Selection rollers = util.select().fromTo(6, 6, 3, 6, 6, 5);

		scene.special().movePointOfInterest(util.grid().at(0, 7, 4));

		for (int i = 8; i >= 0; i--) {
			scene.world().showSection(util.select().fromTo(i, 4, 3, i, 5, 5), Direction.DOWN);
			scene.idle(1);
		}

		// 1

		scene.idle(5);
		scene.world().showSection(util.select().fromTo(8, 1, 2, 6, 4, 2), Direction.DOWN);
		scene.world().showSection(util.select().fromTo(8, 1, 6, 6, 4, 6), Direction.DOWN);
		scene.idle(5);

		scene.world().showSection(util.select().fromTo(7, 4, 1, 7, 5, 1), Direction.SOUTH);
		scene.idle(5);

		ElementLink<WorldSectionElement> trainLink = scene.world().showIndependentSection(train, Direction.DOWN);
		ElementLink<ParrotElement> birbLink =
			scene.special().createBirb(util.vector().centerOf(8, 7, 4), ParrotElement.FacePointOfInterestPose::new);
		scene.idle(5);
		scene.world().showSectionAndMerge(rollers, Direction.EAST, trainLink);
		scene.idle(15);

		Vec3 filterSlot = util.vector().of(6.75 - 1 / 16f, 7, 3.75 - 1 / 16f);
		scene.overlay().showFilterSlotInput(filterSlot, Direction.UP, 60);
		scene.overlay().showText(60)
			.pointAt(filterSlot.add(-.125, 0, 0))
			.attachKeyFrame()
			.text("While disassembled, rollers can be set to other modes")
			.placeNearTarget();
		scene.idle(70);

		scene.overlay().showSelectionWithText(util.select().fromTo(5, 3, 3, 0, 1, 5), 90)
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.text("The 'fill' modes can help to bridge gaps between pavement and terrain")
			.placeNearTarget();
		scene.idle(100);

		scene.overlay().showControls(new InputWindowElement(filterSlot, Pointing.DOWN).showing(AllIcons.I_ROLLER_FILL),
									 50);
		scene.idle(15);
		Block paveMaterial = Blocks.COARSE_DIRT;
		ItemStack paveItem = new ItemStack(paveMaterial);
		scene.overlay()
			.showControls(new InputWindowElement(filterSlot.add(0, 0, -6 / 16f), Pointing.UP).withItem(paveItem), 35);
		scene.idle(7);
		scene.world().setFilterData(rollers, RollerBlockEntity.class, paveItem);
		scene.idle(10);
		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.world().animateTrainStation(stationPos, false);
		scene.world().toggleControls(controlsPos);
		scene.idle(20);

		scene.world().moveSection(trainLink, util.vector().of(-5.5, 0, 0), 110);
		scene.special().moveParrot(birbLink, util.vector().of(-5.5, 0, 0), 110);
		scene.world().animateBogey(bogeyPos, 5.5f, 110);

		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 6, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(-100));

		for (int j = 0; j < 5; j++) {
			for (int i = 0; i < 3; i++) {
				scene.world().showSection(util.select().fromTo(5 - j, 3 - i, 3, 5 - j, 3 - i, 5), null);
				scene.idle(2);
			}

			if (j == 2)
				scene.overlay().showText(90)
					.attachKeyFrame()
					.pointAt(util.vector().blockSurface(util.grid().at(3, 2, 3), Direction.NORTH))
					.text("On 'straight fill', they will place simple columns down to the surface")
					.placeNearTarget();

			scene.idle(14);
		}

		for (int i = 0; i < 3; i++) {
			scene.world().showSection(util.select().fromTo(0, 3 - i, 3, 0, 3 - i, 5), null);
			scene.idle(2);
		}

		scene.idle(4);

		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 6, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(0));

		scene.idle(15);
		scene.world().hideSection(util.select().fromTo(5, 1, 3, 0, 3, 5), Direction.SOUTH);
		scene.world().hideIndependentSection(trainLink, Direction.UP);
		scene.special().hideElement(birbLink, Direction.UP);
		scene.idle(15);

		scene.world().toggleControls(controlsPos);
		scene.idle(15);

		// 2

		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.world().animateTrainStation(stationPos, true);
		birbLink = scene.special().createBirb(util.vector().centerOf(8, 7, 4), ParrotElement.FacePointOfInterestPose::new);
		trainLink = scene.world().showIndependentSection(train, Direction.DOWN);
		scene.idle(5);
		scene.world().showSectionAndMerge(rollers, Direction.EAST, trainLink);
		scene.idle(25);

		scene.overlay()
			.showControls(new InputWindowElement(filterSlot, Pointing.DOWN).showing(AllIcons.I_ROLLER_WIDE_FILL), 40);
		scene.idle(45);

		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.world().animateTrainStation(stationPos, false);
		scene.world().toggleControls(controlsPos);
		scene.idle(20);

		scene.world().moveSection(trainLink, util.vector().of(-5.5, 0, 0), 110);
		scene.special().moveParrot(birbLink, util.vector().of(-5.5, 0, 0), 110);
		scene.world().animateBogey(bogeyPos, 5.5f, 110);

		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 6, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(-100));

		for (int j = 0; j < 5; j++) {
			for (int i = 0; i < 3; i++) {
				scene.world().showSection(util.select().fromTo(5 - j, 3 - i, 1, 5 - j, 3 - i, 7), null);
				scene.idle(2);
			}

			if (j == 2)
				scene.overlay().showText(90)
					.attachKeyFrame()
					.pointAt(util.vector().blockSurface(util.grid().at(3, 2, 3), Direction.NORTH))
					.text("On 'sloped fill', layers placed further down will increase in size")
					.placeNearTarget();

			scene.idle(14);
		}

		for (int i = 0; i < 3; i++) {
			scene.world().showSection(util.select().fromTo(0, 3 - i, 1, 0, 3 - i, 7), null);
			scene.idle(2);
		}

		scene.idle(4);

		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 6, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(0));

		scene.idle(15);
		scene.world().hideSection(util.select().fromTo(5, 1, 1, 0, 3, 7), Direction.SOUTH);
		scene.world().hideIndependentSection(trainLink, Direction.UP);
		scene.special().hideElement(birbLink, Direction.UP);
		scene.idle(15);

		scene.world().toggleControls(controlsPos);
		scene.world().replaceBlocks(util.select().fromTo(5, 1, 3, 0, 3, 5), Blocks.COBBLESTONE.defaultBlockState(), false);
		scene.idle(15);

		// 3

		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.world().animateTrainStation(stationPos, true);
		scene.world().showSection(util.select().fromTo(5, 1, 3, 0, 3, 5), Direction.NORTH);
		birbLink = scene.special().createBirb(util.vector().centerOf(8, 7, 4), ParrotElement.FacePointOfInterestPose::new);
		trainLink = scene.world().showIndependentSection(train, Direction.DOWN);
		scene.idle(5);
		scene.world().showSectionAndMerge(rollers, Direction.EAST, trainLink);
		scene.idle(25);

		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.world().animateTrainStation(stationPos, false);
		scene.world().toggleControls(controlsPos);
		scene.idle(20);

		scene.world().moveSection(trainLink, util.vector().of(-5.5, 0, 0), 110);
		scene.special().moveParrot(birbLink, util.vector().of(-5.5, 0, 0), 110);
		scene.world().animateBogey(bogeyPos, 5.5f, 110);

		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 6, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(-100));

		scene.overlay().showText(110)
			.attachKeyFrame()
			.independent()
			.text(
				"As opposed to 'clear & pave', neither of these modes will cause the rollers to break existing blocks")
			.placeNearTarget();

		for (int j = 0; j < 5; j++) {
			for (int i = 0; i < 3; i++) {
				scene.world().showSection(util.select().fromTo(5 - j, 3 - i, 1, 5 - j, 3 - i, 7), null);
				scene.idle(2);
			}

			scene.idle(14);
		}

		for (int i = 0; i < 3; i++) {
			scene.world().showSection(util.select().fromTo(0, 3 - i, 1, 0, 3 - i, 7), null);
			scene.idle(2);
		}

		scene.rotateCameraY(-30);

		scene.idle(4);

		for (int i = 0; i < 3; i++)
			scene.world().modifyBlockEntity(util.grid().at(6, 6, 3 + i), RollerBlockEntity.class,
				rte -> rte.setAnimatedSpeed(0));

	}

}
