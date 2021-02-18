package com.simibubi.create.foundation.ponder.content;

import static com.simibubi.create.foundation.ponder.content.PonderPalette.WHITE;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.PonderStoryBoard;
import com.simibubi.create.foundation.ponder.Select;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.utility.Pointing;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

class ShaftsCanBeEncased extends PonderStoryBoard {

	@Override
	public String getSchematicName() {
		return "shaft/encasing_shafts";
	}

	@Override
	public String getStoryTitle() {
		return "Encasing Shafts";
	}

	@Override
	public void program(SceneBuilder scene, SceneBuildingUtil util) {
		scene.showBasePlate();

		Select shaft = Select.cuboid(new BlockPos(0, 1, 2), new Vec3i(4, 0, 2));
		Select andesite = Select.pos(3, 1, 2);
		Select brass = Select.pos(1, 1, 2);

		scene.showSection(shaft, Direction.DOWN);
		scene.idle(20);

		BlockEntry<EncasedShaftBlock> andesiteEncased = AllBlocks.ANDESITE_ENCASED_SHAFT;
		scene.showControls(new InputWindowElement(util.topOf(3, 1, 2), Pointing.DOWN).rightClick()
			.withItem(AllBlocks.ANDESITE_CASING.asStack()), 60);
		scene.idle(7);
		scene.setBlocks(andesite, andesiteEncased.getDefaultState()
			.with(EncasedShaftBlock.AXIS, Axis.X));
		scene.setKineticSpeed(shaft, -112);
		scene.idle(10);

		BlockEntry<EncasedShaftBlock> brassEncased = AllBlocks.BRASS_ENCASED_SHAFT;
		scene.showControls(new InputWindowElement(util.topOf(1, 0, 2), Pointing.UP).rightClick()
			.withItem(AllBlocks.BRASS_CASING.asStack()), 60);
		scene.idle(7);
		scene.setBlocks(brass, brassEncased.getDefaultState()
			.with(EncasedShaftBlock.AXIS, Axis.X));
		scene.setKineticSpeed(shaft, -112);

		scene.idle(10);
		scene.showTargetedText(WHITE, new Vec3d(1.5, 2, 2.5), "shaft_can_be_encased",
			"Andesite or Brass Casing can be used to encase them.", 1000);
	}

}
