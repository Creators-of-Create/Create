package com.simibubi.create.content.contraptions.fluids;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.BitSet;

public class TankRenderInfo {
    private final IFluidTank tank;
    private final AxisAlignedBB bounds;
    private final BitSet faces = new BitSet(6);

    public TankRenderInfo(FluidStack stack, int capacity, AxisAlignedBB bounds, Direction... renderFaces) {
        FluidTank tank = new FluidTank(capacity);
        tank.setFluid(stack);
        this.tank = tank;
        this.bounds = bounds;
        if (renderFaces.length == 0) {
            faces.set(0, 6, true);
        } else {
            for (Direction face : renderFaces) {
                faces.set(face.getIndex(), true);
            }
        }
    }

    public TankRenderInfo(IFluidTank tank, AxisAlignedBB bounds, Direction... renderFaces) {
        this(tank.getFluid(), tank.getCapacity(), bounds, renderFaces);
    }

    public TankRenderInfo without(Direction face) {
        faces.clear(face.getIndex());
        return this;
    }

    public boolean shouldRender(Direction face) {
        return faces.get(face.getIndex());
    }

    public IFluidTank getTank() {
        return tank;
    }

    public AxisAlignedBB getBounds() {
        return bounds;
    }

    public BitSet getFaces() {
        return faces;
    }
}