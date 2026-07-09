package com.simibubi.create.infrastructure.gui;

import com.simibubi.create.foundation.render.GlStateManager;
import com.simibubi.create.foundation.render.LegacyRenderSystemBridge;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.CreateBuildInfo;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.CreateLang;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.client.config.BaseConfigScreen;
import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.client.gui.AbstractSimiScreen;
import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.createmod.catnip.api.client.gui.element.BoxElement;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.createmod.catnip.api.client.lang.FontHelper;
import net.createmod.catnip.api.client.lang.FontHelper.Palette;
import net.createmod.catnip.api.theme.Color;
import net.createmod.ponder.impl.client.gui.PonderTagIndexScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import com.simibubi.create.foundation.render.PanoramaRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix3x2fStack;

public class CreateMainMenuScreen extends AbstractSimiScreen {

	public static final CubeMap PANORAMA_RESOURCES =
		new CubeMap(Create.asResource("textures/gui/title/background/panorama"));
	public static final Identifier PANORAMA_OVERLAY_TEXTURES =
		Identifier.withDefaultNamespace("textures/gui/title/background/panorama_overlay.png");
	public static final PanoramaRenderer PANORAMA = new PanoramaRenderer(PANORAMA_RESOURCES);

	private static final Component CURSEFORGE_TOOLTIP;

	static {
		CURSEFORGE_TOOLTIP = Component.literal("CurseForge").withStyle(s -> s.withColor(0xFC785C).withBold(true));
	}

	private static final Component MODRINTH_TOOLTIP;

	static {
		MODRINTH_TOOLTIP = Component.literal("Modrinth").withStyle(s -> s.withColor(0x3FD32B).withBold(true));
	}

	public static final String CURSEFORGE_LINK = "https://www.curseforge.com/minecraft/mc-mods/create";
	public static final String MODRINTH_LINK = "https://modrinth.com/mod/create";
	public static final String ISSUE_TRACKER_LINK = "https://github.com/Creators-of-Create/Create/issues";
	public static final String SUPPORT_LINK = "https://github.com/Creators-of-Create/Create/wiki/Supporting-the-Project";

	protected final Screen parent;
	protected boolean returnOnClose;

	private long firstRenderTime;
	private Button gettingStarted;

	public CreateMainMenuScreen(Screen parent) {
		this.parent = parent;
		returnOnClose = true;
	}

	@Override
	protected void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		if (firstRenderTime == 0L)
			this.firstRenderTime = Util.getMillis();
		float f = (float) (Util.getMillis() - this.firstRenderTime) / 1000.0F;
		float alpha = Mth.clamp(f, 0.0F, 1.0F);

		if (parent instanceof TitleScreen) {
			PANORAMA.render(graphics, this.width, this.height, 1, partialTicks);

			LegacyRenderSystemBridge.enableBlend();
			LegacyRenderSystemBridge.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			graphics.blit(RenderPipelines.GUI_TEXTURED, PANORAMA_OVERLAY_TEXTURES, 0, 0, 0.0F, 0.0F, this.width,
				this.height, 16, 128, 16, 128);
		} else {
			graphics.fill(0, 0, width, height, 0xff101010);
		}

		LegacyRenderSystemBridge.enableDepthTest();

		Matrix3x2fStack ms = graphics.pose();

		for (int side : Iterate.positiveAndNegative) {
			ms.pushMatrix();
			ms.translate(width / 2f, 60);
			ms.scale(24 * side, 24 * side);
			ms.translate((float) (-1.75 * ((alpha * alpha) / 2f + .5f)), .25f);
			GuiGameElement.of(AllBlocks.LARGE_COGWHEEL.getDefaultState())
				.rotateBlock(0, Util.getMillis() / 32f * side, 0)
				.render(graphics);
			ms.translate(-1, 0);
			GuiGameElement.of(AllBlocks.COGWHEEL.getDefaultState())
				.rotateBlock(0, Util.getMillis() / -16f * side + 22.5f, 0)
				.render(graphics);
			ms.popMatrix();
		}

		LegacyRenderSystemBridge.enableBlend();

		ms.pushMatrix();
		ms.translate(width / 2f - 32, 32);
		ms.pushMatrix();
		ms.scale(0.25f, 0.25f);
		AllGuiTextures.LOGO.render(graphics, 0, 0);
		ms.popMatrix();
		new BoxElement().withBackground(0x88_000000)
			.flatBorder(new Color(0x01_000000))
			.at(-32, 56)
			.withBounds(128, 11)
			.render(graphics);
		ms.popMatrix();

		ms.pushMatrix();
		graphics.centeredText(font, Component.literal(Create.NAME).withStyle(ChatFormatting.BOLD)
				.append(
					Component.literal(" v" + CreateBuildInfo.VERSION).withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE)),
			width / 2, 89, 0xFF_E4BB67);
		ms.popMatrix();

		LegacyRenderSystemBridge.disableDepthTest();
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

		addRenderableWidget(Button.builder(CreateLang.translateDirect("menu.return"), $ -> linkTo(parent))
			.bounds(center - 100, yStart + 92, bLongWidth, bHeight)
			.build());
		addRenderableWidget(Button.builder(CreateLang.translateDirect("menu.configure"), $ -> linkTo(new BaseConfigScreen(this, Create.ID)))
			.bounds(center - 100, yStart + 24 + -16, bLongWidth, bHeight)
			.build());

		gettingStarted = Button.builder(CreateLang.translateDirect("menu.ponder_index"), $ -> linkTo(new PonderTagIndexScreen()))
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

		addRenderableWidget(Button.builder(CreateLang.translateDirect("menu.report_bugs"), $ -> linkTo(ISSUE_TRACKER_LINK))
			.bounds(center + 2, yStart + 68, bShortWidth, bHeight)
			.build());
		addRenderableWidget(Button.builder(CreateLang.translateDirect("menu.support"), $ -> linkTo(SUPPORT_LINK))
			.bounds(center - 100, yStart + 68, bShortWidth, bHeight)
			.build());
	}

	@Override
	protected void renderWindowForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWindowForeground(graphics, mouseX, mouseY, partialTicks);
		renderables.forEach(w -> w.extractRenderState(graphics, mouseX, mouseY, partialTicks));

		if (parent instanceof TitleScreen) {
			if (mouseX < gettingStarted.getX() || mouseX > gettingStarted.getX() + 98)
				return;
			if (mouseY < gettingStarted.getY() || mouseY > gettingStarted.getY() + 20)
				return;
			graphics.setComponentTooltipForNextFrame(font,
				FontHelper.cutTextComponent(CreateLang.translateDirect("menu.only_ingame"), Palette.ALL_GRAY), mouseX,
				mouseY, ItemStack.EMPTY);
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
			this.minecraft.gui.setScreen(this);
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
			super(pX, pY, pWidth, pHeight, CommonComponents.EMPTY, pOnPress, DEFAULT_NARRATION);
			this.icon = icon;
			this.scale = scale;
			setTooltip(tooltip);
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pt) {
			Matrix3x2fStack pPoseStack = graphics.pose();
			pPoseStack.pushMatrix();
			pPoseStack.translate(getX() + width / 2f - (icon.getWidth() * scale) / 2f, getY() + height / 2f - (icon.getHeight() * scale) / 2f);
			pPoseStack.scale(scale, scale);
			icon.render(graphics, 0, 0);
			pPoseStack.popMatrix();
		}
	}

}
