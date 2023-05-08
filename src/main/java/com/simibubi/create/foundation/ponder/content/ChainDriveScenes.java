package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.content.contraptions.relays.encased.EncasedBeltBlock;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverBlockEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.TextWindowElement.Builder;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.phys.AABB;

public class ChainDriveScenes {

	public static void chainDriveAsRelay(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("chain_drive", "Relaying rotational force with Chain Drives");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		BlockPos gaugePos = util.grid.at(0, 1, 3);
		Selection gauge = util.select.position(gaugePos);
		scene.world.showSection(gauge, Direction.UP);
		scene.world.setKineticSpeed(gauge, 0);

		scene.idle(5);
		scene.world.showSection(util.select.fromTo(5, 1, 2, 4, 1, 2), Direction.DOWN);
		scene.idle(10);

		for (int i = 0; i < 3; i++) {
			scene.idle(5);
			scene.world.showSection(util.select.position(3, 1, 2 - i), Direction.DOWN);
			if (i != 0)
				scene.world.showSection(util.select.position(3, 1, 2 + i), Direction.DOWN);
		}

		scene.idle(10);
		scene.world.showSection(util.select.position(gaugePos.east(2)), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(gaugePos.east()), Direction.DOWN);
		scene.idle(5);

		scene.world.setKineticSpeed(gauge, 64);
		scene.effects.indicateSuccess(gaugePos);
		scene.idle(20);
		scene.overlay.showText(60)
			.text("Chain Drives relay rotation to each other in a row")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 4), Direction.WEST));
		scene.idle(60);

		Selection shafts = util.select.fromTo(2, 1, 0, 2, 1, 1);
		BlockPos rotatedECD = util.grid.at(3, 1, 0);
		Selection verticalShaft = util.select.fromTo(rotatedECD.above(), rotatedECD.above(2));

		scene.world.showSection(shafts, Direction.EAST);
		scene.idle(10);
		scene.effects.rotationDirectionIndicator(util.grid.at(2, 1, 0));
		scene.effects.rotationDirectionIndicator(util.grid.at(2, 1, 1));
		scene.idle(20);
		scene.overlay.showText(60)
			.text("All shafts connected like this will rotate in the same direction")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 1, 1), Direction.WEST));
		scene.idle(50);
		scene.world.hideSection(shafts, Direction.WEST);
		scene.idle(25);

		scene.addKeyframe();
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(rotatedECD), Pointing.DOWN).rightClick()
			.withWrench(), 30);
		scene.idle(7);
		scene.world.modifyBlock(rotatedECD, s -> s.setValue(EncasedBeltBlock.AXIS, Axis.Y), true);
		scene.idle(40);

		scene.world.showSection(verticalShaft, Direction.DOWN);
		scene.idle(10);

		scene.effects.rotationDirectionIndicator(util.grid.at(3, 3, 0));
		scene.idle(10);
		scene.overlay.showText(60)
			.text("Any part of the row can be rotated by 90 degrees")
			.placeNearTarget()
			.pointAt(util.vector.centerOf(3, 2, 0));

		scene.markAsFinished();
	}

	public static void adjustableChainGearshift(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("chain_gearshift", "Controlling rotational speed with Chain Gearshifts");
		scene.configureBasePlate(0, 0, 5);
		scene.setSceneOffsetY(-1);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos leverPos = util.grid.at(3, 1, 0);
		BlockPos eastDrive = util.grid.at(3, 1, 2);

		BlockPos eastGauge = eastDrive.above(3);
		BlockPos middleGauge = eastGauge.west()
			.below();
		BlockPos westGauge = eastGauge.west(2)
			.below(2);

		ElementLink<WorldSectionElement> lever =
			scene.world.showIndependentSection(util.select.fromTo(leverPos, leverPos.south()), Direction.UP);

		scene.idle(5);
		scene.world.showSection(util.select.fromTo(4, 1, 3, 4, 2, 3), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.fromTo(eastDrive, eastDrive.west(2))
			.add(util.select.position(eastDrive.above())), Direction.DOWN);
		scene.idle(10);

		scene.overlay.showText(60)
			.text("Unpowered Chain Gearshifts behave exactly like Chain Drives")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(eastDrive, Direction.NORTH));
		scene.idle(60);

		scene.world.showSection(util.select.fromTo(eastGauge, eastGauge.below()), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(middleGauge, middleGauge.below()), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(westGauge), Direction.DOWN);
		scene.idle(5);

		for (BlockPos gauge : new BlockPos[] { eastGauge, middleGauge, westGauge }) {
			scene.idle(5);
			scene.overlay.showText(50)
				.sharedText(gauge == eastGauge ? "rpm16_source" : "rpm16")
				.colored(PonderPalette.MEDIUM)
				.placeNearTarget()
				.pointAt(util.vector.blockSurface(gauge, Direction.NORTH));
		}

		scene.idle(60);

		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.south(2)));
		scene.effects.indicateRedstone(leverPos);
		scene.world.modifyKineticSpeed(util.select.fromTo(westGauge.below(), middleGauge), f -> 2 * f);

		scene.idle(10);

		AABB bb = new AABB(eastDrive);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, eastDrive, bb, 160);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.FAST, eastDrive.west(), bb.move(-2, 0, 0)
			.expandTowards(15 / 16f, 0, 0), 160);
		scene.idle(20);

		scene.overlay.showText(80)
			.text("When Powered, the speed transmitted to other Chain Drives in the row is doubled")
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.FAST)
			.pointAt(util.vector.blockSurface(eastDrive.west(2), Direction.WEST));
		scene.idle(80);

		for (BlockPos gauge : new BlockPos[] { eastGauge, middleGauge, westGauge }) {
			scene.idle(5);
			scene.overlay.showText(70)
				.sharedText(gauge == eastGauge ? "rpm16_source" : "rpm32")
				.colored(gauge == eastGauge ? PonderPalette.MEDIUM : PonderPalette.FAST)
				.placeNearTarget()
				.pointAt(util.vector.blockSurface(gauge, Direction.NORTH));
		}

		scene.idle(80);

		scene.world.hideSection(util.select.fromTo(eastDrive, eastDrive.west(2)), Direction.SOUTH);
		scene.idle(15);
		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.south(2)));
		Selection newDriveSelect = util.select.fromTo(eastDrive.south(2), eastDrive.south(2)
			.west(2));
		ElementLink<WorldSectionElement> drives = scene.world.showIndependentSection(newDriveSelect, Direction.NORTH);
		scene.world.modifyKineticSpeed(util.select.fromTo(westGauge.below(), middleGauge), f -> .5f * f);
		scene.world.setKineticSpeed(newDriveSelect, -32);
		scene.world.moveSection(drives, util.vector.of(0, 0, -2), 0);
		scene.world.moveSection(lever, util.vector.of(-2, 0, 0), 10);

		scene.idle(40);

		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.south(1)));
		scene.world.toggleRedstonePower(util.select.position(1, 1, 4));
		BlockPos analogPos = leverPos.west(2);
		scene.effects.indicateRedstone(analogPos);
		scene.world.modifyKineticSpeed(util.select.position(westGauge), f -> .5f * f);

		scene.idle(10);

		bb = new AABB(eastDrive);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.MEDIUM, eastDrive, bb.expandTowards(-15 / 16f, 0, 0), 160);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.SLOW, eastDrive.west(), bb.move(-2, 0, 0), 160);
		scene.idle(20);

		scene.overlay.showText(80)
			.text("Whenever the Powered Gearshift is not at the source, its speed will be halved instead")
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.SLOW)
			.pointAt(util.vector.blockSurface(eastDrive.west(2), Direction.WEST));
		scene.idle(80);

		for (BlockPos gauge : new BlockPos[] { eastGauge, middleGauge, westGauge }) {
			scene.idle(5);
			scene.overlay.showText(180)
				.sharedText(gauge == westGauge ? "rpm8" : gauge == eastGauge ? "rpm16_source" : "rpm16")
				.colored(gauge == westGauge ? PonderPalette.SLOW : PonderPalette.MEDIUM)
				.placeNearTarget()
				.pointAt(util.vector.blockSurface(gauge, Direction.NORTH));
		}

		scene.idle(80);

		scene.overlay.showText(100)
			.text("In both cases, Chain Drives in the row always run at 2x the speed of the Powered Gearshift")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(eastDrive.west(2), Direction.WEST));
		scene.idle(100);

		scene.world.toggleRedstonePower(util.select.fromTo(leverPos, leverPos.south(2)));
		scene.world.toggleRedstonePower(util.select.position(1, 1, 4));
		scene.world.modifyKineticSpeed(util.select.position(westGauge), f -> 2f * f);
		scene.world.hideIndependentSection(lever, Direction.UP);
		scene.idle(15);

		scene.world.showSection(util.select.fromTo(analogPos, analogPos.south()), Direction.DOWN);

		scene.idle(15);
		scene.world.modifyBlockEntityNBT(util.select.position(analogPos), AnalogLeverBlockEntity.class, nbt -> {
			nbt.putInt("State", 8);
		});
		scene.world.modifyBlock(analogPos.south(), s -> s.setValue(RedStoneWireBlock.POWER, 8), false);
		scene.world.toggleRedstonePower(util.select.position(1, 1, 4));
		scene.world.modifyKineticSpeed(util.select.position(westGauge), f -> .75f * f);
		scene.effects.indicateRedstone(analogPos);

		scene.idle(20);

		scene.overlay.showText(100)
			.text("Using analog signals, the ratio can be adjusted more precisely between 1 and 2")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(eastDrive.west(2), Direction.WEST));
		scene.idle(40);

		for (BlockPos gauge : new BlockPos[] { eastGauge, middleGauge, westGauge }) {
			scene.idle(5);
			Builder builder = scene.overlay.showText(180)
				.colored(gauge == westGauge ? PonderPalette.SLOW : PonderPalette.MEDIUM)
				.placeNearTarget()
				.pointAt(util.vector.blockSurface(gauge, Direction.NORTH));
			if (gauge == westGauge)
				builder.text("12 RPM");
			else
				builder.sharedText(gauge == eastGauge ? "rpm16_source" : "rpm16");
		}
	}

}
