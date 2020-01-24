package com.simibubi.create.modules.logistics.block.inventories;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.AllShapes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class FlexcrateBlock extends ProperDirectionalBlock {

	public static final BooleanProperty DOUBLE = BooleanProperty.create("double");

	public FlexcrateBlock() {
		super(Properties.from(Blocks.ANDESITE));
		setDefaultState(getDefaultState().with(FACING, Direction.UP).with(DOUBLE, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.CRATE_BLOCK_SHAPE;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(DOUBLE));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos pos = context.getPos();
		World world = context.getWorld();

		if (!context.isPlacerSneaking()) {
			for (Direction d : Direction.values()) {
				BlockState state = world.getBlockState(pos.offset(d));
				if (AllBlocks.FLEXCRATE.typeOf(state) && !state.get(DOUBLE))
					return getDefaultState().with(FACING, d).with(DOUBLE, true);
			}
		}

		Direction placedOnFace = context.getFace().getOpposite();
		BlockState state = world.getBlockState(pos.offset(placedOnFace));
		if (AllBlocks.FLEXCRATE.typeOf(state) && !state.get(DOUBLE))
			return getDefaultState().with(FACING, placedOnFace).with(DOUBLE, true);
		return getDefaultState();
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (oldState.getBlock() != state.getBlock() && state.hasTileEntity() && state.get(DOUBLE)
				&& state.get(FACING).getAxisDirection() == AxisDirection.POSITIVE) {
			FlexcrateTileEntity te = (FlexcrateTileEntity) worldIn.getTileEntity(pos);
			FlexcrateTileEntity other = te.getOtherCrate();
			if (other == null)
				return;
			for (int slot = 0; slot < other.inventory.getSlots(); slot++) {
				te.inventory.setStackInSlot(slot, other.inventory.getStackInSlot(slot));
				other.inventory.setStackInSlot(slot, ItemStack.EMPTY);
			}
			te.allowedAmount = other.allowedAmount;
		}
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {

		boolean isDouble = stateIn.get(DOUBLE);
		Direction blockFacing = stateIn.get(FACING);
		boolean isFacingOther = AllBlocks.FLEXCRATE.typeOf(facingState) && facingState.get(DOUBLE)
				&& facingState.get(FACING) == facing.getOpposite();

		if (!isDouble) {
			if (!isFacingOther)
				return stateIn;
			return stateIn.with(DOUBLE, true).with(FACING, facing);
		}

		if (facing != blockFacing)
			return stateIn;
		if (!isFacingOther)
			return stateIn.with(DOUBLE, false);

		return stateIn;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {

		if (worldIn.isRemote) {
			return true;
		} else {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof FlexcrateTileEntity) {
				FlexcrateTileEntity fte = (FlexcrateTileEntity) te;
				fte = fte.getMainCrate();
				NetworkHooks.openGui((ServerPlayerEntity) player, fte, fte::sendToContainer);
			}
			return true;
		}
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new FlexcrateTileEntity();
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (worldIn.getTileEntity(pos) == null)
			return;

		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			FlexcrateTileEntity te = (FlexcrateTileEntity) worldIn.getTileEntity(pos);
			te.onDestroyed();
			worldIn.removeTileEntity(pos);
		}

	}

}
