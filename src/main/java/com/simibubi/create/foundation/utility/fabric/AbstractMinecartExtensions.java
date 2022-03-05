package com.simibubi.create.foundation.utility.fabric;

import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;

import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;

public interface AbstractMinecartExtensions {
	CapabilityMinecartController getCap();

	void setCap(CapabilityMinecartController cap);

	LazyOptional<MinecartController> lazyController();

	MinecartController getController();

	String CAP_KEY = "Controller";

	static void minecartSpawn(AbstractMinecart cart, Level level) {
		CapabilityMinecartController.attach(cart);
	}

	static void minecartRead(AbstractMinecart cart, CompoundTag data) {
		if (data.contains(CAP_KEY)) {
			CompoundTag cap = data.getCompound(CAP_KEY);
			cart.getCap().deserializeNBT(cap);
		}
	}

	static void minecartWrite(AbstractMinecart cart, CompoundTag data) {
		if (cart.getCap() != null) {
			CompoundTag capTag = cart.getCap().serializeNBT();
			data.put(CAP_KEY, capTag);
		}
	}

	static void minecartRemove(AbstractMinecart cart, Level level) {
		CapabilityMinecartController.onCartRemoved(level, cart);
		cart.lazyController().invalidate();
	}
}
