package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.redstone.RoseQuartzLampBlock;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.ElementLink;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class RedstoneScenes2 {

	public static void roseQuartzLamp(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("rose_quartz_lamp", "Rose Quartz Lamps");
		scene.configureBasePlate(0, 0, 7);

		BlockPos centerLamp = util.grid().at(3, 1, 3);
		Selection input = util.select().fromTo(3, 1, 1, 3, 1, 2);
		Selection button = util.select().position(3, 1, 1);
		Selection wire = util.select().position(3, 1, 2);
		Selection output = util.select().fromTo(5, 1, 2, 5, 1, 1);
		Selection comparator = util.select().fromTo(1, 1, 3, 0, 1, 3);

		scene.showBasePlate();
		scene.idle(15);

		ElementLink<WorldSectionElement> rowElement =
			scene.world().showIndependentSection(util.select().position(centerLamp), Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(input, Direction.SOUTH);
		scene.idle(15);

		scene.world().toggleRedstonePower(input);
		scene.effects().indicateRedstone(util.grid().at(3, 1, 1));
		scene.world().cycleBlockProperty(centerLamp, RoseQuartzLampBlock.POWERING);
		scene.idle(15);

		scene.overlay().showText(70)
			.pointAt(util.vector().blockSurface(centerLamp, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Rose Quartz Lamps activate on a Redstone signal");
		scene.idle(5);
		scene.world().toggleRedstonePower(button);
		scene.idle(55);

		scene.world().hideSection(input, Direction.EAST);
		scene.idle(10);
		ElementLink<WorldSectionElement> outputElement = scene.world().showIndependentSection(output, Direction.EAST);
		scene.world().moveSection(outputElement, util.vector().of(-2, 0, 0), 0);
		scene.idle(10);
		scene.world().toggleRedstonePower(wire);
		scene.world().toggleRedstonePower(output);
		scene.idle(5);

		scene.overlay().showText(70)
			.pointAt(util.vector().blockSurface(centerLamp, Direction.WEST))
			.placeNearTarget()
			.text("They will continue to emit redstone power afterwards");
		scene.idle(60);

		scene.world().hideIndependentSection(outputElement, Direction.NORTH);
		scene.world().showSectionAndMerge(util.select().position(centerLamp.west()), Direction.EAST, rowElement);
		scene.idle(3);
		scene.world().showSectionAndMerge(util.select().position(centerLamp.east()), Direction.WEST, rowElement);
		scene.idle(25);

		scene.overlay().showText(50)
			.pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("When multiple lamps are arranged in a group...");
		scene.idle(40);

		ElementLink<WorldSectionElement> inputElement = scene.world().showIndependentSection(input, Direction.SOUTH);
		scene.world().moveSection(inputElement, util.vector().of(1, 0, 0), 0);
		scene.idle(15);

		scene.world().toggleRedstonePower(input);
		scene.effects().indicateRedstone(util.grid().at(4, 1, 1));
		scene.world().cycleBlockProperty(centerLamp, RoseQuartzLampBlock.POWERING);
		scene.world().cycleBlockProperty(centerLamp.east(), RoseQuartzLampBlock.POWERING);
		scene.idle(15);

		scene.overlay().showText(80)
			.pointAt(util.vector().blockSurface(util.grid().at(4, 1, 3), Direction.UP))
			.placeNearTarget()
			.text("...activating a Lamp will focus the signal to it, deactivating all others");

		scene.idle(5);
		scene.world().toggleRedstonePower(button);
		scene.idle(60);

		scene.world().hideIndependentSection(inputElement, Direction.NORTH);
		scene.world().moveSection(rowElement, util.vector().of(1, 0, 0), 10);
		scene.idle(15);
		scene.world().showSectionAndMerge(comparator, Direction.EAST, rowElement);
		scene.idle(15);
		scene.world().toggleRedstonePower(comparator);
		scene.world()
				.modifyBlockEntityNBT(comparator, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 13));
		scene.idle(25);

		scene.overlay().showText(80)
			.pointAt(util.vector().blockSurface(util.grid().at(1, 1, 3), Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Comparators output based on the distance to a powered lamp");
		scene.idle(90);

		scene.overlay()
			.showControls(new InputWindowElement(util.vector().topOf(centerLamp.east(2)), Pointing.DOWN).rightClick()
				.withItem(AllItems.WRENCH.asStack()), 20);
		scene.idle(6);
		scene.world().cycleBlockProperty(centerLamp.east(), RoseQuartzLampBlock.POWERING);
		scene.world().toggleRedstonePower(comparator);
		scene.world()
				.modifyBlockEntityNBT(comparator, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 0));
		scene.idle(20);

		scene.overlay().showControls(new InputWindowElement(util.vector().topOf(centerLamp), Pointing.DOWN).rightClick()
			.withItem(AllItems.WRENCH.asStack()), 20);
		scene.idle(6);
		scene.world().cycleBlockProperty(centerLamp.west(), RoseQuartzLampBlock.POWERING);
		scene.world().toggleRedstonePower(comparator);
		scene.world()
				.modifyBlockEntityNBT(comparator, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 15));
		scene.idle(20);

		scene.overlay().showText(80)
			.pointAt(util.vector().blockSurface(util.grid().at(3, 1, 3), Direction.UP))
			.placeNearTarget()
			.attachKeyFrame()
			.text("The Lamps can also be toggled manually using a Wrench");
		scene.idle(50);

	}

}
