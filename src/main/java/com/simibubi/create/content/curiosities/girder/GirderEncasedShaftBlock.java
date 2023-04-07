package com.simibubi.create.content.curiosities.girder;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GirderEncasedShaftBlock extends HorizontalAxisKineticBlock
	implements ITE<KineticTileEntity>, SimpleWaterloggedBlock, IWrenchable, ISpecialBlockItemRequirement {

	public static final BooleanProperty TOP = GirderBlock.TOP;
	public static final BooleanProperty BOTTOM = GirderBlock.BOTTOM;

	public GirderEncasedShaftBlock(Properties properties) {
		super(properties);
		registerDefaultState(super.defaultBlockState()
				.setValue(WATERLOGGED, false)
				.setValue(TOP, false)
				.setValue(BOTTOM, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(TOP, BOTTOM, WATERLOGGED));
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.GIRDER_BEAM_SHAFT.get(pState.getValue(HORIZONTAL_AXIS));
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
		return Shapes.or(super.getBlockSupportShape(pState, pReader, pPos), AllShapes.EIGHT_VOXEL_POLE.get(Axis.Y));
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		return AllBlocks.METAL_GIRDER.getDefaultState()
			.setValue(WATERLOGGED, originalState.getValue(WATERLOGGED))
			.setValue(GirderBlock.X, originalState.getValue(HORIZONTAL_AXIS) == Axis.Z)
			.setValue(GirderBlock.Z, originalState.getValue(HORIZONTAL_AXIS) == Axis.X)
			.setValue(GirderBlock.AXIS, originalState.getValue(HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X)
			.setValue(GirderBlock.BOTTOM, originalState.getValue(BOTTOM))
			.setValue(GirderBlock.TOP, originalState.getValue(TOP));
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		InteractionResult onWrenched = super.onWrenched(state, context);
		Player player = context.getPlayer();
		if (onWrenched == InteractionResult.SUCCESS && player != null && !player.isCreative())
			player.getInventory()
				.placeItemBackInInventory(AllBlocks.SHAFT.asStack());
		return onWrenched;
	}

	@Override
	public Class<KineticTileEntity> getTileEntityClass() {
		return KineticTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends KineticTileEntity> getTileEntityType() {
		return AllTileEntities.ENCASED_SHAFT.get();
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world,
		BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(WATERLOGGED))
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));

		Property<Boolean> updateProperty = direction == Direction.UP ? TOP : BOTTOM;
		if (direction.getAxis()
			.isVertical()) {
			if (world.getBlockState(pos.relative(direction))
				.getBlockSupportShape(world, pos.relative(direction))
				.isEmpty())
				state = state.setValue(updateProperty, false);
			return GirderBlock.updateVerticalProperty(world, pos, state, updateProperty, neighbourState, direction);
		}

		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		FluidState ifluidstate = level.getFluidState(pos);
		BlockState state = super.getStateForPlacement(context);
		return state.setValue(WATERLOGGED, Boolean.valueOf(ifluidstate.getType() == Fluids.WATER));
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
		return ItemRequirement.of(AllBlocks.SHAFT.getDefaultState(), te)
			.union(ItemRequirement.of(AllBlocks.METAL_GIRDER.getDefaultState(), te));
	}

}
