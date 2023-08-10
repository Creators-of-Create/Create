package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.crank.ValveHandleBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelBlock;
import com.simibubi.create.content.kinetics.gauge.GaugeBlock;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlock;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlock;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.foundation.ElementLink;
import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.SceneBuilder;
import net.createmod.ponder.foundation.SceneBuildingUtil;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class KineticsScenes {

	public static void shaftAsRelay(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("shaft", "Relaying rotational force using Shafts");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos gaugePos = util.grid.at(0, 1, 2);
		Selection gauge = util.select.position(gaugePos);
		scene.world.showSection(gauge, Direction.UP);
		scene.world.setKineticSpeed(gauge, 0);

		scene.idle(5);
		scene.world.showSection(util.select.position(5, 1, 2), Direction.DOWN);
		scene.idle(10);

		for (int i = 4; i >= 1; i--) {
			if (i == 2)
				scene.rotateCameraY(70);
			scene.idle(5);
			scene.world.showSection(util.select.position(i, 1, 2), Direction.DOWN);
		}

		scene.world.setKineticSpeed(gauge, 64);
		scene.effects.indicateSuccess(gaugePos);
		scene.idle(10);
		scene.overlay.showText(1000)
			.placeNearTarget()
			.text("Shafts will relay rotation in a straight line.")
			.pointAt(util.vector.of(3, 1.5, 2.5));

		scene.idle(20);
		scene.markAsFinished();
	}

	public static void shaftsCanBeEncased(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("shaft_casing", "Encasing Shafts");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		Selection shaft = util.select.cuboid(new BlockPos(0, 1, 2), new Vec3i(5, 0, 2));
		Selection andesite = util.select.position(3, 1, 2);
		Selection brass = util.select.position(1, 1, 2);

		scene.world.showSection(shaft, Direction.DOWN);
		scene.idle(20);

		BlockEntry<EncasedShaftBlock> andesiteEncased = AllBlocks.ANDESITE_ENCASED_SHAFT;
		ItemStack andesiteCasingItem = AllBlocks.ANDESITE_CASING.asStack();

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(3, 1, 2), Pointing.DOWN).rightClick()
			.withItem(andesiteCasingItem), 60);
		scene.idle(7);
		scene.world.setBlocks(andesite, andesiteEncased.getDefaultState()
			.setValue(EncasedShaftBlock.AXIS, Axis.X), true);
		scene.world.setKineticSpeed(shaft, 32);
		scene.idle(10);

		BlockEntry<EncasedShaftBlock> brassEncased = AllBlocks.BRASS_ENCASED_SHAFT;
		ItemStack brassCasingItem = AllBlocks.BRASS_CASING.asStack();

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(1, 0, 2), Pointing.UP).rightClick()
			.withItem(brassCasingItem), 60);
		scene.idle(7);
		scene.world.setBlocks(brass, brassEncased.getDefaultState()
			.setValue(EncasedShaftBlock.AXIS, Axis.X), true);
		scene.world.setKineticSpeed(shaft, 32);

		scene.idle(10);
		scene.overlay.showText(100)
			.placeNearTarget()
			.text("Brass or Andesite Casing can be used to decorate Shafts")
			.pointAt(util.vector.topOf(1, 1, 2));
		scene.idle(70);
	}

	public static void cogAsRelay(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("cogwheel", "Relaying rotational force using Cogwheels");
		scene.configureBasePlate(0, 0, 5);
		BlockPos gauge = util.grid.at(4, 1, 1);
		Selection gaugeSelect = util.select.position(gauge);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.world.showSection(gaugeSelect, Direction.UP);
		scene.world.setKineticSpeed(gaugeSelect, 0);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 1, 3, 1, 1, 5), Direction.DOWN);
		scene.idle(10);

		for (int i = 1; i <= 4; i++) {
			scene.idle(5);
			if (i == 2)
				scene.world.showSection(util.select.position(0, 1, 2), Direction.DOWN);
			scene.world.showSection(util.select.position(i, 1, 2), Direction.DOWN);
		}

		scene.world.setKineticSpeed(gaugeSelect, 64);
		scene.effects.indicateSuccess(gauge);
		scene.idle(10);
		scene.overlay.showText(60)
			.text("Cogwheels will relay rotation to other adjacent cogwheels")
			.pointAt(util.vector.blockSurface(util.grid.at(0, 1, 2), Direction.EAST));

		scene.idle(60);
		scene.world.showSection(util.select.fromTo(1, 1, 1, 2, 1, 1), Direction.SOUTH);
		scene.idle(10);
		scene.effects.rotationDirectionIndicator(util.grid.at(1, 1, 1));
		scene.effects.rotationDirectionIndicator(util.grid.at(2, 1, 1));
		scene.idle(20);
		scene.overlay.showText(100)
			.text("Neighbouring shafts connected like this will rotate in opposite directions")
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.NORTH));
		scene.idle(70);

	}

	public static void largeCogAsRelay(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("large_cogwheel", "Relaying rotational force using Large Cogwheels");
		scene.configureBasePlate(1, 1, 5);
		scene.world.setBlock(util.grid.at(4, 2, 3), AllBlocks.LARGE_COGWHEEL.getDefaultState()
			.setValue(CogWheelBlock.AXIS, Axis.X), false);

		scene.showBasePlate();
		scene.idle(5);
		scene.world.showSection(util.select.layer(1), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(3, 2, 4), Direction.NORTH);

		for (int i = 3; i >= 1; i--) {
			scene.idle(5);
			if (i == 3)
				scene.world.showSection(util.select.position(3, 2, 5), Direction.DOWN);
			scene.world.showSection(util.select.position(3, 2, i), Direction.DOWN);
		}

		scene.overlay.showText(70)
			.text("Large cogwheels can connect to each other at right angles")
			.placeNearTarget()
			.pointAt(util.vector.centerOf(3, 1, 4));
		scene.idle(70);
		scene.world.hideSection(util.select.fromTo(3, 2, 1, 3, 2, 5), Direction.SOUTH);

		scene.idle(15);
		scene.world.modifyBlock(util.grid.at(3, 2, 3), s -> s.setValue(ShaftBlock.AXIS, Axis.X), false);
		scene.world.setKineticSpeed(util.select.fromTo(1, 2, 3, 5, 2, 3), 16);
		scene.world.showSection(util.select.position(4, 2, 3), Direction.WEST);

		for (int i = 3; i >= 1; i--) {
			scene.idle(5);
			if (i == 3)
				scene.world.showSection(util.select.position(5, 2, 3), Direction.DOWN);
			scene.world.showSection(util.select.position(i, 2, 3), Direction.DOWN);
		}

		scene.idle(5);
		scene.overlay.showText(90)
			.text("It will help relaying conveyed speed to other axes of rotation")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 3), Direction.WEST));
		scene.effects.rotationSpeedIndicator(util.grid.at(3, 1, 3));
		scene.effects.rotationSpeedIndicator(util.grid.at(4, 2, 3));
		scene.idle(60);

	}

	public static void cogsSpeedUp(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("cog_speedup", "Gearshifting with Cogs");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(5, 1, 2, 4, 1, 2), Direction.DOWN);
		scene.idle(10);

		BlockPos lowerCog = util.grid.at(3, 1, 2);
		BlockPos upperCog = util.grid.at(3, 2, 3);
		BlockState largeCogState = AllBlocks.LARGE_COGWHEEL.getDefaultState()
			.setValue(CogWheelBlock.AXIS, Axis.X);
		BlockState smallCogState = AllBlocks.COGWHEEL.getDefaultState()
			.setValue(CogWheelBlock.AXIS, Axis.X);

		scene.world.setBlock(lowerCog, largeCogState, false);
		scene.world.setBlock(upperCog, smallCogState, false);
		BlockPos upperShaftEnd = upperCog.west(3);
		BlockPos lowerShaftEnd = lowerCog.west(3);

		scene.world.setKineticSpeed(util.select.fromTo(upperCog, upperShaftEnd), -64);
		scene.world.showSection(util.select.fromTo(lowerCog, upperCog), Direction.EAST);
		scene.overlay.showText(60)
			.text("Large and Small cogs can be connected diagonally")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(upperCog, Direction.WEST));
		scene.idle(80);

		Selection gaugesSelect = util.select.fromTo(0, 1, 2, 2, 2, 3);
		scene.world.showSection(gaugesSelect, Direction.DOWN);
		scene.overlay.showText(80)
			.text("Shifting from large to small cogs, the conveyed speed will be doubled")
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 3), Direction.NORTH));
		scene.idle(30);
		scene.effects.rotationSpeedIndicator(upperCog);
		scene.idle(60);

		scene.overlay.showText(30)
			.sharedText("rpm32")
			.colored(PonderPalette.FAST)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(upperShaftEnd, Direction.WEST));
		scene.idle(5);
		scene.overlay.showText(30)
			.sharedText("rpm16")
			.colored(PonderPalette.MEDIUM)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(lowerShaftEnd, Direction.WEST));
		scene.idle(45);

		scene.world.setKineticSpeed(util.select.fromTo(lowerCog, upperShaftEnd), 0);
		ElementLink<WorldSectionElement> cogs =
			scene.world.makeSectionIndependent(util.select.fromTo(lowerCog, upperCog));
		scene.world.moveSection(cogs, util.vector.of(0, 1, 0), 5);
		scene.idle(5);
		scene.world.rotateSection(cogs, 180, 0, 0, 10);
		scene.idle(10);
		scene.world.setBlock(lowerCog, smallCogState, false);
		scene.world.setBlock(upperCog, largeCogState, false);
		scene.world.rotateSection(cogs, 180, 0, 0, 0);
		scene.world.moveSection(cogs, util.vector.of(0, -1, 0), 5);
		scene.idle(5);

		scene.world.setKineticSpeed(util.select.fromTo(lowerCog, lowerShaftEnd), 32);
		scene.world.setKineticSpeed(util.select.fromTo(upperCog, upperShaftEnd), -16);

		scene.overlay.showText(80)
			.text("Shifting the opposite way, the conveyed speed will be halved")
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 3), Direction.NORTH));
		scene.idle(10);
		scene.effects.rotationSpeedIndicator(upperCog);
		scene.idle(80);

		scene.overlay.showText(60)
			.sharedText("rpm8")
			.colored(PonderPalette.SLOW)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(upperShaftEnd, Direction.WEST));
		scene.idle(5);
		scene.overlay.showText(60)
			.sharedText("rpm16")
			.colored(PonderPalette.MEDIUM)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(lowerShaftEnd, Direction.WEST));
		scene.idle(40);
	}

	public static void cogwheelsCanBeEncased(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("cogwheel_casing", "Encasing Cogwheels");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		Selection large1 = util.select.position(4, 1, 3);
		Selection small1 = util.select.fromTo(3, 1, 2, 3, 2, 2);
		Selection small2 = util.select.position(2, 1, 2);
		Selection large2 = util.select.fromTo(1, 1, 3, 1, 1, 4);
		Selection shaft2 = util.select.position(2, 2, 2);

		scene.world.setKineticSpeed(shaft2, 0);
		scene.idle(10);

		scene.world.showSection(large1, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(small1, Direction.DOWN);
		scene.world.showSection(small2, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(large2, Direction.EAST);
		scene.idle(20);

		BlockEntry<EncasedCogwheelBlock> andesiteEncased = AllBlocks.ANDESITE_ENCASED_COGWHEEL;
		ItemStack andesiteCasingItem = AllBlocks.ANDESITE_CASING.asStack();

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(3, 0, 2), Pointing.UP).rightClick()
			.withItem(andesiteCasingItem), 100);
		scene.idle(7);
		scene.world.setBlocks(util.select.position(3, 1, 2), andesiteEncased.getDefaultState()
			.setValue(EncasedCogwheelBlock.AXIS, Axis.Y)
			.setValue(EncasedCogwheelBlock.TOP_SHAFT, true), true);
		scene.world.setKineticSpeed(util.select.position(3, 1, 2), -32);
		scene.idle(15);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(2, 1, 2), Pointing.DOWN).rightClick()
			.withItem(andesiteCasingItem), 30);
		scene.idle(7);
		scene.world.setBlocks(small2, andesiteEncased.getDefaultState()
			.setValue(EncasedCogwheelBlock.AXIS, Axis.Y), true);
		scene.world.setKineticSpeed(small2, 32);
		scene.idle(15);

		BlockEntry<EncasedCogwheelBlock> brassEncased = AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL;
		ItemStack brassCasingItem = AllBlocks.BRASS_CASING.asStack();

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(1, 0, 3), Pointing.UP).rightClick()
			.withItem(brassCasingItem), 60);
		scene.idle(7);
		scene.world.setBlocks(util.select.position(1, 1, 3), brassEncased.getDefaultState()
			.setValue(EncasedCogwheelBlock.AXIS, Axis.Y), true);
		scene.world.setKineticSpeed(util.select.position(1, 1, 3), -16);

		scene.idle(10);
		scene.overlay.showText(70)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Brass or Andesite Casing can be used to decorate Cogwheels")
			.pointAt(util.vector.topOf(1, 1, 3));
		scene.idle(80);

		ElementLink<WorldSectionElement> shaftLink = scene.world.showIndependentSection(shaft2, Direction.DOWN);
		scene.idle(15);
		scene.overlay.showText(90)
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.text("Components added after encasing will not connect to the shaft outputs")
			.pointAt(util.vector.centerOf(2, 2, 2));
		scene.idle(90);

		scene.world.moveSection(shaftLink, new Vec3(0, .5f, 0), 10);
		scene.idle(10);

		scene.addKeyframe();
		Vec3 wrenchHere = util.vector.topOf(2, 1, 2)
			.add(.25, 0, -.25);
		scene.overlay.showControls(new InputWindowElement(wrenchHere, Pointing.RIGHT).rightClick()
			.withItem(AllItems.WRENCH.asStack()), 25);
		scene.idle(7);
		scene.world.cycleBlockProperty(util.grid.at(2, 1, 2), EncasedCogwheelBlock.TOP_SHAFT);
		scene.idle(15);
		scene.world.moveSection(shaftLink, new Vec3(0, -.5f, 0), 10);
		scene.idle(10);
		scene.world.setKineticSpeed(shaft2, 32);
		scene.effects.rotationDirectionIndicator(util.grid.at(2, 2, 2));
		scene.idle(20);

		scene.overlay.showText(90)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.text("The Wrench can be used to toggle connections")
			.pointAt(wrenchHere.add(-.5, 0, .5));
		scene.idle(40);

		scene.overlay.showControls(new InputWindowElement(wrenchHere, Pointing.RIGHT).rightClick()
			.withItem(AllItems.WRENCH.asStack()), 25);
		scene.idle(7);
		scene.world.cycleBlockProperty(util.grid.at(2, 1, 2), EncasedCogwheelBlock.TOP_SHAFT);
		scene.world.setKineticSpeed(shaft2, 0);
	}

	public static void gearbox(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("gearbox", "Relaying rotational force using Gearboxes");
		scene.configureBasePlate(1, 1, 5);
		scene.setSceneOffsetY(-1);

		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.world.showSection(util.select.fromTo(4, 1, 6, 3, 2, 5), Direction.UP);
		scene.idle(10);

		BlockPos largeCogBack = util.grid.at(3, 2, 4);
		BlockPos largeCogLeft = util.grid.at(4, 2, 3);
		BlockPos largeCogFront = util.grid.at(3, 2, 2);
		BlockPos largeCogRight = util.grid.at(2, 2, 3);

		scene.world.showSection(util.select.position(largeCogBack), Direction.SOUTH);
		scene.idle(5);
		scene.world.showSection(util.select.position(largeCogLeft), Direction.WEST);
		scene.world.showSection(util.select.position(largeCogLeft.east()), Direction.WEST);
		scene.world.showSection(util.select.position(largeCogRight), Direction.EAST);
		scene.world.showSection(util.select.position(largeCogRight.west()), Direction.EAST);
		scene.idle(5);
		scene.world.showSection(util.select.position(largeCogFront), Direction.SOUTH);
		scene.world.showSection(util.select.position(largeCogFront.north()), Direction.SOUTH);

		scene.idle(10);

		scene.overlay.showText(80)
			.colored(PonderPalette.RED)
			.pointAt(util.vector.blockSurface(largeCogRight.west(), Direction.WEST))
			.placeNearTarget()
			.text("Jumping between axes of rotation can get bulky quickly");
		scene.idle(80);
		Selection gearbox = util.select.position(3, 2, 3);
		scene.world.hideSection(util.select.fromTo(4, 2, 2, 2, 2, 4)
			.substract(gearbox), Direction.UP);
		scene.idle(20);

		BlockState defaultState = AllBlocks.SHAFT.getDefaultState();
		BlockState cogState = AllBlocks.COGWHEEL.getDefaultState();
		scene.world.setBlock(largeCogBack, defaultState.setValue(CogWheelBlock.AXIS, Axis.Z), false);
		scene.world.setBlock(largeCogFront, defaultState.setValue(CogWheelBlock.AXIS, Axis.Z), false);
		scene.world.setBlock(largeCogRight, defaultState.setValue(CogWheelBlock.AXIS, Axis.X), false);
		scene.world.setBlock(largeCogLeft, defaultState.setValue(CogWheelBlock.AXIS, Axis.X), false);
		scene.world.showSection(util.select.fromTo(4, 2, 2, 2, 2, 4), Direction.DOWN);

		scene.idle(20);
		scene.overlay.showText(80)
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.topOf(3, 2, 3))
			.placeNearTarget()
			.attachKeyFrame()
			.text("A gearbox is the more compact equivalent of this setup");

		scene.idle(90);
		scene.world.setBlock(largeCogFront.north(), cogState.setValue(CogWheelBlock.AXIS, Axis.Z), true);
		scene.world.setBlock(largeCogRight.west(), cogState.setValue(CogWheelBlock.AXIS, Axis.X), true);
		scene.idle(10);
		scene.effects.rotationDirectionIndicator(largeCogFront.north());
		scene.effects.rotationDirectionIndicator(largeCogRight.west());
		scene.idle(15);
		scene.overlay.showText(60)
			.pointAt(util.vector.of(3, 2.5, 3))
			.placeNearTarget()
			.text("Shafts around corners rotate in mirrored directions");

		scene.idle(70);

		scene.world.hideSection(util.select.fromTo(1, 2, 3, 2, 2, 3), Direction.WEST);
		scene.world.hideSection(util.select.fromTo(4, 2, 3, 5, 2, 3), Direction.EAST);
		scene.world.setBlock(largeCogBack.south(), cogState.setValue(CogWheelBlock.AXIS, Axis.Z), true);
		scene.idle(10);

		scene.effects.rotationDirectionIndicator(largeCogFront.north());
		scene.effects.rotationDirectionIndicator(largeCogBack.south());
		scene.idle(15);
		scene.overlay.showText(60)
			.pointAt(util.vector.centerOf(3, 2, 5))
			.placeNearTarget()
			.text("Straight connections will be reversed");

	}

	public static void clutch(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("clutch", "Controlling rotational force using a Clutch");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		BlockPos leverPos = util.grid.at(3, 1, 0);
		scene.world.showSection(util.select.fromTo(leverPos, leverPos.south()), Direction.UP);

		BlockPos gaugePos = util.grid.at(0, 1, 2);
		Selection gauge = util.select.position(gaugePos);
		scene.world.showSection(gauge, Direction.UP);
		scene.world.setKineticSpeed(gauge, 0);

		scene.idle(5);
		scene.world.showSection(util.select.position(5, 1, 2), Direction.DOWN);
		scene.idle(10);

		for (int i = 4; i >= 1; i--) {
			scene.idle(5);
			scene.world.showSection(util.select.position(i, 1, 2), Direction.DOWN);
		}

		BlockPos clutch = util.grid.at(3, 1, 2);

		scene.world.setKineticSpeed(gauge, 32);
		scene.effects.indicateSuccess(gaugePos);
		scene.idle(10);
		scene.overlay.showText(50)
			.text("Clutches will relay rotation in a straight line")
			.placeNearTarget()
			.pointAt(util.vector.topOf(clutch));

		scene.idle(60);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.south(2)));
		scene.effects.indicateRedstone(leverPos);
		scene.world.setKineticSpeed(util.select.fromTo(0, 1, 2, 2, 1, 2), 0);
		scene.idle(10);

		scene.idle(10);
		scene.overlay.showText(50)
			.colored(PonderPalette.RED)
			.text("When powered by Redstone, it breaks the connection")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(clutch));

		scene.idle(70);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.south(2)));
		scene.effects.indicateRedstone(leverPos);
		scene.world.setKineticSpeed(util.select.fromTo(0, 1, 2, 2, 1, 2), 32);
		scene.effects.indicateSuccess(gaugePos);
	}

	public static void gearshift(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("gearshift", "Controlling rotational force using a Gearshift");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		BlockPos leverPos = util.grid.at(3, 1, 0);
		scene.world.showSection(util.select.fromTo(leverPos, leverPos.south()), Direction.UP);

		scene.idle(5);
		scene.world.showSection(util.select.position(5, 1, 2), Direction.DOWN);
		scene.idle(10);

		for (int i = 4; i >= 1; i--) {
			scene.idle(5);
			scene.world.showSection(util.select.position(i, 1, 2), Direction.DOWN);
		}

		BlockPos gearshift = util.grid.at(3, 1, 2);
		scene.idle(10);
		scene.overlay.showText(50)
			.placeNearTarget()
			.text("Gearshifts will relay rotation in a straight line")
			.pointAt(util.vector.topOf(gearshift));

		scene.idle(60);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.south(2)));
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(util.select.fromTo(0, 1, 2, 2, 2, 2), f -> -f);
		scene.effects.rotationDirectionIndicator(gearshift.east(2));
		scene.effects.rotationDirectionIndicator(gearshift.west(2));
		scene.idle(30);

		scene.overlay.showText(50)
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.text("When powered by Redstone, it reverses the transmission")
			.attachKeyFrame()
			.pointAt(util.vector.topOf(gearshift));

		for (int i = 0; i < 3; i++) {
			scene.idle(60);
			scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.south(2)));
			scene.effects.indicateRedstone(leverPos);
			scene.world.modifyKineticSpeed(util.select.fromTo(0, 1, 2, 2, 2, 2), f -> -f);
			scene.effects.rotationDirectionIndicator(gearshift.east(2));
			scene.effects.rotationDirectionIndicator(gearshift.west(2));
		}
	}

	public static void creativeMotor(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("creative_motor", "Generating Rotational Force using Creative Motors");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos motor = util.grid.at(3, 1, 2);

		for (int i = 0; i < 3; i++) {
			scene.idle(5);
			scene.world.showSection(util.select.position(1 + i, 1, 2), Direction.DOWN);
		}

		scene.idle(10);
		scene.effects.rotationSpeedIndicator(motor);
		scene.overlay.showText(50)
			.text("Creative motors are a compact and configurable source of Rotational Force")
			.placeNearTarget()
			.pointAt(util.vector.topOf(motor));
		scene.idle(70);

		Vec3 blockSurface = util.vector.blockSurface(motor, Direction.NORTH)
			.add(1 / 16f, 0, 3 / 16f);
		scene.overlay.showFilterSlotInput(blockSurface, Direction.NORTH, 80);
		scene.overlay.showControls(new InputWindowElement(blockSurface, Pointing.DOWN).rightClick(), 60);
		scene.idle(20);

		scene.overlay.showText(60)
			.text("The generated speed can be configured on its input panels")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(blockSurface);
		scene.idle(10);
		scene.idle(50);
		scene.world.modifyKineticSpeed(util.select.fromTo(1, 1, 2, 3, 1, 2), f -> 4 * f);
		scene.idle(10);

		scene.effects.rotationSpeedIndicator(motor);
	}

	public static void waterWheel(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("water_wheel", "Generating Rotational Force using Water Wheels");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 3, 3, 2, 3), Direction.DOWN);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);

		BlockPos gaugePos = util.grid.at(1, 2, 2);

		for (int i = 0; i < 4; i++) {
			scene.idle(5);
			scene.world.showSection(util.select.fromTo(gaugePos.east(i)
				.below(), gaugePos.east(i)), Direction.DOWN);
		}

		scene.idle(10);

		for (int i = 0; i < 2; i++) {
			scene.idle(5);
			scene.world.showSection(util.select.position(3, 3, 3 - i), Direction.DOWN);
		}
		scene.world.setKineticSpeed(util.select.everywhere(), -8);
		scene.effects.indicateSuccess(gaugePos);

		BlockPos wheel = util.grid.at(3, 2, 2);
		scene.effects.rotationSpeedIndicator(wheel);
		scene.overlay.showText(60)
			.text("Water Wheels draw force from adjacent Water Currents")
			.placeNearTarget()
			.pointAt(util.vector.topOf(wheel));
		scene.idle(10);

		AABB bb = new AABB(wheel).inflate(1 / 16f, 0, 0);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.move(0, 1, 0)
			.contract(0, .75, 0), 80);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.move(0, 0, -1)
			.contract(0, 0, -.75), 75);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.move(0, -1, 0)
			.contract(0, -.75, 0), 70);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.move(0, 0, 1)
			.contract(0, 0, .75), 65);
		scene.idle(75);

		scene.addKeyframe();
		scene.world.showSection(util.select.position(3, 3, 1), Direction.DOWN);
		for (int i = 0; i < 2; i++) {
			scene.idle(5);
			scene.world.showSection(util.select.position(3, 2 - i, 1), Direction.DOWN);
		}

		scene.idle(10);
		scene.overlay.showText(50)
			.text("Covering additional sides will not improve its kinetic output further")
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(wheel, Direction.NORTH));

		scene.idle(80);

		scene.addKeyframe();

		ElementLink<WorldSectionElement> water = scene.world.makeSectionIndependent(util.select.fromTo(3, 1, 1, 3, 3, 1)
			.add(util.select.fromTo(3, 3, 2, 3, 3, 3)));
		scene.world.moveSection(water, util.vector.of(1, 0.5, -0.5), 15);
		scene.idle(5);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);

		scene.idle(5);
		ItemStack crimsonPlanks = new ItemStack(Items.CRIMSON_PLANKS);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(wheel), Pointing.DOWN).rightClick()
			.withItem(crimsonPlanks), 20);
		scene.idle(7);
		scene.world.modifyBlockEntity(wheel, WaterWheelBlockEntity.class, be -> be.applyMaterialIfValid(crimsonPlanks));
		scene.overlay.showText(50)
			.text("Use wood planks on the wheel to change its appearance")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(wheel, Direction.WEST));
		scene.idle(40);

		ItemStack birchPlanks = new ItemStack(Items.BIRCH_PLANKS);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(wheel), Pointing.DOWN).rightClick()
			.withItem(birchPlanks), 20);
		scene.idle(7);
		scene.world.modifyBlockEntity(wheel, WaterWheelBlockEntity.class, be -> be.applyMaterialIfValid(birchPlanks));
		scene.idle(40);

		ItemStack junglePlanks = new ItemStack(Items.JUNGLE_PLANKS);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(wheel), Pointing.DOWN).rightClick()
			.withItem(junglePlanks), 20);
		scene.idle(7);
		scene.world.modifyBlockEntity(wheel, WaterWheelBlockEntity.class, be -> be.applyMaterialIfValid(junglePlanks));
		scene.idle(20);

		scene.world.moveSection(water, util.vector.of(-1, -0.5, 0.5), 15);
		scene.idle(10);
		scene.world.setKineticSpeed(util.select.everywhere(), -8);
		scene.effects.indicateSuccess(gaugePos);
	}

	public static void largeWaterWheel(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("large_water_wheel", "Generating Rotational Force using Large Water Wheels");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0)
			.substract(util.select.position(3, 0, 0)), Direction.UP);
		ElementLink<WorldSectionElement> strip =
			scene.world.showIndependentSection(util.select.fromTo(1, 0, 0, 1, 0, 4), Direction.UP);
		scene.world.moveSection(strip, util.vector.of(2, 0, 0), 0);
		scene.idle(10);
		scene.world.showSection(util.select.fromTo(3, 1, 3, 3, 2, 3), Direction.DOWN);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);

		BlockPos gaugePos = util.grid.at(1, 1, 2);

		for (int i = 0; i < 4; i++) {
			scene.idle(5);
			if (i == 0)
				scene.world.hideIndependentSection(strip, Direction.DOWN);
			scene.world.showSection(util.select.position(gaugePos.east(i)), Direction.DOWN);
		}

		scene.idle(10);

		for (int i = 0; i < 3; i++) {
			scene.idle(5);
			scene.world.showSection(util.select.position(3, 3, 3 - i), Direction.DOWN);
		}
		scene.world.setKineticSpeed(util.select.everywhere(), -4);
		scene.effects.indicateSuccess(gaugePos);

		BlockPos wheel = util.grid.at(3, 1, 2);
		scene.effects.rotationSpeedIndicator(wheel);
		scene.overlay.showText(60)
			.text("Large Water Wheels draw force from adjacent Water Currents")
			.placeNearTarget()
			.pointAt(util.vector.topOf(wheel));
		scene.idle(10);

		AABB bb = new AABB(wheel).inflate(.125, 1, 1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.move(0, 3, 0)
			.contract(0, 2.75, 0), 80);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.move(0, 0, -3)
			.contract(0, 0, -2.75), 75);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.move(0, -3, 0)
			.contract(0, -2.75, 0), 70);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.move(0, 0, 3)
			.contract(0, 0, 2.75), 65);
		scene.idle(75);

		scene.addKeyframe();
		scene.world.showSection(util.select.position(3, 3, 0), Direction.DOWN);
		for (int i = 0; i < 3; i++) {
			scene.idle(5);
			scene.world.showSection(util.select.position(3, 2 - i, 0), Direction.DOWN);
		}

		scene.idle(10);
		scene.overlay.showText(50)
			.text("Covering additional sides will not improve its kinetic output further")
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(wheel, Direction.NORTH));

		scene.idle(80);

		scene.idle(10);
		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("These rotate only at half the speed of regular water wheels...")
			.colored(PonderPalette.WHITE)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(gaugePos, Direction.NORTH));

		scene.idle(78);
		scene.overlay.showText(60)
			.text("...but provide a substantially higher stress capacity")
			.colored(PonderPalette.WHITE)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(gaugePos, Direction.WEST));

		scene.idle(80);

		scene.addKeyframe();

		ElementLink<WorldSectionElement> water = scene.world.makeSectionIndependent(util.select.fromTo(3, 0, 0, 3, 3, 0)
			.add(util.select.fromTo(3, 3, 1, 3, 3, 3)));
		scene.world.moveSection(water, util.vector.of(1, 0.5, -0.5), 15);
		scene.idle(5);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);

		BlockPos target = wheel.south()
			.above();

		scene.idle(5);
		ItemStack crimsonPlanks = new ItemStack(Items.CRIMSON_PLANKS);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(target), Pointing.DOWN).rightClick()
			.withItem(crimsonPlanks), 20);
		scene.idle(7);
		scene.world.modifyBlockEntity(wheel, WaterWheelBlockEntity.class, be -> be.applyMaterialIfValid(crimsonPlanks));
		scene.overlay.showText(50)
			.text("Use wood planks on the wheel to change its appearance")
			.colored(PonderPalette.BLUE)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(target, Direction.WEST));
		scene.idle(40);

		ItemStack birchPlanks = new ItemStack(Items.BIRCH_PLANKS);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(target), Pointing.DOWN).rightClick()
			.withItem(birchPlanks), 20);
		scene.idle(7);
		scene.world.modifyBlockEntity(wheel, WaterWheelBlockEntity.class, be -> be.applyMaterialIfValid(birchPlanks));
		scene.idle(40);

		ItemStack junglePlanks = new ItemStack(Items.JUNGLE_PLANKS);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(target), Pointing.DOWN).rightClick()
			.withItem(junglePlanks), 20);
		scene.idle(7);
		scene.world.modifyBlockEntity(wheel, WaterWheelBlockEntity.class, be -> be.applyMaterialIfValid(junglePlanks));
		scene.idle(20);

		scene.world.moveSection(water, util.vector.of(-1, -0.5, 0.5), 15);
		scene.idle(10);
		scene.world.setKineticSpeed(util.select.everywhere(), -4);
		scene.effects.indicateSuccess(gaugePos);
	}

	public static void handCrank(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("hand_crank", "Generating Rotational Force using Hand Cranks");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		BlockPos gaugePos = util.grid.at(1, 3, 3);
		BlockPos handlePos = util.grid.at(2, 2, 2);
		Selection handleSelect = util.select.position(handlePos);

		scene.world.showSection(util.select.layersFrom(1)
			.substract(handleSelect), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(handleSelect, Direction.DOWN);
		scene.idle(20);

		Vec3 centerOf = util.vector.centerOf(handlePos);
		Vec3 sideOf = centerOf.add(-0.5, 0, 0);

		scene.overlay.showText(70)
			.text("Hand Cranks can be used by players to apply rotational force manually")
			.placeNearTarget()
			.pointAt(sideOf);
		scene.idle(80);

		scene.overlay.showControls(new InputWindowElement(centerOf, Pointing.DOWN).rightClick(), 40);
		scene.idle(7);
		scene.world.setKineticSpeed(util.select.everywhere(), 32);
		scene.world.modifyKineticSpeed(util.select.column(1, 3), f -> f * -2);
		scene.effects.rotationDirectionIndicator(handlePos);
		scene.effects.indicateSuccess(gaugePos);
		scene.idle(10);
		scene.overlay.showText(50)
			.text("Hold Right-Click to rotate it Counter-Clockwise")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(sideOf);

		scene.idle(35);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		scene.idle(15);

		scene.overlay.showControls(new InputWindowElement(centerOf, Pointing.DOWN).rightClick()
			.whileSneaking(), 40);
		scene.idle(7);
		scene.world.setKineticSpeed(util.select.everywhere(), -32);
		scene.world.modifyKineticSpeed(util.select.column(1, 3), f -> f * -2);
		scene.effects.rotationDirectionIndicator(handlePos);
		scene.effects.indicateSuccess(gaugePos);
		scene.idle(10);
		scene.overlay.showText(90)
			.text("Sneak and Hold Right-Click to rotate it Clockwise")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(sideOf);

		scene.idle(35);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		scene.idle(45);
	}

	public static void valveHandle(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("valve_handle", "Precise rotation using Valve Handles");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		Selection armS = util.select.fromTo(3, 2, 3, 1, 2, 3);
		BlockPos bearing = util.grid.at(2, 2, 2);
		BlockPos valvePos = util.grid.at(2, 2, 1);
		Vec3 centerOf = util.vector.centerOf(valvePos);
		Vec3 sideOf = centerOf.add(-0.5, 0, 0);
		Vec3 topOf = centerOf.add(0, 0.5, 0);

		scene.world.showSection(util.select.fromTo(bearing, bearing.below()), Direction.DOWN);
		scene.idle(3);
		ElementLink<WorldSectionElement> contraption = scene.world.showIndependentSection(armS, Direction.NORTH);
		scene.idle(3);
		ElementLink<WorldSectionElement> valve =
			scene.world.showIndependentSection(util.select.position(valvePos), Direction.SOUTH);
		scene.world.rotateSection(valve, 0, 0, 45, 0);
		scene.idle(20);

		scene.overlay.showText(70)
			.text("Valve handles can be used to rotate components by a precise angle")
			.placeNearTarget()
			.pointAt(sideOf);

		scene.idle(20);
		scene.world.rotateSection(valve, 0, 0, 45, 15);
		scene.world.rotateSection(contraption, 0, 0, 45, 15);
		scene.world.rotateBearing(bearing, 45, 15);
		scene.world.setKineticSpeed(util.select.everywhere(), 16);
		scene.idle(15);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		scene.idle(60);

		Vec3 blockSurface = util.vector.centerOf(valvePos)
			.add(0, 0, 4 / 16f);
		AABB point = new AABB(blockSurface, blockSurface);
		AABB expanded = point.inflate(1 / 8f, 1 / 8f, 1 / 16f);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, blockSurface, point, 1);
		scene.idle(1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, blockSurface, expanded, 80);
		scene.overlay.showControls(new InputWindowElement(blockSurface, Pointing.DOWN).rightClick(), 60);
		scene.idle(10);

		scene.overlay.showText(60)
			.text("The angle can be configured on the input panel")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(blockSurface);

		scene.idle(70);
		scene.overlay.showControls(new InputWindowElement(topOf, Pointing.DOWN).rightClick(), 40);
		scene.idle(7);
		scene.world.rotateSection(valve, 0, 0, 90, 30);
		scene.world.rotateSection(contraption, 0, 0, 90, 30);
		scene.world.rotateBearing(bearing, 90, 30);
		scene.world.setKineticSpeed(util.select.everywhere(), 16);

		scene.idle(10);
		scene.overlay.showText(40)
			.text("Right-Click to activate one rotation")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(sideOf);

		scene.idle(20);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);

		scene.idle(25);
		scene.overlay.showControls(new InputWindowElement(topOf, Pointing.DOWN).rightClick()
			.whileSneaking(), 40);
		scene.idle(7);
		scene.world.rotateSection(valve, 0, 0, -90, 30);
		scene.world.rotateSection(contraption, 0, 0, -90, 30);
		scene.world.rotateBearing(bearing, -90, 30);
		scene.world.setKineticSpeed(util.select.everywhere(), -16);

		scene.idle(10);
		scene.overlay.showText(50)
			.text("Sneak-Right-Click to activate it in the opposite direction")
			.placeNearTarget()
			.pointAt(sideOf);

		scene.idle(15);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		scene.idle(40);

		blockSurface = util.vector.topOf(bearing)
			.add(0, 0, -1 / 8f);
		point = new AABB(blockSurface, blockSurface);
		expanded = point.inflate(1 / 8f, 0, 1 / 8f);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, blockSurface, point, 1);
		scene.idle(1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, blockSurface, expanded, 80);
		scene.idle(10);
		scene.overlay.showText(70)
			.text("Mind that Bearings have to be specifically told not to disassemble")
			.placeNearTarget()
			.pointAt(blockSurface);

		scene.idle(90);

		scene.addKeyframe();
		scene.overlay.showControls(new InputWindowElement(topOf, Pointing.DOWN).rightClick()
			.withItem(new ItemStack(Items.BLUE_DYE)), 40);
		scene.idle(7);
		scene.world.modifyBlock(valvePos, s -> AllBlocks.DYED_VALVE_HANDLES.get(DyeColor.BLUE)
			.getDefaultState()
			.setValue(ValveHandleBlock.FACING, Direction.NORTH), true);
		scene.idle(10);
		scene.overlay.showText(70)
			.text("Valve handles can be dyed for aesthetic purposes")
			.placeNearTarget()
			.colored(PonderPalette.BLUE)
			.pointAt(sideOf);
		scene.idle(60);
	}

	public static void sequencedGearshift(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("sequenced_gearshift", "Controlling Rotational Speed using Sequenced Gearshifts");
		scene.configureBasePlate(1, 0, 5);
		scene.showBasePlate();

		Selection redstone = util.select.fromTo(3, 1, 0, 3, 1, 1);

		scene.world.showSection(util.select.position(6, 0, 3)
			.add(redstone), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(6, 1, 2, 4, 1, 2), Direction.DOWN);

		BlockPos gearshiftPos = util.grid.at(3, 1, 2);
		Selection gearshiftSelection = util.select.position(gearshiftPos);
		BlockPos bearingPos = util.grid.at(1, 1, 2);
		BlockPos buttonPos = util.grid.at(3, 1, 0);
		Selection outputKinetics = util.select.fromTo(3, 1, 2, 1, 1, 2);

		scene.world.setKineticSpeed(gearshiftSelection, 0);
		scene.idle(10);

		scene.world.showSection(gearshiftSelection, Direction.DOWN);
		scene.idle(10);

		scene.world.showSection(util.select.fromTo(2, 1, 2, 1, 1, 2), Direction.EAST);
		scene.idle(10);

		Vec3 top = util.vector.topOf(gearshiftPos);
		scene.overlay.showText(60)
			.text("Seq. Gearshifts relay rotation by following a timed list of instructions")
			.attachKeyFrame()
			.pointAt(top)
			.placeNearTarget();
		scene.idle(80);

		scene.overlay.showControls(new InputWindowElement(top, Pointing.DOWN).rightClick(), 40);
		scene.idle(7);
		scene.overlay.showSelectionWithText(gearshiftSelection, 50)
			.colored(PonderPalette.BLUE)
			.text("Right-click it to open the Configuration UI")
			.pointAt(top)
			.placeNearTarget();
		scene.idle(60);

		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.fromTo(0, 3, 2, 0, 0, 2), Direction.EAST);
		scene.world.configureCenterOfRotation(contraption, util.vector.centerOf(bearingPos));

		scene.idle(20);
		scene.world.toggleRedstonePower(redstone);
		scene.effects.indicateRedstone(buttonPos);
		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, 16);
		scene.world.rotateBearing(bearingPos, 90, 40);
		scene.world.rotateSection(contraption, 90, 0, 0, 40);
		scene.effects.rotationDirectionIndicator(gearshiftPos.west());
		scene.idle(20);
		scene.world.toggleRedstonePower(redstone);
		scene.idle(20);

		scene.overlay.showText(80)
			.text("Upon receiving a Redstone Signal, it will start running its configured sequence")
			.attachKeyFrame()
			.pointAt(top);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, -32);
		scene.world.rotateBearing(bearingPos, -180, 40);
		scene.world.rotateSection(contraption, -180, 0, 0, 40);
		scene.effects.rotationDirectionIndicator(gearshiftPos.west());
		scene.idle(40);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, 0);
		scene.idle(20);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, 16);
		scene.world.rotateBearing(bearingPos, 90, 40);
		scene.world.rotateSection(contraption, 90, 0, 0, 40);
		scene.effects.rotationDirectionIndicator(gearshiftPos.west());
		scene.idle(40);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, 0);

		scene.idle(20);
		scene.overlay.showText(70)
			.text("Once finished, it waits for the next Redstone Signal and starts over")
			.pointAt(util.vector.topOf(util.grid.at(3, 0, 1)));
		scene.idle(80);

		scene.idle(20);
		scene.world.toggleRedstonePower(redstone);
		scene.effects.indicateRedstone(buttonPos);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, 16);
		scene.world.rotateBearing(bearingPos, 90, 40);
		scene.world.rotateSection(contraption, 90, 0, 0, 40);
		scene.effects.rotationDirectionIndicator(gearshiftPos.west());
		scene.idle(20);

		scene.overlay.showText(60)
			.text("A redstone comparator can be used to read the current progress")
			.attachKeyFrame()
			.pointAt(util.vector.topOf(util.grid.at(3, 0, 1)));

		scene.world.hideSection(redstone, Direction.NORTH);
		scene.idle(15);

		BlockPos wire = util.grid.at(5, 1, 0);
		Selection nixie = util.select.position(4, 1, 0);
		scene.world.cycleBlockProperty(util.grid.at(4, 1, 0), NixieTubeBlock.FACING);
		scene.world.cycleBlockProperty(util.grid.at(4, 1, 0), NixieTubeBlock.FACING);

		ElementLink<WorldSectionElement> comparator =
			scene.world.showIndependentSection(util.select.fromTo(5, 1, 1, 4, 1, 0), Direction.SOUTH);
		scene.world.moveSection(comparator, util.vector.of(-2, 0, 0), 0);
		scene.world.toggleRedstonePower(util.select.position(5, 1, 1));
		scene.world.cycleBlockProperty(wire, RedStoneWireBlock.POWER);
		scene.world.modifyBlockEntityNBT(nixie, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 1));

		scene.idle(5);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, -32);
		scene.world.rotateBearing(bearingPos, -180, 40);
		scene.world.rotateSection(contraption, -180, 0, 0, 40);
		scene.effects.rotationDirectionIndicator(gearshiftPos.west());
		scene.world.cycleBlockProperty(wire, RedStoneWireBlock.POWER);
		scene.world.modifyBlockEntityNBT(nixie, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 2));
		scene.idle(40);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, 0);
		scene.world.cycleBlockProperty(wire, RedStoneWireBlock.POWER);
		scene.world.modifyBlockEntityNBT(nixie, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 3));
		scene.idle(20);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, 16);
		scene.world.rotateBearing(bearingPos, 90, 40);
		scene.world.rotateSection(contraption, 90, 0, 0, 40);
		scene.effects.rotationDirectionIndicator(gearshiftPos.west());
		scene.world.cycleBlockProperty(wire, RedStoneWireBlock.POWER);
		scene.world.modifyBlockEntityNBT(nixie, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 4));
		scene.idle(40);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.modifyBlock(wire, s -> s.setValue(RedStoneWireBlock.POWER, 0), false);
		scene.world.toggleRedstonePower(util.select.position(5, 1, 1));
		scene.world.modifyBlockEntityNBT(nixie, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 0));
		scene.world.setKineticSpeed(outputKinetics, 0);
	}

	public static void speedController(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("rotation_speed_controller", "Using the Rotational Speed Controller");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		BlockPos cogPos = util.grid.at(1, 2, 1);
		Selection gaugeSelect = util.select.position(1, 2, 3);

		scene.world.multiplyKineticSpeed(util.select.everywhere(), 0.5f);
		scene.world.setKineticSpeed(gaugeSelect, 0);
		scene.world.showSection(util.select.fromTo(5, 1, 1, 2, 1, 1), Direction.DOWN);
		scene.world.showSection(util.select.fromTo(1, 1, 3, 1, 2, 3), Direction.DOWN);
		scene.idle(10);
		ElementLink<WorldSectionElement> rsc =
			scene.world.showIndependentSection(util.select.position(0, 1, 1), Direction.DOWN);
		scene.world.moveSection(rsc, util.vector.of(1, 0, 0), 0);
		ElementLink<WorldSectionElement> rsc2 =
			scene.world.showIndependentSection(util.select.position(1, 1, 1), Direction.DOWN);
		scene.world.moveSection(rsc2, util.vector.of(0, -100, 0), 0);
		scene.idle(10);
		scene.world.showSection(util.select.position(1, 2, 1), Direction.DOWN);
		scene.idle(15);
		scene.effects.indicateSuccess(cogPos);
		scene.world.moveSection(rsc2, util.vector.of(0, 100, 0), 0);
		scene.world.moveSection(rsc, util.vector.of(0, -100, 0), 0);
		scene.idle(5);
		scene.world.showSection(util.select.position(1, 2, 2), Direction.DOWN);
		scene.idle(10);
		scene.world.setKineticSpeed(gaugeSelect, 8);
		scene.effects.indicateSuccess(util.grid.at(1, 2, 3));

		scene.overlay.showText(90)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(cogPos, Direction.NORTH))
			.text("Rot. Speed Controllers relay rotation from their axis to a Large Cogwheel above them");
		scene.idle(100);

		Vec3 inputVec = util.vector.of(1.5, 1.75 - 1 / 16f, 1);
		scene.overlay.showFilterSlotInput(inputVec, Direction.NORTH, 60);

		scene.overlay.showText(70)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(inputVec)
			.text("Using the value panel on its side, the conveyed speed can be configured");
		scene.idle(80);

		InputWindowElement input = new InputWindowElement(inputVec, Pointing.UP).rightClick();
		scene.overlay.showControls(input, 40);
		scene.idle(15);
		scene.world.multiplyKineticSpeed(util.select.fromTo(1, 2, 1, 1, 2, 3), 4);
		scene.effects.rotationSpeedIndicator(cogPos);
		scene.idle(55);
		scene.markAsFinished();

		scene.overlay.showControls(input, 30);
		scene.idle(15);
		scene.world.multiplyKineticSpeed(util.select.fromTo(1, 2, 1, 1, 2, 3), 4);
		scene.effects.rotationSpeedIndicator(cogPos);
		scene.idle(55);

		scene.overlay.showControls(input, 30);
		scene.idle(15);
		scene.world.multiplyKineticSpeed(util.select.fromTo(1, 2, 1, 1, 2, 3), -.05f);
		scene.effects.rotationSpeedIndicator(cogPos);
		scene.idle(35);
	}

	public static void speedometer(SceneBuilder scene, SceneBuildingUtil util) {
		gauge(scene, util, true);
	}

	public static void stressometer(SceneBuilder scene, SceneBuildingUtil util) {
		gauge(scene, util, false);
	}

	private static void gauge(SceneBuilder builder, SceneBuildingUtil util, boolean speed) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		String component = speed ? "Speedometer" : "Stressometer";
		String title = "Monitoring Kinetic information using the " + component;
		scene.title(speed ? "speedometer" : "stressometer", title);
		scene.configureBasePlate(1, 0, 5);

		BlockPos gaugePos = util.grid.at(2, 1, 3);

		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		for (int x = 6; x >= 0; x--) {
			scene.idle(2);
			scene.world.showSection(util.select.position(x, 1, 3), Direction.DOWN);
		}
		scene.idle(10);

		scene.world.setBlock(gaugePos, (speed ? AllBlocks.SPEEDOMETER : AllBlocks.STRESSOMETER).getDefaultState()
			.setValue(GaugeBlock.FACING, Direction.UP), true);
		scene.world.setKineticSpeed(util.select.position(gaugePos), 32);
		scene.idle(10);

		scene.overlay.showText(80)
			.text("The " + component + " displays the current " + (speed ? "Speed" : "Stress Capacity")
				+ (speed ? " of attached components" : " of the attached kinetic network"))
			.attachKeyFrame()
			.pointAt(util.vector.topOf(gaugePos))
			.placeNearTarget();
		scene.idle(90);

		if (speed) {
			scene.world.multiplyKineticSpeed(util.select.everywhere(), 4);
			scene.effects.rotationSpeedIndicator(util.grid.at(6, 1, 3));
			scene.idle(5);
			scene.effects.indicateSuccess(gaugePos);

		} else {
			BlockState state = AllBlocks.CRUSHING_WHEEL.getDefaultState()
				.setValue(CrushingWheelBlock.AXIS, Axis.X);
			scene.world.setBlock(util.grid.at(5, 1, 3), state, true);
			scene.world.setKineticSpeed(util.select.position(5, 1, 3), 32);
			scene.world.modifyBlockEntityNBT(util.select.position(gaugePos), StressGaugeBlockEntity.class,
				nbt -> nbt.putFloat("Value", .5f));
			scene.effects.indicateRedstone(gaugePos);
			scene.idle(20);
			scene.world.setBlock(util.grid.at(4, 1, 3), state, true);
			scene.world.setKineticSpeed(util.select.position(4, 1, 3), 32);
			scene.world.modifyBlockEntityNBT(util.select.position(gaugePos), StressGaugeBlockEntity.class,
				nbt -> nbt.putFloat("Value", .9f));
			scene.effects.indicateRedstone(gaugePos);
			scene.idle(10);
		}

		scene.idle(30);

		Vec3 blockSurface = util.vector.blockSurface(gaugePos, Direction.NORTH);
		scene.overlay.showControls(
			new InputWindowElement(blockSurface, Pointing.RIGHT).withItem(AllItems.GOGGLES.asStack()), 80);
		scene.idle(7);
		scene.overlay.showText(80)
			.text("When wearing Engineers' Goggles, the player can get more detailed information from the Gauge")
			.attachKeyFrame()
			.colored(PonderPalette.MEDIUM)
			.pointAt(blockSurface)
			.placeNearTarget();
		scene.idle(100);

		Selection comparator = util.select.fromTo(2, 1, 1, 2, 1, 2);
		scene.world.showSection(comparator, Direction.SOUTH);
		scene.idle(10);
		scene.world.toggleRedstonePower(comparator);
		scene.effects.indicateRedstone(util.grid.at(2, 1, 2));
		scene.idle(20);

		scene.overlay.showText(120)
			.text("Comparators can emit analog Restone Signals relative to the " + component + "'s measurements")
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.pointAt(util.vector.centerOf(2, 1, 2)
				.add(0, -0.35, 0))
			.placeNearTarget();
		scene.idle(130);
		scene.markAsFinished();
	}

	public static void creativeMotorMojang(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("creative_motor_mojang", "Mojang's Enigma");
		scene.configureBasePlate(0, 0, 15);
		scene.scaleSceneView(.55f);
		scene.showBasePlate();
		scene.idle(15);
		scene.world.showSection(util.select.fromTo(12, 1, 7, 12, 1, 2), Direction.WEST);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(5, 1, 2, 7, 2, 1), Direction.EAST);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(7, 1, 3, 7, 1, 8), Direction.NORTH);
		scene.idle(3);
		scene.world.showSection(util.select.position(7, 2, 8), Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(util.select.position(4, 1, 4), Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(util.select.position(4, 1, 6), Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(util.select.position(3, 1, 10), Direction.SOUTH);
		scene.idle(3);
		scene.world.showSection(util.select.position(1, 1, 11), Direction.EAST);
		scene.idle(3);
		scene.world.showSection(util.select.position(11, 1, 3), Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(11, 2, 3, 11, 2, 7), Direction.NORTH);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(8, 1, 2, 10, 1, 2), Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(util.select.position(11, 1, 2), Direction.SOUTH);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(6, 1, 8, 5, 1, 8), Direction.EAST);
		scene.rotateCameraY(-90);
		scene.idle(3);
		scene.world.showSection(util.select.position(12, 1, 10), Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(util.select.position(11, 1, 12), Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(8, 1, 8, 11, 1, 8), Direction.WEST);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(5, 2, 8, 5, 3, 8), Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(8, 1, 5, 8, 2, 7), Direction.WEST);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(7, 3, 9, 8, 3, 8), Direction.UP);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(6, 3, 7, 9, 3, 7)
			.add(util.select.fromTo(6, 3, 8, 6, 3, 10))
			.add(util.select.fromTo(7, 3, 10, 9, 3, 10))
			.add(util.select.fromTo(9, 3, 7, 9, 3, 9)), Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(10, 4, 7, 6, 4, 10), Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(util.select.fromTo(8, 1, 13, 8, 2, 11), Direction.NORTH);
		scene.idle(3);
		scene.idle(20);
	}

}
