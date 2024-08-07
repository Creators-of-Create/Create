package com.simibubi.create.content.decoration.bracket;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BracketedBlockEntityBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<BracketedBlockEntityBehaviour> TYPE = new BehaviourType<>();

	private BlockState bracket;
	private boolean reRender;

	private Predicate<BlockState> pred;

	public BracketedBlockEntityBehaviour(SmartBlockEntity be) {
		this(be, state -> true);
	}

	public BracketedBlockEntityBehaviour(SmartBlockEntity be, Predicate<BlockState> pred) {
		super(be);
		this.pred = pred;
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public void applyBracket(BlockState state) {
		this.bracket = state;
		reRender = true;
		blockEntity.notifyUpdate();
		Level world = getWorld();
		if (world.isClientSide)
			return;
		blockEntity.getBlockState()
			.updateNeighbourShapes(world, getPos(), 3);
	}

	public void transformBracket(StructureTransform transform) {
		if (isBracketPresent()) {
			BlockState transformedBracket = transform.apply(bracket);
			applyBracket(transformedBracket);
		}
	}

	@Nullable
	public BlockState removeBracket(boolean inOnReplacedContext) {
		if (bracket == null) {
			return null;
		}

		BlockState removed = this.bracket;
		Level world = getWorld();
		if (!world.isClientSide)
			world.levelEvent(2001, getPos(), Block.getId(bracket));
		this.bracket = null;
		reRender = true;
		if (inOnReplacedContext) {
			blockEntity.sendData();
			return removed;
		}
		blockEntity.notifyUpdate();
		if (world.isClientSide)
			return removed;
		blockEntity.getBlockState()
			.updateNeighbourShapes(world, getPos(), 3);
		return removed;
	}

	public boolean isBracketPresent() {
		return bracket != null;
	}
	
	public boolean isBracketValid(BlockState bracketState) {
		return bracketState.getBlock() instanceof BracketBlock;
	}

	@Nullable
	public BlockState getBracket() {
		return bracket;
	}

	public boolean canHaveBracket() {
		return pred.test(blockEntity.getBlockState());
	}

	@Override
	public ItemRequirement getRequiredItems() {
		if (!isBracketPresent()) {
			return ItemRequirement.NONE;
		}
		return ItemRequirement.of(bracket, null);
	}

	@Override
	public boolean isSafeNBT() {
		return true;
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		if (isBracketPresent() && isBracketValid(bracket)) {
			nbt.put("Bracket", NbtUtils.writeBlockState(bracket));
		}
		if (clientPacket && reRender) {
			NBTHelper.putMarker(nbt, "Redraw");
			reRender = false;
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		if (nbt.contains("Bracket")) {
			bracket = null;
			BlockState readBlockState = NbtUtils.readBlockState(blockEntity.blockHolderGetter(), nbt.getCompound("Bracket"));
			if (isBracketValid(readBlockState))
				bracket = readBlockState;
		}
		if (clientPacket && nbt.contains("Redraw"))
			getWorld().sendBlockUpdated(getPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 16);
		super.read(nbt, clientPacket);
	}

}
