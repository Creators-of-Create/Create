package com.simibubi.create.modules.logistics.management.base;

import static com.simibubi.create.modules.logistics.management.base.LogisticalCasingBlock.ACTIVE;
import static com.simibubi.create.modules.logistics.management.base.LogisticalCasingBlock.PART;
import static net.minecraft.state.properties.BlockStateProperties.AXIS;
import static net.minecraft.util.Direction.AxisDirection.POSITIVE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.block.IWithoutBlockItem;
import com.simibubi.create.foundation.block.RenderUtilityBlock;
import com.simibubi.create.modules.logistics.management.base.LogisticalCasingBlock.Part;
import com.simibubi.create.modules.logistics.management.controller.CalculationTileEntity;
import com.simibubi.create.modules.logistics.management.controller.RequestTileEntity;
import com.simibubi.create.modules.logistics.management.controller.StorageTileEntity;
import com.simibubi.create.modules.logistics.management.controller.SupplyTileEntity;
import com.simibubi.create.modules.logistics.management.controller.TransactionsTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class LogisticalControllerBlock extends DirectionalBlock
		implements IWithoutBlockItem, IWithTileEntity<LogisticalControllerTileEntity> {

	public static final IProperty<Type> TYPE = EnumProperty.create("type", Type.class);

	public static final VoxelShape UP_SHAPE = makeCuboidShape(2, -1, 2, 14, 3, 14),
			DOWN_SHAPE = makeCuboidShape(2, 13, 2, 14, 17, 14), SOUTH_SHAPE = makeCuboidShape(2, 2, -1, 14, 14, 3),
			NORTH_SHAPE = makeCuboidShape(2, 2, 13, 14, 14, 17), EAST_SHAPE = makeCuboidShape(-1, 2, 2, 3, 14, 14),
			WEST_SHAPE = makeCuboidShape(13, 2, 2, 17, 14, 14);

	public LogisticalControllerBlock() {
		super(Properties.from(Blocks.PISTON));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(TYPE, FACING);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		Type type = state.get(TYPE);

		if (type == Type.SUPPLY)
			return new SupplyTileEntity();
		if (type == Type.REQUEST)
			return new RequestTileEntity();
		if (type == Type.STORAGE)
			return new StorageTileEntity();
		if (type == Type.CALCULATION)
			return new CalculationTileEntity();
		if (type == Type.TRANSACTIONS)
			return new TransactionsTileEntity();

		return null;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (!(placer instanceof PlayerEntity))
			return;
		PlayerEntity player = (PlayerEntity) placer;
		for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
			ItemStack itemStack = player.inventory.getStackInSlot(slot);
			if (!AllItems.LOGISTICAL_DIAL.typeOf(itemStack))
				continue;
			if (!itemStack.hasTag())
				continue;
			withTileEntityDo(worldIn, pos, te -> te.setNetworkId(itemStack.getTag().getUniqueId("NetworkID")));
			return;
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState().with(FACING, context.getFace());

		Item item = context.getItem().getItem();
		if (item instanceof LogisticalControllerItem)
			state = state.with(TYPE, ((LogisticalControllerItem) item).getType());

		return state;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction facing = state.get(FACING);
		BlockPos offset = pos.offset(facing.getOpposite());
		BlockState blockState = worldIn.getBlockState(offset);
		boolean isCasing = AllBlocks.LOGISTICAL_CASING.typeOf(blockState);

		return isCasing;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (player.isSneaking() || !player.isAllowEdit())
			return false;
		ItemStack held = player.getHeldItem(handIn);
		if (held.getItem() != Items.NAME_TAG)
			return false;
		if (!held.hasDisplayName())
			return false;

		withTileEntityDo(worldIn, pos, te -> {
			te.setName(held.getDisplayName().getUnformattedComponentText());
		});

		return true;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		BlockPos start = pos.offset(state.get(FACING).getOpposite());
		List<BlockPos> toUpdate = collectCasings(worldIn, start);

		for (BlockPos blockPos : toUpdate) {
			worldIn.setBlockState(blockPos, worldIn.getBlockState(blockPos).with(ACTIVE, true));
			LogisticalCasingTileEntity tileEntity = (LogisticalCasingTileEntity) worldIn.getTileEntity(blockPos);
			tileEntity.addController(pos);
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		boolean blockChanged = state.getBlock() != newState.getBlock() || state.get(TYPE) != newState.get(TYPE);

		if (blockChanged) {
			BlockPos start = pos.offset(state.get(FACING).getOpposite());
			List<BlockPos> toUpdate = collectCasings(worldIn, start);

			for (BlockPos blockPos : toUpdate) {
				if (!worldIn.getBlockState(blockPos).get(ACTIVE))
					continue;
				LogisticalCasingTileEntity tileEntity = (LogisticalCasingTileEntity) worldIn.getTileEntity(blockPos);
				tileEntity.removeController(pos);
			}
		}

		if (state.hasTileEntity() && blockChanged) {
			worldIn.removeTileEntity(pos);
		}
	}

	public static List<BlockPos> collectCasings(World worldIn, BlockPos start) {
		BlockState casing = worldIn.getBlockState(start);
		if (!AllBlocks.LOGISTICAL_CASING.typeOf(casing))
			return Collections.emptyList();
		List<BlockPos> casings = new ArrayList<>();
		casings.add(start);
		if (casing.get(PART) != Part.NONE) {
			Direction casingDirection = Direction.getFacingFromAxis(POSITIVE, casing.get(AXIS));
			BlockPos search = start;

			for (int i = 0; i < 1000; i++) {
				if (worldIn.getBlockState(search).get(PART) == Part.START)
					break;
				search = search.offset(casingDirection.getOpposite());
				if (!AllBlocks.LOGISTICAL_CASING.typeOf(worldIn.getBlockState(search)))
					break;
				casings.add(search);
			}
			search = start;
			for (int i = 0; i < 1000; i++) {
				if (worldIn.getBlockState(search).get(PART) == Part.END)
					break;
				search = search.offset(casingDirection);
				if (!AllBlocks.LOGISTICAL_CASING.typeOf(worldIn.getBlockState(search)))
					break;
				casings.add(search);
			}
		}
		return casings;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (state.isValidPosition(worldIn, pos))
			return;

		TileEntity tileentity = state.hasTileEntity() ? worldIn.getTileEntity(pos) : null;
		spawnDrops(state, worldIn, pos, tileentity);
		worldIn.removeBlock(pos, false);

		for (Direction direction : Direction.values())
			worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction facing = state.get(FACING);

		if (facing == Direction.UP)
			return UP_SHAPE;
		if (facing == Direction.DOWN)
			return DOWN_SHAPE;
		if (facing == Direction.EAST)
			return EAST_SHAPE;
		if (facing == Direction.WEST)
			return WEST_SHAPE;
		if (facing == Direction.NORTH)
			return NORTH_SHAPE;
		if (facing == Direction.SOUTH)
			return SOUTH_SHAPE;

		return VoxelShapes.empty();
	}

	public static class LogisticalControllerIndicatorBlock extends RenderUtilityBlock {
		@Override
		protected void fillStateContainer(Builder<Block, BlockState> builder) {
			builder.add(TYPE, FACING);
			super.fillStateContainer(builder);
		}
	}

	public enum Type implements IStringSerializable {
		SUPPLY, REQUEST, STORAGE, CALCULATION, TRANSACTIONS;

		@Override
		public String getName() {
			return name().toLowerCase();
		}
	}

}
