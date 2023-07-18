package com.simibubi.create.content.kinetics.fan;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.fan.processingTypes.BlastingType;
import com.simibubi.create.content.kinetics.fan.processingTypes.HauntingType;
import com.simibubi.create.content.kinetics.fan.processingTypes.SmokingType;
import com.simibubi.create.content.kinetics.fan.processingTypes.SplashingType;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class FanProcessing {

	public static final DamageSource FIRE_DAMAGE_SOURCE = new DamageSource("create.fan_fire").setScalesWithDifficulty()
		.setIsFire();
	public static final DamageSource LAVA_DAMAGE_SOURCE = new DamageSource("create.fan_lava").setScalesWithDifficulty()
		.setIsFire();
	public static final AbstractFanProcessingType SPLASHING = new SplashingType();
	public static final AbstractFanProcessingType SMOKING = new SmokingType();
	public static final AbstractFanProcessingType HAUNTING = new HauntingType();
	public static final AbstractFanProcessingType BLASTING = new BlastingType();

	public static final RecipeWrapper RECIPE_WRAPPER = new RecipeWrapper(new ItemStackHandler(1));

	public static boolean canProcess(ItemEntity entity, AbstractFanProcessingType type) {
		if (entity.getPersistentData()
			.contains("CreateData")) {
			CompoundTag compound = entity.getPersistentData()
				.getCompound("CreateData");
			if (compound.contains("Processing")) {
				CompoundTag processing = compound.getCompound("Processing");

				if (AbstractFanProcessingType.valueOf(processing.getString("Type")) != type)
					return type.canProcess(entity.getItem(), entity.level);
				else if (processing.getInt("Time") >= 0)
					return true;
				else if (processing.getInt("Time") == -1)
					return false;
			}
		}
		return type.canProcess(entity.getItem(), entity.level);
	}

	public static boolean applyProcessing(ItemEntity entity, AbstractFanProcessingType type) {
		if (decrementProcessingTime(entity, type) != 0)
			return false;
		List<ItemStack> stacks = process(entity.getItem(), type, entity.level);
		if (stacks == null)
			return false;
		if (stacks.isEmpty()) {
			entity.discard();
			return false;
		}
		entity.setItem(stacks.remove(0));
		for (ItemStack additional : stacks) {
			ItemEntity entityIn = new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), additional);
			entityIn.setDeltaMovement(entity.getDeltaMovement());
			entity.level.addFreshEntity(entityIn);
		}
		return true;
	}

	public static TransportedResult applyProcessing(TransportedItemStack transported, Level world, AbstractFanProcessingType type) {
		TransportedResult ignore = TransportedResult.doNothing();
		if (transported.processedBy != type) {
			transported.processedBy = type;
			int timeModifierForStackSize = ((transported.stack.getCount() - 1) / 16) + 1;
			int processingTime =
				(int) (AllConfigs.server().kinetics.fanProcessingTime.get() * timeModifierForStackSize) + 1;
			transported.processingTime = processingTime;
			if (!type.canProcess(transported.stack, world))
				transported.processingTime = -1;
			return ignore;
		}
		if (transported.processingTime == -1)
			return ignore;
		if (transported.processingTime-- > 0)
			return ignore;

		List<ItemStack> stacks = process(transported.stack, type, world);
		if (stacks == null)
			return ignore;

		List<TransportedItemStack> transportedStacks = new ArrayList<>();
		for (ItemStack additional : stacks) {
			TransportedItemStack newTransported = transported.getSimilar();
			newTransported.stack = additional.copy();
			transportedStacks.add(newTransported);
		}
		return TransportedResult.convertTo(transportedStacks);
	}

	private static List<ItemStack> process(ItemStack stack, AbstractFanProcessingType type, Level world) {
		if (type != null) return type.process(stack, type, world);

		return null;
	}

	private static int decrementProcessingTime(ItemEntity entity, AbstractFanProcessingType type) {
		CompoundTag nbt = entity.getPersistentData();

		if (!nbt.contains("CreateData"))
			nbt.put("CreateData", new CompoundTag());
		CompoundTag createData = nbt.getCompound("CreateData");

		if (!createData.contains("Processing"))
			createData.put("Processing", new CompoundTag());
		CompoundTag processing = createData.getCompound("Processing");

		if (!processing.contains("Type") || AbstractFanProcessingType.valueOf(processing.getString("Type")) != type) {
			processing.putString("Type", type.name());
			int timeModifierForStackSize = ((entity.getItem()
				.getCount() - 1) / 16) + 1;
			int processingTime =
				(int) (AllConfigs.server().kinetics.fanProcessingTime.get() * timeModifierForStackSize) + 1;
			processing.putInt("Time", processingTime);
		}

		int value = processing.getInt("Time") - 1;
		processing.putInt("Time", value);
		return value;
	}

}
