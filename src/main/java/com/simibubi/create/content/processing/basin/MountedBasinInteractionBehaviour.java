package com.simibubi.create.content.processing.basin;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import com.simibubi.create.content.contraptions.MountedStorageManager;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MountedBasinInteractionBehaviour extends MovingInteractionBehaviour {
	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
		Level level = player.level();
		if (level.isClientSide)
			return true;

		MountedStorageManager manager = contraptionEntity.getContraption().getStorage();
		MountedItemStorage storage = manager.getAllItemStorages().get(localPos);
		if (!(storage instanceof BasinMountedItemStorage basin))
			return false;

		boolean success = false;
		for (int slot = 0; slot < basin.getSlots(); slot++) {
			ItemStack stackInSlot = basin.getStackInSlot(slot);
			if (stackInSlot.isEmpty())
				continue;
			player.getInventory()
				.placeItemBackInInventory(stackInSlot);
			basin.setStackInSlot(slot, ItemStack.EMPTY);
			success = true;
		}
		if (success) {
			BlockPos soundPos = BlockPos.containing(contraptionEntity.toGlobalVector(Vec3.atCenterOf(localPos), 0));
			level.playSound(null, soundPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f,
				1f + level.getRandom().nextFloat());
		}

		return success;
	}
}
