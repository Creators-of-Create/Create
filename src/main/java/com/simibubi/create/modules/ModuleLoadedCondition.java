package com.simibubi.create.modules;

import com.google.gson.JsonObject;
import com.simibubi.create.Create;

import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class ModuleLoadedCondition implements ICondition {

	private static final ResourceLocation NAME = new ResourceLocation(Create.ID, "module");
	protected String module;

	public ModuleLoadedCondition(String module) {
		this.module = module;
	}

	@Override
	public ResourceLocation getID() {
		return NAME;
	}

	@Override
	public boolean test() {
		return IModule.isActive(module);
	}

	@Override
	public String toString() {
		return "module_loaded(\"" + module + "\")";
	}

	public static class Serializer implements IConditionSerializer<ModuleLoadedCondition> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public void write(JsonObject json, ModuleLoadedCondition value) {
			json.addProperty("module", value.module);
		}

		@Override
		public ModuleLoadedCondition read(JsonObject json) {
			return new ModuleLoadedCondition(JSONUtils.getString(json, "module"));
		}
		
		@Override
		public ResourceLocation getID() {
			return NAME;
		}
	}

}
