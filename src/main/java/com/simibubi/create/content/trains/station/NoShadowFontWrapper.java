package com.simibubi.create.content.trains.station;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class NoShadowFontWrapper extends Font {

	private Font wrapped;

	public NoShadowFontWrapper(Font wrapped) {
		super(null);
		this.wrapped = wrapped;
	}

	public FontSet getFontSet(ResourceLocation pFontLocation) {
		return wrapped.getFontSet(pFontLocation);
	}

	public int drawShadow(PoseStack pPoseStack, String pText, float pX, float pY, int pColor) {
		return wrapped.draw(pPoseStack, pText, pX, pY, pColor);
	}

	public int drawShadow(PoseStack pPoseStack, String pText, float pX, float pY, int pColor, boolean pTransparent) {
		return wrapped.draw(pPoseStack, pText, pX, pY, pColor);
	}

	public int draw(PoseStack pPoseStack, String pText, float pX, float pY, int pColor) {
		return wrapped.draw(pPoseStack, pText, pX, pY, pColor);
	}

	public int drawShadow(PoseStack pPoseStack, FormattedCharSequence pText, float pX, float pY, int pColor) {
		return wrapped.draw(pPoseStack, pText, pX, pY, pColor);
	}

	public int drawShadow(PoseStack pPoseStack, Component pText, float pX, float pY, int pColor) {
		return wrapped.draw(pPoseStack, pText, pX, pY, pColor);
	}

	public int draw(PoseStack pPoseStack, FormattedCharSequence pText, float pX, float pY, int pColor) {
		return wrapped.draw(pPoseStack, pText, pX, pY, pColor);

	}

	public int draw(PoseStack pPoseStack, Component pText, float pX, float pY, int pColor) {
		return wrapped.draw(pPoseStack, pText, pX, pY, pColor);
	}

	public String bidirectionalShaping(String pText) {
		return wrapped.bidirectionalShaping(pText);
	}

	public int drawInBatch(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix,
		MultiBufferSource pBuffer, boolean pTransparent, int pBackgroundColor, int pPackedLight) {
		return wrapped.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pBackgroundColor,
			pPackedLight);
	}

	public int drawInBatch(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix,
		MultiBufferSource pBuffer, boolean pTransparent, int pBackgroundColor, int pPackedLight, boolean pBidiFlag) {
		return wrapped.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pBackgroundColor,
			pPackedLight, pBidiFlag);
	}

	public int drawInBatch(Component pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix,
		MultiBufferSource pBuffer, boolean pTransparent, int pBackgroundColor, int pPackedLight) {
		return wrapped.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pBackgroundColor,
			pPackedLight);
	}

	public int drawInBatch(FormattedCharSequence pText, float pX, float pY, int pColor, boolean pDropShadow,
		Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pTransparent, int pBackgroundColor, int pPackedLight) {
		return wrapped.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pBackgroundColor,
			pPackedLight);
	}

	public void drawInBatch8xOutline(FormattedCharSequence pText, float pX, float pY, int pColor, int pBackgroundColor,
		Matrix4f pMatrix, MultiBufferSource pBuffer, int pPackedLightCoords) {
		wrapped.drawInBatch8xOutline(pText, pX, pY, pColor, pBackgroundColor, pMatrix, pBuffer, pPackedLightCoords);
	}

	public int width(String pText) {
		return wrapped.width(pText);
	}

	public int width(FormattedText pText) {
		return wrapped.width(pText);
	}

	public int width(FormattedCharSequence pText) {
		return wrapped.width(pText);
	}

	public String plainSubstrByWidth(String p_92838_, int p_92839_, boolean p_92840_) {
		return wrapped.plainSubstrByWidth(p_92838_, p_92839_, p_92840_);
	}

	public String plainSubstrByWidth(String pText, int pMaxWidth) {
		return wrapped.plainSubstrByWidth(pText, pMaxWidth);
	}

	public FormattedText substrByWidth(FormattedText pText, int pMaxWidth) {
		return wrapped.substrByWidth(pText, pMaxWidth);
	}

	public void drawWordWrap(FormattedText pText, int pX, int pY, int pMaxWidth, int pColor) {
		wrapped.drawWordWrap(pText, pX, pY, pMaxWidth, pColor);
	}

	public int wordWrapHeight(String pStr, int pMaxWidth) {
		return wrapped.wordWrapHeight(pStr, pMaxWidth);
	}

	public List<FormattedCharSequence> split(FormattedText pText, int pMaxWidth) {
		return wrapped.split(pText, pMaxWidth);
	}

	public boolean isBidirectional() {
		return wrapped.isBidirectional();
	}

	public StringSplitter getSplitter() {
		return wrapped.getSplitter();
	}

}
