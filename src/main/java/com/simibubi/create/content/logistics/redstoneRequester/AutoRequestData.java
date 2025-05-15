package com.simibubi.create.content.logistics.redstoneRequester;

import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AutoRequestData {

	public PackageOrderWithCrafts encodedRequest = PackageOrderWithCrafts.empty();
	public String encodedTargetAdress = "";
	public BlockPos targetOffset = BlockPos.ZERO;
	public String targetDim = "";
	public boolean isValid = false;

	public static AutoRequestData read(CompoundTag tag) {
		AutoRequestData requestData = new AutoRequestData();
		requestData.targetOffset = NbtUtils.readBlockPos(tag.getCompound("TargetOffset"));
		requestData.targetDim = tag.getString("TargetDim");
		requestData.isValid = tag.getBoolean("Valid");
		requestData.encodedTargetAdress = tag.getString("EncodedAddress");
		requestData.encodedRequest = PackageOrderWithCrafts.read(tag.getCompound("EncodedRequest"));
		return requestData;
	}

	public void write(CompoundTag tag) {
		tag.put("TargetOffset", NbtUtils.writeBlockPos(targetOffset));
		tag.putString("TargetDim", targetDim);
		tag.putBoolean("Valid", isValid);
		tag.putString("EncodedAddress", encodedTargetAdress);
		tag.put("EncodedRequest", encodedRequest.write());
	}

	public void writeToItem(BlockPos position, ItemStack itemStack) {
		CompoundTag tag = itemStack.getOrCreateTag();
		write(tag);
		tag.put("TargetOffset", NbtUtils.writeBlockPos(position.offset(targetOffset)));
		tag.remove("Valid");
		itemStack.setTag(tag);
	}

	public static AutoRequestData readFromItem(Level level, Player player, BlockPos position, ItemStack itemStack) {
		CompoundTag tag = itemStack.getTag();
		if (tag == null || !tag.contains("TargetOffset"))
			return null;

		AutoRequestData requestData = read(tag);
		requestData.targetOffset = requestData.targetOffset.subtract(position);
		requestData.isValid =
			requestData.targetOffset.closerThan(BlockPos.ZERO, 128) && requestData.targetDim.equals(level.dimension()
				.location()
				.toString());

		if (player != null)
			CreateLang
				.translate(requestData.isValid ? "redstone_requester.keeper_connected"
					: "redstone_requester.keeper_too_far_away")
				.style(requestData.isValid ? ChatFormatting.WHITE : ChatFormatting.RED)
				.sendStatus(player);

		return requestData;
	}

}
