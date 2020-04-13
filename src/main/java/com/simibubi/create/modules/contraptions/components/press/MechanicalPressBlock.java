package com.simibubi.create.modules.contraptions.components.press;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.modules.contraptions.components.press.MechanicalPressTileEntity.Mode;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.BeltAttachmentState;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.IBeltAttachment;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.transport.TransportedItemStack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class MechanicalPressBlock extends HorizontalKineticBlock
		implements ITE<MechanicalPressTileEntity>, IBeltAttachment {

	public MechanicalPressBlock() {
		super(Properties.from(Blocks.PISTON));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (context.getEntity() instanceof PlayerEntity)
			return AllShapes.CASING_14PX.get(Direction.DOWN);

		return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return !AllBlocks.BASIN.typeOf(worldIn.getBlockState(pos.down()));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		withTileEntityDo(worldIn, pos, te -> {
			if (!worldIn.isBlockPowered(pos)) {
				te.finished = false;
				return;
			}
			if (!te.finished && !te.running && te.getSpeed() != 0)
				te.start(Mode.WORLD);
		});
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
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
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
	public boolean startProcessingItem(BeltTileEntity belt, TransportedItemStack transported,
			BeltAttachmentState state) {
		try {
			MechanicalPressTileEntity pressTe = getTileEntity(belt.getWorld(), state.attachmentPos);
			if (pressTe.getSpeed() == 0)
				return false;
			if (pressTe.running)
				return false;
			if (!pressTe.getRecipe(transported.stack).isPresent())
				return false;

			state.processingDuration = 1;
			pressTe.start(Mode.BELT);
			return true;

		} catch (TileEntityException e) {}
		return false;
	}

	@Override
	public boolean processItem(BeltTileEntity belt, TransportedItemStack transportedStack, BeltAttachmentState state) {
		try {
			MechanicalPressTileEntity pressTe = getTileEntity(belt.getWorld(), state.attachmentPos);

			// Not powered
			if (pressTe.getSpeed() == 0)
				return false;

			// Running
			if (!pressTe.running)
				return false;
			if (pressTe.runningTicks != 30)
				return true;

			Optional<PressingRecipe> recipe = pressTe.getRecipe(transportedStack.stack);

			pressTe.pressedItems.clear();
			pressTe.pressedItems.add(transportedStack.stack);

			if (!recipe.isPresent())
				return false;

			ItemStack out = recipe.get().getRecipeOutput().copy();
			List<ItemStack> multipliedOutput = ItemHelper.multipliedOutput(transportedStack.stack, out);
			if (multipliedOutput.isEmpty())
				transportedStack.stack = ItemStack.EMPTY;
			transportedStack.stack = multipliedOutput.get(0);

			BeltTileEntity controllerTE = belt.getControllerTE();
			if (controllerTE != null)
				controllerTE.sendData();
			pressTe.sendData();
			return true;

		} catch (TileEntityException e) {}

		return false;
	}

	@Override
	public Class<MechanicalPressTileEntity> getTileEntityClass() {
		return MechanicalPressTileEntity.class;
	}

}
