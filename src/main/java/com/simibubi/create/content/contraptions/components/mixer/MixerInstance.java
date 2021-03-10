package com.simibubi.create.content.contraptions.components.mixer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.ShaftlessCogInstance;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class MixerInstance extends ShaftlessCogInstance implements ITickableInstance {
    public static void register(TileEntityType<? extends KineticTileEntity> type) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, MixerInstance::new));
    }

    private InstanceKey<RotatingData> mixerHead;
    private InstanceKey<ModelData> mixerPole;

    public MixerInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);
    }

    @Override
    protected void init() {
        super.init();

        mixerHead = rotatingMaterial().getModel(AllBlockPartials.MECHANICAL_MIXER_HEAD, lastState)
                                      .createInstance();

        mixerHead.getInstance()
                 .setRotationAxis(Direction.Axis.Y);

        mixerPole = modelManager.getMaterial(RenderMaterials.MODELS)
                                .getModel(AllBlockPartials.MECHANICAL_MIXER_POLE, lastState)
                                .createInstance();


        MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) tile;
        float renderedHeadOffset = getRenderedHeadOffset(mixer);

        transformPole(renderedHeadOffset);
        transformHead(mixer, renderedHeadOffset);
        updateLight();
    }

    @Override
    public void tick() {
        MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) tile;

        float renderedHeadOffset = getRenderedHeadOffset(mixer);

        if (mixer.running) {
            transformPole(renderedHeadOffset);
        }

        transformHead(mixer, renderedHeadOffset);
    }

    private void transformHead(MechanicalMixerTileEntity mixer, float renderedHeadOffset) {
        float speed = mixer.getRenderedHeadRotationSpeed(AnimationTickHolder.getPartialTicks());

        mixerHead.getInstance()
                 .setPosition(pos)
                 .nudge(0, -renderedHeadOffset, 0)
                 .setRotationalSpeed(speed * 2);
    }

    private void transformPole(float renderedHeadOffset) {
        MatrixStack ms = new MatrixStack();

        MatrixStacker msr = MatrixStacker.of(ms);
        msr.translate(getFloatingPos());
        msr.translate(0, -renderedHeadOffset, 0);

        mixerPole.getInstance().setTransformNoCopy(ms);
    }

    private float getRenderedHeadOffset(MechanicalMixerTileEntity mixer) {
        return mixer.getRenderedHeadOffset(AnimationTickHolder.getPartialTicks());
    }

    @Override
    public void updateLight() {
        super.updateLight();

        BlockPos down = pos.down();
        mixerHead.getInstance()
                 .setBlockLight(world.getLightLevel(LightType.BLOCK, down))
                 .setSkyLight(world.getLightLevel(LightType.SKY, down));

        mixerPole.getInstance()
                 .setBlockLight(world.getLightLevel(LightType.BLOCK, pos))
                 .setSkyLight(world.getLightLevel(LightType.SKY, pos));
    }

    @Override
    public void remove() {
        super.remove();
        mixerHead.delete();
        mixerPole.delete();
    }
}
