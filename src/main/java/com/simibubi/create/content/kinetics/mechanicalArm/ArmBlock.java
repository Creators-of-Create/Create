package com.simibubi.create.content.kinetics.mechanicalArm;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity.Phase;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
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

public class ArmBlock extends KineticBlock implements IBE<ArmBlockEntity>, ICogWheel {

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
		withBlockEntityDo(world, pos, ArmBlockEntity::redstoneUpdate);
	}
	
	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_,
		BlockPos p_220069_5_, boolean p_220069_6_) {
		withBlockEntityDo(world, pos, ArmBlockEntity::redstoneUpdate);
	}
	
	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public Class<ArmBlockEntity> getBlockEntityClass() {
		return ArmBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ArmBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.MECHANICAL_ARM.get();
	}
	
	@Override
	public InteractionResult use(BlockState p_225533_1_, Level world, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult p_225533_6_) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (AllItems.GOGGLES.isIn(heldItem)) {
			InteractionResult gogglesResult = onBlockEntityUse(world, pos, ate -> {
				if (ate.goggles)
					return InteractionResult.PASS;
				ate.goggles = true;
				ate.notifyUpdate();
				return InteractionResult.SUCCESS;
			});
			if (gogglesResult.consumesAction())
				return gogglesResult;
		}

		MutableBoolean success = new MutableBoolean(false);
		withBlockEntityDo(world, pos, be -> {
			if (be.heldItem.isEmpty())
				return;
			success.setTrue();
			if (world.isClientSide)
				return;
			player.getInventory().placeItemBackInInventory(be.heldItem);
			be.heldItem = ItemStack.EMPTY;
			be.phase = Phase.SEARCH_INPUTS;
			be.setChanged();
			be.sendData();
		});
		
		return success.booleanValue() ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

}
