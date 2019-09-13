package com.simibubi.create.modules.contraptions.receivers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IRenderUtilityBlock;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.BeltAttachmentState;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.IBeltAttachment;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItemUseContext;
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

	public static VoxelShape SHAPE = makeCuboidShape(0, 2, 0, 16, 16, 16);

	public MechanicalPressBlock() {
		super(Properties.from(Blocks.PISTON));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Red;
		return new ItemDescription(color).withSummary("Applies pressure to items below it.").createTabs();
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
			if (!te.finished && !te.running)
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

	@Override
	public List<BlockPos> getPotentialAttachmentLocations(BeltTileEntity te) {
		return Arrays.asList(te.getPos().up(2));
	}

	@Override
	public Optional<BlockPos> getValidBeltPositionFor(IWorld world, BlockPos pos, BlockState state) {
		BlockState blockState = world.getBlockState(pos.down(2));
		if (!AllBlocks.BELT.typeOf(blockState) || blockState.get(BeltBlock.SLOPE) != Slope.HORIZONTAL)
			return Optional.empty();
		return Optional.of(pos.down(2));
	}

	@Override
	public boolean handleEntity(BeltTileEntity te, Entity entity, BeltAttachmentState state) {
		MechanicalPressTileEntity pressTe = (MechanicalPressTileEntity) te.getWorld()
				.getTileEntity(state.attachmentPos);
		
		// Not powered
		if (pressTe == null || pressTe.getSpeed() == 0)
			return false;

		// Running
		if (pressTe.running) {
			double distanceTo = entity.getPositionVec().distanceTo(VecHelper.getCenterOf(te.getPos()));
			if (distanceTo < .32f)
				return true;
			if (distanceTo < .4f) {
				entity.setPosition(te.getPos().getX() + .5f, entity.posY, te.getPos().getZ() + .5f);
				return true;
			}
			return false;
		}
		
		// Start process
		if (state.processingEntity != entity) {
			state.processingEntity = entity;
			state.processingDuration = 1;
			pressTe.start(true);
			return false;
		}
		
		// Already processed
		if (state.processingDuration == -1)
			return false;
		
		// Just Finished
		if (pressTe.finished) {
			state.processingDuration = -1;
			return false;
		}
		
		return false;
	}

}
