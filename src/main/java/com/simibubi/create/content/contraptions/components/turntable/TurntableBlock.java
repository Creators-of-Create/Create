package com.simibubi.create.content.contraptions.components.turntable;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class TurntableBlock extends KineticBlock implements ITE<TurntableTileEntity> {

	public TurntableBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.TURNTABLE.create();
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.TURNTABLE_SHAPE;
	}

	@Override
	public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity e) {
		if (!e.isOnGround())
			return;
		if (e.getDeltaMovement().y > 0)
			return;
		if (e.getY() < pos.getY() + .5f)
			return;

		withTileEntityDo(worldIn, pos, te -> {
			float speed = ((KineticTileEntity) te).getSpeed() * 3 / 10;
			if (speed == 0)
				return;

			World world = e.getCommandSenderWorld();
			if (world.isClientSide && (e instanceof PlayerEntity)) {
				if (worldIn.getBlockState(e.blockPosition()) != state) {
					Vector3d origin = VecHelper.getCenterOf(pos);
					Vector3d offset = e.position()
						.subtract(origin);
					offset = VecHelper.rotate(offset, MathHelper.clamp(speed, -16, 16) / 1f, Axis.Y);
					Vector3d movement = origin.add(offset)
						.subtract(e.position());
					e.setDeltaMovement(e.getDeltaMovement()
						.add(movement));
					e.hurtMarked = true;
				}
			}

			if ((e instanceof PlayerEntity))
				return;
			if (world.isClientSide)
				return;

			if ((e instanceof LivingEntity)) {
				float diff = e.getYHeadRot() - speed;
				((LivingEntity) e).setNoActionTime(20);
				e.setYBodyRot(diff);
				e.setYHeadRot(diff);
				e.setOnGround(false);
				e.hurtMarked = true;
			}

			e.yRot -= speed;
		});
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.DOWN;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public Class<TurntableTileEntity> getTileEntityClass() {
		return TurntableTileEntity.class;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
