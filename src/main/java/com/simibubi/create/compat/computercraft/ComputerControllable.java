package com.simibubi.create.compat.computercraft;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.utility.Iterate;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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

	default boolean isComputerControlled(BlockEntity tile) {
		if (tile.getLevel() == null || !(tile instanceof ComputerControllable))
			return false;

		for (Direction direction : Iterate.directions) {
			BlockState state = tile.getLevel().getBlockState(tile.getBlockPos().relative(direction));

			// TODO: Add a check for "cable" wired modem.
			//  The difficulty comes since the "cable" wired modem uses an enum property instead of a boolean property.
			//  This could possibly be surpassed with reflection. It would be good to find a more solid solution.
			if (state.getBlock().equals(Mods.COMPUTERCRAFT.getBlock("wired_modem_full"))) {
				return state.getOptionalValue(BooleanProperty.create("peripheral")).orElse(false);
			}
		}

		return false;
	}

}
