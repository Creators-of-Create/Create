package com.simibubi.create.modules.symmetry.block;

import com.simibubi.create.modules.symmetry.mirror.PlaneMirror;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;

public class PlaneSymmetryBlock extends SymmetryBlock {

	public static final EnumProperty<PlaneMirror.Align> align = EnumProperty.create("align", PlaneMirror.Align.class);
	
	public PlaneSymmetryBlock() {
		super(Properties.create(Material.AIR));
		this.setDefaultState(getDefaultState().with(align, PlaneMirror.Align.XY));	
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(align);
		super.fillStateContainer(builder);
	}
	
}
