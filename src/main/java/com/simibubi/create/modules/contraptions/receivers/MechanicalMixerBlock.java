package com.simibubi.create.modules.contraptions.receivers;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IBlockWithScrollableValue;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.KineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class MechanicalMixerBlock extends KineticBlock
		implements IWithTileEntity<MechanicalMixerTileEntity>, IBlockWithScrollableValue {

	private static final Vec3d valuePos = new Vec3d(15.8f / 16f, 6 / 16f, 5 / 16f);

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
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (context.getEntity() instanceof PlayerEntity)
			return AllShapes.SHORT_CASING_14_VOXEL.get(Direction.DOWN);

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
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return false;
	}

	@Override
	public boolean hasCogsTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis().isHorizontal();
	}

	public static class MechanicalMixerBlockItem extends BlockItem {

		public MechanicalMixerBlockItem(Properties builder) {
			super(AllBlocks.MECHANICAL_MIXER.get(), builder);
		}

		@Override
		public ActionResultType tryPlace(BlockItemUseContext context) {

			BlockPos placedOnPos = context.getPos().offset(context.getFace().getOpposite());
			BlockState placedOnState = context.getWorld().getBlockState(placedOnPos);
			if (AllBlocks.BASIN.typeOf(placedOnState)) {
				if (context.getWorld().getBlockState(placedOnPos.up(2)).getMaterial().isReplaceable())
					context = BlockItemUseContext.func_221536_a(context, placedOnPos.up(2), Direction.UP);
				else
					return ActionResultType.FAIL;
			}

			return super.tryPlace(context);
		}

	}

	@Override
	public String getValueName(BlockState state, IWorld world, BlockPos pos) {
		return Lang.translate("mechanical_mixer.min_ingredients");
	}

	@Override
	public Vec3d getValueBoxPosition(BlockState state, IWorld world, BlockPos pos) {
		return valuePos;
	}

	@Override
	public Direction getValueBoxDirection(BlockState state, IWorld world, BlockPos pos) {
		return null;
	}

	@Override
	public boolean isValueOnMultipleFaces() {
		return true;
	}

	@Override
	public boolean requiresWrench() {
		return true;
	}

	@Override
	public boolean isValueOnFace(Direction face) {
		return face.getAxis().isHorizontal();
	}

	@Override
	public void onScroll(BlockState state, IWorld world, BlockPos pos, double value) {
		withTileEntityDo(world, pos, te -> te.setMinIngredientsLazily((int) (te.currentValue + value)));
	}

	@Override
	public int getCurrentValue(BlockState state, IWorld world, BlockPos pos) {
		MechanicalMixerTileEntity tileEntity = (MechanicalMixerTileEntity) world.getTileEntity(pos);
		if (tileEntity == null)
			return 0;
		return tileEntity.currentValue;
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

}
