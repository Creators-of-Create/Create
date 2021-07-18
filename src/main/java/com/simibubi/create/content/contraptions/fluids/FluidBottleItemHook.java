package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.Create;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class FluidBottleItemHook extends Item {

	public FluidBottleItemHook(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@SubscribeEvent
	public static void preventWaterBottlesFromCreatesFluids(PlayerInteractEvent.RightClickItem event) {
		ItemStack itemStack = event.getItemStack();
		if (itemStack.isEmpty())
			return;
		if (!(itemStack.getItem() instanceof GlassBottleItem))
			return;

		World world = event.getWorld();
		PlayerEntity player = event.getPlayer();
		RayTraceResult raytraceresult = getPlayerPOVHitResult(world, player, RayTraceContext.FluidMode.SOURCE_ONLY);
		if (raytraceresult.getType() != RayTraceResult.Type.BLOCK)
			return;
		BlockPos blockpos = ((BlockRayTraceResult) raytraceresult).getBlockPos();
		if (!world.mayInteract(player, blockpos))
			return;

		FluidState fluidState = world.getFluidState(blockpos);
		if (fluidState.is(FluidTags.WATER) && fluidState.getType()
			.getRegistryName()
			.getNamespace()
			.equals(Create.ID)) {
			event.setCancellationResult(ActionResultType.PASS);
			event.setCanceled(true);
			return;
		}

		return;
	}

}
