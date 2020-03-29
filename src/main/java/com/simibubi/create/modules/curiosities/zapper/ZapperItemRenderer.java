package com.simibubi.create.modules.curiosities.zapper;

import com.mojang.blaze3d.systems.RenderSystem;

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

		RenderSystem.pushMatrix();
		RenderSystem.translatef(-0.3F, -0.45F, -0.0F);
		RenderSystem.scalef(0.25F, 0.25F, 0.25F);
		IBakedModel modelForState = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state);

		if (state.getBlock() instanceof FourWayBlock)
			modelForState = Minecraft.getInstance().getItemRenderer()
					.getItemModelWithOverrides(new ItemStack(state.getBlock()), Minecraft.getInstance().world, null);

		itemRenderer.renderItem(new ItemStack(state.getBlock()), modelForState);
		RenderSystem.popMatrix();
	}
	
}
