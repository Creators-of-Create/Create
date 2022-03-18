package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.gui.ScreenOpener;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class StationBlock extends HorizontalDirectionalBlock implements ITE<StationTileEntity> {

	public static final BooleanProperty ASSEMBLING = BooleanProperty.create("assembling");

	public StationBlock(Properties p_54120_) {
		super(p_54120_);
		registerDefaultState(defaultBlockState().setValue(ASSEMBLING, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(FACING, ASSEMBLING));
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState pState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
		return getTileEntityOptional(pLevel, pPos).map(ste -> ste.trainPresent ? 15 : 0)
			.orElse(0);
	}

	@Override
	public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {
		super.fillItemCategory(pTab, pItems);
		pItems.add(AllItems.SCHEDULE.asStack());
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {

		if (pPlayer == null)
			return InteractionResult.PASS;
		ItemStack itemInHand = pPlayer.getItemInHand(pHand);
		if (AllItems.WRENCH.isIn(itemInHand))
			return InteractionResult.PASS;
		if (itemInHand.getItem() == Items.SPONGE)
			Create.RAILWAYS.trains.clear();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
			() -> () -> withTileEntityDo(pLevel, pPos, te -> this.displayScreen(te, pPlayer)));
		return InteractionResult.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(StationTileEntity te, Player player) {
		if (!(player instanceof LocalPlayer))
			return;
		GlobalStation station = te.getStation();
		BlockState blockState = te.getBlockState();
		if (station == null || blockState == null)
			return;
		boolean assembling = blockState.getBlock() == this && blockState.getValue(ASSEMBLING);
		ScreenOpener.open(assembling ? new AssemblyScreen(te, station) : new StationScreen(te, station));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return super.getStateForPlacement(pContext).setValue(FACING, pContext.getHorizontalDirection());
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.STATION.get(pState.getValue(FACING));
	}

	@Override
	public Class<StationTileEntity> getTileEntityClass() {
		return StationTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends StationTileEntity> getTileEntityType() {
		return AllTileEntities.TRACK_STATION.get();
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
