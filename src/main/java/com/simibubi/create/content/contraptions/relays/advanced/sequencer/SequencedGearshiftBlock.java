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

public class SequencedGearshiftBlock extends HorizontalAxisKineticBlock implements ITE<SequencedGearshiftTileEntity> {

	public static final BooleanProperty VERTICAL = BooleanProperty.create("vertical");
	public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 5);

	public SequencedGearshiftBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(STATE, VERTICAL));
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
		if (worldIn.isRemote)
			return;
		if (!worldIn.getPendingBlockTicks()
			.isTickPending(pos, this))
			worldIn.getPendingBlockTicks()
				.scheduleTick(pos, this, 0);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random r) {
		boolean previouslyPowered = state.get(STATE) != 0;
		boolean isPowered = worldIn.isBlockPowered(pos);
		withTileEntityDo(worldIn, pos, sgte -> sgte.onRedstoneUpdate(isPowered, previouslyPowered));
	}

	@Override
	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		return false;
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		if (state.get(VERTICAL))
			return face.getAxis()
				.isVertical();
		return super.hasShaftTowards(world, pos, state, face);
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		ItemStack held = player.getHeldItemMainhand();
		if (AllItems.WRENCH.isIn(held))
			return ActionResultType.PASS;
		if (held.getItem() instanceof BlockItem) {
			BlockItem blockItem = (BlockItem) held.getItem();
			if (blockItem.getBlock() instanceof KineticBlock && hasShaftTowards(worldIn, pos, state, hit.getFace()))
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
			.isSneaking()))
			return withAxis(preferredAxis, context);
		return withAxis(context.getNearestLookingDirection()
			.getAxis(), context);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		BlockState newState = state;

		if (context.getFace()
			.getAxis() != Axis.Y)
			if (newState.get(HORIZONTAL_AXIS) != context.getFace()
				.getAxis())
				newState = newState.cycle(VERTICAL);

		return super.onWrenched(newState, context);
	}

	private BlockState withAxis(Axis axis, BlockItemUseContext context) {
		BlockState state = getDefaultState().with(VERTICAL, axis.isVertical());
		if (axis.isVertical())
			return state.with(HORIZONTAL_AXIS, context.getPlacementHorizontalFacing()
				.getAxis());
		return state.with(HORIZONTAL_AXIS, axis);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		if (state.get(VERTICAL))
			return Axis.Y;
		return super.getRotationAxis(state);
	}

	@Override
	public Class<SequencedGearshiftTileEntity> getTileEntityClass() {
		return SequencedGearshiftTileEntity.class;
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState p_149740_1_) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
		return state.get(STATE)
			.intValue();
	}

}
