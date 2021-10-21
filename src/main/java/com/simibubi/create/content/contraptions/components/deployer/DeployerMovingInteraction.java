package com.simibubi.create.content.contraptions.components.deployer;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.MovingInteractionBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

public class DeployerMovingInteraction extends MovingInteractionBehaviour {

	@Override
	public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		BlockInfo info = contraptionEntity.getContraption()
			.getBlocks()
			.get(localPos);
		if (info == null)
			return false;
		MovementContext ctx = null;
		int index = -1;
		for (MutablePair<BlockInfo, MovementContext> pair : contraptionEntity.getContraption()
			.getActors()) {
			if (info.equals(pair.left)) {
				ctx = pair.right;
				index = contraptionEntity.getContraption()
					.getActors()
					.indexOf(pair);
				break;
			}
		}
		if (ctx == null)
			return false;

		ItemStack heldStack = player.getItemInHand(activeHand);
		if (heldStack.getItem()
			.equals(AllItems.WRENCH.get())) {
			DeployerTileEntity.Mode mode = NBTHelper.readEnum(ctx.tileData, "Mode", DeployerTileEntity.Mode.class);
			NBTHelper.writeEnum(ctx.tileData, "Mode",
				mode == DeployerTileEntity.Mode.PUNCH ? DeployerTileEntity.Mode.USE : DeployerTileEntity.Mode.PUNCH);

		} else {
			if (ctx.world.isClientSide)
				return true; // we'll try again on the server side
			DeployerFakePlayer fake = null;

			if (!(ctx.temporaryData instanceof DeployerFakePlayer) && ctx.world instanceof ServerWorld) {
				DeployerFakePlayer deployerFakePlayer = new DeployerFakePlayer((ServerWorld) ctx.world);
				deployerFakePlayer.inventory.load(ctx.tileData.getList("Inventory", Constants.NBT.TAG_COMPOUND));
				ctx.temporaryData = fake = deployerFakePlayer;
				ctx.tileData.remove("Inventory");
			} else
				fake = (DeployerFakePlayer) ctx.temporaryData;

			if (fake == null)
				return false;

			ItemStack deployerItem = fake.getMainHandItem();
			player.setItemInHand(activeHand, deployerItem.copy());
			fake.setItemInHand(Hand.MAIN_HAND, heldStack.copy());
			ctx.tileData.put("HeldItem", heldStack.serializeNBT());
			ctx.data.put("HeldItem", heldStack.serializeNBT());
		}
		if (index >= 0)
			setContraptionActorData(contraptionEntity, index, info, ctx);
		return true;
	}
}
