package com.simibubi.create.modules.contraptions.components.crafter;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.IHaveConnectedTextures;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.modules.contraptions.components.crafter.ConnectedInputHandler.ConnectedInput;
import com.simibubi.create.modules.contraptions.components.crafter.MechanicalCrafterTileEntity.Phase;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class MechanicalCrafterBlock extends HorizontalKineticBlock
		implements IWithTileEntity<MechanicalCrafterTileEntity>, IHaveConnectedTextures {

	public static final EnumProperty<Pointing> POINTING = EnumProperty.create("pointing", Pointing.class);

	public MechanicalCrafterBlock() {
		super(Properties.from(Blocks.GOLD_BLOCK));
		setDefaultState(getDefaultState().with(POINTING, Pointing.UP));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(POINTING));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MechanicalCrafterTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public boolean hasCogsTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return state.get(HORIZONTAL_FACING).getAxis() != face.getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_FACING).getAxis();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction face = context.getFace();
		BlockPos placedOnPos = context.getPos().offset(face.getOpposite());
		BlockState blockState = context.getWorld().getBlockState(placedOnPos);

		if ((blockState.getBlock() != this) || context.isPlacerSneaking()) {
			BlockState stateForPlacement = super.getStateForPlacement(context);
			Direction direction = stateForPlacement.get(HORIZONTAL_FACING);
			if (direction != face)
				stateForPlacement = stateForPlacement.with(POINTING, pointingFromFacing(face, direction));
			return stateForPlacement;
		}

		Direction otherFacing = blockState.get(HORIZONTAL_FACING);
		Pointing pointing = pointingFromFacing(face, otherFacing);
		return getDefaultState().with(HORIZONTAL_FACING, otherFacing).with(POINTING, pointing);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() == newState.getBlock()) {
			if (getTargetDirection(state) != getTargetDirection(newState)) {
				MechanicalCrafterTileEntity crafter = CrafterHelper.getCrafter(worldIn, pos);
				if (crafter != null)
					crafter.blockChanged();
			}
		}

		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			MechanicalCrafterTileEntity crafter = CrafterHelper.getCrafter(worldIn, pos);
			if (crafter != null)
				crafter.ejectWholeGrid();

			for (Direction direction : Direction.values()) {
				if (direction.getAxis() == state.get(HORIZONTAL_FACING).getAxis())
					continue;

				BlockPos otherPos = pos.offset(direction);
				ConnectedInput thisInput = CrafterHelper.getInput(worldIn, pos);
				ConnectedInput otherInput = CrafterHelper.getInput(worldIn, otherPos);

				if (thisInput == null || otherInput == null)
					continue;
				if (!pos.add(thisInput.data.get(0)).equals(otherPos.add(otherInput.data.get(0))))
					continue;

				ConnectedInputHandler.toggleConnection(worldIn, pos, otherPos);
			}

			worldIn.removeTileEntity(pos);
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
		if (context.getFace() == state.get(HORIZONTAL_FACING)) {
			context.getWorld().setBlockState(context.getPos(), state.cycle(POINTING));
			withTileEntityDo(context.getWorld(), context.getPos(), TileEntity::markDirty);
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		ItemStack heldItem = player.getHeldItem(handIn);
		boolean isHand = heldItem.isEmpty() && handIn == Hand.MAIN_HAND;

		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof MechanicalCrafterTileEntity))
			return false;
		MechanicalCrafterTileEntity crafter = (MechanicalCrafterTileEntity) te;

		if (hit.getFace() == state.get(HORIZONTAL_FACING)) {

			if (crafter.phase != Phase.IDLE && !AllItems.WRENCH.typeOf(heldItem)) {
				crafter.ejectWholeGrid();
				return true;
			}

			if (crafter.phase == Phase.IDLE && !isHand && !AllItems.WRENCH.typeOf(heldItem)) {
				if (worldIn.isRemote)
					return true;
				LazyOptional<IItemHandler> capability = crafter
						.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
				if (!capability.isPresent())
					return false;
				ItemStack remainder = ItemHandlerHelper.insertItem(capability.orElse(new ItemStackHandler()),
						heldItem.copy(), false);
				if (remainder.getCount() != heldItem.getCount())
					player.setHeldItem(handIn, remainder);
				return true;
			}

			ItemStack inSlot = crafter.inventory.getStackInSlot(0);
			if (inSlot.isEmpty())
				return false;
			if (!isHand && !ItemHandlerHelper.canItemStacksStack(heldItem, inSlot))
				return false;
			if (worldIn.isRemote)
				return true;
			player.inventory.placeItemBackInInventory(worldIn, inSlot);
			crafter.inventory.setStackInSlot(0, ItemStack.EMPTY);
			return true;
		}

		return false;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
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
		Direction facing = state.get(HORIZONTAL_FACING);
		Pointing point = state.get(POINTING);
		Vec3d targetVec = new Vec3d(0, 1, 0);
		targetVec = VecHelper.rotate(targetVec, -point.getXRotation(), Axis.Z);
		targetVec = VecHelper.rotate(targetVec, AngleHelper.horizontalAngle(facing), Axis.Y);
		return Direction.getFacingFromVector(targetVec.x, targetVec.y, targetVec.z);
	}

	public static boolean isValidTarget(World world, BlockPos targetPos, BlockState crafterState) {
		BlockState targetState = world.getBlockState(targetPos);
		if (!world.isBlockPresent(targetPos))
			return false;
		if (!AllBlocks.MECHANICAL_CRAFTER.typeOf(targetState))
			return false;
		if (crafterState.get(HORIZONTAL_FACING) != targetState.get(HORIZONTAL_FACING))
			return false;
		if (Math.abs(crafterState.get(POINTING).xRotation - targetState.get(POINTING).xRotation) == 180)
			return false;
		return true;
	}

	public enum Pointing implements IStringSerializable {
		UP(0), LEFT(270), DOWN(180), RIGHT(90);

		private int xRotation;

		private Pointing(int xRotation) {
			this.xRotation = xRotation;
		}

		@Override
		public String getName() {
			return Lang.asId(name());
		}

		public int getXRotation() {
			return xRotation;
		}
	}

	@Override
	public ConnectedTextureBehaviour getBehaviour() {
		return new InputCTBehaviour();
	}

}
