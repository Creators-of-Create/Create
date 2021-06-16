package com.simibubi.create.foundation.gui.mainMenu;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.ui.BaseConfigScreen;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.BoxElement;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderSkybox;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class CreateMainMenuScreen extends AbstractSimiScreen {

	protected final Screen parent;
	protected boolean returnOnClose;

	private static final RenderSkyboxCube PANORAMA_RESOURCES =
		new RenderSkyboxCube(Create.asResource("textures/gui/title/background/panorama"));
	private static final ResourceLocation PANORAMA_OVERLAY_TEXTURES =
		new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
	private RenderSkybox vanillaPanorama = new RenderSkybox(MainMenuScreen.PANORAMA_RESOURCES);
	private RenderSkybox panorama = new RenderSkybox(PANORAMA_RESOURCES);
	private long firstRenderTime;

	public CreateMainMenuScreen(Screen parent) {
		this.parent = parent;
		returnOnClose = true;
		if (parent instanceof MainMenuScreen)
			vanillaPanorama = ObfuscationReflectionHelper.getPrivateValue(MainMenuScreen.class, (MainMenuScreen) parent,
				"field_209101_K");
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		if (firstRenderTime == 0L)
			this.firstRenderTime = Util.milliTime();
		super.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		float f = (float) (Util.milliTime() - this.firstRenderTime) / 1000.0F;
		float alpha = MathHelper.clamp(f, 0.0F, 1.0F);
		
		if (parent instanceof MainMenuScreen) {
			if (alpha < 1)
				vanillaPanorama.render(partialTicks, 1);
			panorama.render(partialTicks, alpha);

			client.getTextureManager()
				.bindTexture(PANORAMA_OVERLAY_TEXTURES);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			drawTexture(ms, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
		}

		RenderSystem.enableDepthTest();

		for (int side : Iterate.positiveAndNegative) {
			ms.push();
			ms.translate(width / 2, 60, -800);
			ms.scale(24 * side, 24 * side, 32);
			ms.translate(-1.75 * ((alpha * alpha) / 2f + .5f), .25f, 0);
			MatrixStacker.of(ms)
				.rotateX(45);
			GuiGameElement.of(AllBlocks.LARGE_COGWHEEL.getDefaultState())
				.rotateBlock(0, Util.milliTime() / 32f * side, 0)
				.render(ms);
			ms.translate(-1, 0, -1);
			GuiGameElement.of(AllBlocks.COGWHEEL.getDefaultState())
				.rotateBlock(0, Util.milliTime() / -16f * side + 22.5f, 0)
				.render(ms);
			ms.pop();
		}

		ms.push();
		ms.translate(width / 2 - 32, 32, -10);
		ms.push();
		ms.scale(0.25f, 0.25f, 0.25f);
		AllGuiTextures.LOGO.draw(ms, 0, 0);
		ms.pop();
		new BoxElement().withBackground(0x88_000000)
			.flatBorder(new Color(0x01_000000, true))
			.at(-32, 56, 100)
			.withBounds(128, 11)
			.render(ms);
		ms.pop();

		ms.push();
		ms.translate(0, 0, 200);
		drawCenteredText(ms, textRenderer, new StringTextComponent(Create.NAME).formatted(TextFormatting.BOLD)
			.append(new StringTextComponent(" v" + Create.VERSION).formatted(TextFormatting.BOLD, TextFormatting.WHITE)),
			width / 2, 89, 0xff_E4BB67);
		ms.pop();

		RenderSystem.disableDepthTest();
	}

	protected void init() {
		super.init();
		returnOnClose = true;
		this.addButtons();
	}

	private void addButtons() {
		buttons.clear();

		int yStart = height / 4 + (parent instanceof MainMenuScreen ? 40 : 40);
		int center = width / 2;
		int bHeight = 20;
		int bShortWidth = 98;
		int bLongWidth = 200;

		addButton(new Button(center - 100, yStart + 92, bLongWidth, bHeight, Lang.translate("menu.return"),
			$ -> onClose()));
		addButton(new Button(center - 100, yStart + 24 + -16, bLongWidth, bHeight, Lang.translate("menu.configure"),
			$ -> linkTo(BaseConfigScreen.forCreate(this))));

		Button gettingStarted = new Button(center + 2, yStart + 48 + -16, bShortWidth, bHeight,
			Lang.translate("menu.getting_started"), $ -> {
			});
		gettingStarted.active = false;
		addButton(gettingStarted);

		String projectLink = "https://www.curseforge.com/minecraft/mc-mods/create";
		String issueTrackerLink = "https://github.com/Creators-of-Create/Create/issues";
		String supportLink = "https://github.com/Creators-of-Create/Create/wiki/Supporting-the-Project";

		addButton(new Button(center - 100, yStart + 48 + -16, bShortWidth, bHeight, Lang.translate("menu.project_page"),
			$ -> linkTo(projectLink)));
		addButton(new Button(center + 2, yStart + 68, bShortWidth, bHeight, Lang.translate("menu.report_bugs"),
			$ -> linkTo(issueTrackerLink)));
		addButton(new Button(center - 100, yStart + 68, bShortWidth, bHeight, Lang.translate("menu.support"),
			$ -> linkTo(supportLink)));
	}

	@Override
	protected void renderWindowForeground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderWindowForeground(ms, mouseX, mouseY, partialTicks);
		buttons.forEach(w -> w.render(ms, mouseX, mouseY, partialTicks));
	}

	public void tick() {
		super.tick();
	}

	private void linkTo(Screen screen) {
		returnOnClose = false;
		ScreenOpener.open(screen);
	}

	private void linkTo(String url) {
		returnOnClose = false;
		ScreenOpener.open(new ConfirmOpenLinkScreen((p_213069_2_) -> {
			if (p_213069_2_)
				Util.getOSType()
					.openURI(url);
			this.client.displayGuiScreen(this);
		}, url, true));
	}

	@Override
	public void onClose() {
		super.onClose();
		ScreenOpener.open(parent);
	}

}
