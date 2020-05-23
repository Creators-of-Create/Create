package com.simibubi.create.foundation.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.text.translate.JavaUnicodeEscaper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.JSONUtils;

@SuppressWarnings("deprecation")
public class LangMerger implements IDataProvider {

	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting()
		.disableHtmlEscaping()
		.create();
	static final String CATEGORY_HEADER = "\t\"_\": \"->------------------------]  %s  [------------------------<-\",";

	private DataGenerator gen;
	private List<Object> data;

	public LangMerger(DataGenerator gen) {
		this.gen = gen;
		this.data = new ArrayList<>();
	}

	@Override
	public String getName() {
		return "Lang merger";
	}

	@Override
	public void act(DirectoryCache cache) throws IOException {
		Path path = this.gen.getOutputFolder()
			.resolve("assets/" + Create.ID + "/lang/" + "en_us.json");
		collectExistingEntries(path);
		collectEntries();
		if (data.isEmpty())
			return;
		save(cache, path);
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

		data.add("\n");
		data.add(header);
		data.add("\n\n");

		MutableObject<String> previousKey = new MutableObject<>("");
		jsonobject.entrySet()
			.stream()
			.forEachOrdered(entry -> {
				String key = entry.getKey();
				String value = entry.getValue()
					.getAsString();
				if (!previousKey.getValue()
					.isEmpty() && shouldAddLineBreak(key, previousKey.getValue()))
					data.add("\n");
				data.add(new LangEntry(key, value));
				previousKey.setValue(key);
			});

		data.add("\n");
	}

	protected boolean shouldAddLineBreak(String key, String previousKey) {
		// Always put tooltips in their own paragraphs
		if (key.endsWith(".tooltip"))
			return true;

		key = new String(key).replaceFirst("\\.", "");
		previousKey = new String(previousKey).replaceFirst("\\.", "");

		String[] split = key.split("\\.");
		String[] split2 = previousKey.split("\\.");
		if (split.length == 0 || split2.length == 0)
			return false;
		
		// Start new paragraph if keys before second point do not match
		return !split[0].equals(split2[0]);
	}

	private void collectEntries() {
		for (AllLangPartials partial : AllLangPartials.values()) {
			String fileName = Lang.asId(partial.name());
			String filepath = "assets/" + Create.ID + "/lang/default/" + fileName + ".json";
			JsonElement element = FilesHelper.loadJsonResource(filepath);
			if (element == null)
				throw new IllegalStateException(String.format("Could not find default lang file: %s", filepath));
			addAll(partial.getDisplay(), element.getAsJsonObject());
		}
	}

	private void save(DirectoryCache cache, Path target) throws IOException {
		String data = createString();
		data = JavaUnicodeEscaper.outsideOf(0, 0x7f)
			.translate(data);
		String hash = IDataProvider.HASH_FUNCTION.hashUnencodedChars(data)
			.toString();
		if (!Objects.equals(cache.getPreviousHash(target), hash) || !Files.exists(target)) {
			Files.createDirectories(target.getParent());

			try (BufferedWriter bufferedwriter = Files.newBufferedWriter(target)) {
				bufferedwriter.write(data);
				bufferedwriter.close();
			}
		}

		cache.recordHash(target, hash);
	}

	protected String createString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		data.forEach(builder::append);
		builder.append("\t\"_\": \"Thank you for translating Create!\"\n\n");
		builder.append("}");
		return builder.toString();
	}

	private class LangEntry {
		static final String ENTRY_FORMAT = "\t\"%s\": \"%s\",\n";

		private String key;
		private String value;

		LangEntry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format(ENTRY_FORMAT, key, value);
		}

	}

}
