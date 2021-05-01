package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.backend.core.ModelData;
import com.jozufozu.flywheel.backend.core.OrientedData;
import com.jozufozu.flywheel.backend.instancing.InstancedModel;

public class MaterialTypes {
	public static final MaterialType<InstancedModel<ModelData>> TRANSFORMED = new MaterialType<>();
	public static final MaterialType<InstancedModel<OrientedData>> ORIENTED = new MaterialType<>();
}
