package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FluidTankTileEntity extends SmartTileEntity {

    LazyOptional<FluidTank> fluid = LazyOptional.of(this::createFluidHandler);

    public FluidTankTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
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
        return new FluidTank(16);
    }

    public IFluidTank getTank() {
        return fluid.orElseGet(this::createFluidHandler);
    }
}
