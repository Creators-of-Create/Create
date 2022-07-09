package com.simibubi.create.foundation.data;

import com.google.common.base.Supplier;
import com.google.gson.JsonElement;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.foundation.utility.Lang;

public enum AllLangPartials {

	ADVANCEMENTS("Advancements", AllAdvancements::provideLangEntries),
	INTERFACE("UI & Messages"),
	SUBTITLES("Subtitles", AllSoundEvents::provideLangEntries),
	TOOLTIPS("Item Descriptions"),
	PONDER("Ponder Content", PonderLocalization::provideLangEntries),

	;

	private String display;
	private Supplier<JsonElement> provider;

	private AllLangPartials(String display) {
		this.display = display;
		this.provider = this::fromResource;
	}

	private AllLangPartials(String display, Supplier<JsonElement> customProvider) {
		this.display = display;
		this.provider = customProvider;
	}

	public String getDisplay() {
		return display;
	}

	public JsonElement provide() {
		return provider.get();
	}

	private JsonElement fromResource() {
		String fileName = Lang.asId(name());
		String filepath = "assets/" + Create.ID + "/lang/default/" + fileName + ".json";
		JsonElement element = FilesHelper.loadJsonResource(filepath);
		if (element == null)
			throw new IllegalStateException(String.format("Could not find default lang file: %s", filepath));
		return element;
	}

}
