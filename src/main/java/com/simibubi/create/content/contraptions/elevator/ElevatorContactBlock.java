package com.simibubi.create.content.contraptions.elevator;

import java.util.Optional;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.elevator.ElevatorColumn.ColumnCoords;
import com.simibubi.create.content.redstone.contact.RedstoneContactBlock;
import com.simibubi.create.content.redstone.diodes.BrassDiodeBlock;
import com.simibubi.create.content.schematics.requirement.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ElevatorContactBlock extends WrenchableDirectionalBlock
	implements IBE<ElevatorContactBlockEntity>, ISpecialBlockItemRequirement {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty CALLING = BooleanProperty.create("calling");
	public static final BooleanProperty POWERING = BrassDiodeBlock.POWERING;

	public ElevatorContactBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(CALLING, false)
			.setValue(POWERING, false)
			.setValue(POWERED, false)
			.setValue(FACING, Direction.SOUTH));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(CALLING, POWERING, POWERED));
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		InteractionResult onWrenched = super.onWrenched(state, context);
		if (onWrenched != InteractionResult.SUCCESS)
			return onWrenched;

		Level level = context.getLevel();
		if (level.isClientSide())
			return onWrenched;

		BlockPos pos = context.getClickedPos();
		state = level.getBlockState(pos);
		Direction facing = state.getValue(RedstoneContactBlock.FACING);
		if (facing.getAxis() != Axis.Y
			&& ElevatorColumn.get(level, new ColumnCoords(pos.getX(), pos.getZ(), facing)) != null)
			return onWrenched;

		level.setBlockAndUpdate(pos, BlockHelper.copyProperties(state, AllBlocks.REDSTONE_CONTACT.getDefaultState()));

		return onWrenched;
	}
	
	@Nullable
	public static ColumnCoords getColumnCoords(LevelAccessor level, BlockPos pos) {
		BlockState blockState = level.getBlockState(pos);
		if (!AllBlocks.ELEVATOR_CONTACT.has(blockState) && !AllBlocks.REDSTONE_CONTACT.has(blockState))
			return null;
		Direction facing = blockState.getValue(FACING);
		BlockPos target = pos;
		return new ColumnCoords(target.getX(), target.getZ(), facing);
	}

	@Override
	public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos,
		boolean pIsMoving) {
		if (pLevel.isClientSide)
			return;

		boolean isPowered = pState.getValue(POWERED);
		if (isPowered == pLevel.hasNeighborSignal(pPos))
			return;

		pLevel.setBlock(pPos, pState.cycle(POWERED), 2);

		if (isPowered)
			return;
		if (pState.getValue(CALLING))
			return;

		ElevatorColumn elevatorColumn = ElevatorColumn.getOrCreate(pLevel, getColumnCoords(pLevel, pPos));
		callToContactAndUpdate(elevatorColumn, pState, pLevel, pPos, true);
	}

	public void callToContactAndUpdate(ElevatorColumn elevatorColumn, BlockState pState, Level pLevel, BlockPos pPos,
		boolean powered) {
		pLevel.setBlock(pPos, pState.cycle(CALLING), 2);

		for (BlockPos otherPos : elevatorColumn.getContacts()) {
			if (otherPos.equals(pPos))
				continue;
			BlockState otherState = pLevel.getBlockState(otherPos);
			if (!AllBlocks.ELEVATOR_CONTACT.has(otherState))
				continue;
			pLevel.setBlock(otherPos, otherState.setValue(CALLING, false), 2 | 16);
			scheduleActivation(pLevel, otherPos);
		}

		if (powered)
			pState = pState.setValue(POWERED, true);
		pLevel.setBlock(pPos, pState.setValue(CALLING, true), 2);
		pLevel.updateNeighborsAt(pPos, this);

		elevatorColumn.target(pPos.getY());
		elevatorColumn.markDirty();
	}

	public void scheduleActivation(LevelAccessor pLevel, BlockPos pPos) {
		if (!pLevel.getBlockTicks()
			.hasScheduledTick(pPos, this))
			pLevel.scheduleTick(pPos, this, 1);
	}

	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand) {
		boolean wasPowering = pState.getValue(POWERING);

		Optional<ElevatorContactBlockEntity> optionalBE = getBlockEntityOptional(pLevel, pPos);
		boolean shouldBePowering = optionalBE.map(be -> {
			boolean activateBlock = be.activateBlock;
			be.activateBlock = false;
			be.setChanged();
			return activateBlock;
		})
			.orElse(false);

		shouldBePowering |= RedstoneContactBlock.hasValidContact(pLevel, pPos, pState.getValue(FACING));

		if (wasPowering || shouldBePowering)
			pLevel.setBlock(pPos, pState.setValue(POWERING, shouldBePowering), 2 | 16);

		pLevel.updateNeighborsAt(pPos, this);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		if (facing != stateIn.getValue(FACING))
			return stateIn;
		boolean hasValidContact = RedstoneContactBlock.hasValidContact(worldIn, currentPos, facing);
		if (stateIn.getValue(POWERING) != hasValidContact)
			scheduleActivation(worldIn, currentPos);
		return stateIn;
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, LevelReader level, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return state.getValue(POWERING);
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
		return AllBlocks.REDSTONE_CONTACT.asStack();
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
		if (side == null)
			return true;
		return state.getValue(FACING) != side.getOpposite();
	}

	@Override
	public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
		if (side == null)
			return 0;
		BlockState toState = blockAccess.getBlockState(pos.relative(side.getOpposite()));
		if (toState.is(this))
			return 0;
		return state.getValue(POWERING) ? 15 : 0;
	}

	@Override
	public Class<ElevatorContactBlockEntity> getBlockEntityClass() {
		return ElevatorContactBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ElevatorContactBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.ELEVATOR_CONTACT.get();
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
		return ItemRequirement.of(AllBlocks.REDSTONE_CONTACT.getDefaultState(), be);
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
	protected void displayScreen(ElevatorContactBlockEntity be, Player player) {
		if (player instanceof LocalPlayer)
			ScreenOpener
				.open(new ElevatorContactScreen(be.getBlockPos(), be.shortName, be.longName, be.doorControls.mode));
	}

	public static int getLight(BlockState state) {
		return state.getValue(POWERING) ? 10 : 0;
	}

}
