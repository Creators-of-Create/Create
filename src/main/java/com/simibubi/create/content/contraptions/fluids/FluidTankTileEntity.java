package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FluidTankTileEntity extends SmartTileEntity {

    LazyOptional<FluidTank> fluid = LazyOptional.of(this::createFluidHandler);
    private int priority = 1000;

    public FluidTankTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    private int calculateDrainAmount(FluidTankTileEntity other, int delta) {
        boolean roundDirection = other.getPriority() > this.getPriority();
        return (int) Math.abs(roundDirection ? Math.floor(delta / 2f) : Math.ceil(delta / 2f));
    }

    @Override
    public void tick() {
        super.tick();
        updatePriority();

        FluidTankTileEntity other;

        other = getOtherFluidTankTileEntity(Direction.NORTH);
        
        if (other != null && other.getTank().isFluidValid(this.getTank().getFluid())) {
            int delta = other.getTank().getFluidAmount() - this.getTank().getFluidAmount();
            if (delta > 0) {
                this.getTank().fill(other.getTank().drain(calculateDrainAmount(other, delta), FluidAction.EXECUTE), FluidAction.EXECUTE);
                other.markDirty();
                this.markDirty();
                other.sendData();
                sendData();
            } else if (delta < 0) {
                other.getTank().fill(this.getTank().drain(calculateDrainAmount(other, delta), FluidAction.EXECUTE), FluidAction.EXECUTE);
                other.markDirty();
                this.markDirty();
                other.sendData();
                sendData();
            }
        }


        other = getOtherFluidTankTileEntity(Direction.WEST);
        if (other != null && other.getTank().isFluidValid(this.getTank().getFluid())) {
            int delta = other.getTank().getFluidAmount() - this.getTank().getFluidAmount();
            if (delta > 0) {
                this.getTank().fill(other.getTank().drain(calculateDrainAmount(other, delta), FluidAction.EXECUTE), FluidAction.EXECUTE);
                other.markDirty();
                this.markDirty();
                other.sendData();
                sendData();
            } else if (delta < 0) {
                other.getTank().fill(this.getTank().drain(calculateDrainAmount(other, delta), FluidAction.EXECUTE), FluidAction.EXECUTE);
                other.markDirty();
                this.markDirty();
                other.sendData();
                sendData();
            }
        }

        other = getOtherFluidTankTileEntity(Direction.UP);
        if (other != null && other.getTank().isFluidValid(this.getTank().getFluid())) {
            int space = this.getTank().getCapacity() - this.getTank().getFluidAmount();
            if (space > 0 && other.getTank().getFluidAmount() > 0) {
                this.getTank().fill(other.getTank().drain(space, FluidAction.EXECUTE), FluidAction.EXECUTE);
                other.markDirty();
                this.markDirty();
                other.sendData();
                sendData();
            }
        }
    }

    @Nullable
    public FluidTankTileEntity getOtherFluidTankTileEntity(Direction direction) {
        TileEntity otherTE = world.getTileEntity(pos.offset(direction));
        if (otherTE instanceof FluidTankTileEntity)
            return (FluidTankTileEntity) otherTE;
        return null;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluid.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void read(CompoundNBT tag) {
        fluid.ifPresent(h -> h.readFromNBT(tag));
        super.read(tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        fluid.ifPresent(h -> h.writeToNBT(tag));
        return super.write(tag);
    }

    @Override
    public void addBehaviours(List<TileEntityBehaviour> behaviours) {
    }

    @Nonnull
    public FluidTank createFluidHandler() {
        return new FluidTank(16000);
    }

    public IFluidTank getTank() {
        return fluid.orElseGet(this::createFluidHandler);
    }

    private void updatePriority() {
        FluidTankTileEntity other = getOtherFluidTankTileEntity(Direction.DOWN);
        priority = 1000;
        if (other != null && other.getTank().getFluidAmount() < other.getTank().getCapacity()) {
            priority = 0;
            return;
        }

        updatePriorityFrom(Direction.SOUTH);
        updatePriorityFrom(Direction.NORTH);
        updatePriorityFrom(Direction.WEST);
        updatePriorityFrom(Direction.EAST);
    }

    private void updatePriorityFrom(Direction direction) {
        FluidTankTileEntity other = getOtherFluidTankTileEntity(direction);
        if (other != null && other.getPriority() + 1 < priority) {
            priority = other.getPriority() + 1;
        }
    }

    public int getPriority() {
        return priority;
    }
}
