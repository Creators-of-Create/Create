package com.simibubi.create.foundation.data;

import com.google.common.base.Supplier;
import com.google.gson.JsonElement;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.foundation.utility.Lang;

import java.util.List;

public enum AllLangPartials implements ILangPartial {

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
		this.provider = getDefault();
	}

	private AllLangPartials(String display, Supplier<JsonElement> customProvider) {
		this.display = display;
		this.provider = customProvider;
	}

	@Override
	public String getDisplay() {
		return display;
	}

	@Override
	public String getFileName() {
		return Lang.asId(name());
	}

	@Override
	public JsonElement provide() {
		return provider.get();
	}

}
