package com.simibubi.create.foundation.map;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.client.gui.map.IMapDecorationRenderer;

public class StationMapDecorationRenderer implements IMapDecorationRenderer {
	@Override
	public boolean render(MapDecoration decoration, PoseStack poseStack, MultiBufferSource bufferSource, @NotNull MapItemSavedData mapData, MapDecorationTextureManager decorationTextures, boolean inItemFrame, int packedLight, int index) {
		poseStack.pushPose();

		poseStack.translate(decoration.x() / 2D + 64.0, decoration.y() / 2D + 64.0, -0.02D);

		poseStack.pushPose();

		poseStack.translate(0.5f, 0f, 0);
		poseStack.scale(4.5F, 4.5F, 3.0F);

		TextureAtlasSprite sprite = decorationTextures.get(decoration);
		float U0 = sprite.getU0();
		float V0 = sprite.getV0();
		float U1 = sprite.getU1();
		float V1 = sprite.getV1();
		VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(sprite.atlasLocation()));
		Matrix4f mat = poseStack.last().pose();
		float zOffset = -0.001f;
		buffer.addVertex(mat, -1.0F, 1.0F, index * zOffset).setColor(-1).setUv(U0, V0).setLight(packedLight);
		buffer.addVertex(mat, 1.0F, 1.0F, index * zOffset).setColor(-1).setUv(U1, V0).setLight(packedLight);
		buffer.addVertex(mat, 1.0F, -1.0F, index * zOffset).setColor(-1).setUv(U1, V1).setLight(packedLight);
		buffer.addVertex(mat, -1.0F, -1.0F, index * zOffset).setColor(-1).setUv(U0, V1).setLight(packedLight);

		poseStack.popPose();

		if (decoration.name().isPresent()) {
			Font font = Minecraft.getInstance().font;
			Component component = decoration.name().get();
			float f6 = (float)font.width(component);
//			float f7 = Mth.clamp(25.0F / f6, 0.0F, 6.0F / 9.0F);
			poseStack.pushPose();
//			poseStack.translate((double)(0.0F + (float)getX() / 2.0F + 64.0F / 2.0F), (double)(0.0F + (float)getY() / 2.0F + 64.0F + 4.0F), (double)-0.025F);
			poseStack.translate(0, 6.0D, -0.005F);

			poseStack.scale(0.8f, 0.8f, 1.0F);
			poseStack.translate(-f6 / 2f + .5f, 0, 0);
//			poseStack.scale(f7, f7, 1.0F);
			font.drawInBatch(component, 0.0F, 0.0F, -1, false, poseStack.last()
					.pose(), bufferSource, Font.DisplayMode.NORMAL, Integer.MIN_VALUE, packedLight);
			poseStack.popPose();
		}

		poseStack.popPose();

		return true;
	}
}
