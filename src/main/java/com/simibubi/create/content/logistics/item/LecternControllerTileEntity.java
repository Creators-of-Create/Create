package com.simibubi.create.content.logistics.item;

import java.util.List;
import java.util.UUID;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.DistExecutor;

public class LecternControllerTileEntity extends SmartTileEntity {

	private ItemStack controller;
	private UUID user;
	private UUID prevUser;	// used only on client
	private boolean deactivatedThisTick;	// used only on server

	public LecternControllerTileEntity(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) { }

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.put("Controller", controller.save(new CompoundNBT()));
		if (user != null)
			compound.putUUID("User", user);
	}

	@Override
	public void writeSafe(CompoundNBT compound, boolean clientPacket) {
		super.writeSafe(compound, clientPacket);
		compound.put("Controller", controller.save(new CompoundNBT()));
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		controller = ItemStack.of(compound.getCompound("Controller"));
		user = compound.hasUUID("User") ? compound.getUUID("User") : null;
	}

	public ItemStack getController() {
		return controller;
	}

	public boolean hasUser() { return user != null; }

	public boolean isUsedBy(PlayerEntity player) {
		return hasUser() && user.equals(player.getUUID());
	}

	public void tryStartUsing(PlayerEntity player) {
		if (!deactivatedThisTick && !hasUser() && !playerIsUsingLectern(player) && playerInRange(player, level, worldPosition))
			startUsing(player);
	}

	public void tryStopUsing(PlayerEntity player) {
		if (isUsedBy(player))
			stopUsing(player);
	}

	private void startUsing(PlayerEntity player) {
		user = player.getUUID();
		player.getPersistentData().putBoolean("IsUsingLecternController", true);
		sendData();
	}

	private void stopUsing(PlayerEntity player) {
		user = null;
		if (player != null)
			player.getPersistentData().remove("IsUsingLecternController");
		deactivatedThisTick = true;
		sendData();
	}

	public static boolean playerIsUsingLectern(PlayerEntity player) {
		return player.getPersistentData().contains("IsUsingLecternController");
	}

	@Override
	public void tick() {
		super.tick();

		if (level.isClientSide) {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::tryToggleActive);
			prevUser = user;
		}

		if (!level.isClientSide) {
			deactivatedThisTick = false;

			if (!(level instanceof ServerWorld))
				return;
			if (user == null)
				return;

			Entity entity = ((ServerWorld) level).getEntity(user);
			if (!(entity instanceof PlayerEntity)) {
				stopUsing(null);
				return;
			}

			PlayerEntity player = (PlayerEntity) entity;
			if (!playerInRange(player, level, worldPosition) || !playerIsUsingLectern(player))
				stopUsing(player);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void tryToggleActive() {
		if (user == null && Minecraft.getInstance().player.getUUID().equals(prevUser)) {
			LinkedControllerClientHandler.deactivateInLectern();
		} else if (prevUser == null && Minecraft.getInstance().player.getUUID().equals(user)) {
			LinkedControllerClientHandler.activateInLectern(worldPosition);
		}
	}

	public void setController(ItemStack newController) {
		controller = newController;
		if (newController != null) {
			AllSoundEvents.CONTROLLER_PUT.playOnServer(level, worldPosition);
		}
	}

	public void swapControllers(ItemStack stack, PlayerEntity player, Hand hand, BlockState state) {
		ItemStack newController = stack.copy();
		stack.setCount(0);
		if (player.getItemInHand(hand).isEmpty()) {
			player.setItemInHand(hand, controller);
		} else {
			dropController(state);
		}
		setController(newController);
	}

	public void dropController(BlockState state) {
		Entity playerEntity = ((ServerWorld) level).getEntity(user);
		if (playerEntity instanceof PlayerEntity)
			stopUsing((PlayerEntity) playerEntity);

		Direction dir = state.getValue(LecternControllerBlock.FACING);
		double x = worldPosition.getX() + 0.5 + 0.25*dir.getStepX();
		double y = worldPosition.getY() + 1;
		double z = worldPosition.getZ() + 0.5 + 0.25*dir.getStepZ();
		ItemEntity itementity = new ItemEntity(level, x, y, z, controller.copy());
		itementity.setDefaultPickUpDelay();
		level.addFreshEntity(itementity);
		controller = null;
	}

	public static boolean playerInRange(PlayerEntity player, World world, BlockPos pos) {
		//double modifier = world.isRemote ? 0 : 1.0;
		double reach = 0.4*player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());// + modifier;
		return player.distanceToSqr(Vector3d.atCenterOf(pos)) < reach*reach;
	}

}
