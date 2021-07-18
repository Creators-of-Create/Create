package com.simibubi.create.content.contraptions.components.actors;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.ITE;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DrillBlock extends DirectionalKineticBlock implements ITE<DrillTileEntity> {
	public static DamageSource damageSourceDrill = new DamageSource("create.mechanical_drill").bypassArmor();

	public DrillBlock(Properties properties) {
		super(properties);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
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
			entityIn.hurt(damageSourceDrill, (float) getDamage(te.getSpeed()));
		});
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.DRILL.create();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.CASING_12PX.get(state.getValue(FACING));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		withTileEntityDo(worldIn, pos, DrillTileEntity::destroyNextTick);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING).getAxis();
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(FACING).getOpposite();
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	@Override
	public Class<DrillTileEntity> getTileEntityClass() {
		return DrillTileEntity.class;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

	public static double getDamage(float speed) {
		float speedAbs = Math.abs(speed);
		double sub1 = Math.min(speedAbs / 16, 2);
		double sub2 = Math.min(speedAbs / 32, 4);
		double sub3 = Math.min(speedAbs / 64, 4);
		return MathHelper.clamp(sub1 + sub2 + sub3, 1, 10);
	}
}
