package com.simibubi.create.content.curiosities.projector;

import java.util.Collections;
import java.util.Vector;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ChromaticProjectorScreen extends AbstractSimiScreen {

	private AllGuiTextures background;
	private ChromaticProjectorTileEntity tile;
	private Vector<FilterStep> stages;

	private ItemStack renderedItem = ItemStack.EMPTY;//AllBlocks.CHROMATIC_PROJECTOR.asStack();
	private Vector<Vector<ScrollInput>> inputs;

	private IconButton confirmButton;

	private ScrollInput radius;
	private ScrollInput density;
	private ScrollInput feather;
	private ScrollInput fade;

	private IconButton blend;

	private ScrollInput strength;
	private IconButton fieldEffect;

	private IconButton rChannel;
	private IconButton gChannel;
	private IconButton bChannel;

	public ChromaticProjectorScreen(ChromaticProjectorTileEntity te) {
		super(Lang.translate("gui.chromatic_projector.title"));
		background = AllGuiTextures.PROJECTOR;
		tile = te;
		stages = te.stages;
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height);
		setWindowOffset(-25, 0);
		super.init();
		widgets.clear();

		int x = guiLeft;
		int y = guiTop;

		inputs = new Vector<>(FilterStep.MAX_STEPS);
		for (int row = 0; row < inputs.capacity(); row++)
			inputs.add(new Vector<>(2));

		for (int row = 0; row < stages.size(); row++)
			initInputsOfRow(row, x, y);

		confirmButton =
				new IconButton(x + background.width - 33, y + background.height - 26, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);

		initEffectSettings(x, y);
		initMetaSettings(x, y);
	}

	public void initInputsOfRow(int x, int y, int row) {
		x += 30;
		y += 18;
		int rowHeight = 22;

		Vector<ScrollInput> rowInputs = inputs.get(row);
		rowInputs.forEach(widgets::remove);
		rowInputs.clear();
		FilterStep filter = stages.get(row);

		final int x1 = x;
		final int y1 = y;
		ScrollInput type =
				new SelectionScrollInput(x, y + rowHeight * row, 86, 18)
						.forOptions(ColorEffect.getOptions())
						.calling(state -> stageUpdated(x1, y1, row, state))
						.setState(filter.filter.id)
						.titled(Lang.translate("gui.chromatic_projector.filter"));
		ScrollInput value =
				new ScrollInput(x + 86 + 2, y + rowHeight * row, 28, 18)
						.calling(state -> filter.value = state);

		rowInputs.add(type);
		rowInputs.add(value);

		widgets.addAll(rowInputs);
		updateParamsOfRow(row);
	}

	public void updateParamsOfRow(int row) {
		FilterStep instruction = stages.get(row);
		Vector<ScrollInput> rowInputs = inputs.get(row);
		ColorEffect def = instruction.filter;
		boolean hasValue = def.hasParameter;

		ScrollInput value = rowInputs.get(1);
		value.active = value.visible = hasValue;
		if (hasValue)
			value.withRange(def.minValue, def.maxValue + 1)
					//.titled(Lang.translate(def.parameterKey))
					.setState(instruction.value)
					.onChanged();

		value.withStepFunction(def.step());
	}

	private void initEffectSettings(int x, int y) {
		x += 188;
		y += 40;

		radius = new ScrollInput(x, y, 28, 18)
				.titled(Lang.translate("gui.chromatic_projector.radius"))
				.withStepFunction(ctx -> step(ctx, 2))
				.calling(tile::setRadius)
				.withRange(0, 201)
				.setState((int) (tile.radius * 2));
		y += 22;
		feather = new ScrollInput(x, y, 28, 18)
				.titled(Lang.translate("gui.chromatic_projector.feather"))
				.withStepFunction(ctx -> step(ctx, 5))
				.calling(tile::setFeather)
				.withRange(0, 201)
				.setState((int) (tile.feather * 10));
		y += 22;
		density = new ScrollInput(x, y, 28, 18)
				.titled(Lang.translate("gui.chromatic_projector.density"))
				.withStepFunction(ctx -> step(ctx, 10))
				.calling(tile::setDensity)
				.withRange(0, 401)
				.setState((int) (tile.density * 100));
		y += 22;
		fade = new ScrollInput(x, y, 28, 18)
				.titled(Lang.translate("gui.chromatic_projector.fade"))
				.withStepFunction(ctx -> step(ctx, 1))
				.calling(tile::setFade)
				.withRange(0, 51)
				.setState((int) (tile.fade * 10));

		Collections.addAll(widgets, radius, density, feather, fade);
	}

	private void initMetaSettings(int x, int y) {
		y += background.height - 23;

		blend = new IconButton(x + 16, y, AllIcons.I_FX_BLEND);
		blend.setToolTip(Lang.translate("gui.chromatic_projector.blend"));

		int channelX = x + 39;
		rChannel = new IconButton(channelX, y, AllIcons.I_FX_BLEND);
		rChannel.setToolTip(new StringTextComponent("R"));
		channelX += 18;
		gChannel = new IconButton(channelX, y, AllIcons.I_FX_BLEND);
		gChannel.setToolTip(new StringTextComponent("G"));
		channelX += 18;
		bChannel = new IconButton(channelX, y, AllIcons.I_FX_BLEND);
		bChannel.setToolTip(new StringTextComponent("B"));

		fieldEffect = new IconButton(x + 135, y, tile.field ? AllIcons.I_FX_FIELD_ON : AllIcons.I_FX_FIELD_OFF);
		fieldEffect.setToolTip(Lang.translate("gui.chromatic_projector.field"));

		strength = new ScrollInput(x + 159, y, 25, 18)
				.titled(Lang.translate("gui.chromatic_projector.strength"))
				.withStepFunction(ctx -> step(ctx, 5))
				.calling(tile::setStrength)
				.withRange(-100, 101)
				.setState((int) (tile.strength * 100));

		Collections.addAll(widgets, blend, rChannel, gChannel, bChannel, fieldEffect, strength);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.draw(ms, this, x, y);

		for (int row = 0; row < stages.capacity(); row++) {
			AllGuiTextures toDraw = AllGuiTextures.PROJECTOR_EMPTY;
			int yOffset = toDraw.height * row;
			if (row >= stages.size()) {
				toDraw.draw(ms, x, y + 14 + yOffset);
				continue;
			}

			FilterStep step = stages.get(row);
			ColorEffect def = step.filter;
			def.background.draw(ms, x, y + 14 + yOffset);

			if (def != ColorEffect.END)
				label(ms, 36, yOffset - 3, Lang.translate(def.translationKey));
			if (def.hasParameter) {
				String text = step.filter.formatValue(step.value);
				int stringWidth = textRenderer.getStringWidth(text);
				label(ms, 118 + (12 - stringWidth / 2), yOffset - 3, new StringTextComponent(text));
			}
		}

		renderScroll(ms, radius, 2f);
		renderScroll(ms, density, 100f);
		renderScroll(ms, feather, 10f);
		renderScroll(ms, fade, 10f);

		renderScroll(ms, strength, 100f);

		drawCenteredText(ms, textRenderer, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

		GuiGameElement.of(renderedItem)
				.scale(5)
				.at(x + background.width + 6, y + background.height - 56, -200)
				.render(ms);
	}

	private void renderScroll(MatrixStack matrixStack, ScrollInput input, float divisor) {
		String text = String.valueOf(input.getState() / divisor);

//		int stringWidth = textRenderer.getStringWidth(text);
		textRenderer.drawWithShadow(matrixStack, text, input.x + 2, input.y + 5, 0xFFFFEE);
	}

	private void label(MatrixStack matrixStack, int x, int y, ITextComponent text) {
		textRenderer.drawWithShadow(matrixStack, text, guiLeft + x, guiTop + 26 + y, 0xFFFFEE);
	}

	private static Integer step(ScrollValueBehaviour.StepContext ctx, int base) {
		if (ctx.control) return 1;
		return base * (ctx.shift ? 5 : 1) - ctx.currentValue % base;
	}

	public void sendPacket() {
		AllPackets.channel.sendToServer(new ConfigureProjectorPacket(tile));
	}

	@Override
	public void removed() {
		sendPacket();
	}

	private void stageUpdated(int x, int y, int index, int state) {
		ColorEffect newValue = ColorEffect.all.get(state);
		stages.get(index).filter = newValue;
		stages.get(index).value = newValue.defaultValue;
		updateParamsOfRow(index);
		if (newValue == ColorEffect.END) {
			for (int i = stages.size() - 1; i > index; i--) {
				stages.remove(i);
				Vector<ScrollInput> rowInputs = inputs.get(i);
				rowInputs.forEach(widgets::remove);
				rowInputs.clear();
			}
		} else {
			if (index + 1 < stages.capacity() && index + 1 == stages.size()) {
				stages.add(new FilterStep(ColorEffect.END));
				initInputsOfRow(x, y, index + 1);
			}
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (confirmButton.isHovered()) {
			client.player.closeScreen();
			return true;
		}

		if (blend.isHovered()) {
			tile.blend = !tile.blend;
			return true;
		}

		if (fieldEffect.isHovered()) {
			tile.field = !tile.field;

			fieldEffect.setIcon(tile.field ? AllIcons.I_FX_FIELD_ON : AllIcons.I_FX_FIELD_OFF);
			return fieldEffect.mouseClicked(x, y, button);
		}

		if (rChannel.isHovered()) {
			tile.rMask = !tile.rMask;
			return true;
		}

		if (gChannel.isHovered()) {
			tile.gMask = !tile.gMask;
			return true;
		}

		if (bChannel.isHovered()) {
			tile.bMask = !tile.bMask;
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

}
