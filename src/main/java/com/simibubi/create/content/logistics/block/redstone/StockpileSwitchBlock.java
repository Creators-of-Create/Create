package com.simibubi.create.content.logistics.block.redstone;

import java.util.Random;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class StockpileSwitchBlock extends HorizontalDirectionalBlock
	implements IBE<StockpileSwitchBlockEntity>, IWrenchable {

	public static final IntegerProperty INDICATOR = IntegerProperty.create("indicator", 0, 6);

	public StockpileSwitchBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateObservedInventory(state, worldIn, pos);
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
//		if (world.isClientSide())
//			return;
//		if (!isObserving(state, pos, neighbor))
//			return;
//		updateObservedInventory(state, world, pos);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return AllShapes.STOCKPILE_SWITCH.get(state.getValue(FACING));
	}

	private void updateObservedInventory(BlockState state, LevelReader world, BlockPos pos) {
		withBlockEntityDo(world, pos, StockpileSwitchBlockEntity::updateCurrentLevel);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return side != null && side.getOpposite() != state.getValue(FACING);
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		if (side == blockState.getValue(FACING)
			.getOpposite())
			return 0;
		return getBlockEntityOptional(blockAccess, pos).filter(StockpileSwitchBlockEntity::isPowered)
			.map($ -> 15)
			.orElse(0);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel world, BlockPos pos, Random random) {
		getBlockEntityOptional(world, pos).ifPresent(StockpileSwitchBlockEntity::updatePowerAfterDelay);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, INDICATOR);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (player != null && AllItems.WRENCH.isIn(player.getItemInHand(handIn)))
			return InteractionResult.PASS;
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
			() -> () -> withBlockEntityDo(worldIn, pos, be -> this.displayScreen(be, player)));
		return InteractionResult.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(StockpileSwitchBlockEntity be, Player player) {
		if (player instanceof LocalPlayer)
			ScreenOpener.open(new StockpileSwitchScreen(be));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = defaultBlockState();
		Capability<IItemHandler> itemCap = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		Capability<IFluidHandler> fluidCap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

		Direction preferredFacing = null;
		for (Direction face : Iterate.horizontalDirections) {
			BlockEntity be = context.getLevel()
				.getBlockEntity(context.getClickedPos()
					.relative(face));
			if (be != null && (be.getCapability(itemCap)
				.isPresent()
				|| be.getCapability(fluidCap)
					.isPresent()))
				if (preferredFacing == null)
					preferredFacing = face;
				else {
					preferredFacing = null;
					break;
				}
		}

		if (preferredFacing != null)
			return state.setValue(FACING, preferredFacing);

		Direction facing = context.getClickedFace()
			.getAxis()
			.isHorizontal() ? context.getClickedFace()
				: context.getHorizontalDirection()
					.getOpposite();
		return state.setValue(FACING, context.getPlayer() != null && context.getPlayer()
			.isSteppingCarefully() ? facing.getOpposite() : facing);
	}

	@Override
	public Class<StockpileSwitchBlockEntity> getBlockEntityClass() {
		return StockpileSwitchBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends StockpileSwitchBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.STOCKPILE_SWITCH.get();
	}

}
