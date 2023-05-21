package com.simibubi.create.content.kinetics.gearbox;

import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class VerticalGearboxItem extends BlockItem {

	public VerticalGearboxItem(Properties builder) {
		super(AllBlocks.GEARBOX.get(), builder);
	}

	@Override
	public void fillItemCategory(CreativeModeTab p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
	}
	
	@Override
	public String getDescriptionId() {
		return "item.create.vertical_gearbox";
	}

	@Override
	public void registerBlocks(Map<Block, Item> p_195946_1_, Item p_195946_2_) {
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, Player player, ItemStack stack, BlockState state) {
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
