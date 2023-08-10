package com.simibubi.create.content.equipment.bell;

import java.util.Random;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PeculiarBellBlock extends AbstractBellBlock<PeculiarBellBlockEntity> {

	public PeculiarBellBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntityType<? extends PeculiarBellBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.PECULIAR_BELL.get();
	}

	@Override
	public Class<PeculiarBellBlockEntity> getBlockEntityClass() {
		return PeculiarBellBlockEntity.class;
	}

	@Override
	public void playSound(Level world, BlockPos pos) {
		AllSoundEvents.PECULIAR_BELL_USE.playOnServer(world, pos, 2f, 0.94f);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState newState = super.getStateForPlacement(ctx);
		if (newState == null)
			return null;

		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		return tryConvert(world, pos, newState, world.getBlockState(pos.relative(Direction.DOWN)));
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world,
										  BlockPos currentPos, BlockPos facingPos) {
		BlockState newState = super.updateShape(state, facing, facingState, world, currentPos, facingPos);
		if (facing != Direction.DOWN)
			return newState;

		return tryConvert(world, currentPos, newState, facingState);
	}

	protected BlockState tryConvert(LevelAccessor world, BlockPos pos, BlockState state, BlockState underState) {
		if (!AllBlocks.PECULIAR_BELL.has(state))
			return state;

		Block underBlock = underState.getBlock();
		if (!(Blocks.SOUL_FIRE.equals(underBlock) || Blocks.SOUL_CAMPFIRE.equals(underBlock)))
			return state;

		if (world.isClientSide()) {
			spawnConversionParticles(world, pos);
		} else if (world instanceof Level) {
			AllSoundEvents.HAUNTED_BELL_CONVERT.playOnServer((Level) world, pos);
		}

		return AllBlocks.HAUNTED_BELL.getDefaultState()
				.setValue(HauntedBellBlock.FACING, state.getValue(FACING))
				.setValue(HauntedBellBlock.ATTACHMENT, state.getValue(ATTACHMENT))
				.setValue(HauntedBellBlock.POWERED, state.getValue(POWERED));
	}

	public void spawnConversionParticles(LevelAccessor world, BlockPos blockPos) {
		Random random = world.getRandom();
		int num = random.nextInt(10) + 15;
		for (int i = 0; i < num; i++) {
			float pitch = random.nextFloat() * 120 - 90;
			float yaw = random.nextFloat() * 360;
			Vec3 vel = Vec3.directionFromRotation(pitch, yaw).scale(random.nextDouble() * 0.1 + 0.1);
			Vec3 pos = Vec3.atCenterOf(blockPos);
			world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
		}
	}

}
