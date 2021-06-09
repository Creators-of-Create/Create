package com.simibubi.create.foundation.gui.mainMenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.ui.BaseConfigScreen;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Lang;

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
		if (parent instanceof MainMenuScreen) {
			float f = (float) (Util.milliTime() - this.firstRenderTime) / 1000.0F;
			float alpha = MathHelper.clamp(f, 0.0F, 1.0F);
			if (alpha < 1)
				vanillaPanorama.render(partialTicks, 1);
			panorama.render(partialTicks, alpha);

			client.getTextureManager()
				.bindTexture(PANORAMA_OVERLAY_TEXTURES);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			drawTexture(ms, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
		}
		drawCenteredText(ms, textRenderer, new StringTextComponent("Create is Installed!"), width / 2, 40, 16777215);
	}

	protected void init() {
		super.init();
		returnOnClose = true;
		this.addButtons();
	}

	private void addButtons() {
		buttons.clear();

		int yStart = height / 4 + (parent instanceof MainMenuScreen ? 40 : 16);
		int center = width / 2;
		int bHeight = 20;
		int bShortWidth = 98;
		int bLongWidth = 200;

		addButton(new Button(center - 100, yStart + 24 + -16, bLongWidth, bHeight, Lang.translate("menu.return"),
			$ -> onClose()));
		addButton(new Button(center - 100, yStart + 48 + -16, bShortWidth, bHeight, Lang.translate("menu.configure"),
			$ -> linkTo(BaseConfigScreen.forCreate(this))));

		Button gettingStarted = new Button(center + 2, yStart + 48 + -16, bShortWidth, bHeight,
			Lang.translate("menu.getting_started"), $ -> {
			});
		gettingStarted.active = false;
		addButton(gettingStarted);

		String feedbackLink = "https://discord.gg/hmaD7Se";
		String issueTrackerLink = "https://github.com/Creators-of-Create/Create/issues";
		String supportLink = "https://github.com/Creators-of-Create/Create/wiki/Supporting-the-Project";

		addButton(new Button(center - 100, yStart + 72 + -16, bShortWidth, bHeight,
			Lang.translate("menu.send_feedback"), $ -> linkTo(feedbackLink)));
		addButton(new Button(center + 2, yStart + 72 + -16, bShortWidth, bHeight, Lang.translate("menu.report_bugs"),
			$ -> linkTo(issueTrackerLink)));
		addButton(new Button(center - 100, yStart + 92, bLongWidth, bHeight, Lang.translate("menu.support"),
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
