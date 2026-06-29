package com.simibubi.create.foundation.mixin.datafixer;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.simibubi.create.foundation.utility.DataFixerHelper;

import net.minecraft.util.datafix.fixes.BlockPosFormatAndRenamesFix;

@Mixin(BlockPosFormatAndRenamesFix.class)
public abstract class BlockPosFormatAndRenamesFixMixin extends DataFix {
	private BlockPosFormatAndRenamesFixMixin(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Shadow
	protected abstract TypeRewriteRule createEntityFixer(DSL.TypeReference reference, String entityId, Map<String, String> renames);

	@Inject(method = "makeRule", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
	private void create$addFixers(CallbackInfoReturnable<TypeRewriteRule> cir, @Local List<TypeRewriteRule> output) {
		for (DataFixerHelper.BlockPosFixer fixer : DataFixerHelper.BLOCK_POS_FIXERS_VIEW) {
			DSL.TypeReference ref = fixer.reference();
			String id = fixer.id();

			TypeRewriteRule rule;

			if (fixer.customFixer() != null) {
				OpticFinder<?> opticfinder = DSL.namedChoice(id, this.getInputSchema().getChoiceType(ref, id));
				rule = fixTypeEverywhereTyped("BlockPos format for " + id + " (" + ref.typeName() + ")",
						getInputSchema().getType(ref),
						typed -> typed.updateTyped(opticfinder, data ->
									data.update(DSL.remainderFinder(), dynamic ->
										fixer.customFixer().apply(dynamic)
									)
								)
				);
			} else {
				rule = createEntityFixer(ref, id, fixer.renames());
			}

			output.add(rule);
		}
	}
}
