package com.simibubi.create.foundation.gui;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.ui.BaseConfigScreen;
import com.simibubi.create.foundation.gui.element.BoxElement;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.ponder.content.PonderTagIndexScreen;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class CreateMainMenuScreen extends AbstractSimiScreen {

	protected final Screen parent;
	protected boolean returnOnClose;

	public static final CubeMap PANORAMA_RESOURCES =
		new CubeMap(Create.asResource("textures/gui/title/background/panorama"));
	public static final ResourceLocation PANORAMA_OVERLAY_TEXTURES =
		new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
	public static PanoramaRenderer panorama = new PanoramaRenderer(PANORAMA_RESOURCES);

	private PanoramaRenderer vanillaPanorama;
	private long firstRenderTime;
	private Button gettingStarted;

	public CreateMainMenuScreen(Screen parent) {
		this.parent = parent;
		returnOnClose = true;
		if (parent instanceof TitleScreen)
			vanillaPanorama = ObfuscationReflectionHelper.getPrivateValue(TitleScreen.class, (TitleScreen) parent,
				"f_96729_"); // panorama
		else
			vanillaPanorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);
	}

	@Override
	public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (firstRenderTime == 0L)
			this.firstRenderTime = Util.getMillis();
		super.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		float f = (float) (Util.getMillis() - this.firstRenderTime) / 1000.0F;
		float alpha = Mth.clamp(f, 0.0F, 1.0F);
		float elapsedPartials = minecraft.getDeltaFrameTime();

		if (parent instanceof TitleScreen) {
			if (alpha < 1)
				vanillaPanorama.render(elapsedPartials, 1);
			panorama.render(elapsedPartials, alpha);

			RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY_TEXTURES);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			blit(ms, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
		}

		RenderSystem.enableDepthTest();

		for (int side : Iterate.positiveAndNegative) {
			ms.pushPose();
			ms.translate(width / 2, 60, 200);
			ms.scale(24 * side, 24 * side, 32);
			ms.translate(-1.75 * ((alpha * alpha) / 2f + .5f), .25f, 0);
			TransformStack.cast(ms)
				.rotateX(45);
			GuiGameElement.of(AllBlocks.LARGE_COGWHEEL.getDefaultState())
				.rotateBlock(0, Util.getMillis() / 32f * side, 0)
				.render(ms);
			ms.translate(-1, 0, -1);
			GuiGameElement.of(AllBlocks.COGWHEEL.getDefaultState())
				.rotateBlock(0, Util.getMillis() / -16f * side + 22.5f, 0)
				.render(ms);
			ms.popPose();
		}

		ms.pushPose();
		ms.translate(width / 2 - 32, 32, -10);
		ms.pushPose();
		ms.scale(0.25f, 0.25f, 0.25f);
		AllGuiTextures.LOGO.render(ms, 0, 0, this);
		ms.popPose();
		new BoxElement().withBackground(0x88_000000)
			.flatBorder(new Color(0x01_000000))
			.at(-32, 56, 100)
			.withBounds(128, 11)
			.render(ms);
		ms.popPose();

		ms.pushPose();
		ms.translate(0, 0, 200);
		drawCenteredString(ms, font, new TextComponent(Create.NAME).withStyle(ChatFormatting.BOLD)
			.append(
				new TextComponent(" v" + Create.VERSION).withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE)),
			width / 2, 89, 0xff_E4BB67);
		ms.popPose();

		RenderSystem.disableDepthTest();
	}

	protected void init() {
		super.init();
		returnOnClose = true;
		this.addButtons();
	}

	private void addButtons() {
		int yStart = height / 4 + (parent instanceof TitleScreen ? 40 : 40);
		int center = width / 2;
		int bHeight = 20;
		int bShortWidth = 98;
		int bLongWidth = 200;

		addRenderableWidget(
			new Button(center - 100, yStart + 92, bLongWidth, bHeight, Lang.translate("menu.return"), $ -> onClose()));
		addRenderableWidget(new Button(center - 100, yStart + 24 + -16, bLongWidth, bHeight, Lang.translate("menu.configure"),
			$ -> linkTo(BaseConfigScreen.forCreate(this))));

		gettingStarted = new Button(center + 2, yStart + 48 + -16, bShortWidth, bHeight,
			Lang.translate("menu.ponder_index"), $ -> linkTo(new PonderTagIndexScreen()));
		gettingStarted.active = !(parent instanceof TitleScreen);
		addRenderableWidget(gettingStarted);

		String projectLink = "https://www.curseforge.com/minecraft/mc-mods/create";
		String issueTrackerLink = "https://github.com/Creators-of-Create/Create/issues";
		String supportLink = "https://github.com/Creators-of-Create/Create/wiki/Supporting-the-Project";

		addRenderableWidget(new Button(center - 100, yStart + 48 + -16, bShortWidth, bHeight, Lang.translate("menu.project_page"),
			$ -> linkTo(projectLink)));
		addRenderableWidget(new Button(center + 2, yStart + 68, bShortWidth, bHeight, Lang.translate("menu.report_bugs"),
			$ -> linkTo(issueTrackerLink)));
		addRenderableWidget(new Button(center - 100, yStart + 68, bShortWidth, bHeight, Lang.translate("menu.support"),
			$ -> linkTo(supportLink)));
	}

	@Override
	protected void renderWindowForeground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderWindowForeground(ms, mouseX, mouseY, partialTicks);
		renderables.forEach(w -> w.render(ms, mouseX, mouseY, partialTicks));

		if (parent instanceof TitleScreen) {
			if (mouseX < gettingStarted.x || mouseX > gettingStarted.x + 98)
				return;
			if (mouseY < gettingStarted.y || mouseY > gettingStarted.y + 20)
				return;
			renderComponentTooltip(ms, TooltipHelper.cutTextComponent(Lang.translate("menu.only_ingame"), ChatFormatting.GRAY,
				ChatFormatting.GRAY), mouseX, mouseY);
		}
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
		ScreenOpener.open(new ConfirmLinkScreen((p_213069_2_) -> {
			if (p_213069_2_)
				Util.getPlatform()
					.openUri(url);
			this.minecraft.setScreen(this);
		}, url, true));
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	@Override
	public void onClose() {
		super.onClose();
		ScreenOpener.open(parent);
	}

}
