package com.simibubi.create.foundation.data;

import com.google.gson.JsonElement;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.AbstractRegistrate;

/**
 * @deprecated Use {@link AbstractRegistrate#addRawLang} or, if absolutely necessary, {@link CreateRegistrate#addLangPostprocessor} instead.
 */
@Deprecated(forRemoval = true)
public interface LangPartial {
	String getDisplayName();

	JsonElement provide();

	static JsonElement fromResource(String namespace, String fileName) {
		String path = "assets/" + namespace + "/lang/default/" + fileName + ".json";
		JsonElement element = FilesHelper.loadJsonResource(path);
		if (element == null)
			throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
		return element;
	}
}
