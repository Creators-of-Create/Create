package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock;
import com.simibubi.create.content.decoration.steamWhistle.WhistleExtenderBlock;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.utility.Iterate;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SteamScenes {

	public static void whistle(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("steam_whistle", "Setting up Steam Whistles");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		Selection tank = util.select.fromTo(3, 1, 2, 3, 2, 2);
		Selection boiler = util.select.fromTo(2, 2, 2, 2, 3, 2);
		BlockPos leverPos = util.grid.at(1, 3, 2);
		Selection lever = util.select.position(leverPos);
		Selection whistleArea = util.select.fromTo(2, 3, 1, 2, 7, 1);
		BlockPos whistlePos = util.grid.at(2, 3, 1);
		Selection campfire = util.select.position(2, 1, 2);

		scene.idle(15);
		ElementLink<WorldSectionElement> tankElement = scene.world.showIndependentSection(tank, Direction.DOWN);
		scene.world.moveSection(tankElement, util.vector.of(-1, 0, 0), 0);
		scene.idle(10);
		ElementLink<WorldSectionElement> whistleElement =
			scene.world.showIndependentSection(whistleArea, Direction.SOUTH);
		scene.world.moveSection(whistleElement, util.vector.of(0, -1, 0), 0);
		scene.idle(15);
		scene.world.moveSection(tankElement, util.vector.of(0, -1000, 0), 0);
		scene.world.hideIndependentSection(tankElement, null);
		ElementLink<WorldSectionElement> boilerElement = scene.world.showIndependentSectionImmediately(boiler);
		scene.world.moveSection(boilerElement, util.vector.of(0, -1, 0), 0);
		scene.effects.indicateSuccess(util.grid.at(2, 1, 2));
		scene.idle(25);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("Steam Whistles can be placed on a Fluid Tank")
			.pointAt(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.NORTH))
			.placeNearTarget();
		scene.idle(60);

		scene.world.moveSection(boilerElement, util.vector.of(0, 1, 0), 15);
		scene.world.moveSection(whistleElement, util.vector.of(0, 1, 0), 15);
		scene.idle(10);
		scene.world.showSection(campfire, Direction.NORTH);
		scene.idle(15);

		scene.overlay.showText(50)
			.attachKeyFrame()
			.text("If the tank receives sufficient heat...")
			.pointAt(util.vector.blockSurface(util.grid.at(2, 1, 2), Direction.WEST))
			.placeNearTarget();
		scene.idle(40);

		scene.world.showSection(lever, Direction.DOWN);
		scene.idle(20);
		scene.world.toggleRedstonePower(whistleArea);
		scene.world.toggleRedstonePower(lever);
		scene.effects.indicateRedstone(leverPos);
		scene.idle(10);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("...the Whistle will play a note when activated")
			.pointAt(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.NORTH))
			.placeNearTarget();

		scene.idle(10);
		scene.world.toggleRedstonePower(whistleArea);
		scene.world.toggleRedstonePower(lever);
		scene.idle(20);
		scene.world.toggleRedstonePower(whistleArea);
		scene.world.toggleRedstonePower(lever);
		scene.effects.indicateRedstone(leverPos);
		scene.idle(20);
		scene.world.toggleRedstonePower(whistleArea);
		scene.world.toggleRedstonePower(lever);
		scene.idle(40);

		InputWindowElement rightClick =
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 3, 1), Direction.EAST), Pointing.RIGHT)
				.withItem(AllBlocks.STEAM_WHISTLE.asStack())
				.rightClick();

		scene.overlay.showControls(rightClick, 50);
		scene.idle(6);
		BlockState extension = AllBlocks.STEAM_WHISTLE_EXTENSION.getDefaultState();
		scene.world.setBlock(whistlePos.above(), extension, false);
		scene.idle(20);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("Use a Whistle item on the block to lower its pitch")
			.pointAt(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.NORTH))
			.placeNearTarget();

		scene.idle(40);

		scene.overlay.showControls(rightClick, 2);
		scene.idle(6);
		scene.world.cycleBlockProperty(whistlePos.above(), WhistleExtenderBlock.SHAPE);
		scene.idle(4);
		scene.overlay.showControls(rightClick, 2);
		scene.idle(6);
		scene.world.setBlock(whistlePos.above(2), extension, false);
		scene.world.cycleBlockProperty(whistlePos.above(), WhistleExtenderBlock.SHAPE);
		scene.idle(4);
		scene.overlay.showControls(rightClick, 2);
		scene.idle(6);
		scene.world.cycleBlockProperty(whistlePos.above(2), WhistleExtenderBlock.SHAPE);
		scene.idle(4);
		scene.overlay.showControls(rightClick, 2);
		scene.idle(6);
		scene.world.cycleBlockProperty(whistlePos.above(2), WhistleExtenderBlock.SHAPE);
		scene.world.setBlock(whistlePos.above(3), extension, false);
		scene.idle(20);

		rightClick =
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 3, 1), Direction.EAST), Pointing.RIGHT)
				.withItem(AllItems.WRENCH.asStack())
				.rightClick();

		scene.overlay.showControls(rightClick, 50);
		scene.idle(6);
		for (int i = 0; i < 4; i++) {
			scene.world.cycleBlockProperty(whistlePos.above(i), WhistleBlock.SIZE);
			scene.idle(1);
		}
		scene.idle(20);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("Cycle between three different octaves using a Wrench")
			.pointAt(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.NORTH))
			.placeNearTarget();

		scene.idle(40);
		scene.overlay.showControls(rightClick, 4);
		scene.idle(6);
		for (int i = 0; i < 4; i++) {
			scene.world.cycleBlockProperty(whistlePos.above(i), WhistleBlock.SIZE);
			scene.idle(1);
		}

		scene.idle(20);
		scene.world.toggleRedstonePower(whistleArea);
		scene.world.toggleRedstonePower(lever);
		scene.effects.indicateRedstone(leverPos);
		scene.idle(20);
		scene.world.toggleRedstonePower(whistleArea);
		scene.world.toggleRedstonePower(lever);
		scene.idle(20);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 3, 1), Direction.DOWN), Pointing.UP)
				.withItem(AllItems.GOGGLES.asStack()),
			80);
		scene.idle(6);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.text("Engineer's Goggles can help to find out the current pitch of a Whistle")
			.pointAt(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.NORTH))
			.placeNearTarget();
		scene.idle(40);
	}

	public static void engine(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("steam_engine", "Setting up Steam Engines");
		scene.configureBasePlate(0, 0, 7);
		scene.setSceneOffsetY(-1);
		scene.scaleSceneView(.9f);
		scene.showBasePlate();

		Selection fakeGround = util.select.fromTo(4, 0, 0, 2, 0, 0);
		ElementLink<WorldSectionElement> fakeGroundElement =
			scene.world.showIndependentSection(fakeGround, Direction.UP);
		scene.world.moveSection(fakeGroundElement, util.vector.of(1, 0, 1), 0);

		Selection campfires = util.select.fromTo(4, 1, 4, 5, 1, 3);
		Selection burners = util.select.fromTo(3, 1, 3, 2, 1, 4);

		Selection tank = util.select.fromTo(4, 2, 3, 5, 2, 4);
		Selection boiler1 = util.select.fromTo(5, 4, 2, 4, 4, 1);
		Selection boiler2 = util.select.fromTo(5, 7, 6, 4, 4, 5);
		Selection boiler3 = util.select.fromTo(5, 4, 3, 4, 11, 4);

		Selection engine = util.select.position(2, 2, 3);
		Selection engine1 = util.select.fromTo(3, 4, 1, 2, 4, 1);
		BlockPos engine1ShaftPos = util.grid.at(1, 4, 1);
		Selection engine1Shaft = util.select.position(1, 4, 1);

		Selection pumpCogs = util.select.fromTo(6, 1, 7, 6, 1, 2);
		Selection largeCog = util.select.position(5, 0, 7);

		Selection pump1 = util.select.fromTo(5, 2, 2, 4, 1, 1);
		Selection pump2 = util.select.fromTo(5, 2, 7, 4, 1, 6);
		Selection pump3 = util.select.fromTo(2, 3, 7, 1, 1, 6);

		scene.world.modifyBlock(util.grid.at(4, 2, 7), s -> s.setValue(PumpBlock.FACING, Direction.SOUTH), false);
		scene.world.modifyBlock(util.grid.at(1, 2, 7), s -> s.setValue(PumpBlock.FACING, Direction.SOUTH), false);
		scene.world.modifyBlock(util.grid.at(2, 3, 7), s -> s.setValue(PumpBlock.FACING, Direction.SOUTH), false);

		scene.idle(15);
		ElementLink<WorldSectionElement> tankElement = scene.world.showIndependentSection(tank, Direction.DOWN);
		scene.world.moveSection(tankElement, util.vector.of(0, -1, 0), 0);
		scene.idle(10);
		ElementLink<WorldSectionElement> engineElement = scene.world.showIndependentSection(engine, Direction.EAST);
		scene.world.moveSection(engineElement, util.vector.of(1, -1, 0), 0);
		scene.idle(15);
		scene.world.moveSection(tankElement, util.vector.of(0, -1000, 0), 0);
		scene.world.hideIndependentSection(tankElement, null);
		ElementLink<WorldSectionElement> boilerElement = scene.world.showIndependentSectionImmediately(boiler1);
		scene.world.moveSection(boilerElement, util.vector.of(0, -3, 2), 0);
		scene.effects.indicateSuccess(util.grid.at(5, 1, 3));
		scene.effects.indicateSuccess(util.grid.at(4, 1, 3));
		scene.effects.indicateSuccess(util.grid.at(5, 1, 4));
		scene.effects.indicateSuccess(util.grid.at(4, 1, 4));
		scene.idle(25);

		scene.overlay.showText(50)
			.attachKeyFrame()
			.text("Steam Engines can be placed on a Fluid Tank")
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 3), Direction.WEST))
			.placeNearTarget();
		scene.idle(60);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(3, 1, 3), Direction.UP), Pointing.DOWN)
				.withItem(AllBlocks.SHAFT.asStack())
				.rightClick(),
			60);
		scene.idle(10);
		scene.world.setBlock(engine1ShaftPos, AllBlocks.SHAFT.getDefaultState()
			.setValue(ShaftBlock.AXIS, Axis.Z), false);
		ElementLink<WorldSectionElement> engineShaftElement = scene.world.showIndependentSection(engine1Shaft, null);
		scene.world.moveSection(engineShaftElement, util.vector.of(0, -3, 2), 0);
		scene.idle(5);
		scene.world.moveSection(engineElement, util.vector.of(0, -1000, 0), 0);
		scene.world.hideIndependentSection(engineElement, null);
		engineElement = scene.world.showIndependentSectionImmediately(engine1);
		scene.world.moveSection(engineElement, util.vector.of(0, -3, 2), 0);
		scene.world.setBlock(engine1ShaftPos, AllBlocks.POWERED_SHAFT.getDefaultState()
			.setValue(ShaftBlock.AXIS, Axis.Z), false);
		scene.effects.indicateSuccess(util.grid.at(1, 1, 3));
		scene.idle(40);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.text("Clicking the engine with a Shaft creates the Kinetic Output")
			.pointAt(util.vector.centerOf(util.grid.at(1, 1, 3)))
			.placeNearTarget();
		scene.idle(90);

		scene.world.multiplyKineticSpeed(largeCog, -1);
		scene.world.multiplyKineticSpeed(pumpCogs, -1);
		scene.world.multiplyKineticSpeed(pump1, -1);
		scene.world.multiplyKineticSpeed(pump2, -1);

		scene.world.moveSection(boilerElement, util.vector.of(0, 1, 0), 15);
		scene.world.moveSection(engineElement, util.vector.of(0, 1, 0), 15);
		scene.world.moveSection(engineShaftElement, util.vector.of(0, 1, 0), 15);
		scene.idle(10);
		scene.world.showSection(campfires, Direction.NORTH);
		scene.idle(10);
		scene.world.hideIndependentSection(fakeGroundElement, Direction.DOWN);
		scene.world.showSection(largeCog, Direction.UP);
		scene.idle(5);
		scene.world.showSection(pump1, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(pumpCogs, Direction.WEST);
		scene.idle(25);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("With sufficient Heat, Water and Boiler space...")
			.pointAt(util.vector.blockSurface(util.grid.at(4, 2, 4), Direction.UP))
			.placeNearTarget();
		scene.idle(30);

		scene.world.setKineticSpeed(engine1Shaft, 16);
		scene.effects.createRedstoneParticles(util.grid.at(3, 2, 3), 0xFFFFFF, 10);
		scene.idle(40);

		scene.overlay.showText(60)
			.text("...they will generate Rotational Force")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 3), Direction.NORTH))
			.placeNearTarget();
		scene.idle(70);

		scene.overlay.showSelectionWithText(util.select.fromTo(5, 2, 3, 4, 2, 4), 50)
			.attachKeyFrame()
			.text("The minimal setup requires 4 Fluid Tanks")
			.pointAt(util.vector.blockSurface(util.grid.at(4, 2, 4), Direction.UP))
			.placeNearTarget();
		scene.idle(60);

		scene.world.hideSection(campfires, Direction.SOUTH);
		scene.idle(15);
		ElementLink<WorldSectionElement> burnersElement = scene.world.showIndependentSection(burners, Direction.SOUTH);
		scene.world.moveSection(burnersElement, util.vector.of(2, 0, 0), 0);
		scene.idle(25);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(4, 1, 3), Direction.EAST), Pointing.RIGHT)
				.withItem(new ItemStack(Items.OAK_LOG))
				.rightClick(),
			60);
		scene.idle(10);
		scene.world.setBlocks(burners, AllBlocks.BLAZE_BURNER.getDefaultState()
			.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED), false);
		scene.idle(5);

		scene.world.setKineticSpeed(engine1Shaft, 64);
		scene.effects.createRedstoneParticles(util.grid.at(3, 2, 3), 0xFFFFFF, 10);
		scene.idle(40);

		scene.overlay.showText(80)
			.text("With the help of Blaze Burners, the power output can be increased")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(4, 1, 3), Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.world.hideSection(pump1, Direction.UP);
		scene.idle(5);
		scene.world.hideIndependentSection(boilerElement, Direction.SOUTH);
		scene.idle(10);
		boilerElement = scene.world.showIndependentSection(boiler2, Direction.SOUTH);
		scene.world.moveSection(boilerElement, util.vector.of(0, -2, -2), 0);
		scene.idle(10);
		ElementLink<WorldSectionElement> pumpElement = scene.world.showIndependentSection(pump2, Direction.DOWN);
		scene.world.moveSection(pumpElement, util.vector.of(0, 0, -5), 0);
		scene.idle(20);

		Vec3 target = util.vector.blockSurface(util.grid.at(4, 3, 3), Direction.WEST);
		scene.overlay.showText(80)
			.text("Higher power levels require more Water, Size and Heat")
			.attachKeyFrame()
			.pointAt(target)
			.placeNearTarget();
		scene.idle(90);

		scene.overlay.showControls(
			new InputWindowElement(target.add(0, 0, 0.5), Pointing.DOWN).withItem(AllItems.GOGGLES.asStack()), 60);
		scene.idle(6);
		scene.overlay.showText(80)
			.text("The boiler's current power level can be inspected with Engineer's Goggles")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.pointAt(target.add(0, 0, 0.5))
			.placeNearTarget();
		scene.idle(90);

		scene.world.showSectionAndMerge(util.select.fromTo(3, 4, 6, 1, 4, 6), Direction.EAST, boilerElement);
		scene.idle(5);
		scene.world.setKineticSpeed(util.select.position(1, 4, 6), 64);
		scene.world.showSectionAndMerge(util.select.fromTo(3, 5, 6, 1, 5, 6), Direction.EAST, boilerElement);
		scene.idle(5);
		scene.world.setKineticSpeed(util.select.position(1, 5, 6), -64);
		scene.world.showSectionAndMerge(util.select.fromTo(3, 5, 5, 1, 5, 5), Direction.EAST, boilerElement);
		scene.idle(5);
		scene.world.setKineticSpeed(util.select.position(1, 5, 5), -64);
		scene.world.showSectionAndMerge(util.select.fromTo(1, 4, 7, 1, 5, 7), Direction.NORTH, boilerElement);
		scene.idle(5);
		scene.world.setKineticSpeed(util.select.position(1, 5, 7), -64);
		scene.world.setKineticSpeed(util.select.position(1, 4, 7), 64);
		scene.idle(20);

		scene.overlay.showText(100)
			.text("With each added power level, an additional Engine can output at full capacity")
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(util.grid.at(1, 3, 3), Direction.NORTH))
			.placeNearTarget();
		scene.idle(110);

		scene.overlay.showText(30)
			.text("Lvl 4")
			.colored(PonderPalette.BLUE)
			.pointAt(util.vector.blockSurface(util.grid.at(4, 4, 4), Direction.WEST))
			.placeNearTarget();
		scene.idle(40);
		scene.overlay.showSelectionWithText(util.select.fromTo(3, 2, 3, 3, 3, 4), 30)
			.text("4 Engines")
			.colored(PonderPalette.BLUE)
			.pointAt(util.vector.blockSurface(util.grid.at(3, 3, 4), Direction.UP))
			.placeNearTarget();
		scene.idle(30);

		scene.world.hideIndependentSection(pumpElement, Direction.UP);
		scene.idle(5);
		scene.world.hideIndependentSection(boilerElement, Direction.SOUTH);
		scene.world.hideIndependentSection(engineElement, Direction.SOUTH);
		scene.world.hideIndependentSection(engineShaftElement, Direction.SOUTH);
		scene.idle(20);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.of(5, 2, 4), Pointing.DOWN).withItem(AllItems.BLAZE_CAKE.asStack())
				.rightClick(),
			10);
		scene.idle(6);
		scene.world.setBlocks(burners, AllBlocks.BLAZE_BURNER.getDefaultState()
			.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.SEETHING), false);
		scene.idle(15);

		boilerElement = scene.world.showIndependentSection(boiler3, Direction.SOUTH);
		scene.world.moveSection(boilerElement, util.vector.of(0, -2, 0), 0);
		scene.idle(10);
		pumpElement = scene.world.showIndependentSection(pump3, Direction.DOWN);
		scene.world.moveSection(pumpElement, util.vector.of(3, 0, -5), 0);
		scene.idle(20);

		ElementLink<WorldSectionElement> cogsElement =
			scene.world.showIndependentSection(util.select.position(1, 1, 7), Direction.NORTH);
		scene.world.moveSection(cogsElement, util.vector.of(0, -2, -2), 0);

		Selection previous = null;
		boolean previousForward = false;
		for (int y = 4; y < 9; y++) {
			if (y != 6)
				for (boolean left : Iterate.trueAndFalse) {
					int z = (left ^ y % 2 == 0) ? 3 : 4;
					if (previous != null)
						scene.world.setKineticSpeed(previous, previousForward ? 64 : -64);
					scene.world.showSectionAndMerge(previous = util.select.fromTo(3, y, z, 1, y, z), Direction.EAST,
						boilerElement);
					previousForward = y % 2 == 0;
					scene.idle(5);
				}

			scene.world.showSectionAndMerge(util.select.position(1, y, 7), Direction.NORTH, cogsElement);
			scene.world.setKineticSpeed(util.select.position(1, y, 7), y % 2 == 0 ? 64 : -64);
		}

		scene.world.setKineticSpeed(previous, 64);

		scene.overlay.showText(30)
			.text("Lvl 8")
			.colored(PonderPalette.BLUE)
			.pointAt(util.vector.blockSurface(util.grid.at(4, 4, 3), Direction.NORTH))
			.placeNearTarget();
		scene.idle(40);
		scene.overlay.showSelectionWithText(util.select.fromTo(3, 2, 3, 3, 6, 4), 30)
			.text("8 Engines")
			.colored(PonderPalette.BLUE)
			.pointAt(util.vector.blockSurface(util.grid.at(3, 3, 4), Direction.UP))
			.placeNearTarget();
		scene.idle(30);

	}

}
