package com.simibubi.create.content.contraptions.behaviour.dispenser;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@MethodsReturnNonnullByDefault
public class ContraptionBlockSource implements BlockSource {
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
		if (context.state.hasProperty(BlockStateProperties.FACING) && overrideFacing != null)
			return context.state.setValue(BlockStateProperties.FACING, overrideFacing);
		return context.state;
	}

	@Override
	@Nullable
	public <T extends BlockEntity> T getEntity() {
		return null;
	}

	@Override
	@Nullable
	public ServerLevel getLevel() {
		MinecraftServer server = context.world.getServer();
		return server != null ? server.getLevel(context.world.dimension()) : null;
	}
}
