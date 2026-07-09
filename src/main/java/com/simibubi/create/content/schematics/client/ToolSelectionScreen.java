package com.simibubi.create.content.schematics.client;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.Window;
import com.simibubi.create.foundation.render.LegacyRenderSystemBridge;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.schematics.client.tools.ToolType;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ToolSelectionScreen extends Screen {

	public final String scrollToCycle = CreateLang.translateDirect("gui.toolmenu.cycle")
		.getString();
	public final String holdToFocus = "gui.toolmenu.focusKey";

	protected List<ToolType> tools;
	protected Consumer<ToolType> callback;
	public boolean focused;
	private float yOffset;
	protected int selection;
	private boolean initialized;

	protected int w;
	protected int h;

	public ToolSelectionScreen(List<ToolType> tools, Consumer<ToolType> callback) {
		super(Component.literal("Tool Selection"));
		this.tools = tools;
		this.callback = callback;
		focused = false;
		yOffset = 0;
		selection = 0;
		initialized = false;

		callback.accept(tools.get(selection));

		w = Math.max(tools.size() * 50 + 30, 220);
		h = 30;
	}

	public void setSelectedElement(ToolType tool) {
		if (!tools.contains(tool))
			return;
		selection = tools.indexOf(tool);
	}

	public void cycle(int direction) {
		selection += (direction < 0) ? 1 : -1;
		selection = (selection + tools.size()) % tools.size();
	}

	private void draw(GuiGraphicsExtractor graphics, float partialTicks) {
		Window mainWindow = minecraft.getWindow();
		if (!initialized)
			init(mainWindow.getGuiScaledWidth(), mainWindow.getGuiScaledHeight());

		int x = (mainWindow.getGuiScaledWidth() - w) / 2 + 15;
		int y = mainWindow.getGuiScaledHeight() - h - 75;

		AllGuiTextures gray = AllGuiTextures.HUD_BACKGROUND;
		LegacyRenderSystemBridge.enableBlend();
		LegacyRenderSystemBridge.setShaderColor(1, 1, 1, focused ? 7 / 8f : 1 / 2f);

		graphics.blit(RenderPipelines.GUI_TEXTURED, gray.location, x - 15, y, gray.getStartX(), gray.getStartY(), w, h,
			gray.getWidth(), gray.getHeight());

		float toolTipAlpha = yOffset / 10;
		List<Component> toolTip = tools.get(selection)
			.getDescription();
		int stringAlphaComponent = ((int) (toolTipAlpha * 0xFF)) << 24;

		if (toolTipAlpha > 0.25f) {
			LegacyRenderSystemBridge.setShaderColor(.7f, .7f, .8f, toolTipAlpha);
			graphics.blit(RenderPipelines.GUI_TEXTURED, gray.location, x - 15, y + 33, gray.getStartX(),
				gray.getStartY(), w, h + 22, gray.getWidth(), gray.getHeight());
			LegacyRenderSystemBridge.setShaderColor(1, 1, 1, 1);

			if (toolTip.size() > 0)
				graphics.text(font, toolTip.get(0), x - 10, y + 38, 0xEEEEEE + stringAlphaComponent, false);
			if (toolTip.size() > 1)
				graphics.text(font, toolTip.get(1), x - 10, y + 50, 0xCCDDFF + stringAlphaComponent, false);
			if (toolTip.size() > 2)
				graphics.text(font, toolTip.get(2), x - 10, y + 60, 0xCCDDFF + stringAlphaComponent, false);
			if (toolTip.size() > 3)
				graphics.text(font, toolTip.get(3), x - 10, y + 72, 0xCCCCDD + stringAlphaComponent, false);
		}

		LegacyRenderSystemBridge.setShaderColor(1, 1, 1, 1);
		if (tools.size() > 1) {
			String keyName = AllKeys.TOOL_MENU.getBoundKey();
			int width = minecraft.getWindow()
				.getGuiScaledWidth();
			if (!focused)
				graphics.centeredText(minecraft.font, CreateLang.translateDirect(holdToFocus, keyName), width / 2,
					y - 10, 0xCCDDFF);
			else
				graphics.centeredText(minecraft.font, scrollToCycle, width / 2, y - 10, 0xCCDDFF);
		} else {
			x += 65;
		}


		for (int i = 0; i < tools.size(); i++) {
			LegacyRenderSystemBridge.enableBlend();

			float alpha = focused ? 1 : .2f;
			if (i == selection) {
				LegacyRenderSystemBridge.setShaderColor(1, 1, 1, 1);
				graphics.centeredText(minecraft.font, tools.get(i)
					.getDisplayName()
					.getString(), x + i * 50 + 24, y + 28, 0xCCDDFF);
				alpha = 1;
			}
			LegacyRenderSystemBridge.setShaderColor(0, 0, 0, alpha);
			tools.get(i)
				.getIcon()
				.render(graphics, x + i * 50 + 16, y + 12);
			LegacyRenderSystemBridge.setShaderColor(1, 1, 1, alpha);
			tools.get(i)
				.getIcon()
				.render(graphics, x + i * 50 + 16, y + 11);
		}

		LegacyRenderSystemBridge.setShaderColor(1, 1, 1, 1);
		LegacyRenderSystemBridge.disableBlend();
	}

	public void update() {
		if (focused)
			yOffset += (10 - yOffset) * .1f;
		else
			yOffset *= .9f;
	}

	public void renderPassive(GuiGraphicsExtractor graphics, float partialTicks) {
		draw(graphics, partialTicks);
	}

	@Override
	public void onClose() {
		callback.accept(tools.get(selection));
	}

	@Override
	protected void init() {
		super.init();
		initialized = true;
	}
}
