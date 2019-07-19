package com.simibubi.create.block.symmetry;

import com.simibubi.create.item.symmetry.SymmetryCrossPlane;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;

public class CrossPlaneSymmetryBlock extends SymmetryBlock {

	public static final EnumProperty<SymmetryCrossPlane.Align> align = EnumProperty.create("align",
			SymmetryCrossPlane.Align.class);

	public CrossPlaneSymmetryBlock() {
		super(Properties.create(Material.AIR));
		this.setDefaultState(getDefaultState().with(align, SymmetryCrossPlane.Align.Y));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(align);
		super.fillStateContainer(builder);
	}

}
