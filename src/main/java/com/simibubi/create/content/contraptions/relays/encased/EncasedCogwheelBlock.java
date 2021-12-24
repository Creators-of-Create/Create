package com.simibubi.create.content.contraptions.relays.encased;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.CasingBlock;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.content.contraptions.relays.elementary.SimpleKineticTileEntity;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.block.ITE;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class EncasedCogwheelBlock extends RotatedPillarKineticBlock
	implements ICogWheel, ITE<SimpleKineticTileEntity>, ISpecialBlockItemRequirement, BlockPickInteractionAware {

	public static final BooleanProperty TOP_SHAFT = BooleanProperty.create("top_shaft");
	public static final BooleanProperty BOTTOM_SHAFT = BooleanProperty.create("bottom_shaft");

	boolean isLarge;
	private BlockEntry<CasingBlock> casing;

	public static EncasedCogwheelBlock andesite(boolean large, Properties properties) {
		return new EncasedCogwheelBlock(large, properties, AllBlocks.ANDESITE_CASING);
	}

	public static EncasedCogwheelBlock brass(boolean large, Properties properties) {
		return new EncasedCogwheelBlock(large, properties, AllBlocks.BRASS_CASING);
	}

	public EncasedCogwheelBlock(boolean large, Properties properties, BlockEntry<CasingBlock> casing) {
		super(properties);
		isLarge = large;
		this.casing = casing;
		registerDefaultState(defaultBlockState().setValue(TOP_SHAFT, false)
			.setValue(BOTTOM_SHAFT, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(TOP_SHAFT, BOTTOM_SHAFT));
	}

	@Override
	public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {}

	@Override
	public ItemStack getPickedStack(BlockState state, BlockGetter view, BlockPos pos, @Nullable Player player, @Nullable HitResult target) {
		if (target instanceof BlockHitResult)
			return ((BlockHitResult) target).getDirection()
				.getAxis() != getRotationAxis(state)
					? isLarge ? AllBlocks.LARGE_COGWHEEL.asStack() : AllBlocks.COGWHEEL.asStack()
					: getCasing().asStack();
		return ItemStack.EMPTY;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState placedOn = context.getLevel()
			.getBlockState(context.getClickedPos()
				.relative(context.getClickedFace()
					.getOpposite()));
		BlockState stateForPlacement = super.getStateForPlacement(context);
		if (ICogWheel.isSmallCog(placedOn))
			stateForPlacement =
				stateForPlacement.setValue(AXIS, ((IRotate) placedOn.getBlock()).getRotationAxis(placedOn));
		return stateForPlacement;
	}

	public BlockEntry<CasingBlock> getCasing() {
		return casing;
	}

	@Override
	public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pDirection) {
		return pState.getBlock() == pAdjacentBlockState.getBlock()
			&& pState.getValue(AXIS) == pAdjacentBlockState.getValue(AXIS);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (context.getClickedFace()
			.getAxis() != state.getValue(AXIS))
			return super.onWrenched(state, context);

		Level level = context.getLevel();
		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		BlockPos pos = context.getClickedPos();
		KineticTileEntity.switchToBlockState(level, pos, state.cycle(context.getClickedFace()
			.getAxisDirection() == AxisDirection.POSITIVE ? TOP_SHAFT : BOTTOM_SHAFT));
		playRotateSound(level, pos);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		if (context.getLevel().isClientSide)
			return InteractionResult.SUCCESS;
		context.getLevel()
			.levelEvent(2001, context.getClickedPos(), Block.getId(state));
		KineticTileEntity.switchToBlockState(context.getLevel(), context.getClickedPos(),
			(isLarge ? AllBlocks.LARGE_COGWHEEL : AllBlocks.COGWHEEL).getDefaultState()
				.setValue(AXIS, state.getValue(AXIS)));
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.getValue(AXIS)
			&& state.getValue(face.getAxisDirection() == AxisDirection.POSITIVE ? TOP_SHAFT : BOTTOM_SHAFT);
	}

	@Override
	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		if (newState.getBlock() instanceof EncasedCogwheelBlock
			&& oldState.getBlock() instanceof EncasedCogwheelBlock) {
			if (newState.getValue(TOP_SHAFT) != oldState.getValue(TOP_SHAFT))
				return false;
			if (newState.getValue(BOTTOM_SHAFT) != oldState.getValue(BOTTOM_SHAFT))
				return false;
		}
		return super.areStatesKineticallyEquivalent(oldState, newState);
	}

	@Override
	public boolean isSmallCog() {
		return !isLarge;
	}

	@Override
	public boolean isLargeCog() {
		return isLarge;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return CogWheelBlock.isValidCogwheelPosition(ICogWheel.isLargeCog(state), worldIn, pos, state.getValue(AXIS));
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(AXIS);
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
		return ItemRequirement
			.of(isLarge ? AllBlocks.LARGE_COGWHEEL.getDefaultState() : AllBlocks.COGWHEEL.getDefaultState(), te);
	}

	@Override
	public Class<SimpleKineticTileEntity> getTileEntityClass() {
		return SimpleKineticTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends SimpleKineticTileEntity> getTileEntityType() {
		return isLarge ? AllTileEntities.ENCASED_LARGE_COGWHEEL.get() : AllTileEntities.ENCASED_COGWHEEL.get();
	}

}
