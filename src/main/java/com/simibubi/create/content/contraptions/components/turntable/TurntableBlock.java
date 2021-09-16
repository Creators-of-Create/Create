package com.simibubi.create.content.contraptions.components.turntable;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class TurntableBlock extends KineticBlock implements ITE<TurntableTileEntity> {

	public TurntableBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return AllTileEntities.TURNTABLE.create();
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.TURNTABLE_SHAPE;
	}

	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity e) {
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

			Level world = e.getCommandSenderWorld();
			if (world.isClientSide && (e instanceof Player)) {
				if (worldIn.getBlockState(e.blockPosition()) != state) {
					Vec3 origin = VecHelper.getCenterOf(pos);
					Vec3 offset = e.position()
						.subtract(origin);
					offset = VecHelper.rotate(offset, Mth.clamp(speed, -16, 16) / 1f, Axis.Y);
					Vec3 movement = origin.add(offset)
						.subtract(e.position());
					e.setDeltaMovement(e.getDeltaMovement()
						.add(movement));
					e.hurtMarked = true;
				}
			}

			if ((e instanceof Player))
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
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
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
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
