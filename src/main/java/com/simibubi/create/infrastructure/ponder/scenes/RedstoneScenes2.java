package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.redstone.RoseQuartzLampBlock;
import com.simibubi.create.content.redstone.diodes.BrassDiodeBlock;
import com.simibubi.create.content.redstone.diodes.PulseTimerBlockEntity;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder.WorldInstructions;

import net.createmod.catnip.api.math.Pointing;
import net.createmod.ponder.api.client.element.ElementLink;
import net.createmod.ponder.api.client.element.WorldSectionElement;
import net.createmod.ponder.api.client.scene.SceneBuilder;
import net.createmod.ponder.api.client.scene.SceneBuildingUtil;
import net.createmod.ponder.api.client.scene.Selection;
import net.createmod.ponder.api.client.scene.SelectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class RedstoneScenes2 {

	public static void roseQuartzLamp(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("rose_quartz_lamp", "Rose Quartz Lamps");
		scene.configureBasePlate(0, 0, 7);

		BlockPos centerLamp = util.grid()
			.at(3, 1, 3);
		Selection input = util.select()
			.fromTo(3, 1, 1, 3, 1, 2);
		Selection button = util.select()
			.position(3, 1, 1);
		Selection wire = util.select()
			.position(3, 1, 2);
		Selection output = util.select()
			.fromTo(5, 1, 2, 5, 1, 1);
		Selection comparator = util.select()
			.fromTo(1, 1, 3, 0, 1, 3);

		scene.showBasePlate();
		scene.idle(15);

		ElementLink<WorldSectionElement> rowElement = scene.world()
			.showIndependentSection(util.select()
				.position(centerLamp), Direction.DOWN);
		scene.idle(5);
		scene.world()
			.showSection(input, Direction.SOUTH);
		scene.idle(15);

		scene.world()
			.toggleRedstonePower(input);
		scene.effects()
			.indicateRedstone(util.grid()
				.at(3, 1, 1));
		scene.world()
			.cycleBlockProperty(centerLamp, RoseQuartzLampBlock.POWERING);
		scene.idle(15);

		scene.overlay()
			.showText(70)
			.pointAt(util.vector()
				.blockSurface(centerLamp, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Rose Quartz Lamps activate on a Redstone signal");
		scene.idle(5);
		scene.world()
			.toggleRedstonePower(button);
		scene.idle(55);

		scene.world()
			.hideSection(input, Direction.EAST);
		scene.idle(10);
		ElementLink<WorldSectionElement> outputElement = scene.world()
			.showIndependentSection(output, Direction.EAST);
		scene.world()
			.moveSection(outputElement, util.vector()
				.of(-2, 0, 0), 0);
		scene.idle(10);
		scene.world()
			.toggleRedstonePower(wire);
		scene.world()
			.toggleRedstonePower(output);
		scene.idle(5);

		scene.overlay()
			.showText(70)
			.pointAt(util.vector()
				.blockSurface(centerLamp, Direction.WEST))
			.placeNearTarget()
			.text("They will continue to emit redstone power afterwards");
		scene.idle(60);

		scene.world()
			.hideIndependentSection(outputElement, Direction.NORTH);
		scene.world()
			.showSectionAndMerge(util.select()
				.position(centerLamp.west()), Direction.EAST, rowElement);
		scene.idle(3);
		scene.world()
			.showSectionAndMerge(util.select()
				.position(centerLamp.east()), Direction.WEST, rowElement);
		scene.idle(25);

		scene.overlay()
			.showText(50)
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(2, 1, 3), Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("When multiple lamps are arranged in a group...");
		scene.idle(40);

		ElementLink<WorldSectionElement> inputElement = scene.world()
			.showIndependentSection(input, Direction.SOUTH);
		scene.world()
			.moveSection(inputElement, util.vector()
				.of(1, 0, 0), 0);
		scene.idle(15);

		scene.world()
			.toggleRedstonePower(input);
		scene.effects()
			.indicateRedstone(util.grid()
				.at(4, 1, 1));
		scene.world()
			.cycleBlockProperty(centerLamp, RoseQuartzLampBlock.POWERING);
		scene.world()
			.cycleBlockProperty(centerLamp.east(), RoseQuartzLampBlock.POWERING);
		scene.idle(15);

		scene.overlay()
			.showText(80)
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(4, 1, 3), Direction.UP))
			.placeNearTarget()
			.text("...activating a Lamp will focus the signal to it, deactivating all others");

		scene.idle(5);
		scene.world()
			.toggleRedstonePower(button);
		scene.idle(60);

		scene.world()
			.hideIndependentSection(inputElement, Direction.NORTH);
		scene.world()
			.moveSection(rowElement, util.vector()
				.of(1, 0, 0), 10);
		scene.idle(15);
		scene.world()
			.showSectionAndMerge(comparator, Direction.EAST, rowElement);
		scene.idle(15);
		scene.world()
			.toggleRedstonePower(comparator);
		scene.world()
			.modifyBlockEntityNBT(comparator, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 13));
		scene.idle(25);

		scene.overlay()
			.showText(80)
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(1, 1, 3), Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Comparators output based on the distance to a powered lamp");
		scene.idle(90);

		scene.overlay()
			.showControls(util.vector()
				.topOf(centerLamp.east(2)), Pointing.DOWN, 20)
			.rightClick()
			.withItem(AllItems.WRENCH.asStack());
		scene.idle(6);
		scene.world()
			.cycleBlockProperty(centerLamp.east(), RoseQuartzLampBlock.POWERING);
		scene.world()
			.toggleRedstonePower(comparator);
		scene.world()
			.modifyBlockEntityNBT(comparator, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 0));
		scene.idle(20);

		scene.overlay()
			.showControls(util.vector()
				.topOf(centerLamp), Pointing.DOWN, 20)
			.rightClick()
			.withItem(AllItems.WRENCH.asStack());
		scene.idle(6);
		scene.world()
			.cycleBlockProperty(centerLamp.west(), RoseQuartzLampBlock.POWERING);
		scene.world()
			.toggleRedstonePower(comparator);
		scene.world()
			.modifyBlockEntityNBT(comparator, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 15));
		scene.idle(20);

		scene.overlay()
			.showText(80)
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(3, 1, 3), Direction.UP))
			.placeNearTarget()
			.attachKeyFrame()
			.text("The Lamps can also be toggled manually using a Wrench");
		scene.idle(50);

	}

	public static void pulseTimer(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("pulse_timer", "Redstone output of the Pulse Timer");
		scene.configureBasePlate(0, 0, 5);
		WorldInstructions world = scene.world();
		SelectionUtil select = util.select();
		world.showSection(select.layer(0), Direction.UP);

		BlockPos circuitPos = util.grid()
			.at(2, 1, 2);
		BlockPos leverPos = util.grid()
			.at(4, 1, 2);
		Vec3 circuitTop = util.vector()
			.blockSurface(circuitPos, Direction.DOWN)
			.add(0, 3 / 16f, 0);

		world.modifyBlockEntityNBT(select.position(circuitPos), PulseTimerBlockEntity.class,
			nbt -> nbt.putInt("ScrollValue", 30));
		world.showSection(select.fromTo(1, 1, 2, 0, 1, 2), Direction.UP);
		scene.idle(10);
		world.showSection(select.position(circuitPos), Direction.DOWN);
		scene.idle(8);

		for (int i = 0; i < 1; i++) {
			scene.idle(12);
			world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
			world.toggleRedstonePower(select.fromTo(1, 1, 2, 0, 1, 2));
			scene.idle(2);
			world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
			world.toggleRedstonePower(select.position(1, 1, 2));
			scene.idle(1);
			world.toggleRedstonePower(select.position(0, 1, 2));
			scene.idle(15);
		}

		scene.overlay()
			.showText(60)
			.text("Pulse Timers repeatedly emit short pulses")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.idle(13);

		for (int i = 0; i < 3; i++) {
			world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
			world.toggleRedstonePower(select.fromTo(1, 1, 2, 0, 1, 2));
			scene.idle(2);
			world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
			world.toggleRedstonePower(select.position(1, 1, 2));
			scene.idle(1);
			world.toggleRedstonePower(select.position(0, 1, 2));
			scene.idle(27);
		}

		scene.overlay()
			.showRepeaterScrollInput(circuitPos, 60);
		scene.overlay()
			.showControls(circuitTop, Pointing.DOWN, 60)
			.rightClick();
		scene.idle(10);
		scene.overlay()
			.showText(60)
			.text("Using the value panel, the time interval can be configured")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(circuitTop);

		world.modifyBlockEntityNBT(select.position(circuitPos), PulseTimerBlockEntity.class,
			nbt -> nbt.putInt("ScrollValue", 100));
		scene.idle(70);

		world.showSection(select.fromTo(3, 1, 2, 4, 1, 2), Direction.WEST);

		scene.idle(20);
		world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		world.toggleRedstonePower(select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(2);
		world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		world.toggleRedstonePower(select.position(1, 1, 2));
		scene.idle(1);
		world.toggleRedstonePower(select.position(0, 1, 2));
		scene.idle(10);

		scene.effects()
			.indicateRedstone(leverPos);
		scene.world()
			.toggleRedstonePower(util.select()
				.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(30);

		scene.overlay()
			.showText(60)
			.text("Powering the input side will pause and reset them")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector()
				.topOf(4, 0, 2));
		scene.idle(70);

		world.hideSection(select.fromTo(3, 1, 2, 4, 1, 2), Direction.EAST);
		scene.idle(5);
		world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERED);
		world.hideSection(select.position(0, 1, 2), Direction.WEST);
		scene.idle(10);

		scene.overlay()
			.showControls(circuitTop.add(-.375, 0, .375), Pointing.DOWN, 60)
			.rightClick();
		scene.idle(10);
		world.cycleBlockProperty(circuitPos, BrassDiodeBlock.INVERTED);
		world.toggleRedstonePower(select.position(1, 1, 2));
		scene.overlay()
			.showText(60)
			.text("Right-click the circuit base to invert the output")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(circuitTop.add(-.375, 0, .375));

		scene.idle(70);
		ElementLink<WorldSectionElement> link = world.showIndependentSection(select.position(0, 1, 4), Direction.EAST);
		world.moveSection(link, util.vector()
			.of(0, 0, -2), 0);
		scene.idle(10);

		world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		world.toggleRedstonePower(select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(3);
		world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		world.toggleRedstonePower(select.position(1, 1, 2));
		scene.idle(1);
		world.toggleRedstonePower(select.position(0, 1, 2));
		scene.idle(10);

		scene.overlay()
			.showText(80)
			.text("This helps trigger mechanisms that activate only without a redstone signal")
			.placeNearTarget()
			.pointAt(util.vector()
				.centerOf(0, 1, 2));

		scene.idle(86);
		world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		world.toggleRedstonePower(select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(3);
		world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		world.toggleRedstonePower(select.position(1, 1, 2));
		scene.idle(1);
		world.toggleRedstonePower(select.position(0, 1, 2));
		scene.idle(10);

		scene.markAsFinished();

		scene.idle(86);
		world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		world.toggleRedstonePower(select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(3);
		world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		world.toggleRedstonePower(select.position(1, 1, 2));
		scene.idle(1);
		world.toggleRedstonePower(select.position(0, 1, 2));
	}

}
