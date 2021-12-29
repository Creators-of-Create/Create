package com.simibubi.create.compat.rei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.lib.util.FluidTextUtil;

import com.simibubi.create.lib.util.FluidUnit;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.AbstractEntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.EntryStack;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;

public class FluidStackEntryRenderer extends AbstractEntryRenderer<FluidStack> {
	private static final int TEX_WIDTH = 16;
	private static final int TEX_HEIGHT = 16;
	private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible

	@Override
	public void render(EntryStack<FluidStack> entry, PoseStack poseStack, Rectangle bounds, int mouseX, int mouseY, float delta) {
		RenderSystem.enableBlend();

		drawFluid(poseStack, bounds.getX(), bounds.getY(), entry.getValue());

		RenderSystem.setShaderColor(1, 1, 1, 1);

//		if (overlay != null) {
//			poseStack.pushPose();
//			poseStack.translate(0, 0, 200);
//			overlay.draw(poseStack, bounds.getX(), bounds.getY());
//			poseStack.popPose();
//		}

		RenderSystem.disableBlend();
	}

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public @Nullable Tooltip getTooltip(EntryStack<FluidStack> entry, Point mouse) {
		FluidVariant variant = FluidVariant.of(entry.getValue().getFluid(), entry.getValue().getOrCreateTag());
		List<Component> tooltip = FluidVariantRendering.getTooltip(variant);
		FluidStack fluid = entry.getValue();

		if (AllFluids.POTION.is(fluid.getFluid())) {
			Component name = FluidVariantRendering.getName(FluidVariant.of(fluid.getFluid()));
			if (tooltip.isEmpty())
				tooltip.add(0, name);
			else
				tooltip.set(0, name);

			ArrayList<Component> potionTooltip = new ArrayList<>();
			PotionFluidHandler.addPotionTooltip(new com.simibubi.create.lib.transfer.fluid.FluidStack(fluid.getFluid(), fluid.getAmount(), fluid.getTag()), potionTooltip, 1);
			tooltip.addAll(1, new ArrayList<>(potionTooltip));
		}

		String amount = FluidTextUtil.getUnicodeMillibuckets(fluid.getAmount());
		FluidUnit unit = AllConfigs.CLIENT.fluidUnitType.get();
		Component text = Lang.translate(unit.getTranslationKey(), amount).withStyle(ChatFormatting.GOLD);
		if (tooltip.isEmpty())
			tooltip.add(0, text);
		else {
			List<Component> siblings = tooltip.get(0)
					.getSiblings();
			siblings.add(new TextComponent(" "));
			siblings.add(text);
		}
//		tooltip.add(FluidVariantRendering.getName(FluidVariant.of(entry.getValue().getFluid())));
//		tooltip.add(new TextComponent(FluidTextUtil.getUnicodeMillibuckets(entry.getValue().getAmount())));
		return Tooltip.create(tooltip);
	}

	private void drawFluid(PoseStack poseStack, final int xPosition, final int yPosition, @Nullable FluidStack fluidStack) {
		if (fluidStack == null) {
			return;
		}
		Fluid fluid = fluidStack.getFluid();
		if (fluid == null) {
			return;
		}

		FluidVariant fluidVariant = FluidVariant.of(fluidStack.getFluid());
		TextureAtlasSprite fluidStillSprite = FluidVariantRendering.getSprite(fluidVariant);

		int fluidColor = FluidVariantRendering.getColor(fluidVariant);

		long amount = fluidStack.getAmount();
		long scaledAmount = (amount * TEX_HEIGHT) / FluidConstants.BUCKET;
		if (amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
			scaledAmount = MIN_FLUID_HEIGHT;
		}
		if (scaledAmount > TEX_HEIGHT) {
			scaledAmount = TEX_HEIGHT;
		}

		drawTiledSprite(poseStack, xPosition, yPosition, TEX_WIDTH, TEX_HEIGHT, fluidColor, scaledAmount, fluidStillSprite);
	}

	private static void drawTiledSprite(PoseStack poseStack, final int xPosition, final int yPosition, final int tiledWidth, final int tiledHeight, int color, long scaledAmount, TextureAtlasSprite sprite) {
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		Matrix4f matrix = poseStack.last().pose();
		setGLColorFromInt(color);

		final int xTileCount = tiledWidth / TEX_WIDTH;
		final int xRemainder = tiledWidth - (xTileCount * TEX_WIDTH);
		final long yTileCount = scaledAmount / TEX_HEIGHT;
		final long yRemainder = scaledAmount - (yTileCount * TEX_HEIGHT);

		final int yStart = yPosition + tiledHeight;

		for (int xTile = 0; xTile <= xTileCount; xTile++) {
			for (int yTile = 0; yTile <= yTileCount; yTile++) {
				int width = (xTile == xTileCount) ? xRemainder : TEX_WIDTH;
				long height = (yTile == yTileCount) ? yRemainder : TEX_HEIGHT;
				int x = xPosition + (xTile * TEX_WIDTH);
				int y = yStart - ((yTile + 1) * TEX_HEIGHT);
				if (width > 0 && height > 0) {
					long maskTop = TEX_HEIGHT - height;
					int maskRight = TEX_WIDTH - width;

					drawTextureWithMasking(matrix, x, y, sprite, maskTop, maskRight, 100);
				}
			}
		}
	}

	private static void setGLColorFromInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;
		float alpha = ((color >> 24) & 0xFF) / 255F;

		RenderSystem.setShaderColor(red, green, blue, alpha);
	}

	private static void drawTextureWithMasking(Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, long maskTop, int maskRight, float zLevel) {
		float uMin = textureSprite.getU0();
		float uMax = textureSprite.getU1();
		float vMin = textureSprite.getV0();
		float vMax = textureSprite.getV1();
		uMax = uMax - (maskRight / 16F * (uMax - uMin));
		vMax = vMax - (maskTop / 16F * (vMax - vMin));

		RenderSystem.setShader(GameRenderer::getPositionTexShader);

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(matrix, xCoord, yCoord + 16, zLevel).uv(uMin, vMax).endVertex();
		bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + 16, zLevel).uv(uMax, vMax).endVertex();
		bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).uv(uMax, vMin).endVertex();
		bufferBuilder.vertex(matrix, xCoord, yCoord + maskTop, zLevel).uv(uMin, vMin).endVertex();
		tessellator.end();
	}
}
