package com.simibubi.create.content.equipment.zapper;

import java.util.Objects;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ZapperInteractionHandler {

	@SubscribeEvent
	public static void leftClickingBlocksWithTheZapperSelectsTheBlock(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getLevel().isClientSide)
			return;
		ItemStack heldItem = event.getEntity()
			.getMainHandItem();
		if (heldItem.getItem() instanceof ZapperItem && trySelect(heldItem, event.getEntity())) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
		}
	}

	public static boolean trySelect(ItemStack stack, Player player) {
		if (player.isShiftKeyDown())
			return false;

		Vec3 start = player.position()
			.add(0, player.getEyeHeight(), 0);
		Vec3 range = player.getLookAngle()
			.scale(getRange(stack));
		BlockHitResult raytrace = player.level
			.clip(new ClipContext(start, start.add(range), Block.OUTLINE, Fluid.NONE, player));
		BlockPos pos = raytrace.getBlockPos();
		if (pos == null)
			return false;

		player.level.destroyBlockProgress(player.getId(), pos, -1);
		BlockState newState = player.level.getBlockState(pos);

		if (BlockHelper.getRequiredItem(newState)
			.isEmpty())
			return false;
		if (newState.hasBlockEntity() && !AllBlockTags.SAFE_NBT.matches(newState))
			return false;
		if (newState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF))
			return false;
		if (newState.hasProperty(BlockStateProperties.ATTACHED))
			return false;
		if (newState.hasProperty(BlockStateProperties.HANGING))
			return false;
		if (newState.hasProperty(BlockStateProperties.BED_PART))
			return false;
		if (newState.hasProperty(BlockStateProperties.STAIRS_SHAPE))
			newState = newState.setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.STRAIGHT);
		if (newState.hasProperty(BlockStateProperties.PERSISTENT))
			newState = newState.setValue(BlockStateProperties.PERSISTENT, true);
		if (newState.hasProperty(BlockStateProperties.WATERLOGGED))
			newState = newState.setValue(BlockStateProperties.WATERLOGGED, false);

		CompoundTag data = null;
		BlockEntity blockEntity = player.level.getBlockEntity(pos);
		if (blockEntity != null) {
			data = blockEntity.saveWithFullMetadata();
			data.remove("x");
			data.remove("y");
			data.remove("z");
			data.remove("id");
		}
		CompoundTag tag = stack.getOrCreateTag();
		if (tag.contains("BlockUsed") && NbtUtils.readBlockState(player.level.holderLookup(Registries.BLOCK), stack.getTag()
			.getCompound("BlockUsed")) == newState && Objects.equals(data, tag.get("BlockData"))) {
			return false;
		}

		tag.put("BlockUsed", NbtUtils.writeBlockState(newState));
		if (data == null)
			tag.remove("BlockData");
		else
			tag.put("BlockData", data);

		AllSoundEvents.CONFIRM.playOnServer(player.level, player.blockPosition());
		return true;
	}

	public static int getRange(ItemStack stack) {
		if (stack.getItem() instanceof ZapperItem)
			return ((ZapperItem) stack.getItem()).getZappingRange(stack);
		return 0;
	}
}
