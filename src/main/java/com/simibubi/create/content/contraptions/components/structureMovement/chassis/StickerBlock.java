package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class StickerBlock extends ProperDirectionalBlock implements ITE<StickerTileEntity>, EntityBlock {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;

	public StickerBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(EXTENDED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction nearestLookingDirection = context.getNearestLookingDirection();
		boolean shouldPower = context.getLevel()
			.hasNeighborSignal(context.getClickedPos());
		Direction facing = context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown() ? nearestLookingDirection : nearestLookingDirection.getOpposite();

		return defaultBlockState().setValue(FACING, facing)
			.setValue(POWERED, shouldPower);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED, EXTENDED));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != worldIn.hasNeighborSignal(pos)) {
			state = state.cycle(POWERED);
			if (state.getValue(POWERED))
				state = state.cycle(EXTENDED);
			worldIn.setBlock(pos, state, 2);
		}
	}



	@Override
	public boolean shouldCheckWeakPower(BlockState state, LevelReader world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return AllTileEntities.STICKER.create(pos, state);
	}

	@Override
	public Class<StickerTileEntity> getTileEntityClass() {
		return StickerTileEntity.class;
	}

	// Slime block stuff

	private boolean isUprightSticker(BlockGetter world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		return AllBlocks.STICKER.has(blockState) && blockState.getValue(FACING) == Direction.UP;
	}

	@Override
	public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, float height) {
		if (!isUprightSticker(world, pos) || entity.isSuppressingBounce()) {
			super.fallOn(world, state, pos, entity, height);
		} else {
			entity.causeFallDamage(pos.getY(), height, DamageSource.FALL);
		}
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter p_176216_1_, Entity p_176216_2_) {
		if (!isUprightSticker(p_176216_1_, p_176216_2_.blockPosition()
			.below()) || p_176216_2_.isSuppressingBounce()) {
			super.updateEntityAfterFallOn(p_176216_1_, p_176216_2_);
		} else {
			this.bounceUp(p_176216_2_);
		}
	}

	private void bounceUp(Entity p_226946_1_) {
		Vec3 Vector3d = p_226946_1_.getDeltaMovement();
		if (Vector3d.y < 0.0D) {
			double d0 = p_226946_1_ instanceof LivingEntity ? 1.0D : 0.8D;
			p_226946_1_.setDeltaMovement(Vector3d.x, -Vector3d.y * d0, Vector3d.z);
		}
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		double d0 = Math.abs(entity.getDeltaMovement().y);
		if (d0 < 0.1D && !entity.isSteppingCarefully() && isUprightSticker(level, pos)) {
			double d1 = 0.4D + d0 * 0.2D;
			entity.setDeltaMovement(entity.getDeltaMovement()
				.multiply(d1, 1.0D, d1));
		}
		super.stepOn(level, pos, state, entity);
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerLevel worldserver, BlockPos pos, BlockState state2,
		LivingEntity entity, int numberOfParticles) {
		if (isUprightSticker(worldserver, pos)) {
			worldserver.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.defaultBlockState()),
				entity.getX(), entity.getY(), entity.getZ(), numberOfParticles, 0.0D, 0.0D, 0.0D, (double) 0.15F);
			return true;
		}
		return super.addLandingEffects(state1, worldserver, pos, state2, entity, numberOfParticles);
	}

	@Override
	public boolean addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity) {
		if (state.getValue(FACING) == Direction.UP) {
			Vec3 Vector3d = entity.getDeltaMovement();
			world.addParticle(
				new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.defaultBlockState()).setPos(pos),
				entity.getX() + ((double) world.random.nextFloat() - 0.5D) * (double) entity.getBbWidth(),
				entity.getY() + 0.1D,
				entity.getZ() + ((double) world.random.nextFloat() - 0.5D) * (double) entity.getBbWidth(), Vector3d.x * -4.0D,
				1.5D, Vector3d.z * -4.0D);
			return true;
		}
		return super.addRunningEffects(state, world, pos, entity);
	}

}
