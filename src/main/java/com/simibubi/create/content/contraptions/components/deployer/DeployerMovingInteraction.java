package com.simibubi.create.content.contraptions.components.deployer;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.MovingInteractionBehaviour;

import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import org.apache.commons.lang3.tuple.MutablePair;

public class DeployerMovingInteraction extends MovingInteractionBehaviour {
	@Override
	public boolean handlePlayerInteraction (PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
		BlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
		if (info == null) return false;
		MovementContext ctx = null;
		int index = -1;
		for (MutablePair<BlockInfo, MovementContext> pair : contraptionEntity.getContraption().getActors()) {
			if (info.equals(pair.left)) {
				ctx = pair.right;
				index = contraptionEntity.getContraption().getActors().indexOf(pair);
				break;
			}
		}
		if (ctx == null) return false;

		ItemStack heldStack = player.getItemInHand(activeHand);
	//	Create.LOGGER.info("<-CTX: " + ctx.data.toString());
		if (heldStack.getItem().equals(AllItems.WRENCH.get())) {
			DeployerTileEntity.Mode mode = NBTHelper.readEnum(ctx.tileData, "Mode", DeployerTileEntity.Mode.class);
			NBTHelper.writeEnum(ctx.tileData, "Mode",
				mode==DeployerTileEntity.Mode.PUNCH ? DeployerTileEntity.Mode.USE : DeployerTileEntity.Mode.PUNCH
			);
		//	Create.LOGGER.info("Changed mode");
		} else {
			if (ctx.world.isClientSide) return true; // we'll try again on the server side
			DeployerFakePlayer fake = null;
			if ( !(ctx.temporaryData instanceof DeployerFakePlayer) && ctx.world instanceof ServerWorld) {
				ctx.temporaryData = new DeployerFakePlayer((ServerWorld) ctx.world);
			} else {
				fake = (DeployerFakePlayer)ctx.temporaryData;
			}
			if (fake == null) return false;
			fake.inventory.load(ctx.tileData.getList("Inventory", Constants.NBT.TAG_COMPOUND));
			if (ctx.data.contains("HeldItem")) {
				player.setItemInHand(activeHand, ItemStack.of(ctx.data.getCompound("HeldItem")));
				fake.setItemInHand(Hand.MAIN_HAND, heldStack);
				ctx.tileData.put("HeldItem", heldStack.serializeNBT());
				ctx.data.put("HeldItem", heldStack.serializeNBT());
			}
			ctx.tileData.remove("Inventory");
			ctx.temporaryData = fake;
			//	Create.LOGGER.info("Swapped items");
		}
		if (index >= 0) {
		//	Create.LOGGER.info("->CTX: " + ctx.data.toString());
			setContraptionActorData(contraptionEntity, index, info, ctx);
		}
		return true;
	}
}
