package com.simibubi.create.infrastructure.gametest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.Create;
import com.simibubi.create.infrastructure.gametest.tests.TestContraptions;
import com.simibubi.create.infrastructure.gametest.tests.TestFluids;
import com.simibubi.create.infrastructure.gametest.tests.TestItems;
import com.simibubi.create.infrastructure.gametest.tests.TestMisc;
import com.simibubi.create.infrastructure.gametest.tests.TestProcessing;
import com.simibubi.create.infrastructure.gametest.tests.TestRegressions;

import com.simibubi.create.infrastructure.gametest.legacy.GameTestGenerator;
import com.simibubi.create.infrastructure.gametest.legacy.TestFunction;

import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber
public class CreateGameTests {
	private static final Class<?>[] testHolders = {
			TestContraptions.class,
			TestFluids.class,
			TestItems.class,
			TestMisc.class,
			TestProcessing.class,
			TestRegressions.class
	};

	@SubscribeEvent
	public static void registerTests(RegisterGameTestsEvent event) {
		Map<String, Holder<TestEnvironmentDefinition<?>>> environments = new HashMap<>();
		for (TestFunction test : generateTests()) {
			Identifier testId = testId(test);
			Holder<TestEnvironmentDefinition<?>> environment = environments.computeIfAbsent(test.batchName(), batch -> event
					.registerEnvironment(Create.asResource("gametest/" + sanitize(batch)), new TestEnvironmentDefinition.AllOf()));
			event.registerTest(testId, new LegacyGameTestInstance(test, testData(test, environment)));
		}
	}

	@GameTestGenerator
	public static Collection<TestFunction> generateTests() {
		return CreateTestFunction.getTestsFrom(testHolders);
	}

	private static TestData<Holder<TestEnvironmentDefinition<?>>> testData(TestFunction test,
		Holder<TestEnvironmentDefinition<?>> environment) {
		return new TestData<>(environment, Identifier.parse(test.structureName()), test.maxTicks(),
			Math.toIntExact(test.setupTicks()), test.required(), test.rotation(), test.manualOnly(), test.maxAttempts(),
			test.requiredSuccesses(), test.skyAccess(), 0);
	}

	private static Identifier testId(TestFunction test) {
		return Create.asResource("gametest/" + sanitize(test.testName()));
	}

	private static String sanitize(String value) {
		return value.replace('.', '/')
			.toLowerCase(Locale.ROOT);
	}

	private static class LegacyGameTestInstance extends GameTestInstance {
		private final String name;
		private final Consumer<GameTestHelper> function;

		private LegacyGameTestInstance(TestFunction test, TestData<Holder<TestEnvironmentDefinition<?>>> info) {
			super(info);
			name = test.testName();
			function = test.function();
		}

		@Override
		public void run(GameTestHelper helper) {
			function.accept(helper);
		}

		@Override
		public MapCodec<? extends GameTestInstance> codec() {
			throw new UnsupportedOperationException("Create legacy GameTests are registered in code only");
		}

		@Override
		protected MutableComponent typeDescription() {
			return Component.literal("Create legacy GameTest");
		}

		@Override
		public Component describe() {
			return describeType()
				.append(descriptionRow("test_instance.description.function", Component.literal(name)))
				.append(describeInfo());
		}
	}
}
