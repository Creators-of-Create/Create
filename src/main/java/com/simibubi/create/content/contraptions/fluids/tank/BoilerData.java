package com.simibubi.create.content.contraptions.fluids.tank;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.steam.SteamEngineBlock;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Debug;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class BoilerData {

	static final int SAMPLE_RATE = 5;
	public int gatheredSupply;
	public float[] supplyOverTime = new float[10];
	int ticksUntilNextSample;
	int currentIndex;
	boolean needsTemperatureUpdate;

	public float currentTemperature;
	public float targetTemperature;
	public float waterSupply;
	public float steamUnits;
	public int attachedEngines;
	public int engineScore;

	public LerpedFloat pressure = LerpedFloat.linear();

	static final float MAX_ENGINE_USAGE = 32;

	public void tick(FluidTankTileEntity controller) {
		if (!isActive())
			return;
		if (controller.getLevel().isClientSide) {
			pressure.tickChaser();
			float current = pressure.getValue(1);
			if (current > 1 && Create.RANDOM.nextFloat() < 1 / 2f)
				pressure.setValueNoUpdate(current + Math.min(-(current - 1) * Create.RANDOM.nextFloat(), 0));
			return;
		}
		if (needsTemperatureUpdate && updateTemperature(controller))
			controller.notifyUpdate();
		ticksUntilNextSample--;
		if (ticksUntilNextSample > 0)
			return;
		int capacity = controller.tankInventory.getCapacity();
		if (capacity == 0)
			return;

		ticksUntilNextSample = SAMPLE_RATE;
		waterSupply -= supplyOverTime[currentIndex];
		supplyOverTime[currentIndex] = gatheredSupply / (float) SAMPLE_RATE;
		waterSupply += supplyOverTime[currentIndex];
		currentIndex = (currentIndex + 1) % supplyOverTime.length;
		gatheredSupply = 0;

		if (currentIndex == 0) {
			waterSupply = 0;
			for (float i : supplyOverTime)
				waterSupply += i;
		}

		currentTemperature = Mth.clamp(currentTemperature + Math.signum(targetTemperature - currentTemperature)
			* (0.5f + (targetTemperature - currentTemperature) * .125f), 0, targetTemperature);

		float steamPerTick = Math.min(waterSupply / 2, currentTemperature - 100);
		steamUnits += steamPerTick;

		float pressure = steamUnits / capacity;
		float engineEfficiency = (float) (Math.max(0, pressure - 0.5) * 2);
		float usagePerEngine = engineEfficiency * MAX_ENGINE_USAGE;
		float consumedSteam = Math.min(steamUnits, attachedEngines * usagePerEngine);
		float equilibrium = steamPerTick / (attachedEngines * MAX_ENGINE_USAGE * 2) + .5f;

//		if (Math.abs(engineEfficiency - equilibrium) < 1 / 8f) // Anti-flicker at balance point
//			engineEfficiency = equilibrium;

		engineScore = Mth.floor(engineEfficiency * 8);
		steamUnits -= consumedSteam;

		if (steamUnits > capacity * 1.25f) {
			Debug.debugChat("Boiler exploding: Bang. " + controller.getBlockPos());
			steamUnits = 0;
		}

		controller.notifyUpdate();
	}

	String spacing = "    ";
	Component componentSpacing = new TextComponent(spacing);

	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!isActive())
			return false;

		float steamPerTick = Math.min(waterSupply / 2, currentTemperature - 100);
		float equilibrium = steamPerTick / (attachedEngines * MAX_ENGINE_USAGE * 2) + .5f;

		tooltip.add(componentSpacing.plainCopy()
			.append(Lang.translate("gui.goggles.fluid_container")));
		TranslatableComponent mb = Lang.translate("generic.unit.millibuckets");

		Component engines = new TextComponent("Engines: ").withStyle(ChatFormatting.GRAY)
			.append(new TextComponent(attachedEngines + "").withStyle(ChatFormatting.GOLD));
		Component power = new TextComponent("Temperature: ").withStyle(ChatFormatting.GRAY)
			.append(new TextComponent(IHaveGoggleInformation.format(currentTemperature) + "")
				.withStyle(ChatFormatting.GOLD));
		Component score = new TextComponent("Engine Efficiency: ").withStyle(ChatFormatting.GRAY)
			.append(new TextComponent(engineScore + "").withStyle(ChatFormatting.GOLD));
		Component supply = new TextComponent("Water Supply: ").withStyle(ChatFormatting.GRAY)
			.append(new TextComponent(IHaveGoggleInformation.format(waterSupply)).append(mb)
				.withStyle(ChatFormatting.GOLD));
		Component steam = new TextComponent("Steam Volume: ").withStyle(ChatFormatting.GRAY)
			.append(new TextComponent(IHaveGoggleInformation.format(steamUnits)).append(mb)
				.withStyle(ChatFormatting.GOLD));

		int approachingPressure = (int) (equilibrium * 100);
		int actualPressure =
			(int) ((this.pressure.getChaseTarget() > 1 ? this.pressure.getChaseTarget() : this.pressure.getValue())
				* 100);
		MutableComponent pressure = new TextComponent("Pressure: ").withStyle(ChatFormatting.GRAY)
			.append(new TextComponent(IHaveGoggleInformation.format(actualPressure)).append(new TextComponent("%"))
				.withStyle(ChatFormatting.GOLD));
		if (actualPressure != approachingPressure)
			pressure.append(new TextComponent(" >> ").append(
				new TextComponent(IHaveGoggleInformation.format(approachingPressure)).append(new TextComponent("%"))
					.withStyle(ChatFormatting.GREEN)));

		Component indent = new TextComponent(spacing + " ");

		tooltip.add(indent.plainCopy()
			.append(engines));
		tooltip.add(indent.plainCopy()
			.append(score));
		tooltip.add(indent.plainCopy()
			.append(power));
		tooltip.add(indent.plainCopy()
			.append(supply));
		tooltip.add(indent.plainCopy()
			.append(steam));
		tooltip.add(indent.plainCopy()
			.append(pressure));
		return true;
	}

	public boolean evaluate(FluidTankTileEntity controller) {
		BlockPos controllerPos = controller.getBlockPos();
		Level level = controller.getLevel();
		int prev = attachedEngines;
		attachedEngines = 0;

		for (int yOffset = 0; yOffset < controller.height; yOffset++) {
			for (int xOffset = 0; xOffset < controller.width; xOffset++) {
				for (int zOffset = 0; zOffset < controller.width; zOffset++) {

					BlockPos pos = controllerPos.offset(xOffset, yOffset, zOffset);
					BlockState blockState = level.getBlockState(pos);
					if (!FluidTankBlock.isTank(blockState))
						continue;
					for (Direction d : Iterate.directions) {
						BlockPos enginePos = pos.relative(d);
						BlockState engineState = level.getBlockState(enginePos);
						if (!AllBlocks.STEAM_ENGINE.has(engineState))
							continue;
						if (SteamEngineBlock.getFacing(engineState) != d)
							continue;
						attachedEngines++;
					}
				}
			}
		}

		needsTemperatureUpdate = true;
		return prev != attachedEngines;
	}

	public boolean updateTemperature(FluidTankTileEntity controller) {
		BlockPos controllerPos = controller.getBlockPos();
		Level level = controller.getLevel();
		float prev = targetTemperature;
		targetTemperature = 0;
		needsTemperatureUpdate = false;

		for (int xOffset = 0; xOffset < controller.width; xOffset++) {
			for (int zOffset = 0; zOffset < controller.width; zOffset++) {
				BlockPos pos = controllerPos.offset(xOffset, -1, zOffset);
				BlockState blockState = level.getBlockState(pos);
				targetTemperature += BoilerHeaters.getAddedHeatOf(blockState);
			}
		}

		if (targetTemperature != 0)
			targetTemperature += 100;

		return prev != attachedEngines;
	}

	public boolean isActive() {
		return attachedEngines > 0;
	}

	public void clear() {
		waterSupply = 0;
		targetTemperature = 0;
		attachedEngines = 0;
		steamUnits = 0;
		engineScore = 0;
		Arrays.fill(supplyOverTime, 0);
	}

	public CompoundTag write() {
		CompoundTag nbt = new CompoundTag();
		nbt.putFloat("Supply", waterSupply);
		nbt.putFloat("Temperature", currentTemperature);
		nbt.putFloat("Power", targetTemperature);
		nbt.putFloat("Pressure", steamUnits);
		nbt.putInt("Engines", attachedEngines);
		nbt.putBoolean("Update", needsTemperatureUpdate);
		nbt.putInt("Score", engineScore);
		return nbt;
	}

	public void read(CompoundTag nbt, int capacity) {
		waterSupply = nbt.getFloat("Supply");
		currentTemperature = nbt.getFloat("Temperature");
		targetTemperature = nbt.getFloat("Power");
		steamUnits = nbt.getFloat("Pressure");
		engineScore = nbt.getInt("Score");
		attachedEngines = nbt.getInt("Engines");
		needsTemperatureUpdate = nbt.getBoolean("Update");
		Arrays.fill(supplyOverTime, (int) waterSupply);
		if (capacity > 0)
			pressure.chase(steamUnits / capacity, 0.125f, Chaser.EXP);
	}

	public BoilerFluidHandler createHandler() {
		return new BoilerFluidHandler();
	}

	public class BoilerFluidHandler implements IFluidHandler {

		@Override
		public int getTanks() {
			return 1;
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			return 10000;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return FluidHelper.isWater(stack.getFluid());
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if (!isFluidValid(0, resource))
				return 0;
			if (targetTemperature == 0)
				return 0;
			int amount = resource.getAmount();
			if (action.execute())
				gatheredSupply += amount;
			return amount;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return FluidStack.EMPTY;
		}

	}

}
