package com.simibubi.create.content.schematics.client;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.schematics.client.tools.ToolType;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ToolSelectionScreen extends Screen {

	public final String scrollToCycle = Lang.translateDirect("gui.toolmenu.cycle")
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
		super(Components.literal("Tool Selection"));
		this.minecraft = Minecraft.getInstance();
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

	private void draw(PoseStack matrixStack, float partialTicks) {
		Window mainWindow = minecraft.getWindow();
		if (!initialized)
			init(minecraft, mainWindow.getGuiScaledWidth(), mainWindow.getGuiScaledHeight());

		int x = (mainWindow.getGuiScaledWidth() - w) / 2 + 15;
		int y = mainWindow.getGuiScaledHeight() - h - 75;

		matrixStack.pushPose();
		matrixStack.translate(0, -yOffset, focused ? 100 : 0);

		AllGuiTextures gray = AllGuiTextures.HUD_BACKGROUND;
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1, 1, 1, focused ? 7 / 8f : 1 / 2f);

		RenderSystem.setShaderTexture(0, gray.location);
		blit(matrixStack, x - 15, y, gray.startX, gray.startY, w, h, gray.width, gray.height);

		float toolTipAlpha = yOffset / 10;
		List<Component> toolTip = tools.get(selection)
			.getDescription();
		int stringAlphaComponent = ((int) (toolTipAlpha * 0xFF)) << 24;

		if (toolTipAlpha > 0.25f) {
			RenderSystem.setShaderColor(.7f, .7f, .8f, toolTipAlpha);
			blit(matrixStack, x - 15, y + 33, gray.startX, gray.startY, w, h + 22, gray.width, gray.height);
			RenderSystem.setShaderColor(1, 1, 1, 1);

			if (toolTip.size() > 0)
				font.draw(matrixStack, toolTip.get(0), x - 10, y + 38, 0xEEEEEE + stringAlphaComponent);
			if (toolTip.size() > 1)
				font.draw(matrixStack, toolTip.get(1), x - 10, y + 50, 0xCCDDFF + stringAlphaComponent);
			if (toolTip.size() > 2)
				font.draw(matrixStack, toolTip.get(2), x - 10, y + 60, 0xCCDDFF + stringAlphaComponent);
			if (toolTip.size() > 3)
				font.draw(matrixStack, toolTip.get(3), x - 10, y + 72, 0xCCCCDD + stringAlphaComponent);
		}

		RenderSystem.setShaderColor(1, 1, 1, 1);
		if (tools.size() > 1) {
			String keyName = AllKeys.TOOL_MENU.getBoundKey();
			int width = minecraft.getWindow()
				.getGuiScaledWidth();
			if (!focused)
				drawCenteredString(matrixStack, minecraft.font, Lang.translateDirect(holdToFocus, keyName), width / 2,
					y - 10, 0xCCDDFF);
			else
				drawCenteredString(matrixStack, minecraft.font, scrollToCycle, width / 2, y - 10, 0xCCDDFF);
		} else {
			x += 65;
		}

		for (int i = 0; i < tools.size(); i++) {
			matrixStack.pushPose();

			float alpha = focused ? 1 : .2f;
			if (i == selection) {
				matrixStack.translate(0, -10, 0);
				drawCenteredString(matrixStack, minecraft.font, tools.get(i)
					.getDisplayName()
					.getString(), x + i * 50 + 24, y + 28, 0xCCDDFF);
				alpha = 1;
			}
			RenderSystem.setShaderColor(0, 0, 0, alpha);
			tools.get(i)
				.getIcon()
				.render(matrixStack, x + i * 50 + 16, y + 12, this);
			RenderSystem.setShaderColor(1, 1, 1, alpha);
			tools.get(i)
				.getIcon()
				.render(matrixStack, x + i * 50 + 16, y + 11, this);

			matrixStack.popPose();
		}

		RenderSystem.disableBlend();
		matrixStack.popPose();
	}

	public void update() {
		if (focused)
			yOffset += (10 - yOffset) * .1f;
		else
			yOffset *= .9f;
	}

	public void renderPassive(PoseStack matrixStack, float partialTicks) {
		draw(matrixStack, partialTicks);
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
