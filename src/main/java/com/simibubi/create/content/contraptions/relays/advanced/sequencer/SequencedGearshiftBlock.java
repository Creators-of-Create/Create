package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.Random;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.gui.ScreenOpener;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import net.minecraft.block.AbstractBlock.Properties;

public class SequencedGearshiftBlock extends HorizontalAxisKineticBlock implements ITE<SequencedGearshiftTileEntity> {

	public static final BooleanProperty VERTICAL = BooleanProperty.create("vertical");
	public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 5);

	public SequencedGearshiftBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(STATE, VERTICAL));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.SEQUENCED_GEARSHIFT.create();
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;
		if (!worldIn.getBlockTicks()
			.willTickThisTick(pos, this))
			worldIn.getBlockTicks()
				.scheduleTick(pos, this, 0);
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random r) {
		boolean previouslyPowered = state.getValue(STATE) != 0;
		boolean isPowered = worldIn.hasNeighborSignal(pos);
		withTileEntityDo(worldIn, pos, sgte -> sgte.onRedstoneUpdate(isPowered, previouslyPowered));
	}

	@Override
	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		return false;
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		if (state.getValue(VERTICAL))
			return face.getAxis()
				.isVertical();
		return super.hasShaftTowards(world, pos, state, face);
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		ItemStack held = player.getMainHandItem();
		if (AllItems.WRENCH.isIn(held))
			return ActionResultType.PASS;
		if (held.getItem() instanceof BlockItem) {
			BlockItem blockItem = (BlockItem) held.getItem();
			if (blockItem.getBlock() instanceof KineticBlock && hasShaftTowards(worldIn, pos, state, hit.getDirection()))
				return ActionResultType.PASS;
		}

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
			() -> () -> withTileEntityDo(worldIn, pos, te -> this.displayScreen(te, player)));
		return ActionResultType.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(SequencedGearshiftTileEntity te, PlayerEntity player) {
		if (player instanceof ClientPlayerEntity)
			ScreenOpener.open(new SequencedGearshiftScreen(te));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Axis preferredAxis = RotatedPillarKineticBlock.getPreferredAxis(context);
		if (preferredAxis != null && (context.getPlayer() == null || !context.getPlayer()
			.isShiftKeyDown()))
			return withAxis(preferredAxis, context);
		return withAxis(context.getNearestLookingDirection()
			.getAxis(), context);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		BlockState newState = state;

		if (context.getClickedFace()
			.getAxis() != Axis.Y)
			if (newState.getValue(HORIZONTAL_AXIS) != context.getClickedFace()
				.getAxis())
				newState = newState.cycle(VERTICAL);

		return super.onWrenched(newState, context);
	}

	private BlockState withAxis(Axis axis, BlockItemUseContext context) {
		BlockState state = defaultBlockState().setValue(VERTICAL, axis.isVertical());
		if (axis.isVertical())
			return state.setValue(HORIZONTAL_AXIS, context.getHorizontalDirection()
				.getAxis());
		return state.setValue(HORIZONTAL_AXIS, axis);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		if (state.getValue(VERTICAL))
			return Axis.Y;
		return super.getRotationAxis(state);
	}

	@Override
	public Class<SequencedGearshiftTileEntity> getTileEntityClass() {
		return SequencedGearshiftTileEntity.class;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState p_149740_1_) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos) {
		return state.getValue(STATE)
			.intValue();
	}

}
