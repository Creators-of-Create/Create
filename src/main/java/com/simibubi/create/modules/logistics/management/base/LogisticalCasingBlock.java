package com.simibubi.create.modules.logistics.management.base;

import static net.minecraft.util.Direction.AxisDirection.POSITIVE;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IWithTileEntity;

import com.simibubi.create.foundation.utility.VoxelShaper;
import com.simibubi.create.foundation.utility.AllShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class LogisticalCasingBlock extends Block implements IWithTileEntity<LogisticalCasingTileEntity> {

	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
	public static final IProperty<Part> PART = EnumProperty.create("part", Part.class);
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public LogisticalCasingBlock() {
		super(Properties.from(Blocks.DARK_OAK_PLANKS));
		setDefaultState(getDefaultState().with(PART, Part.NONE).with(AXIS, Axis.Y).with(ACTIVE, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();
		for (Direction face : Direction.values()) {
			BlockState neighbour = context.getWorld().getBlockState(context.getPos().offset(face));
			if (!AllBlocks.LOGISTICAL_CASING.typeOf(neighbour))
				continue;
			if (neighbour.get(PART) != Part.NONE && face.getAxis() != neighbour.get(AXIS))
				continue;
			state = state.with(PART, face.getAxisDirection() == AxisDirection.POSITIVE ? Part.START : Part.END);
			state = state.with(AXIS, face.getAxis());
		}

		return state;
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
		BlockState invState = world.getBlockState(neighbor);

		if (!invState.hasTileEntity())
			return;
		TileEntity invTE = world.getTileEntity(neighbor);

		LazyOptional<IItemHandler> inventory = invTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (inventory.isPresent() && world instanceof IWorld) {
			withTileEntityDo((IWorld) world, pos, te -> te.neighbourChanged(neighbor));
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		boolean blockChanged = state.getBlock() != newState.getBlock();
		if (state.hasTileEntity() && (blockChanged || !newState.hasTileEntity())) {
			worldIn.removeTileEntity(pos);
			return;
		}
		if (blockChanged) {
			Part part = state.get(PART);
			Direction facing = Direction.getFacingFromAxis(POSITIVE, state.get(AXIS));
			if (part == Part.END || part == Part.MIDDLE)
				worldIn.getPendingBlockTicks().scheduleTick(pos.offset(facing.getOpposite()), state.getBlock(), 1);
			if (part == Part.START || part == Part.MIDDLE)
				worldIn.getPendingBlockTicks().scheduleTick(pos.offset(facing), state.getBlock(), 1);
		}
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return state.get(ACTIVE);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new LogisticalCasingTileEntity();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Part part = state.get(PART);

		if (part == Part.NONE)
			return AllShapes.LOGISTICAL_CASING_SINGLE_SHAPE;

		if (part == Part.MIDDLE)
			return AllShapes.LOGISTICAL_CASING_MIDDLE.get(state.get(AXIS));

		Direction facing = VoxelShaper.axisAsFace(state.get(AXIS));
		if (part == Part.END)
			facing = facing.getOpposite();

		return AllShapes.LOGISTICAL_CASING_CAP.get(facing);
		//return state.get(PART) == Part.NONE ? VoxelShapers.LOGISTICAL_CASING_SINGLE_SHAPE : VoxelShapes.fullCube();
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction face, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		Part part = state.get(PART);
		boolean neighbourPresent = AllBlocks.LOGISTICAL_CASING.typeOf(facingState);
		boolean alongAxis = face.getAxis() == state.get(AXIS);
		boolean positive = face.getAxisDirection() == AxisDirection.POSITIVE;
		boolean neighbourAlongAxis = neighbourPresent
				&& (facingState.get(PART) == Part.NONE || facingState.get(AXIS) == face.getAxis());

		if (part == Part.NONE && neighbourPresent && neighbourAlongAxis) {
			state = state.with(PART, positive ? Part.START : Part.END);
			return state.with(AXIS, face.getAxis());
		}

		if (!alongAxis)
			return state;

		if (part == Part.END) {
			if (positive && neighbourPresent && neighbourAlongAxis)
				return state.with(PART, Part.MIDDLE);
			if (!positive && !neighbourPresent)
				return state.with(PART, Part.NONE).with(AXIS, Axis.Y);
		}

		if (part == Part.START) {
			if (!positive && neighbourPresent && neighbourAlongAxis)
				return state.with(PART, Part.MIDDLE);
			if (positive && !neighbourPresent)
				return state.with(PART, Part.NONE).with(AXIS, Axis.Y);
		}

		if (part == Part.MIDDLE) {
			if (!positive && !neighbourPresent)
				return state.with(PART, Part.START);
			if (positive && !neighbourPresent)
				return state.with(PART, Part.END);
		}

		return state;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (!player.getHeldItem(handIn).isEmpty())
			return false;
		if (worldIn.isRemote)
			return true;
		if (!state.get(ACTIVE))
			player.sendStatusMessage(new StringTextComponent("Not Active").applyTextStyle(TextFormatting.RED), false);
		else {
			LogisticalCasingTileEntity tileEntity = (LogisticalCasingTileEntity) worldIn.getTileEntity(pos);
			player.sendStatusMessage(new StringTextComponent("Controllers: " + tileEntity.controllers.toString())
					.applyTextStyle(TextFormatting.GREEN), false);
		}

		return true;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		boolean blockChanged = state.getBlock() != oldState.getBlock();
		if (blockChanged)
			worldIn.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), 1);
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		synchronizeCasingGroup(worldIn, pos);
	}

	protected void synchronizeCasingGroup(World world, BlockPos start) {
		List<BlockPos> chain = LogisticalControllerBlock.collectCasings(world, start);
		Set<BlockPos> controllers = new HashSet<>();

		// Collect all Controllers
		for (BlockPos pos : chain) {
			BlockState casing = world.getBlockState(pos);
			if (!casing.get(ACTIVE))
				continue;
			LogisticalCasingTileEntity te = (LogisticalCasingTileEntity) world.getTileEntity(pos);
			if (te == null)
				continue;
			for (BlockPos controller : te.controllers) {
				if (controller.withinDistance(te.getPos(), 1 + 1 / 512f))
					controllers.add(controller);
			}
		}

		// Advertise all Controllers
		for (BlockPos pos : chain) {
			BlockState state = world.getBlockState(pos);
			boolean shouldBeActive = !controllers.isEmpty();
			if (state.get(ACTIVE) != shouldBeActive) {
				if (!shouldBeActive) {
					LogisticalCasingTileEntity te = (LogisticalCasingTileEntity) world.getTileEntity(pos);
					te.controllers.forEach(te::detachController);
				}
				world.setBlockState(pos, state.with(ACTIVE, shouldBeActive));
			}
			if (!shouldBeActive)
				continue;

			LogisticalCasingTileEntity te = (LogisticalCasingTileEntity) world.getTileEntity(pos);
			if (te == null)
				continue;

			// detach missing
			for (Iterator<BlockPos> iterator = te.controllers.iterator(); iterator.hasNext();) {
				BlockPos controller = iterator.next();
				if (controllers.contains(controller))
					continue;
				iterator.remove();
				te.detachController(controller);
			}

			// attach new
			for (BlockPos controller : controllers) {
				if (!te.controllers.contains(controller))
					te.addController(controller);
			}
		}
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(AXIS, PART, ACTIVE);
		super.fillStateContainer(builder);
	}

	public enum Part implements IStringSerializable {
		START, MIDDLE, END, NONE;

		@Override
		public String getName() {
			return name().toLowerCase();
		}
	}

}
