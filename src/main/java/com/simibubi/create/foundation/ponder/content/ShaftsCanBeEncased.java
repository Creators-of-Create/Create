package com.simibubi.create.foundation.ponder.content;

import static com.simibubi.create.foundation.ponder.content.PonderPalette.WHITE;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.PonderStoryBoard;
import com.simibubi.create.foundation.ponder.Select;

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

		scene.setBlocks(andesite, AllBlocks.ANDESITE_ENCASED_SHAFT.getDefaultState()
			.with(EncasedShaftBlock.AXIS, Axis.X));
		scene.setKineticSpeed(shaft, -112);
		scene.idle(10);
		
		scene.setBlocks(brass, AllBlocks.BRASS_ENCASED_SHAFT.getDefaultState()
			.with(EncasedShaftBlock.AXIS, Axis.X));
		scene.setKineticSpeed(shaft, -112);

		scene.idle(10);
		scene.showTargetedText(WHITE, new Vec3d(1.5, 2, 2.5), "shaft_can_be_encased",
			"I could use Brass or Andesite Casing to hide them.", 1000);
	}

}
