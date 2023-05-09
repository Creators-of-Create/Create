package com.simibubi.create.foundation.tileEntity.behaviour;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.outliner.ChasingAABBOutline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ValueBox extends ChasingAABBOutline {

	protected Component label;
	protected Component sublabel = Components.immutableEmpty();
	protected Component scrollTooltip = Components.immutableEmpty();
	protected Vec3 labelOffset = Vec3.ZERO;

	protected int passiveColor;
	protected int highlightColor;
	public boolean isPassive;

	protected BlockPos pos;
	protected ValueBoxTransform transform;
	protected BlockState blockState;

	public ValueBox(Component label, AABB bb, BlockPos pos) {
		super(bb);
		this.label = label;
		this.pos = pos;
		this.blockState = Minecraft.getInstance().level.getBlockState(pos);
	}

	public ValueBox transform(ValueBoxTransform transform) {
		this.transform = transform;
		return this;
	}

	public ValueBox offsetLabel(Vec3 offset) {
		this.labelOffset = offset;
		return this;
	}

	public ValueBox subLabel(Component sublabel) {
		this.sublabel = sublabel;
		return this;
	}

	public ValueBox scrollTooltip(Component scrollTip) {
		this.scrollTooltip = scrollTip;
		return this;
	}

	public ValueBox withColors(int passive, int highlight) {
		this.passiveColor = passive;
		this.highlightColor = highlight;
		return this;
	}

	public ValueBox passive(boolean passive) {
		this.isPassive = passive;
		return this;
	}

	@Override
	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, float pt) {
		boolean hasTransform = transform != null;
		if (transform instanceof Sided && params.getHighlightedFace() != null)
			((Sided) transform).fromSide(params.getHighlightedFace());
		if (hasTransform && !transform.shouldRender(blockState))
			return;

		ms.pushPose();
		ms.translate(pos.getX(), pos.getY(), pos.getZ());
		if (hasTransform)
			transform.transform(blockState, ms);
		params.colored(isPassive ? passiveColor : highlightColor);
		super.render(ms, buffer, pt);

		float fontScale = hasTransform ? -transform.getFontScale() : -1 / 64f;
		ms.scale(fontScale, fontScale, fontScale);

		ms.pushPose();
		renderContents(ms, buffer);
		ms.popPose();

		if (!isPassive) {
			ms.pushPose();
			ms.translate(17.5, -.5, 7);
			ms.translate(labelOffset.x, labelOffset.y, labelOffset.z);

			renderHoveringText(ms, buffer, label);
			if (!sublabel.getString().isEmpty()) {
				ms.translate(0, 10, 0);
				renderHoveringText(ms, buffer, sublabel);
			}
			if (!scrollTooltip.getString().isEmpty()) {
				ms.translate(0, 10, 0);
				renderHoveringText(ms, buffer, scrollTooltip, 0x998899, 0x111111);
			}

			ms.popPose();
		}

		ms.popPose();
	}

	public void renderContents(PoseStack ms, MultiBufferSource buffer) {}

	public static class ItemValueBox extends ValueBox {
		ItemStack stack;
		int count;

		public ItemValueBox(Component label, AABB bb, BlockPos pos, ItemStack stack, int count) {
			super(label, bb, pos);
			this.stack = stack;
			this.count = count;
		}

		@Override
		public void renderContents(PoseStack ms, MultiBufferSource buffer) {
			super.renderContents(ms, buffer);
			Font font = Minecraft.getInstance().font;
			Component countString = Components.literal(count == 0 ? "*" : count + "");
			ms.translate(17.5f, -5f, 7f);

			boolean isFilter = stack.getItem() instanceof FilterItem;
			boolean isEmpty = stack.isEmpty();
			float scale = 1.5f;
			ms.translate(-font.width(countString), 0, 0);

			if (isFilter)
				ms.translate(3, 8, 7.25f);
			else if (isEmpty) {
				ms.translate(-17, -2, 3f);
				scale = 2f;
			}
			else
				ms.translate(-7, 10, 10 + 1 / 4f);

			ms.scale(scale, scale, scale);
			drawString(ms, buffer, countString, 0, 0, isFilter ? 0xFFFFFF : 0xEDEDED);
			ms.translate(0, 0, -1 / 16f);
			drawString(ms, buffer, countString, 1 - 1 / 8f, 1 - 1 / 8f, 0x4F4F4F);
		}

	}

	public static class TextValueBox extends ValueBox {
		Component text;

		public TextValueBox(Component label, AABB bb, BlockPos pos, Component text) {
			super(label, bb, pos);
			this.text = text;
		}

		@Override
		public void renderContents(PoseStack ms, MultiBufferSource buffer) {
			super.renderContents(ms, buffer);
			Font font = Minecraft.getInstance().font;
			float scale = 4;
			ms.scale(scale, scale, 1);
			ms.translate(-4, -4, 5);

			int stringWidth = font.width(text);
			float numberScale = (float) font.lineHeight / stringWidth;
			boolean singleDigit = stringWidth < 10;
			if (singleDigit)
				numberScale = numberScale / 2;
			float verticalMargin = (stringWidth - font.lineHeight) / 2f;

			ms.scale(numberScale, numberScale, numberScale);
			ms.translate(singleDigit ? stringWidth / 2 : 0, singleDigit ? -verticalMargin : verticalMargin, 0);

			renderHoveringText(ms, buffer, text, 0xEDEDED, 0x4f4f4f);
		}

	}

	public static class IconValueBox extends ValueBox {
		AllIcons icon;

		public IconValueBox(Component label, INamedIconOptions iconValue, AABB bb, BlockPos pos) {
			super(label, bb, pos);
			subLabel(Lang.translateDirect(iconValue.getTranslationKey()));
			icon = iconValue.getIcon();
		}

		@Override
		public void renderContents(PoseStack ms, MultiBufferSource buffer) {
			super.renderContents(ms, buffer);
			float scale = 4 * 16;
			ms.scale(scale, scale, scale);
			ms.translate(-.5f, -.5f, 1 / 32f);
			icon.render(ms, buffer, 0xFFFFFF);
		}

	}

	// util

	protected void renderHoveringText(PoseStack ms, MultiBufferSource buffer, Component text) {
		renderHoveringText(ms, buffer, text, highlightColor, Color.mixColors(passiveColor, 0, 0.75f));
	}

	protected void renderHoveringText(PoseStack ms, MultiBufferSource buffer, Component text, int color,
		int shadowColor) {
		ms.pushPose();
		drawString(ms, buffer, text, 0, 0, color);
		ms.translate(0, 0, -.25);
		drawString(ms, buffer, text, 1, 1, shadowColor);
		ms.popPose();
	}

	private static void drawString(PoseStack ms, MultiBufferSource buffer, Component text, float x, float y, int color) {
		Minecraft.getInstance().font.drawInBatch(text, x, y, color, false, ms.last()
			.pose(), buffer, false, 0, LightTexture.FULL_BRIGHT);
	}

}
