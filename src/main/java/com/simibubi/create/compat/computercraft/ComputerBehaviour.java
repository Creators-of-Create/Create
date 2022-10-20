package com.simibubi.create.compat.computercraft;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

public class ComputerBehaviour extends TileEntityBehaviour {

	public static final BehaviourType<ComputerBehaviour> TYPE = new BehaviourType<>();
	protected static final Capability<IPeripheral> PERIPHERAL_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

	LazyOptional<IPeripheral> peripheral;
	NonNullSupplier<IPeripheral> peripheralSupplier;

	boolean hasAttachedComputer;

	public ComputerBehaviour(SmartTileEntity te, NonNullSupplier<IPeripheral> peripheralSupplier) {
		super(te);
		this.peripheralSupplier = peripheralSupplier;
		this.hasAttachedComputer = false;
	}

	public static <T> boolean isPeripheralCap(@NotNull Capability<T> cap) {
		return cap == PERIPHERAL_CAPABILITY;
	}

	public <T> LazyOptional<T> getPeripheralCapability() {
		if (peripheral == null || !peripheral.isPresent())
			peripheral = LazyOptional.of(peripheralSupplier);

		return peripheral.cast();
	}

	public void removePeripheral() {
		if (peripheral != null) {
			peripheral.invalidate();
		}
	}

	public void setHasAttachedComputer(boolean hasAttachedComputer) {
		this.hasAttachedComputer = hasAttachedComputer;
	}

	public boolean hasAttachedComputer() {
		return hasAttachedComputer;
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
