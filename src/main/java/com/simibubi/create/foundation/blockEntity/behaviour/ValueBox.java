package com.simibubi.create.foundation.blockEntity.behaviour;

import java.lang.ref.WeakReference;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;

import net.createmod.catnip.outliner.ChasingAABBOutline;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ValueBox extends ChasingAABBOutline {
	protected Component label;

	public int overrideColor = -1;
	public boolean isPassive;

	protected ValueBoxTransform transform;

	protected WeakReference<LevelAccessor> level;
	protected BlockPos pos;
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
		this.level = new WeakReference<>(Minecraft.getInstance().level);
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

		LevelAccessor levelAccessor = level.get();
		if (hasTransform && !transform.shouldRender(levelAccessor, pos, blockState))
			return;

		ms.pushPose();
		ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
		if (hasTransform)
			transform.transform(levelAccessor, pos, blockState, ms);

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

	public void renderContents(PoseStack ms, MultiBufferSource buffer) {
	}

	public static class ItemValueBox extends ValueBox {
		ItemStack stack;
		MutableComponent count;

		public ItemValueBox(Component label, AABB bb, BlockPos pos, ItemStack stack, MutableComponent count) {
			super(label, bb, pos);
			this.stack = stack;
			this.count = count;
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
			if (count == null)
				return;

			Font font = Minecraft.getInstance().font;
			ms.translate(17.5, -5, 7);

			boolean isFilter = stack.getItem() instanceof FilterItem;
			boolean isEmpty = stack.isEmpty();

			ItemRenderer itemRenderer = Minecraft.getInstance()
				.getItemRenderer();
			BakedModel modelWithOverrides = itemRenderer.getModel(stack, null, null, 0);
			boolean blockItem = modelWithOverrides.isGui3d();

			float scale = 1.5f;
			ms.translate(-font.width(count), 0, 0);

			if (isFilter) {
				ms.translate(-5, 8, 0);
			} else if (isEmpty) {
				ms.translate(-15, -1, -2.75);
				scale = 1.65f;
			} else {
				ms.translate(-7, 10, blockItem ? 10 + 1 / 4f : 0);
			}

			if (count.getString().equals("*"))
				ms.translate(-1, 3, 0);

			ms.scale(scale, scale, scale);
			drawString8x(ms, buffer, count, 0, 0, isFilter ? 0xFFFFFF : 0xEDEDED);
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
			if (overrideColor == -1)
				drawString8x(ms, buffer, text, 0, 0, 0xEDEDED);
			else
				drawString(ms, buffer, text, 0, 0, overrideColor);
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

	private static void drawString(PoseStack ms, MultiBufferSource buffer, Component text, float x, float y,
								   int color) {
		Minecraft.getInstance().font.drawInBatch(text, x, y, color, false, ms.last()
			.pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
	}

	private static void drawString8x(PoseStack ms, MultiBufferSource buffer, Component text, float x, float y,
									 int color) {
		Minecraft.getInstance().font.drawInBatch8xOutline(text.getVisualOrderText(), x, y, color, 0xff333333, ms.last()
			.pose(), buffer, LightTexture.FULL_BRIGHT);
	}

}
