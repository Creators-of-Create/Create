package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.AllShapes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class ItemDrainBlock extends Block {

	public ItemDrainBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.CASING_13PX.get(Direction.UP);
	}

	@Override
	public void fillItemGroup(ItemGroup p_149666_1_, NonNullList<ItemStack> p_149666_2_) {}

}
