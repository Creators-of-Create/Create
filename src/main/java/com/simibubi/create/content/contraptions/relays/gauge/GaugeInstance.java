package com.simibubi.create.content.contraptions.relays.gauge;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.ITickableInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public abstract class GaugeInstance extends ShaftInstance implements ITickableInstance {

    protected ArrayList<DialFace> faces;

    protected MatrixStack ms;

    protected GaugeInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);
    }

    @Override
    protected void init() {
        super.init();

        faces = new ArrayList<>(2);

        GaugeTileEntity gaugeTile = (GaugeTileEntity) tile;
        GaugeBlock gaugeBlock = (GaugeBlock) lastState.getBlock();

        InstancedModel<ModelData> dialModel = modelManager.getMaterial(RenderMaterials.MODELS).getModel(AllBlockPartials.GAUGE_DIAL, lastState);
        InstancedModel<ModelData> headModel = getHeadModel();

        ms = new MatrixStack();
        MatrixStacker msr = MatrixStacker.of(ms);
        msr.translate(getFloatingPos());

        float progress = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), gaugeTile.prevDialState, gaugeTile.dialState);

        for (Direction facing : Iterate.directions) {
            if (!gaugeBlock.shouldRenderHeadOnFace(world, pos, lastState, facing))
                continue;

            DialFace face = makeFace(facing, dialModel, headModel);

            faces.add(face);

            face.setupTransform(msr, progress);
        }

        updateLight();
    }

    private DialFace makeFace(Direction face, InstancedModel<ModelData> dialModel, InstancedModel<ModelData> headModel) {
        return new DialFace(face, dialModel.createInstance(), headModel.createInstance());
    }

    @Override
    public void tick() {
        GaugeTileEntity gaugeTile = (GaugeTileEntity) tile;

        if (MathHelper.epsilonEquals(gaugeTile.prevDialState, gaugeTile.dialState))
            return;

        float progress = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), gaugeTile.prevDialState, gaugeTile.dialState);

        MatrixStacker msr = MatrixStacker.of(ms);

        for (DialFace faceEntry : faces) {
            faceEntry.updateTransform(msr, progress);
        }
    }

    @Override
    public void updateLight() {
        super.updateLight();

        relight(pos, faces.stream()
                          .flatMap(Couple::stream)
                          .map(InstanceKey::getInstance));
    }

    @Override
    public void remove() {
        super.remove();

        faces.forEach(DialFace::delete);
    }

    protected abstract InstancedModel<ModelData> getHeadModel();

    private class DialFace extends Couple<InstanceKey<ModelData>> {

        Direction face;

        public DialFace(Direction face, InstanceKey<ModelData> first, InstanceKey<ModelData> second) {
            super(first, second);
            this.face = face;
        }

        private void setupTransform(MatrixStacker msr, float progress) {
            float dialPivot = 5.75f / 16;

            ms.push();
            rotateToFace(msr);

            getSecond().getInstance().setTransform(ms);

            msr.translate(0, dialPivot, dialPivot)
               .rotate(Direction.EAST, (float) (Math.PI / 2 * -progress))
               .translate(0, -dialPivot, -dialPivot);

            getFirst().getInstance().setTransform(ms);

            ms.pop();
        }

        private void updateTransform(MatrixStacker msr, float progress) {
            float dialPivot = 5.75f / 16;

            ms.push();

            rotateToFace(msr)
                    .translate(0, dialPivot, dialPivot)
                    .rotate(Direction.EAST, (float) (Math.PI / 2 * -progress))
                    .translate(0, -dialPivot, -dialPivot);

            getFirst().getInstance().setTransform(ms);

            ms.pop();
        }

        protected MatrixStacker rotateToFace(MatrixStacker msr) {
            return msr.centre()
                      .rotate(Direction.UP, (float) ((-face.getHorizontalAngle() - 90) / 180 * Math.PI))
                      .unCentre();
        }

        private void delete() {
            getFirst().delete();
            getSecond().delete();
        }
    }

    public static class Speed extends GaugeInstance {
        public Speed(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
            super(dispatcher, tile);
        }

        @Override
        protected InstancedModel<ModelData> getHeadModel() {
            return modelManager.getMaterial(RenderMaterials.MODELS).getModel(AllBlockPartials.GAUGE_HEAD_SPEED, lastState);
        }
    }

    public static class Stress extends GaugeInstance {
        public Stress(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
            super(dispatcher, tile);
        }

        @Override
        protected InstancedModel<ModelData> getHeadModel() {
            return modelManager.getMaterial(RenderMaterials.MODELS).getModel(AllBlockPartials.GAUGE_HEAD_STRESS, lastState);
        }
    }
}
