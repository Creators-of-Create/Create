package com.simibubi.create.content.logistics.packagerLink;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PackagerLinkBlock extends FaceAttachedHorizontalDirectionalBlock
	implements IBE<PackagerLinkBlockEntity>, ProperWaterloggedBlock, IWrenchable {
	public static final MapCodec<PackagerLinkBlock> CODEC = simpleCodec(PackagerLinkBlock::new);

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public PackagerLinkBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(WATERLOGGED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		BlockState placed = super.getStateForPlacement(context);
		if (placed == null)
			return null;
		if (placed.getValue(FACE) == AttachFace.CEILING)
			placed = placed.setValue(FACING, placed.getValue(FACING)
				.getOpposite());
		return withWater(placed.setValue(POWERED, getPower(placed, context.getLevel(), pos) > 0), context);
	}

	public static Direction getConnectedDirection(BlockState state) {
		return FaceAttachedHorizontalDirectionalBlock.getConnectedDirection(state);
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		return true;
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
		updateWater(pLevel, pState, pPos);
		return pState;
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		int power = getPower(state, worldIn, pos);
		boolean powered = power > 0;
		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != powered)
			worldIn.setBlock(pos, state.cycle(POWERED), Block.UPDATE_CLIENTS);
		withBlockEntityDo(worldIn, pos, link -> link.behaviour.redstonePowerChanged(power));
	}

	public static int getPower(BlockState state, Level worldIn, BlockPos pos) {
		int power = 0;
		for (Direction d : Iterate.directions)
			if (d.getOpposite() != getConnectedDirection(state))
				power = Math.max(power, worldIn.getSignal(pos.relative(d), d));
		return power;
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		withBlockEntityDo(pLevel, pPos, plbe -> {
			if (pPlacer instanceof Player player) {
				plbe.placedBy = player.getUUID();
				plbe.notifyUpdate();
			}
		});
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.STOCK_LINK.get(getConnectedDirection(pState));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED, WATERLOGGED, FACE, FACING));
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public Class<PackagerLinkBlockEntity> getBlockEntityClass() {
		return PackagerLinkBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends PackagerLinkBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.PACKAGER_LINK.get();
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
		IBE.onRemove(pState, pLevel, pPos, pNewState);
	}

	@Override
	protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
		return CODEC;
	}
}
