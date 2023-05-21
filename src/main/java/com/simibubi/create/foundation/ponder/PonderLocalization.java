package com.simibubi.create.foundation.ponder;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.ui.PonderTagIndexScreen;
import com.simibubi.create.foundation.ponder.ui.PonderTagScreen;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.infrastructure.ponder.AllPonderTags;
import com.simibubi.create.infrastructure.ponder.PonderIndex;
import com.simibubi.create.infrastructure.ponder.SharedText;
import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class PonderLocalization {

	static final Map<ResourceLocation, String> SHARED = new HashMap<>();
	static final Map<ResourceLocation, Couple<String>> TAG = new HashMap<>();
	static final Map<ResourceLocation, String> CHAPTER = new HashMap<>();
	static final Map<ResourceLocation, Map<String, String>> SPECIFIC = new HashMap<>();

	//

	public static void registerShared(ResourceLocation key, String enUS) {
		SHARED.put(key, enUS);
	}

	public static void registerTag(ResourceLocation key, String enUS, String description) {
		TAG.put(key, Couple.create(enUS, description));
	}

	public static void registerChapter(ResourceLocation key, String enUS) {
		CHAPTER.put(key, enUS);
	}

	public static void registerSpecific(ResourceLocation sceneId, String key, String enUS) {
		SPECIFIC.computeIfAbsent(sceneId, $ -> new HashMap<>())
			.put(key, enUS);
	}

	//

	public static String getShared(ResourceLocation key) {
		if (PonderIndex.editingModeActive())
			return SHARED.containsKey(key) ? SHARED.get(key) : ("unregistered shared entry: " + key);
		return I18n.get(langKeyForShared(key));
	}

	public static String getTag(ResourceLocation key) {
		if (PonderIndex.editingModeActive())
			return TAG.containsKey(key) ? TAG.get(key)
				.getFirst() : ("unregistered tag entry: " + key);
		return I18n.get(langKeyForTag(key));
	}

	public static String getTagDescription(ResourceLocation key) {
		if (PonderIndex.editingModeActive())
			return TAG.containsKey(key) ? TAG.get(key)
				.getSecond() : ("unregistered tag entry: " + key);
		return I18n.get(langKeyForTagDescription(key));
	}

	public static String getChapter(ResourceLocation key) {
		if (PonderIndex.editingModeActive())
			return CHAPTER.containsKey(key) ? CHAPTER.get(key) : ("unregistered chapter entry: " + key);
		return I18n.get(langKeyForChapter(key));
	}

	public static String getSpecific(ResourceLocation sceneId, String k) {
		if (PonderIndex.editingModeActive())
			return SPECIFIC.get(sceneId)
				.get(k);
		return I18n.get(langKeyForSpecific(sceneId, k));
	}

	//

	public static final String LANG_PREFIX = "ponder.";

	public static void record(String namespace, JsonObject object) {
		SHARED.forEach((k, v) -> {
			if (k.getNamespace().equals(namespace)) {
				object.addProperty(langKeyForShared(k), v);
			}
		});

		TAG.forEach((k, v) -> {
			if (k.getNamespace().equals(namespace)) {
				object.addProperty(langKeyForTag(k), v.getFirst());
				object.addProperty(langKeyForTagDescription(k), v.getSecond());
			}
		});

		CHAPTER.forEach((k, v) -> {
			if (k.getNamespace().equals(namespace)) {
				object.addProperty(langKeyForChapter(k), v);
			}
		});

		SPECIFIC.entrySet()
			.stream()
			.filter(entry -> entry.getKey().getNamespace().equals(namespace))
			.sorted(Map.Entry.comparingByKey())
			.forEach(entry -> {
				entry.getValue()
					.entrySet()
					.stream()
					.sorted(Map.Entry.comparingByKey())
					.forEach(subEntry -> object.addProperty(
						langKeyForSpecific(entry.getKey(), subEntry.getKey()), subEntry.getValue()));
			});
	}

	private static void recordGeneral(JsonObject object) {
		addGeneral(object, PonderTooltipHandler.HOLD_TO_PONDER, "Hold [%1$s] to Ponder");
		addGeneral(object, PonderTooltipHandler.SUBJECT, "Subject of this scene");
		addGeneral(object, PonderUI.PONDERING, "Pondering about...");
		addGeneral(object, PonderUI.IDENTIFY_MODE, "Identify mode active.\nUnpause with [%1$s]");
		addGeneral(object, PonderTagScreen.ASSOCIATED, "Associated Entries");

		addGeneral(object, PonderUI.CLOSE, "Close");
		addGeneral(object, PonderUI.IDENTIFY, "Identify");
		addGeneral(object, PonderUI.NEXT, "Next Scene");
		addGeneral(object, PonderUI.NEXT_UP, "Up Next:");
		addGeneral(object, PonderUI.PREVIOUS, "Previous Scene");
		addGeneral(object, PonderUI.REPLAY, "Replay");
		addGeneral(object, PonderUI.THINK_BACK, "Think Back");
		addGeneral(object, PonderUI.SLOW_TEXT, "Comfy Reading");

		addGeneral(object, PonderTagIndexScreen.EXIT, "Exit");
		addGeneral(object, PonderTagIndexScreen.WELCOME, "Welcome to Ponder");
		addGeneral(object, PonderTagIndexScreen.CATEGORIES, "Available Categories in Create");
		addGeneral(object, PonderTagIndexScreen.DESCRIPTION,
			"Click one of the icons to learn about its associated Items and Blocks");
		addGeneral(object, PonderTagIndexScreen.TITLE, "Ponder Index");
	}

	private static void addGeneral(JsonObject json, String key, String enUS) {
		json.addProperty(Create.ID + "." + key, enUS);
	}

	public static void generateSceneLang() {
		PonderRegistry.ALL.forEach((id, list) -> {
			for (int i = 0; i < list.size(); i++)
				PonderRegistry.compileScene(i, list.get(i), null);
		});
	}

	/**
	 * Internal use only.
	 */
	public static JsonObject provideLangEntries() {
		SharedText.gatherText();
		AllPonderTags.register();
		PonderIndex.register();

		generateSceneLang();

		JsonObject object = new JsonObject();
		recordGeneral(object);
		record(Create.ID, object);
		return object;
	}

	public static void provideRegistrateLang(AbstractRegistrate<?> registrate) {
		generateSceneLang();

		JsonObject object = new JsonObject();
		record(registrate.getModid(), object);

		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			registrate.addRawLang(entry.getKey(), entry.getValue().getAsString());
		}
	}

	//

	protected static String langKeyForShared(ResourceLocation k) {
		return k.getNamespace() + "." + LANG_PREFIX + "shared." + k.getPath();
	}

	protected static String langKeyForTag(ResourceLocation k) {
		return k.getNamespace() + "." + LANG_PREFIX + "tag." + k.getPath();
	}

	protected static String langKeyForTagDescription(ResourceLocation k) {
		return k.getNamespace() + "." + LANG_PREFIX + "tag." + k.getPath() + ".description";
	}

	protected static String langKeyForChapter(ResourceLocation k) {
		return k.getNamespace() + "." + LANG_PREFIX + "chapter." + k.getPath();
	}

	protected static String langKeyForSpecific(ResourceLocation sceneId, String k) {
		return sceneId.getNamespace() + "." + LANG_PREFIX + sceneId.getPath() + "." + k;
	}

}
