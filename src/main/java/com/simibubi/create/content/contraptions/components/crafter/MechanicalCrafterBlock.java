package com.simibubi.create.content.contraptions.components.crafter;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.crafter.ConnectedInputHandler.ConnectedInput;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity.Phase;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pointing;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class MechanicalCrafterBlock extends HorizontalKineticBlock implements ITE<MechanicalCrafterTileEntity>, ICogWheel {

	public static final EnumProperty<Pointing> POINTING = EnumProperty.create("pointing", Pointing.class);

	public MechanicalCrafterBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POINTING, Pointing.UP));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POINTING));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.MECHANICAL_CRAFTER.create();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction face = context.getClickedFace();
		BlockPos placedOnPos = context.getClickedPos()
			.relative(face.getOpposite());
		BlockState blockState = context.getLevel()
			.getBlockState(placedOnPos);

		if ((blockState.getBlock() != this) || (context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown())) {
			BlockState stateForPlacement = super.getStateForPlacement(context);
			Direction direction = stateForPlacement.getValue(HORIZONTAL_FACING);
			if (direction != face)
				stateForPlacement = stateForPlacement.setValue(POINTING, pointingFromFacing(face, direction));
			return stateForPlacement;
		}

		Direction otherFacing = blockState.getValue(HORIZONTAL_FACING);
		Pointing pointing = pointingFromFacing(face, otherFacing);
		return defaultBlockState().setValue(HORIZONTAL_FACING, otherFacing)
			.setValue(POINTING, pointing);
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() == newState.getBlock()) {
			if (getTargetDirection(state) != getTargetDirection(newState)) {
				MechanicalCrafterTileEntity crafter = CrafterHelper.getCrafter(worldIn, pos);
				if (crafter != null)
					crafter.blockChanged();
			}
		}

		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			MechanicalCrafterTileEntity crafter = CrafterHelper.getCrafter(worldIn, pos);
			if (crafter != null) {
				if (crafter.covered)
					Block.popResource(worldIn, pos, AllItems.CRAFTER_SLOT_COVER.asStack());
				crafter.ejectWholeGrid();
			}

			for (Direction direction : Iterate.directions) {
				if (direction.getAxis() == state.getValue(HORIZONTAL_FACING)
					.getAxis())
					continue;

				BlockPos otherPos = pos.relative(direction);
				ConnectedInput thisInput = CrafterHelper.getInput(worldIn, pos);
				ConnectedInput otherInput = CrafterHelper.getInput(worldIn, otherPos);

				if (thisInput == null || otherInput == null)
					continue;
				if (!pos.offset(thisInput.data.get(0))
					.equals(otherPos.offset(otherInput.data.get(0))))
					continue;

				ConnectedInputHandler.toggleConnection(worldIn, pos, otherPos);
			}

			worldIn.removeBlockEntity(pos);
		}
	}

	public static Pointing pointingFromFacing(Direction pointingFace, Direction blockFacing) {
		boolean positive = blockFacing.getAxisDirection() == AxisDirection.POSITIVE;

		Pointing pointing = pointingFace == Direction.DOWN ? Pointing.UP : Pointing.DOWN;
		if (pointingFace == Direction.EAST)
			pointing = positive ? Pointing.LEFT : Pointing.RIGHT;
		if (pointingFace == Direction.WEST)
			pointing = positive ? Pointing.RIGHT : Pointing.LEFT;
		if (pointingFace == Direction.NORTH)
			pointing = positive ? Pointing.LEFT : Pointing.RIGHT;
		if (pointingFace == Direction.SOUTH)
			pointing = positive ? Pointing.RIGHT : Pointing.LEFT;
		return pointing;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (context.getClickedFace() == state.getValue(HORIZONTAL_FACING)) {
			if (!context.getLevel().isClientSide)
				KineticTileEntity.switchToBlockState(context.getLevel(), context.getClickedPos(), state.cycle(POINTING));
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		ItemStack heldItem = player.getItemInHand(handIn);
		boolean isHand = heldItem.isEmpty() && handIn == Hand.MAIN_HAND;

		TileEntity te = worldIn.getBlockEntity(pos);
		if (!(te instanceof MechanicalCrafterTileEntity))
			return ActionResultType.PASS;
		MechanicalCrafterTileEntity crafter = (MechanicalCrafterTileEntity) te;
		boolean wrenched = AllItems.WRENCH.isIn(heldItem);

		if (AllBlocks.MECHANICAL_ARM.isIn(heldItem))
			return ActionResultType.PASS;
		
		if (hit.getDirection() == state.getValue(HORIZONTAL_FACING)) {

			if (crafter.phase != Phase.IDLE && !wrenched) {
				crafter.ejectWholeGrid();
				return ActionResultType.SUCCESS;
			}

			if (crafter.phase == Phase.IDLE && !isHand && !wrenched) {
				if (worldIn.isClientSide)
					return ActionResultType.SUCCESS;

				if (AllItems.CRAFTER_SLOT_COVER.isIn(heldItem)) {
					if (crafter.covered)
						return ActionResultType.PASS;
					if (!crafter.inventory.isEmpty())
						return ActionResultType.PASS;
					crafter.covered = true;
					crafter.setChanged();
					crafter.sendData();
					if (!player.isCreative())
						heldItem.shrink(1);
					return ActionResultType.SUCCESS;
				}

				LazyOptional<IItemHandler> capability =
					crafter.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
				if (!capability.isPresent())
					return ActionResultType.PASS;
				ItemStack remainder =
					ItemHandlerHelper.insertItem(capability.orElse(new ItemStackHandler()), heldItem.copy(), false);
				if (remainder.getCount() != heldItem.getCount())
					player.setItemInHand(handIn, remainder);
				return ActionResultType.SUCCESS;
			}

			ItemStack inSlot = crafter.getInventory().getItem(0);
			if (inSlot.isEmpty()) {
				if (crafter.covered && !wrenched) {
					if (worldIn.isClientSide)
						return ActionResultType.SUCCESS;
					crafter.covered = false;
					crafter.setChanged();
					crafter.sendData();
					if (!player.isCreative())
						player.inventory.placeItemBackInInventory(worldIn, AllItems.CRAFTER_SLOT_COVER.asStack());
					return ActionResultType.SUCCESS;
				}
				return ActionResultType.PASS;
			}
			if (!isHand && !ItemHandlerHelper.canItemStacksStack(heldItem, inSlot))
				return ActionResultType.PASS;
			if (worldIn.isClientSide)
				return ActionResultType.SUCCESS;
			player.inventory.placeItemBackInInventory(worldIn, inSlot);
			crafter.getInventory().setStackInSlot(0, ItemStack.EMPTY);
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		InvManipulationBehaviour behaviour = TileEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
		if (behaviour != null)
			behaviour.onNeighborChanged(fromPos);
	}

	@Override
	public float getParticleTargetRadius() {
		return .85f;
	}

	@Override
	public float getParticleInitialRadius() {
		return .75f;
	}

	public static Direction getTargetDirection(BlockState state) {
		if (!AllBlocks.MECHANICAL_CRAFTER.has(state))
			return Direction.UP;
		Direction facing = state.getValue(HORIZONTAL_FACING);
		Pointing point = state.getValue(POINTING);
		Vector3d targetVec = new Vector3d(0, 1, 0);
		targetVec = VecHelper.rotate(targetVec, -point.getXRotation(), Axis.Z);
		targetVec = VecHelper.rotate(targetVec, AngleHelper.horizontalAngle(facing), Axis.Y);
		return Direction.getNearest(targetVec.x, targetVec.y, targetVec.z);
	}

	public static boolean isValidTarget(World world, BlockPos targetPos, BlockState crafterState) {
		BlockState targetState = world.getBlockState(targetPos);
		if (!world.isLoaded(targetPos))
			return false;
		if (!AllBlocks.MECHANICAL_CRAFTER.has(targetState))
			return false;
		if (crafterState.getValue(HORIZONTAL_FACING) != targetState.getValue(HORIZONTAL_FACING))
			return false;
		if (Math.abs(crafterState.getValue(POINTING)
			.getXRotation()
			- targetState.getValue(POINTING)
				.getXRotation()) == 180)
			return false;
		return true;
	}

	@Override
	public Class<MechanicalCrafterTileEntity> getTileEntityClass() {
		return MechanicalCrafterTileEntity.class;
	}

}
