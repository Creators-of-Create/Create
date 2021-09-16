package com.simibubi.create.foundation.config.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.StencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.mainMenu.CreateMainMenuScreen;
import com.simibubi.create.foundation.utility.animation.Force;
import com.simibubi.create.foundation.utility.animation.PhysicalFloat;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlConst;
import net.minecraft.core.Direction;

public abstract class ConfigScreen extends AbstractSimiScreen {

	/*
	 *
	 * TO DO
	 *
	 * reduce number of packets sent to the server when saving a bunch of values
	 *
	 * FIXME
	 *
	 * tooltips are hidden underneath the scrollbar, if the bar is near the middle
	 *
	 * */

	public static final Map<String, TriConsumer<Screen, PoseStack, Float>> backgrounds = new HashMap<>();
	public static final PhysicalFloat cogSpin = PhysicalFloat.create().withLimit(10f).withDrag(0.3).addForce(new Force.Static(.2f));
	public static final BlockState cogwheelState = AllBlocks.LARGE_COGWHEEL.getDefaultState().setValue(CogWheelBlock.AXIS, Direction.Axis.Y);
	public static String modID = null;
	protected final Screen parent;

	public ConfigScreen(Screen parent) {
		this.parent = parent;
	}

	@Override
	public void tick() {
		super.tick();
		cogSpin.tick();
	}

	@Override
	public void renderBackground(@Nonnull PoseStack ms) {
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this, ms));
	}

	@Override
	protected void renderWindowBackground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (this.minecraft != null && this.minecraft.level != null) {
			//in game
			fill(ms, 0, 0, this.width, this.height, 0xb0_282c34);
		} else {
			//in menus
			renderMenuBackground(ms, partialTicks);
		}

		new StencilElement() {
			@Override
			protected void renderStencil(PoseStack ms) {
				renderCog(ms, partialTicks);
			}

			@Override
			protected void renderElement(PoseStack ms) {
				fill(ms, -200, -200, 200, 200, 0x60_000000);
			}
		}.at(width * 0.5f, height * 0.5f, 0).render(ms);

		super.renderWindowBackground(ms, mouseX, mouseY, partialTicks);

	}

	@Override
	protected void prepareFrame() {
		RenderTarget thisBuffer = UIRenderHelper.framebuffer;
		RenderTarget mainBuffer = Minecraft.getInstance().getMainRenderTarget();

		GlCompat functions = Backend.getInstance().compat;
		functions.fbo.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainBuffer.frameBufferId);
		functions.fbo.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, thisBuffer.frameBufferId);
		functions.blit.blitFramebuffer(0, 0, mainBuffer.viewWidth, mainBuffer.viewHeight, 0, 0, mainBuffer.viewWidth, mainBuffer.viewHeight, GL30.GL_COLOR_BUFFER_BIT, GL20.GL_LINEAR);

		functions.fbo.bindFramebuffer(GlConst.GL_FRAMEBUFFER, thisBuffer.frameBufferId);
		GL11.glClear(GL30.GL_STENCIL_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

	}

	@Override
	protected void endFrame() {

		RenderTarget thisBuffer = UIRenderHelper.framebuffer;
		RenderTarget mainBuffer = Minecraft.getInstance().getMainRenderTarget();

		GlCompat functions = Backend.getInstance().compat;
		functions.fbo.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, thisBuffer.frameBufferId);
		functions.fbo.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mainBuffer.frameBufferId);
		functions.blit.blitFramebuffer(0, 0, mainBuffer.viewWidth, mainBuffer.viewHeight, 0, 0, mainBuffer.viewWidth, mainBuffer.viewHeight, GL30.GL_COLOR_BUFFER_BIT, GL20.GL_LINEAR);

		functions.fbo.bindFramebuffer(GlConst.GL_FRAMEBUFFER, mainBuffer.frameBufferId);
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		cogSpin.bump(3, -delta * 5);

		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	public static String toHumanReadable(String key) {
		String s = key.replaceAll("_", " ");
		s = Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(s)).map(StringUtils::capitalize).collect(Collectors.joining(" "));
		s = StringUtils.normalizeSpace(s);
		return s;
	}

	/**
	 * By default ConfigScreens will render the Create Panorama as
	 * their background when opened from the Main- or ModList-Menu.
	 * If your addon wants to render something else, please add to the
	 * backgrounds Map in this Class with your modID as the key.
	 */
	protected void renderMenuBackground(PoseStack ms, float partialTicks) {
		TriConsumer<Screen, PoseStack, Float> customBackground = backgrounds.get(modID);
		if (customBackground != null) {
			customBackground.accept(this, ms, partialTicks);
			return;
		}

		float elapsedPartials = minecraft.getDeltaFrameTime();
		CreateMainMenuScreen.panorama.render(elapsedPartials, 1);

		minecraft.getTextureManager().bind(CreateMainMenuScreen.PANORAMA_OVERLAY_TEXTURES);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		blit(ms, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);

		fill(ms, 0, 0, this.width, this.height, 0x90_282c34);
	}

	protected void renderCog(PoseStack ms, float partialTicks) {
		ms.pushPose();

		ms.translate(-100, 100, -100);
		ms.scale(200, 200, 1);
		GuiGameElement.of(cogwheelState)
				.rotateBlock(22.5, cogSpin.getValue(partialTicks), 22.5)
				.render(ms);

		ms.popPose();
	}
}
