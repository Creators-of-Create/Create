package com.simibubi.create.foundation.behaviour;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.logistics.item.filter.FilterItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class ValueBox {

	String label = "Value Box";
	String sublabel = "";
	String scrollTooltip = "";
	Vec3d labelOffset = Vec3d.ZERO;
	int passiveColor;
	int highlightColor;
	AxisAlignedBB bb = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

	public ValueBox(String label, AxisAlignedBB bb) {
		this.label = label;
		this.bb = bb;
	}

	public ValueBox offsetLabel(Vec3d offset) {
		this.labelOffset = offset;
		return this;
	}

	public ValueBox subLabel(String sublabel) {
		this.sublabel = sublabel;
		return this;
	}

	public ValueBox scrollTooltip(String scrollTip) {
		this.scrollTooltip = scrollTip;
		return this;
	}

	public ValueBox withColors(int passive, int highlight) {
		this.passiveColor = passive;
		this.highlightColor = highlight;
		return this;
	}

	public void render(boolean highlighted) {

	}

	public static class ItemValueBox extends ValueBox {
		ItemStack stack;
		int count;

		public ItemValueBox(String label, AxisAlignedBB bb, ItemStack stack, int count) {
			super(label, bb);
			this.stack = stack;
			this.count = count;
		}

		@Override
		public void render(boolean highlighted) {
			super.render(highlighted);
			FontRenderer font = Minecraft.getInstance().fontRenderer;
			String countString = count == 0 ? "*" : count + "";
			RenderSystem.translated(17.5f, -5f, 7f);

			boolean isFilter = stack.getItem() instanceof FilterItem;
			if (isFilter)
				RenderSystem.translated(3, 8, 7.25f);
			else
				RenderSystem.translated(-7 - font.getStringWidth(countString), 10, 10 + 1 / 4f);

			double scale = 1.5;
			RenderSystem.rotatef(0, 1, 0, 0);
			RenderSystem.scaled(scale, scale, scale);
			font.drawString(countString, 0, 0, isFilter ? 0xFFFFFF : 0xEDEDED);
			RenderSystem.translated(0, 0, -1 / 16f);
			font.drawString(countString, 1 - 1 / 8f, 1 - 1 / 8f, 0x4F4F4F);
		}

	}

	public static class TextValueBox extends ValueBox {
		String text;

		public TextValueBox(String label, AxisAlignedBB bb, String text) {
			super(label, bb);
			this.text = text;
		}

		@Override
		public void render(boolean highlighted) {
			super.render(highlighted);
			FontRenderer font = Minecraft.getInstance().fontRenderer;
			double scale = 4;
			RenderSystem.scaled(scale, scale, 1);
			RenderSystem.translated(-4, -4, 5);

			int stringWidth = font.getStringWidth(text);
			float numberScale = (float) font.FONT_HEIGHT / stringWidth;
			boolean singleDigit = stringWidth < 10;
			if (singleDigit)
				numberScale = numberScale / 2;
			float verticalMargin = (stringWidth - font.FONT_HEIGHT) / 2f;

			RenderSystem.scaled(numberScale, numberScale, numberScale);
			RenderSystem.translated(singleDigit ? stringWidth / 2 : 0, singleDigit ? -verticalMargin : verticalMargin,
					0);

			ValueBoxRenderer.renderText(font, text, 0xEDEDED, 0x4f4f4f);
		}

	}

	public static class IconValueBox extends ValueBox {
		ScreenResources icon;

		public IconValueBox(String label, INamedIconOptions iconValue, AxisAlignedBB bb) {
			super(label, bb);
			subLabel(Lang.translate(iconValue.getTranslationKey()));
			icon = iconValue.getIcon();
		}
		
		@Override
		public void render(boolean highlighted) {
			super.render(highlighted);
			double scale = 4;
			RenderSystem.scaled(scale, scale, 1);
			RenderSystem.translated(-8, -8, 3/2f);
			icon.draw(0, 0);
			RenderSystem.color4f(.25f, .25f, .25f, 1);
			RenderSystem.translated(.5f, .5f, -1);
			icon.draw(0, 0);
			RenderSystem.color4f(1, 1, 1, 1);
		}

	}

}
