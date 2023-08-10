package com.simibubi.create.foundation.blockEntity.behaviour;

import java.util.function.Function;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.network.chat.MutableComponent;

public class ValueSettingsFormatter {

	private Function<ValueSettings, MutableComponent> formatter;

	public ValueSettingsFormatter(Function<ValueSettings, MutableComponent> formatter) {
		this.formatter = formatter;
	}

	public MutableComponent format(ValueSettings valueSettings) {
		return formatter.apply(valueSettings);
	}

	public static class ScrollOptionSettingsFormatter extends ValueSettingsFormatter {

		private INamedIconOptions[] options;

		public ScrollOptionSettingsFormatter(INamedIconOptions[] options) {
			super(v -> CreateLang.translateDirect(options[v.value()].getTranslationKey()));
			this.options = options;
		}

		public AllIcons getIcon(ValueSettings valueSettings) {
			return options[valueSettings.value()].getIcon();
		}

	}

}
