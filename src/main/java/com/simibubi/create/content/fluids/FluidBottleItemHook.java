package com.simibubi.create.content.fluids;

import com.simibubi.create.Create;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class FluidBottleItemHook extends Item {

	private FluidBottleItemHook(Properties p) {
		super(p);
	}

	@SubscribeEvent
	public static void preventWaterBottlesFromCreatesFluids(PlayerInteractEvent.RightClickItem event) {
		ItemStack itemStack = event.getItemStack();
		if (itemStack.isEmpty())
			return;
		if (!(itemStack.getItem() instanceof BottleItem))
			return;

		Level world = event.getWorld();
		Player player = event.getPlayer();
		HitResult raytraceresult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);
		if (raytraceresult.getType() != HitResult.Type.BLOCK)
			return;
		BlockPos blockpos = ((BlockHitResult) raytraceresult).getBlockPos();
		if (!world.mayInteract(player, blockpos))
			return;

		FluidState fluidState = world.getFluidState(blockpos);
		if (fluidState.is(FluidTags.WATER) && CatnipServices.REGISTRIES.getKeyOrThrow(fluidState.getType())
			.getNamespace()
			.equals(Create.ID)) {
			event.setCancellationResult(InteractionResult.PASS);
			event.setCanceled(true);
			return;
		}

		return;
	}

}
