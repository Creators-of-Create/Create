package com.simibubi.create.block.symmetry;

import com.simibubi.create.item.symmetry.SymmetryPlane;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;

public class PlaneSymmetryBlock extends SymmetryBlock {

	public static final EnumProperty<SymmetryPlane.Align> align = EnumProperty.create("align", SymmetryPlane.Align.class);
	
	public PlaneSymmetryBlock() {
		super(Properties.create(Material.AIR));
		this.setDefaultState(getDefaultState().with(align, SymmetryPlane.Align.XY));	
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(align);
		super.fillStateContainer(builder);
	}
	
}
