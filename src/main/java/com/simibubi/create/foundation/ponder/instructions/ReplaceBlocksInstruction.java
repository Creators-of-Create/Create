package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.Select;

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
	protected void runModification(Select selection, PonderScene scene) {
		PonderWorld world = scene.getWorld();
		selection.all()
			.forEach(pos -> {
				if (!world.getBounds()
					.isVecInside(pos))
					return;
				BlockState prevState = world.getBlockState(pos);
				if (!replaceAir && prevState == Blocks.AIR.getDefaultState())
					return;
				world.addBlockDestroyEffects(pos, prevState);
				world.setBlockState(pos, stateToUse);
			});
	}

	@Override
	protected boolean needsRedraw() {
		return true;
	}

}
