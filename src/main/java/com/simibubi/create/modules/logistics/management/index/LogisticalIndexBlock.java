package com.simibubi.create.modules.logistics.management.index;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IHaveColorHandler;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.AllShapes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class LogisticalIndexBlock extends HorizontalBlock
		implements IHaveColorHandler, IWithTileEntity<LogisticalIndexTileEntity> {

	public LogisticalIndexBlock() {
		super(Properties.from(Blocks.GRANITE));
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction facing = state.get(HORIZONTAL_FACING);
		BlockPos offset = pos.offset(facing.getOpposite());
		BlockState blockState = worldIn.getBlockState(offset);
		return !blockState.getMaterial().isReplaceable();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
		super.fillStateContainer(builder);
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		super.fillItemGroup(group, items);
		AllItems[] logisiticalItems = new AllItems[] { AllItems.LOGISTICAL_DIAL, AllItems.LOGISTICAL_CONTROLLER_STORAGE,
				AllItems.LOGISTICAL_CONTROLLER_SUPPLY, AllItems.LOGISTICAL_CONTROLLER_REQUEST,
				AllItems.LOGISTICAL_CONTROLLER_CALCULATION, AllItems.LOGISTICAL_CONTROLLER_TRANSACTIONS };
		for (AllItems item : logisiticalItems) {
			item.get().fillItemGroup(group, items);
		}
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState defaultState = getDefaultState();
		if (context.getFace().getAxis().isHorizontal())
			return defaultState.with(HORIZONTAL_FACING, context.getFace());
		return defaultState;
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
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new LogisticalIndexTileEntity();
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (AllItems.LOGISTICAL_DIAL.typeOf(player.getHeldItem(handIn))) {
			return false;
		}

		if (worldIn.isRemote) {
			return true;
		} else {
			withTileEntityDo(worldIn, pos, te -> {
				NetworkHooks.openGui((ServerPlayerEntity) player, te, te::sendToContainer);
			});
			return true;
		}
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
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.LOGISTICAL_INDEX.get(state.get(HORIZONTAL_FACING));
	}

	@Override
	public IBlockColor getColorHandler() {
		return (state, world, pos, layer) -> {
			if (layer == 0) {
				LogisticalIndexTileEntity tileEntity = getTileEntity(world, pos);
				if (tileEntity == null)
					return 0;
				return tileEntity.getColor();
			}

			return 0;
		};
	}

}
