package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RewriteRule<T> {
	private final Property.Type<T> writes;
	private final Set<Property.Relative<?>> reads;
	private final Descriptor<T> descriptor;

	public interface Descriptor<T> {
		Property.Type<T> getWrittenProperty();

		default Set<Property.Relative<?>> getReadProperties() { return Set.of(); }

		Optional<T> getRewrittenValue(PropertyReader reader);
	}

	public RewriteRule(Descriptor<T> descriptor) {
		this.descriptor = descriptor;
		this.writes = descriptor.getWrittenProperty();
		this.reads = descriptor.getReadProperties();
	}

	record PropertyReader(BlockPos pos, PropertyMap map) {
		public <U> U read(Property.Relative<U> property) {
			return map.read(property.toAbsolute(pos)).get();
		}
	}

	private PropertyMap.WriteResult rewrite(BlockPos pos, PropertyMap properties) {
		return descriptor
				.getRewrittenValue(new PropertyReader(pos, properties))
					.map(v -> properties.write(new Property<>(pos, writes), v))
				.orElse(PropertyMap.WriteResult.Ok.V);
	}

	public static class Tracker<T> {
		public final RewriteRule<T> rule;
		public final BlockPos pos;
		public final Property<T> writes;
		public final Set<Property<?>> reads;
		private int dependencies;

		public Tracker(RewriteRule<T> rule, BlockPos pos, Function<Tracker<?>, Integer> dependencies) {
			this.rule = rule;
			this.pos = pos;
			this.writes = new Property<>(pos, rule.writes);
			this.reads = rule.reads.stream().map(p -> p.toAbsolute(pos)).collect(Collectors.toSet());
			this.dependencies = dependencies.apply(this);
		}

		public PropertyMap.WriteResult rewrite(PropertyMap properties) {
			if (!canRewrite()) throw new IllegalStateException();
			return rule.rewrite(pos, properties);
		}

		public void dependencyAdded() {
			dependencies += 1;
		}

		public void dependencyRemoved() {
			if (dependencies == 0) throw new IllegalStateException();
			dependencies -= 1;
		}

		public boolean canRewrite() {
			return dependencies == 0;
		}
	}

}
