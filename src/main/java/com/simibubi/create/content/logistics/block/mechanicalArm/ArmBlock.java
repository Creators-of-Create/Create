package com.simibubi.create.content.logistics.block.mechanicalArm;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmTileEntity.Phase;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ArmBlock extends KineticBlock implements ITE<ArmTileEntity>, ICogWheel {

	public static final BooleanProperty CEILING = BooleanProperty.create("ceiling");

	public ArmBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(CEILING, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> p_206840_1_) {
		super.createBlockStateDefinition(p_206840_1_.add(CEILING));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState().setValue(CEILING, ctx.getClickedFace() == Direction.DOWN);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return state.getValue(CEILING) ? AllShapes.MECHANICAL_ARM_CEILING : AllShapes.MECHANICAL_ARM;
	}
	
	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, world, pos, oldState, isMoving);
		withTileEntityDo(world, pos, ArmTileEntity::redstoneUpdate);
	}
	
	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_,
		BlockPos p_220069_5_, boolean p_220069_6_) {
		withTileEntityDo(world, pos, ArmTileEntity::redstoneUpdate);
	}
	
	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public Class<ArmTileEntity> getTileEntityClass() {
		return ArmTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends ArmTileEntity> getTileEntityType() {
		return AllTileEntities.MECHANICAL_ARM.get();
	}
	
	@Override
	public void onRemove(BlockState p_196243_1_, Level world, BlockPos pos, BlockState p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasBlockEntity()
			&& (p_196243_1_.getBlock() != p_196243_4_.getBlock() || !p_196243_4_.hasBlockEntity())) {
			withTileEntityDo(world, pos, te -> {
				if (!te.heldItem.isEmpty())
					Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), te.heldItem);
			});
			world.removeBlockEntity(pos);
		}
	}

	@Override
	public InteractionResult use(BlockState p_225533_1_, Level world, BlockPos pos, Player player,
		InteractionHand p_225533_5_, BlockHitResult p_225533_6_) {
		MutableBoolean success = new MutableBoolean(false);
		withTileEntityDo(world, pos, te -> {
			if (te.heldItem.isEmpty())
				return;
			success.setTrue();
			if (world.isClientSide)
				return;
			player.getInventory().placeItemBackInInventory(te.heldItem);
			te.heldItem = ItemStack.EMPTY;
			te.phase = Phase.SEARCH_INPUTS;
			te.setChanged();
			te.sendData();
		});
		
		return success.booleanValue() ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

}
