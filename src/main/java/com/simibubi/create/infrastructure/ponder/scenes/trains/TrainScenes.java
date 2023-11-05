package com.simibubi.create.infrastructure.ponder.scenes.trains;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.trains.station.StationBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TrainScenes {

	public static void controls(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("train_controls", "Controlling Trains");
		scene.configureBasePlate(1, 0, 9);
		scene.scaleSceneView(.75f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();

		for (int i = 10; i >= 0; i--) {
			scene.world().showSection(util.select().position(i, 1, 4), Direction.DOWN);
			scene.idle(1);
		}

		BlockPos stationPos = util.grid().at(4, 1, 1);
		Selection station = util.select().position(stationPos);
		Selection whistle = util.select().fromTo(4, 3, 4, 4, 4, 4);
		Selection train = util.select().fromTo(5, 2, 3, 1, 3, 5)
			.substract(whistle);

		scene.world().showSection(station, Direction.DOWN);
		scene.idle(20);

		ElementLink<WorldSectionElement> trainElement = scene.world().showIndependentSection(train, Direction.DOWN);
		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.idle(15);

		BlockPos initialControlsPos = util.grid().at(3, 3, 4);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.WHITE, train,
												new AABB(initialControlsPos).contract(-6 / 16f, 2 / 16f, 0), 85);
		scene.idle(15);

		scene.overlay().showText(70)
			.pointAt(util.vector().of(3.35f, 3.75f, 5))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Train Controls are required on every train contraption");
		scene.idle(60);

		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.effects().indicateSuccess(stationPos);
		scene.world().animateTrainStation(stationPos, true);
		scene.world().toggleControls(initialControlsPos);
		scene.idle(20);

		scene.overlay().showControls(util.vector().topOf(initialControlsPos), Pointing.DOWN, 70).rightClick();
		scene.idle(20);

		scene.overlay().showText(60)
			.pointAt(util.vector().of(3.35f, 3.75f, 5))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Once assembled, right-click the block to start driving");
		scene.idle(60);

		scene.world().moveSection(trainElement, util.vector().of(4, 0, 0), 20);
		scene.world().animateBogey(util.grid().at(3, 2, 4), -4f, 20);
		scene.world().animateTrainStation(stationPos, false);
		scene.idle(30);

		scene.overlay().showText(60)
			.pointAt(util.vector().of(7.35f, 3.75f, 5))
			.placeNearTarget()
			.text("Accelerate and steer the Train using movement keybinds");
		scene.idle(60);

		scene.world().moveSection(trainElement, util.vector().of(-4, 0, 0), 30);
		scene.world().animateBogey(util.grid().at(3, 2, 4), 4f, 30);
		scene.idle(40);

		scene.overlay().showControls(util.vector().topOf(initialControlsPos), Pointing.DOWN, 70).scroll();
		scene.idle(20);

		scene.overlay().showText(90)
			.pointAt(util.vector().of(3.35f, 3.75f, 5))
			.placeNearTarget()
			.text("If desired, the top speed can be fine-tuned using the mouse wheel");
		scene.idle(90);

		scene.world().moveSection(trainElement, util.vector().of(2, 0, 0), 30);
		scene.world().animateBogey(util.grid().at(3, 2, 4), -2f, 30);
		scene.idle(40);

		scene.world().moveSection(trainElement, util.vector().of(-3, 0, 0), 60);
		scene.world().animateBogey(util.grid().at(3, 2, 4), 3f, 60);
		scene.idle(70);

		scene.overlay().showText(50)
			.pointAt(util.vector().of(2.35f, 3.75f, 5))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Hold space to approach a nearby Station");
		scene.idle(40);

		scene.world().moveSection(trainElement, util.vector().of(1, 0, 0), 20);
		scene.world().animateBogey(util.grid().at(3, 2, 4), -1f, 20);
		scene.idle(20);
		scene.effects().indicateSuccess(stationPos);
		scene.world().animateTrainStation(stationPos, true);
		scene.idle(10);

		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(stationPos))
			.placeNearTarget()
			.text("Trains can only be disassembled back into blocks at Stations");
		scene.idle(40);
		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.world().toggleControls(initialControlsPos);
		scene.idle(20);
		scene.world().showSectionAndMerge(whistle, Direction.DOWN, trainElement);
		scene.idle(20);
		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.effects().indicateSuccess(stationPos);
		scene.world().toggleControls(initialControlsPos);
		scene.idle(20);

		scene.overlay().showText(70)
			.pointAt(util.vector().of(4.95f, 3.75f, 5))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Assembled Whistles can be activated with the sprint key");

		scene.idle(40);
		scene.world().toggleRedstonePower(whistle);
		scene.idle(20);
		scene.world().toggleRedstonePower(whistle);
		scene.idle(20);

		scene.overlay().showText(70)
			.pointAt(util.vector().of(3.35f, 3.75f, 5))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Sneak or click again to stop controlling the Train");
		scene.idle(60);
	}

	public static void schedule(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("train_schedule", "Using Train Schedules");
		scene.configureBasePlate(1, 0, 9);
		scene.scaleSceneView(.75f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();

		scene.world().cycleBlockProperty(util.grid().at(3, 3, 4), BlazeBurnerBlock.HEAT_LEVEL);

		for (int i = 10; i >= 0; i--) {
			scene.world().showSection(util.select().position(i, 1, 4), Direction.DOWN);
			scene.idle(1);
		}

		scene.world().toggleControls(util.grid().at(4, 3, 4));
		scene.world().toggleControls(util.grid().at(4, 3, 7));

		BlockPos stationPos = util.grid().at(5, 1, 1);
		Selection train1 = util.select().fromTo(6, 2, 3, 2, 3, 5);
		Selection train2 = util.select().fromTo(6, 2, 6, 2, 3, 8);

		scene.idle(10);

		scene.world().showSection(util.select().position(stationPos), Direction.DOWN);
		scene.idle(5);
		ElementLink<WorldSectionElement> trainElement1 = scene.world().showIndependentSection(train1, Direction.DOWN);
		scene.idle(10);
		scene.world().animateTrainStation(stationPos, true);
		scene.idle(10);

		scene.overlay().showText(70)
			.pointAt(util.vector().blockSurface(util.grid().at(3, 3, 4), Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Schedules allow Trains to be controlled by other Drivers");
		scene.idle(80);

		Vec3 target = util.vector().topOf(util.grid().at(4, 0, 2));
		scene.overlay().showControls(target, Pointing.RIGHT, 80).rightClick()
			.withItem(AllItems.SCHEDULE.asStack());
		scene.overlay().showText(80)
			.pointAt(target)
			.placeNearTarget()
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.text("Right-click with the item in hand to open its Interface");
		scene.idle(100);

		scene.overlay().showControls(util.vector().topOf(util.grid().at(3, 3, 4)), Pointing.DOWN, 80).rightClick()
				.withItem(AllItems.SCHEDULE.asStack());
		scene.idle(6);
		scene.world().conductorBlaze(util.grid().at(3, 3, 4), true);
		scene.overlay().showText(70)
			.pointAt(util.vector().blockSurface(util.grid().at(3, 3, 4), Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Once programmed, the Schedule can be handed off to a Train Driver");
		scene.idle(80);

		scene.world().moveSection(trainElement1, util.vector().of(12, 0, 0), 60);
		scene.world().animateBogey(util.grid().at(4, 2, 4), -12f, 60);
		scene.world().animateTrainStation(stationPos, false);
		scene.idle(20);
		scene.world().hideIndependentSection(trainElement1, null);
		scene.idle(25);

		ElementLink<WorldSectionElement> trainElement2 = scene.world().showIndependentSection(train2, Direction.DOWN);
		scene.world().moveSection(trainElement2, util.vector().of(0, 0, -3), 0);
		scene.idle(10);
		Vec3 birbVec = util.vector().topOf(util.grid().at(3, 0, 7));
		ElementLink<ParrotElement> birb = scene.special().createBirb(birbVec, ParrotPose.FacePointOfInterestPose::new);
		scene.world().animateTrainStation(stationPos, true);

		scene.overlay().showText(110)
			.pointAt(birbVec)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Any mob or blaze burner sitting in front of Train Controls is an eligible conductor");
		scene.idle(80);

		scene.overlay().showControls(util.vector().centerOf(util.grid().at(3, 1, 7)), Pointing.DOWN, 30)
			.withItem(new ItemStack(Items.LEAD));
		scene.idle(40);
		target = util.vector().centerOf(util.grid().at(3, 3, 4));
		scene.overlay().showControls(target.add(0.5, 0, 0), Pointing.RIGHT, 30).rightClick()
			.withItem(new ItemStack(Items.LEAD));
		scene.idle(6);
		scene.special().moveParrot(birb, target.subtract(birbVec), 5);
		scene.effects().indicateSuccess(util.grid().at(3, 3, 4));
		scene.idle(15);

		scene.overlay().showText(70)
			.pointAt(target)
			.placeNearTarget()
			.colored(PonderPalette.BLUE)
			.attachKeyFrame()
			.text("Creatures on a lead can be given their seat more conveniently");
		scene.idle(80);

		scene.overlay().showControls(util.vector().topOf(util.grid().at(3, 3, 4)), Pointing.DOWN, 15)
			.withItem(AllItems.SCHEDULE.asStack());
		scene.idle(6);
		scene.special().conductorBirb(birb, true);
		scene.special().movePointOfInterest(util.grid().at(16, 4, 4));
		scene.idle(14);

		scene.world().moveSection(trainElement2, util.vector().of(3, 0, 0), 30);
		scene.world().animateBogey(util.grid().at(4, 2, 7), -3f, 30);
		scene.special().moveParrot(birb, util.vector().of(3, 0, 0), 30);
		scene.idle(40);

		scene.overlay().showControls(util.vector().topOf(util.grid().at(6, 3, 4)), Pointing.DOWN, 70).rightClick();
		scene.idle(6);
		scene.special().conductorBirb(birb, false);
		scene.special().movePointOfInterest(util.grid().at(3, 4, 1));
		scene.idle(19);
		scene.overlay().showText(70)
			.pointAt(target.add(3, 0, 0))
			.placeNearTarget()
			.colored(PonderPalette.BLUE)
			.attachKeyFrame()
			.text("Schedules can be retrieved from Drivers at any moment");
		scene.idle(80);

	}

}
