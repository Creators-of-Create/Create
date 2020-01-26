package com.simibubi.create.modules.logistics.management.base;

import static com.simibubi.create.AllItems.LOGISTICAL_CONTROLLER_CALCULATION;
import static com.simibubi.create.AllItems.LOGISTICAL_CONTROLLER_REQUEST;
import static com.simibubi.create.AllItems.LOGISTICAL_CONTROLLER_STORAGE;
import static com.simibubi.create.AllItems.LOGISTICAL_CONTROLLER_SUPPLY;
import static com.simibubi.create.AllItems.LOGISTICAL_CONTROLLER_TRANSACTIONS;
import static com.simibubi.create.modules.logistics.management.base.LogisticalCasingBlock.ACTIVE;
import static com.simibubi.create.modules.logistics.management.base.LogisticalCasingBlock.PART;
import static net.minecraft.state.properties.BlockStateProperties.AXIS;
import static net.minecraft.util.Direction.AxisDirection.POSITIVE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.IHaveNoBlockItem;
import com.simibubi.create.foundation.block.IWithContainer;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.block.RenderUtilityBlock;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.logistics.management.base.LogisticalCasingBlock.Part;
import com.simibubi.create.modules.logistics.management.controller.CalculationTileEntity;
import com.simibubi.create.modules.logistics.management.controller.LogisticalInventoryControllerTileEntity;
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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class LogisticalControllerBlock extends DirectionalBlock
		implements IHaveNoBlockItem, IWithTileEntity<LogisticalActorTileEntity> {

	public static final IProperty<Type> TYPE = EnumProperty.create("type", Type.class);

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
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
			PlayerEntity player) {
		return getItem(world, pos, state);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, net.minecraft.world.storage.loot.LootContext.Builder builder) {
		return Arrays.asList(getItem(builder.getWorld(), BlockPos.ZERO, state));
	}

	@Override
	public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
		for (AllItems item : Arrays.asList(LOGISTICAL_CONTROLLER_CALCULATION, LOGISTICAL_CONTROLLER_REQUEST,
				LOGISTICAL_CONTROLLER_STORAGE, LOGISTICAL_CONTROLLER_SUPPLY, LOGISTICAL_CONTROLLER_TRANSACTIONS)) {
			if (((LogisticalControllerItem) item.get()).getType() == state.get(TYPE))
				return item.asStack();
		}
		return ItemStack.EMPTY;
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
	public String getTranslationKey() {
		return "block." + Create.ID + ".logistical_controller";
	}

	public static String getControllerTypeTranslationKey(BlockState state) {
		return "item." + Create.ID + ".logistical_controller_" + state.get(TYPE).name().toLowerCase();
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (player.isSneaking() || !player.isAllowEdit())
			return false;
		if (!state.hasTileEntity())
			return false;

		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof LogisticalInventoryControllerTileEntity))
			return false;
		if (state.get(TYPE) == Type.CALCULATION)
			return false;
		if (AllItems.LOGISTICAL_DIAL.typeOf(player.getHeldItem(handIn)))
			return false;
		if (worldIn.isRemote)
			return true;

		IWithContainer<?, ?> cte = (IWithContainer<?, ?>) te;
		NetworkHooks.openGui((ServerPlayerEntity) player, cte, cte::sendToContainer);
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
		return AllShapes.LOGISTICAL_CONTROLLER.get(state.get(FACING));
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
