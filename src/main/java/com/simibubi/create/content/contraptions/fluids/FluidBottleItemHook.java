package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.Create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class FluidBottleItemHook extends Item {

	public FluidBottleItemHook(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	public static InteractionResultHolder<ItemStack> preventWaterBottlesFromCreatesFluids(Player player, Level world, InteractionHand hand) {
		ItemStack itemStack = player.getItemInHand(hand);
		if (itemStack.isEmpty())
			return InteractionResultHolder.pass(itemStack);
		if (!(itemStack.getItem() instanceof BottleItem))
			return InteractionResultHolder.pass(itemStack);

//		Level world = event.getWorld();
//		Player player = event.getPlayer();
		HitResult raytraceresult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);
		if (raytraceresult.getType() != HitResult.Type.BLOCK)
			return InteractionResultHolder.pass(itemStack);
		BlockPos blockpos = ((BlockHitResult) raytraceresult).getBlockPos();
		if (!world.mayInteract(player, blockpos))
			return InteractionResultHolder.pass(itemStack);

		FluidState fluidState = world.getFluidState(blockpos);
		if (fluidState.is(FluidTags.WATER) && Registry.FLUID.getKey(fluidState.getType())
			.getNamespace()
			.equals(Create.ID)) {
			return InteractionResultHolder.fail(itemStack);
//			event.setCancellationResult(InteractionResult.PASS);
//			event.setCanceled(true);
//			return;

		}

		return InteractionResultHolder.pass(itemStack);
	}

}
