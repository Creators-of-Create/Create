package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPoint.Mode;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ArmInteractionPointHandler {

	static List<ArmInteractionPoint> currentSelection = new ArrayList<>();
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

		ArmInteractionPoint selected = getSelected(pos);

		if (selected == null) {
			ArmInteractionPoint point = ArmInteractionPoint.createAt(world, pos);
			if (point == null)
				return;
			selected = point;
			put(point);
		}

		selected.cycleMode();
		PlayerEntity player = event.getPlayer();
		if (player != null) {
			String key = selected.mode == Mode.DEPOSIT ? "mechanical_arm.deposit_to" : "mechanical_arm.extract_from";
			TextFormatting colour = selected.mode == Mode.DEPOSIT ? TextFormatting.GOLD : TextFormatting.AQUA;
			TranslationTextComponent translatedBlock = new TranslationTextComponent(selected.state.getBlock()
				.getTranslationKey());
			player.sendStatusMessage((Lang.translate(key, translatedBlock.formatted(TextFormatting.WHITE, colour)).formatted(colour)),
				true);
		}

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
		if (remove(pos) != null) {
			event.setCanceled(true);
			event.setCancellationResult(ActionResultType.SUCCESS);
		}
	}

	public static void flushSettings(BlockPos pos) {
		if (currentItem == null)
			return;

		int removed = 0;
		for (Iterator<ArmInteractionPoint> iterator = currentSelection.iterator(); iterator.hasNext();) {
			ArmInteractionPoint point = iterator.next();
			if (point.pos.withinDistance(pos, ArmTileEntity.getRange()))
				continue;
			iterator.remove();
			removed++;
		}

		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (removed > 0) {
			player.sendStatusMessage(Lang.createTranslationTextComponent("mechanical_arm.points_outside_range", removed)
				.formatted(TextFormatting.RED), true);
		} else {
			int inputs = 0;
			int outputs = 0;
			for (ArmInteractionPoint armInteractionPoint : currentSelection) {
				if (armInteractionPoint.mode == Mode.DEPOSIT)
					outputs++;
				else
					inputs++;
			}
			if (inputs + outputs > 0)
				player.sendStatusMessage(Lang.createTranslationTextComponent("mechanical_arm.summary", inputs, outputs)
					.formatted(TextFormatting.WHITE), true);
		}

		AllPackets.channel.sendToServer(new ArmPlacementPacket(currentSelection, pos));
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
		if (!AllItems.WRENCH.isIn(heldItem)) {
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
			arm.inputs.forEach(ArmInteractionPointHandler::put);
			arm.outputs.forEach(ArmInteractionPointHandler::put);
			lastBlockPos = pos.toLong();
		}

		if (lastBlockPos != -1) {
			drawOutlines(currentSelection);
		}
	}

	private static void drawOutlines(Collection<ArmInteractionPoint> selection) {
		World world = Minecraft.getInstance().world;
		for (Iterator<ArmInteractionPoint> iterator = selection.iterator(); iterator.hasNext();) {
			ArmInteractionPoint point = iterator.next();
			BlockPos pos = point.pos;
			BlockState state = world.getBlockState(pos);

			if (!point.isValid(world, pos, state)) {
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

	private static void put(ArmInteractionPoint point) {
		currentSelection.add(point);
	}

	private static ArmInteractionPoint remove(BlockPos pos) {
		ArmInteractionPoint result = getSelected(pos);
		if (result != null)
			currentSelection.remove(result);
		return result;
	}

	private static ArmInteractionPoint getSelected(BlockPos pos) {
		for (ArmInteractionPoint point : currentSelection) {
			if (point.pos.equals(pos))
				return point;
		}
		return null;
	}

}
