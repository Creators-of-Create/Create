package com.simibubi.create;

import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class AllRegistries {
	public static final ResourceLocation BOGEY_NAME = new ResourceLocation(Create.ID, "bogeys");

	public static Supplier<IForgeRegistry<BogeyStyle>> BOGEY_STYLES;

	public static void register(final NewRegistryEvent event) {
		BOGEY_STYLES = event.create(new RegistryBuilder<BogeyStyle>().setName(BOGEY_NAME));
	}
}
