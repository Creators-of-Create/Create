package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class PulleyBlock extends HorizontalAxisKineticBlock implements ITE<PulleyTileEntity> {

	public static EnumProperty<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	
	public PulleyBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ROPE_PULLEY.create();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			if (!worldIn.isRemote) {
				BlockState below = worldIn.getBlockState(pos.down());
				if (below.getBlock() instanceof RopeBlockBase)
					worldIn.destroyBlock(pos.down(), true);
			}
			worldIn.removeTileEntity(pos);
		}
	}

	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (!player.isAllowEdit())
			return ActionResultType.PASS;
		if (player.isSneaking())
			return ActionResultType.PASS;
		if (player.getHeldItem(handIn).isEmpty()) {
			withTileEntityDo(worldIn, pos, te -> te.assembleNextTick = true);
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.PULLEY.get(state.get(HORIZONTAL_AXIS));
	}

	private static void onRopeBroken(World world, BlockPos pulleyPos) {
		TileEntity te = world.getTileEntity(pulleyPos);
		if (!(te instanceof PulleyTileEntity))
			return;
		PulleyTileEntity pulley = (PulleyTileEntity) te;
		pulley.offset = 0;
		pulley.sendData();
	}

	private static class RopeBlockBase extends Block {

		public RopeBlockBase(Properties properties) {
			super(properties);
		}

		@Override
		public PushReaction getPushReaction(BlockState state) {
			return PushReaction.BLOCK;
		}

		@Override
		public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
			PlayerEntity player) {
			return AllBlocks.ROPE_PULLEY.asStack();
		}
		
		@Override
		public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
			if (!isMoving) {
				onRopeBroken(worldIn, pos.up());
				if (!worldIn.isRemote) {
					BlockState above = worldIn.getBlockState(pos.up());
					BlockState below = worldIn.getBlockState(pos.down());
					if (above.getBlock() instanceof RopeBlockBase)
						worldIn.destroyBlock(pos.up(), true);
					if (below.getBlock() instanceof RopeBlockBase)
						worldIn.destroyBlock(pos.down(), true);
				}
			}
			if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
				worldIn.removeTileEntity(pos);
			}
		}

	}

	public static class MagnetBlock extends RopeBlockBase {

		public MagnetBlock(Properties properties) {
			super(properties);
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
			return AllShapes.PULLEY_MAGNET;
		}

	}

	public static class RopeBlock extends RopeBlockBase {

		public RopeBlock(Properties properties) {
			super(properties);
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
			return AllShapes.FOUR_VOXEL_POLE.get(Direction.UP);
		}

	}

	@Override
	public Class<PulleyTileEntity> getTileEntityClass() {
		return PulleyTileEntity.class;
	}

}
