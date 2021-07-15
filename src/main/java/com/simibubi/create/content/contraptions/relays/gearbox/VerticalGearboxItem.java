package com.simibubi.create.content.contraptions.relays.gearbox;

import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.foundation.utility.Iterate;

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

import net.minecraft.item.Item.Properties;

public class VerticalGearboxItem extends BlockItem {

	public VerticalGearboxItem(Properties builder) {
		super(AllBlocks.GEARBOX.get(), builder);
	}

	@Override
	public void fillItemCategory(ItemGroup p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
	}
	
	@Override
	public String getDescriptionId() {
		return "item.create.vertical_gearbox";
	}

	@Override
	public void registerBlocks(Map<Block, Item> p_195946_1_, Item p_195946_2_) {
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, World world, PlayerEntity player, ItemStack stack, BlockState state) {
		Axis prefferedAxis = null;
		for (Direction side : Iterate.horizontalDirections) {
			BlockState blockState = world.getBlockState(pos.relative(side));
			if (blockState.getBlock() instanceof IRotate) {
				if (((IRotate) blockState.getBlock()).hasShaftTowards(world, pos.relative(side), blockState,
						side.getOpposite()))
					if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
						prefferedAxis = null;
						break;
					} else {
						prefferedAxis = side.getAxis();
					}
			}
		}

		Axis axis = prefferedAxis == null ? player.getDirection()
				.getClockWise()
				.getAxis() : prefferedAxis == Axis.X ? Axis.Z : Axis.X;
		world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.AXIS, axis));
		return super.updateCustomBlockEntityTag(pos, world, player, stack, state);
	}

}
