package com.simibubi.create.modules.logistics.block;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.logistics.IHaveWireless;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public interface IBlockWithFrequency {

	public Pair<Vec3d, Vec3d> getFrequencyItemPositions(BlockState state);
	public Direction getFrequencyItemFacing(BlockState state);

	public default float getItemHitboxScale() {
		return 2 / 16f;
	}

	public default boolean handleActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof IHaveWireless))
			return false;

		IHaveWireless actor = (IHaveWireless) te;
		Pair<Vec3d, Vec3d> positions = getFrequencyItemPositions(state);
		ItemStack stack = player.getHeldItem(handIn);
		Vec3d vec = new Vec3d(pos);
		Vec3d first = positions.getLeft().add(vec);
		Vec3d second = positions.getRight().add(vec);
		float scale = getItemHitboxScale();

		if (new AxisAlignedBB(first, first).grow(scale).contains(hit.getHitVec())) {
			if (worldIn.isRemote)
				return true;
			actor.setFrequency(true, stack);
			return true;
		}

		if (new AxisAlignedBB(second, second).grow(scale).contains(hit.getHitVec())) {
			if (worldIn.isRemote)
				return true;
			actor.setFrequency(false, stack);
			return true;
		}

		return false;
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
		if (event.getTarget() == null || !(event.getTarget() instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) event.getTarget();
		ClientWorld world = Minecraft.getInstance().world;
		BlockPos pos = result.getPos();
		BlockState state = world.getBlockState(pos);

		if (!(state.getBlock() instanceof IBlockWithFrequency))
			return;

		IBlockWithFrequency freqBlock = (IBlockWithFrequency) state.getBlock();
		Pair<Vec3d, Vec3d> positions = freqBlock.getFrequencyItemPositions(state);
		Vec3d vec = new Vec3d(pos);
		Vec3d first = positions.getLeft().add(vec);
		Vec3d second = positions.getRight().add(vec);
		float scale = freqBlock.getItemHitboxScale();

		AxisAlignedBB firstBB = new AxisAlignedBB(first, first).grow(scale);
		AxisAlignedBB secondBB = new AxisAlignedBB(second, second).grow(scale);

		TessellatorHelper.prepareForDrawing();
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture();
		GlStateManager.depthMask(false);
		GlStateManager.matrixMode(5889);

		if (firstBB.contains(result.getHitVec())) {
			GlStateManager.lineWidth(2);
			WorldRenderer.drawSelectionBoundingBox(firstBB.grow(1 / 128f), 1, 1, .5f, 1f);
		} else {
			GlStateManager.lineWidth(2);
			WorldRenderer.drawSelectionBoundingBox(firstBB.grow(1 / 128f), .5f, .5f, .2f, 1f);
		}

		if (secondBB.contains(result.getHitVec())) {
			GlStateManager.lineWidth(2);
			WorldRenderer.drawSelectionBoundingBox(secondBB.grow(1 / 128f), 1, 1, .5f, 1f);
		} else {
			GlStateManager.lineWidth(2);
			WorldRenderer.drawSelectionBoundingBox(secondBB.grow(1 / 128f), .5f, .5f, .2f, 1f);
		}

		GlStateManager.matrixMode(5888);
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
		GlStateManager.lineWidth(1);
		TessellatorHelper.cleanUpAfterDrawing();
	}

}
