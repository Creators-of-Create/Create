package com.simibubi.create.compat.computercraft.implementation;

import java.util.function.Supplier;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.implementation.peripherals.DisplayLinkPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.RedstoneRequesterPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SequencedGearshiftPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SpeedControllerPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SpeedGaugePeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.StationPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.StressGaugePeripheral;
import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.registries.BuiltInRegistries;

public class ComputerBehaviour extends AbstractComputerBehaviour {

	IPeripheral peripheral;
	Supplier<IPeripheral> peripheralSupplier;
	SmartBlockEntity be;

	public ComputerBehaviour(SmartBlockEntity be) {
		super(be);
		this.peripheralSupplier = getPeripheralFor(be);
		this.be = be;
	}

	public static Supplier<IPeripheral> getPeripheralFor(SmartBlockEntity be) {
		if (be instanceof SpeedControllerBlockEntity scbe)
			return () -> new SpeedControllerPeripheral(scbe, scbe.targetSpeed);
		if (be instanceof DisplayLinkBlockEntity dlbe)
			return () -> new DisplayLinkPeripheral(dlbe);
		if (be instanceof SequencedGearshiftBlockEntity sgbe)
			return () -> new SequencedGearshiftPeripheral(sgbe);
		if (be instanceof SpeedGaugeBlockEntity sgbe)
			return () -> new SpeedGaugePeripheral(sgbe);
		if (be instanceof StressGaugeBlockEntity sgbe)
			return () -> new StressGaugePeripheral(sgbe);
		if (be instanceof StationBlockEntity sbe)
			return () -> new StationPeripheral(sbe);
		if (be instanceof RedstoneRequesterBlockEntity rrbe)
			return () -> new RedstoneRequesterPeripheral(rrbe);

		throw new IllegalArgumentException(
			"No peripheral available for " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()));
	}

	@Override
	public IPeripheral getPeripheralCapability() {
		if (peripheral == null)
			peripheral = peripheralSupplier.get();
		return peripheral;
	}

	@Override
	public void removePeripheral() {
		if (peripheral != null)
			getWorld().invalidateCapabilities(be.getBlockPos());
	}

}
