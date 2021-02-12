package com.simibubi.create.foundation.metadoc;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.metadoc.content.MetaDocIndex;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.ResourceLocation;

public class MetaDocLocalization {

	static Map<String, String> shared = new HashMap<>();
	static Map<ResourceLocation, Map<Integer, Map<String, String>>> specific = new HashMap<>();

	//

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
		if (MetaDocIndex.EDITOR_MODE)
			return shared.get(key);
		return Lang.translate(langKeyForShared(key));
	}

	public static String getSpecific(ResourceLocation component, int scene, String k) {
		if (MetaDocIndex.EDITOR_MODE)
			return specific.get(component)
				.get(scene)
				.get(k);
		return Lang.translate(langKeyForSpecific(component.getPath(), scene, k));
	}

	//

	static final String LANG_PREFIX = "metadoc.";

	public static JsonElement record() {
		JsonObject object = new JsonObject();
		shared.forEach((k, v) -> object.addProperty(Create.ID + "." + langKeyForShared(k), v));
		specific.forEach((rl, map) -> {
			String component = rl.getPath();
			for (int i = 0; i < map.size(); i++) {
				final int scene = i;
				Map<String, String> sceneMap = map.get(i);
				sceneMap.forEach(
					(k, v) -> object.addProperty(Create.ID + "." + langKeyForSpecific(component, scene, k), v));
			}
		});
		return object;
	}

	protected static String langKeyForSpecific(String component, int scene, String k) {
		return LANG_PREFIX + component + ".scene_" + scene + "." + k;
	}

	protected static String langKeyForShared(String k) {
		return LANG_PREFIX + "shared." + k;
	}

}
