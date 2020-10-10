package com.simibubi.create.content.contraptions.components.deployer;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;

public class DeployerMovementBehaviour extends MovementBehaviour {

	@Override
	public Vec3d getActiveAreaOffset(MovementContext context) {
		return new Vec3d(context.state.get(DeployerBlock.FACING)
			.getDirectionVec()).scale(2);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		if (context.world.isRemote)
			return;

		tryGrabbingItem(context);
		DeployerFakePlayer player = getPlayer(context);
		Mode mode = getMode(context);
		if (mode == Mode.USE && !DeployerHandler.shouldActivate(player.getHeldItemMainhand(), context.world, pos))
			return;

		activate(context, pos, player, mode);
		tryDisposeOfExcess(context);
		context.stall = player.blockBreakingProgress != null;
	}

	public void activate(MovementContext context, BlockPos pos, DeployerFakePlayer player, Mode mode) {
		Vec3d facingVec = new Vec3d(context.state.get(DeployerBlock.FACING)
			.getDirectionVec());
		facingVec = context.rotation.apply(facingVec);
		Vec3d vec = context.position.subtract(facingVec.scale(2));
		player.rotationYaw = ContraptionEntity.yawFromVector(facingVec);
		player.rotationPitch = ContraptionEntity.pitchFromVector(facingVec) - 90;

		DeployerHandler.activate(player, vec, pos, facingVec, mode);
	}

	@Override
	public void tick(MovementContext context) {
		if (context.world.isRemote)
			return;
		if (!context.stall)
			return;

		DeployerFakePlayer player = getPlayer(context);
		Mode mode = getMode(context);

		Pair<BlockPos, Float> blockBreakingProgress = player.blockBreakingProgress;
		if (blockBreakingProgress != null) {
			int timer = context.data.getInt("Timer");
			if (timer < 20) {
				timer++;
				context.data.putInt("Timer", timer);
				return;
			}

			context.data.remove("Timer");
			activate(context, blockBreakingProgress.getKey(), player, mode);
			tryDisposeOfExcess(context);
		}

		context.stall = player.blockBreakingProgress != null;
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (context.world.isRemote)
			return;

		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;

		context.tileData.put("Inventory", player.inventory.write(new ListNBT()));
		player.remove();
	}

	private void tryGrabbingItem(MovementContext context) {
		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;
		if (player.getHeldItemMainhand()
			.isEmpty()) {
			ItemStack filter = getFilter(context);
			ItemStack held = ItemHelper.extract(context.contraption.inventory,
				stack -> FilterItem.test(context.world, stack, filter), 1, false);
			player.setHeldItem(Hand.MAIN_HAND, held);
		}
	}

	private void tryDisposeOfExcess(MovementContext context) {
		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;
		PlayerInventory inv = player.inventory;
		ItemStack filter = getFilter(context);

		for (List<ItemStack> list : Arrays.asList(inv.armorInventory, inv.offHandInventory, inv.mainInventory)) {
			for (int i = 0; i < list.size(); ++i) {
				ItemStack itemstack = list.get(i);
				if (itemstack.isEmpty())
					continue;

				if (list == inv.mainInventory && i == inv.currentItem
					&& FilterItem.test(context.world, itemstack, filter))
					continue;

				dropItem(context, itemstack);
				list.set(i, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public void writeExtraData(MovementContext context) {
		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;
		context.data.put("HeldItem", player.getHeldItemMainhand()
			.serializeNBT());
	}

	private DeployerFakePlayer getPlayer(MovementContext context) {
		if (!(context.temporaryData instanceof DeployerFakePlayer) && context.world instanceof ServerWorld) {
			DeployerFakePlayer deployerFakePlayer = new DeployerFakePlayer((ServerWorld) context.world);
			deployerFakePlayer.inventory.read(context.tileData.getList("Inventory", NBT.TAG_COMPOUND));
			if (context.data.contains("HeldItem"))
				deployerFakePlayer.setHeldItem(Hand.MAIN_HAND, ItemStack.read(context.data.getCompound("HeldItem")));
			context.tileData.remove("Inventory");
			context.temporaryData = deployerFakePlayer;
		}
		return (DeployerFakePlayer) context.temporaryData;
	}

	private ItemStack getFilter(MovementContext context) {
		return ItemStack.read(context.tileData.getCompound("Filter"));
	}

	private Mode getMode(MovementContext context) {
		return NBTHelper.readEnum(context.tileData, "Mode", Mode.class);
	}

	@Override
	public void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffers) {
		DeployerRenderer.renderInContraption(context, ms, msLocal, buffers);
	}

}
