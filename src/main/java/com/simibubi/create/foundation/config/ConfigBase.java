package com.simibubi.create.foundation.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.simibubi.create.lib.config.BooleanValue;
import com.simibubi.create.lib.config.ConfigValue;
import com.simibubi.create.lib.config.DoubleValue;
import com.simibubi.create.lib.config.EnumValue;
import com.simibubi.create.lib.config.FloatValue;
import com.simibubi.create.lib.config.IntValue;

public abstract class ConfigBase {

	public ConfigSpec specification;

	protected int depth;
	protected List<CValue<?, ?>> allValues;
	protected List<ConfigBase> children;

	protected void registerAll(final ConfigSpec builder) {
		for (CValue<?, ?> cValue : allValues)
			cValue.register(builder);
	}

	public void onLoad() {
		if (children != null)
			children.forEach(ConfigBase::onLoad);
	}

	public void onReload() {
		if (children != null)
			children.forEach(ConfigBase::onReload);
	}

	public abstract String getName();

	@FunctionalInterface
	protected static interface IValueProvider<V, T extends ConfigValue<V>>
		extends Function<ConfigSpec, T> {
	}

	protected ConfigBool b(boolean current, String name, String... comment) {
		return new ConfigBool(name, current, comment);
	}

	protected ConfigFloat f(float current, float min, float max, String name, String... comment) {
		return new ConfigFloat(name, current, min, max, comment);
	}

	protected ConfigFloat f(float current, float min, String name, String... comment) {
		return f(current, min, Float.MAX_VALUE, name, comment);
	}

	protected ConfigInt i(int current, int min, int max, String name, String... comment) {
		return new ConfigInt(name, current, min, max, comment);
	}

	protected ConfigInt i(int current, int min, String name, String... comment) {
		return i(current, min, Integer.MAX_VALUE, name, comment);
	}

	protected <T extends Enum<T>> ConfigEnum<T> e(T defaultValue, String name, String... comment) {
		return new ConfigEnum<>(name, defaultValue, comment);
	}

	protected ConfigGroup group(int depth, String name, String... comment) {
		return new ConfigGroup(name, depth, comment);
	}

	protected <T extends ConfigBase> T nested(int depth, Supplier<T> constructor, String... comment) {
		T config = constructor.get();
		new ConfigGroup(config.getName(), depth, comment);
		new CValue<Boolean, BooleanValue>(config.getName(), builder -> {
			config.depth = depth;
			config.registerAll(builder);
			if (config.depth > depth)
				builder.pop(config.depth - depth);
			return null;
		});
		if (children == null)
			children = new ArrayList<>();
		children.add(config);
		return config;
	}

	public class CValue<V, T extends ConfigValue<V>> {
		protected ConfigValue<V> value;
		protected String name;
		private IValueProvider<V, T> provider;

		public CValue(String name, IValueProvider<V, T> provider, String... comment) {
			this.name = name;
			this.provider = builder -> {
				addComments(builder, comment);
				return provider.apply(builder);
			};
			if (allValues == null)
				allValues = new ArrayList<>();
			allValues.add(this);
		}

		public void addComments(ConfigSpec builder, String... comment) {
			if (comment.length > 0) {
				String[] comments = new String[comment.length + 1];
				comments[0] = " ";
				System.arraycopy(comment, 0, comments, 1, comment.length);
				builder.comment(comments);
			} else
				builder.comment(" ");
		}

		public void register(ConfigSpec builder) {
			value = provider.apply(builder);
		}

		public V get() {
			return value.get();
		}

		public void set(V value) {
			this.value.set(value);
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Marker for config subgroups
	 */
	public class ConfigGroup extends CValue<Boolean, BooleanValue> {

		private int groupDepth;
		private String[] comment;

		public ConfigGroup(String name, int depth, String... comment) {
			super(name, builder -> null, comment);
			groupDepth = depth;
			this.comment = comment;
		}

		@Override
		public void register(ConfigSpec builder) {
			if (depth > groupDepth)
				builder.pop(depth - groupDepth);
			depth = groupDepth;
			addComments(builder, comment);
			builder.push(getName());
			depth++;
		}

	}

	public class ConfigBool extends CValue<Boolean, BooleanValue> {

		public ConfigBool(String name, boolean def, String... comment) {
			super(name, builder -> {
				builder.define(name, def);
				return new BooleanValue(def);
			}, comment);
		}
	}

	public class ConfigEnum<T extends Enum<T>> extends CValue<T, EnumValue<T>> {

		public ConfigEnum(String name, T defaultValue, String[] comment) {
			super(name, builder -> {
				builder.defineEnum(name, defaultValue, EnumGetMethod.NAME_IGNORECASE);
				return new EnumValue<>(defaultValue);
			}, comment);
		}

	}

	public class ConfigFloat extends CValue<Double, DoubleValue> {

		public ConfigFloat(String name, float current, float min, float max, String... comment) {
			super(name, builder -> {
				builder.defineInRange(name, current, min, max);
				return new DoubleValue(current);
			}, comment);
		}

		public float getF() {
			return get().floatValue();
		}
	}

	public class ConfigInt extends CValue<Integer, IntValue> {

		public ConfigInt(String name, int current, int min, int max, String... comment) {
			super(name, builder -> {
				builder.defineInRange(name, current, min, max);
				return new IntValue(current);
			}, comment);
		}
	}

}
