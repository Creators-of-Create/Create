package com.simibubi.create.content.decoration;

import java.util.function.Predicate;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

public class MetalLadderBlock extends LadderBlock implements IWrenchable {

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public MetalLadderBlock(Properties p_54345_) {
		super(p_54345_);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean supportsExternalFaceHiding(BlockState state) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("deprecation")
	public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pDirection) {
		if (pDirection != null && pDirection.getAxis()
			.isHorizontal())
			return pAdjacentBlockState.isAir() || !pAdjacentBlockState.blocksMotion();
		return pDirection == Direction.UP && pAdjacentBlockState.getBlock() instanceof LadderBlock;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return AllShapes.SIX_VOXEL_POLE.get(Axis.Y);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
								  BlockPos pCurrentPos, BlockPos pFacingPos) {
		if (!pState.canSurvive(pLevel, pCurrentPos))
			return Blocks.AIR.defaultBlockState();
		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		BlockState otherState = pLevel.getBlockState(pPos.relative(Direction.UP));
		return super.canSurvive(pState, pLevel, pPos) ||
			(otherState.is(this) && pState.getValue(FACING).equals(otherState.getValue(FACING)));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
								 BlockHitResult ray) {
		if (player.isShiftKeyDown() || !player.mayBuild())
			return InteractionResult.PASS;
		ItemStack heldItem = player.getItemInHand(hand);
		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		if (helper.matchesItem(heldItem))
			return helper.getOffset(player, world, state, pos, ray)
				.placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);
		return InteractionResult.PASS;
	}

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper implements IPlacementHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return i -> i.getItem() instanceof BlockItem
				&& ((BlockItem) i.getItem()).getBlock() instanceof MetalLadderBlock;
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return s -> s.getBlock() instanceof LadderBlock;
		}

		public int attachedLadders(Level world, BlockPos pos, Direction direction) {
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
			Direction dir = player.getXRot() < 0 ? Direction.UP : Direction.DOWN;

			int range = AllConfigs.server().equipment.placementAssistRange.get();
			if (player != null) {
				AttributeInstance reach = player.getAttribute(ForgeMod.BLOCK_REACH.get());
				if (reach != null && reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier))
					range += 4;
			}

			int ladders = attachedLadders(world, pos, dir);
			if (ladders >= range)
				return PlacementOffset.fail();

			BlockPos newPos = pos.relative(dir, ladders + 1);
			BlockState newState = world.getBlockState(newPos);

			if (!state.canSurvive(world, newPos))
				return PlacementOffset.fail();

			if (newState.canBeReplaced())
				return PlacementOffset.success(newPos, bState -> bState.setValue(FACING, state.getValue(FACING)));
			return PlacementOffset.fail();
		}

	}

}
