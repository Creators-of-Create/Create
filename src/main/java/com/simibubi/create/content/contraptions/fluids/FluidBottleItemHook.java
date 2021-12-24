package com.simibubi.create.content.contraptions.fluids;

import net.minecraft.world.item.Item;

public class FluidBottleItemHook extends Item {

	public FluidBottleItemHook(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

//	public static void preventWaterBottlesFromCreatesFluids(PlayerInteractEvent.RightClickItem event) {
//		ItemStack itemStack = event.getItemStack();
//		if (itemStack.isEmpty())
//			return;
//		if (!(itemStack.getItem() instanceof BottleItem))
//			return;
//
//		Level world = event.getWorld();
//		Player player = event.getPlayer();
//		HitResult raytraceresult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);
//		if (raytraceresult.getType() != HitResult.Type.BLOCK)
//			return;
//		BlockPos blockpos = ((BlockHitResult) raytraceresult).getBlockPos();
//		if (!world.mayInteract(player, blockpos))
//			return;
//
//		FluidState fluidState = world.getFluidState(blockpos);
//		if (fluidState.is(FluidTags.WATER) && fluidState.getType()
//			.getRegistryName()
//			.getNamespace()
//			.equals(Create.ID)) {
//			event.setCancellationResult(InteractionResult.PASS);
//			event.setCanceled(true);
//			return;
//		}
//
//		return;
//	}

}
