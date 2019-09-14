package com.simibubi.create.modules.logistics.block;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.TessellatorHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public interface IBlockWithFilter {

	public Vec3d getFilterPosition(BlockState state);

	public Direction getFilterFacing(BlockState state);

	public default float getItemHitboxScale() {
		return 2 / 16f;
	}

	public default boolean handleActivatedFilterSlots(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof IHaveFilter))
			return false;

		IHaveFilter actor = (IHaveFilter) te;
		Vec3d vec = new Vec3d(pos);
		Vec3d position = vec.add(getFilterPosition(state));
		ItemStack stack = player.getHeldItem(handIn);
		float scale = getItemHitboxScale();

		if (new AxisAlignedBB(position, position).grow(scale * 2).contains(hit.getHitVec())) {
			if (worldIn.isRemote)
				return true;
			actor.setFilter(stack);
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

		if (!(state.getBlock() instanceof IBlockWithFilter))
			return;

		IBlockWithFilter filterBlock = (IBlockWithFilter) state.getBlock();
		Vec3d vec = new Vec3d(pos);
		Vec3d position = filterBlock.getFilterPosition(state).add(vec);
		float scale = filterBlock.getItemHitboxScale();

		AxisAlignedBB bb = new AxisAlignedBB(position, position).grow(scale, scale / 1.25f, scale).offset(0, -scale / 16f, 0);
		boolean contains = bb.grow(scale).contains(result.getHitVec());

		TessellatorHelper.prepareForDrawing();
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture();
		GlStateManager.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
		bb = bb.grow(1 / 128f);
		Vec3d center = bb.getCenter().subtract(vec);
		bb = bb.offset(center);
		Direction facing = filterBlock.getFilterFacing(state);
		Vec3i direction = facing.getDirectionVec();
		GlStateManager.pushMatrix();
		GlStateManager.translated(position.x, position.y, position.z);
		GlStateManager.rotated(22.5f, direction.getZ(), 0, -direction.getX());
		GlStateManager.translated(-center.x, -center.y, -center.z);
		GlStateManager.translated(-position.x, -position.y, -position.z);

		if (contains) {
			GlStateManager.lineWidth(2);
			WorldRenderer.drawBoundingBox(bufferbuilder, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, .5f, 1, .75f, 1f);
		} else {
			GlStateManager.lineWidth(2);
			WorldRenderer.drawBoundingBox(bufferbuilder, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, .25f, .5f, .35f, 1f);
		}
		
		tessellator.draw();
		
		GlStateManager.popMatrix();
		GlStateManager.enableTexture();
		GlStateManager.depthMask(true);
		
		if (contains) {
			float textScale = 1/128f;
			GlStateManager.translated(position.x, position.y, position.z);
			GlStateManager.rotated(facing.getHorizontalAngle() * (facing.getAxis() == Axis.X ? -1 : 1), 0, 1, 0);
			GlStateManager.scaled(textScale, -textScale, textScale);
			GlStateManager.translated(17.5f, -5f, -5f);
			GlStateManager.rotated(67.5f, 1, 0, 0);
			
			String text = Lang.translate("logistics.filter");
			Minecraft.getInstance().fontRenderer.drawString(text, 0, 0, 0x88FFBB);
			GlStateManager.translated(0, 0, -1/4f);
			Minecraft.getInstance().fontRenderer.drawString(text, 1, 1, 0x224433);
		}
		GlStateManager.disableBlend();

		GlStateManager.lineWidth(1);
		TessellatorHelper.cleanUpAfterDrawing();
	}

}
