package com.simibubi.create.modules.curiosities.zapper;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockState;
import net.minecraft.block.FourWayBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;

public abstract class ZapperItemRenderer extends ItemStackTileEntityRenderer {

	protected void renderBlockUsed(ItemStack stack, ItemRenderer itemRenderer) {
		BlockState state = NBTUtil.readBlockState(stack.getTag().getCompound("BlockUsed"));

		GlStateManager.pushMatrix();
		GlStateManager.translatef(-0.3F, -0.45F, -0.0F);
		GlStateManager.scalef(0.25F, 0.25F, 0.25F);
		IBakedModel modelForState = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state);

		if (state.getBlock() instanceof FourWayBlock)
			modelForState = Minecraft.getInstance().getItemRenderer()
					.getModelWithOverrides(new ItemStack(state.getBlock()));

		itemRenderer.renderItem(new ItemStack(state.getBlock()), modelForState);
		GlStateManager.popMatrix();
	}
	
}
