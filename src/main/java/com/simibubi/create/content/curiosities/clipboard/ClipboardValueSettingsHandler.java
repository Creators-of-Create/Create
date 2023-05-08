package com.simibubi.create.content.curiosities.clipboard;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.curiosities.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.content.logistics.trains.track.TrackBlockOutline;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ClipboardValueSettingsHandler {

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void drawCustomBlockSelection(RenderHighlightEvent.Block event) {
		Minecraft mc = Minecraft.getInstance();
		BlockHitResult target = event.getTarget();
		BlockPos pos = target.getBlockPos();
		BlockState blockstate = mc.level.getBlockState(pos);

		if (mc.player == null || mc.player.isSpectator())
			return;
		if (!mc.level.getWorldBorder()
			.isWithinBounds(pos))
			return;
		if (!AllBlocks.CLIPBOARD.isIn(mc.player.getMainHandItem()))
			return;
		if (!(mc.level.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
			return;
		if (!smartBE.getAllBehaviours()
			.stream()
			.anyMatch(b -> b instanceof ClipboardCloneable cc
				&& cc.writeToClipboard(new CompoundTag(), target.getDirection())))
			return;

		VoxelShape shape = blockstate.getShape(mc.level, pos);
		if (shape.isEmpty())
			return;

		VertexConsumer vb = event.getMultiBufferSource()
			.getBuffer(RenderType.lines());
		Vec3 camPos = event.getCamera()
			.getPosition();

		PoseStack ms = event.getPoseStack();

		ms.pushPose();
		ms.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
		TrackBlockOutline.renderShape(shape, ms, vb, true);
		event.setCanceled(true);

		ms.popPose();
	}

	@OnlyIn(Dist.CLIENT)
	public static void clientTick() {
		Minecraft mc = Minecraft.getInstance();
		if (!(mc.hitResult instanceof BlockHitResult target))
			return;
		if (!AllBlocks.CLIPBOARD.isIn(mc.player.getMainHandItem()))
			return;
		BlockPos pos = target.getBlockPos();
		if (!(mc.level.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
			return;

		CompoundTag tagElement = mc.player.getMainHandItem()
			.getTagElement("CopiedValues");

		boolean canCopy = smartBE.getAllBehaviours()
			.stream()
			.anyMatch(b -> b instanceof ClipboardCloneable cc
				&& cc.writeToClipboard(new CompoundTag(), target.getDirection()))
			|| smartBE instanceof ClipboardCloneable ccbe
				&& ccbe.writeToClipboard(new CompoundTag(), target.getDirection());

		boolean canPaste = tagElement != null && (smartBE.getAllBehaviours()
			.stream()
			.anyMatch(b -> b instanceof ClipboardCloneable cc && cc.readFromClipboard(
				tagElement.getCompound(cc.getClipboardKey()), mc.player, target.getDirection(), true))
			|| smartBE instanceof ClipboardCloneable ccbe && ccbe.readFromClipboard(
				tagElement.getCompound(ccbe.getClipboardKey()), mc.player, target.getDirection(), true));

		if (!canCopy && !canPaste)
			return;

		List<MutableComponent> tip = new ArrayList<>();
		tip.add(Lang.translateDirect("clipboard.actions"));
		if (canCopy)
			tip.add(Lang.translateDirect("clipboard.to_copy", Components.keybind("key.use")));
		if (canPaste)
			tip.add(Lang.translateDirect("clipboard.to_paste", Components.keybind("key.attack")));

		CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
	}

	@SubscribeEvent
	public static void rightClickToCopy(PlayerInteractEvent.RightClickBlock event) {
		interact(event, false);
	}

	@SubscribeEvent
	public static void leftClickToPaste(PlayerInteractEvent.LeftClickBlock event) {
		interact(event, true);
	}

	private static void interact(PlayerInteractEvent event, boolean paste) {
		ItemStack itemStack = event.getItemStack();
		if (!AllBlocks.CLIPBOARD.isIn(itemStack))
			return;

		BlockPos pos = event.getPos();
		Level world = event.getLevel();
		Player player = event.getEntity();
		if (player != null && player.isSpectator())
			return;
		if (player.isSteppingCarefully())
			return;
		if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
			return;
		CompoundTag tag = itemStack.getTagElement("CopiedValues");
		if (paste && tag == null)
			return;
		if (!paste)
			tag = new CompoundTag();

		boolean anySuccess = false;
		boolean anyValid = false;
		for (BlockEntityBehaviour behaviour : smartBE.getAllBehaviours()) {
			if (!(behaviour instanceof ClipboardCloneable cc))
				continue;
			anyValid = true;
			String clipboardKey = cc.getClipboardKey();
			if (paste) {
				anySuccess |=
					cc.readFromClipboard(tag.getCompound(clipboardKey), player, event.getFace(), world.isClientSide());
				continue;
			}
			CompoundTag compoundTag = new CompoundTag();
			boolean success = cc.writeToClipboard(compoundTag, event.getFace());
			anySuccess |= success;
			if (success)
				tag.put(clipboardKey, compoundTag);
		}

		if (smartBE instanceof ClipboardCloneable ccbe) {
			anyValid = true;
			String clipboardKey = ccbe.getClipboardKey();
			if (paste) {
				anySuccess |= ccbe.readFromClipboard(tag.getCompound(clipboardKey), player, event.getFace(),
					world.isClientSide());
			} else {
				CompoundTag compoundTag = new CompoundTag();
				boolean success = ccbe.writeToClipboard(compoundTag, event.getFace());
				anySuccess |= success;
				if (success)
					tag.put(clipboardKey, compoundTag);
			}
		}

		if (!anyValid)
			return;

		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);

		if (world.isClientSide())
			return;
		if (!anySuccess)
			return;

		player.displayClientMessage(Lang
			.translate(paste ? "clipboard.pasted_to" : "clipboard.copied_from", world.getBlockState(pos)
				.getBlock()
				.getName()
				.withStyle(ChatFormatting.WHITE))
			.style(ChatFormatting.GREEN)
			.component(), true);

		if (!paste) {
			ClipboardOverrides.switchTo(ClipboardType.WRITTEN, itemStack);
			itemStack.getOrCreateTag()
				.put("CopiedValues", tag);
		}
	}

}
