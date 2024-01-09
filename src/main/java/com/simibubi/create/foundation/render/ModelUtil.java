package com.simibubi.create.foundation.render;

import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class ModelUtil {
	public static final ModelProperty<Boolean> VIRTUAL_PROPERTY = new ModelProperty<>();
	public static final ModelData VIRTUAL_DATA = ModelData.builder().with(VIRTUAL_PROPERTY, true).build();

	public static boolean isVirtual(ModelData data) {
		return data.has(ModelUtil.VIRTUAL_PROPERTY) && Boolean.TRUE.equals(data.get(ModelUtil.VIRTUAL_PROPERTY));
	}
}
