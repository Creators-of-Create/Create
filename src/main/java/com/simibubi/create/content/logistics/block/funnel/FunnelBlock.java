package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FunnelBlock extends AbstractDirectionalFunnelBlock {

	public static final BooleanProperty EXTRACTING = BooleanProperty.create("extracting");

	public FunnelBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		registerDefaultState(defaultBlockState().setValue(EXTRACTING, false));
	}

	public abstract BlockState getEquivalentBeltFunnel(BlockGetter world, BlockPos pos, BlockState state);

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);

		boolean sneak = context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown();
		state = state.setValue(EXTRACTING, !sneak);

		for (Direction direction : context.getNearestLookingDirections()) {
			BlockState blockstate = state.setValue(FACING, direction.getOpposite());
			if (blockstate.canSurvive(context.getLevel(), context.getClickedPos()))
				return blockstate.setValue(POWERED, state.getValue(POWERED));
		}

		return state;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(EXTRACTING));
	}
	
	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {

		ItemStack heldItem = player.getItemInHand(handIn);
		boolean shouldntInsertItem = AllBlocks.MECHANICAL_ARM.isIn(heldItem) || !canInsertIntoFunnel(state);

		if (AllItems.WRENCH.isIn(heldItem))
			return InteractionResult.PASS;

		if (hit.getDirection() == getFunnelFacing(state) && !shouldntInsertItem) {
			if (!worldIn.isClientSide)
				withBlockEntityDo(worldIn, pos, be -> {
					ItemStack toInsert = heldItem.copy();
					ItemStack remainder = tryInsert(worldIn, pos, toInsert, false);
					if (!ItemStack.matches(remainder, toInsert))
						player.setItemInHand(handIn, remainder);
				});
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level world = context.getLevel();
		if (!world.isClientSide)
			world.setBlockAndUpdate(context.getClickedPos(), state.cycle(EXTRACTING));
		return InteractionResult.SUCCESS;
	}

	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		if (worldIn.isClientSide)
			return;
		if (!(entityIn instanceof ItemEntity))
			return;
		if (!canInsertIntoFunnel(state))
			return;
		if (!entityIn.isAlive())
			return;
		ItemEntity itemEntity = (ItemEntity) entityIn;

		Direction direction = getFunnelFacing(state);
		Vec3 diff = entityIn.position()
			.subtract(VecHelper.getCenterOf(pos)
				.add(Vec3.atLowerCornerOf(direction.getNormal())
					.scale(-.325f)));
		double projectedDiff = direction.getAxis()
			.choose(diff.x, diff.y, diff.z);
		if (projectedDiff < 0 == (direction.getAxisDirection() == AxisDirection.POSITIVE))
			return;

		ItemStack toInsert = itemEntity.getItem();
		ItemStack remainder = tryInsert(worldIn, pos, toInsert, false);

		if (remainder.isEmpty())
			itemEntity.discard();
		if (remainder.getCount() < toInsert.getCount())
			itemEntity.setItem(remainder);
	}

	protected boolean canInsertIntoFunnel(BlockState state) {
		return !state.getValue(POWERED) && !state.getValue(EXTRACTING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Direction facing = state.getValue(FACING);
		return facing == Direction.DOWN ? AllShapes.FUNNEL_CEILING
			: facing == Direction.UP ? AllShapes.FUNNEL_FLOOR : AllShapes.FUNNEL_WALL.get(facing);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext
			&& ((EntityCollisionContext) context).getEntity() instanceof ItemEntity && getFacing(state).getAxis()
				.isHorizontal())
			return AllShapes.FUNNEL_COLLISION.get(getFacing(state));
		return getShape(state, world, pos, context);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState p_196271_3_, LevelAccessor world,
		BlockPos pos, BlockPos p_196271_6_) {
		updateWater(world, state, pos);
		if (getFacing(state).getAxis()
			.isVertical() || direction != Direction.DOWN)
			return state;
		BlockState equivalentFunnel =
			ProperWaterloggedBlock.withWater(world, getEquivalentBeltFunnel(null, null, state), pos);
		if (BeltFunnelBlock.isOnValidBelt(equivalentFunnel, world, pos))
			return equivalentFunnel.setValue(BeltFunnelBlock.SHAPE,
				BeltFunnelBlock.getShapeForPosition(world, pos, getFacing(state), state.getValue(EXTRACTING)));
		return state;
	}

}
