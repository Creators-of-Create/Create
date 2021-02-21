package com.simibubi.create.foundation.ponder.content;

import static com.simibubi.create.foundation.ponder.content.PonderPalette.WHITE;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.utility.Pointing;
import com.tterrag.registrate.util.entry.BlockEntry;

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
		scene.overlay.showTargetedText(WHITE, util.vector.at(3, 1.5, 2.5), "shaft_relay",
			"Shafts will relay rotation in a straight line.", 1000);

		scene.idle(20);
		scene.markAsFinished();
	}

	public static void shaftsCanBeEncased(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Encasing Shafts");
		scene.showBasePlate();

		Selection shaft = util.select.cuboid(new BlockPos(0, 1, 2), new Vec3i(4, 0, 2));
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
		scene.world.setKineticSpeed(shaft, -112);
		scene.idle(10);

		BlockEntry<EncasedShaftBlock> brassEncased = AllBlocks.BRASS_ENCASED_SHAFT;
		ItemStack brassCasingItem = AllBlocks.BRASS_CASING.asStack();

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(1, 0, 2), Pointing.UP).rightClick()
			.withItem(brassCasingItem), 60);
		scene.idle(7);
		scene.world.setBlocks(brass, brassEncased.getDefaultState()
			.with(EncasedShaftBlock.AXIS, Axis.X), true);
		scene.world.setKineticSpeed(shaft, -112);

		scene.idle(10);
		scene.overlay.showTargetedText(WHITE, util.vector.at(1.5, 2, 2.5), "shaft_can_be_encased",
			"Andesite or Brass Casing can be used to encase them.", 1000);
	}

}
