package com.simibubi.create.modules.contraptions.receivers;

import java.util.List;

import com.simibubi.create.foundation.block.IRenderUtilityBlock;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.VoxelShapers;
import com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.modules.contraptions.receivers.contraptions.IHaveMovementBehavior;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DrillBlock extends DirectionalKineticBlock
		implements IHaveMovementBehavior, IWithTileEntity<DrillTileEntity> {

	public DrillBlock() {
		super(Properties.from(Blocks.IRON_BLOCK));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new DrillTileEntity();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapers.SHORT_CASING.get(state.get(FACING));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		withTileEntityDo(worldIn, pos, DrillTileEntity::destroyNextTick);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING).getAxis();
	}

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(FACING).getOpposite();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public SuperByteBuffer renderInContraption(MovementContext context) {
		return DrillTileEntityRenderer.renderInContraption(context);
	}

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.get(FACING).getOpposite());
	}

	@Override
	public Vec3d getActiveAreaOffset(MovementContext context) {
		return new Vec3d(context.state.get(FACING).getDirectionVec()).scale(.65f);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		World world = context.world;
		BlockState stateVisited = world.getBlockState(pos);

		if (world.isRemote)
			return;
		if (stateVisited.getCollisionShape(world, pos).isEmpty())
			return;
		if (stateVisited.getBlockHardness(world, pos) == -1)
			return;

		world.playEvent(2001, pos, Block.getStateId(stateVisited));
		List<ItemStack> drops = Block.getDrops(stateVisited, (ServerWorld) world, pos, null);
		world.setBlockState(pos, Blocks.AIR.getDefaultState());

		for (ItemStack stack : drops) {
			ItemEntity itemEntity = new ItemEntity(world, pos.getX() + .5f, pos.getY() + .25f, pos.getZ() + .5f, stack);
			itemEntity.setMotion(context.motion.add(0, 0.5f, 0).scale(world.rand.nextFloat() * .3f));
			world.addEntity(itemEntity);
		}
	}

	public static class DrillHeadBlock extends DirectionalBlock implements IRenderUtilityBlock {

		public DrillHeadBlock() {
			super(Properties.from(Blocks.AIR));
		}

		@Override
		protected void fillStateContainer(Builder<Block, BlockState> builder) {
			builder.add(FACING);
			super.fillStateContainer(builder);
		}

	}

}
