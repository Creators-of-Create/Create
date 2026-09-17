package com.simibubi.create.infrastructure.gametest;

import java.util.Collection;

import com.simibubi.create.infrastructure.gametest.tests.TestSchematicannonFluidIntegration;
import com.simibubi.create.infrastructure.gametest.tests.TestContraptions;
import com.simibubi.create.infrastructure.gametest.tests.TestFluids;
import com.simibubi.create.infrastructure.gametest.tests.TestItems;
import com.simibubi.create.infrastructure.gametest.tests.TestMisc;
import com.simibubi.create.infrastructure.gametest.tests.TestProcessing;
import com.simibubi.create.infrastructure.gametest.tests.TestRegressions;
import com.simibubi.create.infrastructure.gametest.tests.TestSchematicannonFluids;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

@EventBusSubscriber
public class CreateGameTests {
	public static final String FLUID_CONTAINER_MARKER = "CreateGameTestFluidContainer";
	public static final String FLUID_CONTAINER_AMOUNT = "CreateGameTestFluidAmount";
	private static final int TEST_FLUID_CONTAINER_CAPACITY = 4000;

	private static final Class<?>[] testHolders = {
			TestContraptions.class,
			TestFluids.class,
			TestItems.class,
			TestMisc.class,
			TestSchematicannonFluids.class,
			TestSchematicannonFluidIntegration.class,
			TestProcessing.class,
			TestRegressions.class
	};

	@SubscribeEvent
	public static void registerTests(RegisterGameTestsEvent event) {
	    event.register(CreateGameTests.class);
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> {
			CustomData data = stack.get(DataComponents.CUSTOM_DATA);
			if (data == null || !data.contains(FLUID_CONTAINER_MARKER))
				return null;
			return new GameTestFluidContainer(stack);
		}, Items.DEBUG_STICK);
	}

	@GameTestGenerator
	public static Collection<TestFunction> generateTests() {
		return CreateTestFunction.getTestsFrom(testHolders);
	}

	private static class GameTestFluidContainer implements IFluidHandlerItem {
		private final ItemStack container;

		private GameTestFluidContainer(ItemStack container) {
			this.container = container;
		}

		@Override
		public ItemStack getContainer() {
			return container;
		}

		@Override
		public int getTanks() {
			return 1;
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			return getFluid();
		}

		@Override
		public int getTankCapacity(int tank) {
			return TEST_FLUID_CONTAINER_CAPACITY;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return stack.is(Fluids.WATER);
		}

		@Override
		public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
			if (!resource.is(Fluids.WATER))
				return 0;
			int amount = getAmount();
			int fillAmount = Math.min(TEST_FLUID_CONTAINER_CAPACITY - amount, resource.getAmount());
			if (fillAmount <= 0)
				return 0;
			if (action.execute())
				setAmount(amount + fillAmount);
			return fillAmount;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			if (!resource.is(Fluids.WATER))
				return FluidStack.EMPTY;
			return drain(resource.getAmount(), action);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			int amount = getAmount();
			int drainAmount = Math.min(amount, maxDrain);
			if (drainAmount <= 0)
				return FluidStack.EMPTY;
			if (action.execute())
				setAmount(amount - drainAmount);
			return new FluidStack(Fluids.WATER, drainAmount);
		}

		private FluidStack getFluid() {
			int amount = getAmount();
			return amount <= 0 ? FluidStack.EMPTY : new FluidStack(Fluids.WATER, amount);
		}

		private int getAmount() {
			CustomData data = container.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
			return Math.max(0, data.copyTag()
				.getInt(FLUID_CONTAINER_AMOUNT));
		}

		private void setAmount(int amount) {
			CustomData.update(DataComponents.CUSTOM_DATA, container, tag -> {
				tag.putBoolean(FLUID_CONTAINER_MARKER, true);
				if (amount > 0)
					tag.putInt(FLUID_CONTAINER_AMOUNT, amount);
				else
					tag.remove(FLUID_CONTAINER_AMOUNT);
			});
		}
	}

	public static ItemStack testFluidContainer(int amount) {
		ItemStack stack = new ItemStack(Items.DEBUG_STICK);
		CompoundTag data = new CompoundTag();
		data.putBoolean(FLUID_CONTAINER_MARKER, true);
		data.putInt(FLUID_CONTAINER_AMOUNT, amount);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
		return stack;
	}

	public static int testFluidContainerAmount(ItemStack stack) {
		CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		return data.copyTag()
			.getInt(FLUID_CONTAINER_AMOUNT);
	}
}
