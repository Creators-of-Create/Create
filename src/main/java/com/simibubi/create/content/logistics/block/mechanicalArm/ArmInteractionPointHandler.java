package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPoint.Mode;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ArmInteractionPointHandler {

	static Map<BlockPos, ArmInteractionPoint> currentSelection = new HashMap<>();
	static ItemStack currentItem;

	@SubscribeEvent
	public static void rightClickingBlocksSelectsThem(PlayerInteractEvent.RightClickBlock event) {
		if (currentItem == null)
			return;
		BlockPos pos = event.getPos();
		World world = event.getWorld();
		if (!world.isRemote)
			return;

		if (!currentSelection.containsKey(pos)) {
			ArmInteractionPoint point = ArmInteractionPoint.createAt(world, pos);
			if (point == null)
				return;
			currentSelection.put(pos, point);
		}

		currentSelection.get(pos)
			.cycleMode();
		event.setCanceled(true);
		event.setCancellationResult(ActionResultType.SUCCESS);
	}

	@SubscribeEvent
	public static void leftClickingBlocksDeselectsThem(PlayerInteractEvent.LeftClickBlock event) {
		if (currentItem == null)
			return;
		if (!event.getWorld().isRemote)
			return;
		BlockPos pos = event.getPos();
		if (currentSelection.remove(pos) != null) {
			event.setCanceled(true);
			event.setCancellationResult(ActionResultType.SUCCESS);
		}
	}

	public static void flushSettings(BlockPos pos) {
		if (currentItem == null)
			return;
		AllPackets.channel.sendToServer(new ArmPlacementPacket(currentSelection.values(), pos));
		currentSelection.clear();
		currentItem = null;
	}

	public static void tick() {
		PlayerEntity player = Minecraft.getInstance().player;
		World world = Minecraft.getInstance().world;
		if (player == null)
			return;

		ItemStack heldItemMainhand = player.getHeldItemMainhand();
		if (!AllBlocks.MECHANICAL_ARM.isIn(heldItemMainhand)) {
			currentItem = null;
			return;
		}
		if (heldItemMainhand != currentItem) {
			currentSelection.clear();
			currentItem = heldItemMainhand;
		}

		for (Iterator<Entry<BlockPos, ArmInteractionPoint>> iterator = currentSelection.entrySet()
			.iterator(); iterator.hasNext();) {
			Entry<BlockPos, ArmInteractionPoint> entry = iterator.next();
			BlockPos pos = entry.getKey();
			BlockState state = world.getBlockState(pos);
			ArmInteractionPoint point = entry.getValue();

			if (!point.isValid(state)) {
				iterator.remove();
				continue;
			}

			VoxelShape shape = state.getShape(world, pos);
			if (shape.isEmpty())
				continue;

			int color = point.mode == Mode.DEPOSIT ? 0xffcb74 : 0x4f8a8b;
			CreateClient.outliner.showAABB(point, shape.getBoundingBox()
				.offset(pos))
				.colored(color)
				.lineWidth(1 / 16f);
		}
	}

}
