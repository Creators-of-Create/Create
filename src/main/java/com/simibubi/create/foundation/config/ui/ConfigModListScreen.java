package com.simibubi.create.foundation.config.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.element.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.widget.BoxWidget;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

public class ConfigModListScreen extends ConfigScreen {

	ConfigScreenList list;
	HintableTextFieldWidget search;
	BoxWidget goBack;
	List<ModEntry> allEntries;

	public ConfigModListScreen(Screen parent) {
		super(parent);
	}

	@Override
	protected void init() {
		super.init();

		int listWidth = Math.min(width - 80, 300);

		list = new ConfigScreenList(minecraft, listWidth, height - 60, 15, height - 45, 40);
		list.setLeftPos(this.width / 2 - list.getWidth() / 2);
		addRenderableWidget(list);

		allEntries = new ArrayList<>();
		ModList.get().getMods().stream().map(IModInfo::getModId).forEach(id -> allEntries.add(new ModEntry(id, this)));
		allEntries.sort((e1, e2) -> {
			int empty = (e2.button.active ? 1 : 0) - (e1.button.active ? 1 : 0);
			if (empty != 0)
				return empty;

			return e1.id.compareToIgnoreCase(e2.id);
		});
		list.children().clear();
		list.children().addAll(allEntries);

		goBack = new BoxWidget(width / 2 - listWidth / 2 - 30, height / 2 + 65, 20, 20).withPadding(2, 2)
				.withCallback(() -> ScreenOpener.open(parent));
		goBack.showingElement(AllIcons.I_CONFIG_BACK.asStencil()
				.withElementRenderer(BoxWidget.gradientFactory.apply(goBack)));
		goBack.getToolTip()
				.add(Components.literal("Go Back"));
		addRenderableWidget(goBack);

		search = new HintableTextFieldWidget(font, width / 2 - listWidth / 2, height - 35, listWidth, 20);
		search.setResponder(this::updateFilter);
		search.setHint("Search...");
		search.moveCursorToStart();
		addRenderableWidget(search);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers))
			return true;
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			ScreenOpener.open(parent);
		}
		return false;
	}

	private void updateFilter(String search) {
		list.children().clear();
		allEntries
				.stream()
				.filter(modEntry -> modEntry.id.contains(search.toLowerCase(Locale.ROOT)))
				.forEach(list.children()::add);

		list.setScrollAmount(list.getScrollAmount());
		if (list.children().size() > 0) {
			this.search.setTextColor(Theme.i(Theme.Key.TEXT));
		} else {
			this.search.setTextColor(Theme.i(Theme.Key.BUTTON_FAIL));
		}
	}

	public static class ModEntry extends ConfigScreenList.LabeledEntry {

		protected BoxWidget button;
		protected String id;

		public ModEntry(String id, Screen parent) {
			super(BaseConfigScreen.getCustomTitle(id).orElse(toHumanReadable(id)));
			this.id = id;

			button = new BoxWidget(0, 0, 35, 16)
					.showingElement(AllIcons.I_CONFIG_OPEN.asStencil().at(10, 0));
			button.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.gradientFactory.apply(button)));

			if (ConfigHelper.hasAnyForgeConfig(id)) {
				button.withCallback(() -> ScreenOpener.open(new BaseConfigScreen(parent, id)));
			} else {
				button.active = false;
				button.updateColorsFromState();
				button.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BaseConfigScreen.DISABLED_RENDERER));
				labelTooltip.add(Components.literal(BaseConfigScreen.getCustomTitle(id).orElse(toHumanReadable(id))));
				labelTooltip.addAll(TooltipHelper.cutStringTextComponent("This Mod does not have any configs registered or is not using Forge's config system", Palette.ALL_GRAY));
			}

			listeners.add(button);
		}

		public String getId() {
			return id;
		}

		@Override
		public void tick() {
			super.tick();
			button.tick();
		}

		@Override
		public void render(PoseStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
			super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

			button.x = x + width - 108;
			button.y = y + 10;
			button.setHeight(height - 20);
			button.render(ms, mouseX, mouseY, partialTicks);
		}

		@Override
		protected int getLabelWidth(int totalWidth) {
			return (int) (totalWidth * labelWidthMult) + 30;
		}
	}
}
