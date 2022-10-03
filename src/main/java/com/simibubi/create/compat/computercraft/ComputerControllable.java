package com.simibubi.create.compat.computercraft;

import org.jetbrains.annotations.NotNull;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

public interface ComputerControllable {

	Capability<IPeripheral> PERIPHERAL_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

	IPeripheral createPeripheral();

	void setPeripheralHandler(LazyOptional<IPeripheral> peripheralHandler);

	LazyOptional<IPeripheral> getPeripheralHandler();

	default <T> LazyOptional<T> getPeripheralCapability(@NotNull Capability<T> cap) {
		if (cap == PERIPHERAL_CAPABILITY) {
			if (getPeripheralHandler() == null || !getPeripheralHandler().isPresent())
				setPeripheralHandler(LazyOptional.of(this::createPeripheral));

			return getPeripheralHandler().cast();
		}

		return LazyOptional.empty();
	}

	default void removePeripheral() {
		if (getPeripheralHandler() != null)
			getPeripheralHandler().invalidate();
	}

}
