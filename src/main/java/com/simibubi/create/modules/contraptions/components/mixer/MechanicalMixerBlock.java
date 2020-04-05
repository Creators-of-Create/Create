package com.simibubi.create.modules.contraptions.components.mixer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IHaveCustomBlockItem;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.base.KineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class MechanicalMixerBlock extends KineticBlock
		implements ITE<MechanicalMixerTileEntity>, IHaveCustomBlockItem {

	public MechanicalMixerBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MechanicalMixerTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return !AllBlocks.BASIN.typeOf(worldIn.getBlockState(pos.down()));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (context.getEntity() instanceof PlayerEntity)
			return AllShapes.CASING_14PX.get(Direction.DOWN);

		return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return false;
	}

	@Override
	public boolean hasIntegratedCogwheel(IWorldReader world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public float getParticleTargetRadius() {
		return .85f;
	}

	@Override
	public float getParticleInitialRadius() {
		return .75f;
	}

	@Override
	public SpeedLevel getMinimumRequiredSpeedLevel() {
		return SpeedLevel.MEDIUM;
	}

	@Override
	public BlockItem getCustomItem(net.minecraft.item.Item.Properties properties) {
		return new BasinOperatorBlockItem(AllBlocks.MECHANICAL_MIXER, properties);
	}

	@Override
	public Class<MechanicalMixerTileEntity> getTileEntityClass() {
		return MechanicalMixerTileEntity.class;
	}

}
