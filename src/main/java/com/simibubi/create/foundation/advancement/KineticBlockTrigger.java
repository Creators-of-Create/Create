package com.simibubi.create.foundation.advancement;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.simibubi.create.Create;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class KineticBlockTrigger extends CriterionTriggerBase<KineticBlockTrigger.Instance> {

	private static final ResourceLocation ID = new ResourceLocation(Create.ID, "kinetic_block");

	public KineticBlockTrigger(String id) {
		super(id);
	}

	public Instance forBlock(Block block) {
		return new Instance(block);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		Block block = null;
		if (json.has("block")) {
			ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getString(json, "block"));
			block = Registry.BLOCK.getValue(resourcelocation).orElseThrow(() -> {
				return new JsonSyntaxException("Unknown block type '" + resourcelocation + "'");
			});
		}

		return new Instance(block);
	}

	public void trigger(ServerPlayerEntity player, BlockState state) {
		trigger(player, Arrays.asList(() -> state.getBlock()));
	}

	public static class Instance extends CriterionTriggerBase.Instance {
		private final Block block;

		public Instance(Block block) {
			super(KineticBlockTrigger.ID);
			this.block = block;
		}
		
		@Override
		protected boolean test(List<Supplier<Object>> suppliers) {
			if (suppliers.isEmpty())
				return false;
			return block == suppliers.get(0).get();
		}

		@Override
		@SuppressWarnings("deprecation")
		public JsonElement serialize() {
			JsonObject jsonobject = new JsonObject();
			if (this.block != null)
				jsonobject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
			return jsonobject;
		}
	}

	
}
