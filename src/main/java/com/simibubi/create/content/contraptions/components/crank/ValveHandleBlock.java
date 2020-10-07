package com.simibubi.create.content.contraptions.components.crank;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllShapes;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@ParametersAreNonnullByDefault
public class ValveHandleBlock extends HandCrankBlock {
	private final boolean inCreativeTab;

	public ValveHandleBlock(Properties properties, boolean inCreativeTab) {
		super(properties);
		this.inCreativeTab = inCreativeTab;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> p_149666_2_) {
		if (group != ItemGroup.SEARCH && !inCreativeTab)
			return;
		super.fillItemGroup(group, p_149666_2_);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.VALVE_HANDLE.get(state.get(FACING));
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public AllBlockPartials getRenderedHandle() {
		return null;
	}
	
	@Override
	public int getRotationSpeed() {
		return 16;
	}

}
