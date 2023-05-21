package com.simibubi.create.content.equipment.toolbox;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ToolBoxInstance extends BlockEntityInstance<ToolboxBlockEntity> implements DynamicInstance {

	private final Direction facing;
	private ModelData lid;
	private ModelData[] drawers;

	public ToolBoxInstance(MaterialManager materialManager, ToolboxBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		facing = blockState.getValue(ToolboxBlock.FACING)
				.getOpposite();
	}

	@Override
	public void init() {
		BlockState blockState = blockEntity.getBlockState();

		Instancer<ModelData> drawerModel = materialManager.defaultSolid()
				.material(Materials.TRANSFORMED)
				.getModel(AllPartialModels.TOOLBOX_DRAWER, blockState);

		drawers = new ModelData[]{drawerModel.createInstance(), drawerModel.createInstance()};
		lid = materialManager.defaultCutout()
				.material(Materials.TRANSFORMED)
				.getModel(AllPartialModels.TOOLBOX_LIDS.get(blockEntity.getColor()), blockState)
				.createInstance();

	}

	@Override
	public void remove() {
		lid.delete();

		for (ModelData drawer : drawers) {
			drawer.delete();
		}
	}

	@Override
	public void beginFrame() {

		float partialTicks = AnimationTickHolder.getPartialTicks();

		float lidAngle = blockEntity.lid.getValue(partialTicks);
		float drawerOffset = blockEntity.drawers.getValue(partialTicks);

		lid.loadIdentity()
				.translate(instancePos)
				.centre()
				.rotateY(-facing.toYRot())
				.unCentre()
				.translate(0, 6 / 16f, 12 / 16f)
				.rotateX(135 * lidAngle)
				.translateBack(0, 6 / 16f, 12 / 16f);

		for (int offset : Iterate.zeroAndOne) {
			drawers[offset].loadIdentity()
					.translate(instancePos)
					.centre()
					.rotateY(-facing.toYRot())
					.unCentre()
					.translate(0, offset * 1 / 8f, -drawerOffset * .175f * (2 - offset));
		}
	}

	@Override
	public void updateLight() {
		relight(pos, drawers);
		relight(pos, lid);
	}
}
