package com.simibubi.create.modules.contraptions.receivers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IRenderUtilityBlock;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.BeltAttachmentState;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.IBeltAttachment;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.TransportedItemStack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class MechanicalPressBlock extends HorizontalKineticBlock
		implements IWithTileEntity<MechanicalPressTileEntity>, IBeltAttachment {

	public static VoxelShape SHAPE = makeCuboidShape(0, 0, 0, 16, 16, 16);

	public MechanicalPressBlock() {
		super(Properties.from(Blocks.PISTON));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;
		MechanicalPressTileEntity te = (MechanicalPressTileEntity) worldIn.getTileEntity(pos);
		if (te == null)
			return;

		if (worldIn.isBlockPowered(pos)) {
			if (!te.finished && !te.running && te.getSpeed() != 0)
				te.start(false);
		} else {
			te.finished = false;
		}

	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MechanicalPressTileEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction prefferedSide = getPreferredHorizontalFacing(context);
		if (prefferedSide != null)
			return getDefaultState().with(HORIZONTAL_FACING, prefferedSide);
		return super.getStateForPlacement(context);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_FACING).getAxis();
	}

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(HORIZONTAL_FACING).getAxis();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		onAttachmentPlaced(worldIn, pos, state);
	}

	@Override
	public List<BlockPos> getPotentialAttachmentPositions(IWorld world, BlockPos pos, BlockState beltState) {
		return Arrays.asList(pos.up(2));
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		onAttachmentRemoved(worldIn, pos, state);
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeTileEntity(pos);
		}
	}

	@Override
	public BlockPos getBeltPositionForAttachment(IWorld world, BlockPos pos, BlockState state) {
		return pos.down(2);
	}

	@Override
	public boolean isAttachedCorrectly(IWorld world, BlockPos attachmentPos, BlockPos beltPos,
			BlockState attachmentState, BlockState beltState) {
		return AllBlocks.BELT.typeOf(beltState) && beltState.get(BeltBlock.SLOPE) == Slope.HORIZONTAL;
	}

	@Override
	public boolean startProcessingItem(BeltTileEntity te, TransportedItemStack transported, BeltAttachmentState state) {
		MechanicalPressTileEntity pressTe = (MechanicalPressTileEntity) te.getWorld()
				.getTileEntity(state.attachmentPos);

		if (pressTe == null || pressTe.getSpeed() == 0)
			return false;
		if (pressTe.running)
			return false;
		if (!pressTe.getRecipe(transported.stack).isPresent())
			return false;

		state.processingDuration = 1;
		pressTe.start(true);
		return true;
	}

	@Override
	public boolean processItem(BeltTileEntity te, TransportedItemStack transportedStack, BeltAttachmentState state) {
		MechanicalPressTileEntity pressTe = (MechanicalPressTileEntity) te.getWorld()
				.getTileEntity(state.attachmentPos);

		// Not powered
		if (pressTe == null || pressTe.getSpeed() == 0)
			return false;

		// Running
		if (pressTe.running) {
			if (pressTe.runningTicks == 30) {
				Optional<PressingRecipe> recipe = pressTe.getRecipe(transportedStack.stack);
				if (!recipe.isPresent())
					return false;
				ItemStack out = recipe.get().getRecipeOutput().copy();
				List<ItemStack> multipliedOutput = ItemHelper.multipliedOutput(transportedStack.stack, out);
				if (multipliedOutput.isEmpty())
					transportedStack.stack = ItemStack.EMPTY;
				transportedStack.stack = multipliedOutput.get(0);

				TileEntity controllerTE = te.getWorld().getTileEntity(te.getController());
				if (controllerTE != null && controllerTE instanceof BeltTileEntity)
					((SyncedTileEntity) controllerTE).sendData();
			}
			return true;
		}

		return false;
	}

	public static class Head extends HorizontalBlock implements IRenderUtilityBlock {

		public Head() {
			super(Properties.from(Blocks.AIR));
		}

		@Override
		protected void fillStateContainer(Builder<Block, BlockState> builder) {
			builder.add(HORIZONTAL_FACING);
			super.fillStateContainer(builder);
		}
	}

}
