package com.simibubi.create.block;

import java.util.List;

import com.simibubi.create.utility.Keyboard;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CreativeCrateBlock extends Block {

	protected static final VoxelShape shape = makeCuboidShape(1, 0, 1, 15, 14, 15);
	
	public CreativeCrateBlock() {
		super(Properties.create(Material.WOOD));
	}
	
	@Override
	public boolean isSolid(BlockState state) {
		return false;
	}
	
	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		if (Keyboard.isKeyDown(Keyboard.LSHIFT)) {
			tooltip.add(new StringTextComponent(TextFormatting.LIGHT_PURPLE + "Creative Item"));
			tooltip.add(new StringTextComponent(TextFormatting.GRAY + "Grants an attached " + TextFormatting.BLUE + "Schematicannon"));
			tooltip.add(new StringTextComponent(TextFormatting.GRAY + "unlimited access to blocks."));
		} else 
			tooltip.add(new StringTextComponent(TextFormatting.DARK_GRAY + "< Hold Shift >"));
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return shape;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
			ISelectionContext context) {
		return shape;
	}

}
