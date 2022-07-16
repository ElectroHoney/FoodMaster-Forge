//package net.electrohoney.foodmastermod.block.custom.abstracts;
//
//import net.electrohoney.foodmastermod.block.entity.custom.AgerBlockEntity;
//import net.minecraft.core.BlockPos;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.MenuProvider;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.context.BlockPlaceContext;
//import net.minecraft.world.level.BlockGetter;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.*;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.entity.BlockEntityTicker;
//import net.minecraft.world.level.block.entity.BlockEntityType;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.block.state.StateDefinition;
//import net.minecraft.world.level.block.state.properties.BlockStateProperties;
//import net.minecraft.world.level.block.state.properties.DirectionProperty;
//import net.minecraft.world.phys.BlockHitResult;
//import net.minecraft.world.phys.shapes.CollisionContext;
//import net.minecraft.world.phys.shapes.VoxelShape;
//import net.minecraftforge.network.NetworkHooks;
//import org.jetbrains.annotations.Nullable;
//
//public abstract class AbstractCookingBlock extends BaseEntityBlock {
//
//    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
//
//    private static final VoxelShape SHAPE = Block.box(3, 3,3, 13, 13, 13);
//
//    protected AbstractCookingBlock(Properties properties) {
//        super(properties);
//    }
//
//    @Override
//    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
//        return SHAPE;
//    }
//
//    /* FACING */
//
//    @Override
//    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
//        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
//    }
//
//    @Override
//    public BlockState rotate(BlockState pState, Rotation pRotation) {
//        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
//    }
//
//    @Override
//    public BlockState mirror(BlockState pState, Mirror pMirror) {
//        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
//    }
//
//    @Override
//    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
//        pBuilder.add(FACING);
//    }
//
//    //    /* BLOCK ENTITY */
//
//    @Override
//    public RenderShape getRenderShape(BlockState pState) {
//        return RenderShape.MODEL;
//    }
//
//    @Override
//    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
//        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
//    }
//
//    @Nullable
//    @Override
//    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
//        return null;
//    }
//
//    @Override
//    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos,
//                                 Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
//        return InteractionResult.sidedSuccess(pLevel.isClientSide());
//    }
//
//}
