package com.simibubi.create.infrastructure.ponder.scenes.highLogistics;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.api.math.Pointing;
import net.createmod.ponder.api.client.PonderPalette;
import net.createmod.ponder.api.client.element.ElementLink;
import net.createmod.ponder.api.client.element.ParrotElement;
import net.createmod.ponder.api.client.element.ParrotPose.FacePointOfInterestPose;
import net.createmod.ponder.api.client.element.WorldSectionElement;
import net.createmod.ponder.api.client.scene.SceneBuilder;
import net.createmod.ponder.api.client.scene.SceneBuildingUtil;
import net.createmod.ponder.api.client.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PostboxScenes {

	public static void postbox(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("postbox", "Transporting packages between Postboxes");
		scene.configureBasePlate(0, 1, 9);
		scene.scaleSceneView(.875f);
		scene.removeShadow();
		scene.setSceneOffsetY(-0.5f);

		BlockPos station = util.grid()
			.at(1, 1, 8);
		Selection stationS = util.select()
			.position(1, 1, 8);
		BlockPos box2 = util.grid()
			.at(3, 1, 8);
		Selection box2S = util.select()
			.position(3, 1, 8);
		Selection glass = util.select()
			.fromTo(3, 1, 9, 1, 1, 9);
		BlockPos box = util.grid()
			.at(1, 2, 2);
		Selection boxS = util.select()
			.position(1, 2, 2);
		Selection girder = util.select()
			.position(1, 1, 2);
		Selection belt = util.select()
			.fromTo(4, 1, 2, 2, 1, 2);
		Selection cog = util.select()
			.fromTo(4, 1, 1, 4, 1, 0);
		Selection largeCog = util.select()
			.position(5, 0, 0);
		Selection train1 = util.select()
			.fromTo(0, 2, 4, 3, 3, 6);
		Selection train2 = util.select()
			.fromTo(4, 2, 4, 8, 3, 6);
		BlockPos controls = util.grid()
			.at(2, 3, 5);
		Selection tracks = util.select()
			.fromTo(9, 1, 5, 30, 1, 5);
		BlockPos funnel = util.grid()
			.at(2, 2, 2);

		ItemStack boxItem2 = PackageItem.containing(List.of());
		PackageItem.addAddress(boxItem2, "Peter");
		scene.world()
			.createItemOnBeltLike(util.grid()
				.at(6, 2, 4), Direction.DOWN, boxItem2);

		scene.world()
			.toggleControls(controls);
		ElementLink<WorldSectionElement> base = scene.world()
			.showIndependentSection(util.select()
				.fromTo(0, 0, 1, 8, 0, 9), Direction.UP);
		scene.idle(10);

		ElementLink<WorldSectionElement> tracksL = scene.world()
			.showIndependentSection(util.select()
				.position(8, 1, 5), Direction.DOWN);
		scene.idle(1);
		for (int i = 7; i >= 0; i--) {
			scene.world()
				.showSectionAndMerge(util.select()
					.position(i, 1, 5), Direction.DOWN, tracksL);
			scene.idle(1);
		}
		scene.idle(5);

		scene.world()
			.showSectionAndMerge(stationS, Direction.DOWN, base);
		scene.idle(15);

		Vec3 fromTarget = util.vector()
			.topOf(station);

		ItemStack postboxItem = AllBlocks.PACKAGE_POSTBOXES.get(DyeColor.WHITE)
			.asStack();
		scene.overlay()
			.showControls(fromTarget, Pointing.DOWN, 50)
			.rightClick()
			.withItem(postboxItem);
		scene.idle(5);

		AABB bb1 = new AABB(fromTarget, fromTarget);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.WHITE, box, bb1, 10);
		scene.idle(1);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.WHITE, box, bb1.inflate(0.025, 0.025, 0.025), 50);
		scene.idle(26);

		scene.overlay()
			.showText(80)
			.text("Right-click a Train Station and place the Postbox nearby")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(fromTarget);

		scene.idle(40);

		ElementLink<WorldSectionElement> postboxE = scene.world()
			.showIndependentSection(boxS, Direction.DOWN);
		scene.world()
			.moveSection(postboxE, util.vector()
				.of(0, -1, 0), 0);

		scene.idle(15);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, box, bb1.inflate(0.025, 0.025, 0.025), 50);

		AABB bb2 = new AABB(box.below()).deflate(0.125, 0, 0)
			.contract(0, 0.125, 0);

		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.GREEN, box2, bb2, 50);
		scene.idle(10);
		scene.overlay()
			.showLine(PonderPalette.GREEN, util.vector()
				.topOf(box.below()), fromTarget, 40);
		scene.idle(45);

		scene.overlay()
			.showControls(util.vector()
				.topOf(box.below()), Pointing.DOWN, 40)
			.rightClick();
		scene.idle(7);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, funnel, bb2, 70);
		scene.overlay()
			.showText(70)
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.text("Assign it an address in the inventory UI")
			.pointAt(util.vector()
				.topOf(box.below()))
			.placeNearTarget();
		scene.idle(80);

		scene.world()
			.moveSection(postboxE, util.vector()
				.of(0, 1, 0), 10);
		scene.idle(10);
		scene.world()
			.showSectionAndMerge(girder, Direction.NORTH, base);
		scene.idle(5);
		scene.world()
			.showSectionAndMerge(largeCog, Direction.UP, base);
		scene.world()
			.showSectionAndMerge(cog, Direction.DOWN, base);
		scene.world()
			.showSectionAndMerge(belt, Direction.WEST, base);
		scene.idle(5);

		scene.world()
			.showSectionAndMerge(util.select()
				.position(funnel), Direction.DOWN, base);
		scene.idle(10);

		ItemStack boxItem = PackageStyles.getDefaultBox()
			.copy();
		PackageItem.addAddress(boxItem, "Peter");

		scene.world()
			.createItemOnBelt(util.grid()
				.at(3, 1, 2), Direction.EAST, boxItem);
		scene.idle(5);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 1 / 32f);

		scene.overlay()
			.showText(60)
			.attachKeyFrame()
			.text("If the address of an inserted package does not match it..")
			.pointAt(util.vector()
				.centerOf(3, 2, 2))
			.placeNearTarget();

		scene.idle(70);

		scene.overlay()
			.showText(40)
			.colored(PonderPalette.BLUE)
			.text("Warehouse")
			.pointAt(util.vector()
				.blockSurface(box, Direction.NORTH)
				.add(-.5, 0, 0))
			.placeNearTarget();
		scene.idle(5);
		scene.overlay()
			.showText(40)
			.colored(PonderPalette.OUTPUT)
			.text("\u2192 Outpost")
			.pointAt(util.vector()
				.centerOf(3, 2, 2))
			.placeNearTarget();

		scene.idle(50);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 32f);
		scene.idle(17);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(2, 1, 2));
		animatePostbox(scene, box, true);
		scene.world()
			.flapFunnel(funnel, false);
		scene.idle(15);

		ElementLink<WorldSectionElement> train1L = scene.world()
			.showIndependentSection(train1, null);
		ElementLink<ParrotElement> birbL = scene.special()
			.createBirb(util.vector()
				.of(9.5, 3.5, 5.5), FacePointOfInterestPose::new);
		scene.special()
			.movePointOfInterest(util.grid()
				.at(-5, 4, 5));
		scene.world()
			.moveSection(train1L, util.vector()
				.of(6, 0, 0), 0);
		scene.idle(1);
		scene.special()
			.moveParrot(birbL, util.vector()
				.of(-5, 0, 0), 25);
		scene.world()
			.moveSection(train1L, util.vector()
				.of(-5, 0, 0), 25);
		scene.world()
			.animateBogey(util.grid()
				.at(2, 2, 5), 6, 25);
		scene.idle(14);
		ElementLink<WorldSectionElement> train2L = scene.world()
			.showIndependentSection(train2, null);
		scene.world()
			.moveSection(train2L, util.vector()
				.of(3, 0, 0), 0);
		scene.idle(1);
		scene.world()
			.moveSection(train2L, util.vector()
				.of(-2, 0, 0), 10);
		scene.world()
			.animateBogey(util.grid()
				.at(6, 2, 5), 2, 10);

		scene.idle(10);
		scene.special()
			.moveParrot(birbL, util.vector()
				.of(-1, 0, 0), 10);
		scene.world()
			.moveSection(train1L, util.vector()
				.of(-1, 0, 0), 10);
		scene.world()
			.animateBogey(util.grid()
				.at(2, 2, 5), 1, 10);
		scene.world()
			.moveSection(train2L, util.vector()
				.of(-1, 0, 0), 10);
		scene.world()
			.animateBogey(util.grid()
				.at(6, 2, 5), 1, 10);
		scene.idle(10);
		scene.world()
			.animateTrainStation(station, true);
		scene.idle(10);
		scene.effects()
			.indicateSuccess(box);
		scene.effects()
			.indicateSuccess(util.grid()
				.at(5, 3, 4));
		scene.world()
			.createItemOnBeltLike(util.grid()
				.at(5, 2, 4), Direction.DOWN, boxItem);

		scene.idle(15);
		animatePostbox(scene, box, false);
		scene.overlay()
			.showText(80)
			.text("..trains stopping at the station will collect it as cargo")
			.attachKeyFrame()
			.pointAt(util.vector()
				.topOf(5, 2, 4))
			.placeNearTarget();
		scene.idle(95);

		scene.overlay()
			.showText(80)
			.text("Conversely, packages matching the address will be dropped off")
			.attachKeyFrame()
			.pointAt(util.vector()
				.topOf(6, 2, 4))
			.placeNearTarget();
		scene.idle(50);

		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(6, 2, 4));
		scene.effects()
			.indicateSuccess(box);
		scene.effects()
			.indicateSuccess(util.grid()
				.at(6, 2, 4));
		animatePostbox(scene, box, true);
		scene.idle(60);

		scene.overlay()
			.showText(100)
			.text("Packages that arrived by train can be extracted from the Postbox")
			.attachKeyFrame()
			.pointAt(util.vector()
				.blockSurface(box, Direction.NORTH))
			.placeNearTarget();
		scene.idle(60);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), -1);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(2, 1, 2), Direction.WEST, boxItem2);
		animatePostbox(scene, box, false);
		scene.idle(25);
		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 0);

		scene.idle(25);
		scene.overlay()
			.showText(40)
			.colored(PonderPalette.BLUE)
			.text("Warehouse")
			.pointAt(util.vector()
				.blockSurface(box, Direction.NORTH)
				.add(-.5, 0, 0))
			.placeNearTarget();
		scene.idle(5);
		scene.overlay()
			.showText(40)
			.colored(PonderPalette.OUTPUT)
			.text("\u2192 Warehouse")
			.pointAt(util.vector()
				.centerOf(3, 2, 2)
				.add(0, -.25, 0))
			.placeNearTarget();

		scene.idle(50);

		ElementLink<WorldSectionElement> tracksL2 = scene.world()
			.showIndependentSection(tracks, Direction.EAST);
		scene.world()
			.moveSection(tracksL2, util.vector()
				.of(-31, 0, 0), 0);

		scene.idle(15);
		scene.world()
			.animateTrainStation(station, false);
		scene.world()
			.moveSection(tracksL, util.vector()
				.of(12, 0, 0), 120);
		scene.world()
			.moveSection(tracksL2, util.vector()
				.of(12, 0, 0), 120);
		scene.world()
			.moveSection(postboxE, util.vector()
				.of(12, 0, 0), 120);
		scene.world()
			.moveSection(base, util.vector()
				.of(12, 0, 0), 120);
		scene.world()
			.animateBogey(util.grid()
				.at(2, 2, 5), 12f, 120);
		scene.world()
			.animateBogey(util.grid()
				.at(6, 2, 5), 12f, 120);
		scene.idle(15);
		scene.world()
			.hideIndependentSection(base, null);
		scene.world()
			.hideIndependentSection(postboxE, null);
		scene.idle(40);
		scene.special()
			.hideElement(birbL, null);
		scene.world()
			.hideIndependentSection(train1L, null);
		scene.world()
			.hideIndependentSection(train2L, null);
		scene.idle(5);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, train1L, new AABB(util.grid()
				.at(1, 3, 4)).inflate(1, .75f, .5f), 280);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, train2L, new AABB(util.grid()
				.at(5, 3, 4)).inflate(1, .75f, .5f), 280);
		scene.idle(19);

		ElementLink<WorldSectionElement> outpostL = scene.world()
			.showIndependentSection(glass, Direction.UP);
		scene.world()
			.moveSection(outpostL, util.vector()
				.of(-4, -1, -1), 0);
		scene.idle(1);
		scene.world()
			.moveSection(outpostL, util.vector()
				.of(4, 0, 0), 40);
		scene.idle(9);
		ElementLink<WorldSectionElement> stationL = scene.world()
			.showIndependentSection(stationS.add(box2S), Direction.DOWN);
		scene.world()
			.moveSection(stationL, util.vector()
				.of(-3, 0, 0), 0);
		scene.idle(1);
		scene.world()
			.moveSection(stationL, util.vector()
				.of(3, 0, 0), 30);
		scene.idle(30);
		scene.world()
			.animateTrainStation(util.grid()
				.at(1, 1, 8), true);
		scene.idle(10);

		scene.overlay()
			.showText(90)
			.text("Just like trains, Postboxes maintain their behaviour in unloaded chunks")
			.colored(PonderPalette.BLUE)
			.attachKeyFrame()
			.independent(30);
		scene.idle(100);
		scene.effects()
			.indicateSuccess(util.grid()
				.at(3, 1, 8));
		animatePostbox(scene, util.grid()
			.at(3, 1, 8), true);

		scene.overlay()
			.showText(90)
			.text("Packages can still be delivered from or to their inventory")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(3, 1, 8), Direction.NORTH));
		scene.idle(80);
	}

	public static void animatePostbox(CreateSceneBuilder scene, BlockPos box, boolean raise) {
		scene.world()
			.modifyBlockEntity(box, PostboxBlockEntity.class, be -> be.forceFlag = raise);
	}

}
