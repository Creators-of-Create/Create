package com.simibubi.create.gametest;

import java.util.Collection;

import com.simibubi.create.gametest.infrastructure.CreateTestFunction;

import com.simibubi.create.gametest.tests.TestContraptions;
import com.simibubi.create.gametest.tests.TestFluids;
import com.simibubi.create.gametest.tests.TestItems;
import com.simibubi.create.gametest.tests.TestMisc;
import com.simibubi.create.gametest.tests.TestProcessing;

import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.MOD)
public class CreateGameTests {
	private static final Class<?>[] testHolders = {
			TestContraptions.class,
			TestFluids.class,
			TestItems.class,
			TestMisc.class,
			TestProcessing.class
	};

	@SubscribeEvent
	public static void registerTests(RegisterGameTestsEvent event) {
	    event.register(CreateGameTests.class);
	}

	@GameTestGenerator
	public static Collection<TestFunction> generateTests() {
		return CreateTestFunction.getTestsFrom(testHolders);
	}
}
