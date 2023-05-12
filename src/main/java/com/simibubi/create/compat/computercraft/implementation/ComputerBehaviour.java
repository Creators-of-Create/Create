package com.simibubi.create.compat.computercraft.implementation;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.implementation.peripherals.DisplayLinkPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SequencedGearshiftPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SpeedControllerPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SpeedGaugePeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.StationPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.StressGaugePeripheral;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerTileEntity;
import com.simibubi.create.content.contraptions.relays.advanced.sequencer.SequencedGearshiftTileEntity;
import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeTileEntity;
import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeTileEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkTileEntity;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationTileEntity;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

public class ComputerBehaviour extends AbstractComputerBehaviour {

	protected static final Capability<IPeripheral> PERIPHERAL_CAPABILITY =
		CapabilityManager.get(new CapabilityToken<>() {
		});
	LazyOptional<IPeripheral> peripheral;
	NonNullSupplier<IPeripheral> peripheralSupplier;

	public ComputerBehaviour(SmartTileEntity te) {
		super(te);
		this.peripheralSupplier = getPeripheralFor(te);
	}

	public static NonNullSupplier<IPeripheral> getPeripheralFor(SmartTileEntity te) {
		if (te instanceof SpeedControllerTileEntity scte)
			return () -> new SpeedControllerPeripheral(scte, scte.targetSpeed);
		if (te instanceof DisplayLinkTileEntity dlte)
			return () -> new DisplayLinkPeripheral(dlte);
		if (te instanceof SequencedGearshiftTileEntity sgte)
			return () -> new SequencedGearshiftPeripheral(sgte);
		if (te instanceof SpeedGaugeTileEntity sgte)
			return () -> new SpeedGaugePeripheral(sgte);
		if (te instanceof StressGaugeTileEntity sgte)
			return () -> new StressGaugePeripheral(sgte);
		if (te instanceof StationTileEntity ste)
			return () -> new StationPeripheral(ste);

		throw new IllegalArgumentException("No peripheral available for " + te.getType()
			.getRegistryName());
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
