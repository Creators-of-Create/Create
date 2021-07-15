package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import net.minecraft.block.AbstractBlock.Properties;

public class StickerBlock extends ProperDirectionalBlock implements ITE<StickerTileEntity> {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;

	public StickerBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(EXTENDED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
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
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
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
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.STICKER.create();
	}

	@Override
	public Class<StickerTileEntity> getTileEntityClass() {
		return StickerTileEntity.class;
	}

	// Slime block stuff

	private boolean isUprightSticker(IBlockReader world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		return AllBlocks.STICKER.has(blockState) && blockState.getValue(FACING) == Direction.UP;
	}

	@Override
	public void fallOn(World p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
		if (!isUprightSticker(p_180658_1_, p_180658_2_) || p_180658_3_.isSuppressingBounce()) {
			super.fallOn(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_);
		} else {
			p_180658_3_.causeFallDamage(p_180658_4_, 0.0F);
		}
	}

	@Override
	public void updateEntityAfterFallOn(IBlockReader p_176216_1_, Entity p_176216_2_) {
		if (!isUprightSticker(p_176216_1_, p_176216_2_.blockPosition()
			.below()) || p_176216_2_.isSuppressingBounce()) {
			super.updateEntityAfterFallOn(p_176216_1_, p_176216_2_);
		} else {
			this.bounceUp(p_176216_2_);
		}
	}

	private void bounceUp(Entity p_226946_1_) {
		Vector3d Vector3d = p_226946_1_.getDeltaMovement();
		if (Vector3d.y < 0.0D) {
			double d0 = p_226946_1_ instanceof LivingEntity ? 1.0D : 0.8D;
			p_226946_1_.setDeltaMovement(Vector3d.x, -Vector3d.y * d0, Vector3d.z);
		}
	}

	@Override
	public void stepOn(World p_176199_1_, BlockPos p_176199_2_, Entity p_176199_3_) {
		double d0 = Math.abs(p_176199_3_.getDeltaMovement().y);
		if (d0 < 0.1D && !p_176199_3_.isSteppingCarefully() && isUprightSticker(p_176199_1_, p_176199_2_)) {
			double d1 = 0.4D + d0 * 0.2D;
			p_176199_3_.setDeltaMovement(p_176199_3_.getDeltaMovement()
				.multiply(d1, 1.0D, d1));
		}
		super.stepOn(p_176199_1_, p_176199_2_, p_176199_3_);
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2,
		LivingEntity entity, int numberOfParticles) {
		if (isUprightSticker(worldserver, pos)) {
			worldserver.sendParticles(new BlockParticleData(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.defaultBlockState()),
				entity.getX(), entity.getY(), entity.getZ(), numberOfParticles, 0.0D, 0.0D, 0.0D, (double) 0.15F);
			return true;
		}
		return super.addLandingEffects(state1, worldserver, pos, state2, entity, numberOfParticles);
	}

	@Override
	public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
		if (state.getValue(FACING) == Direction.UP) {
			Vector3d Vector3d = entity.getDeltaMovement();
			world.addParticle(
				new BlockParticleData(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.defaultBlockState()).setPos(pos),
				entity.getX() + ((double) world.random.nextFloat() - 0.5D) * (double) entity.getBbWidth(),
				entity.getY() + 0.1D,
				entity.getZ() + ((double) world.random.nextFloat() - 0.5D) * (double) entity.getBbWidth(), Vector3d.x * -4.0D,
				1.5D, Vector3d.z * -4.0D);
			return true;
		}
		return super.addRunningEffects(state, world, pos, entity);
	}

}
