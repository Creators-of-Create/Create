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
import com.simibubi.create.foundation.ponder.ui.PonderTagIndexScreen;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CreateMainMenuScreen extends AbstractSimiScreen {

	public static final CubeMap PANORAMA_RESOURCES =
		new CubeMap(Create.asResource("textures/gui/title/background/panorama"));
	public static final ResourceLocation PANORAMA_OVERLAY_TEXTURES =
		new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
	public static final PanoramaRenderer PANORAMA = new PanoramaRenderer(PANORAMA_RESOURCES);

	private static final Component CURSEFORGE_TOOLTIP = Components.literal("CurseForge").withStyle(s -> s.withColor(0xFC785C).withBold(true));
	private static final Component MODRINTH_TOOLTIP = Components.literal("Modrinth").withStyle(s -> s.withColor(0x3FD32B).withBold(true));

	public static final String CURSEFORGE_LINK = "https://www.curseforge.com/minecraft/mc-mods/create";
	public static final String MODRINTH_LINK = "https://modrinth.com/mod/create";
	public static final String ISSUE_TRACKER_LINK = "https://github.com/Creators-of-Create/Create/issues";
	public static final String SUPPORT_LINK = "https://github.com/Creators-of-Create/Create/wiki/Supporting-the-Project";

	protected final Screen parent;
	protected boolean returnOnClose;

	private PanoramaRenderer vanillaPanorama;
	private long firstRenderTime;
	private Button gettingStarted;

	public CreateMainMenuScreen(Screen parent) {
		this.parent = parent;
		returnOnClose = true;
		if (parent instanceof TitleScreen titleScreen)
			vanillaPanorama = titleScreen.panorama;
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
			PANORAMA.render(elapsedPartials, alpha);

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
		drawCenteredString(ms, font, Components.literal(Create.NAME).withStyle(ChatFormatting.BOLD)
			.append(
				Components.literal(" v" + Create.VERSION).withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE)),
			width / 2, 89, 0xFF_E4BB67);
		ms.popPose();

		RenderSystem.disableDepthTest();
	}

	protected void init() {
		super.init();
		returnOnClose = true;
		this.addButtons();
	}

	private void addButtons() {
		int yStart = height / 4 + 40;
		int center = width / 2;
		int bHeight = 20;
		int bShortWidth = 98;
		int bLongWidth = 200;

		addRenderableWidget(
			new Button(center - 100, yStart + 92, bLongWidth, bHeight, Lang.translateDirect("menu.return"), $ -> linkTo(parent)));
		addRenderableWidget(new Button(center - 100, yStart + 24 + -16, bLongWidth, bHeight, Lang.translateDirect("menu.configure"),
			$ -> linkTo(BaseConfigScreen.forCreate(this))));

		gettingStarted = new Button(center + 2, yStart + 48 + -16, bShortWidth, bHeight,
			Lang.translateDirect("menu.ponder_index"), $ -> linkTo(new PonderTagIndexScreen()));
		gettingStarted.active = !(parent instanceof TitleScreen);
		addRenderableWidget(gettingStarted);

		addRenderableWidget(new PlatformIconButton(center - 100, yStart + 48 + -16, bShortWidth / 2, bHeight,
			AllGuiTextures.CURSEFORGE_LOGO, 0.085f,
			b -> linkTo(CURSEFORGE_LINK),
			(b, ps, mx, my) -> renderTooltip(ps, CURSEFORGE_TOOLTIP, mx, my)));
		addRenderableWidget(new PlatformIconButton(center - 50, yStart + 48 + -16, bShortWidth / 2, bHeight,
			AllGuiTextures.MODRINTH_LOGO, 0.0575f,
			b -> linkTo(MODRINTH_LINK),
			(b, ps, mx, my) -> renderTooltip(ps, MODRINTH_TOOLTIP, mx, my)));

		addRenderableWidget(new Button(center + 2, yStart + 68, bShortWidth, bHeight,
			Lang.translateDirect("menu.report_bugs"),
			$ -> linkTo(ISSUE_TRACKER_LINK)));
		addRenderableWidget(new Button(center - 100, yStart + 68, bShortWidth, bHeight,
			Lang.translateDirect("menu.support"),
			$ -> linkTo(SUPPORT_LINK)));
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
			renderComponentTooltip(ms, TooltipHelper.cutTextComponent(Lang.translateDirect("menu.only_ingame"), ChatFormatting.GRAY,
				ChatFormatting.GRAY), mouseX, mouseY);
		}
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

	protected static class PlatformIconButton extends Button {
		protected final AllGuiTextures icon;
		protected final float scale;

		public PlatformIconButton(int pX, int pY, int pWidth, int pHeight, AllGuiTextures icon, float scale, OnPress pOnPress, OnTooltip pOnTooltip) {
			super(pX, pY, pWidth, pHeight, Components.immutableEmpty(), pOnPress, pOnTooltip);
			this.icon = icon;
			this.scale = scale;
		}

		@Override
		protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
			pPoseStack.pushPose();
			pPoseStack.translate(x + width / 2 - (icon.width * scale) / 2, y + height / 2 - (icon.height * scale) / 2, 0);
			pPoseStack.scale(scale, scale, 1);
			icon.render(pPoseStack, 0, 0);
			pPoseStack.popPose();
		}
	}

}
