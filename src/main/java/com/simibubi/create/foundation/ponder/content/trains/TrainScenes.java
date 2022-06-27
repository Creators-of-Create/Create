package com.simibubi.create.foundation.ponder.content.trains;

import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationBlock;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class TrainScenes {

	public static void controls(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("train_controls", "Controlling Trains");
		scene.configureBasePlate(1, 0, 9);
		scene.scaleSceneView(.75f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();

		for (int i = 10; i >= 0; i--) {
			scene.world.showSection(util.select.position(i, 1, 4), Direction.DOWN);
			scene.idle(1);
		}

		BlockPos stationPos = util.grid.at(4, 1, 1);
		Selection station = util.select.position(stationPos);
		Selection whistle = util.select.fromTo(4, 3, 4, 4, 4, 4);
		Selection train = util.select.fromTo(5, 2, 3, 1, 3, 5)
			.substract(whistle);

		scene.world.showSection(station, Direction.DOWN);
		scene.idle(20);

		ElementLink<WorldSectionElement> trainElement = scene.world.showIndependentSection(train, Direction.DOWN);
		scene.world.cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.idle(15);

		BlockPos initialControlsPos = util.grid.at(3, 3, 4);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, train,
			new AABB(initialControlsPos).contract(-6 / 16f, 2 / 16f, 0), 85);
		scene.idle(15);

		scene.overlay.showText(70)
			.pointAt(util.vector.of(3.35f, 3.75f, 5))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Train Controls are required on every train contraption");
		scene.idle(60);

		scene.world.cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.effects.indicateSuccess(stationPos);
		scene.world.animateTrainStation(stationPos, true);
		scene.world.toggleControls(initialControlsPos);
		scene.idle(20);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.topOf(initialControlsPos), Pointing.DOWN).rightClick(), 70);
		scene.idle(20);

		scene.overlay.showText(60)
			.pointAt(util.vector.of(3.35f, 3.75f, 5))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Once assembled, right-click the block to start driving");
		scene.idle(60);

		scene.world.moveSection(trainElement, util.vector.of(4, 0, 0), 20);
		scene.world.animateTrainStation(stationPos, false);
		scene.idle(30);

		scene.overlay.showText(60)
			.pointAt(util.vector.of(7.35f, 3.75f, 5))
			.placeNearTarget()
			.text("Accelerate and steer the Train using movement keybinds");
		scene.idle(60);

		scene.world.moveSection(trainElement, util.vector.of(-4, 0, 0), 30);
		scene.idle(40);

		scene.overlay
			.showControls(new InputWindowElement(util.vector.topOf(initialControlsPos), Pointing.DOWN).scroll(), 70);
		scene.idle(20);

		scene.overlay.showText(90)
			.pointAt(util.vector.of(3.35f, 3.75f, 5))
			.placeNearTarget()
			.text("If desired, the top speed can be fine-tuned using the mouse wheel");
		scene.idle(90);

		scene.world.moveSection(trainElement, util.vector.of(2, 0, 0), 30);
		scene.idle(40);

		scene.world.moveSection(trainElement, util.vector.of(-3, 0, 0), 60);
		scene.idle(70);

		scene.overlay.showText(50)
			.pointAt(util.vector.of(2.35f, 3.75f, 5))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Hold space to approach a nearby Station");
		scene.idle(40);

		scene.world.moveSection(trainElement, util.vector.of(1, 0, 0), 20);
		scene.idle(20);
		scene.effects.indicateSuccess(stationPos);
		scene.world.animateTrainStation(stationPos, true);
		scene.idle(10);

		scene.overlay.showText(80)
			.pointAt(util.vector.topOf(stationPos))
			.placeNearTarget()
			.text("Trains can only be disassembled back into blocks at Stations");
		scene.idle(40);
		scene.world.cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.world.toggleControls(initialControlsPos);
		scene.idle(20);
		scene.world.showSectionAndMerge(whistle, Direction.DOWN, trainElement);
		scene.idle(20);
		scene.world.cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.effects.indicateSuccess(stationPos);
		scene.world.toggleControls(initialControlsPos);
		scene.idle(20);

		scene.overlay.showText(70)
			.pointAt(util.vector.of(4.95f, 3.75f, 5))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Assembled Whistles can be activated with the sprint key");

		scene.idle(40);
		scene.world.toggleRedstonePower(whistle);
		scene.idle(20);
		scene.world.toggleRedstonePower(whistle);
		scene.idle(20);

		scene.overlay.showText(70)
			.pointAt(util.vector.of(3.35f, 3.75f, 5))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Sneak or click again to stop controlling the Train");
		scene.idle(60);
	}

	public static void schedule(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("train_schedule", "Using Train Schedules");
		scene.configureBasePlate(1, 0, 9);
		scene.scaleSceneView(.75f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();
		scene.debug.debugSchematic();

		for (int i = 10; i >= 0; i--) {
			scene.world.showSection(util.select.position(i, 1, 4), Direction.DOWN);
			scene.idle(1);
		}

		scene.idle(10);
	}

}
