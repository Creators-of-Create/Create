package com.simibubi.create.infrastructure.gametest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;

/**
 * An extension to game tests implementing functionality for {@link CreateGameTestHelper} and {@link GameTestGroup}.
 * To use, create a {@link GameTestGenerator} that provides tests using {@link #getTestsFrom(Class[])}.
 */
public class CreateTestFunction {
	// for structure blocks and /test runthis
	public static final Map<String, CreateTestFunction> NAMES_TO_FUNCTIONS = new HashMap<>();

	public final String fullName;
	public final String simpleName;
	public final TestFunction testFunction;

	protected CreateTestFunction(String fullName, String simpleName, String pBatchName,
								 String pStructureName, Rotation pRotation, int pMaxTicks, long pSetupTicks,
								 boolean pRequired, int pMaxAttempts, int pRequiredSuccesses, Consumer<GameTestHelper> pFunction) {
		testFunction = new TestFunction(pBatchName, simpleName, pStructureName, pRotation, pMaxTicks, pSetupTicks, pRequired, false, pMaxAttempts, pRequiredSuccesses, true, pFunction);
		this.fullName = fullName;
		this.simpleName = simpleName;
		NAMES_TO_FUNCTIONS.put(fullName, this);
	}

	/**
	 * Get all Create test functions from the given classes. This enables functionality
	 * of {@link CreateGameTestHelper} and {@link GameTestGroup}.
	 */
	public static Collection<TestFunction> getTestsFrom(Class<?>... classes) {
		return Stream.of(classes)
				.map(Class::getDeclaredMethods)
				.flatMap(Stream::of)
				.map(CreateTestFunction::of)
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(TestFunction::testName))
				.toList();
	}

	@Nullable
	public static TestFunction of(Method method) {
		GameTest gt = method.getAnnotation(GameTest.class);
		if (gt == null) // skip non-test methods
			return null;
		Class<?> owner = method.getDeclaringClass();
		GameTestGroup group = owner.getAnnotation(GameTestGroup.class);
		String simpleName = owner.getSimpleName() + '.' + method.getName();
		validateTestMethod(method, gt, owner, group, simpleName);

		String structure = "%s:gametest/%s/%s".formatted(group.namespace(), group.path(), gt.template());
		Rotation rotation = StructureUtils.getRotationForRotationSteps(gt.rotationSteps());

		String fullName = owner.getName() + "." + method.getName();

		// 		// give structure block test info
		//		StructureBlockEntity be = (StructureBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
		//		be.getPersistentData().putString("CreateTestFunction", fullName);
		//		super.run(CreateGameTestHelper.of(helper));

		return new CreateTestFunction(
				// use structure for test name since that's what MC fills structure blocks with for some reason
				fullName, simpleName, gt.batch(), structure, rotation, gt.timeoutTicks(), gt.setupTicks(),
				gt.required(), gt.attempts(), gt.requiredSuccesses(), run(fullName, asConsumer(method))
		).testFunction;
	}

	private static void validateTestMethod(Method method, GameTest gt, Class<?> owner, GameTestGroup group, String simpleName) {
		if (gt.template().isEmpty())
			throw new IllegalArgumentException(simpleName + " must provide a template structure");

		if (!Modifier.isStatic(method.getModifiers()))
			throw new IllegalArgumentException(simpleName + " must be static");

		if (method.getReturnType() != void.class)
			throw new IllegalArgumentException(simpleName + " must return void");

		if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != CreateGameTestHelper.class)
			throw new IllegalArgumentException(simpleName + " must take 1 parameter of type CreateGameTestHelper");

		if (group == null)
			throw new IllegalArgumentException(owner.getName() + " must be annotated with @GameTestGroup");
	}

	private static Consumer<GameTestHelper> asConsumer(Method method) {
		return (helper) -> {
			try {
				method.invoke(null, helper);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static Consumer<GameTestHelper> run(String fullName, @NotNull Consumer<GameTestHelper> helper) {
		return consumer -> {
			helper.andThen(gameTestHelper -> {
				// give structure block test info
				StructureBlockEntity be = gameTestHelper.getBlockEntity(BlockPos.ZERO);
				be.getPersistentData().putString("CreateTestFunction", fullName);
			}).accept(CreateGameTestHelper.of(consumer));
		};
	}
}
