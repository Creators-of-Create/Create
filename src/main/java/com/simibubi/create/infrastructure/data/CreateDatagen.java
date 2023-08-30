package com.simibubi.create.infrastructure.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.infrastructure.ponder.AllPonderTags;
import com.simibubi.create.infrastructure.ponder.GeneralText;
import com.simibubi.create.infrastructure.ponder.PonderIndex;
import com.simibubi.create.infrastructure.ponder.SharedText;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public class CreateDatagen {
	public static void gatherData(GatherDataEvent event) {
		addExtraRegistrateData();

		DataGenerator generator = event.getGenerator();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

		if (event.includeClient()) {
			generator.addProvider(AllSoundEvents.provider(generator));
		}

		if (event.includeServer()) {
			generator.addProvider(new CreateRecipeSerializerTagsProvider(generator, existingFileHelper));

			generator.addProvider(new AllAdvancements(generator));

			generator.addProvider(new StandardRecipeGen(generator));
			generator.addProvider(new MechanicalCraftingRecipeGen(generator));
			generator.addProvider(new SequencedAssemblyRecipeGen(generator));
			ProcessingRecipeGen.registerAll(generator);

//			AllOreFeatureConfigEntries.gatherData(event);
		}
	}

	private static void addExtraRegistrateData() {
		CreateRegistrateTags.addGenerators();

		// Really all additional lang entries should be added using AbstractRegistrate#addRawLang,
		// but doing so would change the generated lang entry order and potentially mess up Crowndin.
		Create.REGISTRATE.addLangPostprocessor(entries -> {
			Map<String, String> newEntries = new LinkedHashMap<>(entries);
			BiConsumer<String, String> langConsumer = (key, value) -> {
				String oldValue = newEntries.put(key, value);
				if (oldValue != null) {
					Create.LOGGER.warn("Value of lang key '" + key + "' was overridden!");
				}
			};

			AllAdvancements.consumeLang(langConsumer);
			consumeDefaultLang("interface", langConsumer);
			AllSoundEvents.consumeLang(langConsumer);
			consumeDefaultLang("tooltips", langConsumer);
			consumePonderLang(langConsumer);

			return newEntries;
		});
	}

	private static void consumeDefaultLang(String fileName, BiConsumer<String, String> consumer) {
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

	private static void consumePonderLang(BiConsumer<String, String> consumer) {
		// Register these since FMLClientSetupEvent does not run during datagen
		AllPonderTags.register();
		PonderIndex.register();

		SharedText.gatherText();
		PonderLocalization.generateSceneLang();

		GeneralText.consumeLang(consumer);
		PonderLocalization.consumeLang(Create.ID, consumer);
	}
}
