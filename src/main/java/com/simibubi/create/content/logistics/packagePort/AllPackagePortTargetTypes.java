package com.simibubi.create.content.logistics.packagePort;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget.ChainConveyorFrogportTarget;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget.TrainStationFrogportTarget;

import net.minecraft.core.Holder;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllPackagePortTargetTypes {
	private static final DeferredRegister<PackagePortTargetType> REGISTER = DeferredRegister.create(CreateRegistries.PACKAGE_PORT_TARGET_TYPE, Create.ID);

	public static final Holder<PackagePortTargetType> CHAIN_CONVEYOR = REGISTER.register("chain_conveyor", ChainConveyorFrogportTarget.Type::new);
	public static final Holder<PackagePortTargetType> TRAIN_STATION = REGISTER.register("train_station", TrainStationFrogportTarget.Type::new);

	@Internal
	public static void register(IEventBus eventBus) {
		REGISTER.register(eventBus);
	}
}
