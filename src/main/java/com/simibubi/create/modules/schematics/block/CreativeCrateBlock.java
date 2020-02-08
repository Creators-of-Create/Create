package com.simibubi.create.modules.schematics.block;

import com.simibubi.create.foundation.utility.AllShapes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class CreativeCrateBlock extends Block {

	public CreativeCrateBlock() {
		super(Properties.create(Material.WOOD));
	}

	@Override
	public boolean isSolid(BlockState state) {
		return false;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.CRATE_BLOCK_SHAPE;
	}

}
