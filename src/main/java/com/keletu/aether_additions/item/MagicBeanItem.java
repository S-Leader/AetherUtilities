package com.keletu.aether_additions.item;

import com.keletu.aether_additions.block.AetherPlantBlocks;
import com.keletu.aether_additions.block.BeanVineBlock;
import com.keletu.aether_additions.block.BeanVineVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Places ten bean-vine segments in the direction of the clicked block face.
 */
public class MagicBeanItem extends Item {
    public static final int VINE_LENGTH = 10;

    public MagicBeanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction growthDirection = context.getClickedFace();
        BlockState clickedState = level.getBlockState(clickedPos);
        BlockPos firstVinePos = clickedPos.relative(growthDirection);
        Player player = context.getPlayer();

        if (!isValidInitialSupport(clickedState, level, clickedPos, growthDirection)) {
            return InteractionResult.FAIL;
        }
        if (player != null && !player.mayUseItemAt(firstVinePos, growthDirection, context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        if (!hasRoomForWholeVine(level, firstVinePos, growthDirection)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            placeVine((ServerLevel) level, firstVinePos, growthDirection);
            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        level.playSound(player, clickedPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static boolean isValidInitialSupport(BlockState clickedState, Level level, BlockPos clickedPos, Direction direction) {
        if (clickedState.is(AetherPlantBlocks.BEAN_VINE.get())) {
            Direction existingDirection = clickedState.getValue(BeanVineBlock.FACING);
            return existingDirection == direction
                    || ((existingDirection.getAxis() == Direction.Axis.Y)
                    != (direction.getAxis() == Direction.Axis.Y));
        }
        return clickedState.isFaceSturdy(level, clickedPos, direction);
    }

    private static boolean hasRoomForWholeVine(Level level, BlockPos firstPos, Direction direction) {
        for (int index = 0; index < VINE_LENGTH; index++) {
            BlockPos pos = firstPos.relative(direction, index);
            if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }

            BlockState state = level.getBlockState(pos);
            if (!state.canBeReplaced() || !state.getFluidState().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void placeVine(ServerLevel level, BlockPos firstPos, Direction direction) {
        BlockState firstSegment = AetherPlantBlocks.BEAN_VINE.get().defaultBlockState()
                .setValue(BeanVineBlock.FACING, direction)
                .setValue(BeanVineBlock.VARIANT, BeanVineVariant.random(level.getRandom()))
                .setValue(BeanVineBlock.GROWTH_INDEX, 0)
                .setValue(BeanVineBlock.GROWING, true);

        level.setBlock(firstPos, firstSegment, Block.UPDATE_ALL);
        level.scheduleTick(firstPos, AetherPlantBlocks.BEAN_VINE.get(), BeanVineBlock.GROWTH_DELAY);
    }
}
