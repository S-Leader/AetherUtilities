package com.keletu.aether_additions.data;

import com.keletu.aether_additions.AetherAdditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class AADataGen {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        BlockTagsProvider blockTagsProvider = new BlockTagsProvider(output, lookupProvider, AetherAdditions.MODID, existingFileHelper) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
            }
        };

        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new AATags(output, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new AARecipeProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(), new AALootModifierData(output, lookupProvider));
    }
}