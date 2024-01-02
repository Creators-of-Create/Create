package com.simibubi.create.content.contraptions.actors.psi;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class PIInstance {
	private final VisualizationContext materialManager;
	private final BlockState blockState;
	private final BlockPos instancePos;
	private final float angleX;
	private final float angleY;

	private boolean lit;
	TransformedInstance middle;
	TransformedInstance top;

	public PIInstance(VisualizationContext materialManager, BlockState blockState, BlockPos instancePos) {
		this.materialManager = materialManager;
		this.blockState = blockState;
		this.instancePos = instancePos;
		Direction facing = blockState.getValue(PortableStorageInterfaceBlock.FACING);
		angleX = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
		angleY = AngleHelper.horizontalAngle(facing);
	}

	public void init(boolean lit) {
		this.lit = lit;
		middle = materialManager.defaultSolid()
				.material(InstanceTypes.TRANSFORMED)
				.getModel(PortableStorageInterfaceRenderer.getMiddleForState(blockState, lit), blockState)
				.createInstance();
		top = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.block(PortableStorageInterfaceRenderer.getTopForState(blockState), RenderStage.AFTER_BLOCK_ENTITIES), blockState)
				.createInstance();
	}

	public void beginFrame(float progress) {
		middle.loadIdentity()
				.translate(instancePos)
				.center()
				.rotateY(angleY)
				.rotateX(angleX)
				.uncenter();

		top.loadIdentity()
				.translate(instancePos)
				.center()
				.rotateY(angleY)
				.rotateX(angleX)
				.uncenter();

		middle.translate(0, progress * 0.5f + 0.375f, 0);
		top.translate(0, progress, 0);

	}

	public void tick(boolean lit) {
		if (this.lit != lit) {
			this.lit = lit;
			materialManager.defaultSolid()
					.material(InstanceTypes.TRANSFORMED)
					.getModel(PortableStorageInterfaceRenderer.getMiddleForState(blockState, lit), blockState)
					.stealInstance(middle);
		}
	}

	public void remove() {
		middle.delete();
		top.delete();
	}
}
