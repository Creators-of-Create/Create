package com.simibubi.create.content.contraptions.relays.encased;

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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EncasedCogwheelBlock extends RotatedPillarKineticBlock
	implements ICogWheel, ITE<SimpleKineticTileEntity>, ISpecialBlockItemRequirement {

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
	}

	@Override
	public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {}
	
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
		return pState == pAdjacentBlockState;
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
		return false;
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
