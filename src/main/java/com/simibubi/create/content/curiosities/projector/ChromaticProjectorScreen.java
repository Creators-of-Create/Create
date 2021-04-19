package com.simibubi.create.content.curiosities.projector;

import java.util.ArrayList;
import java.util.Collections;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ChromaticProjectorScreen extends AbstractSimiContainerScreen<ChromaticProjectorContainer> {

	private ScrollInput filter;
	private ScrollInput radius;
	private ScrollInput feather;
	private ScrollInput fade;

	public ChromaticProjectorScreen(ChromaticProjectorContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
	}

	@Override
	protected void init() {
		super.init();

		widgets.clear();

		int x = guiLeft + 11;
		int y = guiTop + 20;

		ArrayList<ITextComponent> filterOptions = new ArrayList<>();

		filterOptions.add(new StringTextComponent("Test"));
		filterOptions.add(new StringTextComponent("Test1"));

		filter = new SelectionScrollInput(x, y, 77, 18)
				.forOptions(filterOptions);
		y += 20;
		radius = new ScrollInput(x, y, 30, 20);
		y += 20;
		feather = new ScrollInput(x, y, 30, 20);
		y += 20;
		fade = new ScrollInput(x, y, 30, 20);
		y += 20;
		Collections.addAll(widgets, filter, radius, feather, fade);

	}

	@Override
	protected void renderWindow(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		//AllGuiTextures.PLAYER_INVENTORY.draw(matrixStack, this, guiLeft - 10, guiTop + 145);
		AllGuiTextures.PROJECTOR.draw(matrixStack, this, guiLeft, guiTop);
//		BG_TOP.draw(matrixStack, this, guiLeft + 20, guiTop);
//		BG_BOTTOM.draw(matrixStack, this, guiLeft + 20, guiTop + BG_TOP.height);

	}
}
