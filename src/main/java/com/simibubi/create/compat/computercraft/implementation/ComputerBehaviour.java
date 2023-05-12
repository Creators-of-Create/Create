package com.simibubi.create.compat.computercraft.implementation;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.implementation.peripherals.DisplayLinkPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SequencedGearshiftPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SpeedControllerPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SpeedGaugePeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.StationPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.StressGaugePeripheral;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerBlockEntity;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftBlockEntity;
import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkBlockEntity;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.ForgeRegistries;

public class ComputerBehaviour extends AbstractComputerBehaviour {

	protected static final Capability<IPeripheral> PERIPHERAL_CAPABILITY =
		CapabilityManager.get(new CapabilityToken<>() {
		});
	LazyOptional<IPeripheral> peripheral;
	NonNullSupplier<IPeripheral> peripheralSupplier;

	public ComputerBehaviour(SmartBlockEntity te) {
		super(te);
		this.peripheralSupplier = getPeripheralFor(te);
	}

	public static NonNullSupplier<IPeripheral> getPeripheralFor(SmartBlockEntity te) {
		if (te instanceof SpeedControllerBlockEntity scte)
			return () -> new SpeedControllerPeripheral(scte, scte.targetSpeed);
		if (te instanceof DisplayLinkBlockEntity dlte)
			return () -> new DisplayLinkPeripheral(dlte);
		if (te instanceof SequencedGearshiftBlockEntity sgte)
			return () -> new SequencedGearshiftPeripheral(sgte);
		if (te instanceof SpeedGaugeBlockEntity sgte)
			return () -> new SpeedGaugePeripheral(sgte);
		if (te instanceof StressGaugeBlockEntity sgte)
			return () -> new StressGaugePeripheral(sgte);
		if (te instanceof StationBlockEntity ste)
			return () -> new StationPeripheral(ste);

		throw new IllegalArgumentException(
			"No peripheral available for " + ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(te.getType()));
	}

	@Override
	public <T> boolean isPeripheralCap(Capability<T> cap) {
		return cap == PERIPHERAL_CAPABILITY;
	}

	@Override
	public <T> LazyOptional<T> getPeripheralCapability() {
		if (peripheral == null || !peripheral.isPresent())
			peripheral = LazyOptional.of(peripheralSupplier);
		return peripheral.cast();
	}

	@Override
	public void removePeripheral() {
		if (peripheral != null)
			peripheral.invalidate();
	}

}
