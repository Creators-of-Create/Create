package com.simibubi.create.foundation.config.ui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.common.ForgeConfigSpec;

import org.apache.commons.lang3.mutable.MutableInt;
import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.ui.entries.BooleanEntry;
import com.simibubi.create.foundation.config.ui.entries.SubMenuEntry;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;

public class SubMenuConfigScreen extends ConfigScreen {

	ForgeConfigSpec spec;
	UnmodifiableConfig configGroup;
	ConfigScreenList list;


	public SubMenuConfigScreen(Screen parent, ForgeConfigSpec configSpec, UnmodifiableConfig configGroup) {
		super(parent);
		this.spec = configSpec;
		this.configGroup = configGroup;
	}

	public SubMenuConfigScreen(Screen parent, ForgeConfigSpec configSpec) {
		super(parent);
		this.spec = configSpec;
		this.configGroup = configSpec.getValues();
	}

	@Override
	public void tick() {
		super.tick();
		list.tick();
	}

	@Override
	protected void init() {
		widgets.clear();
		super.init();

		int lWidth = width - 66;
		list = new ConfigScreenList(client, lWidth, height - 30, 15, height - 15, 50);
		list.setLeftPos(this.width /2 - list.getWidth()/2);

		children.add(list);

		MutableInt y = new MutableInt(15);

		configGroup.valueMap().forEach((s, o) -> {
			String humanKey = toHumanReadable(s);

			if (o instanceof AbstractConfig) {
				SubMenuEntry entry = new SubMenuEntry(this, humanKey, spec, (UnmodifiableConfig) o);
				list.children().add(entry);

			}  else if (o instanceof ForgeConfigSpec.ConfigValue<?>) {
				ForgeConfigSpec.ConfigValue<?> configValue = (ForgeConfigSpec.ConfigValue<?>) o;
				ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(configValue.getPath());
				Object value = configValue.get();

				if (value instanceof Boolean) {
					BooleanEntry entry = new BooleanEntry(humanKey, (ForgeConfigSpec.ConfigValue<Boolean>) configValue, valueSpec);
					list.children().add(entry);
				} else {
					AbstractSimiWidget widget = createWidgetForValue(configValue, valueSpec, value, s, this);
					widget.y = y.getValue();
					//list.children().add(new ConfigScreenList.WrappedEntry(widget));
					list.children().add(new ConfigScreenList.LabeledEntry(humanKey + " : " + value));
					//widgets.add(widget);
				}
			}

			y.add(50);
		});
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(ms, mouseX, mouseY, partialTicks);

		list.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	public void resize(@Nonnull Minecraft client, int width, int height) {
		double scroll = list.getScrollAmount();
		init(client, width, height);
		list.setScrollAmount(scroll);
	}

	public static AbstractSimiWidget createWidgetForValue(ForgeConfigSpec.ConfigValue<?> configValue, ForgeConfigSpec.ValueSpec valueSpec, Object value, String key, SubMenuConfigScreen parent) {
		String title = toHumanReadable(key);
		title += " : " + value;
		TextStencilElement text = new TextStencilElement(parent.client.fontRenderer, title).at(5, 11, 0);
		return ConfigButton.createFromStencilElement(parent.width/2 - 100, 0, text);
	}
}
