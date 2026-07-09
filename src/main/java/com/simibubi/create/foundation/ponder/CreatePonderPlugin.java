package com.simibubi.create.foundation.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.crank.ValveHandleBlock;
import com.simibubi.create.content.logistics.packagePort.postbox.PostboxBlock;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlock;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderScenes;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;

import net.createmod.ponder.api.client.level.PonderLevel;
import net.createmod.ponder.api.client.registration.IndexExclusionHelper;
import net.createmod.ponder.api.client.registration.PonderPlugin;
import net.createmod.ponder.api.client.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.client.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.client.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

public class CreatePonderPlugin implements PonderPlugin {

	@Override
	public String getModId() {
		return Create.ID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<Identifier> helper) {
		AllCreatePonderScenes.register(helper);
	}

	@Override
	public void registerTags(PonderTagRegistrationHelper<Identifier> helper) {
		AllCreatePonderTags.register(helper);
	}

	@Override
	public void registerSharedText(SharedTextRegistrationHelper helper) {
		helper.registerSharedText("rpm8", "8 RPM");
		helper.registerSharedText("rpm16", "16 RPM");
		helper.registerSharedText("rpm16_source", "Source: 16 RPM");
		helper.registerSharedText("rpm32", "32 RPM");

		helper.registerSharedText("movement_anchors", "With the help of Super Glue, larger structures can be moved.");
		helper.registerSharedText("behaviour_modify_value_panel", "This behaviour can be modified using the value panel");
		helper.registerSharedText("storage_on_contraption", "Inventories attached to the Contraption will pick up their drops automatically");
	}

	@Override
	public void onPonderLevelRestore(PonderLevel ponderLevel) {
		PonderWorldBlockEntityFix.fixControllerBlockEntities(ponderLevel);
	}

	@Override
	public void indexExclusions(IndexExclusionHelper helper) {
		helper.excludeBlockVariants(ValveHandleBlock.class, AllBlocks.COPPER_VALVE_HANDLE.get());
		helper.excludeBlockVariants(PostboxBlock.class, AllBlocks.PACKAGE_POSTBOXES.get(DyeColor.WHITE).get());
		helper.excludeBlockVariants(TableClothBlock.class, AllBlocks.TABLE_CLOTHS.get(DyeColor.WHITE).get());
	}
}
