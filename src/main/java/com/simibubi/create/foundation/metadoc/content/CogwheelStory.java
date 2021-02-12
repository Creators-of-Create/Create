package com.simibubi.create.foundation.metadoc.content;

import com.simibubi.create.foundation.metadoc.MetaDocScene.SceneBuilder;
import com.simibubi.create.foundation.metadoc.MetaDocStoryBoard;
import com.simibubi.create.foundation.metadoc.Select;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class CogwheelStory extends MetaDocStoryBoard {

	private int index;

	public CogwheelStory(int index) {
		this.index = index;
	}

	@Override
	public String getSchematicName() {
		return "cogwheel/s" + index;
	}

	@Override
	public String getStoryTitle() {
		return "My First Metadoc Story, Part " + index;
	}

	@Override
	public void program(SceneBuilder scene, Vec3i worldSize) {
		scene.showBasePlate();
		scene.idle(10);

		scene.showSection(Select.cuboid(BlockPos.ZERO.up(), worldSize), Direction.DOWN);
		scene.multiplyKineticSpeed(scene.everywhere(), 2);
		scene.rotateCameraY(90);
		scene.createParrotOn(new BlockPos(0.5, 2.5, 1.5), Direction.DOWN);
//		scene.idle(10);
//		scene.createParrotOn(new BlockPos(5, 1, 5), Direction.DOWN);
//		scene.idle(10);
//		scene.createParrotOn(new BlockPos(0, 1, 5), Direction.DOWN);
		
		scene.idle(40);
		scene.showText(new Vec3d(0.5, 2, 1.5), "swinging_text", "there's a parrot", 10, 50);
		scene.idle(10);
		scene.rotateCameraY(180);
		
	}

}
