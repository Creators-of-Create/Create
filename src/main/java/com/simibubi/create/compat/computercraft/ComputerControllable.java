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

	void setPeripheral(LazyOptional<IPeripheral> peripheral);

	LazyOptional<IPeripheral> getPeripheral();
	default <T> LazyOptional<T> getPeripheralCapability(@NotNull Capability<T> cap) {
		if (cap == PERIPHERAL_CAPABILITY) {
			if (getPeripheral() == null || !getPeripheral().isPresent())
				setPeripheral(LazyOptional.of(this::createPeripheral));

			return getPeripheral().cast();
		}

		return LazyOptional.empty();
	}

	default void removePeripheral() {
		if (getPeripheral() != null) {
			getPeripheral().invalidate();
		}
	}

}
