package com.simibubi.create.modules.palettes;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.IHaveConnectedTextures;
import com.simibubi.create.foundation.block.connected.StandardCTBehaviour;
import com.simibubi.create.modules.curiosities.partialWindows.WindowInABlockTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CTGlassPaneBlock extends GlassPaneBlock implements IHaveConnectedTextures {

	protected CTGlassBlock ctGlass;
	protected ConnectedTextureBehaviour behaviour;

	public CTGlassPaneBlock(Block ctGlass) {
		super(Properties.from(Blocks.GLASS));
		this.ctGlass = (CTGlassBlock) ctGlass;
		behaviour = createBehaviour();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		if (side.getAxis().isVertical())
			return adjacentBlockState == state;
		return super.isSideInvisible(state, adjacentBlockState, side);
	}

	protected ConnectedTextureBehaviour createBehaviour() {
		for (CTSpriteShiftEntry ctSpriteShiftEntry : ctGlass.getBehaviour().getAllCTShifts()) {
			return new StandardCTBehaviour(ctSpriteShiftEntry) {
				@Override
				public boolean connectsTo(BlockState state, BlockState other, IEnviromentBlockReader reader,
						BlockPos pos, BlockPos otherPos, Direction face) {
					
					TileEntity te = reader.getTileEntity(pos);
					if (te instanceof WindowInABlockTileEntity)
						state = ((WindowInABlockTileEntity) te).getWindowBlock();
					
					TileEntity otherTE = reader.getTileEntity(otherPos);
					if (otherTE instanceof WindowInABlockTileEntity)
						other = ((WindowInABlockTileEntity) otherTE).getWindowBlock();
					
					return state.getBlock() == other.getBlock();
				}
				
				@Override
				protected boolean reverseUVsHorizontally(BlockState state, net.minecraft.util.Direction face) {
					if (face.getAxisDirection() == AxisDirection.NEGATIVE)
						return true;
					return super.reverseUVsHorizontally(state, face);
				}
			};
		}
		return null;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return ctGlass.getRenderLayer();
	}

	@Override
	public ConnectedTextureBehaviour getBehaviour() {
		return behaviour;
	}

}
