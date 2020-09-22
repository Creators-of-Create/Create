package com.simibubi.create.foundation.gui.widgets;

import java.util.LinkedList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

public abstract class AbstractSimiWidget extends Widget {

	protected List<ITextComponent> toolTip;
	
	public AbstractSimiWidget(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn, ITextComponent.of(""));
		toolTip = new LinkedList<>();
	}
	
	public List<ITextComponent> getToolTip() {
		return toolTip;
	}
	
	@Override
	public void renderButton(MatrixStack matrixStack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
	}

}
