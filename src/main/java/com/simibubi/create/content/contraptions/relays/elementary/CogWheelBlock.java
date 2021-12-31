package com.simibubi.create.content.contraptions.relays.elementary;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerBlock;
import com.simibubi.create.content.contraptions.relays.encased.EncasedCogwheelBlock;
import com.simibubi.create.foundation.utility.IAugment;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CogWheelBlock extends AbstractShaftBlock implements ICogWheel, IAugment {

	boolean isLarge;

	protected CogWheelBlock(boolean large, Properties properties) {
		super(properties);
		isLarge = large;
	}

	public static CogWheelBlock small(Properties properties) {
		return new CogWheelBlock(false, properties);
	}

	public static CogWheelBlock large(Properties properties) {
		return new CogWheelBlock(true, properties);
	}

	@Override
	public boolean isLargeCog() {
		return isLarge;
	}

	@Override
	public boolean isSmallCog() {
		return !isLarge;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return (isLarge ? AllShapes.LARGE_GEAR : AllShapes.SMALL_GEAR).get(state.getValue(AXIS));
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return isValidCogwheelPosition(ICogWheel.isLargeCog(state), worldIn, pos, state.getValue(AXIS));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {
		if (player.isShiftKeyDown() || !player.mayBuild())
			return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);
		EncasedCogwheelBlock[] encasedBlocks = isLarge
			? new EncasedCogwheelBlock[] { AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL.get(),
				AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL.get() }
			: new EncasedCogwheelBlock[] { AllBlocks.ANDESITE_ENCASED_COGWHEEL.get(),
				AllBlocks.BRASS_ENCASED_COGWHEEL.get() };

		for (EncasedCogwheelBlock encasedCog : encasedBlocks) {
			if (!encasedCog.getCasing()
				.isIn(heldItem))
				continue;

			if (world.isClientSide)
				return InteractionResult.SUCCESS;

			BlockState encasedState = encasedCog.defaultBlockState()
				.setValue(AXIS, state.getValue(AXIS));

			for (Direction d : Iterate.directionsInAxis(state.getValue(AXIS))) {
				BlockState adjacentState = world.getBlockState(pos.relative(d));
				if (!(adjacentState.getBlock() instanceof IRotate))
					continue;
				IRotate def = (IRotate) adjacentState.getBlock();
				if (!def.hasShaftTowards(world, pos.relative(d), adjacentState, d.getOpposite()))
					continue;
				encasedState =
					encasedState.cycle(d.getAxisDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT
						: EncasedCogwheelBlock.BOTTOM_SHAFT);
			}

			KineticTileEntity.switchToBlockState(world, pos, encasedState);
			playAugmentationSound(world, pos, encasedCog.defaultBlockState());
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	public static boolean isValidCogwheelPosition(boolean large, LevelReader worldIn, BlockPos pos, Axis cogAxis) {
		for (Direction facing : Iterate.directions) {
			if (facing.getAxis() == cogAxis)
				continue;

			BlockPos offsetPos = pos.relative(facing);
			BlockState blockState = worldIn.getBlockState(offsetPos);
			if (blockState.hasProperty(AXIS) && facing.getAxis() == blockState.getValue(AXIS))
				continue;

			if (ICogWheel.isLargeCog(blockState) || large && ICogWheel.isSmallCog(blockState))
				return false;
		}
		return true;
	}

	protected Axis getAxisForPlacement(BlockPlaceContext context) {
		if (context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown())
			return context.getClickedFace()
				.getAxis();

		Level world = context.getLevel();
		BlockState stateBelow = world.getBlockState(context.getClickedPos()
			.below());

		if (AllBlocks.ROTATION_SPEED_CONTROLLER.has(stateBelow) && isLargeCog())
			return stateBelow.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X;

		BlockPos placedOnPos = context.getClickedPos()
			.relative(context.getClickedFace()
				.getOpposite());
		BlockState placedAgainst = world.getBlockState(placedOnPos);

		Block block = placedAgainst.getBlock();
		if (ICogWheel.isSmallCog(placedAgainst))
			return ((IRotate) block).getRotationAxis(placedAgainst);

		Axis preferredAxis = getPreferredAxis(context);
		return preferredAxis != null ? preferredAxis
			: context.getClickedFace()
				.getAxis();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		boolean shouldWaterlog = context.getLevel()
			.getFluidState(context.getClickedPos())
			.getType() == Fluids.WATER;
		return this.defaultBlockState()
			.setValue(AXIS, getAxisForPlacement(context))
			.setValue(BlockStateProperties.WATERLOGGED, shouldWaterlog);
	}

	@Override
	public float getParticleTargetRadius() {
		return isLargeCog() ? 1.125f : .65f;
	}

	@Override
	public float getParticleInitialRadius() {
		return isLargeCog() ? 1f : .75f;
	}

	@Override
	public boolean isDedicatedCogWheel() {
		return true;
	}
}
