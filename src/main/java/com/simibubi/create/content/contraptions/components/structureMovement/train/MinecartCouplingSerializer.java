package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.util.Constants.NBT;

public class MinecartCouplingSerializer {

	public static void addCouplingToCart(AbstractMinecartEntity minecart, MinecartCoupling coupling) {
		CompoundNBT nbt = minecart.getPersistentData();
		ListNBT couplingList = nbt.getList("Couplings", NBT.TAG_COMPOUND);
		boolean main = coupling.mainCart.get() == minecart;
		couplingList.add(createCouplingTag(main, coupling));
		nbt.put("Couplings", couplingList);
	}

	public static void removeCouplingFromCart(AbstractMinecartEntity minecart, MinecartCoupling coupling) {
		CompoundNBT nbt = minecart.getPersistentData();
		ListNBT couplingList = nbt.getList("Couplings", NBT.TAG_COMPOUND);
		couplingList.removeIf(inbt -> coupling.getId()
			.equals(NBTUtil.readUniqueId(((CompoundNBT) inbt).getCompound("Id"))));
		nbt.put("Couplings", couplingList);
	}

	private static CompoundNBT createCouplingTag(boolean main, MinecartCoupling coupling) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.put("Id", NBTUtil.fromUuid(coupling.getId()));
		nbt.putBoolean("Main", main);
		nbt.putDouble("Length", coupling.length);
		return nbt;
	}

	public static List<CouplingData> getCouplingData(AbstractMinecartEntity minecart) {
		List<CouplingData> list = new ArrayList<>();
		CompoundNBT nbt = minecart.getPersistentData();
		NBTHelper.iterateCompoundList(nbt.getList("Couplings", NBT.TAG_COMPOUND), c -> {
			boolean main = c.getBoolean("Main");
			UUID id = NBTUtil.readUniqueId(c.getCompound("Id"));
			double length = c.getDouble("Length");
			list.add(new CouplingData(main, id, length));
		});
		return list;
	}

	static class CouplingData {
		boolean main;
		UUID id;
		double length;

		public CouplingData(boolean main, UUID id, double length) {
			this.main = main;
			this.id = id;
			this.length = length;
		}
	}

}
