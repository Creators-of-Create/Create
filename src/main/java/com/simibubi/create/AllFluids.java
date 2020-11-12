package com.simibubi.create;

import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid.PotionFluidAttributes;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class AllFluids {

	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static RegistryEntry<PotionFluid> POTION =
		REGISTRATE.virtualFluid("potion", PotionFluidAttributes::new, PotionFluid::new)
			.lang(f -> "fluid.create.flowing_potion", "Potion")
			.register();

	// Load this class

	public static void register() {}

}
