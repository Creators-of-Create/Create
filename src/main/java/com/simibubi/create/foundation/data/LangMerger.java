package com.simibubi.create.foundation.data;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.tterrag.registrate.providers.RegistrateDataProvider;

import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class LangMerger implements DataProvider {

	static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();
	private static final String CATEGORY_HEADER =
		"\t\"_\": \"->------------------------]  %s  [------------------------<-\",";

	private final String modid;
	private final String displayName;
	private final LangPartial[] langPartials;

	private List<Object> mergedLangData;
	private List<String> langIgnore;
	private PackOutput output;

	public <T extends LangPartial> LangMerger(PackOutput output, String modid, String displayName,
		AllLangPartials[] allLangPartials) {
		this.output = output;
		this.modid = modid;
		this.displayName = displayName;
		this.langPartials = allLangPartials;
		this.mergedLangData = new ArrayList<>();
		this.langIgnore = new ArrayList<>();
		populateLangIgnore();
	}

	public static void attachToRegistrateProvider(DataGenerator gen, PackOutput output) {
		Map<String, DataProvider> providers =
			ObfuscationReflectionHelper.getPrivateValue(DataGenerator.class, gen, "providersToRun");
		Entry<String, DataProvider> entryToReplace = null;
		for (Entry<String, DataProvider> entry : providers.entrySet())
			if (entry.getValue() instanceof RegistrateDataProvider rdp)
				entryToReplace = entry;
		if (entryToReplace != null)
			providers.put(entryToReplace.getKey(), new ChainedDataProvider(entryToReplace.getValue(),
				new LangMerger(output, Create.ID, Create.NAME, AllLangPartials.values())));
	}

	protected void populateLangIgnore() {
		// Key prefixes added here will NOT be transferred to lang templates
		langIgnore.add("create.ponder.debug_"); // Ponder debug scene text
		langIgnore.add("create.gui.chromatic_projector");
	}

	private boolean shouldIgnore(String key) {
		for (String string : langIgnore)
			if (key.startsWith(string))
				return true;
		return false;
	}

	@Override
	public String getName() {
		return displayName + "'s lang merger";
	}

	@Override
	public CompletableFuture<?> run(CachedOutput pOutput) {
		Path path = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang")
			.json(new ResourceLocation(modid, "en_us"));

		return CompletableFuture.runAsync(() -> {
			try {
				collectExistingEntries(path);
				collectEntries();
				if (mergedLangData.isEmpty())
					return;
				save(pOutput, mergedLangData, path,
					"Merging en_us.json with hand-written lang entries...");

			} catch (IOException ioexception) {
				LOGGER.error("Failed to run LangMerger", ioexception);
			}

		}, Util.backgroundExecutor());
	}

	private void collectExistingEntries(Path path) throws IOException {
		if (!Files.exists(path)) {
			Create.LOGGER.warn("Nothing to merge! It appears no lang was generated before me.");
			return;
		}

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			JsonObject jsonobject = GsonHelper.fromJson(GSON, reader, JsonObject.class);

			/*
			 * Erase additional sections from previous lang in case registrate did not
			 * create a new one (this assumes advancements to be the first section after
			 * game elements)
			 */
			Set<String> keysToRemove = new HashSet<>();
			MutableBoolean startErasing = new MutableBoolean();
			jsonobject.entrySet()
				.stream()
				.forEachOrdered(entry -> {
					String key = entry.getKey();
					if (key.startsWith("advancement"))
						startErasing.setTrue();
					if (startErasing.isFalse())
						return;
					keysToRemove.add(key);
				});
			jsonobject.remove("_");
			keysToRemove.forEach(jsonobject::remove);

			addAll("Game Elements", jsonobject);
			reader.close();
		}
	}

	protected void addAll(String header, JsonObject jsonobject) {
		if (jsonobject == null)
			return;
		header = String.format(CATEGORY_HEADER, header);

		writeData("\n");
		writeData(header);
		writeData("\n\n");

		MutableObject<String> previousKey = new MutableObject<>("");
		jsonobject.entrySet()
			.stream()
			.forEachOrdered(entry -> {
				String key = entry.getKey();
				if (shouldIgnore(key))
					return;
				String value = entry.getValue()
					.getAsString();
				if (!previousKey.getValue()
					.isEmpty() && shouldAddLineBreak(key, previousKey.getValue()))
					writeData("\n");
				writeEntry(key, value);
				previousKey.setValue(key);
			});

		writeData("\n");
	}

	private void writeData(String data) {
		mergedLangData.add(data);
	}

	private void writeEntry(String key, String value) {
		mergedLangData.add(new LangEntry(key, value));
	}

	protected boolean shouldAddLineBreak(String key, String previousKey) {
		// Always put tooltips and ponder scenes in their own paragraphs
		if (key.endsWith(".tooltip"))
			return true;
		if (key.startsWith(modid + ".ponder") && key.endsWith(PonderScene.TITLE_KEY))
			return true;

		key = key.replaceFirst("\\.", "");
		previousKey = previousKey.replaceFirst("\\.", "");

		String[] split = key.split("\\.");
		String[] split2 = previousKey.split("\\.");
		if (split.length == 0 || split2.length == 0)
			return false;

		// Start new paragraph if keys before second point do not match
		return !split[0].equals(split2[0]);
	}

	private void collectEntries() {
		for (LangPartial partial : langPartials)
			addAll(partial.getDisplayName(), partial.provide()
				.getAsJsonObject());
	}

	private void save(CachedOutput cache, List<Object> dataIn, Path target, String message) throws IOException {
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(bytearrayoutputstream, StandardCharsets.UTF_8);
		writer.append(createString(dataIn));
		writer.close();

		CachedOutput.NO_CACHE.writeIfNeeded(target, bytearrayoutputstream.toByteArray(), HashCode.fromInt(0));
		Create.LOGGER.info(message);
	}

	protected String createString(List<Object> data) {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		data.forEach(builder::append);
		builder.append("\t\"_\": \"Thank you for translating ")
			.append(displayName)
			.append("!\"\n\n");
		builder.append("}");
		return builder.toString();
	}

}
