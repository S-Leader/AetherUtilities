package com.keletu.aether_additions.block;

import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

/**
 * The three visual variants used by bean-vine segments.
 */
public enum BeanVineVariant implements StringRepresentable {
    NONE("none"),
    ONE_LEAF("one_leaf"),
    TWO_LEAVES("two_leaves");

    private static final BeanVineVariant[] VALUES = values();
    private final String serializedName;

    BeanVineVariant(String serializedName) {
        this.serializedName = serializedName;
    }

    public static BeanVineVariant random(RandomSource random) {
        return VALUES[random.nextInt(VALUES.length)];
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
