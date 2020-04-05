package com.simibubi.create.modules.contraptions.components.saw;

import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.modules.contraptions.components.actors.SawMovementBehaviour;
import com.simibubi.create.modules.contraptions.components.contraptions.IPortableBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementBehaviour;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class SawBlock extends DirectionalAxisKineticBlock implements ITE<SawTileEntity>, IPortableBlock {

	public static final BooleanProperty RUNNING = BooleanProperty.create("running");
	public static DamageSource damageSourceSaw = new DamageSource("create.saw").setDamageBypassesArmor();
	public static MovementBehaviour MOVEMENT = new SawMovementBehaviour();

	public SawBlock() {
		super(Properties.from(Blocks.ANDESITE));
		setDefaultState(getDefaultState().with(RUNNING, false));
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState stateForPlacement = super.getStateForPlacement(context);
		Direction facing = stateForPlacement.get(FACING);
		if (facing.getAxis().isVertical())
			return stateForPlacement;
		return stateForPlacement.with(AXIS_ALONG_FIRST_COORDINATE, facing.getAxis() == Axis.X);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(RUNNING);
		super.fillStateContainer(builder);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SawTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.CASING_12PX.get(state.get(FACING));
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (entityIn instanceof ItemEntity)
			return;
		if (!new AxisAlignedBB(pos).shrink(.1f).intersects(entityIn.getBoundingBox()))
			return;
		withTileEntityDo(worldIn, pos, te -> {
			if (te.getSpeed() == 0)
				return;
			entityIn.attackEntityFrom(damageSourceSaw, MathHelper.clamp(Math.abs(te.getSpeed() / 32f) + 1, 0, 20));
		});
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		if (!(entityIn instanceof ItemEntity))
			return;
		if (entityIn.world.isRemote)
			return;

		BlockPos pos = entityIn.getPosition();
		withTileEntityDo(entityIn.world, pos, te -> {
			if (te.getSpeed() == 0)
				return;
			te.insertItem((ItemEntity) entityIn);
		});
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	public static boolean isHorizontal(BlockState state) {
		return state.get(FACING).getAxis().isHorizontal();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return isHorizontal(state) ? state.get(FACING).getAxis() : super.getRotationAxis(state);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return isHorizontal(state) ? face == state.get(FACING).getOpposite()
				: super.hasShaftTowards(world, pos, state, face);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.getBlock() == newState.getBlock())
			return;

		withTileEntityDo(worldIn, pos, te -> ItemHelper.dropContents(worldIn, pos, te.inventory));
		TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
		worldIn.removeTileEntity(pos);
	}

	@Override
	public MovementBehaviour getMovementBehaviour() {
		return MOVEMENT;
	}

	@Override
	public Class<SawTileEntity> getTileEntityClass() {
		return SawTileEntity.class;
	}

}
