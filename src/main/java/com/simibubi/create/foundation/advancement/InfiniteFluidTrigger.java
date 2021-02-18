package com.simibubi.create.foundation.advancement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.fluid.FluidHelper;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class InfiniteFluidTrigger extends CriterionTriggerBase<InfiniteFluidTrigger.Instance> {
	private static final ResourceLocation ID = new ResourceLocation(Create.ID, "infinite_fluid");

	public InfiniteFluidTrigger(String id) {
		super(id);
	}

	public Instance forFluid(Fluid fluid) {
		return new Instance(fluid);
	}

	@Override
	public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		Fluid fluid = null;
		if (json.has("fluid")) {
			ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getString(json, "fluid"));
			fluid = ForgeRegistries.FLUIDS.getValue(resourcelocation);

			if (fluid == null)
				throw new JsonSyntaxException("Unknown fluid type '" + resourcelocation + "'");
		}

		return new Instance(fluid);
	}

	public void trigger(ServerPlayerEntity player, Fluid fluid) {
		trigger(player, Collections.singletonList(() -> fluid));
	}


	public static class Instance extends CriterionTriggerBase.Instance {

		private final Fluid fluid;

		public Instance(@Nullable Fluid fluid) {
			super(InfiniteFluidTrigger.ID);
			this.fluid = FluidHelper.convertToStill(fluid);
		}

		@Override
		protected boolean test(List<Supplier<Object>> suppliers) {
			if (fluid == null || suppliers.isEmpty())
				return false;
			return fluid.equals(suppliers.get(0).get());
		}

		@Override
		public JsonElement serialize() {
			JsonObject jsonobject = new JsonObject();
			ResourceLocation key = ForgeRegistries.FLUIDS.getKey(this.fluid);
			if (key != null)
				jsonobject.addProperty("fluid", key.toString());
			return jsonobject;
		}
	}

}
