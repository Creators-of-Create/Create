package com.simibubi.create.content.equipment.toolbox;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class ToolBoxInstance extends AbstractBlockEntityVisual<ToolboxBlockEntity> implements DynamicVisual {

	private final Direction facing;
	private TransformedInstance lid;
	private TransformedInstance[] drawers;

	public ToolBoxInstance(VisualizationContext materialManager, ToolboxBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		facing = blockState.getValue(ToolboxBlock.FACING)
				.getOpposite();
	}

	@Override
	public void init(float pt) {
		Instancer<TransformedInstance> drawerModel = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.TOOLBOX_DRAWER), RenderStage.AFTER_BLOCK_ENTITIES);

		drawers = new TransformedInstance[]{drawerModel.createInstance(), drawerModel.createInstance()};
		lid = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.TOOLBOX_LIDS.get(blockEntity.getColor())), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();

	}

	@Override
	protected void _delete() {
		lid.delete();

		for (var drawer : drawers) {
			drawer.delete();
		}
	}

	@Override
	public void beginFrame() {

		float partialTicks = AnimationTickHolder.getPartialTicks();

		float lidAngle = blockEntity.lid.getValue(partialTicks);
		float drawerOffset = blockEntity.drawers.getValue(partialTicks);

		lid.loadIdentity()
				.translate(getVisualPosition())
				.center()
				.rotateY(-facing.toYRot())
				.uncenter()
				.translate(0, 6 / 16f, 12 / 16f)
				.rotateX(135 * lidAngle)
				.translateBack(0, 6 / 16f, 12 / 16f);

		for (int offset : Iterate.zeroAndOne) {
			drawers[offset].loadIdentity()
					.translate(getVisualPosition())
					.center()
					.rotateY(-facing.toYRot())
					.uncenter()
					.translate(0, offset * 1 / 8f, -drawerOffset * .175f * (2 - offset));
		}
	}

	@Override
	public void updateLight() {
		relight(pos, drawers);
		relight(pos, lid);
	}
}
