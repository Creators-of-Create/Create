package com.simibubi.create.content.kinetics.belt;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.joml.Quaternionf;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.Iterate;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LightLayer;

public class BeltVisual extends KineticBlockEntityVisual<BeltBlockEntity> {

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

    public BeltVisual(VisualizationContext context, BeltBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

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

            Instancer<BeltInstance> beltModel = instancerProvider.instancer(AllInstanceTypes.BELT, Models.partial(beltPartial));

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
					.setRotationalSpeed(getScrollSpeed())
					.setChanged();
            bottom = false;
        }

        if (pulleyKey != null) {
            updateRotation(pulleyKey);
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(keys.toArray(FlatLit[]::new));

        if (pulleyKey != null) relight(pulleyKey);
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

        var model = Models.partial(AllPartialModels.BELT_PULLEY, dir.getAxis(), (axis11, modelTransform1) -> {
            var msr = TransformStack.of(modelTransform1);
            msr.center();
            if (axis11 == Direction.Axis.X) msr.rotateYDegrees(90);
            if (axis11 == Direction.Axis.Y) msr.rotateXDegrees(90);
            msr.rotateXDegrees(90);
            msr.uncenter();
        });

		return instancerProvider.instancer(AllInstanceTypes.ROTATING, model);
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
                .light(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos))
				.setChanged();

        return key;
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		if (pulleyKey != null) {
			consumer.accept(pulleyKey);
		}
		keys.forEach(consumer);
	}
}
