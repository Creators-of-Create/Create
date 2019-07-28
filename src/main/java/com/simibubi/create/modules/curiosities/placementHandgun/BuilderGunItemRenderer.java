package com.simibubi.create.modules.curiosities.placementHandgun;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunItem.ComponentTier;
import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunItem.Components;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.animation.Animation;

public class BuilderGunItemRenderer extends ItemStackTileEntityRenderer {

	@Override
	public void renderByItem(ItemStack stack) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		BuilderGunModel mainModel = (BuilderGunModel) itemRenderer.getModelWithOverrides(stack);
		float worldTime = Animation.getWorldTime(Minecraft.getInstance().world,
				Minecraft.getInstance().getRenderPartialTicks());
		
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.5F, 0.5F, 0.5F);
		float lastCoordx = GLX.lastBrightnessX;
		float lastCoordy = GLX.lastBrightnessY;
		
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, Math.min(lastCoordx + 60, 240), Math.min(lastCoordy + 120, 240));
		
		itemRenderer.renderItem(stack, mainModel.getBakedModel());
		
		if (BuilderGunItem.getTier(Components.Body, stack) == ComponentTier.None)
			itemRenderer.renderItem(stack, mainModel.body);
		if (BuilderGunItem.getTier(Components.Body, stack) == ComponentTier.ChorusChrome)
			itemRenderer.renderItem(stack, mainModel.chorusBody);
		
		if (BuilderGunItem.getTier(Components.Scope, stack) == ComponentTier.BlazeBrass)
			itemRenderer.renderItem(stack, mainModel.goldScope);
		if (BuilderGunItem.getTier(Components.Scope, stack) == ComponentTier.ChorusChrome)
			itemRenderer.renderItem(stack, mainModel.chorusScope);
		
		if (BuilderGunItem.getTier(Components.Amplifier, stack) == ComponentTier.BlazeBrass)
			itemRenderer.renderItem(stack, mainModel.goldAmp);
		if (BuilderGunItem.getTier(Components.Amplifier, stack) == ComponentTier.ChorusChrome)
			itemRenderer.renderItem(stack, mainModel.chorusAmp);
		
		if (BuilderGunItem.getTier(Components.Retriever, stack) == ComponentTier.BlazeBrass)
			itemRenderer.renderItem(stack, mainModel.goldRetriever);
		if (BuilderGunItem.getTier(Components.Retriever, stack) == ComponentTier.ChorusChrome)
			itemRenderer.renderItem(stack, mainModel.chorusRetriever);
		
		if (BuilderGunItem.getTier(Components.Accelerator, stack) == ComponentTier.BlazeBrass)
			itemRenderer.renderItem(stack, mainModel.goldAcc);
		if (BuilderGunItem.getTier(Components.Accelerator, stack) == ComponentTier.ChorusChrome)
			itemRenderer.renderItem(stack, mainModel.chorusAcc);

		if (mainModel.showBlock && stack.hasTag() && stack.getTag().contains("BlockUsed")) {
			BlockState state = NBTUtil.readBlockState(stack.getTag().getCompound("BlockUsed"));

			GlStateManager.pushMatrix();
			GlStateManager.translatef(-0.8F, -0.7F, -0.5F);
			GlStateManager.scalef(0.25F, 0.25F, 0.25F);
			itemRenderer.renderItem(new ItemStack(state.getBlock()),
					Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state));
			GlStateManager.popMatrix();
		}

		GlStateManager.disableLighting();
		
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, MathHelper.sin(worldTime * 5) * 120 + 120, 120);
		if (BuilderGunItem.getTier(Components.Accelerator, stack) == ComponentTier.BlazeBrass)
			itemRenderer.renderItem(stack, mainModel.goldAccCore);
		if (BuilderGunItem.getTier(Components.Accelerator, stack) == ComponentTier.ChorusChrome)
			itemRenderer.renderItem(stack, mainModel.chorusAccCore);
		
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240, 120);
		if (BuilderGunItem.getTier(Components.Body, stack) == ComponentTier.BlazeBrass)
			itemRenderer.renderItem(stack, mainModel.goldBody);		
		
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240, 240);
		if (BuilderGunItem.getTier(Components.Amplifier, stack) == ComponentTier.BlazeBrass)
			itemRenderer.renderItem(stack, mainModel.goldAmpCore);
		if (BuilderGunItem.getTier(Components.Amplifier, stack) == ComponentTier.ChorusChrome)
			itemRenderer.renderItem(stack, mainModel.chorusAmpCore);

		float angle = worldTime * -50;
		angle %= 360;

		float offset = -.19f;
		GlStateManager.translatef(0, offset, 0);
		GlStateManager.rotatef(angle, 0, 0, 1);
		GlStateManager.translatef(0, -offset, 0);
		itemRenderer.renderItem(stack, mainModel.rod);

		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, lastCoordx, lastCoordy);
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
	}

}
