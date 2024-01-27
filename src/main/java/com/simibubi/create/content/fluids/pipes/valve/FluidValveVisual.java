package com.simibubi.create.content.fluids.pipes.valve;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class FluidValveVisual extends ShaftVisual<FluidValveBlockEntity> implements DynamicVisual {

	protected TransformedInstance pointer;
	protected boolean settled;

    protected final double xRot;
    protected final double yRot;
    protected final int pointerRotationOffset;

    public FluidValveVisual(VisualizationContext dispatcher, FluidValveBlockEntity blockEntity) {
        super(dispatcher, blockEntity);

        Direction facing = blockState.getValue(FluidValveBlock.FACING);

        yRot = AngleHelper.horizontalAngle(facing);
        xRot = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;

        Direction.Axis pipeAxis = FluidValveBlock.getPipeAxis(blockState);
        Direction.Axis shaftAxis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);

        boolean twist = pipeAxis.isHorizontal() && shaftAxis == Direction.Axis.X || pipeAxis.isVertical();
        pointerRotationOffset = twist ? 90 : 0;
        settled = false;

        pointer = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FLUID_VALVE_POINTER), RenderStage.AFTER_BLOCK_ENTITIES).createInstance();

		transformPointer();
    }

	@Override
	public void beginFrame(VisualFrameContext ctx) {
		if (blockEntity.pointer.settled() && settled)
			return;

		transformPointer();
	}

	private void transformPointer() {
		float value = blockEntity.pointer.getValue(AnimationTickHolder.getPartialTicks());
		float pointerRotation = Mth.lerp(value, 0, -90);
		settled = (value == 0 || value == 1) && blockEntity.pointer.settled();

        pointer.loadIdentity()
				 .translate(getVisualPosition())
				 .center()
				 .rotateYDegrees((float) yRot)
				 .rotateXDegrees((float) xRot)
				 .rotateYDegrees(pointerRotationOffset + pointerRotation)
				 .uncenter()
				.setChanged();
	}

    @Override
    public void updateLight() {
        super.updateLight();
        relight(pos, pointer);
    }

    @Override
    protected void _delete() {
        super._delete();
        pointer.delete();
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		consumer.accept(pointer);
	}
}
