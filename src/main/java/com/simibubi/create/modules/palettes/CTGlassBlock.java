package com.simibubi.create.modules.palettes;

import com.simibubi.create.AllCTs;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.IHaveConnectedTextures;
import com.simibubi.create.foundation.block.connected.StandardCTBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlassBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CTGlassBlock extends GlassBlock implements IHaveConnectedTextures {

	private boolean hasAlpha;
	protected ConnectedTextureBehaviour behaviour;

	public CTGlassBlock(AllCTs spriteShift, boolean hasAlpha) {
		super(Properties.from(Blocks.GLASS));
		this.hasAlpha = hasAlpha;
		this.behaviour = createBehaviour(spriteShift.get());
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() instanceof CTGlassBlock
				? (!state.canRenderInLayer(BlockRenderLayer.TRANSLUCENT) && side.getAxis().isHorizontal()
						|| state.getBlock() == adjacentBlockState.getBlock())
				: super.isSideInvisible(state, adjacentBlockState, side);
	}

	public ConnectedTextureBehaviour createBehaviour(CTSpriteShiftEntry spriteShift) {
		return new StandardCTBehaviour(spriteShift);
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return hasAlpha ? BlockRenderLayer.TRANSLUCENT : super.getRenderLayer();
	}

	@Override
	public ConnectedTextureBehaviour getBehaviour() {
		return behaviour;
	}

}
