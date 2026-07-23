package com.keletu.aether_additions.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A directional chain of climbable bean-vine segments.
 *
 * FACING points from the supporting block/segment toward the tip of the vine.
 * The six CONNECTION_* properties only describe perpendicular branches. They
 * are used by the multipart blockstate to add wall-like stem pieces between a
 * vertical vine and a horizontal vine.
 */
public class BeanVineBlock extends Block {
    public static final MapCodec<BeanVineBlock> CODEC = simpleCodec(BeanVineBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<BeanVineVariant> VARIANT = EnumProperty.create("variant", BeanVineVariant.class);
    public static final BooleanProperty GROWING = BooleanProperty.create("growing");
    public static final IntegerProperty GROWTH_INDEX = IntegerProperty.create("growth_index", 0, 9);

    public static final BooleanProperty CONNECTION_DOWN = BooleanProperty.create("connection_down");
    public static final BooleanProperty CONNECTION_UP = BooleanProperty.create("connection_up");
    public static final BooleanProperty CONNECTION_NORTH = BooleanProperty.create("connection_north");
    public static final BooleanProperty CONNECTION_SOUTH = BooleanProperty.create("connection_south");
    public static final BooleanProperty CONNECTION_WEST = BooleanProperty.create("connection_west");
    public static final BooleanProperty CONNECTION_EAST = BooleanProperty.create("connection_east");

    private static final int BREAK_DELAY = 2;
    public static final int GROWTH_DELAY = 2;
    public static final int MAX_GROWTH_INDEX = 9;

    // 8 x 8 visual/selection body, matching the width of a vanilla wall post.
    private static final VoxelShape Y_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    private static final VoxelShape X_SHAPE = Block.box(0.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D);
    private static final VoxelShape Z_SHAPE = Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 16.0D);

    // Half-block visual arms used at perpendicular junctions.
    private static final VoxelShape CONNECTION_DOWN_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D);
    private static final VoxelShape CONNECTION_UP_SHAPE = Block.box(4.0D, 8.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    private static final VoxelShape CONNECTION_NORTH_SHAPE = Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 8.0D);
    private static final VoxelShape CONNECTION_SOUTH_SHAPE = Block.box(4.0D, 4.0D, 8.0D, 12.0D, 12.0D, 16.0D);
    private static final VoxelShape CONNECTION_WEST_SHAPE = Block.box(0.0D, 4.0D, 4.0D, 8.0D, 12.0D, 12.0D);
    private static final VoxelShape CONNECTION_EAST_SHAPE = Block.box(8.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D);

    // Keep the physical climbing core at 4 x 4. An 8 x 8 collision post stops
    // the player's centre in the neighbouring air block, preventing the vanilla
    // climbable-block check from seeing the bean vine beneath the player.
    private static final VoxelShape Y_COLLISION_SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final VoxelShape X_COLLISION_SHAPE = Block.box(0.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
    private static final VoxelShape Z_COLLISION_SHAPE = Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 16.0D);

    public BeanVineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(VARIANT, BeanVineVariant.NONE)
                .setValue(GROWING, false)
                .setValue(GROWTH_INDEX, MAX_GROWTH_INDEX)
                .setValue(CONNECTION_DOWN, false)
                .setValue(CONNECTION_UP, false)
                .setValue(CONNECTION_NORTH, false)
                .setValue(CONNECTION_SOUTH, false)
                .setValue(CONNECTION_WEST, false)
                .setValue(CONNECTION_EAST, false));
    }

    @Override
    public MapCodec<BeanVineBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = switch (state.getValue(FACING).getAxis()) {
            case X -> X_SHAPE;
            case Y -> Y_SHAPE;
            case Z -> Z_SHAPE;
        };

        if (state.getValue(CONNECTION_DOWN)) {
            shape = Shapes.or(shape, CONNECTION_DOWN_SHAPE);
        }
        if (state.getValue(CONNECTION_UP)) {
            shape = Shapes.or(shape, CONNECTION_UP_SHAPE);
        }
        if (state.getValue(CONNECTION_NORTH)) {
            shape = Shapes.or(shape, CONNECTION_NORTH_SHAPE);
        }
        if (state.getValue(CONNECTION_SOUTH)) {
            shape = Shapes.or(shape, CONNECTION_SOUTH_SHAPE);
        }
        if (state.getValue(CONNECTION_WEST)) {
            shape = Shapes.or(shape, CONNECTION_WEST_SHAPE);
        }
        if (state.getValue(CONNECTION_EAST)) {
            shape = Shapes.or(shape, CONNECTION_EAST_SHAPE);
        }
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING).getAxis()) {
            case X -> X_COLLISION_SHAPE;
            case Y -> Y_COLLISION_SHAPE;
            case Z -> Z_COLLISION_SHAPE;
        };
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return true;
    }

    /** Returns the block directly behind this segment, toward the root. */
    public static BlockPos getSupportPos(BlockPos pos, Direction facing) {
        return pos.relative(facing.getOpposite());
    }

    /**
     * A segment may be supported by a sturdy face, a preceding segment growing
     * in the same direction, or a bean-vine segment running vertically while this
     * segment runs horizontally (and vice versa). This permits both branch directions
     * while still rejecting a same-axis branch growing backwards.
     */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = getSupportPos(pos, facing);
        BlockState supportState = level.getBlockState(supportPos);

        if (supportState.is(this)) {
            Direction supportFacing = supportState.getValue(FACING);
            return supportFacing == facing || isVerticalHorizontalPair(supportFacing, facing);
        }
        return supportState.isFaceSturdy(level, supportPos, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction changedSide, BlockState neighborState,
                                     LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        // A visual arm belongs to the segment containing the empty half-block gap.
        // Example: a vertical segment with an east-facing horizontal neighbour gets
        // an east arm; the horizontal neighbour already reaches its west block face.
        state = state.setValue(connectionProperty(changedSide),
                shouldConnectPerpendicularly(state, changedSide, neighborState));

        Direction facing = state.getValue(FACING);
        if (changedSide == facing.getOpposite() && !this.canSurvive(state, level, currentPos)) {
            // Scheduling removal creates the visible root-to-tip breaking sequence.
            level.scheduleTick(currentPos, this, BREAK_DELAY);
        }
        return state;
    }

    private boolean shouldConnectPerpendicularly(BlockState state, Direction side, BlockState neighborState) {
        if (!neighborState.is(this)) {
            return false;
        }

        Direction currentFacing = state.getValue(FACING);
        Direction neighborFacing = neighborState.getValue(FACING);
        return isVerticalHorizontalPair(currentFacing, neighborFacing)
                && side.getAxis() == neighborFacing.getAxis();
    }

    private static boolean isVerticalHorizontalPair(Direction first, Direction second) {
        return (first.getAxis() == Direction.Axis.Y) != (second.getAxis() == Direction.Axis.Y);
    }

    private static BooleanProperty connectionProperty(Direction direction) {
        return switch (direction) {
            case DOWN -> CONNECTION_DOWN;
            case UP -> CONNECTION_UP;
            case NORTH -> CONNECTION_NORTH;
            case SOUTH -> CONNECTION_SOUTH;
            case WEST -> CONNECTION_WEST;
            case EAST -> CONNECTION_EAST;
        };
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        BlockPos nextPos = pos.relative(facing);

        if (!this.canSurvive(state, level, pos)) {
            BlockState nextState = level.getBlockState(nextPos);

            level.levelEvent(2001, pos, Block.getId(state));
            level.removeBlock(pos, false);

            // Explicitly queue the following segment so every orientation breaks in
            // a deterministic root-to-tip sequence.
            if (nextState.is(this) && nextState.getValue(FACING) == facing) {
                level.scheduleTick(nextPos, this, BREAK_DELAY);
            }
            return;
        }

        // Only the current tip owns the growth tick. Each successful step turns the
        // old tip off, creates the next segment, and transfers the growth tick to it.
        if (!state.getValue(GROWING)) {
            return;
        }

        int growthIndex = state.getValue(GROWTH_INDEX);
        if (growthIndex >= MAX_GROWTH_INDEX) {
            level.setBlock(pos, state.setValue(GROWING, false), Block.UPDATE_CLIENTS);
            return;
        }

        BlockState nextState = level.getBlockState(nextPos);
        if (!nextState.canBeReplaced() || !nextState.getFluidState().isEmpty()) {
            level.setBlock(pos, state.setValue(GROWING, false), Block.UPDATE_CLIENTS);
            return;
        }

        int nextGrowthIndex = growthIndex + 1;
        boolean continueGrowing = nextGrowthIndex < MAX_GROWTH_INDEX;

        level.setBlock(pos, state.setValue(GROWING, false), Block.UPDATE_CLIENTS);
        BlockState newSegment = this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(VARIANT, BeanVineVariant.random(random))
                .setValue(GROWTH_INDEX, nextGrowthIndex)
                .setValue(GROWING, continueGrowing);
        level.setBlock(nextPos, newSegment, Block.UPDATE_ALL);

        level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                nextPos.getX() + 0.5D,
                nextPos.getY() + 0.5D,
                nextPos.getZ() + 0.5D,
                continueGrowing ? 2 : 8,
                0.18D,
                0.18D,
                0.18D,
                0.01D);

        if (continueGrowing) {
            level.scheduleTick(nextPos, this, GROWTH_DELAY);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState rotated = state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        return switch (rotation) {
            case NONE -> rotated;
            case CLOCKWISE_90 -> rotated
                    .setValue(CONNECTION_NORTH, state.getValue(CONNECTION_WEST))
                    .setValue(CONNECTION_EAST, state.getValue(CONNECTION_NORTH))
                    .setValue(CONNECTION_SOUTH, state.getValue(CONNECTION_EAST))
                    .setValue(CONNECTION_WEST, state.getValue(CONNECTION_SOUTH));
            case CLOCKWISE_180 -> rotated
                    .setValue(CONNECTION_NORTH, state.getValue(CONNECTION_SOUTH))
                    .setValue(CONNECTION_EAST, state.getValue(CONNECTION_WEST))
                    .setValue(CONNECTION_SOUTH, state.getValue(CONNECTION_NORTH))
                    .setValue(CONNECTION_WEST, state.getValue(CONNECTION_EAST));
            case COUNTERCLOCKWISE_90 -> rotated
                    .setValue(CONNECTION_NORTH, state.getValue(CONNECTION_EAST))
                    .setValue(CONNECTION_EAST, state.getValue(CONNECTION_SOUTH))
                    .setValue(CONNECTION_SOUTH, state.getValue(CONNECTION_WEST))
                    .setValue(CONNECTION_WEST, state.getValue(CONNECTION_NORTH));
        };
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        Direction facing = state.getValue(FACING);
        BlockState mirrored = state.setValue(FACING, mirror.getRotation(facing).rotate(facing));

        return switch (mirror) {
            case NONE -> mirrored;
            // LEFT_RIGHT reflects the north/south sides.
            case LEFT_RIGHT -> mirrored
                    .setValue(CONNECTION_NORTH, state.getValue(CONNECTION_SOUTH))
                    .setValue(CONNECTION_SOUTH, state.getValue(CONNECTION_NORTH));
            // FRONT_BACK reflects the east/west sides.
            case FRONT_BACK -> mirrored
                    .setValue(CONNECTION_EAST, state.getValue(CONNECTION_WEST))
                    .setValue(CONNECTION_WEST, state.getValue(CONNECTION_EAST));
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT, GROWING, GROWTH_INDEX,
                CONNECTION_DOWN, CONNECTION_UP,
                CONNECTION_NORTH, CONNECTION_SOUTH,
                CONNECTION_WEST, CONNECTION_EAST);
    }
}
