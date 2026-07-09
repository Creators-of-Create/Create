package com.simibubi.create.infrastructure.data;

import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.data.CreateDatamapProvider;
import com.simibubi.create.foundation.data.DamageTypeTagGen;
import com.simibubi.create.foundation.data.recipe.CreateMechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import com.simibubi.create.foundation.data.recipe.CreateSequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateStandardRecipeGen;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;

import net.createmod.ponder.api.client.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public class CreateDatagen {
	private static boolean defaultComponentsBoundForDatagen;

	public static void gatherDataHighPriority(GatherDataEvent.Client event) {
		if (event.getModContainer().getModId().equals(Create.ID))
			addExtraRegistrateData();
	}

	public static void gatherData(GatherDataEvent.Client event) {
		if (!event.getModContainer().getModId().equals(Create.ID))
			return;

		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		generator.addProvider(true, AllSoundEvents.provider(generator));

		bindDefaultComponentsForDatagen();
		GeneratedEntriesProvider generatedEntriesProvider = new GeneratedEntriesProvider(output, lookupProvider);
		lookupProvider = generatedEntriesProvider.getRegistryProvider();
		generator.addProvider(true, generatedEntriesProvider);

		generator.addProvider(true, new CreateRecipeSerializerTagsProvider(output, lookupProvider));
		generator.addProvider(true, new CreateContraptionTypeTagsProvider(output, lookupProvider));
		generator.addProvider(true, new CreateMountedItemStorageTypeTagsProvider(output, lookupProvider));
		generator.addProvider(true, new DamageTypeTagGen(output, lookupProvider));
		generator.addProvider(true, new AllAdvancements(output, lookupProvider));
		generator.addProvider(true, new CreateStandardRecipeGen(output, lookupProvider));
		generator.addProvider(true, new CreateMechanicalCraftingRecipeGen(output, lookupProvider));
		generator.addProvider(true, new CreateSequencedAssemblyRecipeGen(output, lookupProvider));
		generator.addProvider(true, new CreateDatamapProvider(output, lookupProvider));
		generator.addProvider(true, new VanillaHatOffsetGenerator(output, lookupProvider));
		generator.addProvider(true, new CreateEnchantmentTagsProvider(output, lookupProvider));
		generator.addProvider(true, new CreateWikiBlockInfoProvider(output));

		CreateRecipeProvider.registerAllProcessing(generator, output, lookupProvider);
	}

	private static void addExtraRegistrateData() {
		CreateRegistrateTags.addGenerators();

		Create.registrate().addDataGenerator(ProviderType.LANG, provider -> {
			BiConsumer<String, String> langConsumer = provider::add;

			provideDefaultLang("interface", langConsumer);
			provideDefaultLang("tooltips", langConsumer);
			AllAdvancements.provideLang(langConsumer);
			AllSoundEvents.provideLang(langConsumer);
			AllKeys.provideLang(langConsumer);
			providePonderLang(provider, langConsumer);
			new TagLangGenerator(langConsumer).generate();
		});
	}

	private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
		String path = "assets/create/lang/default/" + fileName + ".json";
		JsonElement jsonElement = FilesHelper.loadJsonResource(path);
		if (jsonElement == null) {
			throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().getAsString();
			consumer.accept(key, value);
		}
	}

	private static synchronized void bindDefaultComponentsForDatagen() {
		if (defaultComponentsBoundForDatagen)
			return;

		BuiltInRegistries.ITEM.listElements()
			.forEach(item -> {
				if (item.areComponentsBound())
					return;
				item.bindComponents(DataComponentMap.builder()
					.addAll(DataComponents.COMMON_ITEM_COMPONENTS)
					.set(DataComponents.ITEM_NAME, Component.translatable(item.value()
						.getDescriptionId()))
					.set(DataComponents.ITEM_MODEL, item.key()
						.identifier())
					.build());
			});
		BuiltInRegistries.FLUID.listElements()
			.forEach(fluid -> {
				if (fluid.areComponentsBound())
					return;
				fluid.bindComponents(DataComponentMap.EMPTY);
			});
		defaultComponentsBoundForDatagen = true;
	}

	private static void providePonderLang(RegistrateLangProvider provider, BiConsumer<String, String> consumer) {
		bindDefaultComponentsForDatagen();

		// Register this since FMLClientSetupEvent does not run during datagen
		PonderIndex.addPlugin(new CreatePonderPlugin());

		PonderIndex.getLangAccess().provideLang(Create.ID, consumer);
	}
}
