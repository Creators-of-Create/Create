package com.simibubi.create.content.contraptions.actors.psi;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;

import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class PIInstance {
	private final MaterialManager materialManager;
	private final BlockState blockState;
	private final BlockPos instancePos;
	private final float angleX;
	private final float angleY;

	private boolean lit;
	ModelData middle;
	ModelData top;

	public PIInstance(MaterialManager materialManager, BlockState blockState, BlockPos instancePos) {
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
				.material(Materials.TRANSFORMED)
				.getModel(PortableStorageInterfaceRenderer.getMiddleForState(blockState, lit), blockState)
				.createInstance();
		top = materialManager.defaultSolid()
				.material(Materials.TRANSFORMED)
				.getModel(PortableStorageInterfaceRenderer.getTopForState(blockState), blockState)
				.createInstance();
	}

	public void beginFrame(float progress) {
		middle.loadIdentity()
				.translate(instancePos)
				.centre()
				.rotateY(angleY)
				.rotateX(angleX)
				.unCentre();

		top.loadIdentity()
				.translate(instancePos)
				.centre()
				.rotateY(angleY)
				.rotateX(angleX)
				.unCentre();

		middle.translate(0, progress * 0.5f + 0.375f, 0);
		top.translate(0, progress, 0);

	}

	public void tick(boolean lit) {
		if (this.lit != lit) {
			this.lit = lit;
			materialManager.defaultSolid()
					.material(Materials.TRANSFORMED)
					.getModel(PortableStorageInterfaceRenderer.getMiddleForState(blockState, lit), blockState)
					.stealInstance(middle);
		}
	}

	public void remove() {
		middle.delete();
		top.delete();
	}
}
