package com.simibubi.create.content.curiosities.projector;

import java.util.Collections;
import java.util.Vector;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
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

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ChromaticProjectorScreen extends AbstractSimiScreen {

	private final ItemStack renderedItem = AllBlocks.CHROMATIC_PROJECTOR.asStack();
	private final AllGuiTextures background = AllGuiTextures.PROJECTOR;
	private IconButton confirmButton;

	private final ITextComponent title = Lang.translate("gui.chromatic_projector.title");
	private Vector<FilterStep> stages;

	private Vector<Vector<ScrollInput>> inputs;

	ChromaticProjectorTileEntity tile;

	private ScrollInput radius;
	private ScrollInput density;
	private ScrollInput feather;
	private ScrollInput fade;

	public ChromaticProjectorScreen(ChromaticProjectorTileEntity te) {
		this.tile = te;
		this.stages = te.stages;
	}

	private static Integer step(ScrollValueBehaviour.StepContext ctx, int base) {
		if (ctx.control) return 1;
		return base * (ctx.shift ? 5 : 1) - ctx.currentValue % base;
	}

	@Override
	protected void init() {
		setWindowSize(background.width + 50, background.height);
		super.init();
		widgets.clear();

		inputs = new Vector<>(FilterStep.MAX_STEPS);
		for (int row = 0; row < inputs.capacity(); row++)
			inputs.add(new Vector<>(2));

		for (int row = 0; row < stages.size(); row++)
			initInputsOfRow(row);

		confirmButton =
				new IconButton(guiLeft + background.width - 33, guiTop + background.height - 24, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);

		int xRight = guiLeft + 53;
		int xLeft = guiLeft + 93;
		int yTop = guiTop + 117;
		int yBottom = guiTop + 139;

		radius = new ScrollInput(xRight, yTop, 28, 18)
				.titled(new StringTextComponent("Radius"))
				.withStepFunction(ctx -> step(ctx, 2))
				.calling(tile::setRadius)
				.withRange(0, 201)
				.setState((int) (tile.radius * 2));
		feather = new ScrollInput(xRight, yBottom, 28, 18)
				.titled(new StringTextComponent("Feather"))
				.withStepFunction(ctx -> step(ctx, 5))
				.calling(tile::setFeather)
				.withRange(0, 201)
				.setState((int) (tile.feather * 10));
		density = new ScrollInput(xLeft, yTop, 28, 18)
				.titled(new StringTextComponent("Density"))
				.withStepFunction(ctx -> step(ctx, 10))
				.calling(tile::setDensity)
				.withRange(0, 401)
				.setState((int) (tile.density * 100));
		fade = new ScrollInput(xLeft, yBottom, 28, 18)
				.titled(new StringTextComponent("Fade"))
				.withStepFunction(ctx -> step(ctx, 1))
				.calling(tile::setFade)
				.withRange(0, 51)
				.setState((int) (tile.fade * 10));

		Collections.addAll(widgets, radius, density, feather, fade);
	}

	public void initInputsOfRow(int row) {
		int x = guiLeft + 30;
		int y = guiTop + 18;
		int rowHeight = 22;

		Vector<ScrollInput> rowInputs = inputs.get(row);
		rowInputs.forEach(widgets::remove);
		rowInputs.clear();
		FilterStep filter = stages.get(row);

		ScrollInput type =
				new SelectionScrollInput(x, y + rowHeight * row, 86, 18)
						.forOptions(ColorEffect.getOptions())
						.calling(state -> instructionUpdated(row, state))
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

	@Override
	protected void renderWindow(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int hFontColor = 0xD3CBBE;
		background.draw(matrixStack, this, guiLeft, guiTop);

		for (int row = 0; row < stages.capacity(); row++) {
			AllGuiTextures toDraw = AllGuiTextures.PROJECTOR_EMPTY;
			int yOffset = toDraw.height * row;
			if (row >= stages.size()) {
				toDraw.draw(matrixStack, guiLeft, guiTop + 14 + yOffset);
				continue;
			}

			FilterStep step = stages.get(row);
			ColorEffect def = step.filter;
			def.background.draw(matrixStack, guiLeft, guiTop + 14 + yOffset);

			if (def != ColorEffect.END)
				label(matrixStack, 36, yOffset - 3, Lang.translate(def.translationKey));
			if (def.hasParameter) {
				String text = step.filter.formatValue(step.value);
				int stringWidth = textRenderer.getStringWidth(text);
				label(matrixStack, 118 + (12 - stringWidth / 2), yOffset - 3, new StringTextComponent(text));
			}
		}

		renderScroll(matrixStack, radius, 2f);
		renderScroll(matrixStack, density, 100f);
		renderScroll(matrixStack, feather, 10f);
		renderScroll(matrixStack, fade, 10f);

		textRenderer.drawWithShadow(matrixStack, title, guiLeft - 3 + (background.width - textRenderer.getWidth(title)) / 2, guiTop + 3,
				0xffffff);

		GuiGameElement.of(renderedItem)
				.at(guiLeft + background.width + 10, guiTop + 140, -150)
				.scale(5)
				.render(matrixStack);
	}

	private void renderScroll(MatrixStack matrixStack, ScrollInput input, float divisor) {

		String text = String.valueOf(input.getState() / divisor);

		int stringWidth = textRenderer.getStringWidth(text);
		textRenderer.drawWithShadow(matrixStack, text, input.x + (12 - stringWidth / 2), input.y + 5, 0xFFFFEE);
	}

	private void label(MatrixStack matrixStack, int x, int y, ITextComponent text) {
		textRenderer.drawWithShadow(matrixStack, text, guiLeft + x, guiTop + 26 + y, 0xFFFFEE);
	}

	public void sendPacket() {
		AllPackets.channel.sendToServer(new ConfigureProjectorPacket(tile));
	}

	@Override
	public void removed() {
		sendPacket();
	}

	private void instructionUpdated(int index, int state) {
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
				initInputsOfRow(index + 1);
			}
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (confirmButton.isHovered()) {
			Minecraft.getInstance().player.closeScreen();
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

}
