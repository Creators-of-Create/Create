package com.simibubi.create.content.contraptions.components.deployer;

import java.util.UUID;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MountedContraption;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class DeployerMovingInteraction extends MovingInteractionBehaviour {

	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		MutablePair<StructureBlockInfo, MovementContext> actor = contraptionEntity.getContraption()
			.getActorAt(localPos);
		if (actor == null || actor.right == null)
			return false;
		
		MovementContext ctx = actor.right;
		ItemStack heldStack = player.getItemInHand(activeHand);
		if (heldStack.getItem()
			.equals(AllItems.WRENCH.get())) {
			DeployerBlockEntity.Mode mode = NBTHelper.readEnum(ctx.blockEntityData, "Mode", DeployerBlockEntity.Mode.class);
			NBTHelper.writeEnum(ctx.blockEntityData, "Mode",
				mode == DeployerBlockEntity.Mode.PUNCH ? DeployerBlockEntity.Mode.USE : DeployerBlockEntity.Mode.PUNCH);

		} else {
			if (ctx.world.isClientSide)
				return true; // we'll try again on the server side
			DeployerFakePlayer fake = null;

			if (!(ctx.temporaryData instanceof DeployerFakePlayer) && ctx.world instanceof ServerLevel) {
				UUID owner = ctx.blockEntityData.contains("Owner") ? ctx.blockEntityData.getUUID("Owner") : null;
				DeployerFakePlayer deployerFakePlayer = new DeployerFakePlayer((ServerLevel) ctx.world, owner);
				deployerFakePlayer.onMinecartContraption = ctx.contraption instanceof MountedContraption;
				deployerFakePlayer.getInventory()
					.load(ctx.blockEntityData.getList("Inventory", Tag.TAG_COMPOUND));
				ctx.temporaryData = fake = deployerFakePlayer;
				ctx.blockEntityData.remove("Inventory");
			} else
				fake = (DeployerFakePlayer) ctx.temporaryData;

			if (fake == null)
				return false;

			ItemStack deployerItem = fake.getMainHandItem();
			player.setItemInHand(activeHand, deployerItem.copy());
			fake.setItemInHand(InteractionHand.MAIN_HAND, heldStack.copy());
			ctx.blockEntityData.put("HeldItem", heldStack.serializeNBT());
			ctx.data.put("HeldItem", heldStack.serializeNBT());
		}
//		if (index >= 0)
//			setContraptionActorData(contraptionEntity, index, info, ctx);
		return true;
	}
}
