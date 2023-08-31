package com.simibubi.create.content.trains.entity;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;

import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class ArrivalSoundQueue {

	public int offset;
	int min, max;
	Multimap<Integer, BlockPos> sources;

	public ArrivalSoundQueue() {
		sources = Multimaps.newMultimap(new HashMap<>(), ArrayList::new);
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
	}

	@Nullable
	public Integer firstTick() {
		return sources.isEmpty() ? null : min + offset;
	}

	@Nullable
	public Integer lastTick() {
		return sources.isEmpty() ? null : max + offset;
	}

	public boolean tick(CarriageContraptionEntity entity, int tick, boolean backwards) {
		tick = tick - offset;
		if (!sources.containsKey(tick))
			return backwards ? tick > min : tick < max;
		Contraption contraption = entity.getContraption();
		for (BlockPos blockPos : sources.get(tick))
			play(entity, contraption.getBlocks()
				.get(blockPos));
		return backwards ? tick > min : tick < max;
	}

	public Pair<Boolean, Integer> getFirstWhistle(CarriageContraptionEntity entity) {
		Integer firstTick = firstTick();
		Integer lastTick = lastTick();
		if (firstTick == null || lastTick == null || firstTick > lastTick)
			return null;
		for (int i = firstTick; i <= lastTick; i++) {
			if (!sources.containsKey(i - offset))
				continue;
			Contraption contraption = entity.getContraption();
			for (BlockPos blockPos : sources.get(i - offset)) {
				StructureBlockInfo info = contraption.getBlocks()
					.get(blockPos);
				if (info == null)
					continue;
				BlockState state = info.state();
				if (state.getBlock() instanceof WhistleBlock && info.nbt() != null) {
					int pitch = info.nbt().getInt("Pitch");
					WhistleSize size = state.getValue(WhistleBlock.SIZE);
					return Pair.of(size == WhistleSize.LARGE, (size == WhistleSize.SMALL ? 12 : 0) - pitch);
				}
			}
		}
		return null;
	}

	public void serialize(CompoundTag tagIn) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("Offset", offset);
		tag.put("Sources", NBTHelper.writeCompoundList(sources.entries(), e -> {
			CompoundTag c = new CompoundTag();
			c.putInt("Tick", e.getKey());
			c.put("Pos", NbtUtils.writeBlockPos(e.getValue()));
			return c;
		}));
		tagIn.put("SoundQueue", tag);
	}

	public void deserialize(CompoundTag tagIn) {
		CompoundTag tag = tagIn.getCompound("SoundQueue");
		offset = tag.getInt("Offset");
		NBTHelper.iterateCompoundList(tag.getList("Sources", Tag.TAG_COMPOUND),
			c -> add(c.getInt("Tick"), NbtUtils.readBlockPos(c.getCompound("Pos"))));
	}

	public void add(int offset, BlockPos localPos) {
		sources.put(offset, localPos);
		min = Math.min(offset, min);
		max = Math.max(offset, max);
	}

	public static boolean isPlayable(BlockState state) {
		if (state.getBlock() instanceof BellBlock)
			return true;
		if (state.getBlock() instanceof NoteBlock)
			return true;
		if (state.getBlock() instanceof WhistleBlock)
			return true;
		return false;
	}

	public static void play(CarriageContraptionEntity entity, StructureBlockInfo info) {
		if (info == null)
			return;
		BlockState state = info.state();

		if (state.getBlock() instanceof BellBlock) {
			if (AllBlocks.HAUNTED_BELL.has(state))
				playSimple(entity, AllSoundEvents.HAUNTED_BELL_USE.getMainEvent(), 1, 1);
			else
				playSimple(entity, SoundEvents.BELL_BLOCK, 1, 1);
		}

		if (state.getBlock() instanceof NoteBlock nb) {
			float f = (float) Math.pow(2, (state.getValue(NoteBlock.NOTE) - 12) / 12.0);
			playSimple(entity, state.getValue(NoteBlock.INSTRUMENT)
				.getSoundEvent()
				.get(), 1, f);
		}

		if (state.getBlock() instanceof WhistleBlock && info.nbt() != null) {
			int pitch = info.nbt().getInt("Pitch");
			WhistleSize size = state.getValue(WhistleBlock.SIZE);
			float f = (float) Math.pow(2, ((size == WhistleSize.SMALL ? 12 : 0) - pitch) / 12.0);
			playSimple(entity,
				(size == WhistleSize.LARGE ? AllSoundEvents.WHISTLE_TRAIN_LOW : AllSoundEvents.WHISTLE_TRAIN)
					.getMainEvent(),
				1, f);
//			playSimple(entity, AllSoundEvents.WHISTLE_CHIFF.getMainEvent(), .75f,
//				size == WhistleSize.SMALL ? f + .75f : f);
		}

	}

	private static void playSimple(CarriageContraptionEntity entity, SoundEvent event, float volume, float pitch) {
		entity.level().playSound(null, entity, event, SoundSource.NEUTRAL, 5 * volume, pitch);
	}

}
