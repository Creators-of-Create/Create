package com.simibubi.create.foundation.blockEntity.behaviour.scrollValue;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.IdentityHashMap;
import java.util.Map;

// Example implementation of ISidedScrollValueBehavior
public class SidedScrollOptionBehavior<E extends Enum<E> & INamedIconOptions> extends ScrollValueBehaviour implements ISidedScrollValueBehavior<ScrollOptionBehaviour<E>> {
    Map<Direction, ScrollOptionBehaviour<E>> sidedBehaviours;

	/**
	 * Creates a new SidedScrollOptionBehavior with the given enum type, label, block entity, and slot transform.
	 *
	 * @param label The label for the behavior.
	 * @param be The SmartBlockEntity this behavior is associated with.
	 * @param slot The ValueBoxTransform for the slot.
	 */

    public SidedScrollOptionBehavior(Class<E> enum_, Component label, SmartBlockEntity be, ValueBoxTransform slot) {
		super(label, be, slot);
		sidedBehaviours = new IdentityHashMap<>();
		for (Direction d : Iterate.directions) {
			sidedBehaviours.put(d, new ScrollOptionBehaviour<>(enum_, label, be, slot));
		}
    }

    @Override
	public ScrollOptionBehaviour<E> get(Direction side) {
		return sidedBehaviours.get(side);
	}

	@Override
	public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
		NBTHelper.iterateCompoundList(nbt.getList("Behaviors", Tag.TAG_COMPOUND), compound -> {
			Direction face = Direction.from3DDataValue(compound.getInt("Side"));
			if (sidedBehaviours.containsKey(face))
				sidedBehaviours.get(face)
					.read(compound, registries, clientPacket);
		});
		super.read(nbt, registries, clientPacket);
	}

	@Override
	public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
		nbt.put("Behaviors", NBTHelper.writeCompoundList(sidedBehaviours.entrySet(), entry -> {
			CompoundTag compound = new CompoundTag();
			compound.putInt("Side", entry.getKey()
				.get3DDataValue());
			entry.getValue()
				.write(compound, registries, clientPacket);
			return compound;
		}));
		super.write(nbt, registries, clientPacket);
	}
}
