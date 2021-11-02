package com.simibubi.create.foundation.ponder.content;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.LinearChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.RadialChassisBlock;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.EntityElement;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ChassisScenes {

	public static void linearGroup(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("linear_chassis_group", "Moving Linear Chassis in groups");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layer(1), Direction.DOWN);
		scene.idle(10);

		BlockPos centralChassis = util.grid.at(2, 2, 2);
		ElementLink<WorldSectionElement> chassis =
			scene.world.showIndependentSection(util.select.position(centralChassis), Direction.DOWN);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.position(centralChassis.west()), Direction.EAST, chassis);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(centralChassis.east()), Direction.WEST, chassis);
		scene.idle(4);
		scene.world.showSectionAndMerge(util.select.position(centralChassis.east()
			.north()), Direction.SOUTH, chassis);
		scene.idle(3);
		scene.world.showSectionAndMerge(util.select.position(centralChassis.above()), Direction.DOWN, chassis);
		scene.idle(2);
		scene.world.showSectionAndMerge(util.select.position(centralChassis.above()
			.east()), Direction.DOWN, chassis);
		scene.idle(10);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.placeNearTarget()
			.text("Linear Chassis connect to identical Chassis blocks next to them")
			.pointAt(util.vector.topOf(util.grid.at(2, 3, 2)));
		scene.idle(90);

		BlockPos bearingPos = util.grid.at(2, 1, 2);
		scene.world.moveSection(chassis, util.vector.of(0, -1 / 1024f, 0), 0);
		scene.world.configureCenterOfRotation(chassis, util.vector.centerOf(bearingPos));
		scene.world.rotateBearing(bearingPos, 360, 80);
		scene.world.rotateSection(chassis, 0, 360, 0, 80);

		scene.idle(20);
		scene.overlay.showText(80)
			.placeNearTarget()
			.text("When one is moved by a Contraption, the others are dragged with it")
			.pointAt(util.vector.topOf(util.grid.at(2, 3, 2)));
		scene.idle(90);

		Selection wrong1 = util.select.position(2, 4, 2);
		Selection wrong2 = util.select.position(0, 2, 2);

		scene.addKeyframe();
		scene.world.showSection(wrong2, Direction.EAST);
		scene.idle(10);
		scene.world.showSection(wrong1, Direction.DOWN);
		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.RED, wrong2, wrong2, 80);
		scene.overlay.showSelectionWithText(wrong1, 80)
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.text("Chassis of a different type or facing another direction will not attach");
		scene.idle(40);

		scene.world.rotateBearing(bearingPos, 360, 80);
		scene.world.rotateSection(chassis, 0, 360, 0, 80);
	}

	public static void linearAttachement(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("linear_chassis_attachment", "Attaching blocks using Linear Chassis");
		scene.configureBasePlate(0, 0, 5);
		scene.setSceneOffsetY(-1);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		BlockPos chassisPos = util.grid.at(2, 2, 2);
		Selection chassis = util.select.position(chassisPos);

		scene.world.showSection(util.select.layer(1), Direction.DOWN);
		scene.world.showSection(chassis, Direction.DOWN);
		scene.idle(10);

		InputWindowElement input =
			new InputWindowElement(util.vector.blockSurface(chassisPos, Direction.WEST), Pointing.LEFT).rightClick()
				.withItem(new ItemStack(Items.SLIME_BALL));
		scene.overlay.showControls(input, 30);
		scene.idle(7);
		scene.world.modifyBlock(chassisPos, s -> s.setValue(LinearChassisBlock.STICKY_BOTTOM, true), false);
		scene.effects.superGlue(chassisPos, Direction.WEST, false);
		scene.idle(30);

		scene.overlay.showText(60)
			.text("The open faces of a Linear Chassis can be made Sticky")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(chassisPos, Direction.WEST));
		scene.idle(70);

		scene.overlay.showControls(input, 15);
		scene.idle(7);
		scene.world.modifyBlock(chassisPos, s -> s.setValue(LinearChassisBlock.STICKY_TOP, true), false);
		scene.effects.superGlue(chassisPos, Direction.EAST, false);
		scene.idle(15);

		scene.overlay.showText(60)
			.text("Click again to make the opposite side sticky")
			.placeNearTarget()
			.pointAt(util.vector.topOf(chassisPos));
		scene.idle(10);
		scene.rotateCameraY(60);
		scene.idle(35);
		scene.rotateCameraY(-60);
		scene.idle(25);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(chassisPos, Direction.WEST), Pointing.LEFT).rightClick()
				.whileSneaking(),
			30);
		scene.idle(7);
		scene.world.modifyBlock(chassisPos, s -> s.setValue(LinearChassisBlock.STICKY_BOTTOM, false), false);
		scene.effects.superGlue(chassisPos, Direction.WEST, false);
		scene.idle(30);

		scene.overlay.showText(60)
			.text("Sneak and Right-Click with an empty hand to remove the slime")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(chassisPos, Direction.WEST));
		scene.idle(70);

		scene.world.hideSection(chassis, Direction.UP);

		scene.idle(20);
		ElementLink<WorldSectionElement> glassSection =
			scene.world.showIndependentSection(util.select.position(chassisPos.above()), Direction.DOWN);
		scene.world.moveSection(glassSection, util.vector.of(0, -1, 0), 0);
		scene.idle(25);
		scene.addKeyframe();
		scene.world.showSectionAndMerge(util.select.fromTo(2, 4, 2, 2, 5, 2), Direction.DOWN, glassSection);
		ElementLink<WorldSectionElement> topGlassSection =
			scene.world.showIndependentSection(util.select.position(2, 6, 2), Direction.DOWN);
		scene.world.moveSection(topGlassSection, util.vector.of(0, -1, 0), 0);
		scene.idle(30);

		Selection column1 = util.select.fromTo(2, 3, 2, 2, 3, 2);
		Selection column2 = util.select.fromTo(2, 3, 2, 2, 4, 2);
		Selection column3 = util.select.fromTo(2, 3, 2, 2, 5, 2);

		scene.overlay.showSelectionWithText(column3, 80)
			.colored(PonderPalette.GREEN)
			.text("Stickied faces of the Linear Chassis will attach a line of blocks in front of it")
			.placeNearTarget();
		scene.idle(90);

		BlockPos bearingPos = util.grid.at(2, 1, 2);
		scene.world.configureCenterOfRotation(glassSection, util.vector.centerOf(bearingPos));
		scene.world.rotateBearing(bearingPos, 180, 40);
		scene.world.rotateSection(glassSection, 0, 180, 0, 40);
		scene.world.rotateSection(topGlassSection, 0, 180, 0, 40);
		scene.idle(50);

		Vec3 blockSurface = util.vector.blockSurface(chassisPos, Direction.NORTH);
		scene.overlay.showCenteredScrollInput(chassisPos, Direction.NORTH, 50);
		scene.overlay.showControls(new InputWindowElement(blockSurface, Pointing.UP).scroll()
			.withWrench(), 50);

		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, column3, 20);
		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, column2, 20);
		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, column1, 20);
		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, column2, 15);
		scene.idle(10);

		scene.overlay.showText(60)
			.pointAt(blockSurface)
			.text("Using a Wrench, a precise Range can be specified for this chassis")
			.placeNearTarget();
		scene.idle(70);

		scene.world.rotateBearing(bearingPos, 180, 40);
		scene.world.rotateSection(glassSection, 0, 180, 0, 40);
		scene.idle(50);

		scene.world.rotateSection(topGlassSection, 0, 180, 0, 0);
		scene.world.showSectionAndMerge(util.select.position(1, 3, 2), Direction.UP, glassSection);
		scene.world.showSectionAndMerge(util.select.position(3, 3, 2), Direction.UP, glassSection);
		scene.world.showSectionAndMerge(util.select.fromTo(1, 4, 2, 1, 6, 2), Direction.DOWN, glassSection);
		scene.world.showSectionAndMerge(util.select.fromTo(3, 4, 2, 3, 6, 2), Direction.DOWN, glassSection);
		scene.addKeyframe();
		scene.idle(20);

		scene.overlay.showCenteredScrollInput(chassisPos, Direction.NORTH, 50);
		scene.overlay.showControls(new InputWindowElement(blockSurface, Pointing.UP).whileCTRL()
			.scroll()
			.withWrench(), 50);

		column1 = util.select.fromTo(1, 3, 2, 3, 3, 2);
		column2 = util.select.fromTo(1, 3, 2, 3, 4, 2);
		column3 = util.select.fromTo(1, 3, 2, 3, 5, 2);

		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, column2, 20);
		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, column1, 20);
		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, column2, 20);
		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, column3, 15);
		scene.idle(10);

		scene.overlay.showText(80)
			.pointAt(blockSurface)
			.text("Holding CTRL and scrolling adjusts the range of all attached Chassis Blocks")
			.placeNearTarget();
		scene.idle(90);

		scene.world.rotateBearing(bearingPos, 180, 40);
		scene.world.rotateSection(glassSection, 0, 180, 0, 40);
		scene.world.rotateSection(topGlassSection, 0, 180, 0, 40);
		scene.idle(50);

		Vec3 glueSurface = util.vector.blockSurface(chassisPos.west(), Direction.NORTH);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.pointAt(glueSurface)
			.text("Attaching blocks to any other side requires the use of Super Glue")
			.placeNearTarget();
		scene.idle(90);
		scene.overlay.showControls(new InputWindowElement(glueSurface, Pointing.DOWN).rightClick()
			.withItem(AllItems.SUPER_GLUE.asStack()), 30);
		scene.idle(7);
		ElementLink<EntityElement> glueEntity = scene.world.createGlueEntity(chassisPos.west(), Direction.NORTH);
		scene.idle(20);
		ElementLink<WorldSectionElement> gluedPlank =
			scene.world.showIndependentSection(util.select.position(3, 3, 1), Direction.SOUTH);
		scene.world.moveSection(gluedPlank, util.vector.of(-2, -1, 0), 0);
		scene.idle(15);
		scene.effects.superGlue(chassisPos.west(), Direction.NORTH, true);
		scene.idle(20);

		scene.world.modifyEntity(glueEntity, Entity::remove);
		scene.world.hideIndependentSection(glassSection, Direction.UP);
		scene.world.hideIndependentSection(gluedPlank, Direction.UP);
		scene.world.hideIndependentSection(topGlassSection, Direction.UP);
		scene.idle(15);

		scene.addKeyframe();
		ElementLink<WorldSectionElement> chain =
			scene.world.showIndependentSection(util.select.position(2, 7, 2), Direction.DOWN);
		scene.world.configureCenterOfRotation(chain, util.vector.centerOf(bearingPos));
		scene.world.moveSection(chain, util.vector.of(0, -5, 0), 0);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.fromTo(2, 8, 2, 3, 9, 2), Direction.DOWN, chain);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.fromTo(3, 9, 1, 3, 9, 0), Direction.SOUTH, chain);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.fromTo(2, 9, 0, 1, 9, 0), Direction.EAST, chain);
		scene.idle(20);

		scene.overlay.showText(80)
			.pointAt(util.vector.topOf(chassisPos.above(2)))
			.text("Using these mechanics, structures of any shape can move as a Contraption")
			.placeNearTarget();
		scene.idle(30);

		scene.world.rotateBearing(bearingPos, 720, 160);
		scene.world.rotateSection(chain, 0, 720, 0, 160);
	}

	public static void radial(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("radial_chassis", "Attaching blocks using Radial Chassis");
		scene.configureBasePlate(0, 0, 5);
		scene.setSceneOffsetY(-1);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		util.select.position(2, 4, 2);

		BlockPos chassisPos = util.grid.at(2, 2, 2);
		Selection chassis = util.select.position(chassisPos);

		scene.world.showSection(util.select.layer(1), Direction.DOWN);
		scene.idle(10);
		ElementLink<WorldSectionElement> contraption = scene.world.showIndependentSection(chassis, Direction.DOWN);
		scene.idle(5);
		ElementLink<WorldSectionElement> top =
			scene.world.showIndependentSection(util.select.position(chassisPos.above()), Direction.DOWN);
		scene.idle(10);

		scene.overlay.showText(50)
			.attachKeyFrame()
			.placeNearTarget()
			.text("Radial Chassis connect to identical Chassis blocks in a row")
			.pointAt(util.vector.topOf(chassisPos.above()));
		scene.idle(60);

		BlockPos bearingPos = util.grid.at(2, 1, 2);
		scene.world.moveSection(contraption, util.vector.of(0, -1 / 1024f, 0), 0);
		scene.world.configureCenterOfRotation(contraption, util.vector.centerOf(bearingPos));
		scene.world.rotateBearing(bearingPos, 360, 80);
		scene.world.rotateSection(contraption, 0, 360, 0, 80);
		scene.world.rotateSection(top, 0, 360, 0, 80);

		scene.idle(20);
		scene.overlay.showText(70)
			.placeNearTarget()
			.text("When one is moved by a Contraption, the others are dragged with it")
			.pointAt(util.vector.topOf(util.grid.at(2, 3, 2)));
		scene.idle(80);

		scene.world.hideIndependentSection(top, Direction.UP);
		scene.idle(15);

		scene.addKeyframe();
		InputWindowElement input =
			new InputWindowElement(util.vector.blockSurface(chassisPos, Direction.WEST), Pointing.LEFT).rightClick()
				.withItem(new ItemStack(Items.SLIME_BALL));
		scene.overlay.showControls(input, 30);
		scene.idle(7);
		scene.world.modifyBlock(chassisPos, s -> s.setValue(RadialChassisBlock.STICKY_WEST, true), false);
		scene.effects.superGlue(chassisPos, Direction.WEST, false);
		scene.idle(30);

		scene.overlay.showText(60)
			.text("The side faces of a Radial Chassis can be made Sticky")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(chassisPos, Direction.WEST));
		scene.idle(70);

		scene.overlay.showControls(input, 15);
		scene.idle(7);
		scene.world.modifyBlock(chassisPos, s -> s.setValue(RadialChassisBlock.STICKY_EAST, true)
			.setValue(RadialChassisBlock.STICKY_NORTH, true)
			.setValue(RadialChassisBlock.STICKY_SOUTH, true), false);
		scene.effects.superGlue(chassisPos, Direction.EAST, false);
		scene.effects.superGlue(chassisPos, Direction.SOUTH, false);
		scene.effects.superGlue(chassisPos, Direction.NORTH, false);
		scene.idle(15);

		scene.overlay.showText(60)
			.text("Click again to make all other sides sticky")
			.placeNearTarget()
			.pointAt(util.vector.topOf(chassisPos));
		scene.idle(10);
		scene.rotateCameraY(60);
		scene.idle(35);
		scene.rotateCameraY(-60);
		scene.idle(25);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(chassisPos, Direction.WEST), Pointing.LEFT).rightClick()
				.whileSneaking(),
			30);
		scene.idle(7);
		scene.world.modifyBlock(chassisPos, s -> s.setValue(RadialChassisBlock.STICKY_WEST, false), false);
		scene.effects.superGlue(chassisPos, Direction.WEST, false);
		scene.idle(30);

		scene.overlay.showText(60)
			.text("Sneak and Right-Click with an empty hand to remove the slime")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(chassisPos, Direction.WEST));
		scene.idle(70);

		Selection s = util.select.position(chassisPos.north());
		Selection growing = s.copy();
		Selection r1 = util.select.fromTo(1, 2, 1, 3, 2, 3)
			.substract(chassis);
		Selection r2 = r1.copy()
			.add(util.select.fromTo(0, 2, 1, 0, 2, 3))
			.add(util.select.fromTo(1, 2, 0, 3, 2, 0))
			.add(util.select.fromTo(1, 2, 4, 3, 2, 4))
			.add(util.select.fromTo(4, 2, 1, 4, 2, 3));
		Selection r3 = util.select.layer(2)
			.add(util.select.fromTo(-1, 2, 1, 5, 2, 3))
			.add(util.select.fromTo(1, 2, -1, 3, 2, 5))
			.substract(chassis);

		scene.addKeyframe();
		scene.world.showSectionAndMerge(r1, Direction.DOWN, contraption);
		ElementLink<WorldSectionElement> outer = scene.world.showIndependentSection(util.select.layer(2)
			.substract(chassis)
			.substract(r1), Direction.DOWN);
		scene.world.showSection(util.select.fromTo(0, 3, 3, 1, 3, 4), Direction.DOWN);
		scene.idle(10);
		Vec3 blockSurface = util.vector.blockSurface(chassisPos, Direction.NORTH);
		AABB bb = new AABB(blockSurface, blockSurface).inflate(.501, .501, 0);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb, 60);
		scene.overlay.showOutline(PonderPalette.WHITE, s, s, 80);
		scene.overlay.showText(40)
			.text("Whenever a Block is next to a sticky face...")
			.placeNearTarget()
			.pointAt(blockSurface.add(0, .5, 0));
		scene.idle(60);

		MutableObject<Selection> obj = new MutableObject<>(growing);
		r2.forEach(pos -> {
			scene.idle(1);
			Selection add = obj.getValue()
				.copy()
				.add(util.select.position(pos));
			scene.overlay.showOutline(PonderPalette.WHITE, s, add, 3);
			obj.setValue(add);
		});

		scene.overlay.showSelectionWithText(obj.getValue(), 60)
			.colored(PonderPalette.GREEN)
			.text("...it will attach all reachable blocks within a radius on that layer");
		scene.idle(70);

		scene.world.configureCenterOfRotation(outer, util.vector.centerOf(bearingPos));
		scene.world.rotateBearing(bearingPos, 360, 80);
		scene.world.rotateSection(contraption, 0, 360, 0, 80);
		scene.world.rotateSection(outer, 0, 360, 0, 80);
		scene.idle(90);

		scene.addKeyframe();
		blockSurface = util.vector.topOf(chassisPos);
		scene.overlay.showCenteredScrollInput(chassisPos, Direction.UP, 50);
		scene.overlay.showControls(new InputWindowElement(blockSurface, Pointing.DOWN).scroll()
			.withWrench(), 50);

		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, r2, 20);
		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, r3, 20);
		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, r2, 20);
		scene.idle(10);
		scene.overlay.showOutline(PonderPalette.WHITE, chassis, r1, 15);
		scene.idle(10);

		scene.overlay.showText(60)
			.pointAt(blockSurface)
			.text("Using a Wrench, a precise Radius can be specified for this chassis")
			.placeNearTarget();
		scene.idle(70);

		scene.world.rotateBearing(bearingPos, 360, 80);
		scene.world.rotateSection(contraption, 0, 360, 0, 80);
		scene.idle(90);
		
		scene.world.destroyBlock(util.grid.at(1, 2, 0));
		scene.idle(1);
		scene.world.destroyBlock(util.grid.at(1, 2, 1));
		scene.idle(1);
		scene.world.destroyBlock(util.grid.at(1, 2, 3));
		scene.idle(1);
		scene.world.destroyBlock(util.grid.at(1, 2, 4));
		scene.idle(10);

		Selection ignored = util.select.fromTo(0, 2, 1, 0, 2, 3)
			.add(util.select.position(1, 2, 2));
		scene.overlay.showOutline(PonderPalette.GREEN, r2, r2.copy()
			.substract(util.select.fromTo(0, 2, 0, 1, 2, 4)), 80);
		scene.markAsFinished();
		scene.overlay.showSelectionWithText(ignored, 80)
			.colored(PonderPalette.RED)
			.text("Blocks not reachable by any sticky face will not attach");
	}

	public static void superGlue(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("super_glue", "Attaching blocks using Super Glue");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layer(1), Direction.DOWN);
		scene.idle(10);

		BlockPos central = util.grid.at(2, 2, 2);
		ElementLink<WorldSectionElement> plank =
			scene.world.showIndependentSection(util.select.position(central), Direction.DOWN);
		scene.idle(15);
		Vec3 blockSurface = util.vector.blockSurface(central, Direction.NORTH);
		scene.overlay.showControls(new InputWindowElement(blockSurface, Pointing.DOWN).rightClick()
			.withItem(AllItems.SUPER_GLUE.asStack()), 40);
		scene.idle(7);
		ElementLink<EntityElement> glueEntity = scene.world.createGlueEntity(central, Direction.NORTH);
		scene.idle(10);
		scene.overlay.showText(60)
			.pointAt(blockSurface)
			.placeNearTarget()
			.text("Super Glue can be used between any two blocks")
			.colored(PonderPalette.GREEN);
		scene.idle(50);

		scene.world.glueBlockOnto(central.north(), Direction.SOUTH, plank);
		scene.idle(20);
		scene.world.modifyEntity(glueEntity, Entity::remove);

		BlockPos bearingPos = util.grid.at(2, 1, 2);
		scene.world.configureCenterOfRotation(plank, util.vector.centerOf(bearingPos));
		scene.world.rotateBearing(bearingPos, 360, 80);
		scene.world.rotateSection(plank, 0, 360, 0, 80);
		scene.idle(30);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(central))
			.placeNearTarget()
			.text("The attached blocks will move together when assembled into a Contraption");
		scene.idle(90);

		scene.overlay.showText(50)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(central))
			.placeNearTarget()
			.text("Whenever Super Glue is held in the off-hand...");
		scene.idle(60);

		scene.world.glueBlockOnto(central.south(), Direction.NORTH, plank);
		scene.idle(5);
		scene.world.glueBlockOnto(central.north()
			.east(), Direction.WEST, plank);
		scene.idle(5);
		scene.world.glueBlockOnto(central.above(), Direction.DOWN, plank);
		scene.idle(5);
		scene.world.glueBlockOnto(central.south()
			.west(), Direction.EAST, plank);
		scene.idle(10);

		scene.overlay.showText(80)
			.pointAt(util.vector.topOf(central)
				.subtract(.5, 0, 0))
			.placeNearTarget()
			.text("...added blocks will be glued to the face they were placed on automatically");
		scene.idle(90);

		scene.world.rotateBearing(bearingPos, 360, 80);
		scene.world.rotateSection(plank, 0, 360, 0, 80);
		scene.idle(90);

		glueEntity = scene.world.createGlueEntity(central, Direction.UP);
		scene.world.destroyBlock(central.above());
		scene.idle(20);
		scene.addKeyframe();
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(central), Pointing.DOWN).leftClick(), 40);
		scene.idle(7);
		scene.world.modifyEntity(glueEntity, Entity::remove);
		scene.effects.superGlue(central, Direction.UP, false);
		scene.idle(10);
		scene.overlay.showText(60)
			.pointAt(util.vector.topOf(central))
			.placeNearTarget()
			.text("Super Glue can be removed with Left-Click");
	}

}
