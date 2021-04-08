package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class FunnelBlock extends AbstractDirectionalFunnelBlock {

	public static final BooleanProperty EXTRACTING = BooleanProperty.create("extracting");

	public FunnelBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		setDefaultState(getDefaultState().with(EXTRACTING, false));
	}

	public abstract BlockState getEquivalentBeltFunnel(IBlockReader world, BlockPos pos, BlockState state);

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = super.getStateForPlacement(context);

		boolean sneak = context.getPlayer() != null && context.getPlayer()
			.isSneaking();
		state = state.with(EXTRACTING, !sneak);

		for (Direction direction : context.getNearestLookingDirections()) {
			BlockState blockstate = state.with(FACING, direction.getOpposite());
			if (blockstate.isValidPosition(context.getWorld(), context.getPos()))
				return blockstate.with(POWERED, state.get(POWERED));
		}

		return state;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(EXTRACTING));
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {

		ItemStack heldItem = player.getHeldItem(handIn);
		boolean shouldntInsertItem = AllBlocks.MECHANICAL_ARM.isIn(heldItem) || !canInsertIntoFunnel(state);

		if (AllItems.WRENCH.isIn(heldItem))
			return ActionResultType.PASS;

		if (hit.getFace() == getFunnelFacing(state) && !shouldntInsertItem) {
			if (!worldIn.isRemote)
				withTileEntityDo(worldIn, pos, te -> {
					ItemStack toInsert = heldItem.copy();
					ItemStack remainder = tryInsert(worldIn, pos, toInsert, false);
					if (!ItemStack.areItemStacksEqual(remainder, toInsert))
						player.setHeldItem(handIn, remainder);
				});
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		if (!world.isRemote)
			world.setBlockState(context.getPos(), state.cycle(EXTRACTING));
		return ActionResultType.SUCCESS;
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (worldIn.isRemote)
			return;
		if (!(entityIn instanceof ItemEntity))
			return;
		if (!canInsertIntoFunnel(state))
			return;
		if (!entityIn.isAlive())
			return;
		ItemEntity itemEntity = (ItemEntity) entityIn;

		Direction direction = getFunnelFacing(state);
		Vector3d diff = entityIn.getPositionVec()
			.subtract(VecHelper.getCenterOf(pos)
				.add(Vector3d.of(direction.getDirectionVec()).scale(-.325f)));
		double projectedDiff = direction.getAxis()
			.getCoordinate(diff.x, diff.y, diff.z);
		if (projectedDiff < 0 == (direction.getAxisDirection() == AxisDirection.POSITIVE))
			return;

		ItemStack toInsert = itemEntity.getItem();
		ItemStack remainder = tryInsert(worldIn, pos, toInsert, false);

		if (remainder.isEmpty())
			itemEntity.remove();
		if (remainder.getCount() < toInsert.getCount())
			itemEntity.setItem(remainder);
	}

	protected boolean canInsertIntoFunnel(BlockState state) {
		return !state.get(POWERED) && !state.get(EXTRACTING);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		Direction facing = state.get(FACING);
		return facing == Direction.DOWN ? AllShapes.FUNNEL_CEILING
			: facing == Direction.UP ? AllShapes.FUNNEL_FLOOR : AllShapes.FUNNEL_WALL.get(facing);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		if (context.getEntity() instanceof ItemEntity && getFacing(state).getAxis()
			.isHorizontal())
			return AllShapes.FUNNEL_COLLISION.get(getFacing(state));
		return getShape(state, world, pos, context);
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState p_196271_3_, IWorld world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (getFacing(state).getAxis()
			.isVertical() || direction != Direction.DOWN)
			return state;
		BlockState equivalentFunnel = getEquivalentBeltFunnel(null, null, state);
		if (BeltFunnelBlock.isOnValidBelt(equivalentFunnel, world, pos))
			return equivalentFunnel.with(BeltFunnelBlock.SHAPE,
				BeltFunnelBlock.getShapeForPosition(world, pos, getFacing(state), state.get(EXTRACTING)));
		return state;
	}

}
