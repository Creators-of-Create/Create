package com.simibubi.create.content.schematics.block;

import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;

public class SchematicannonInstance extends BlockEntityInstance<SchematicannonBlockEntity> implements DynamicInstance {

    private final ModelData connector;
    private final ModelData pipe;

    public SchematicannonInstance(MaterialManager materialManager, SchematicannonBlockEntity blockEntity) {
        super(materialManager, blockEntity);

        Material<ModelData> mat = getTransformMaterial();

        connector = mat.getModel(AllPartialModels.SCHEMATICANNON_CONNECTOR, blockState).createInstance();
        pipe = mat.getModel(AllPartialModels.SCHEMATICANNON_PIPE, blockState).createInstance();
	}

    @Override
    public void beginFrame() {
        float partialTicks = AnimationTickHolder.getPartialTicks();

        double[] cannonAngles = SchematicannonRenderer.getCannonAngles(blockEntity, pos, partialTicks);

        double yaw = cannonAngles[0];
        double pitch = cannonAngles[1];

        double recoil = SchematicannonRenderer.getRecoil(blockEntity, partialTicks);

        PoseStack ms = new PoseStack();
        TransformStack msr = TransformStack.cast(ms);

        msr.translate(getInstancePosition());

        ms.pushPose();
        msr.centre();
        msr.rotate(Direction.UP, (float) ((yaw + 90) / 180 * Math.PI));
        msr.unCentre();
        connector.setTransform(ms);
        ms.popPose();

        msr.translate(.5f, 15 / 16f, .5f);
        msr.rotate(Direction.UP, (float) ((yaw + 90) / 180 * Math.PI));
        msr.rotate(Direction.SOUTH, (float) (pitch / 180 * Math.PI));
        msr.translateBack(.5f, 15 / 16f, .5f);
        msr.translate(0, -recoil / 100, 0);

        pipe.setTransform(ms);
    }

    @Override
    public void remove() {
        connector.delete();
        pipe.delete();
    }

    @Override
    public void updateLight() {
        relight(pos, connector, pipe);
    }
}
