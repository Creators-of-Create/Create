package com.simibubi.create.infrastructure.ponder.scenes.trains;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.trains.observer.TrackObserverBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TrackObserverScenes {

	public static void observe(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("track_observer", "Detecting Trains");
		scene.configureBasePlate(1, 1, 9);
		scene.scaleSceneView(.65f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();

		scene.world().toggleControls(util.grid().at(5, 3, 7));
		scene.special().movePointOfInterest(util.grid().at(-10, 2, 7));

		Selection observer = util.select().position(5, 1, 4);
		Selection redstone = util.select().fromTo(5, 1, 3, 5, 1, 2);

		Selection train1 = util.select().fromTo(7, 2, 6, 3, 3, 8);
		Selection train2 = util.select().fromTo(11, 2, 6, 8, 3, 8);
		Selection train2a = util.select().fromTo(11, 2, 3, 8, 3, 5);
		Selection train2b = util.select().fromTo(11, 2, 0, 8, 3, 2);

		for (int i = 10; i >= 0; i--) {
			scene.world().showSection(util.select().position(i, 1, 7), Direction.DOWN);
			scene.idle(1);
		}

		scene.idle(10);

		Vec3 target = util.vector().topOf(5, 0, 7);
		AABB bb = new AABB(target, target).move(0, 2 / 16f, 0);

		scene.overlay().showControls(new InputWindowElement(target, Pointing.DOWN).rightClick()
			.withItem(AllBlocks.TRACK_OBSERVER.asStack()), 40);
		scene.idle(6);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.inflate(.45f, 1 / 16f, .45f), 60);
		scene.idle(10);

		scene.overlay().showText(50)
			.pointAt(target)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.text("Select a Train Track then place the Observer nearby");
		scene.idle(20);

		scene.world().showSection(observer, Direction.DOWN);
		scene.idle(15);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, bb, new AABB(util.grid().at(5, 1, 4)), 20);
		scene.idle(25);

		scene.overlay().showText(70)
			.pointAt(util.vector().blockSurface(util.grid().at(5, 1, 4), Direction.WEST))
			.attachKeyFrame()
			.placeNearTarget()
			.text("The Observer will detect any Trains passing over the marker");
		scene.idle(20);
		scene.world().showSection(redstone, Direction.SOUTH);
		scene.idle(30);

		ElementLink<WorldSectionElement> trainInstance1 = scene.world().showIndependentSection(train1, Direction.WEST);
		scene.world().moveSection(trainInstance1, util.vector().of(6, 0, 0), 0);
		scene.world().moveSection(trainInstance1, util.vector().of(-16, 0, 0), 80);
		scene.world().animateBogey(util.grid().at(5, 2, 7), 16, 80);
		ElementLink<ParrotElement> birb =
			scene.special().createBirb(util.vector().centerOf(12, 3, 7), ParrotElement.FacePointOfInterestPose::new);
		scene.special().moveParrot(birb, util.vector().of(-16, 0, 0), 80);
		scene.idle(10);

		ElementLink<WorldSectionElement> trainInstance2 = scene.world().showIndependentSection(train2, Direction.WEST);
		scene.world().moveSection(trainInstance2, util.vector().of(4, 0, 0), 0);
		scene.world().moveSection(trainInstance2, util.vector().of(-14, 0, 0), 70);
		scene.world().animateBogey(util.grid().at(9, 2, 7), 14, 70);

		Selection add = redstone.add(observer);

		scene.idle(13);
		scene.world().toggleRedstonePower(add);
		scene.effects().indicateRedstone(util.grid().at(5, 1, 4));
		scene.idle(20);
		scene.world().hideIndependentSection(trainInstance1, Direction.WEST);
		scene.special().hideElement(birb, Direction.WEST);
		scene.idle(10);
		scene.world().toggleRedstonePower(add);
		scene.idle(5);
		scene.world().hideIndependentSection(trainInstance2, Direction.WEST);
		scene.idle(20);

		target = util.vector().topOf(5, 1, 4);
		bb = new AABB(target, target);
		scene.overlay().showCenteredScrollInput(util.grid().at(5, 1, 4), Direction.UP, 60);

		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(5, 1, 4))
			.attachKeyFrame()
			.placeNearTarget()
			.text("Observers can be filtered to activate for matching cargo");
		scene.idle(40);

		ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
		scene.overlay().showControls(new InputWindowElement(target, Pointing.DOWN).withItem(waterBucket), 30);
		scene.idle(6);
		scene.world().setFilterData(observer, TrackObserverBlockEntity.class, waterBucket);
		scene.idle(50);

		trainInstance1 = scene.world().showIndependentSection(train1, Direction.WEST);
		scene.world().moveSection(trainInstance1, util.vector().of(6, 0, 0), 0);
		scene.world().moveSection(trainInstance1, util.vector().of(-16, 0, 0), 80);
		scene.world().animateBogey(util.grid().at(5, 2, 7), 16, 80);
		birb = scene.special().createBirb(util.vector().centerOf(12, 3, 7), ParrotElement.FacePointOfInterestPose::new);
		scene.special().moveParrot(birb, util.vector().of(-16, 0, 0), 80);
		scene.idle(10);

		trainInstance2 = scene.world().showIndependentSection(train2b, Direction.WEST);
		scene.world().moveSection(trainInstance2, util.vector().of(4, 0, 6), 0);
		scene.world().moveSection(trainInstance2, util.vector().of(-14, 0, 0), 70);
		scene.world().animateBogey(util.grid().at(9, 2, 1), 14, 80);

		scene.idle(33);
		scene.world().hideIndependentSection(trainInstance1, Direction.WEST);
		scene.special().hideElement(birb, Direction.WEST);
		scene.idle(10);
		scene.world().hideIndependentSection(trainInstance2, Direction.WEST);
		scene.idle(20);

		trainInstance1 = scene.world().showIndependentSection(train1, Direction.WEST);
		scene.world().moveSection(trainInstance1, util.vector().of(6, 0, 0), 0);
		scene.world().moveSection(trainInstance1, util.vector().of(-16, 0, 0), 80);
		scene.world().animateBogey(util.grid().at(5, 2, 7), 16, 80);
		birb = scene.special().createBirb(util.vector().centerOf(12, 3, 7), ParrotElement.FacePointOfInterestPose::new);
		scene.special().moveParrot(birb, util.vector().of(-16, 0, 0), 80);
		scene.idle(10);

		trainInstance2 = scene.world().showIndependentSection(train2a, Direction.WEST);
		scene.world().moveSection(trainInstance2, util.vector().of(4, 0, 3), 0);
		scene.world().moveSection(trainInstance2, util.vector().of(-14, 0, 0), 70);
		scene.world().animateBogey(util.grid().at(9, 2, 4), 14, 70);

		scene.idle(13);
		scene.world().toggleRedstonePower(add);
		scene.effects().indicateRedstone(util.grid().at(5, 1, 4));
		scene.idle(20);
		scene.world().hideIndependentSection(trainInstance1, Direction.WEST);
		scene.special().hideElement(birb, Direction.WEST);
		scene.idle(10);
		scene.world().toggleRedstonePower(add);
		scene.idle(5);
		scene.world().hideIndependentSection(trainInstance2, Direction.WEST);

	}

}
