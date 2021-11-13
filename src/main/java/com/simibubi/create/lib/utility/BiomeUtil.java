package com.simibubi.create.lib.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.simibubi.create.lib.mixin.accessor.BiomeGenerationSettings$BuilderAccessor;

import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

public class BiomeUtil {
	public static BiomeGenerationSettings.Builder settingsToBuilder(BiomeGenerationSettings settings) {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder();
		BiomeGenerationSettings$BuilderAccessor builderAccessor = MixinHelper.cast(builder);
		builderAccessor.setSurfaceBuilder(Optional.of(settings.getSurfaceBuilder()));
		Collections.unmodifiableSet(builderAccessor.getCarvers().keySet()).forEach(c -> {
			Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> newCarvers = builderAccessor.getCarvers();
			newCarvers.put(c, new ArrayList<>(settings.getCarvers(c)));
			builderAccessor.setCarvers(newCarvers);
		});
		builderAccessor.getFeatures().forEach(f -> {
			List<List<Supplier<ConfiguredFeature<?, ?>>>> newFeatures = ((BiomeGenerationSettings$BuilderAccessor) builderAccessor).getFeatures();
			newFeatures.add(new ArrayList<>(f));
			builderAccessor.setFeatures(newFeatures);
		});
		List<Supplier<ConfiguredStructureFeature<?, ?>>> newStructureFeatures = builderAccessor.getStructureFeatures();
		newStructureFeatures.addAll(settings.structures());
		builderAccessor.setStructureFeatures(newStructureFeatures);
		return builder;
	}
}
