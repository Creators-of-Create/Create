package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.chest.ChestMountedStorageType;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;
import com.simibubi.create.content.contraptions.behaviour.dispenser.storage.DispenserMountedStorageType;
import com.simibubi.create.content.equipment.toolbox.ToolboxMountedStorageType;
import com.simibubi.create.content.fluids.tank.storage.FluidTankMountedStorageType;
import com.simibubi.create.content.fluids.tank.storage.creative.CreativeFluidTankMountedStorageType;
import com.simibubi.create.content.logistics.crate.CreativeCrateMountedStorageType;
import com.simibubi.create.content.logistics.depot.storage.DepotMountedStorageType;
import com.simibubi.create.content.logistics.vault.ItemVaultMountedStorageType;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.impl.contraption.storage.FallbackMountedStorageType;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.world.level.block.Blocks;

public class AllMountedStorageTypes {
	private static final CreateRegistrate REGISTRATE = Create.registrate();

	// fallback is special, provider is registered on lookup creation so it's always last
	public static final RegistryEntry<MountedItemStorageType<?>, FallbackMountedStorageType> FALLBACK = simpleItem("fallback", FallbackMountedStorageType::new);

	// registrations for these are handled by the blocks, not the types
	public static final RegistryEntry<MountedItemStorageType<?>, CreativeCrateMountedStorageType> CREATIVE_CRATE = simpleItem("creative_crate", CreativeCrateMountedStorageType::new);
	public static final RegistryEntry<MountedItemStorageType<?>, ItemVaultMountedStorageType> VAULT = simpleItem("vault", ItemVaultMountedStorageType::new);
	public static final RegistryEntry<MountedItemStorageType<?>, ToolboxMountedStorageType> TOOLBOX = simpleItem("toolbox", ToolboxMountedStorageType::new);
	public static final RegistryEntry<MountedItemStorageType<?>, DepotMountedStorageType> DEPOT = simpleItem("depot", DepotMountedStorageType::new);
	public static final RegistryEntry<MountedFluidStorageType<?>, FluidTankMountedStorageType> FLUID_TANK = simpleFluid("fluid_tank", FluidTankMountedStorageType::new);
	public static final RegistryEntry<MountedFluidStorageType<?>, CreativeFluidTankMountedStorageType> CREATIVE_FLUID_TANK = simpleFluid("creative_fluid_tank", CreativeFluidTankMountedStorageType::new);

	// these are for external blocks, register associations here
	public static final RegistryEntry<MountedItemStorageType<?>, SimpleMountedStorageType.Impl> SIMPLE = REGISTRATE.mountedItemStorage("simple", SimpleMountedStorageType.Impl::new)
		.associateBlockTag(AllTags.AllBlockTags.SIMPLE_MOUNTED_STORAGE.tag)
		.register();
	public static final RegistryEntry<MountedItemStorageType<?>, ChestMountedStorageType> CHEST = REGISTRATE.mountedItemStorage("chest", ChestMountedStorageType::new)
		.associateBlockTag(AllTags.AllBlockTags.CHEST_MOUNTED_STORAGE.tag)
		.register();
	public static final RegistryEntry<MountedItemStorageType<?>, DispenserMountedStorageType> DISPENSER = REGISTRATE.mountedItemStorage("dispenser", DispenserMountedStorageType::new)
		.associate(Blocks.DISPENSER)
		.associate(Blocks.DROPPER)
		.register();

	private static <T extends MountedItemStorageType<?>> RegistryEntry<MountedItemStorageType<?>, T> simpleItem(String name, Supplier<T> supplier) {
		return REGISTRATE.mountedItemStorage(name, supplier).register();
	}

	private static <T extends MountedFluidStorageType<?>> RegistryEntry<MountedFluidStorageType<?>, T> simpleFluid(String name, Supplier<T> supplier) {
		return REGISTRATE.mountedFluidStorage(name, supplier).register();
	}

	public static void register() {
	}
}
