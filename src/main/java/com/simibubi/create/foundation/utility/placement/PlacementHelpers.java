package com.simibubi.create.foundation.utility.placement;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlacementHelpers {

	private static final List<IPlacementHelper> helpers = new ArrayList<>();

	public static int register(IPlacementHelper helper) {
		helpers.add(helper);
		return helpers.size() - 1;
	}

	public static IPlacementHelper get(int id) {
		if (id < 0 || id >= helpers.size())
			throw new ArrayIndexOutOfBoundsException("id " + id + " for placement helper not known");

		return helpers.get(id);
	}

	@OnlyIn(Dist.CLIENT)
	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;

		if (world == null)
			return;

		if (!(mc.objectMouseOver instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult ray = (BlockRayTraceResult) mc.objectMouseOver;

		if (mc.player == null)
			return;

		List<IPlacementHelper> filteredForHeldItem = helpers.stream().filter(helper -> Arrays.stream(Hand.values()).anyMatch(hand -> helper.getItemPredicate().test(mc.player.getHeldItem(hand)))).collect(Collectors.toList());
		if (filteredForHeldItem.isEmpty())
			return;

		if (mc.player.isSneaking())//for now, disable all helpers when sneaking TODO add helpers that respect sneaking but still show position
			return;

		BlockPos pos = ray.getPos();
		BlockState state = world.getBlockState(pos);

		List<IPlacementHelper> filteredForState = filteredForHeldItem.stream().filter(helper -> helper.getStatePredicate().test(state)).collect(Collectors.toList());

		if (filteredForState.isEmpty())
			return;

		for (IPlacementHelper h : filteredForState) {
			PlacementOffset offset = h.getOffset(world, state, pos, ray);

			if (offset.isSuccessful()) {
				h.renderAt(pos, state, ray, offset);
				break;
			}

		}
	}
}
