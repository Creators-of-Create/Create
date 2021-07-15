package com.simibubi.create.content.contraptions.components.actors.dispenser;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

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
	public double x() {
		return (double)this.pos.getX() + 0.5D;
	}

	@Override
	public double y() {
		return (double)this.pos.getY() + 0.5D;
	}

	@Override
	public double z() {
		return (double)this.pos.getZ() + 0.5D;
	}

	@Override
	public BlockPos getPos() {
		return pos;
	}

	@Override
	public BlockState getBlockState() {
		if(context.state.hasProperty(BlockStateProperties.FACING) && overrideFacing != null)
			return context.state.setValue(BlockStateProperties.FACING, overrideFacing);
		return context.state;
	}

	@Override
	@Nullable
	public <T extends TileEntity> T getEntity() {
		return null;
	}

	@Override
	@Nullable
	public ServerWorld getLevel() {
		MinecraftServer server = context.world.getServer();
		return server != null ? server.getLevel(context.world.dimension()) : null;
	}
}
