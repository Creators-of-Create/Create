package com.simibubi.create.foundation.data;

import com.google.common.base.Supplier;
import com.google.gson.JsonElement;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.FilesHelper;

public interface ILangPartial {

	String getDisplay();
	String getFileName();

	default String getModID() { return Create.ID; }

	JsonElement provide();

	private JsonElement fromResource() {
		String fileName = getFileName();
		String filepath = "assets/" + getModID() + "/lang/default/" + fileName + ".json";
		JsonElement element = FilesHelper.loadJsonResource(filepath);
		if (element == null)
			throw new IllegalStateException(String.format("Could not find default lang file: %s", filepath));
		return element;
	}

	default Supplier<JsonElement> getDefault() { return this::fromResource; }
}
