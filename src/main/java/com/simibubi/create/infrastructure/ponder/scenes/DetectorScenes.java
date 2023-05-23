package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlock;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DetectorScenes {

	public static void smartObserver(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("smart_observer", "Advanced detection with Smart Observers");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		Selection chuteObserver = util.select.fromTo(0, 1, 4, 0, 2, 4);
		Selection chute = util.select.fromTo(1, 1, 4, 1, 3, 4);
		Selection pipe = util.select.fromTo(3, 1, 4, 3, 3, 4);
		Selection pipeObserver = util.select.fromTo(4, 1, 4, 4, 2, 4);
		Selection redstoneDust = util.select.fromTo(1, 1, 2, 0, 1, 2);
		Selection belt = util.select.fromTo(1, 1, 1, 3, 1, 1);
		Selection chest = util.select.position(2, 1, 0);
		Selection amethyst = util.select.position(3, 1, 0);
		Selection largeCog = util.select.position(5, 0, 2);
		Selection smallCogs = util.select.fromTo(3, 1, 2, 4, 1, 2);
		BlockPos observerPos = util.grid.at(2, 1, 2);
		BlockPos funnelPos = util.grid.at(3, 2, 1);
		Selection funnelChest = util.select.fromTo(4, 1, 1, 4, 2, 1);

		scene.world.showSection(util.select.position(observerPos), Direction.DOWN);
		scene.idle(10);

		scene.overlay.showText(60)
			.text("Smart Observers can be used to detect a variety of events")
			.pointAt(util.vector.blockSurface(observerPos, Direction.WEST))
			.placeNearTarget();
		scene.idle(50);

		scene.world.showSection(redstoneDust, Direction.EAST);
		scene.idle(5);
		ElementLink<WorldSectionElement> chestLink = scene.world.showIndependentSection(chest, Direction.SOUTH);
		scene.world.moveSection(chestLink, util.vector.of(0, 0, 1), 0);
		scene.idle(15);

		ItemStack copperIngot = new ItemStack(Items.COPPER_INGOT);
		ItemStack amethystItem = new ItemStack(Blocks.AMETHYST_BLOCK);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(observerPos.north(), Direction.NORTH), Pointing.RIGHT)
				.withItem(copperIngot),
			40);
		scene.idle(7);
		scene.world.toggleRedstonePower(util.select.position(observerPos));
		scene.world.toggleRedstonePower(redstoneDust);
		scene.effects.indicateRedstone(observerPos);
		scene.idle(15);

		scene.overlay.showText(60)
			.text("It can detect items or fluids inside of generic containers")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(observerPos, Direction.WEST))
			.placeNearTarget();
		scene.idle(65);

		scene.overlay.showCenteredScrollInput(observerPos, Direction.UP, 10);
		scene.idle(5);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(observerPos, Direction.UP), Pointing.DOWN).rightClick()
				.withItem(amethystItem),
			60);
		scene.idle(7);
		scene.world.setFilterData(util.select.position(observerPos), SmartObserverBlockEntity.class, amethystItem);
		scene.world.toggleRedstonePower(util.select.position(observerPos));
		scene.world.toggleRedstonePower(redstoneDust);
		scene.idle(25);

		scene.overlay.showText(60)
			.text("The filter slot can be used to look for specific contents only")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(observerPos, Direction.UP))
			.placeNearTarget();
		scene.idle(50);

		scene.world.hideIndependentSection(chestLink, Direction.EAST);
		scene.idle(10);
		ElementLink<WorldSectionElement> amethystLink = scene.world.showIndependentSection(amethyst, Direction.EAST);
		scene.world.moveSection(amethystLink, util.vector.of(-1, 0, 1), 0);
		scene.idle(15);
		scene.world.toggleRedstonePower(util.select.position(observerPos));
		scene.world.toggleRedstonePower(redstoneDust);
		scene.effects.indicateRedstone(observerPos);
		scene.idle(15);

		scene.overlay.showText(50)
			.text("It also activates when the block itself matches the filter")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(observerPos.north(), Direction.WEST))
			.placeNearTarget();

		scene.idle(45);
		scene.world.hideIndependentSection(amethystLink, Direction.EAST);
		scene.world.toggleRedstonePower(util.select.position(observerPos));
		scene.world.toggleRedstonePower(redstoneDust);
		scene.idle(15);
		scene.world.showSection(largeCog, Direction.UP);
		scene.idle(5);
		scene.world.showSection(smallCogs, Direction.DOWN);
		scene.world.showSection(belt, Direction.SOUTH);
		scene.idle(15);
		scene.world.setFilterData(util.select.position(0, 2, 4), SmartObserverBlockEntity.class, copperIngot);
		scene.world.showSection(chuteObserver, Direction.DOWN);
		scene.idle(2);
		scene.world.setFilterData(util.select.position(4, 2, 4), SmartObserverBlockEntity.class,
			new ItemStack(Items.LAVA_BUCKET));
		scene.world.showSection(pipeObserver, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(chute, Direction.WEST);
		scene.idle(2);
		scene.world.showSection(pipe, Direction.EAST);
		scene.idle(10);

		scene.overlay.showText(60)
			.text("Additionally, smart observers can monitor belts, chutes and pipes")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(observerPos, Direction.UP))
			.placeNearTarget();
		scene.idle(60);

		scene.world.createItemOnBelt(util.grid.at(3, 1, 1), Direction.EAST, amethystItem);
		scene.idle(15);

		scene.world.toggleRedstonePower(util.select.position(observerPos));
		scene.world.toggleRedstonePower(redstoneDust);
		scene.effects.indicateRedstone(observerPos);
		scene.idle(13);

		scene.world.toggleRedstonePower(util.select.position(observerPos));
		scene.world.toggleRedstonePower(redstoneDust);
		scene.idle(25);

		scene.world.showSection(funnelChest, Direction.WEST);
		scene.idle(5);
		scene.world.showSection(util.select.position(funnelPos), Direction.DOWN);
		scene.idle(5);
		ElementLink<WorldSectionElement> observerLink =
			scene.world.makeSectionIndependent(util.select.position(observerPos));
		scene.world.moveSection(observerLink, util.vector.of(1, 1, 0), 10);
		scene.world.hideSection(redstoneDust, Direction.EAST);
		scene.idle(20);

		scene.overlay.showText(60)
			.text("...and will emit a pulse, if an item enters or exits a funnel")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(observerPos.above()
				.east(), Direction.WEST))
			.placeNearTarget();
		scene.idle(60);

		for (int i = 0; i < 3; i++) {
			scene.world.createItemOnBelt(util.grid.at(3, 1, 1), Direction.EAST, amethystItem);

			scene.world.toggleRedstonePower(util.select.position(observerPos));
			scene.effects.indicateRedstone(observerPos.above()
				.east());
			scene.idle(5);

			scene.world.toggleRedstonePower(util.select.position(observerPos));
			scene.idle(25);
		}

	}

	public static void thresholdSwitch(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("threshold_switch", "Monitoring with the Threshold Switch");
		scene.configureBasePlate(0, 1, 5);

		Selection fluidTank = util.select.fromTo(1, 1, 5, 1, 3, 5);
		Selection pulley = util.select.fromTo(3, 2, 3, 2, 2, 3);
		BlockPos pulleyPos = util.grid.at(2, 2, 3);
		BlockPos switchPos = util.grid.at(1, 1, 3);
		Selection redstone = util.select.fromTo(1, 1, 2, 1, 1, 1);
		Selection chest = util.select.fromTo(3, 1, 3, 2, 1, 3);
		Selection belt = util.select.fromTo(3, 0, 0, 3, 0, 6);
		Selection cogs = util.select.fromTo(4, 0, 6, 5, 0, 6)
			.add(util.select.position(5, 0, 5));
		Selection inFunnel = util.select.position(3, 1, 2);
		Selection outFunnel = util.select.position(3, 1, 4);
		Selection baseStrip = util.select.fromTo(1, 0, 1, 1, 0, 5);
		Selection basePlate = util.select.fromTo(0, 0, 1, 2, 0, 5)
			.add(util.select.fromTo(4, 0, 5, 4, 0, 1));

		scene.world.showSection(basePlate, Direction.UP);
		ElementLink<WorldSectionElement> stripLink = scene.world.showIndependentSection(baseStrip, Direction.UP);
		scene.world.moveSection(stripLink, util.vector.of(2, 0, 0), 0);
		scene.idle(5);

		scene.world.showSection(util.select.position(switchPos), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(chest, Direction.WEST);
		scene.idle(10);
		scene.world.cycleBlockProperty(switchPos, ThresholdSwitchBlock.LEVEL);
		scene.idle(5);

		scene.overlay.showText(60)
			.text("Threshold Switches monitor the fill level of containers")
			.pointAt(util.vector.blockSurface(switchPos, Direction.NORTH))
			.placeNearTarget();
		scene.idle(60);

		scene.world.hideIndependentSection(stripLink, Direction.DOWN);
		scene.idle(15);
		scene.world.showSection(cogs, Direction.WEST);
		scene.world.showSection(belt, Direction.NORTH);
		scene.idle(5);
		scene.world.showSection(inFunnel, Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(redstone, Direction.SOUTH);
		ItemStack ironIngot = new ItemStack(Items.IRON_INGOT, 32);

		for (int i = 0; i < 5; i++) {
			scene.world.createItemOnBelt(util.grid.at(3, 0, 0), Direction.NORTH, ironIngot);
			scene.idle(10);
			scene.world.removeItemsFromBelt(util.grid.at(3, 0, 2));
			scene.world.flapFunnel(util.grid.at(3, 1, 2), false);

			if (i % 2 == 1)
				scene.world.cycleBlockProperty(switchPos, ThresholdSwitchBlock.LEVEL);
		}

		scene.addLazyKeyframe();
		scene.world.createItemOnBelt(util.grid.at(3, 0, 0), Direction.NORTH, ironIngot);
		scene.world.removeItemsFromBelt(util.grid.at(3, 0, 2));
		scene.world.multiplyKineticSpeed(util.select.everywhere(), 1 / 8f);
		scene.idle(10);

		Vec3 upper = util.vector.blockSurface(switchPos, Direction.NORTH)
			.add(0, 3 / 16f, 0);
		scene.overlay.showLine(PonderPalette.RED, upper.add(2 / 16f, 0, 0), upper.subtract(2 / 16f, 0, 0), 60);
		scene.overlay.showText(70)
			.text("When the inventory content exceeds the upper threshold...")
			.colored(PonderPalette.RED)
			.pointAt(upper.subtract(2 / 16f, 0, 0))
			.placeNearTarget();

		scene.idle(60);
		scene.world.removeItemsFromBelt(util.grid.at(3, 0, 2));
		scene.world.flapFunnel(util.grid.at(3, 1, 2), false);
		scene.world.cycleBlockProperty(switchPos, ThresholdSwitchBlock.LEVEL);
		scene.effects.indicateRedstone(switchPos);
		scene.world.toggleRedstonePower(redstone);
		scene.idle(20);

		scene.overlay.showText(50)
			.text("...the switch will change its redstone output")
			.pointAt(util.vector.blockSurface(switchPos.north(), Direction.DOWN))
			.placeNearTarget();
		scene.idle(50);

		scene.world.showSection(outFunnel, Direction.DOWN);
		scene.world.toggleRedstonePower(outFunnel);
		scene.idle(15);

		scene.world.multiplyKineticSpeed(util.select.everywhere(), 8f);
		for (int i = 0; i < 5; i++) {
			scene.idle(10);
			scene.world.createItemOnBelt(util.grid.at(3, 0, 4), Direction.NORTH, ironIngot);
			if (i % 3 == 1)
				scene.world.modifyBlock(switchPos,
					s -> s.setValue(ThresholdSwitchBlock.LEVEL, s.getValue(ThresholdSwitchBlock.LEVEL) - 1), false);
		}
		scene.world.multiplyKineticSpeed(util.select.everywhere(), 1 / 8f);

		Vec3 lower = util.vector.blockSurface(switchPos, Direction.NORTH)
			.add(0, -3 / 16f, 0);
		scene.overlay.showLine(PonderPalette.GREEN, lower.add(2 / 16f, 0, 0), lower.subtract(2 / 16f, 0, 0), 60);
		scene.overlay.showText(70)
			.text("The signal stays until the lower threshold is reached")
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.pointAt(lower.subtract(2 / 16f, 0, 0))
			.placeNearTarget();
		scene.idle(30);

		for (int i = 0; i < 3; i++) {
			scene.idle(10);
			scene.world.createItemOnBelt(util.grid.at(3, 0, 4), Direction.NORTH, ironIngot);
			if (i % 3 == 2)
				scene.world.modifyBlock(switchPos,
					s -> s.setValue(ThresholdSwitchBlock.LEVEL, s.getValue(ThresholdSwitchBlock.LEVEL) - 1), false);
		}

		scene.world.toggleRedstonePower(redstone);
		scene.idle(40);

		scene.overlay.showText(90)
			.text("The redstone output can now be used to control item supply, keeping the buffer filled")
			.pointAt(util.vector.blockSurface(switchPos.north(), Direction.DOWN))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(100);

		scene.addKeyframe();
		scene.overlay.showLine(PonderPalette.GREEN, lower.add(2 / 16f, 0, 0), lower.subtract(2 / 16f, 0, 0), 105);
		scene.idle(5);
		scene.overlay.showLine(PonderPalette.RED, upper.add(2 / 16f, 0, 0), upper.subtract(2 / 16f, 0, 0), 100);
		scene.idle(15);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(switchPos, Direction.UP), Pointing.DOWN).rightClick(), 60);
		scene.idle(7);
		scene.overlay.showText(70)
			.text("The specific thresholds can be changed in the UI")
			.pointAt(upper.subtract(2 / 16f, 0, 0))
			.placeNearTarget();
		scene.idle(80);

		scene.overlay.showCenteredScrollInput(switchPos, Direction.UP, 70);
		scene.overlay.showText(70)
			.text("A filter can help to only count specific contents toward the total")
			.pointAt(util.vector.blockSurface(switchPos, Direction.UP))
			.attachKeyFrame()
			.placeNearTarget();

		scene.idle(80);
		scene.world.hideSection(belt, Direction.SOUTH);
		scene.world.hideSection(cogs, Direction.EAST);
		scene.idle(2);
		scene.world.hideSection(inFunnel, Direction.EAST);
		scene.idle(2);
		scene.world.hideSection(chest, Direction.EAST);
		scene.idle(2);
		scene.world.hideSection(outFunnel, Direction.EAST);
		scene.idle(9);
		stripLink = scene.world.showIndependentSection(baseStrip, Direction.UP);
		scene.world.moveSection(stripLink, util.vector.of(2, 0, 0), 0);
		scene.idle(5);
		ElementLink<WorldSectionElement> tankLink = scene.world.showIndependentSection(fluidTank, Direction.DOWN);
		scene.world.moveSection(tankLink, util.vector.of(1, 0, -2), 0);
		scene.idle(10);

		scene.world.cycleBlockProperty(switchPos, ThresholdSwitchBlock.LEVEL);
		scene.world.cycleBlockProperty(switchPos, ThresholdSwitchBlock.LEVEL);
		scene.idle(15);

		scene.overlay.showText(70)
			.text("Fluid buffers can be monitored in a similar fashion")
			.pointAt(util.vector.blockSurface(switchPos, Direction.NORTH))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(80);

		scene.world.hideIndependentSection(tankLink, Direction.SOUTH);
		scene.world.hideSection(redstone, Direction.NORTH);
		ElementLink<WorldSectionElement> switchLink =
			scene.world.makeSectionIndependent(util.select.position(switchPos));
		scene.idle(10);
		scene.world.moveSection(switchLink, util.vector.of(0, 1, 0), 15);
		scene.world.modifyBlock(switchPos, s -> s.setValue(ThresholdSwitchBlock.LEVEL, 0), false);
		scene.idle(5);
		scene.world.showSection(pulley, Direction.DOWN);
		scene.idle(15);
		ElementLink<WorldSectionElement> hole = scene.world.makeSectionIndependent(util.select.position(2, 0, 3));
		scene.world.hideIndependentSection(hole, Direction.DOWN);

		scene.overlay.showText(70)
			.text("...as well as, curiously, the length of an extended rope pulley")
			.pointAt(util.vector.blockSurface(switchPos.above(), Direction.NORTH))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(10);

		scene.world.setKineticSpeed(pulley, 32);
		scene.world.movePulley(pulleyPos, 15, 205);
		
		for (int i = 0; i < 4; i++) {
			scene.idle(5);
			scene.world.cycleBlockProperty(switchPos, ThresholdSwitchBlock.LEVEL);
			scene.idle(45);
			if (i == 1)
				scene.markAsFinished();
		}
		
		scene.idle(5);
		scene.world.cycleBlockProperty(switchPos, ThresholdSwitchBlock.LEVEL);
		scene.world.setKineticSpeed(pulley, 0);

	}

}
