package com.simibubi.create;

import com.simibubi.create.modules.contraptions.CachedBufferReloader;
import com.simibubi.create.modules.contraptions.receivers.EncasedFanParticleHandler;
import com.simibubi.create.modules.schematics.ClientSchematicLoader;
import com.simibubi.create.modules.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.modules.schematics.client.SchematicHandler;
import com.simibubi.create.modules.schematics.client.SchematicHologram;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(bus = Bus.MOD)
public class CreateClient {

	public static ClientSchematicLoader schematicSender;
	public static SchematicHandler schematicHandler;
	public static SchematicHologram schematicHologram;
	public static SchematicAndQuillHandler schematicAndQuillHandler;
	public static EncasedFanParticleHandler fanParticles;

	public static ModConfig config;

	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		schematicSender = new ClientSchematicLoader();
		schematicHandler = new SchematicHandler();
		schematicHologram = new SchematicHologram();
		schematicAndQuillHandler = new SchematicAndQuillHandler();
		fanParticles = new EncasedFanParticleHandler();

		AllKeys.register();
		AllContainers.registerScreenFactories();
		AllTileEntities.registerRenderers();
		AllItems.registerColorHandlers();
		AllBlocks.registerColorHandlers();

		IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		if (resourceManager instanceof IReloadableResourceManager)
			((IReloadableResourceManager) resourceManager).addReloadListener(new CachedBufferReloader());
	}

	@SubscribeEvent
	public static void createConfigs(ModConfig.ModConfigEvent event) {
		if (event.getConfig().getSpec() == CreateConfig.specification)
			return;

		config = event.getConfig();
	}

	public static void gameTick() {
		schematicSender.tick();
		schematicAndQuillHandler.tick();
		schematicHandler.tick();
		schematicHologram.tick();
	}

}
