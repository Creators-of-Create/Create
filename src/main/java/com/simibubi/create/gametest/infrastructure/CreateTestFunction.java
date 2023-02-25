package com.simibubi.create.gametest.infrastructure;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;

import net.minecraft.world.level.block.entity.StructureBlockEntity;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

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

/**
 * An extension to game tests implementing functionality for {@link CreateGameTestHelper} and {@link GameTestGroup}.
 * To use, create a {@link GameTestGenerator} that provides tests using {@link #getTestsFrom(Class[])}.
 */
public class CreateTestFunction extends TestFunction {
	public static final Map<String, CreateTestFunction> NAMES_TO_FUNCTIONS = new HashMap<>();

	public final String fullyQualifiedName;

	protected CreateTestFunction(String fullyQualifiedName, String pBatchName, String pTestName, String pStructureName, Rotation pRotation, int pMaxTicks, long pSetupTicks, boolean pRequired, int pRequiredSuccesses, int pMaxAttempts, Consumer<GameTestHelper> pFunction) {
		super(pBatchName, pTestName, pStructureName, pRotation, pMaxTicks, pSetupTicks, pRequired, pRequiredSuccesses, pMaxAttempts, pFunction);
		this.fullyQualifiedName = fullyQualifiedName;
		NAMES_TO_FUNCTIONS.put(fullyQualifiedName, this);
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
				.sorted(Comparator.comparing(TestFunction::getTestName))
				.toList();
	}

	@Nullable
	public static TestFunction of(Method method) {
		GameTest gt = method.getAnnotation(GameTest.class);
		if (gt == null) // skip non-test methods
			return null;
		Class<?> owner = method.getDeclaringClass();
		GameTestGroup group = owner.getAnnotation(GameTestGroup.class);
		String qualifiedName = owner.getSimpleName() + "." + method.getName();
		validateTestMethod(method, gt, owner, group, qualifiedName);

		String structure = "%s:gametest/%s/%s".formatted(group.namespace(), group.path(), gt.template());
		Rotation rotation = StructureUtils.getRotationForRotationSteps(gt.rotationSteps());

		String fullyQualifiedName = owner.getName() + "." + method.getName();
		return new CreateTestFunction(
				// use structure for test name since that's what MC fills structure blocks with for some reason
				fullyQualifiedName, gt.batch(), structure, structure, rotation, gt.timeoutTicks(), gt.setupTicks(),
				gt.required(), gt.requiredSuccesses(), gt.attempts(), asConsumer(method)
		);
	}

	private static void validateTestMethod(Method method, GameTest gt, Class<?> owner, GameTestGroup group, String qualifiedName) {
		if (gt.template().isEmpty())
			throw new IllegalArgumentException(qualifiedName + " must provide a template structure");

		if (!Modifier.isStatic(method.getModifiers()))
			throw new IllegalArgumentException(qualifiedName + " must be static");

		if (method.getReturnType() != void.class)
			throw new IllegalArgumentException(qualifiedName + " must return void");

		if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != CreateGameTestHelper.class)
			throw new IllegalArgumentException(qualifiedName + " must take 1 parameter of type CreateGameTestHelper");

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

	@Override
	public void run(@NotNull GameTestHelper helper) {
		// give structure block test info
		StructureBlockEntity be = (StructureBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
		be.getTileData().putString("CreateTestFunction", fullyQualifiedName);
		super.run(CreateGameTestHelper.of(helper));
	}
}
