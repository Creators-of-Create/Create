package com.simibubi.create;

import com.simibubi.create.content.logistics.trains.BogeyRenderer;
import com.simibubi.create.content.logistics.trains.BogeySizes;
import com.simibubi.create.content.logistics.trains.StandardBogeyRenderer;
import com.simibubi.create.content.logistics.trains.TestBogeyRenderer;
import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.simibubi.create.Create.LOGGER;
import static com.simibubi.create.Create.REGISTRATE;

@SuppressWarnings("unused")
public class AllBogeyStyles {
	public static final RegistryEntry<BogeyStyle> STANDARD = REGISTRATE
			.bogeyStyle("standard", new BogeyStyle())
			.block(BogeySizes.SMALL, AllBlocks.SMALL_BOGEY)
			.block(BogeySizes.LARGE, AllBlocks.LARGE_BOGEY)
			.renderer(new StandardBogeyRenderer())
			.register();

	/*
	public static final RegistryEntry<BogeyStyle> TEST = REGISTRATE
			.bogeyStyle("test", new BogeyStyle())
			.block(BogeySizes.LARGE, AllBlocks.LARGE_BOGEY)
			.renderer(new TestBogeyRenderer())
			.register();

	public static final RegistryEntry<BogeyStyle> TEST_TWO = REGISTRATE
			.bogeyStyle("test_two", new BogeyStyle())
			.block(BogeySizes.SMALL, AllBlocks.SMALL_BOGEY)
			.renderer(new TestBogeyRenderer())
			.register();

	 */

	public static void register() {
		LOGGER.info("Registered bogey styles from " + Create.ID);
		AllRegistries.DEFERRED_BOGEY_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}
