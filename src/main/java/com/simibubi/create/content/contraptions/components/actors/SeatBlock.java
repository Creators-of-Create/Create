package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllShapes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class SeatBlock extends Block {

	private boolean inCreativeTab;

	public SeatBlock(Properties p_i48440_1_, boolean inCreativeTab) {
		super(p_i48440_1_);
		this.inCreativeTab = inCreativeTab;
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> p_149666_2_) {
		if (group != ItemGroup.SEARCH && !inCreativeTab)
			return;
		super.fillItemGroup(group, p_149666_2_);
	}

	@Override
	public void onFallenUpon(World p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
		super.onFallenUpon(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_ * 0.5F);
	}

	@Override
	public void onLanded(IBlockReader p_176216_1_, Entity p_176216_2_) {
		Blocks.PINK_BED.onLanded(p_176216_1_, p_176216_2_);
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.SEAT;
	}

	@Override
	public ActionResultType onUse(BlockState p_225533_1_, World world, BlockPos pos, PlayerEntity player, Hand p_225533_5_, BlockRayTraceResult p_225533_6_) {
		if (SeatEntity.TAKEN.containsKey(pos))
			return ActionResultType.FAIL;

		if (world.isRemote)
			return ActionResultType.SUCCESS;

		SeatEntity seat = new SeatEntity(world, pos);
		world.addEntity(seat);
		player.startRiding(seat);

		return ActionResultType.SUCCESS;
	}
}
