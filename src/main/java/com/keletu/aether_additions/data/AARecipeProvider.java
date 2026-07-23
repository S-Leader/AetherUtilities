package com.keletu.aether_additions.data;

import com.aetherteam.aether.block.AetherBlocks;
import com.aetherteam.aether.data.providers.AetherRecipeProvider;
import com.aetherteam.aether.item.AetherItems;
import com.keletu.aether_additions.AetherAdditions;
import com.keletu.aether_additions.item.AAPItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class AARecipeProvider extends AetherRecipeProvider {

    public AARecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, AetherAdditions.MODID);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        crossbow(output, AAPItems.SKYROOT_CROSSBOW.get(), AetherBlocks.SKYROOT_PLANKS.get(), "skyroot_crossbow");
        crossbow(output, AAPItems.HOLYSTONE_CROSSBOW.get(), AetherBlocks.HOLYSTONE.get(), "holystone_crossbow");
        crossbow(output, AAPItems.ZANITE_CROSSBOW.get(), AetherItems.ZANITE_GEMSTONE.get(), "zanite_crossbow");
        crossbow(output, AAPItems.GRAVITITE_CROSSBOW.get(), AetherBlocks.ENCHANTED_GRAVITITE.get(), "gravitite_crossbow");

        this.repairingRecipe(RecipeCategory.COMBAT, AAPItems.SKYROOT_CROSSBOW.get(), 250).group("altar_corssbow_repair").save(output, this.name("skyroot_corssbow_repairing"));
        this.repairingRecipe(RecipeCategory.COMBAT, AAPItems.HOLYSTONE_CROSSBOW.get(), 500).group("altar_corssbow_repair").save(output, this.name("holystone_corssbow_repairing"));
        this.repairingRecipe(RecipeCategory.COMBAT, AAPItems.ZANITE_CROSSBOW.get(), 750).group("altar_corssbow_repair").save(output, this.name("zanite_corssbow_repairing"));
        this.repairingRecipe(RecipeCategory.COMBAT, AAPItems.GRAVITITE_CROSSBOW.get(), 1500).group("altar_corssbow_repair").save(output, this.name("gravitite_corssbow_repairing"));

    }

    private static void crossbow(RecipeOutput output, ItemLike result, ItemLike material, String id) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result).pattern("SMS").pattern("PTP").pattern(" S ").define('S', AetherItems.SKYROOT_STICK.get()).define('M', material).define('P', Tags.Items.STRINGS).define('T', Items.TRIPWIRE_HOOK).group("aether_crossbows").unlockedBy("has_" + getItemName(material), has(material)).unlockedBy("has_tripwire_hook", has(Items.TRIPWIRE_HOOK)).save(output, ResourceLocation.fromNamespaceAndPath(AetherAdditions.MODID, id));
    }
}