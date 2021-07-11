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
		compound.put("Controller", controller.write(new CompoundNBT()));
		if (user != null)
			compound.putUniqueId("User", user);
	}

	@Override
	public void writeSafe(CompoundNBT compound, boolean clientPacket) {
		super.writeSafe(compound, clientPacket);
		compound.put("Controller", controller.write(new CompoundNBT()));
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		controller = ItemStack.read(compound.getCompound("Controller"));
		user = compound.hasUniqueId("User") ? compound.getUniqueId("User") : null;
	}

	public ItemStack getController() {
		return controller;
	}

	public boolean hasUser() { return user != null; }

	public boolean isUsedBy(PlayerEntity player) {
		return hasUser() && user.equals(player.getUniqueID());
	}

	public void tryStartUsing(PlayerEntity player) {
		if (!deactivatedThisTick && !hasUser() && !playerIsUsingLectern(player) && playerInRange(player, world, pos))
			startUsing(player);
	}

	public void tryStopUsing(PlayerEntity player) {
		if (isUsedBy(player))
			stopUsing(player);
	}

	private void startUsing(PlayerEntity player) {
		user = player.getUniqueID();
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

		if (world.isRemote) {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::tryToggleActive);
			prevUser = user;
		}

		if (!world.isRemote) {
			deactivatedThisTick = false;

			if (!(world instanceof ServerWorld))
				return;
			if (user == null)
				return;

			Entity entity = ((ServerWorld) world).getEntityByUuid(user);
			if (!(entity instanceof PlayerEntity)) {
				stopUsing(null);
				return;
			}

			PlayerEntity player = (PlayerEntity) entity;
			if (!playerInRange(player, world, pos) || !playerIsUsingLectern(player))
				stopUsing(player);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void tryToggleActive() {
		if (user == null && Minecraft.getInstance().player.getUniqueID().equals(prevUser)) {
			LinkedControllerClientHandler.deactivateInLectern();
		} else if (prevUser == null && Minecraft.getInstance().player.getUniqueID().equals(user)) {
			LinkedControllerClientHandler.activateInLectern(pos);
		}
	}

	public void setController(ItemStack newController) {
		controller = newController;
		if (newController != null) {
			AllSoundEvents.CONTROLLER_PUT.playOnServer(world, pos);
		}
	}

	public void swapControllers(ItemStack stack, PlayerEntity player, Hand hand, BlockState state) {
		ItemStack newController = stack.copy();
		stack.setCount(0);
		if (player.getHeldItem(hand).isEmpty()) {
			player.setHeldItem(hand, controller);
		} else {
			dropController(state);
		}
		setController(newController);
	}

	public void dropController(BlockState state) {
		Entity playerEntity = ((ServerWorld) world).getEntityByUuid(user);
		if (playerEntity instanceof PlayerEntity)
			stopUsing((PlayerEntity) playerEntity);

		Direction dir = state.get(LecternControllerBlock.FACING);
		double x = pos.getX() + 0.5 + 0.25*dir.getXOffset();
		double y = pos.getY() + 1;
		double z = pos.getZ() + 0.5 + 0.25*dir.getZOffset();
		ItemEntity itementity = new ItemEntity(world, x, y, z, controller.copy());
		itementity.setDefaultPickupDelay();
		world.addEntity(itementity);
		controller = null;
	}

	public static boolean playerInRange(PlayerEntity player, World world, BlockPos pos) {
		//double modifier = world.isRemote ? 0 : 1.0;
		double reach = 0.4*player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());// + modifier;
		return player.getDistanceSq(Vector3d.ofCenter(pos)) < reach*reach;
	}

}
