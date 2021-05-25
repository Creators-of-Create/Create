package com.simibubi.create.content.contraptions.components.crank;

import javax.annotation.ParametersAreNonnullByDefault;

import com.jozufozu.flywheel.backend.core.PartialModel;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.DyeHelper;

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
	private final boolean inCreativeTab;

	public static ValveHandleBlock copper(Properties properties) {
		return new ValveHandleBlock(properties, true);
	}

	public static ValveHandleBlock dyed(Properties properties) {
		return new ValveHandleBlock(properties, false);
	}

	private ValveHandleBlock(Properties properties, boolean inCreativeTab) {
		super(properties);
		this.inCreativeTab = inCreativeTab;
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		ItemStack heldItem = player.getHeldItem(handIn);
		for (DyeColor color : DyeColor.values()) {
			if (!heldItem.getItem()
					.isIn(DyeHelper.getTagOfDye(color)))
				continue;
			if (worldIn.isRemote)
				return ActionResultType.SUCCESS;

			BlockState newState = AllBlocks.DYED_VALVE_HANDLES[color.ordinal()]
					.getDefaultState()
					.with(FACING, state.get(FACING));
			if (newState != state)
				worldIn.setBlockState(pos, newState);
			return ActionResultType.SUCCESS;
		}

		return super.onUse(state, worldIn, pos, player, handIn, hit);
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> p_149666_2_) {
		if (group != ItemGroup.SEARCH && !inCreativeTab)
			return;
		super.fillItemGroup(group, p_149666_2_);
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
