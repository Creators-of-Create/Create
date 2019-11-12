package com.simibubi.create.modules.contraptions.relays.belt;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.block.IWithoutBlockItem;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltMovementHandler.TransportedEntityInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
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
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BeltBlock extends HorizontalKineticBlock implements IWithoutBlockItem, IWithTileEntity<BeltTileEntity> {

	public static final IProperty<Slope> SLOPE = EnumProperty.create("slope", Slope.class);
	public static final IProperty<Part> PART = EnumProperty.create("part", Part.class);

	public BeltBlock() {
		super(Properties.from(Blocks.BROWN_WOOL));
		setDefaultState(getDefaultState().with(SLOPE, Slope.HORIZONTAL).with(PART, Part.START));
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
		return state.get(HORIZONTAL_FACING).rotateY().getAxis();
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
			PlayerEntity player) {
		return AllItems.BELT_CONNECTOR.asStack();
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		BlockPos entityPosition = entityIn.getPosition();
		BlockPos beltPos = null;

		if (AllBlocks.BELT.typeOf(worldIn.getBlockState(entityPosition)))
			beltPos = entityPosition;
		else if (AllBlocks.BELT.typeOf(worldIn.getBlockState(entityPosition.down())))
			beltPos = entityPosition.down();
		if (beltPos == null)
			return;
		if (!(worldIn instanceof World))
			return;

		onEntityCollision(worldIn.getBlockState(beltPos), (World) worldIn, beltPos, entityIn);
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		BeltTileEntity belt = null;
		belt = (BeltTileEntity) worldIn.getTileEntity(pos);

		if (entityIn instanceof PlayerEntity && entityIn.isSneaking())
			return;
		if (belt == null || belt.getSpeed() == 0)
			return;
		if (entityIn instanceof ItemEntity && entityIn.isAlive()) {
			if (worldIn.isRemote)
				return;
			withTileEntityDo(worldIn, pos, te -> {
				ItemEntity itemEntity = (ItemEntity) entityIn;
				ItemStack remainder = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
						.orElseGet(() -> new ItemStackHandler(0)).insertItem(0, itemEntity.getItem().copy(), false);
				if (remainder.isEmpty())
					itemEntity.remove();
			});
			return;
		}

		BeltTileEntity controller = (BeltTileEntity) worldIn.getTileEntity(belt.getController());
		if (controller == null || controller.passengers == null)
			return;
		if (controller.passengers.containsKey(entityIn)) {
			TransportedEntityInfo info = controller.passengers.get(entityIn);
			if (info.ticksSinceLastCollision != 0 || pos.equals(entityIn.getPosition()))
				info.refresh(pos, state);
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
		withTileEntityDo(worldIn, pos, te -> {
			DyeColor dyeColor = DyeColor.getColor(heldItem);
			if (dyeColor == null)
				return;
			te.applyColor(dyeColor);
		});
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
		return BeltShapes.getShape(state, worldIn, pos, context);
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

}
