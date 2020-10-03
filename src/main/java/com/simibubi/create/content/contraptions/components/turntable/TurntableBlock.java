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

public class TurntableBlock extends KineticBlock implements ITE<TurntableTileEntity> {

	public TurntableBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.TURNTABLE.create();
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.TURNTABLE_SHAPE;
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity e) {
		if (!e.isOnGround())
			return;
		if (e.getMotion().y > 0)
			return;
		if (e.getY() < pos.getY() + .5f)
			return;

		withTileEntityDo(worldIn, pos, te -> {
			float speed = ((KineticTileEntity) te).getSpeed() * 3 / 10;
			if (speed == 0)
				return;

			World world = e.getEntityWorld();
			if (world.isRemote && (e instanceof PlayerEntity)) {
				if (worldIn.getBlockState(e.getBlockPos()) != state) {
					Vector3d origin = VecHelper.getCenterOf(pos);
					Vector3d offset = e.getPositionVec()
						.subtract(origin);
					offset = VecHelper.rotate(offset, MathHelper.clamp(speed, -16, 16) / 1f, Axis.Y);
					Vector3d movement = origin.add(offset)
						.subtract(e.getPositionVec());
					e.setMotion(e.getMotion()
						.add(movement));
					e.velocityChanged = true;
				}
			}

			if ((e instanceof PlayerEntity))
				return;
			if (world.isRemote)
				return;

			if ((e instanceof LivingEntity)) {
				float diff = e.getRotationYawHead() - speed;
				((LivingEntity) e).setIdleTime(20);
				e.setRenderYawOffset(diff);
				e.setRotationYawHead(diff);
				e.setOnGround(false);
				e.velocityChanged = true;
			}

			e.rotationYaw -= speed;
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

}
