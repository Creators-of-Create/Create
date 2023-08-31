package com.simibubi.create.content.schematics;

import java.util.Iterator;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SchematicAndQuillItem extends Item {

	public SchematicAndQuillItem(Properties properties) {
		super(properties);
	}

	public static void replaceStructureVoidWithAir(CompoundTag nbt) {
		String air = CatnipServices.REGISTRIES.getKeyOrThrow(Blocks.AIR)
			.toString();
		String structureVoid = CatnipServices.REGISTRIES.getKeyOrThrow(Blocks.STRUCTURE_VOID)
			.toString();

		NBTHelper.iterateCompoundList(nbt.getList("palette", 10), c -> {
			if (c.contains("Name") && c.getString("Name")
				.equals(structureVoid)) {
				c.putString("Name", air);
			}
		});
	}

	public static void clampGlueBoxes(Level level, AABB aabb, CompoundTag nbt) {
		ListTag listtag = nbt.getList("entities", 10)
			.copy();

		for (Iterator<Tag> iterator = listtag.iterator(); iterator.hasNext();) {
			Tag tag = iterator.next();
			if (!(tag instanceof CompoundTag compoundtag))
				continue;
			if (compoundtag.contains("nbt") && new ResourceLocation(compoundtag.getCompound("nbt")
				.getString("id")).equals(AllEntityTypes.SUPER_GLUE.getId())) {
				iterator.remove();
			}
		}

		for (SuperGlueEntity entity : SuperGlueEntity.collectCropped(level, aabb)) {
			Vec3 vec3 = new Vec3(entity.getX() - aabb.minX, entity.getY() - aabb.minY, entity.getZ() - aabb.minZ);
			CompoundTag compoundtag = new CompoundTag();
			entity.save(compoundtag);
			BlockPos blockpos = BlockPos.containing(vec3);

			CompoundTag entityTag = new CompoundTag();
			entityTag.put("pos", newDoubleList(vec3.x, vec3.y, vec3.z));
			entityTag.put("blockPos", newIntegerList(blockpos.getX(), blockpos.getY(), blockpos.getZ()));
			entityTag.put("nbt", compoundtag.copy());
			listtag.add(entityTag);
		}

		nbt.put("entities", listtag);
	}

	private static ListTag newIntegerList(int... pValues) {
		ListTag listtag = new ListTag();
		for (int i : pValues)
			listtag.add(IntTag.valueOf(i));
		return listtag;
	}

	private static ListTag newDoubleList(double... pValues) {
		ListTag listtag = new ListTag();
		for (double d0 : pValues)
			listtag.add(DoubleTag.valueOf(d0));
		return listtag;
	}

}
