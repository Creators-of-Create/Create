package com.simibubi.create.foundation.config.ui;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.StencilElement;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.ponder.NavigatableSimiScreen;
import com.simibubi.create.foundation.utility.animation.Force;
import com.simibubi.create.foundation.utility.animation.PhysicalFloat;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Direction;

public abstract class ConfigScreen extends NavigatableSimiScreen {

	/*
	* TODO match style with ponderUI
	* TODO cache changes before setting values and saving to file
	* TODO don't exit on ESC
	* TODO reset text field focus for any click inside screen
	* TODO adjust transition animation of screens
	* TODO allow backspace in text fields
	*
	* TODO some color themes maybe?
	*
	* */

	protected final Screen parent;
	protected static final PhysicalFloat cogSpin = PhysicalFloat.create().withDrag(0.3).addForce(new Force.Static(.2f));
	protected static final BlockState cogwheelState = AllBlocks.LARGE_COGWHEEL.getDefaultState().with(CogWheelBlock.AXIS, Direction.Axis.Y);

	protected StencilElement testStencil;

	public ConfigScreen(Screen parent) {
		this.parent = parent;
	}

	@Override
	public void tick() {
		cogSpin.tick();

		widgets.stream()
				.filter(w -> w instanceof ConfigButton)
				.forEach(w -> ((ConfigButton) w).tick());

		super.tick();
	}

	@Override
	protected void init() {
		/*super.init();
		if (backTrack != null) {
			widgets.remove(backTrack);
			backTrack = null;
		}*/


		testStencil = new TextStencilElement(client.fontRenderer, "POGGERS").at(width*0.5f, height*0.5f, 0);
	}

	@Override
	public void renderBackground(@Nonnull MatrixStack ms) {
		//fill(ms, 0, 0, this.width, this.height, 0xe8_101010);
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this, ms));
	}

	@Override
	protected void renderWindowBackground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.disableDepthTest();
		if (this.client != null && this.client.world != null){
			fill(ms, 0, 0, this.width, this.height, 0xb0_282c34);
		} else {
			fill(ms, 0, 0, this.width, this.height, 0xff_282c34);
		}

		/*ms.push();
		ms.translate(width*0.5f, height*0.5f, 0);
		renderCog(ms, partialTicks);
		ms.pop();*/

		new StencilElement() {
			@Override
			protected void renderStencil(MatrixStack ms) {
				renderCog(ms, partialTicks);
			}

			@Override
			protected void renderElement(MatrixStack ms) {
				fill(ms, -200, -200, 200, 200, 0x40_000000);
			}
		}.at(width * 0.5f, height * 0.5f, 0).render(ms);

		super.renderWindowBackground(ms, mouseX, mouseY, partialTicks);

	}

	protected void renderCog(MatrixStack ms, float partialTicks) {
		ms.push();

		ms.translate(-100, 100, -200);
		ms.scale(200, 200, .1f);
		GuiGameElement.of(cogwheelState)
				.rotateBlock(22.5, cogSpin.getValue(partialTicks), 22.5)
				.render(ms);

		ms.pop();
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
}
