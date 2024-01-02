package com.simibubi.create.content.kinetics.belt;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.AbstractInstance;
import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityInstance;
import com.simibubi.create.content.kinetics.base.flwdata.BeltInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingInstance;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LightLayer;

public class BeltVisual extends KineticBlockEntityInstance<BeltBlockEntity> {

    boolean upward;
    boolean diagonal;
    boolean sideways;
    boolean vertical;
    boolean alongX;
    boolean alongZ;
    BeltSlope beltSlope;
    Direction facing;
    protected ArrayList<BeltInstance> keys;
    protected RotatingInstance pulleyKey;

    public BeltVisual(VisualizationContext materialManager, BeltBlockEntity blockEntity) {
        super(materialManager, blockEntity);

        if (!AllBlocks.BELT.has(blockState))
            return;

        keys = new ArrayList<>(2);

        beltSlope = blockState.getValue(BeltBlock.SLOPE);
        facing = blockState.getValue(BeltBlock.HORIZONTAL_FACING);
        upward = beltSlope == BeltSlope.UPWARD;
        diagonal = beltSlope.isDiagonal();
        sideways = beltSlope == BeltSlope.SIDEWAYS;
        vertical = beltSlope == BeltSlope.VERTICAL;
        alongX = facing.getAxis() == Direction.Axis.X;
        alongZ = facing.getAxis() == Direction.Axis.Z;

        BeltPart part = blockState.getValue(BeltBlock.PART);
        boolean start = part == BeltPart.START;
        boolean end = part == BeltPart.END;
        DyeColor color = blockEntity.color.orElse(null);

        for (boolean bottom : Iterate.trueAndFalse) {
            PartialModel beltPartial = BeltRenderer.getBeltPartial(diagonal, start, end, bottom);
            SpriteShiftEntry spriteShift = BeltRenderer.getSpriteShiftEntry(color, diagonal, bottom);

            Instancer<BeltInstance> beltModel = instancerProvider.instancer(AllInstanceTypes.BELTS, Models.partial(beltPartial), RenderStage.AFTER_BLOCK_ENTITIES);

            keys.add(setup(beltModel.createInstance(), bottom, spriteShift));

            if (diagonal) break;
        }

        if (blockEntity.hasPulley()) {
            Instancer<RotatingInstance> pulleyModel = getPulleyModel();

            pulleyKey = setup(pulleyModel.createInstance());
        }
    }

    @Override
    public void update(float pt) {
        DyeColor color = blockEntity.color.orElse(null);

        boolean bottom = true;
        for (BeltInstance key : keys) {

            SpriteShiftEntry spriteShiftEntry = BeltRenderer.getSpriteShiftEntry(color, diagonal, bottom);
            key.setScrollTexture(spriteShiftEntry)
               .setColor(blockEntity)
               .setRotationalSpeed(getScrollSpeed());
            bottom = false;
        }

        if (pulleyKey != null) {
            updateRotation(pulleyKey);
        }
    }

    @Override
    public void updateLight() {
        relight(pos, keys.stream());

        if (pulleyKey != null) relight(pos, pulleyKey);
    }

    @Override
    protected void _delete() {
        keys.forEach(AbstractInstance::delete);
        keys.clear();
        if (pulleyKey != null) pulleyKey.delete();
        pulleyKey = null;
    }

    private float getScrollSpeed() {
        float speed = blockEntity.getSpeed();
        if (((facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) ^ upward) ^
                ((alongX && !diagonal) || (alongZ && diagonal))) {
            speed = -speed;
        }
        if (sideways && (facing == Direction.SOUTH || facing == Direction.WEST) || (vertical && facing == Direction.EAST))
            speed = -speed;

        return speed;
    }

    private Instancer<RotatingInstance> getPulleyModel() {
        Direction dir = getOrientation();

        Direction.Axis axis = dir.getAxis();

        Supplier<PoseStack> ms = () -> {
            PoseStack modelTransform = new PoseStack();
            TransformStack msr = TransformStack.of(modelTransform);
            msr.center();
            if (axis == Direction.Axis.X)
                msr.rotateY(90);
            if (axis == Direction.Axis.Y)
                msr.rotateX(90);
            msr.rotateX(90);
            msr.uncenter();

            return modelTransform;
        };

		return materialManager.defaultSolid()
				.material(AllInstanceTypes.ROTATING)
				.getModel(AllPartialModels.BELT_PULLEY, blockState, dir, ms);
    }

    private Direction getOrientation() {
        Direction dir = blockState.getValue(BeltBlock.HORIZONTAL_FACING)
                                  .getClockWise();
        if (beltSlope == BeltSlope.SIDEWAYS)
            dir = Direction.UP;

        return dir;
    }

    private BeltInstance setup(BeltInstance key, boolean bottom, SpriteShiftEntry spriteShift) {
        boolean downward = beltSlope == BeltSlope.DOWNWARD;
        float rotX = (!diagonal && beltSlope != BeltSlope.HORIZONTAL ? 90 : 0) + (downward ? 180 : 0) + (sideways ? 90 : 0) + (vertical && alongZ ? 180 : 0);
        float rotY = facing.toYRot() + ((diagonal ^ alongX) && !downward ? 180 : 0) + (sideways && alongZ ? 180 : 0) + (vertical && alongX ? 90 : 0);
        float rotZ = (sideways ? 90 : 0) + (vertical && alongX ? 90 : 0);

        Quaternionf q = new Quaternionf().rotationXYZ(rotX * Mth.DEG_TO_RAD, rotY * Mth.DEG_TO_RAD, rotZ * Mth.DEG_TO_RAD);

		key.setScrollTexture(spriteShift)
				.setScrollMult(diagonal ? 3f / 8f : 0.5f)
				.setRotation(q)
				.setRotationalSpeed(getScrollSpeed())
				.setRotationOffset(bottom ? 0.5f : 0f)
                .setColor(blockEntity)
                .setPosition(getVisualPosition())
                .setBlockLight(level.getBrightness(LightLayer.BLOCK, pos))
                .setSkyLight(level.getBrightness(LightLayer.SKY, pos));

        return key;
    }

}
