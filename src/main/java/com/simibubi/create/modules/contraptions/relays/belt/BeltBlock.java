package com.simibubi.create.modules.contraptions.relays.belt;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.block.IWithoutBlockItem;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity.TransportedEntityInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public class BeltBlock extends HorizontalKineticBlock implements IWithoutBlockItem, IWithTileEntity<BeltTileEntity> {

	public static final IProperty<Slope> SLOPE = EnumProperty.create("slope", Slope.class);
	public static final IProperty<Part> PART = EnumProperty.create("part", Part.class);

	private static final VoxelShape FULL = makeCuboidShape(0, 0, 0, 16, 16, 16),
			FLAT_STRAIGHT_X = makeCuboidShape(1, 3, 0, 15, 13, 16),
			FLAT_STRAIGHT_Z = makeCuboidShape(0, 3, 1, 16, 13, 15),
			VERTICAL_STRAIGHT_X = makeCuboidShape(3, 0, 1, 13, 16, 15),
			VERTICAL_STRAIGHT_Z = makeCuboidShape(1, 0, 3, 15, 16, 13),

			SLOPE_END_EAST = makeCuboidShape(0, 3, 1, 10, 13, 15),
			SLOPE_END_WEST = makeCuboidShape(6, 3, 1, 16, 13, 15),
			SLOPE_END_SOUTH = makeCuboidShape(1, 3, 0, 15, 13, 10),
			SLOPE_END_NORTH = makeCuboidShape(1, 3, 6, 15, 13, 16),

			SLOPE_BUILDING_BLOCK_X = makeCuboidShape(5, 5, 1, 11, 11, 15),
			SLOPE_BUILDING_BLOCK_Z = makeCuboidShape(1, 5, 5, 15, 11, 11),

			SLOPE_UPWARD_END_EAST = VoxelShapes.or(SLOPE_END_EAST, createHalfSlope(Direction.EAST, false)),
			SLOPE_UPWARD_END_WEST = VoxelShapes.or(SLOPE_END_WEST, createHalfSlope(Direction.WEST, false)),
			SLOPE_UPWARD_END_SOUTH = VoxelShapes.or(SLOPE_END_SOUTH, createHalfSlope(Direction.SOUTH, false)),
			SLOPE_UPWARD_END_NORTH = VoxelShapes.or(SLOPE_END_NORTH, createHalfSlope(Direction.NORTH, false)),

			SLOPE_DOWNWARD_END_EAST = VoxelShapes.or(SLOPE_END_EAST, createHalfSlope(Direction.EAST, true)),
			SLOPE_DOWNWARD_END_WEST = VoxelShapes.or(SLOPE_END_WEST, createHalfSlope(Direction.WEST, true)),
			SLOPE_DOWNWARD_END_SOUTH = VoxelShapes.or(SLOPE_END_SOUTH, createHalfSlope(Direction.SOUTH, true)),
			SLOPE_DOWNWARD_END_NORTH = VoxelShapes.or(SLOPE_END_NORTH, createHalfSlope(Direction.NORTH, true)),

			SLOPE_EAST = createSlope(Direction.EAST), SLOPE_WEST = createSlope(Direction.WEST),
			SLOPE_NORTH = createSlope(Direction.NORTH), SLOPE_SOUTH = createSlope(Direction.SOUTH);

	public BeltBlock() {
		super(Properties.from(Blocks.BROWN_WOOL));
		setDefaultState(getDefaultState().with(SLOPE, Slope.HORIZONTAL).with(PART, Part.START));
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
			PlayerEntity player) {
		return new ItemStack(AllItems.BELT_CONNECTOR.item);
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);

		if (entityIn instanceof PlayerEntity && entityIn.isSneaking())
			return;
		if (entityIn instanceof PlayerEntity && ((PlayerEntity) entityIn).moveVertical > 0)
			return;

		BeltTileEntity belt = null;
		BlockPos entityPosition = entityIn.getPosition();

		if (AllBlocks.BELT.typeOf(worldIn.getBlockState(entityPosition)))
			belt = (BeltTileEntity) worldIn.getTileEntity(entityPosition);
		else if (AllBlocks.BELT.typeOf(worldIn.getBlockState(entityPosition.down())))
			belt = (BeltTileEntity) worldIn.getTileEntity(entityPosition.down());

		if (belt == null || !belt.hasSource())
			return;

		BeltTileEntity controller = (BeltTileEntity) worldIn.getTileEntity(belt.getController());

		if (controller == null)
			return;
		if (controller.passengers == null)
			return;

		if (controller.passengers.containsKey(entityIn))
			controller.passengers.get(entityIn).refresh(belt.getPos(), belt.getBlockState());
		else
			controller.passengers.put(entityIn, new TransportedEntityInfo(belt.getPos(), belt.getBlockState()));
	}

	@Override
	public float getSlipperiness(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
		return super.getSlipperiness(state, world, pos, entity);
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (entityIn instanceof PlayerEntity && entityIn.isSneaking())
			return;
		if (entityIn instanceof PlayerEntity && ((PlayerEntity) entityIn).moveVertical > 0)
			return;

		BeltTileEntity belt = null;
		belt = (BeltTileEntity) worldIn.getTileEntity(pos);

		if (belt == null || !belt.hasSource())
			return;

		BeltTileEntity controller = (BeltTileEntity) worldIn.getTileEntity(belt.getController());

		if (controller == null)
			return;
		if (controller.passengers == null)
			return;

		if (controller.passengers.containsKey(entityIn)) {
			TransportedEntityInfo transportedEntityInfo = controller.passengers.get(entityIn);
			if (transportedEntityInfo.ticksSinceLastCollision != 0 || pos.equals(entityIn.getPosition()))
				transportedEntityInfo.refresh(pos, state);
		} else
			controller.passengers.put(entityIn, new TransportedEntityInfo(pos, state));
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		withTileEntityDo(worldIn, pos, te -> {
			te.attachmentTracker.findAttachments(te);
		});
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (player.isSneaking() || !player.isAllowEdit())
			return false;
		ItemStack heldItem = player.getHeldItem(handIn);
		if (!Tags.Items.DYES.contains(heldItem.getItem()))
			return false;
		if (worldIn.isRemote)
			return true;
		withTileEntityDo(worldIn, pos, te -> te.applyColor(DyeColor.getColor(heldItem)));
		if (!player.isCreative())
			heldItem.shrink(1);
		return true;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(SLOPE, PART);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction facing = state.get(HORIZONTAL_FACING);
		Axis axis = facing.getAxis();
		Part part = state.get(PART);
		Slope slope = state.get(SLOPE);

		if (slope == Slope.HORIZONTAL)
			return axis == Axis.Z ? FLAT_STRAIGHT_X : FLAT_STRAIGHT_Z;
		if (slope == Slope.VERTICAL)
			return axis == Axis.X ? VERTICAL_STRAIGHT_X : VERTICAL_STRAIGHT_Z;

		if (part != Part.MIDDLE) {
			if (part == Part.START)
				slope = slope == Slope.UPWARD ? Slope.DOWNWARD : Slope.UPWARD;
			else
				facing = facing.getOpposite();

			if (facing == Direction.NORTH)
				return slope == Slope.UPWARD ? SLOPE_UPWARD_END_NORTH : SLOPE_DOWNWARD_END_NORTH;
			if (facing == Direction.SOUTH)
				return slope == Slope.UPWARD ? SLOPE_UPWARD_END_SOUTH : SLOPE_DOWNWARD_END_SOUTH;
			if (facing == Direction.EAST)
				return slope == Slope.UPWARD ? SLOPE_UPWARD_END_EAST : SLOPE_DOWNWARD_END_EAST;
			if (facing == Direction.WEST)
				return slope == Slope.UPWARD ? SLOPE_UPWARD_END_WEST : SLOPE_DOWNWARD_END_WEST;
		}

		if (slope == Slope.DOWNWARD)
			facing = facing.getOpposite();

		if (facing == Direction.NORTH)
			return SLOPE_NORTH;
		if (facing == Direction.SOUTH)
			return SLOPE_SOUTH;
		if (facing == Direction.EAST)
			return SLOPE_EAST;
		if (facing == Direction.WEST)
			return SLOPE_WEST;

		return FULL;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BeltTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return false;
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		withTileEntityDo(worldIn, pos, te -> {
			if (te.hasPulley())
				Block.spawnDrops(AllBlocks.SHAFT.get().getDefaultState(), worldIn, pos);
		});
		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (worldIn.isRemote)
			return;

		boolean endWasDestroyed = state.get(PART) == Part.END;
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity == null)
			return;
		if (!(tileEntity instanceof BeltTileEntity))
			return;
		BeltTileEntity beltEntity = (BeltTileEntity) tileEntity;
		BlockPos controller = beltEntity.getController();
		beltEntity.setSource(null);
		beltEntity.remove();

		int limit = 1000;
		BlockPos toDestroy = controller;
		BlockState destroyedBlock = null;

		do {

			if (!toDestroy.equals(pos)) {
				destroyedBlock = worldIn.getBlockState(toDestroy);
				if (!AllBlocks.BELT.typeOf(destroyedBlock))
					break;

				BeltTileEntity te = (BeltTileEntity) worldIn.getTileEntity(toDestroy);
				boolean hasPulley = te.hasPulley();
				te.setSource(null);
				te.remove();

				if (hasPulley) {
					worldIn.setBlockState(toDestroy, AllBlocks.SHAFT.get().getDefaultState()
							.with(BlockStateProperties.AXIS, getRotationAxis(destroyedBlock)), 3);
				} else {
					worldIn.destroyBlock(toDestroy, false);
				}

				if (destroyedBlock.get(PART) == Part.END)
					break;
			} else {
				if (endWasDestroyed)
					break;
			}

			Slope slope = state.get(SLOPE);
			Direction direction = state.get(HORIZONTAL_FACING);

			if (slope == Slope.VERTICAL) {
				toDestroy = toDestroy.up(direction.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -1);
				continue;
			}

			toDestroy = toDestroy.offset(direction);
			if (slope != Slope.HORIZONTAL)
				toDestroy = toDestroy.up(slope == Slope.UPWARD ? 1 : -1);

		} while (limit-- > 0);

	}

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		if (face.getAxis() != getRotationAxis(state))
			return false;
		BeltTileEntity beltEntity = (BeltTileEntity) world.getTileEntity(pos);
		return beltEntity != null && beltEntity.hasPulley();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_FACING).getAxis() == Axis.X ? Axis.Z : Axis.X;
	}

	public enum Slope implements IStringSerializable {
		HORIZONTAL, UPWARD, DOWNWARD, VERTICAL;

		@Override
		public String getName() {
			return Lang.asId(name());
		}
	}

	public enum Part implements IStringSerializable {
		START, MIDDLE, END;

		@Override
		public String getName() {
			return Lang.asId(name());
		}
	}

	public static boolean isUpperEnd(BlockState state, float speed) {
		Direction facing = state.get(HORIZONTAL_FACING);
		if (state.get(SLOPE) == Slope.UPWARD && state.get(PART) == Part.END) {
			return facing.getAxisDirection().getOffset() * Math.signum(speed) == (facing.getAxis() == Axis.X ? -1 : 1);
		}
		if (state.get(SLOPE) == Slope.DOWNWARD && state.get(PART) == Part.START) {
			return facing.getAxisDirection().getOffset() * Math.signum(speed) == (facing.getAxis() == Axis.Z ? -1 : 1);
		}

		return false;
	}

	public static List<BlockPos> getBeltChain(World world, BlockPos controllerPos) {
		List<BlockPos> positions = new LinkedList<>();

		BlockState blockState = world.getBlockState(controllerPos);
		if (!AllBlocks.BELT.typeOf(blockState))
			return positions;

		Slope slope = blockState.get(SLOPE);
		Direction direction = blockState.get(HORIZONTAL_FACING);

		int limit = 1000;
		BlockPos current = controllerPos;
		do {
			positions.add(current);

			if (!AllBlocks.BELT.typeOf(world.getBlockState(current)))
				break;
			if (world.getBlockState(current).get(PART) == Part.END)
				break;
			if (slope == Slope.VERTICAL) {
				current = current.up(direction.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -1);
				continue;
			}
			current = current.offset(direction);
			if (slope != Slope.HORIZONTAL)
				current = current.up(slope == Slope.UPWARD ? 1 : -1);
		} while (limit-- > 0);

		return positions;
	}

	protected static VoxelShape createSlope(Direction facing) {
		return VoxelShapes.or(createHalfSlope(facing.getOpposite(), false), createHalfSlope(facing, true));
	}

	protected static VoxelShape createHalfSlope(Direction facing, boolean upward) {
		VoxelShape shape = VoxelShapes.empty();
		VoxelShape buildingBlock = facing.getAxis() == Axis.X ? SLOPE_BUILDING_BLOCK_X : SLOPE_BUILDING_BLOCK_Z;
		Vec3i directionVec = facing.getDirectionVec();

		int x = directionVec.getX();
		int y = upward ? 1 : -1;
		int z = directionVec.getZ();

		for (int segment = 0; segment < 6; segment++)
			shape = VoxelShapes.or(shape,
					buildingBlock.withOffset(x * segment / 16f, y * segment / 16f, z * segment / 16f));

		if (!upward)
			return shape;

		VoxelShape mask = makeCuboidShape(0, -8, 0, 16, 24, 16);
		for (int segment = 6; segment < 11; segment++)
			shape = VoxelShapes.or(shape,
					VoxelShapes.combine(mask,
							buildingBlock.withOffset(x * segment / 16f, y * segment / 16f, z * segment / 16f),
							IBooleanFunction.AND));
		return shape;
	}

}
