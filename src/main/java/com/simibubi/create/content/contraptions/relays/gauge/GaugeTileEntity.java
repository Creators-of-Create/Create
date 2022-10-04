package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.compat.computercraft.ComputerControllable;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.Lang;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class GaugeTileEntity extends KineticTileEntity implements IHaveGoggleInformation, ComputerControllable {

	public float dialTarget;
	public float dialState;
	public float prevDialState;
	public int color;

	private LazyOptional<IPeripheral> peripheral;

	public GaugeTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putFloat("Value", dialTarget);
		compound.putInt("Color", color);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		dialTarget = compound.getFloat("Value");
		color = compound.getInt("Color");
		super.read(compound, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();
		prevDialState = dialState;
		dialState += (dialTarget - dialState) * .125f;
		if (dialState > 1 && level.random.nextFloat() < 1 / 2f)
			dialState -= (dialState - 1) * level.random.nextFloat();
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		tooltip.add(componentSpacing.plainCopy().append(Lang.translateDirect("gui.gauge.info_header")));

		return true;
	}

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		LazyOptional<T> peripheralCap = getPeripheralCapability(cap);

		return peripheralCap.isPresent() ? peripheralCap : super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		removePeripheral();
	}

	@Override
	public void setPeripheral(LazyOptional<IPeripheral> peripheral) {
		this.peripheral = peripheral;
	}

	@Override
	public LazyOptional<IPeripheral> getPeripheral() {
		return this.peripheral;
	}

}
