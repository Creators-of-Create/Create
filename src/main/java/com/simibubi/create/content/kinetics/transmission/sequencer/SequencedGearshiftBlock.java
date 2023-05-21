package com.simibubi.create.content.kinetics.transmission.sequencer;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.ITransformableBlock;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.gui.ScreenOpener;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class SequencedGearshiftBlock extends HorizontalAxisKineticBlock implements IBE<SequencedGearshiftBlockEntity>, ITransformableBlock {

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
	public boolean shouldCheckWeakPower(BlockState state, LevelReader world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;
		if (!worldIn.getBlockTicks()
			.willTickThisTick(pos, this))
			worldIn.scheduleTick(pos, this, 0);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource r) {
		boolean previouslyPowered = state.getValue(STATE) != 0;
		boolean isPowered = worldIn.hasNeighborSignal(pos);
		withBlockEntityDo(worldIn, pos, sgte -> sgte.onRedstoneUpdate(isPowered, previouslyPowered));
	}

	@Override
	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		return false;
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		if (state.getValue(VERTICAL))
			return face.getAxis()
				.isVertical();
		return super.hasShaftTowards(world, pos, state, face);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		ItemStack held = player.getMainHandItem();
		if (AllItems.WRENCH.isIn(held))
			return InteractionResult.PASS;
		if (held.getItem() instanceof BlockItem) {
			BlockItem blockItem = (BlockItem) held.getItem();
			if (blockItem.getBlock() instanceof KineticBlock && hasShaftTowards(worldIn, pos, state, hit.getDirection()))
				return InteractionResult.PASS;
		}

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
			() -> () -> withBlockEntityDo(worldIn, pos, be -> this.displayScreen(be, player)));
		return InteractionResult.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(SequencedGearshiftBlockEntity be, Player player) {
		if (player instanceof LocalPlayer)
			ScreenOpener.open(new SequencedGearshiftScreen(be));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Axis preferredAxis = RotatedPillarKineticBlock.getPreferredAxis(context);
		if (preferredAxis != null && (context.getPlayer() == null || !context.getPlayer()
			.isShiftKeyDown()))
			return withAxis(preferredAxis, context);
		return withAxis(context.getNearestLookingDirection()
			.getAxis(), context);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		BlockState newState = state;

		if (context.getClickedFace()
			.getAxis() != Axis.Y)
			if (newState.getValue(HORIZONTAL_AXIS) != context.getClickedFace()
				.getAxis())
				newState = newState.cycle(VERTICAL);

		return super.onWrenched(newState, context);
	}

	private BlockState withAxis(Axis axis, BlockPlaceContext context) {
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
	public Class<SequencedGearshiftBlockEntity> getBlockEntityClass() {
		return SequencedGearshiftBlockEntity.class;
	}
	
	@Override
	public BlockEntityType<? extends SequencedGearshiftBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.SEQUENCED_GEARSHIFT.get();
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState p_149740_1_) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		return state.getValue(STATE)
			.intValue();
	}

	@Override
	public BlockState transform(BlockState state, StructureTransform transform) {
		if (transform.mirror != null) {
			state = mirror(state, transform.mirror);
		}

		if (transform.rotationAxis == Direction.Axis.Y) {
			return rotate(state, transform.rotation);
		}

		if (transform.rotation.ordinal() % 2 == 1) {
			if (transform.rotationAxis != state.getValue(HORIZONTAL_AXIS)) {
				return state.cycle(VERTICAL);
			} else if (state.getValue(VERTICAL)) {
				return state.cycle(VERTICAL).cycle(HORIZONTAL_AXIS);
			}
		}
		return state;
	}

}
