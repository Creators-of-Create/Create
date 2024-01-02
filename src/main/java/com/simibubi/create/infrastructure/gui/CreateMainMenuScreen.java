package com.simibubi.create.infrastructure.gui;

import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.ui.BaseConfigScreen;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.element.BoxElement;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;
import com.simibubi.create.foundation.ponder.ui.PonderTagIndexScreen;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
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
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (firstRenderTime == 0L)
			this.firstRenderTime = Util.getMillis();
		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		float f = (float) (Util.getMillis() - this.firstRenderTime) / 1000.0F;
		float alpha = Mth.clamp(f, 0.0F, 1.0F);
		float elapsedPartials = minecraft.getDeltaFrameTime();

		if (parent instanceof TitleScreen) {
			if (alpha < 1)
				vanillaPanorama.render(elapsedPartials, 1);
			PANORAMA.render(elapsedPartials, alpha);

			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			graphics.blit(PANORAMA_OVERLAY_TEXTURES, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
		}

		RenderSystem.enableDepthTest();

		PoseStack ms = graphics.pose();

		for (int side : Iterate.positiveAndNegative) {
			ms.pushPose();
			ms.translate(width / 2, 60, 200);
			ms.scale(24 * side, 24 * side, 32);
			ms.translate(-1.75 * ((alpha * alpha) / 2f + .5f), .25f, 0);
			TransformStack.of(ms)
				.rotateX(45);
			GuiGameElement.of(AllBlocks.LARGE_COGWHEEL.getDefaultState())
				.rotateBlock(0, Util.getMillis() / 32f * side, 0)
				.render(graphics);
			ms.translate(-1, 0, -1);
			GuiGameElement.of(AllBlocks.COGWHEEL.getDefaultState())
				.rotateBlock(0, Util.getMillis() / -16f * side + 22.5f, 0)
				.render(graphics);
			ms.popPose();
		}

		ms.pushPose();
		ms.translate(width / 2 - 32, 32, -10);
		ms.pushPose();
		ms.scale(0.25f, 0.25f, 0.25f);
		AllGuiTextures.LOGO.render(graphics, 0, 0);
		ms.popPose();
		new BoxElement().withBackground(0x88_000000)
			.flatBorder(new Color(0x01_000000))
			.at(-32, 56, 100)
			.withBounds(128, 11)
			.render(graphics);
		ms.popPose();

		ms.pushPose();
		ms.translate(0, 0, 200);
		graphics.drawCenteredString(font, Components.literal(Create.NAME).withStyle(ChatFormatting.BOLD)
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

		addRenderableWidget(Button.builder(Lang.translateDirect("menu.return"), $ -> linkTo(parent))
			.bounds(center - 100, yStart + 92, bLongWidth, bHeight)
			.build());
		addRenderableWidget(Button.builder(Lang.translateDirect("menu.configure"), $ -> linkTo(BaseConfigScreen.forCreate(this)))
			.bounds(center - 100, yStart + 24 + -16, bLongWidth, bHeight)
			.build());

		gettingStarted = Button.builder(Lang.translateDirect("menu.ponder_index"), $ -> linkTo(new PonderTagIndexScreen()))
			.bounds(center + 2, yStart + 48 + -16, bShortWidth, bHeight)
			.build();
		gettingStarted.active = !(parent instanceof TitleScreen);
		addRenderableWidget(gettingStarted);

		addRenderableWidget(new PlatformIconButton(center - 100, yStart + 48 + -16, bShortWidth / 2, bHeight,
			AllGuiTextures.CURSEFORGE_LOGO, 0.085f,
			b -> linkTo(CURSEFORGE_LINK),
			Tooltip.create(CURSEFORGE_TOOLTIP)));
		addRenderableWidget(new PlatformIconButton(center - 50, yStart + 48 + -16, bShortWidth / 2, bHeight,
			AllGuiTextures.MODRINTH_LOGO, 0.0575f,
			b -> linkTo(MODRINTH_LINK),
			Tooltip.create(MODRINTH_TOOLTIP)));

		addRenderableWidget(Button.builder(Lang.translateDirect("menu.report_bugs"), $ -> linkTo(ISSUE_TRACKER_LINK))
			.bounds(center + 2, yStart + 68, bShortWidth, bHeight)
			.build());
		addRenderableWidget(Button.builder(Lang.translateDirect("menu.support"), $ -> linkTo(SUPPORT_LINK))
			.bounds(center - 100, yStart + 68, bShortWidth, bHeight)
			.build());
	}

	@Override
	protected void renderWindowForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWindowForeground(graphics, mouseX, mouseY, partialTicks);
		renderables.forEach(w -> w.render(graphics, mouseX, mouseY, partialTicks));

		if (parent instanceof TitleScreen) {
			if (mouseX < gettingStarted.getX() || mouseX > gettingStarted.getX() + 98)
				return;
			if (mouseY < gettingStarted.getY() || mouseY > gettingStarted.getY() + 20)
				return;
			graphics.renderComponentTooltip(font,
				TooltipHelper.cutTextComponent(Lang.translateDirect("menu.only_ingame"), Palette.ALL_GRAY), mouseX,
				mouseY);
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

		public PlatformIconButton(int pX, int pY, int pWidth, int pHeight, AllGuiTextures icon, float scale, OnPress pOnPress, Tooltip tooltip) {
			super(pX, pY, pWidth, pHeight, Components.immutableEmpty(), pOnPress, DEFAULT_NARRATION);
			this.icon = icon;
			this.scale = scale;
			setTooltip(tooltip);
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pt) {
			super.renderWidget(graphics, pMouseX, pMouseY, pt);
			PoseStack pPoseStack = graphics.pose();
			pPoseStack.pushPose();
			pPoseStack.translate(getX() + width / 2 - (icon.width * scale) / 2, getY() + height / 2 - (icon.height * scale) / 2, 0);
			pPoseStack.scale(scale, scale, 1);
			icon.render(graphics, 0, 0);
			pPoseStack.popPose();
		}
	}

}
