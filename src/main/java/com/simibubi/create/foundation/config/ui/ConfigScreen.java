package com.simibubi.create.foundation.config.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.StencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.utility.animation.Force;
import com.simibubi.create.foundation.utility.animation.PhysicalFloat;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.FramebufferConstants;
import net.minecraft.util.Direction;

public abstract class ConfigScreen extends AbstractSimiScreen {

	/*
	 *
	 * zelo's list for configUI
	 *
	 * reduce number of packets sent to the server when saving a bunch of values
	 * maybe replace java's awt color with something mutable
	 * find out why framebuffer blending is incorrect
	 *
	 * FIXME
	 *
	 * tooltips are hidden underneath the scrollbar, if the bar is near the middle
	 *
	 * */

	public static final PhysicalFloat cogSpin = PhysicalFloat.create().withDrag(0.3).addForce(new Force.Static(.2f));
	public static final BlockState cogwheelState = AllBlocks.LARGE_COGWHEEL.getDefaultState().with(CogWheelBlock.AXIS, Direction.Axis.Y);
	public static final Map<String, Object> changes = new HashMap<>();
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
	public void renderBackground(@Nonnull MatrixStack ms) {
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this, ms));
	}

	@Override
	protected void renderWindowBackground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		if (this.client != null && this.client.world != null) {
			fill(ms, 0, 0, this.width, this.height, 0xb0_282c34);
		} else {
			fill(ms, 0, 0, this.width, this.height, 0xff_282c34);
		}

		new StencilElement() {
			@Override
			protected void renderStencil(MatrixStack ms) {
				renderCog(ms, partialTicks);
			}

			@Override
			protected void renderElement(MatrixStack ms) {
				fill(ms, -200, -200, 200, 200, 0x60_000000);
			}
		}.at(width * 0.5f, height * 0.5f, 0).render(ms);

		super.renderWindowBackground(ms, mouseX, mouseY, partialTicks);

	}

	@Override
	protected void prepareFrame() {
		Framebuffer thisBuffer = UIRenderHelper.framebuffer;
		Framebuffer mainBuffer = Minecraft.getInstance().getFramebuffer();

		GlCompat functions = Backend.getInstance().compat;
		functions.fbo.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainBuffer.framebufferObject);
		functions.fbo.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, thisBuffer.framebufferObject);
		functions.blit.blitFramebuffer(0, 0, mainBuffer.framebufferWidth, mainBuffer.framebufferHeight, 0, 0, mainBuffer.framebufferWidth, mainBuffer.framebufferHeight, GL30.GL_COLOR_BUFFER_BIT, GL20.GL_LINEAR);

		functions.fbo.bindFramebuffer(FramebufferConstants.FRAME_BUFFER, thisBuffer.framebufferObject);
		GL11.glClear(GL30.GL_STENCIL_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

	}

	@Override
	protected void endFrame() {

		Framebuffer thisBuffer = UIRenderHelper.framebuffer;
		Framebuffer mainBuffer = Minecraft.getInstance().getFramebuffer();

		GlCompat functions = Backend.getInstance().compat;
		functions.fbo.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, thisBuffer.framebufferObject);
		functions.fbo.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mainBuffer.framebufferObject);
		functions.blit.blitFramebuffer(0, 0, mainBuffer.framebufferWidth, mainBuffer.framebufferHeight, 0, 0, mainBuffer.framebufferWidth, mainBuffer.framebufferHeight, GL30.GL_COLOR_BUFFER_BIT, GL20.GL_LINEAR);

		functions.fbo.bindFramebuffer(FramebufferConstants.FRAME_BUFFER, mainBuffer.framebufferObject);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		cogSpin.bump(3, -delta * 5);

		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	public static String toHumanReadable(String key) {
		String s = key.replaceAll("_", " ");
		s = Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(s)).map(StringUtils::capitalize).collect(Collectors.joining(" "));
		s = s.replaceAll("\\s\\s+", " ");
		return s;
	}

	protected void renderCog(MatrixStack ms, float partialTicks) {
		ms.push();

		ms.translate(-100, 100, -100);
		ms.scale(200, 200, 1);
		GuiGameElement.of(cogwheelState)
				.rotateBlock(22.5, cogSpin.getValue(partialTicks), 22.5)
				.render(ms);

		ms.pop();
	}
}
