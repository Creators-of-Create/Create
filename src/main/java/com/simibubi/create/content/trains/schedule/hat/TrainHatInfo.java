package com.simibubi.create.content.trains.schedule.hat;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;

public record TrainHatInfo(String part, int cubeIndex, Vec3 offset, float scale) {
	public static final Codec<TrainHatInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("model_part", "").forGetter(TrainHatInfo::part),
			Codec.INT.optionalFieldOf("cube_index", 0).forGetter(TrainHatInfo::cubeIndex),
			Vec3.CODEC.fieldOf("offset").forGetter(TrainHatInfo::offset),
			Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(TrainHatInfo::scale)
	).apply(instance, TrainHatInfo::new));

	public static List<ModelPart> getAdjustedPart(TrainHatInfo info, ModelPart root, String defaultPart) {
		List<ModelPart> finalParts = new ArrayList<>();
		finalParts.add(root);
		ModelPart parent = root;
		if (!info.part().isEmpty() && !info.part().equals(defaultPart)) {
			String[] partList = info.part().split("/");
			for (String part : partList) {
				if (parent.children.containsKey(part)) {
					finalParts.add(parent.children.get(part));
					parent = parent.children.get(part);
				}
			}
		} else {
			if (parent.children.containsKey(defaultPart)) {
				finalParts.add(parent.children.get(defaultPart));
			}
		}

		return finalParts;
	}
}
