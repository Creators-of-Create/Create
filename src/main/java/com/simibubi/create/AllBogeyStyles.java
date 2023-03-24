package com.simibubi.create;

import com.simibubi.create.content.logistics.trains.BogeyRenderer;

import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;

import com.simibubi.create.content.logistics.trains.entity.StandardBogeyInstance;

import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

import static com.simibubi.create.Create.REGISTRATE;

@SuppressWarnings("unused")
public class AllBogeyStyles {
	public static final DeferredRegister<BogeyStyle> BOGEYS = DeferredRegister.create(AllRegistries.BOGEY_NAME, Create.ID);

	public static final RegistryEntry<BogeyStyle> STANDARD = REGISTRATE
			.bogeyStyle("standard", new BogeyStyle(StandardBogeyInstance.class))
			.block(BogeyRenderer.BogeySize.SMALL, AllBlocks.SMALL_BOGEY.get())
			.block(BogeyRenderer.BogeySize.LARGE, AllBlocks.LARGE_BOGEY.get())
			.register();

	public static void register() {
		BOGEYS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}
