package com.simibubi.create.modules.contraptions.relays.gearbox;

import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.IRotate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VerticalGearboxItem extends BlockItem {

	public VerticalGearboxItem(Properties builder) {
		super(AllBlocks.GEARBOX.get(), builder);
	}

	@Override
	public void fillItemGroup(ItemGroup p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
	}

	@Override
	public void addToBlockToItemMap(Map<Block, Item> p_195946_1_, Item p_195946_2_) {
	}

	@Override
	protected boolean onBlockPlaced(BlockPos pos, World world, PlayerEntity player, ItemStack stack, BlockState state) {
		Axis prefferedAxis = null;
		for (Direction side : Direction.values()) {
			if (side.getAxis()
					.isVertical())
				continue;
			BlockState blockState = world.getBlockState(pos.offset(side));
			if (blockState.getBlock() instanceof IRotate) {
				if (((IRotate) blockState.getBlock()).hasShaftTowards(world, pos.offset(side), blockState,
						side.getOpposite()))
					if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
						prefferedAxis = null;
						break;
					} else {
						prefferedAxis = side.getAxis();
					}
			}
		}

		Axis axis = prefferedAxis == null ? player.getHorizontalFacing()
				.rotateY()
				.getAxis() : prefferedAxis == Axis.X ? Axis.Z : Axis.X;
		world.setBlockState(pos, state.with(BlockStateProperties.AXIS, axis));
		return super.onBlockPlaced(pos, world, player, stack, state);
	}

}
