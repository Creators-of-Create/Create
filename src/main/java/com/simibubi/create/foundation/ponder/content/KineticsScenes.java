package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class KineticsScenes {

	public static void template(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("This is a template");
		scene.showBasePlate();
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}

	//

	public static void shaftAsRelay(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Relaying rotational force using Shafts");
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
			.text("Shafts will relay rotation in a straight line.")
			.pointAt(util.vector.of(3, 1.5, 2.5));

		scene.idle(20);
		scene.markAsFinished();
	}

	public static void shaftsCanBeEncased(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Encasing Shafts");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

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
			.with(EncasedShaftBlock.AXIS, Axis.X), true);
		scene.world.setKineticSpeed(shaft, 32);
		scene.idle(10);

		BlockEntry<EncasedShaftBlock> brassEncased = AllBlocks.BRASS_ENCASED_SHAFT;
		ItemStack brassCasingItem = AllBlocks.BRASS_CASING.asStack();

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(1, 0, 2), Pointing.UP).rightClick()
			.withItem(brassCasingItem), 60);
		scene.idle(7);
		scene.world.setBlocks(brass, brassEncased.getDefaultState()
			.with(EncasedShaftBlock.AXIS, Axis.X), true);
		scene.world.setKineticSpeed(shaft, 32);

		scene.idle(10);
		scene.overlay.showText(1000)
			.text("Brass or Andesite Casing can be used to decorate Shafts")
			.pointAt(util.vector.topOf(1, 1, 2));
	}

	public static void cogAsRelay(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Relaying rotational force using Cogwheels");
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
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.NORTH));

	}

	public static void largeCogAsRelay(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Relaying rotational force using Large Cogwheels");
		scene.configureBasePlate(1, 1, 5);
		scene.world.setBlock(util.grid.at(4, 2, 3), AllBlocks.LARGE_COGWHEEL.getDefaultState()
			.with(CogWheelBlock.AXIS, Axis.X), false);

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
		scene.world.modifyBlock(util.grid.at(3, 2, 3), s -> s.with(ShaftBlock.AXIS, Axis.X), false);
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

	}

	public static void cogsSpeedUp(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Gearshifting with Cogs");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(5, 1, 2, 4, 1, 2), Direction.DOWN);
		scene.idle(10);

		BlockPos lowerCog = util.grid.at(3, 1, 2);
		BlockPos upperCog = util.grid.at(3, 2, 3);
		BlockState largeCogState = AllBlocks.LARGE_COGWHEEL.getDefaultState()
			.with(CogWheelBlock.AXIS, Axis.X);
		BlockState smallCogState = AllBlocks.COGWHEEL.getDefaultState()
			.with(CogWheelBlock.AXIS, Axis.X);

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
		scene.overlay.showText(60)
			.text("Shifting from large to small cogs, the conveyed speed will be doubled")
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 3), Direction.NORTH));
		scene.idle(10);
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

	public static void gearbox(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Relaying rotational force using Gearboxes");
		scene.configureBasePlate(1, 1, 5);
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
		scene.world.setBlock(largeCogBack, defaultState.with(CogWheelBlock.AXIS, Axis.Z), false);
		scene.world.setBlock(largeCogFront, defaultState.with(CogWheelBlock.AXIS, Axis.Z), false);
		scene.world.setBlock(largeCogRight, defaultState.with(CogWheelBlock.AXIS, Axis.X), false);
		scene.world.setBlock(largeCogLeft, defaultState.with(CogWheelBlock.AXIS, Axis.X), false);
		scene.world.showSection(util.select.fromTo(4, 2, 2, 2, 2, 4), Direction.DOWN);

		scene.idle(20);
		scene.overlay.showText(80)
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.topOf(3, 2, 3))
			.placeNearTarget()
			.text("A gearbox is the more compact equivalent of this setup");

		scene.idle(90);
		scene.world.setBlock(largeCogFront.north(), cogState.with(CogWheelBlock.AXIS, Axis.Z), true);
		scene.world.setBlock(largeCogRight.west(), cogState.with(CogWheelBlock.AXIS, Axis.X), true);
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
		scene.world.setBlock(largeCogBack.south(), cogState.with(CogWheelBlock.AXIS, Axis.Z), true);
		scene.idle(10);

		scene.effects.rotationDirectionIndicator(largeCogFront.north());
		scene.effects.rotationDirectionIndicator(largeCogBack.south());
		scene.idle(15);
		scene.overlay.showText(60)
			.pointAt(util.vector.centerOf(3, 2, 5))
			.placeNearTarget()
			.text("Straight connections will be reversed");

	}

	public static void clutch(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Controlling rotational force using a Clutch");
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
			.placeNearTarget()
			.pointAt(util.vector.topOf(clutch));

		scene.idle(70);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.south(2)));
		scene.effects.indicateRedstone(leverPos);
		scene.world.setKineticSpeed(util.select.fromTo(0, 1, 2, 2, 1, 2), 32);
		scene.effects.indicateSuccess(gaugePos);
	}

	public static void gearshift(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Controlling rotational force using a Gearshift");
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

}
