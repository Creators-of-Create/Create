package com.simibubi.create.content.schematics.cannon;

import java.util.function.Consumer;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;

import net.minecraft.core.Direction;

public class SchematicannonVisual extends AbstractBlockEntityVisual<SchematicannonBlockEntity> implements SimpleDynamicVisual {

    private final TransformedInstance connector;
    private final TransformedInstance pipe;

    public SchematicannonVisual(VisualizationContext context, SchematicannonBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        connector = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.SCHEMATICANNON_CONNECTOR)).createInstance();
        pipe = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.SCHEMATICANNON_PIPE)).createInstance();
	}

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float partialTicks = ctx.partialTick();

        double[] cannonAngles = SchematicannonRenderer.getCannonAngles(blockEntity, pos, partialTicks);

        double yaw = cannonAngles[0];
        double pitch = cannonAngles[1];

        double recoil = SchematicannonRenderer.getRecoil(blockEntity, partialTicks);

        PoseStack ms = new PoseStack();
        var msr = TransformStack.of(ms);

        msr.translate(getVisualPosition());

        ms.pushPose();
        msr.center();
        msr.rotate((float) ((yaw + 90) / 180 * Math.PI), Direction.UP);
        msr.uncenter();
        connector.setTransform(ms);
        ms.popPose();

        msr.translate(.5f, 15 / 16f, .5f);
        msr.rotate((float) ((yaw + 90) / 180 * Math.PI), Direction.UP);
        msr.rotate((float) (pitch / 180 * Math.PI), Direction.SOUTH);
        msr.translateBack(.5f, 15 / 16f, .5f);
        msr.translate(0, -recoil / 100, 0);

        pipe.setTransform(ms);

		connector.setChanged();
		pipe.setChanged();
    }

    @Override
    protected void _delete() {
        connector.delete();
        pipe.delete();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(pos, connector, pipe);
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(connector);
		consumer.accept(pipe);
	}
}
