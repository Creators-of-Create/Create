package com.simibubi.create.modules.logistics.transport.villager;

import java.util.Iterator;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.DummyTask;
import net.minecraft.entity.ai.brain.task.FindInteractionAndLookTargetTask;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.ai.brain.task.LookAtEntityTask;
import net.minecraft.entity.ai.brain.task.StayNearPointTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.UpdateActivityTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsPosTask;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.RegistryEvent;

public class LogisticianHandler {

	public static VillagerProfession LOGISTICIAN;
	public static PointOfInterestType LOGISTICIANS_TABLE;

	public static void registerPointsOfInterest(RegistryEvent.Register<PointOfInterestType> event) {
		ImmutableList<BlockState> validStates = AllBlocks.LOGISTICIANS_TABLE.get().getStateContainer().getValidStates();
		LOGISTICIANS_TABLE = new PointOfInterestType("logistician_table", ImmutableSet.copyOf(validStates), 1,
				SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN, 1)
						.setRegistryName(new ResourceLocation(Create.ID, "logistician_table"));
		event.getRegistry().register(LOGISTICIANS_TABLE);
	}

	public static void registerVillagerProfessions(RegistryEvent.Register<VillagerProfession> event) {
		LOGISTICIAN = new VillagerProfession("logistician", LOGISTICIANS_TABLE, ImmutableSet.of(), ImmutableSet.of())
				.setRegistryName(new ResourceLocation(Create.ID, "logistician"));
		event.getRegistry().register(LOGISTICIAN);
	}

	public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> work(float p_220639_1_) {
		return ImmutableList.of(uselessTasks(), actualWorkTasks(),
				Pair.of(10, new FindInteractionAndLookTargetTask(EntityType.PLAYER, 4)),
				Pair.of(2, new StayNearPointTask(MemoryModuleType.JOB_SITE, p_220639_1_, 9, 100, 1200)),
				Pair.of(3, new ExpireWorkstationTask()), Pair.of(99, new UpdateActivityTask()));
	}

	public static Pair<Integer, Task<VillagerEntity>> actualWorkTasks() {
		return Pair.of(5,
				new FirstShuffledTask<>(ImmutableList.of(
						Pair.of(new WalkToPackageTask(), 1),
						Pair.of(new CollectPackageTask(), 1), 
						Pair.of(new WalkToWorkstationTask(), 1),
						Pair.of(new LookupAddressTask(), 1),
						Pair.of(new DeliverPackageToDestinationTask(), 1),
						Pair.of(new DropPackageAtDestinationTask(), 1),
						Pair.of(new WalkTowardsPosTask(MemoryModuleType.JOB_SITE, 1, 10), 5))));
	}

	private static Pair<Integer, Task<LivingEntity>> uselessTasks() {
		return Pair.of(6,
				new FirstShuffledTask<>(ImmutableList.of(Pair.of(new LookAtEntityTask(EntityType.VILLAGER, 8.0F), 2),
						Pair.of(new LookAtEntityTask(EntityType.PLAYER, 8.0F), 2), Pair.of(new DummyTask(30, 60), 8))));
	}

	public static GlobalPos getJobSite(VillagerEntity villager) {
		CompoundNBT nbt = villager.getPersistentData();
		String jobSiteKey = "JobSite";
		if (!nbt.contains(jobSiteKey))
			return null;
		return GlobalPos.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt.getCompound(jobSiteKey)));
	}

	public static void setJobSite(VillagerEntity villager, GlobalPos pos) {
		CompoundNBT nbt = villager.getPersistentData();
		String jobSiteKey = "JobSite";
		nbt.put(jobSiteKey, pos.serialize(NBTDynamicOps.INSTANCE));
	}

	public static void rememberAddress(VillagerEntity villager, String address, GlobalPos pos) {
		ListNBT list = getAddressList(villager);

		for (Iterator<INBT> iterator = list.iterator(); iterator.hasNext();) {
			INBT inbt = iterator.next();
			if (((CompoundNBT) inbt).getString("Address").equals(address)) {
				iterator.remove();
			}
		}

		if (list.size() > 5)
			list.remove(0);

		CompoundNBT addedNBT = new CompoundNBT();
		addedNBT.putString("Address", address);
		addedNBT.put("Pos", pos.serialize(NBTDynamicOps.INSTANCE));
		list.add(addedNBT);
	}

	public static ListNBT getAddressList(VillagerEntity villager) {
		CompoundNBT nbt = villager.getPersistentData();
		String listKey = "MemorizedAddresses";
		if (!nbt.contains(listKey))
			nbt.put(listKey, new ListNBT());
		ListNBT list = nbt.getList(listKey, NBT.TAG_COMPOUND);
		return list;
	}

	@Nullable
	public static GlobalPos getRememberedAddress(VillagerEntity villager, String address) {
		ListNBT list = getAddressList(villager);

		for (INBT inbt : list) {
			if (((CompoundNBT) inbt).getString("Address").equals(address)) {
				Dynamic<INBT> dynamic = new Dynamic<>(NBTDynamicOps.INSTANCE, ((CompoundNBT) inbt).getCompound("Pos"));
				return GlobalPos.deserialize(dynamic);
			}
		}

		return null;
	}

	public static void ponder(VillagerEntity entityIn, String thought) {
		Minecraft.getInstance().player.sendMessage(
				new StringTextComponent("<" + entityIn.getDisplayName().getFormattedText() + "> " + thought));
	}

}
