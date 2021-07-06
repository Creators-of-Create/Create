package com.simibubi.create.foundation.advancement;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.Maps;
import com.simibubi.create.Create;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CriterionTriggerBase<T extends CriterionTriggerBase.Instance> implements ICriterionTrigger<T> {

	public CriterionTriggerBase(String id) {
		this.ID = new ResourceLocation(Create.ID, id);
	}

	private final ResourceLocation ID;
	protected final Map<PlayerAdvancements, Set<Listener<T>>> listeners = Maps.newHashMap();

	@Override
	public void addListener(PlayerAdvancements playerAdvancementsIn, Listener<T> listener) {
		Set<Listener<T>> playerListeners = this.listeners.computeIfAbsent(playerAdvancementsIn, k -> new HashSet<>());

		playerListeners.add(listener);
	}

	@Override
	public void removeListener(PlayerAdvancements playerAdvancementsIn, Listener<T> listener) {
		Set<Listener<T>> playerListeners = this.listeners.get(playerAdvancementsIn);
		if (playerListeners != null) {
			playerListeners.remove(listener);
			if (playerListeners.isEmpty()) {
				this.listeners.remove(playerAdvancementsIn);
			}
		}
	}

	@Override
	public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
		this.listeners.remove(playerAdvancementsIn);
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	protected void trigger(ServerPlayerEntity player, @Nullable List<Supplier<Object>> suppliers) {
		PlayerAdvancements playerAdvancements = player.getAdvancements();
		Set<Listener<T>> playerListeners = this.listeners.get(playerAdvancements);
		if (playerListeners != null) {
			List<Listener<T>> list = new LinkedList<>();

			for (Listener<T> listener : playerListeners) {
				if (listener.getCriterionInstance()
					.test(suppliers)) {
					list.add(listener);
				}
			}

			list.forEach(listener -> listener.grantCriterion(playerAdvancements));

		}
	}

	public abstract static class Instance extends CriterionInstance {

		public Instance(ResourceLocation idIn, EntityPredicate.AndPredicate p_i231464_2_) {
			super(idIn, p_i231464_2_);
		}

		protected abstract boolean test(@Nullable List<Supplier<Object>> suppliers);
	}

}
