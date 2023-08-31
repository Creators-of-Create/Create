package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.TooltipArea;

import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.nbt.CompoundTag;

public class ModularGuiLine {

	List<Pair<AbstractWidget, String>> widgets;
	List<Couple<Integer>> customBoxes;
	boolean speechBubble;

	public ModularGuiLine() {
		widgets = new ArrayList<>();
		customBoxes = new ArrayList<>();
		speechBubble = false;
	}

	public void renderWidgetBG(int guiLeft, GuiGraphics graphics) {
		boolean first = true;

		if (!customBoxes.isEmpty()) {
			for (Couple<Integer> couple : customBoxes) {
				int x = couple.getFirst() + guiLeft;
				int width = couple.getSecond();
				box(graphics, x, width, first & speechBubble);
				first = false;
			}
			return;
		}

		for (Pair<AbstractWidget, String> pair : widgets) {
			if (pair.getSecond()
				.equals("Dummy"))
				continue;

			AbstractWidget aw = pair.getFirst();
			int x = aw.getX();
			int width = aw.getWidth();

			if (aw instanceof EditBox) {
				x -= 5;
				width += 9;
			}

			box(graphics, x, width, first & speechBubble);
			first = false;
		}
	}

	private void box(GuiGraphics graphics, int x, int width, boolean b) {
		UIRenderHelper.drawStretched(graphics, x, 0, width, 18, 0, AllGuiTextures.DATA_AREA);
		if (b)
			AllGuiTextures.DATA_AREA_SPEECH.render(graphics, x - 3, 0);
		else
			AllGuiTextures.DATA_AREA_START.render(graphics, x, 0);
		AllGuiTextures.DATA_AREA_END.render(graphics, x + width - 2, 0);
	}

	public void saveValues(CompoundTag data) {
		for (Pair<AbstractWidget, String> pair : widgets) {
			AbstractWidget w = pair.getFirst();
			String key = pair.getSecond();
			if (w instanceof EditBox eb)
				data.putString(key, eb.getValue());
			if (w instanceof ScrollInput si)
				data.putInt(key, si.getState());
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends GuiEventListener & Renderable & NarratableEntry> void loadValues(CompoundTag data,
		Consumer<T> addRenderable, Consumer<T> addRenderableOnly) {
		for (Pair<AbstractWidget, String> pair : widgets) {
			AbstractWidget w = pair.getFirst();
			String key = pair.getSecond();
			if (w instanceof EditBox eb)
				eb.setValue(data.getString(key));
			if (w instanceof ScrollInput si)
				si.setState(data.getInt(key));

			if (w instanceof TooltipArea)
				addRenderableOnly.accept((T) w);
			else
				addRenderable.accept((T) w);
		}
	}

	public void forEach(Consumer<GuiEventListener> callback) {
		widgets.forEach(p -> callback.accept(p.getFirst()));
	}

	public void clear() {
		widgets.clear();
		customBoxes.clear();
	}

	public void add(Pair<AbstractWidget, String> pair) {
		widgets.add(pair);
	}

}
