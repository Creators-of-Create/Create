package com.simibubi.create.foundation.metadoc.instructions;

import com.simibubi.create.foundation.metadoc.MetaDocScene;
import com.simibubi.create.foundation.metadoc.MetaDocWorld;
import com.simibubi.create.foundation.metadoc.Select;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class ReplaceBlocksInstruction extends WorldModifyInstruction {

	private BlockState stateToUse;
	private boolean replaceAir;

	public ReplaceBlocksInstruction(Select selection, BlockState stateToUse, boolean replaceAir) {
		super(selection);
		this.stateToUse = stateToUse;
		this.replaceAir = replaceAir;
	}

	@Override
	protected void runModification(Select selection, MetaDocScene scene) {
		MetaDocWorld world = scene.getWorld();
		selection.all()
			.forEach(pos -> {
				if (!world.getBounds()
					.isVecInside(pos))
					return;
				if (!replaceAir && world.getBlockState(pos) == Blocks.AIR.getDefaultState())
					return;
				world.setBlockState(pos, stateToUse);
			});
	}

	@Override
	protected boolean needsRedraw() {
		return true;
	}

}
