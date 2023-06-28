package com.simibubi.create.foundation.blockEntity.behaviour;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.outliner.ChasingAABBOutline;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
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

	public int overrideColor = -1;
	
	public boolean isPassive;

	protected BlockPos pos;
	protected ValueBoxTransform transform;
	protected BlockState blockState;

	protected AllIcons outline = AllIcons.VALUE_BOX_HOVER_4PX;

	public ValueBox(Component label, AABB bb, BlockPos pos) {
		this(label, bb, pos, Minecraft.getInstance().level.getBlockState(pos));
	}

	public ValueBox(Component label, AABB bb, BlockPos pos, BlockState state) {
		super(bb);
		this.label = label;
		this.pos = pos;
		this.blockState = state;
	}

	public ValueBox transform(ValueBoxTransform transform) {
		this.transform = transform;
		return this;
	}

	public ValueBox wideOutline() {
		this.outline = AllIcons.VALUE_BOX_HOVER_6PX;
		return this;
	}

	public ValueBox passive(boolean passive) {
		this.isPassive = passive;
		return this;
	}
	
	public ValueBox withColor(int color) {
		this.overrideColor = color;
		return this;
	}

	@Override
	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt) {
		boolean hasTransform = transform != null;
		if (transform instanceof Sided && params.getHighlightedFace() != null)
			((Sided) transform).fromSide(params.getHighlightedFace());
		if (hasTransform && !transform.shouldRender(blockState))
			return;

		ms.pushPose();
		ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
		if (hasTransform)
			transform.transform(blockState, ms);

		if (!isPassive) {
			ms.pushPose();
			ms.scale(-2.01f, -2.01f, 2.01f);
			ms.translate(-8 / 16.0, -8 / 16.0, -.5 / 16.0);
			getOutline().render(ms, buffer, 0xffffff);
			ms.popPose();
		}

		float fontScale = hasTransform ? -transform.getFontScale() : -1 / 64f;
		ms.scale(fontScale, fontScale, fontScale);
		renderContents(ms, buffer);
		
		ms.popPose();
	}

	public AllIcons getOutline() {
		return outline;
	}

	public void renderContents(PoseStack ms, MultiBufferSource buffer) {}

	public static class ItemValueBox extends ValueBox {
		ItemStack stack;
		int count;
		boolean upTo;

		public ItemValueBox(Component label, AABB bb, BlockPos pos, ItemStack stack, int count, boolean upTo) {
			super(label, bb, pos);
			this.stack = stack;
			this.count = count;
			this.upTo = upTo;
		}

		@Override
		public AllIcons getOutline() {
			if (!stack.isEmpty())
				return AllIcons.VALUE_BOX_HOVER_6PX;
			return super.getOutline();
		}

		@Override
		public void renderContents(PoseStack ms, MultiBufferSource buffer) {
			super.renderContents(ms, buffer);
			if (count == -1)
				return;

			Font font = Minecraft.getInstance().font;
			boolean wildcard = count == 0 || upTo && count == stack.getMaxStackSize();
			Component countString = Components.literal(wildcard ? "*" : count + "");
			ms.translate(17.5f, -5f, 7f);

			boolean isFilter = stack.getItem() instanceof FilterItem;
			boolean isEmpty = stack.isEmpty();

			ItemRenderer itemRenderer = Minecraft.getInstance()
				.getItemRenderer();
			BakedModel modelWithOverrides = itemRenderer.getModel(stack, null, null, 0);
			boolean blockItem = modelWithOverrides.isGui3d() && modelWithOverrides.getRenderPasses(stack, false)
				.size() <= 1;

			float scale = 1.5f;
			ms.translate(-font.width(countString), 0, 0);

			if (isFilter)
				ms.translate(-5, 8, 7.25f);
			else if (isEmpty) {
				ms.translate(-15, -1f, -2.75f);
				scale = 1.65f;
			} else
				ms.translate(-7, 10, blockItem ? 10 + 1 / 4f : 0);

			if (wildcard)
				ms.translate(-1, 3f, 0);

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

		public TextValueBox(Component label, AABB bb, BlockPos pos, BlockState state, Component text) {
			super(label, bb, pos, state);
			this.text = text;
		}

		@Override
		public void renderContents(PoseStack ms, MultiBufferSource buffer) {
			super.renderContents(ms, buffer);
			Font font = Minecraft.getInstance().font;
			float scale = 3;
			ms.scale(scale, scale, 1);
			ms.translate(-4, -3.75, 5);

			int stringWidth = font.width(text);
			float numberScale = (float) font.lineHeight / stringWidth;
			boolean singleDigit = stringWidth < 10;
			if (singleDigit)
				numberScale = numberScale / 2;
			float verticalMargin = (stringWidth - font.lineHeight) / 2f;

			ms.scale(numberScale, numberScale, numberScale);
			ms.translate(singleDigit ? stringWidth / 2 : 0, singleDigit ? -verticalMargin : verticalMargin, 0);

			int overrideColor = transform.getOverrideColor();
			renderHoveringText(ms, buffer, text, overrideColor != -1 ? overrideColor : 0xEDEDED);
		}

	}

	public static class IconValueBox extends ValueBox {
		AllIcons icon;

		public IconValueBox(Component label, INamedIconOptions iconValue, AABB bb, BlockPos pos) {
			super(label, bb, pos);
			icon = iconValue.getIcon();
		}

		@Override
		public void renderContents(PoseStack ms, MultiBufferSource buffer) {
			super.renderContents(ms, buffer);
			float scale = 2 * 16;
			ms.scale(scale, scale, scale);
			ms.translate(-.5f, -.5f, 5 / 32f);
			
			int overrideColor = transform.getOverrideColor();
			icon.render(ms, buffer, overrideColor != -1 ? overrideColor : 0xFFFFFF);
		}

	}

	protected void renderHoveringText(PoseStack ms, MultiBufferSource buffer, Component text, int color) {
		ms.pushPose();
		drawString(ms, buffer, text, 0, 0, color);
		ms.popPose();
	}

	private static void drawString(PoseStack ms, MultiBufferSource buffer, Component text, float x, float y,
		int color) {
		Minecraft.getInstance().font.drawInBatch(text, x, y, color, false, ms.last()
			.pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
	}

}
