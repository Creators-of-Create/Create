package com.simibubi.create.content.contraptions.components.crank;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllShapes;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ValveHandleBlock extends HandCrankBlock {

	public ValveHandleBlock(Properties properties) {
		super(properties);
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
