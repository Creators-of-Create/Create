package com.simibubi.create.content.curiosities.zapper;

import java.util.Objects;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ZapperInteractionHandler {

	@SubscribeEvent
	public static void leftClickingBlocksWithTheZapperSelectsTheBlock(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getWorld().isClientSide)
			return;
		ItemStack heldItem = event.getPlayer()
			.getMainHandItem();
		if (heldItem.getItem() instanceof ZapperItem && trySelect(heldItem, event.getPlayer())) {
			event.setCancellationResult(ActionResultType.FAIL);
			event.setCanceled(true);
		}
	}

	public static boolean trySelect(ItemStack stack, PlayerEntity player) {
		if (player.isShiftKeyDown())
			return false;

		Vector3d start = player.position()
			.add(0, player.getEyeHeight(), 0);
		Vector3d range = player.getLookAngle()
			.scale(getRange(stack));
		BlockRayTraceResult raytrace = player.level
			.clip(new RayTraceContext(start, start.add(range), BlockMode.OUTLINE, FluidMode.NONE, player));
		BlockPos pos = raytrace.getBlockPos();
		if (pos == null)
			return false;

		player.level.destroyBlockProgress(player.getId(), pos, -1);
		BlockState newState = player.level.getBlockState(pos);

		if (BlockHelper.getRequiredItem(newState)
			.isEmpty())
			return false;
		if (newState.hasTileEntity() && !AllBlockTags.SAFE_NBT.matches(newState))
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

		CompoundNBT data = null;
		TileEntity tile = player.level.getBlockEntity(pos);
		if (tile != null) {
			data = tile.save(new CompoundNBT());
			data.remove("x");
			data.remove("y");
			data.remove("z");
			data.remove("id");
		}
		CompoundNBT tag = stack.getOrCreateTag();
		if (tag.contains("BlockUsed") && NBTUtil.readBlockState(stack.getTag()
			.getCompound("BlockUsed")) == newState && Objects.equals(data, tag.get("BlockData"))) {
			return false;
		}

		tag.put("BlockUsed", NBTUtil.writeBlockState(newState));
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
