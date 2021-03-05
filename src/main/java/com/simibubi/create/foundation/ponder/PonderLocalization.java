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

import net.minecraft.util.ResourceLocation;

public class PonderLocalization {

	static Map<String, String> shared = new HashMap<>();
	static Map<String, Couple<String>> tag = new HashMap<>();
	static Map<ResourceLocation, Map<Integer, Map<String, String>>> specific = new HashMap<>();

	//

	public static void registerTag(String key, String enUS, String description) {
		tag.put(key, Couple.create(enUS, description));
	}

	public static void registerShared(String key, String enUS) {
		shared.put(key, enUS);
	}

	public static void registerSpecific(ResourceLocation component, int scene, String key, String enUS) {
		specific.computeIfAbsent(component, $ -> new HashMap<>())
			.computeIfAbsent(scene, $ -> new HashMap<>())
			.put(key, enUS);
	}

	//

	public static String getShared(String key) {
		if (PonderIndex.EDITOR_MODE)
			return shared.containsKey(key) ? shared.get(key) : ("unregistered shared entry:" + key);
		return Lang.translate(langKeyForShared(key));
	}

	public static String getSpecific(ResourceLocation component, int scene, String k) {
		if (PonderIndex.EDITOR_MODE)
			return specific.get(component)
				.get(scene)
				.get(k);
		return Lang.translate(langKeyForSpecific(component.getPath(), scene, k));
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
		
		specific.forEach((rl, map) -> {
			String component = rl.getPath();
			for (int i = 0; i < map.size(); i++) {
				final int scene = i;
				Map<String, String> sceneMap = map.get(i);
				sceneMap.entrySet()
					.stream()
					.sorted(Map.Entry.comparingByKey())
					.forEach(e -> object.addProperty(Create.ID + "." + langKeyForSpecific(component, scene, e.getKey()),
						e.getValue()));
			}
		});
		return object;
	}

	private static void addGeneral(JsonObject json, String key, String enUS) {
		json.addProperty(Create.ID + "." + key, enUS);
	}

	protected static String langKeyForSpecific(String component, int scene, String k) {
		return LANG_PREFIX + component + ".scene_" + scene + "." + k;
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
