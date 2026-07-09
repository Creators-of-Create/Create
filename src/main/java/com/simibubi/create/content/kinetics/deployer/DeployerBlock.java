package com.simibubi.create.content.kinetics.deployer;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.processing.AssemblyOperatorUseContext;
import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.api.placement.IPlacementHelper;
import net.createmod.catnip.api.placement.PlacementHelpers;
import net.createmod.catnip.api.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@ParametersAreNonnullByDefault
public class DeployerBlock extends DirectionalAxisKineticBlock implements IBE<DeployerBlockEntity> {

	private static final IPlacementHelper PLACEMENT_HELPER = PlacementHelpers.register(new PlacementHelper());

	public DeployerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.DEPLOYER_INTERACTION.get(state.getValue(FACING));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.CASING_12PX.get(state.getValue(FACING));
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Vec3 normal = Vec3.atLowerCornerOf(state.getValue(FACING)
			.getUnitVec3i());
		Vec3 location = context.getClickLocation()
			.subtract(Vec3.atCenterOf(context.getClickedPos())
				.subtract(normal.scale(.5)))
			.multiply(normal);
		if (location.length() > .75f) {
			if (!context.getLevel().isClientSide())
				withBlockEntityDo(context.getLevel(), context.getClickedPos(), DeployerBlockEntity::changeMode);
			return InteractionResult.SUCCESS;
		}
		return super.onWrenched(state, context);
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(worldIn, pos, state, placer, stack);
		if (placer instanceof ServerPlayer)
			withBlockEntityDo(worldIn, pos, dbe -> dbe.owner = placer.getUUID());
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel worldIn, BlockPos pos, boolean isMoving) {
		if (!isMoving)
			withBlockEntityDo(worldIn, pos, DeployerBlockEntity::discardPlayer);
		super.affectNeighborsAfterRemoval(state, worldIn, pos, isMoving);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		ItemStack heldByPlayer = stack.copy();

		IPlacementHelper placementHelper = PLACEMENT_HELPER;
		if (!player.isShiftKeyDown() && player.mayBuild()) {
			if (placementHelper.matchesItem(heldByPlayer) && placementHelper.getOffset(player, level, state, pos, hitResult)
				.placeInWorld(level, (BlockItem) heldByPlayer.getItem(), player, hand, hitResult)
				.consumesAction())
				return InteractionResult.SUCCESS;
		}

		if (AllItems.WRENCH.isIn(heldByPlayer))
			return InteractionResult.TRY_WITH_EMPTY_HAND;

		Vec3 normal = Vec3.atLowerCornerOf(state.getValue(FACING)
			.getUnitVec3i());
		Vec3 location = hitResult.getLocation()
			.subtract(Vec3.atCenterOf(pos)
				.subtract(normal.scale(.5)))
			.multiply(normal);
		if (location.length() < .75f)
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (level.isClientSide())
			return InteractionResult.SUCCESS;

		withBlockEntityDo(level, pos, be -> {
			ItemStack heldByDeployer = be.player.getMainHandItem()
				.copy();
			if (heldByDeployer.isEmpty() && heldByPlayer.isEmpty())
				return;

			player.setItemInHand(hand, heldByDeployer);
			be.player.setItemInHand(InteractionHand.MAIN_HAND, heldByPlayer);
			be.notifyUpdate();
		});

		return InteractionResult.SUCCESS;
	}

	@Override
	public Class<DeployerBlockEntity> getBlockEntityClass() {
		return DeployerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends DeployerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.DEPLOYER.get();
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, world, pos, oldState, isMoving);
		withBlockEntityDo(world, pos, DeployerBlockEntity::redstoneUpdate);
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, Orientation orientation, boolean p_220069_6_) {
		withBlockEntityDo(world, pos, DeployerBlockEntity::redstoneUpdate);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected Direction getFacingForPlacement(BlockPlaceContext context) {
		if (context instanceof AssemblyOperatorUseContext)
			return Direction.DOWN;
		else
			return super.getFacingForPlacement(context);
	}

		private static class PlacementHelper implements IPlacementHelper {

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return AllBlocks.DEPLOYER::isIn;
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return AllBlocks.DEPLOYER::has;
		}

		@Override
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
										 BlockHitResult ray) {
			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(),
				state.getValue(FACING)
					.getAxis(),
				dir -> world.getBlockState(pos.relative(dir))
					.canBeReplaced());

			if (directions.isEmpty())
				return PlacementOffset.fail();
			else {
				return PlacementOffset.success(pos.relative(directions.get(0)),
					s -> s.setValue(FACING, state.getValue(FACING))
						.setValue(AXIS_ALONG_FIRST_COORDINATE, state.getValue(AXIS_ALONG_FIRST_COORDINATE)));
			}
		}

	}

}
