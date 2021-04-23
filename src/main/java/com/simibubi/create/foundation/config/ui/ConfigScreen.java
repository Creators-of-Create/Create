package com.simibubi.create.foundation.config.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.StencilElement;
import com.simibubi.create.foundation.utility.animation.Force;
import com.simibubi.create.foundation.utility.animation.PhysicalFloat;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Direction;

public abstract class ConfigScreen extends AbstractSimiScreen {

	/*
	 *
	 * TODO
	 * zelo's list for configUI
	 *
	 * adjust transition animation of screens -> disabled for now
	 * move config button's animations to ponder button or a new superclass
	 * get some proper icons for reset button and enum cycle
	 *
	 * some color themes maybe?
	 * at least a helper class to unite colors throughout different uis
	 *
	 * FIXME
	 *
	 * tooltip are hidden underneath the scrollbar, if the bar is near the middle
	 * misalignment of the label-streak and textboxes/enum stuff
	 * framebuffer blending is incorrect
	 *
	 * */

	public static final PhysicalFloat cogSpin = PhysicalFloat.create().withDrag(0.3).addForce(new Force.Static(.2f));
	public static final BlockState cogwheelState = AllBlocks.LARGE_COGWHEEL.getDefaultState().with(CogWheelBlock.AXIS, Direction.Axis.Y);
	public static final Map<String, Object> changes = new HashMap<>();
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
		RenderSystem.disableDepthTest();
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
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = (int) (width * 0.5f);
		int y = (int) (height * 0.5f);
		//this.drawHorizontalLine(ms, x-25, x+25, y, 0xff_807060);
		//this.drawVerticalLine(ms, x, y-25, y+25, 0xff_90a0b0);

		//this.testStencil.render(ms);

		//UIRenderHelper.streak(ms, 0, mouseX, mouseY, 16, 50, 0xaa_1e1e1e);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		cogSpin.bump(3, -delta * 5);

		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	public static String toHumanReadable(String key) {
		String s = Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(key)).map(StringUtils::capitalize).collect(Collectors.joining(" "));
		return s;
	}

	protected void renderCog(MatrixStack ms, float partialTicks) {
		ms.push();

		ms.translate(-100, 100, -100);
		ms.scale(200, 200, .1f);
		GuiGameElement.of(cogwheelState)
				.rotateBlock(22.5, cogSpin.getValue(partialTicks), 22.5)
				.render(ms);

		ms.pop();
	}
}
