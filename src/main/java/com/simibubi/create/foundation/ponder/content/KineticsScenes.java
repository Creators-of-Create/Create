package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.crank.ValveHandleBlock;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelBlock;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelBlock;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftBlock;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeBlock;
import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeTileEntity;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeTileEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.utility.Pointing;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class KineticsScenes {

	public static void shaftAsRelay(SceneBuilder scene, SceneBuildingUtil util) {
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
			.text("Shafts will relay rotation in a straight line.")
			.pointAt(util.vector.of(3, 1.5, 2.5));

		scene.idle(20);
		scene.markAsFinished();
	}

	public static void shaftsCanBeEncased(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("shaft_casing", "Encasing Shafts");
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
			.placeNearTarget()
			.text("Brass or Andesite Casing can be used to decorate Shafts")
			.pointAt(util.vector.topOf(1, 1, 2));
	}

	public static void cogAsRelay(SceneBuilder scene, SceneBuildingUtil util) {
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
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.NORTH));

	}

	public static void largeCogAsRelay(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("large_cogwheel", "Relaying rotational force using Large Cogwheels");
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
		scene.title("cog_speedup", "Gearshifting with Cogs");
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
			.placeNearTarget()
			.pointAt(util.vector.topOf(clutch));

		scene.idle(70);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.south(2)));
		scene.effects.indicateRedstone(leverPos);
		scene.world.setKineticSpeed(util.select.fromTo(0, 1, 2, 2, 1, 2), 32);
		scene.effects.indicateSuccess(gaugePos);
	}

	public static void gearshift(SceneBuilder scene, SceneBuildingUtil util) {
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

	public static void creativeMotor(SceneBuilder scene, SceneBuildingUtil util) {
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
		scene.idle(50);

		scene.rotateCameraY(90);
		scene.idle(20);

		Vec3d blockSurface = util.vector.blockSurface(motor, Direction.EAST);
		AxisAlignedBB point = new AxisAlignedBB(blockSurface, blockSurface);
		AxisAlignedBB expanded = point.grow(1 / 16f, 1 / 5f, 1 / 5f);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, blockSurface, point, 1);
		scene.idle(1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, blockSurface, expanded, 60);
		scene.overlay.showControls(new InputWindowElement(blockSurface, Pointing.DOWN).scroll(), 60);
		scene.idle(20);

		scene.overlay.showText(50)
			.text("Scrolling on the back panel changes the RPM of the motors' rotational output")
			.placeNearTarget()
			.pointAt(blockSurface);
		scene.idle(10);
		scene.world.modifyKineticSpeed(util.select.fromTo(1, 1, 2, 3, 1, 2), f -> 4 * f);
		scene.idle(50);

		scene.effects.rotationSpeedIndicator(motor);
		scene.rotateCameraY(-90);
	}

	public static void waterWheel(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("water_wheel", "Generating Rotational Force using Water Wheels");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(4, 1, 1, 4, 3, 3)
			.add(util.select.fromTo(3, 1, 3, 3, 2, 3)), Direction.DOWN);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);

		BlockPos gaugePos = util.grid.at(0, 2, 2);

		for (int i = 0; i < 4; i++) {
			scene.idle(5);
			scene.world.showSection(util.select.fromTo(gaugePos.east(i)
				.down(), gaugePos.east(i)), Direction.DOWN);
		}

		scene.idle(10);

		for (int i = 0; i < 3; i++) {
			scene.idle(5);
			scene.world.showSection(util.select.position(3, 3, 3 - i), Direction.DOWN);
		}
		scene.world.setKineticSpeed(util.select.everywhere(), -12);
		scene.effects.indicateSuccess(gaugePos);
		for (int i = 0; i < 2; i++) {
			scene.idle(5);
			scene.world.showSection(util.select.position(3, 2 - i, 1), Direction.DOWN);
		}

		BlockPos wheel = util.grid.at(3, 2, 2);
		scene.effects.rotationSpeedIndicator(wheel);
		scene.overlay.showText(50)
			.text("Water Wheels draw force from adjacent Water Currents")
			.placeNearTarget()
			.pointAt(util.vector.topOf(wheel));
		scene.idle(50);

		AxisAlignedBB bb = new AxisAlignedBB(wheel).grow(.125f, 0, 0);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.offset(0, 1.2, 0)
			.contract(0, .75, 0), 80);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.offset(0, 0, 1.2)
			.contract(0, 0, .75), 80);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.offset(0, -1.2, 0)
			.contract(0, -.75, 0), 80);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, new Object(), bb.offset(0, 0, -1.2)
			.contract(0, 0, -.75), 80);
		scene.idle(5);
		scene.overlay.showText(50)
			.text("The more faces are powered, the faster the Water Wheel will rotate")
			.colored(PonderPalette.MEDIUM)
			.placeNearTarget()
			.pointAt(util.vector.topOf(wheel));

		scene.idle(80);
		scene.rotateCameraY(-30);
		scene.overlay.showText(70)
			.text("The Wheels' blades should be oriented against the flow")
			.placeNearTarget()
			.pointAt(util.vector.topOf(wheel));
		scene.idle(80);

		ElementLink<WorldSectionElement> water = scene.world.makeSectionIndependent(util.select.fromTo(3, 1, 1, 3, 3, 1)
			.add(util.select.fromTo(3, 3, 2, 3, 3, 3)));
		ElementLink<WorldSectionElement> wheelElement = scene.world.makeSectionIndependent(util.select.position(wheel));

		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		scene.world.moveSection(water, util.vector.of(0, 2, -2), 10);
		scene.world.moveSection(wheelElement, util.vector.of(0, 1, -1), 10);
		scene.idle(10);
		scene.world.rotateSection(wheelElement, 0, 180, 0, 5);
		scene.idle(10);
		scene.world.modifyBlock(wheel, s -> s.with(WaterWheelBlock.HORIZONTAL_FACING, Direction.WEST), false);
		scene.world.rotateSection(wheelElement, 0, -180, 0, 0);
		scene.idle(1);
		scene.world.moveSection(water, util.vector.of(0, -2, 2), 10);
		scene.world.moveSection(wheelElement, util.vector.of(0, -1, 1), 10);
		scene.idle(10);
		scene.world.setKineticSpeed(util.select.everywhere(), -8);

		scene.overlay.showText(70)
			.colored(PonderPalette.RED)
			.text("Facing the opposite way, they will not be as effective")
			.placeNearTarget()
			.pointAt(util.vector.topOf(wheel));
		scene.idle(80);

		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		scene.world.moveSection(water, util.vector.of(0, 2, -2), 10);
		scene.world.moveSection(wheelElement, util.vector.of(0, 1, -1), 10);
		scene.idle(10);
		scene.rotateCameraY(30);
		scene.world.rotateSection(wheelElement, 0, 180, 0, 5);
		scene.idle(10);
		scene.world.modifyBlock(wheel, s -> s.with(WaterWheelBlock.HORIZONTAL_FACING, Direction.EAST), false);
		scene.world.rotateSection(wheelElement, 0, -180, 0, 0);
		scene.idle(1);
		scene.world.moveSection(water, util.vector.of(0, -2, 2), 10);
		scene.world.moveSection(wheelElement, util.vector.of(0, -1, 1), 10);
		scene.idle(10);
		scene.world.setKineticSpeed(util.select.everywhere(), -12);
		scene.effects.indicateSuccess(gaugePos);
	}

	public static void handCrank(SceneBuilder scene, SceneBuildingUtil util) {
		manualSource(scene, util, true);
	}

	public static void valveHandle(SceneBuilder scene, SceneBuildingUtil util) {
		manualSource(scene, util, false);
		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		scene.idle(20);
		Vec3d centerOf = util.vector.centerOf(2, 2, 2);
		scene.overlay.showControls(new InputWindowElement(centerOf, Pointing.DOWN).rightClick()
			.withItem(new ItemStack(Items.BLUE_DYE)), 40);
		scene.idle(7);
		scene.world.modifyBlock(util.grid.at(2, 2, 2), s -> AllBlocks.DYED_VALVE_HANDLES[11].getDefaultState()
			.with(ValveHandleBlock.FACING, Direction.UP), true);
		scene.idle(10);
		scene.overlay.showText(70)
			.text("Valve handles can be dyed for aesthetic purposes")
			.placeNearTarget()
			.pointAt(centerOf);
	}

	private static void manualSource(SceneBuilder scene, SceneBuildingUtil util, boolean handCrank) {
		String name = handCrank ? "Hand Cranks" : "Valve Handles";
		scene.title(handCrank ? "hand_crank" : "valve_handle", "Generating Rotational Force using " + name);
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

		Vec3d centerOf = util.vector.centerOf(handlePos);
		scene.overlay.showText(70)
			.text(name + " can be used by players to apply rotational force manually")
			.placeNearTarget()
			.pointAt(centerOf);
		scene.idle(80);

		scene.overlay.showControls(new InputWindowElement(centerOf, Pointing.DOWN).rightClick(), 40);
		scene.idle(7);
		scene.world.setKineticSpeed(util.select.everywhere(), handCrank ? 32 : 16);
		scene.world.modifyKineticSpeed(util.select.column(1, 3), f -> f * -2);
		scene.effects.rotationDirectionIndicator(handlePos);
		scene.effects.indicateSuccess(gaugePos);
		scene.idle(10);
		scene.overlay.showText(50)
			.text("Hold Right-Click to rotate it Counter-Clockwise")
			.placeNearTarget()
			.pointAt(centerOf);
		scene.idle(70);
		scene.overlay.showText(50)
			.colored(handCrank ? PonderPalette.MEDIUM : PonderPalette.SLOW)
			.text("Its conveyed speed is " + (handCrank ? "relatively high" : "slow and precise"))
			.placeNearTarget()
			.pointAt(centerOf);
		scene.idle(70);

		scene.world.setKineticSpeed(util.select.everywhere(), 0);
		scene.idle(10);

		scene.overlay.showControls(new InputWindowElement(centerOf, Pointing.DOWN).rightClick()
			.whileSneaking(), 40);
		scene.idle(7);
		scene.world.setKineticSpeed(util.select.everywhere(), handCrank ? -32 : -16);
		scene.world.modifyKineticSpeed(util.select.column(1, 3), f -> f * -2);
		scene.effects.rotationDirectionIndicator(handlePos);
		scene.effects.indicateSuccess(gaugePos);
		scene.idle(10);
		scene.overlay.showText(90)
			.text("Sneak and Hold Right-Click to rotate it Clockwise")
			.placeNearTarget()
			.pointAt(centerOf);
		scene.idle(90);
	}

	public static void sequencedGearshift(SceneBuilder scene, SceneBuildingUtil util) {
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

		Vec3d top = util.vector.topOf(gearshiftPos);
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
		ElementLink<WorldSectionElement> comparator =
			scene.world.showIndependentSection(util.select.fromTo(5, 1, 1, 4, 1, 0), Direction.SOUTH);
		scene.world.moveSection(comparator, util.vector.of(-2, 0, 0), 0);
		scene.world.toggleRedstonePower(util.select.position(5, 1, 1));
		scene.world.cycleBlockProperty(wire, RedstoneWireBlock.POWER);
		scene.world.modifyTileNBT(nixie, NixieTubeTileEntity.class, nbt -> nbt.putInt("RedstoneStrength", 1));

		scene.idle(5);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, -32);
		scene.world.rotateBearing(bearingPos, -180, 40);
		scene.world.rotateSection(contraption, -180, 0, 0, 40);
		scene.effects.rotationDirectionIndicator(gearshiftPos.west());
		scene.world.cycleBlockProperty(wire, RedstoneWireBlock.POWER);
		scene.world.modifyTileNBT(nixie, NixieTubeTileEntity.class, nbt -> nbt.putInt("RedstoneStrength", 2));
		scene.idle(40);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, 0);
		scene.world.cycleBlockProperty(wire, RedstoneWireBlock.POWER);
		scene.world.modifyTileNBT(nixie, NixieTubeTileEntity.class, nbt -> nbt.putInt("RedstoneStrength", 3));
		scene.idle(20);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.setKineticSpeed(outputKinetics, 16);
		scene.world.rotateBearing(bearingPos, 90, 40);
		scene.world.rotateSection(contraption, 90, 0, 0, 40);
		scene.effects.rotationDirectionIndicator(gearshiftPos.west());
		scene.world.cycleBlockProperty(wire, RedstoneWireBlock.POWER);
		scene.world.modifyTileNBT(nixie, NixieTubeTileEntity.class, nbt -> nbt.putInt("RedstoneStrength", 4));
		scene.idle(40);

		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.cycleBlockProperty(gearshiftPos, SequencedGearshiftBlock.STATE);
		scene.world.modifyBlock(wire, s -> s.with(RedstoneWireBlock.POWER, 0), false);
		scene.world.toggleRedstonePower(util.select.position(5, 1, 1));
		scene.world.modifyTileNBT(nixie, NixieTubeTileEntity.class, nbt -> nbt.putInt("RedstoneStrength", 0));
		scene.world.setKineticSpeed(outputKinetics, 0);
	}

	public static void furnaceEngine(SceneBuilder scene, SceneBuildingUtil util) {
		furnaceEngine(scene, util, false);
	}

	public static void flywheel(SceneBuilder scene, SceneBuildingUtil util) {
		furnaceEngine(scene, util, true);
	}

	private static void furnaceEngine(SceneBuilder scene, SceneBuildingUtil util, boolean flywheel) {
		scene.title(flywheel ? "flywheel" : "furnace_engine",
			"Generating Rotational Force using the " + (flywheel ? "Flywheel" : "Furnace Engine"));
		scene.configureBasePlate(0, 0, 6);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos furnacePos = util.grid.at(4, 1, 3);
		BlockPos cogPos = util.grid.at(1, 1, 2);
		BlockPos gaugePos = util.grid.at(1, 1, 1);

		scene.idle(5);
		Selection furnaceSelect = util.select.position(furnacePos);
		scene.world.showSection(furnaceSelect, Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(furnacePos.west()), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(furnacePos.west(3)), Direction.EAST);
		scene.idle(10);

		String text = flywheel ? "Flywheels are required for generating rotational force with the Furnace Engine"
			: "Furnace Engines generate Rotational Force while their attached Furnace is running";
		scene.overlay.showText(80)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(furnacePos.west(flywheel ? 3 : 1)))
			.text(text);
		scene.idle(90);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.topOf(furnacePos), Pointing.DOWN).withItem(new ItemStack(Items.OAK_LOG)),
			30);
		scene.idle(5);
		scene.overlay
			.showControls(new InputWindowElement(util.vector.blockSurface(furnacePos, Direction.NORTH), Pointing.RIGHT)
				.withItem(new ItemStack(Items.COAL)), 30);
		scene.idle(7);
		scene.world.cycleBlockProperty(furnacePos, FurnaceBlock.LIT);
		scene.effects.emitParticles(util.vector.of(4.5, 1.2, 2.9), Emitter.simple(ParticleTypes.LAVA, Vec3d.ZERO), 4,
			1);
		scene.world.setKineticSpeed(util.select.fromTo(1, 1, 3, 1, 1, 1), 16);
		scene.idle(40);

		scene.world.showSection(util.select.position(cogPos), Direction.SOUTH);
		scene.idle(15);
		scene.effects.rotationSpeedIndicator(cogPos);
		scene.world.showSection(util.select.position(gaugePos), Direction.SOUTH);
		scene.idle(15);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(gaugePos, Direction.WEST))
			.text("The provided Rotational Force has a very large stress capacity");
		scene.idle(90);

		ElementLink<WorldSectionElement> engine =
			scene.world.makeSectionIndependent(util.select.fromTo(3, 1, 3, 1, 1, 1));
		scene.world.moveSection(engine, util.vector.of(0, 1, 0), 15);
		scene.idle(10);
		scene.world.hideSection(furnaceSelect, Direction.NORTH);
		scene.idle(15);
		scene.world.setBlock(furnacePos, Blocks.BLAST_FURNACE.getDefaultState()
			.with(FurnaceBlock.FACING, Direction.NORTH)
			.with(FurnaceBlock.LIT, true), false);
		scene.world.showSection(furnaceSelect, Direction.NORTH);
		scene.idle(10);
		scene.world.moveSection(engine, util.vector.of(0, -1, 0), 15);
		scene.idle(10);
		scene.world.setKineticSpeed(util.select.fromTo(1, 1, 3, 1, 1, 1), 32);
		scene.idle(5);
		scene.effects.rotationSpeedIndicator(cogPos);

		scene.overlay.showText(80)
			.placeNearTarget()
			.colored(PonderPalette.MEDIUM)
			.pointAt(util.vector.topOf(furnacePos.west()))
			.text("Using a Blast Furnace will double the efficiency of the Engine");

	}

	public static void speedController(SceneBuilder scene, SceneBuildingUtil util) {
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

		Vec3d inputVec = util.vector.of(1.5, 1.75, 1);
		scene.overlay.showFilterSlotInput(inputVec, 60);

		scene.overlay.showText(70)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(inputVec)
			.text("Using the scroll input on its side, the conveyed speed can be configured");
		scene.idle(80);

		InputWindowElement input = new InputWindowElement(inputVec, Pointing.UP).scroll();
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

	private static void gauge(SceneBuilder scene, SceneBuildingUtil util, boolean speed) {
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
			.with(GaugeBlock.FACING, Direction.UP), true);
		scene.world.setKineticSpeed(util.select.position(gaugePos), 32);
		scene.idle(10);

		scene.overlay.showText(80)
			.text("The " + component + " displays the current " + (speed ? "Speed" : "Stress Capacity")
				+ " of the attached " + (speed ? "components" : "kinetic network"))
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
				.with(CrushingWheelBlock.AXIS, Axis.X);
			scene.world.setBlock(util.grid.at(5, 1, 3), state, true);
			scene.world.setKineticSpeed(util.select.position(5, 1, 3), 32);
			scene.world.modifyTileNBT(util.select.position(gaugePos), StressGaugeTileEntity.class,
				nbt -> nbt.putFloat("Value", .5f));
			scene.effects.indicateRedstone(gaugePos);
			scene.idle(20);
			scene.world.setBlock(util.grid.at(4, 1, 3), state, true);
			scene.world.setKineticSpeed(util.select.position(4, 1, 3), 32);
			scene.world.modifyTileNBT(util.select.position(gaugePos), StressGaugeTileEntity.class,
				nbt -> nbt.putFloat("Value", .9f));
			scene.effects.indicateRedstone(gaugePos);
			scene.idle(10);
		}

		scene.idle(30);

		Vec3d blockSurface = util.vector.blockSurface(gaugePos, Direction.NORTH);
		scene.overlay.showControls(
			new InputWindowElement(blockSurface, Pointing.RIGHT).withItem(AllItems.GOGGLES.asStack()), 40);
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

}
