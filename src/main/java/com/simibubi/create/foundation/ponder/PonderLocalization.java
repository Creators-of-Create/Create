package com.simibubi.create.foundation.ponder;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.content.PonderTagScreen;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;

public class PonderLocalization {

	static Map<String, String> shared = new HashMap<>();
	static Map<String, Couple<String>> tag = new HashMap<>();
	static Map<String, Map<String, String>> specific = new HashMap<>();

	//

	public static void registerTag(String key, String enUS, String description) {
		tag.put(key, Couple.create(enUS, description));
	}

	public static void registerShared(String key, String enUS) {
		shared.put(key, enUS);
	}

	public static void registerSpecific(String sceneId, String key, String enUS) {
		specific.computeIfAbsent(sceneId, $ -> new HashMap<>())
			.put(key, enUS);
	}

	//

	public static String getShared(String key) {
		if (PonderIndex.EDITOR_MODE)
			return shared.containsKey(key) ? shared.get(key) : ("unregistered shared entry:" + key);
		return Lang.translate(langKeyForShared(key));
	}

	public static String getSpecific(String sceneId, String k) {
		if (PonderIndex.EDITOR_MODE)
			return specific.get(sceneId)
				.get(k);
		return Lang.translate(langKeyForSpecific(sceneId, k));
	}

	public static String getTag(String key) {
		if (PonderIndex.EDITOR_MODE)
			return tag.containsKey(key) ? tag.get(key)
				.getFirst() : ("unregistered tag entry:" + key);
		return Lang.translate(langKeyForTag(key));
	}

	public static String getTagDescription(String key) {
		if (PonderIndex.EDITOR_MODE)
			return tag.containsKey(key) ? tag.get(key)
				.getSecond() : ("unregistered tag entry:" + key);
		return Lang.translate(langKeyForTagDescription(key));
	}

	//

	public static final String LANG_PREFIX = "ponder.";

	public static JsonElement record() {
		JsonObject object = new JsonObject();

		addGeneral(object, PonderTooltipHandler.HOLD_TO_PONDER, "Hold [%1$s] to Ponder");
		addGeneral(object, PonderTooltipHandler.SUBJECT, "Subject of this scene");
		addGeneral(object, PonderUI.PONDERING, "Pondering about...");
		addGeneral(object, PonderUI.IDENTIFY_MODE, "Identify mode active.\nUnpause with [%1$s]");
		addGeneral(object, PonderTagScreen.ASSOCIATED, "Associated Entries");

		shared.forEach((k, v) -> object.addProperty(Create.ID + "." + langKeyForShared(k), v));
		tag.forEach((k, v) -> {
			object.addProperty(Create.ID + "." + langKeyForTag(k), v.getFirst());
			object.addProperty(Create.ID + "." + langKeyForTagDescription(k), v.getSecond());
		});

		specific.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByKey())
			.forEach(entry -> {
				entry.getValue()
					.entrySet()
					.stream()
					.sorted(Map.Entry.comparingByKey())
					.forEach(subEntry -> object.addProperty(Create.ID + "." + langKeyForSpecific(entry.getKey(), subEntry.getKey()),
						subEntry.getValue()));
			});
		return object;
	}

	private static void addGeneral(JsonObject json, String key, String enUS) {
		json.addProperty(Create.ID + "." + key, enUS);
	}

	protected static String langKeyForSpecific(String sceneId, String k) {
		return LANG_PREFIX + sceneId + "." + k;
	}

	protected static String langKeyForShared(String k) {
		return LANG_PREFIX + "shared." + k;
	}

	protected static String langKeyForTag(String k) {
		return LANG_PREFIX + "tag." + k;
	}

	protected static String langKeyForTagDescription(String k) {
		return LANG_PREFIX + "tag." + k + ".description";
	}

}
