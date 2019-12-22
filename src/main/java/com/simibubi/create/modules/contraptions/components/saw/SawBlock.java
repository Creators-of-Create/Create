package com.simibubi.create.modules.contraptions.components.saw;

import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.IHaveMovementBehavior;
import com.simibubi.create.modules.logistics.block.IHaveFilterSlot;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class SawBlock extends DirectionalAxisKineticBlock
		implements IWithTileEntity<SawTileEntity>, IHaveMovementBehavior, IHaveFilterSlot {

	public static final BooleanProperty RUNNING = BooleanProperty.create("running");
	public static DamageSource damageSourceSaw = new DamageSource("create.saw").setDamageBypassesArmor();

	public SawBlock() {
		super(Properties.from(Blocks.ANDESITE));
		setDefaultState(getDefaultState().with(RUNNING, false));
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		return handleActivatedFilterSlots(state, worldIn, pos, player, handIn, hit);
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
		return AllShapes.SHORT_CASING_12_VOXEL.get(state.get(FACING));
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
			entityIn.attackEntityFrom(damageSourceSaw, MathHelper.clamp(Math.abs(te.speed / 512f) + 1, 0, 20));
		});
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		if (!(entityIn instanceof ItemEntity))
			return;
		withTileEntityDo(entityIn.world, entityIn.getPosition(), te -> {
			te.insertItem((ItemEntity) entityIn);
		});
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
	}

	public boolean isHorizontal(BlockState state) {
		return state.get(FACING).getAxis().isHorizontal();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return isHorizontal(state) ? state.get(FACING).getAxis() : super.getRotationAxis(state);
	}

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return isHorizontal(state) ? face == state.get(FACING).getOpposite()
				: super.hasShaftTowards(world, pos, state, face);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (worldIn.getTileEntity(pos) == null)
			return;

		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			withTileEntityDo(worldIn, pos, te -> {
				for (int slot = 0; slot < te.inventory.getSizeInventory(); slot++) {
					InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(),
							te.inventory.getStackInSlot(slot));
				}
			});

			worldIn.removeTileEntity(pos);
		}

	}

	@Override
	public Vec3d getFilterPosition(BlockState state) {
		Vec3d x = new Vec3d(8f / 16f, 12.5f / 16f + 1f / 256f, 12.25f / 16f);
		Vec3d z = new Vec3d(12.25f / 16f, 12.5f / 16f + 1f / 256f, 8f / 16f);
		return state.get(AXIS_ALONG_FIRST_COORDINATE) ? z : x;
	}

	@Override
	public float getFilterAngle(BlockState state) {
		return 0;
	}

	@Override
	public boolean isFilterVisible(BlockState state) {
		return state.get(FACING) == Direction.UP;
	}

	@Override
	public Direction getFilterFacing(BlockState state) {
		return state.get(AXIS_ALONG_FIRST_COORDINATE) ? Direction.EAST : Direction.SOUTH;
	}

}
