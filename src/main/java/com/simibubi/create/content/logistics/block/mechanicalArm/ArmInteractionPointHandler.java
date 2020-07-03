package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPoint.Mode;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ArmInteractionPointHandler {

	static Map<BlockPos, ArmInteractionPoint> currentSelection = new HashMap<>();
	static ItemStack currentItem;

	static long lastBlockPos = -1;

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

		if (player == null)
			return;

		ItemStack heldItemMainhand = player.getHeldItemMainhand();
		if (!AllBlocks.MECHANICAL_ARM.isIn(heldItemMainhand)) {
			currentItem = null;
		} else {
			if (heldItemMainhand != currentItem) {
				currentSelection.clear();
				currentItem = heldItemMainhand;
			}

			drawOutlines(currentSelection);
		}

		checkForWrench(heldItemMainhand);
	}

	private static void checkForWrench(ItemStack heldItem) {
		if(!AllItems.WRENCH.isIn(heldItem)) {
			return;
		}

		RayTraceResult objectMouseOver = Minecraft.getInstance().objectMouseOver;
		if (!(objectMouseOver instanceof BlockRayTraceResult)) {
			return;
		}

		BlockRayTraceResult result = (BlockRayTraceResult) objectMouseOver;
		BlockPos pos = result.getPos();

		TileEntity te = Minecraft.getInstance().world.getTileEntity(pos);
		if (!(te instanceof ArmTileEntity)) {
			lastBlockPos = -1;
			currentSelection.clear();
			return;
		}

		if (lastBlockPos == -1 || lastBlockPos != pos.toLong()) {
			currentSelection.clear();
			ArmTileEntity arm = (ArmTileEntity) te;
			arm.inputs.forEach(point -> currentSelection.put(point.pos, point));
			arm.outputs.forEach(point -> currentSelection.put(point.pos, point));
			lastBlockPos = pos.toLong();
		}

		if (lastBlockPos != -1) {
			drawOutlines(currentSelection);
		}
	}

	private static void drawOutlines(Map<BlockPos, ArmInteractionPoint> selection) {
		World world = Minecraft.getInstance().world;
		for (Iterator<Entry<BlockPos, ArmInteractionPoint>> iterator = selection.entrySet()
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
