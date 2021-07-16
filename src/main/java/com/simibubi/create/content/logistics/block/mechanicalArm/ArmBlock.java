package com.simibubi.create.content.logistics.block.mechanicalArm;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmTileEntity.Phase;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class ArmBlock extends KineticBlock implements ITE<ArmTileEntity>, ICogWheel {

	public static final BooleanProperty CEILING = BooleanProperty.create("ceiling");

	public ArmBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(CEILING, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(CEILING));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		return defaultBlockState().setValue(CEILING, ctx.getClickedFace() == Direction.DOWN);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader pWorldIn, BlockPos pPos,
		ISelectionContext pContext) {
		return state.getValue(CEILING) ? AllShapes.MECHANICAL_ARM_CEILING : AllShapes.MECHANICAL_ARM;
	}
	
	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, world, pos, oldState, isMoving);
		withTileEntityDo(world, pos, ArmTileEntity::redstoneUpdate);
	}
	
	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block pBlockIn,
		BlockPos pFromPos, boolean pIsMoving) {
		withTileEntityDo(world, pos, ArmTileEntity::redstoneUpdate);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.MECHANICAL_ARM.create();
	}

	@Override
	public Class<ArmTileEntity> getTileEntityClass() {
		return ArmTileEntity.class;
	}

	@Override
	public void onRemove(BlockState pState, World world, BlockPos pos, BlockState pNewState,
		boolean pIsMoving) {
		if (pState.hasTileEntity()
			&& (pState.getBlock() != pNewState.getBlock() || !pNewState.hasTileEntity())) {
			withTileEntityDo(world, pos, te -> {
				if (!te.heldItem.isEmpty())
					InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), te.heldItem);
			});
			world.removeBlockEntity(pos);
		}
	}

	@Override
	public ActionResultType use(BlockState pState, World world, BlockPos pos, PlayerEntity player,
		Hand pHandIn, BlockRayTraceResult pHit) {
		MutableBoolean success = new MutableBoolean(false);
		withTileEntityDo(world, pos, te -> {
			if (te.heldItem.isEmpty())
				return;
			success.setTrue();
			if (world.isClientSide)
				return;
			player.inventory.placeItemBackInInventory(world, te.heldItem);
			te.heldItem = ItemStack.EMPTY;
			te.phase = Phase.SEARCH_INPUTS;
			te.setChanged();
			te.sendData();
		});
		
		return success.booleanValue() ? ActionResultType.SUCCESS : ActionResultType.PASS;
	}

}
