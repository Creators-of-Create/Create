package com.simibubi.create.modules.curiosities.symmetry.block;

import com.simibubi.create.modules.curiosities.symmetry.mirror.CrossPlaneMirror;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;

public class CrossPlaneSymmetryBlock extends SymmetryBlock {

	public static final EnumProperty<CrossPlaneMirror.Align> align = EnumProperty.create("align",
			CrossPlaneMirror.Align.class);

	public CrossPlaneSymmetryBlock() {
		super(Properties.create(Material.AIR));
		this.setDefaultState(getDefaultState().with(align, CrossPlaneMirror.Align.Y));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(align);
		super.fillStateContainer(builder);
	}

}
