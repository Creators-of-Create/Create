package com.simibubi.create.content.contraptions.components.crank;

import javax.annotation.ParametersAreNonnullByDefault;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@ParametersAreNonnullByDefault
public class ValveHandleBlock extends HandCrankBlock {

	private final DyeColor color;
	private final boolean inCreativeTab;

	public static ValveHandleBlock copper(Properties properties) {
		return new ValveHandleBlock(properties, null, true);
	}

	public static ValveHandleBlock dyed(Properties properties, DyeColor color) {
		return new ValveHandleBlock(properties, color, false);
	}

	private ValveHandleBlock(Properties properties, DyeColor color, boolean inCreativeTab) {
		super(properties);
		this.color = color;
		this.inCreativeTab = inCreativeTab;
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult hit) {
		ItemStack heldItem = player.getItemInHand(hand);
		DyeColor color = DyeColor.getColor(heldItem);
		if (color != null && color != this.color) {
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			BlockState newState = BlockHelper.copyProperties(state, AllBlocks.DYED_VALVE_HANDLES.get(color).getDefaultState());
			world.setBlockAndUpdate(pos, newState);
			return ActionResultType.SUCCESS;
		}

		return super.use(state, world, pos, player, hand, hit);
	}

	@Override
	public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> p_149666_2_) {
		if (group != ItemGroup.TAB_SEARCH && !inCreativeTab)
			return;
		super.fillItemCategory(group, p_149666_2_);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public PartialModel getRenderedHandle() {
		return null;
	}

	@Override
	public int getRotationSpeed() {
		return 16;
	}

}
