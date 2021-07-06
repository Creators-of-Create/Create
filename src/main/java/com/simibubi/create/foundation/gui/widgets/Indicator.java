package com.simibubi.create.foundation.gui.widgets;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.minecraft.util.text.ITextComponent;

public class Indicator extends AbstractSimiWidget {
	
	public enum State {
		OFF, ON,
		RED, YELLOW, GREEN;
	}
	
	public State state;
	
	public Indicator(int x, int y, ITextComponent tooltip) {
		super(x, y, AllGuiTextures.INDICATOR.width, AllGuiTextures.INDICATOR.height);
		this.toolTip = ImmutableList.of(tooltip);
		this.state = State.OFF;
	}
	
	@Override
	public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks ) {
		AllGuiTextures toDraw;
		switch(state) {
			case ON: toDraw = AllGuiTextures.INDICATOR_WHITE; break;
			case OFF: toDraw = AllGuiTextures.INDICATOR; break;
			case RED: toDraw = AllGuiTextures.INDICATOR_RED; break;
			case YELLOW: toDraw = AllGuiTextures.INDICATOR_YELLOW; break;
			case GREEN: toDraw = AllGuiTextures.INDICATOR_GREEN; break;
			default: toDraw = AllGuiTextures.INDICATOR; break;
		}
		toDraw.draw(matrixStack, this, x, y);
	}
	
}
