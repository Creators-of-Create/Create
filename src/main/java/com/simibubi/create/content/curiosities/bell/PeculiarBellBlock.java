package com.simibubi.create.content.curiosities.bell;

import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;

import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class PeculiarBellBlock extends AbstractBellBlock<PeculiarBellTileEntity> {

	public PeculiarBellBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.PECULIAR_BELL.create();
	}

	@Override
	public Class<PeculiarBellTileEntity> getTileEntityClass() { return PeculiarBellTileEntity.class; }

	@Override
	public void playSound(World world, BlockPos pos) {
		AllSoundEvents.PECULIAR_BELL_USE.playOnServer(world, pos, 2f, 0.94f);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		BlockState newState = super.getStateForPlacement(ctx);
		if (newState == null)
			return null;

		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		return tryConvert(world, pos, newState, world.getBlockState(pos.offset(Direction.DOWN)));
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world,
										  BlockPos currentPos, BlockPos facingPos) {
		BlockState newState = super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
		if (facing != Direction.DOWN)
			return newState;

		return tryConvert(world, currentPos, newState, facingState);
	}

	protected BlockState tryConvert(IWorld world, BlockPos pos, BlockState state, BlockState underState) {
		Block underBlock = underState.getBlock();
		if (!(Blocks.SOUL_FIRE.is(underBlock) || Blocks.SOUL_CAMPFIRE.is(underBlock)))
			return state;

		if (world.isRemote()) {
			spawnConversionParticles(world, pos);
		} else if (world instanceof World) {
			AllSoundEvents.CURSED_BELL_CONVERT.playOnServer((World) world, pos);
		}

		return AllBlocks.CURSED_BELL.getDefaultState()
				.with(BellBlock.field_220133_a, state.get(BellBlock.field_220133_a))
				.with(BellBlock.field_220134_b, state.get(BellBlock.field_220134_b))
				.with(BellBlock.POWERED, state.get(BellBlock.POWERED));
	}

	public void spawnConversionParticles(IWorld world, BlockPos blockPos) {
		Random random = world.getRandom();
		int num = random.nextInt(10) + 15;
		for (int i = 0; i < num; i++) {
			float pitch = random.nextFloat()*120 - 90;
			float yaw = random.nextFloat()*360;
			Vector3d vel = Vector3d.fromPitchYaw(pitch, yaw).scale(random.nextDouble()*0.1 + 0.1);
			Vector3d pos = Vector3d.ofCenter(blockPos);
			world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
		}
	}

}
