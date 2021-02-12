package com.simibubi.create.foundation.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.FilesHelper;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.JSONUtils;

public class LangMerger implements IDataProvider {

	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting()
		.disableHtmlEscaping()
		.create();
	static final String CATEGORY_HEADER = "\t\"_\": \"->------------------------]  %s  [------------------------<-\",";

	private DataGenerator gen;

	private List<Object> mergedLangData;
	private Map<String, List<Object>> populatedLangData;
	private Map<String, Map<String, String>> allLocalizedEntries;
	private Map<String, MutableInt> missingTranslationTally;

	public LangMerger(DataGenerator gen) {
		this.gen = gen;
		this.mergedLangData = new ArrayList<>();
		this.allLocalizedEntries = new HashMap<>();
		this.populatedLangData = new HashMap<>();
		this.missingTranslationTally = new HashMap<>();
	}

	@Override
	public String getName() {
		return "Lang merger";
	}

	@Override
	public void act(DirectoryCache cache) throws IOException {
		Path path = this.gen.getOutputFolder()
			.resolve("assets/" + Create.ID + "/lang/" + "en_us.json");

		for (Pair<String, JsonElement> pair : getAllLocalizationFiles()) {
			if (!pair.getRight()
				.isJsonObject())
				continue;
			Map<String, String> localizedEntries = new HashMap<>();
			JsonObject jsonobject = pair.getRight()
				.getAsJsonObject();
			jsonobject.entrySet()
				.stream()
				.forEachOrdered(entry -> {
					String key = entry.getKey();
					if (key.startsWith("_"))
						return;
					String value = entry.getValue()
						.getAsString();
					localizedEntries.put(key, value);
				});
			String key = pair.getKey();
			allLocalizedEntries.put(key, localizedEntries);
			populatedLangData.put(key, new ArrayList<>());
			missingTranslationTally.put(key, new MutableInt(0));
		}

		collectExistingEntries(path);
		collectEntries();
		if (mergedLangData.isEmpty())
			return;

		save(cache, mergedLangData, -1, path, "Merging en_us.json with hand-written lang entries...");
		for (Entry<String, List<Object>> localization : populatedLangData.entrySet()) {
			String key = localization.getKey();
			Path populatedLangPath = this.gen.getOutputFolder()
				.resolve("assets/" + Create.ID + "/lang/unfinished/" + key);
			save(cache, localization.getValue(), missingTranslationTally.get(key)
				.intValue(), populatedLangPath, "Populating " + key + " with missing entries...");
		}
	}

	private void collectExistingEntries(Path path) throws IOException {
		if (!Files.exists(path)) {
			Create.logger.warn("Nothing to merge! It appears no lang was generated before me.");
			return;
		}

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			JsonObject jsonobject = JSONUtils.fromJson(GSON, reader, JsonObject.class);
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
		populatedLangData.values()
			.forEach(l -> l.add(data));
	}

	private void writeEntry(String key, String value) {
		mergedLangData.add(new LangEntry(key, value));
		populatedLangData.forEach((k, l) -> {
			ForeignLangEntry entry = new ForeignLangEntry(key, value, allLocalizedEntries.get(k));
			if (entry.isMissing())
				missingTranslationTally.get(k)
					.increment();
			l.add(entry);
		});
	}

	protected boolean shouldAddLineBreak(String key, String previousKey) {
		// Always put tooltips in their own paragraphs
		if (key.endsWith(".tooltip"))
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

	private List<Pair<String, JsonElement>> getAllLocalizationFiles() {
		ArrayList<Pair<String, JsonElement>> list = new ArrayList<>();

		String filepath = "assets/" + Create.ID + "/lang/";
		try {
			InputStream resourceAsStream = Create.class.getClassLoader()
				.getResourceAsStream(filepath);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
			while (true) {
				String readLine = bufferedReader.readLine();
				if (readLine == null)
					break;
				if (!readLine.endsWith(".json"))
					continue;
				if (readLine.startsWith("en_us") || readLine.startsWith("en_ud"))
					continue;
				list.add(Pair.of(readLine, FilesHelper.loadJsonResource(filepath + "/" + readLine)));
			}
			resourceAsStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

	private void collectEntries() {
		for (AllLangPartials partial : AllLangPartials.values())
			addAll(partial.getDisplay(), partial.provide()
				.getAsJsonObject());
	}

	private void save(DirectoryCache cache, List<Object> dataIn, int missingKeys, Path target, String message)
		throws IOException {
		String data = createString(dataIn, missingKeys);
//		data = JavaUnicodeEscaper.outsideOf(0, 0x7f)
//			.translate(data);
		String hash = IDataProvider.HASH_FUNCTION.hashUnencodedChars(data)
			.toString();
		if (!Objects.equals(cache.getPreviousHash(target), hash) || !Files.exists(target)) {
			Files.createDirectories(target.getParent());

			try (BufferedWriter bufferedwriter = Files.newBufferedWriter(target)) {
				Create.logger.info(message);
				bufferedwriter.write(data);
				bufferedwriter.close();
			}
		}

		cache.recordHash(target, hash);
	}

	protected String createString(List<Object> data, int missingKeys) {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		if (missingKeys != -1)
			builder.append("\t\"_\": \"Missing Localizations: " + missingKeys + "\",\n");
		data.forEach(builder::append);
		builder.append("\t\"_\": \"Thank you for translating Create!\"\n\n");
		builder.append("}");
		return builder.toString();
	}

	private class LangEntry {
		static final String ENTRY_FORMAT = "\t\"%s\": %s,\n";

		private String key;
		private String value;

		LangEntry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format(ENTRY_FORMAT, key, GSON.toJson(value, String.class));
		}

	}

	private class ForeignLangEntry extends LangEntry {

		private boolean missing;

		ForeignLangEntry(String key, String value, Map<String, String> localizationMap) {
			super(key, localizationMap.getOrDefault(key, "UNLOCALIZED: " + value));
			missing = !localizationMap.containsKey(key);
		}

		public boolean isMissing() {
			return missing;
		}

	}

}
