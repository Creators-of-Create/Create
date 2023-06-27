package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class GantryScenes {

	public static void introForPinion(SceneBuilder scene, SceneBuildingUtil util) {
		intro(scene, util, true);
	}

	public static void introForShaft(SceneBuilder scene, SceneBuildingUtil util) {
		intro(scene, util, false);
	}

	private static void intro(SceneBuilder scene, SceneBuildingUtil util, boolean pinion) {
		String id = "gantry_" + (pinion ? "carriage" : "shaft");
		String title = "Using Gantry " + (pinion ? "Carriages" : "Shafts");
		scene.title(id, title);

		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -2 * f);
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.layer(1), Direction.DOWN);
		scene.idle(10);
		ElementLink<WorldSectionElement> gantry =
			scene.world.showIndependentSection(util.select.layer(2), Direction.DOWN);
		scene.idle(10);

		BlockPos centralShaft = util.grid.at(2, 1, 2);

		scene.world.moveSection(gantry, util.vector.of(-4, 0, 0), 60);

		String text = pinion ? "Gantry Carriages can mount to and slide along a Gantry Shaft."
			: "Gantry Shafts form the basis of a gantry setup. Attached Carriages will move along them.";

		scene.overlay.showText(80)
			.attachKeyFrame()
			.text(text)
			.pointAt(util.vector.centerOf(centralShaft));
		scene.idle(80);

		scene.world.hideIndependentSection(gantry, Direction.UP);
		scene.idle(10);
		gantry = scene.world.showIndependentSection(util.select.layer(2), Direction.DOWN);
		Vec3 gantryTop = util.vector.topOf(4, 2, 2);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> 0f);
		scene.overlay.showText(40)
			.attachKeyFrame()
			.text("Gantry setups can move attached Blocks.")
			.pointAt(gantryTop)
			.placeNearTarget();
		scene.idle(30);

		Selection planks = util.select.position(5, 3, 1);

		scene.world.showSectionAndMerge(util.select.layersFrom(3)
			.substract(planks), Direction.DOWN, gantry);
		scene.world.replaceBlocks(util.select.fromTo(5, 3, 2, 3, 4, 2), Blocks.OAK_PLANKS.defaultBlockState(), false);
		scene.idle(10);
		scene.world.showSectionAndMerge(planks, Direction.SOUTH, gantry);

		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.GREEN, "glue", util.select.position(3, 4, 2)
			.add(util.select.fromTo(3, 3, 2, 5, 3, 2))
			.add(util.select.position(5, 3, 1)), 40);
		scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(util.grid.at(3, 3, 2)), Pointing.UP)
			.withItem(AllItems.SUPER_GLUE.asStack()), 40);
		scene.effects.superGlue(util.grid.at(5, 3, 1), Direction.SOUTH, true);
		scene.idle(20);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.sharedText("movement_anchors")
			.pointAt(util.vector.blockSurface(util.grid.at(3, 3, 2), Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		scene.world.modifyKineticSpeed(util.select.layer(0), f -> 32f);
		scene.world.modifyKineticSpeed(util.select.layer(1), f -> -64f);

		scene.world.moveSection(gantry, util.vector.of(-4, 0, 0), 60);
		scene.idle(20);
		scene.markAsFinished();
	}

	public static void redstone(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("gantry_redstone", "Gantry Power Propagation");
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -f);

		Selection leverRedstone = util.select.fromTo(3, 1, 0, 3, 1, 1);
		Selection shaft = util.select.fromTo(0, 1, 2, 4, 1, 2);
		Selection shaftAndCog = util.select.fromTo(0, 1, 2, 5, 1, 2);

		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0)
			.add(leverRedstone), Direction.UP);

		scene.idle(10);
		scene.world.showSection(shaftAndCog, Direction.DOWN);
		scene.idle(10);

		BlockPos gantryPos = util.grid.at(4, 2, 2);
		ElementLink<WorldSectionElement> gantry =
			scene.world.showIndependentSection(util.select.position(gantryPos), Direction.DOWN);
		scene.idle(15);
		scene.world.moveSection(gantry, util.vector.of(-3, 0, 0), 40);
		scene.idle(40);

		scene.world.toggleRedstonePower(shaft);
		scene.world.toggleRedstonePower(util.select.position(3, 1, 0));
		scene.world.toggleRedstonePower(util.select.position(3, 1, 1));
		scene.effects.indicateRedstone(util.grid.at(3, 1, 0));
		scene.world.modifyKineticSpeed(util.select.position(gantryPos), f -> 32f);
		scene.idle(40);

		BlockPos cogPos = util.grid.at(1, 2, 1);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.pointAt(util.vector.centerOf(cogPos.below()
				.south()))
			.text("Redstone-powered gantry shafts stop moving their carriages")
			.placeNearTarget();
		scene.idle(70);

		Selection cogSelection = util.select.position(cogPos);
		scene.world.showSection(cogSelection, Direction.SOUTH);
		scene.world.modifyKineticSpeed(cogSelection, f -> 32f);
		scene.overlay.showText(180)
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(cogPos, Direction.NORTH))
			.text("Instead, its rotational force is relayed to the carriages' output shaft")
			.placeNearTarget();
		scene.idle(10);

		scene.effects.rotationSpeedIndicator(cogPos);
		scene.markAsFinished();
	}

	public static void direction(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("gantry_direction", "Gantry Movement Direction");
		scene.configureBasePlate(0, 0, 5);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -f);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(10);

		Selection shaftAndGearshiftAndLever = util.select.fromTo(0, 1, 2, 5, 2, 2);
		Selection shafts = util.select.fromTo(0, 1, 2, 3, 1, 2);

		scene.world.showSection(shaftAndGearshiftAndLever, Direction.DOWN);
		scene.overlay.showText(60)
			.text("Gantry Shafts can have opposite orientations")
			.pointAt(util.vector.of(2, 1.5, 2.5))
			.placeNearTarget();
		scene.idle(60);

		ElementLink<WorldSectionElement> gantry1 =
			scene.world.showIndependentSection(util.select.position(0, 1, 3), Direction.NORTH);
		ElementLink<WorldSectionElement> gantry2 =
			scene.world.showIndependentSection(util.select.position(3, 1, 3), Direction.NORTH);
		scene.idle(10);

		scene.world.moveSection(gantry1, util.vector.of(1, 0, 0), 20);
		scene.world.moveSection(gantry2, util.vector.of(-1, 0, 0), 20);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("The movement direction of carriages depend on their shafts' orientation")
			.pointAt(util.vector.topOf(1, 1, 3))
			.placeNearTarget();
		scene.idle(80);

		BlockPos lastShaft = util.grid.at(0, 1, 2);
		boolean flip = true;

		for (int i = 0; i < 3; i++) {
			scene.world.modifyBlocks(util.select.fromTo(4, 1, 2, 4, 2, 2), s -> s.cycle(BlockStateProperties.POWERED),
				false);
			scene.effects.indicateRedstone(util.grid.at(4, 2, 2));
			scene.world.moveSection(gantry1, util.vector.of(flip ? -1 : 1, 0, 0), 20);
			scene.world.moveSection(gantry2, util.vector.of(flip ? 1 : -1, 0, 0), 20);
			scene.world.modifyKineticSpeed(shafts, f -> -f);
			scene.effects.rotationDirectionIndicator(lastShaft.east(flip ? 1 : 0));
			scene.idle(20);

			if (i == 0) {
				scene.overlay.showText(80)
					.attachKeyFrame()
					.text("...as well as the rotation direction of the shaft")
					.pointAt(util.vector.blockSurface(lastShaft, Direction.WEST))
					.placeNearTarget();
			}

			scene.idle(30);
			flip = !flip;
		}

		Selection kinetics = util.select.fromTo(0, 2, 3, 3, 3, 3);
		Selection gears1 = util.select.fromTo(0, 1, 3, 0, 3, 3);
		Selection gears2 = util.select.fromTo(3, 1, 3, 3, 3, 3);

		scene.world.showSection(kinetics, Direction.DOWN);
		scene.world.showSection(util.select.fromTo(0, 1, 0, 4, 1, 1), Direction.SOUTH);
		scene.idle(20);

		BlockPos leverPos = util.grid.at(4, 1, 0);
		scene.world.modifyBlocks(util.select.fromTo(1, 1, 0, 3, 1, 1),
			s -> s.hasProperty(RedStoneWireBlock.POWER) ? s.setValue(RedStoneWireBlock.POWER, 15) : s, false);
		scene.world.toggleRedstonePower(util.select.position(leverPos));
		scene.world.toggleRedstonePower(shafts);
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(gears1, f -> -32f);
		scene.world.modifyKineticSpeed(gears2, f -> 32f);

		scene.idle(20);
		scene.overlay.showText(120)
			.attachKeyFrame()
			.text("Same rules apply for the propagated rotation")
			.pointAt(util.vector.topOf(0, 3, 3))
			.placeNearTarget();
		scene.idle(20);

		for (boolean flip2 : Iterate.trueAndFalse) {
			scene.effects.rotationDirectionIndicator(util.grid.at(0, 3, 3));
			scene.effects.rotationDirectionIndicator(util.grid.at(3, 3, 3));

			scene.idle(60);
			scene.world.modifyBlocks(util.select.fromTo(4, 1, 2, 4, 2, 2), s -> s.cycle(BlockStateProperties.POWERED),
				false);
			scene.effects.indicateRedstone(util.grid.at(4, 2, 2));
			scene.world.modifyKineticSpeed(gears1, f -> -f);
			scene.world.modifyKineticSpeed(gears2, f -> -f);

			if (!flip2) {
				scene.effects.rotationDirectionIndicator(util.grid.at(0, 3, 3));
				scene.effects.rotationDirectionIndicator(util.grid.at(3, 3, 3));
				scene.markAsFinished();
			}
		}

	}

	public static void subgantry(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("gantry_cascaded", "Cascaded Gantries");
		scene.configureBasePlate(0, 0, 5);
		scene.setSceneOffsetY(-1);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -2 * f);
		scene.world.showSection(util.select.layer(0)
			.add(util.select.column(5, 3))
			.add(util.select.fromTo(2, 1, 3, 4, 1, 3)), Direction.UP);
		scene.idle(10);

		BlockPos gantryPos = util.grid.at(5, 1, 2);
		BlockPos gantryPos2 = util.grid.at(3, 2, 2);
		ElementLink<WorldSectionElement> gantry =
			scene.world.showIndependentSection(util.select.position(gantryPos), Direction.SOUTH);
		scene.idle(5);

		scene.world.showSectionAndMerge(util.select.fromTo(0, 1, 2, 4, 1, 2), Direction.EAST, gantry);
		scene.idle(15);

		scene.world.moveSection(gantry, util.vector.of(0, 2, 0), 40);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("Gantry shafts attach to a carriage without the need of super glue")
			.independent(20);
		scene.idle(40);

		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> -f);
		scene.world.moveSection(gantry, util.vector.of(0, -2, 0), 40);
		scene.idle(40);

		ElementLink<WorldSectionElement> secondGantry =
			scene.world.showIndependentSection(util.select.position(gantryPos2), Direction.DOWN);
		scene.idle(15);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("Same applies for carriages on moved Gantry Shafts")
			.independent(20);
		scene.idle(15);

		scene.world.moveSection(gantry, util.vector.of(0, 2, 0), 40);
		scene.world.moveSection(secondGantry, util.vector.of(0, 2, 0), 40);

		scene.idle(40);
		BlockPos leverPos = util.grid.at(2, 1, 3);
		scene.world.toggleRedstonePower(util.select.position(leverPos));
		scene.world.toggleRedstonePower(util.select.fromTo(3, 1, 3, 4, 1, 3));
		scene.world.toggleRedstonePower(util.select.fromTo(5, 1, 3, 5, 4, 3));
		scene.world.modifyKineticSpeed(util.select.fromTo(0, 1, 2, 5, 1, 2), f -> -32f);
		scene.effects.indicateRedstone(leverPos);
		scene.world.moveSection(secondGantry, util.vector.of(-3, 0, 0), 60);

		scene.idle(20);
		scene.overlay.showText(120)
			.text("Thus, a gantry system can be cascaded to cover multiple axes of movement")
			.independent(20);
	}

}
