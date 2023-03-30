package com.simibubi.create;

import com.simibubi.create.content.logistics.trains.BogeyRenderer;
import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;
import com.simibubi.create.content.logistics.trains.entity.StandardBogeyInstance;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.simibubi.create.Create.LOGGER;
import static com.simibubi.create.Create.REGISTRATE;

@SuppressWarnings("unused")
public class AllBogeyStyles {
	public static final RegistryEntry<BogeyStyle> STANDARD = REGISTRATE
			.bogeyStyle("standard", new BogeyStyle())
			.block(BogeyRenderer.BogeySize.SMALL, AllBlocks.SMALL_BOGEY)
			.block(BogeyRenderer.BogeySize.LARGE, AllBlocks.LARGE_BOGEY)
			.register();

	public static void register() {
		LOGGER.info("Registered bogey styles from " + Create.ID);
		AllRegistries.DEFERRED_BOGEY_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}
