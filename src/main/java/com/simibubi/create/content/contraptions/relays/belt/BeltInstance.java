package com.simibubi.create.content.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.renderer.Quaternion;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraft.world.LightType;

public class BeltInstance extends KineticTileInstance<BeltTileEntity> {

    boolean upward;
    boolean diagonal;
    boolean sideways;
    boolean vertical;
    boolean alongX;
    boolean alongZ;
    BeltSlope beltSlope;
    Direction facing;
    protected ArrayList<InstanceKey<BeltData>> keys;
    protected InstanceKey<RotatingData> pulleyKey;

    public BeltInstance(InstancedTileRenderer<?> modelManager, BeltTileEntity tile) {
        super(modelManager, tile);

        if (!AllBlocks.BELT.has(blockState))
            return;

        keys = new ArrayList<>(2);

        beltSlope = blockState.get(BeltBlock.SLOPE);
        facing = blockState.get(BeltBlock.HORIZONTAL_FACING);
        upward = beltSlope == BeltSlope.UPWARD;
        diagonal = beltSlope.isDiagonal();
        sideways = beltSlope == BeltSlope.SIDEWAYS;
        vertical = beltSlope == BeltSlope.VERTICAL;
        alongX = facing.getAxis() == Direction.Axis.X;
        alongZ = facing.getAxis() == Direction.Axis.Z;

        BeltPart part = blockState.get(BeltBlock.PART);
        boolean start = part == BeltPart.START;
        boolean end = part == BeltPart.END;
        DyeColor color = tile.color.orElse(null);

        for (boolean bottom : Iterate.trueAndFalse) {
            AllBlockPartials beltPartial = BeltRenderer.getBeltPartial(diagonal, start, end, bottom);
            SpriteShiftEntry spriteShift = BeltRenderer.getSpriteShiftEntry(color, diagonal, bottom);

            InstancedModel<BeltData> beltModel = beltPartial.renderOnBelt(modelManager, blockState);

            keys.add(setup(beltModel.createInstance(), bottom, spriteShift));

            if (diagonal) break;
        }

        if (tile.hasPulley()) {
            InstancedModel<RotatingData> pulleyModel = getPulleyModel();

            pulleyKey = setup(pulleyModel.createInstance());
        }
    }

    @Override
    public void update() {
        DyeColor color = tile.color.orElse(null);

        boolean bottom = true;
        for (InstanceKey<BeltData> key : keys) {

            SpriteShiftEntry spriteShiftEntry = BeltRenderer.getSpriteShiftEntry(color, diagonal, bottom);
            key.getInstance()
               .setScrollTexture(spriteShiftEntry)
               .setColor(tile)
               .setRotationalSpeed(getScrollSpeed());
            bottom = false;
        }

        if (pulleyKey != null) {
            updateRotation(pulleyKey.getInstance());
        }
    }

    @Override
    public void updateLight() {
        relight(pos, keys.stream().map(InstanceKey::getInstance));

        if (pulleyKey != null) relight(pos, pulleyKey.getInstance());
    }

    @Override
    public void remove() {
        keys.forEach(InstanceKey::delete);
        keys.clear();
        if (pulleyKey != null) pulleyKey.delete();
        pulleyKey = null;
    }

    private float getScrollSpeed() {
        float speed = tile.getSpeed();
        if (((facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) ^ upward) ^
                ((alongX && !diagonal) || (alongZ && diagonal))) {
            speed = -speed;
        }
        if (sideways && (facing == Direction.SOUTH || facing == Direction.WEST) || (vertical && facing == Direction.EAST))
            speed = -speed;

        return speed;
    }

    private InstancedModel<RotatingData> getPulleyModel() {
        Direction dir = getOrientation();

        Direction.Axis axis = dir.getAxis();

        Supplier<MatrixStack> ms = () -> {
            MatrixStack modelTransform = new MatrixStack();
            MatrixStacker msr = MatrixStacker.of(modelTransform);
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
        Direction dir = blockState.get(BeltBlock.HORIZONTAL_FACING)
                                  .rotateY();
        if (beltSlope == BeltSlope.SIDEWAYS)
            dir = Direction.UP;

        return dir;
    }

    private InstanceKey<BeltData> setup(InstanceKey<BeltData> key, boolean bottom, SpriteShiftEntry spriteShift) {
        boolean downward = beltSlope == BeltSlope.DOWNWARD;
        float rotX = (!diagonal && beltSlope != BeltSlope.HORIZONTAL ? 90 : 0) + (downward ? 180 : 0) + (sideways ? 90 : 0) + (vertical && alongZ ? 180 : 0);
        float rotY = facing.getHorizontalAngle() + ((diagonal ^ alongX) && !downward ? 180 : 0) + (sideways && alongZ ? 180 : 0) + (vertical && alongX ? 90 : 0);
        float rotZ = (sideways ? 90 : 0) + (vertical && alongX ? 90 : 0);

        Quaternion q = new Quaternion(rotX, rotY, rotZ, true);

		key.getInstance()
				.setScrollTexture(spriteShift)
				.setScrollMult(diagonal ? 3f / 8f : 0.5f)
				.setRotation(q)
				.setRotationalSpeed(getScrollSpeed())
				.setRotationOffset(bottom ? 0.5f : 0f)
                .setColor(tile)
                .setPosition(getInstancePosition())
                .setBlockLight(world.getLightLevel(LightType.BLOCK, pos))
                .setSkyLight(world.getLightLevel(LightType.SKY, pos));

        return key;
    }

}
