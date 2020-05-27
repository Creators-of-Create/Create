package com.simibubi.create.content.contraptions.relays.gearbox;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.storage.loot.LootContext.Builder;

public class GearboxBlock extends RotatedPillarKineticBlock {

	public GearboxBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new GearboxTileEntity();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		super.fillItemGroup(group, items);
		items.add(AllItems.VERTICAL_GEARBOX.asStack());
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		if (state.get(AXIS).isVertical())
			return super.getDrops(state, builder);
		return Arrays.asList(new ItemStack(AllItems.VERTICAL_GEARBOX.get()));
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
			PlayerEntity player) {
		if (state.get(AXIS).isVertical())
			return super.getPickBlock(state, target, world, pos, player);
		return new ItemStack(AllItems.VERTICAL_GEARBOX.get());
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(AXIS, Axis.Y);
	}

	// IRotate:

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() != state.get(AXIS);
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(AXIS);
	}
}
