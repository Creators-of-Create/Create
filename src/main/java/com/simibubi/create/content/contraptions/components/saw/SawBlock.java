package com.simibubi.create.content.contraptions.components.saw;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.components.actors.DrillBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SawBlock extends DirectionalAxisKineticBlock implements ITE<SawTileEntity> {
	public static DamageSource damageSourceSaw = new DamageSource("create.mechanical_saw").bypassArmor();

	public SawBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState stateForPlacement = super.getStateForPlacement(context);
		Direction facing = stateForPlacement.getValue(FACING);
		if (facing.getAxis().isVertical())
			return stateForPlacement;
		return stateForPlacement.setValue(AXIS_ALONG_FIRST_COORDINATE, facing.getAxis() == Axis.X);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.SAW.create();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.CASING_12PX.get(state.getValue(FACING));
	}

	@Override
	public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (entityIn instanceof ItemEntity)
			return;
		if (!new AxisAlignedBB(pos).deflate(.1f).intersects(entityIn.getBoundingBox()))
			return;
		withTileEntityDo(worldIn, pos, te -> {
			if (te.getSpeed() == 0)
				return;
			entityIn.hurt(damageSourceSaw, (float) DrillBlock.getDamage(te.getSpeed()));
		});
	}

	@Override
	public void updateEntityAfterFallOn(IBlockReader worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);
		if (!(entityIn instanceof ItemEntity))
			return;
		if (entityIn.level.isClientSide)
			return;

		BlockPos pos = entityIn.blockPosition();
		withTileEntityDo(entityIn.level, pos, te -> {
			if (te.getSpeed() == 0)
				return;
			te.insertItem((ItemEntity) entityIn);
		});
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	public static boolean isHorizontal(BlockState state) {
		return state.getValue(FACING).getAxis().isHorizontal();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return isHorizontal(state) ? state.getValue(FACING).getAxis() : super.getRotationAxis(state);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return isHorizontal(state) ? face == state.getValue(FACING).getOpposite()
				: super.hasShaftTowards(world, pos, state, face);
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.getBlock() == newState.getBlock())
			return;

		withTileEntityDo(worldIn, pos, te -> ItemHelper.dropContents(worldIn, pos, te.inventory));
		TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
      		worldIn.removeBlockEntity(pos);
	}

	@Override
	public Class<SawTileEntity> getTileEntityClass() {
		return SawTileEntity.class;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
