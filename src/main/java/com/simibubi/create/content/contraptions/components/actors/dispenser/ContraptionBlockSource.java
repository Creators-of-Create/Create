package com.simibubi.create.content.contraptions.components.actors.dispenser;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@MethodsReturnNonnullByDefault
public class ContraptionBlockSource implements IBlockSource {
	private final BlockPos pos;
	private final MovementContext context;
	private final Direction overrideFacing;

	public ContraptionBlockSource(MovementContext context, BlockPos pos) {
		this(context, pos, null);
	}

	public ContraptionBlockSource(MovementContext context, BlockPos pos, @Nullable Direction overrideFacing) {
		this.pos = pos;
		this.context = context;
		this.overrideFacing = overrideFacing;
	}

	@Override
	public double getX() {
		return (double)this.pos.getX() + 0.5D;
	}

	@Override
	public double getY() {
		return (double)this.pos.getY() + 0.5D;
	}

	@Override
	public double getZ() {
		return (double)this.pos.getZ() + 0.5D;
	}

	@Override
	public BlockPos getBlockPos() {
		return pos;
	}

	@Override
	public BlockState getBlockState() {
		if(context.state.has(BlockStateProperties.FACING) && overrideFacing != null)
			return context.state.with(BlockStateProperties.FACING, overrideFacing);
		return context.state;
	}

	@Override
	@Nullable
	public <T extends TileEntity> T getBlockTileEntity() {
		return null;
	}

	@Override
	public World getWorld() {
		return context.world;
	}
}
