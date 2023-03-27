package com.simibubi.create.content.contraptions.components.actors;

import java.util.function.Predicate;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.curiosities.tools.ExtendoGripItem;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;

public class RollerBlock extends AttachedActorBlock implements IBE<RollerBlockEntity> {

	public static DamageSource damageSourceRoller = new DamageSource("create.mechanical_roller");

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public RollerBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withWater(defaultBlockState().setValue(FACING, context.getHorizontalDirection()
			.getOpposite()), context);
	}

	@Override
	public Class<RollerBlockEntity> getBlockEntityClass() {
		return RollerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends RollerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.MECHANICAL_ROLLER.get();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(FACING);
		return AllShapes.ROLLER_BASE.get(direction);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return true;
	}
	
	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		withBlockEntityDo(pLevel, pPos, RollerBlockEntity::searchForSharedValues);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {
		ItemStack heldItem = player.getItemInHand(hand);

		IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
		if (!player.isShiftKeyDown() && player.mayBuild()) {
			if (placementHelper.matchesItem(heldItem)) {
				placementHelper.getOffset(player, world, state, pos, ray)
					.placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	private static class PlacementHelper implements IPlacementHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return AllBlocks.MECHANICAL_ROLLER::isIn;
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return AllBlocks.MECHANICAL_ROLLER::has;
		}

		public int attachedSteps(Level world, BlockPos pos, Direction direction) {
			BlockPos checkPos = pos.relative(direction);
			BlockState state = world.getBlockState(checkPos);
			int count = 0;
			while (getStatePredicate().test(state)) {
				count++;
				checkPos = checkPos.relative(direction);
				state = world.getBlockState(checkPos);
			}
			return count;
		}

		@Override
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
			BlockHitResult ray) {

			Direction dir = null;
			Direction facing = state.getValue(FACING);

			for (Direction nearest : Direction.orderedByNearest(player)) {
				if (nearest.getAxis() != facing.getClockWise()
					.getAxis())
					continue;
				dir = nearest;
				break;
			}

			int range = AllConfigs.server().curiosities.placementAssistRange.get();
			if (player != null) {
				AttributeInstance reach = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
				if (reach != null && reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier))
					range += 4;
			}

			int row = attachedSteps(world, pos, dir);
			if (row >= range)
				return PlacementOffset.fail();

			BlockPos newPos = pos.relative(dir, row + 1);
			BlockState newState = world.getBlockState(newPos);

			if (!state.canSurvive(world, newPos))
				return PlacementOffset.fail();

			if (newState.getMaterial()
				.isReplaceable())
				return PlacementOffset.success(newPos, bState -> bState.setValue(FACING, facing));
			return PlacementOffset.fail();
		}

	}

}
