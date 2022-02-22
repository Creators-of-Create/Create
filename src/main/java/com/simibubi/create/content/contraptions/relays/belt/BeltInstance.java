package com.simibubi.create.content.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.function.Supplier;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.flwdata.BeltData;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LightLayer;

public class BeltInstance extends KineticTileInstance<BeltTileEntity> {

    boolean upward;
    boolean diagonal;
    boolean sideways;
    boolean vertical;
    boolean alongX;
    boolean alongZ;
    BeltSlope beltSlope;
    Direction facing;
    protected ArrayList<BeltData> keys;
    protected RotatingData pulleyKey;

    public BeltInstance(MaterialManager materialManager, BeltTileEntity tile) {
        super(materialManager, tile);

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
        DyeColor color = tile.color.orElse(null);

        for (boolean bottom : Iterate.trueAndFalse) {
            PartialModel beltPartial = BeltRenderer.getBeltPartial(diagonal, start, end, bottom);
            SpriteShiftEntry spriteShift = BeltRenderer.getSpriteShiftEntry(color, diagonal, bottom);

            Instancer<BeltData> beltModel = materialManager.defaultSolid()
                    .material(AllMaterialSpecs.BELTS)
                    .getModel(beltPartial, blockState);

            keys.add(setup(beltModel.createInstance(), bottom, spriteShift));

            if (diagonal) break;
        }

        if (tile.hasPulley()) {
            Instancer<RotatingData> pulleyModel = getPulleyModel();

            pulleyKey = setup(pulleyModel.createInstance());
        }
    }

    @Override
    public void update() {
        DyeColor color = blockEntity.color.orElse(null);

        boolean bottom = true;
        for (BeltData key : keys) {

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
    public void remove() {
        keys.forEach(InstanceData::delete);
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

    private Instancer<RotatingData> getPulleyModel() {
        Direction dir = getOrientation();

        Direction.Axis axis = dir.getAxis();

        Supplier<PoseStack> ms = () -> {
            PoseStack modelTransform = new PoseStack();
            TransformStack msr = TransformStack.cast(modelTransform);
            msr.centre();
            if (axis == Direction.Axis.X)
                msr.rotateY(90);
            if (axis == Direction.Axis.Y)
                msr.rotateX(90);
            msr.rotateX(90);
            msr.unCentre();

            return modelTransform;
        };

        return getRotatingMaterial().getModel(AllBlockPartials.BELT_PULLEY, blockState, dir, ms);
    }

    private Direction getOrientation() {
        Direction dir = blockState.getValue(BeltBlock.HORIZONTAL_FACING)
                                  .getClockWise();
        if (beltSlope == BeltSlope.SIDEWAYS)
            dir = Direction.UP;

        return dir;
    }

    private BeltData setup(BeltData key, boolean bottom, SpriteShiftEntry spriteShift) {
        boolean downward = beltSlope == BeltSlope.DOWNWARD;
        float rotX = (!diagonal && beltSlope != BeltSlope.HORIZONTAL ? 90 : 0) + (downward ? 180 : 0) + (sideways ? 90 : 0) + (vertical && alongZ ? 180 : 0);
        float rotY = facing.toYRot() + ((diagonal ^ alongX) && !downward ? 180 : 0) + (sideways && alongZ ? 180 : 0) + (vertical && alongX ? 90 : 0);
        float rotZ = (sideways ? 90 : 0) + (vertical && alongX ? 90 : 0);

        Quaternion q = new Quaternion(rotX, rotY, rotZ, true);

		key.setScrollTexture(spriteShift)
				.setScrollMult(diagonal ? 3f / 8f : 0.5f)
				.setRotation(q)
				.setRotationalSpeed(getScrollSpeed())
				.setRotationOffset(bottom ? 0.5f : 0f)
                .setColor(blockEntity)
                .setPosition(getInstancePosition())
                .setBlockLight(world.getBrightness(LightLayer.BLOCK, pos))
                .setSkyLight(world.getBrightness(LightLayer.SKY, pos));

        return key;
    }

}
