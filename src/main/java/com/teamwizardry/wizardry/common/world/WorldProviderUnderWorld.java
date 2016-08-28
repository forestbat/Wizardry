package com.teamwizardry.wizardry.common.world;

import com.google.common.base.Predicate;
import com.teamwizardry.librarianlib.common.util.MethodHandleHelper;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.lib.LibObfuscation;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.google.common.base.Throwables.propagate;
import static com.ibm.icu.impl.CurrencyData.provider;

/**
 * Created by LordSaad44
 */
public class WorldProviderUnderWorld extends WorldProvider {
    
    public WorldProviderUnderWorld() {
        UnderworldProviderEventHandler.add(this);
    }
    
    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorUnderWorld(worldObj);
    }

    @Override
    public DimensionType getDimensionType() {
        return Wizardry.underWorld;
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    @Override
    public boolean canDoRainSnowIce(Chunk chunk) {
        return false;
    }

    @Override
    public boolean isSurfaceWorld() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isSkyColored() {
        return true;
    }

    @Override
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        return new Vec3d(0, 180, 50);
    }


    @Override
    public String getSaveFolder() {
        return "underworld";
    }
    
    
    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return super.calculateCelestialAngle(worldTime, partialTicks);
    }
    
    public void wrapChunk(Chunk chunk) {
        if(chunk.getWorld() != this.worldObj)
            return;
        IChunkProvider provider = UnderworldMethodHandles.getProvider(worldObj);
        Long2ObjectMap<Chunk> mapping = null;
        if(provider instanceof ChunkProviderClient) {
            mapping = UnderworldMethodHandles.getChunkMappingClient((ChunkProviderClient) provider);
        }
        if(provider instanceof ChunkProviderServer) {
            mapping = UnderworldMethodHandles.getChunkMappingServer((ChunkProviderServer) provider);
        }
        if(mapping != null) {
            WrappedChunk wrapped = new WrappedChunk(chunk);
            mapping.put(ChunkPos.chunkXZ2Int(chunk.xPosition, chunk.zPosition), wrapped);
        }
    }
    
    private static class UnderworldMethodHandles {
        private static final MethodHandle getProvider = MethodHandleHelper.handleForField(World.class, true, LibObfuscation.WORLD_CHUNKPROVIDER);
        private static final MethodHandle getChunkMappingClient = MethodHandleHelper.handleForField(ChunkProviderClient.class, true, LibObfuscation.CHUNKPROVIDERCLIENT_CHUNKMAPPING);
        private static final MethodHandle getChunkMappingServer = MethodHandleHelper.handleForField(ChunkProviderServer.class, true, LibObfuscation.CHUNKPROVIDERSERVER_ID2CHUNKMAP);
    
        private static final MethodHandle chunkGenerateHeightMap = MethodHandleHelper.handleForMethod(Chunk.class, LibObfuscation.CHUNK_GENERATEHEIGHTMAP);
        private static final MethodHandle chunkPopulateChunk = MethodHandleHelper.handleForMethod(Chunk.class, LibObfuscation.CHUNK_POPULATECHUNK, IChunkGenerator.class);
        private static final MethodHandle chunkPropagateSkylightOcclusion = MethodHandleHelper.handleForMethod(Chunk.class, LibObfuscation.CHUNK_PROPAGATESKYLIGHTOCCLUSION, int.class, int.class);
    
        private static final MethodHandle chunkGetPrecipitationHeightMap = MethodHandleHelper.handleForField(Chunk.class, true, LibObfuscation.CHUNK_PRECIPITATIONHEIGHTMAP);
        private static final MethodHandle chunkGetHeightMap = MethodHandleHelper.handleForField(Chunk.class, true, LibObfuscation.CHUNK_HEIGHTMAP);
        private static final MethodHandle chunkSetIsModified = MethodHandleHelper.handleForField(Chunk.class, false, LibObfuscation.CHUNK_ISMODIFIED);
        private static final MethodHandle chunkGetHeightMapMinimum = MethodHandleHelper.handleForField(Chunk.class, true, LibObfuscation.CHUNK_HEIGHTMAPMINIMUM);
        private static final MethodHandle chunkSetHeightMapMinimum = MethodHandleHelper.handleForField(Chunk.class, false, LibObfuscation.CHUNK_HEIGHTMAPMINIMUM);
    
        public static IChunkProvider getProvider(@Nonnull World obj) {
            try {
                return (IChunkProvider) getProvider.invokeExact(obj);
            } catch (Throwable t) {
                throw propagate(t);
            }
        }
    
        public static Long2ObjectMap<Chunk> getChunkMappingClient(@Nonnull ChunkProviderClient obj) {
            try {
                return (Long2ObjectMap<Chunk>) getChunkMappingClient.invokeExact(obj);
            } catch (Throwable t) {
                throw propagate(t);
            }
        }
    
        public static Long2ObjectMap<Chunk> getChunkMappingServer(@Nonnull ChunkProviderServer obj) {
            try {
                return (Long2ObjectMap<Chunk>) getChunkMappingServer.invokeExact(obj);
            } catch (Throwable t) {
                throw propagate(t);
            }
        }
    
        public static void chunkGenerateHeightMap(@Nonnull Chunk obj) {
            try {
                chunkGenerateHeightMap.invokeExact(obj);
            } catch (Throwable t) {
                throw propagate(t);
            }
        }
    
        public static void chunkPropagateSkylightOcclusion(@Nonnull Chunk obj, int x, int y) {
            try {
                chunkPropagateSkylightOcclusion.invokeExact(obj, x, y);
            } catch (Throwable t) {
                throw propagate(t);
            }
        }
    
        public static void chunkSetIsModified(@Nonnull Chunk obj, boolean value) {
            try {
                chunkSetIsModified.invokeExact(obj, value);
            } catch (Throwable t) {
                throw propagate(t);
            }
        }
        
        public static void chunkPopulateChunk(@Nonnull Chunk obj, IChunkGenerator generator) {
            try {
                chunkPopulateChunk.invokeExact(obj, generator);
            } catch (Throwable t) {
                throw propagate(t);
            }
        }
        
        public static int[] chunkGetPrecipitationHeightMap(@Nonnull Chunk obj) {
            try {
                return (int[]) chunkGetPrecipitationHeightMap.invokeExact(obj);
            } catch (Throwable t) {
                throw propagate(t);
            }
        }
    
        public static int[] chunkGetHeightMap(@Nonnull Chunk obj) {
            try {
                return (int[]) chunkGetHeightMap.invokeExact(obj);
            } catch (Throwable t) {
                throw propagate(t);
            }
        }
    
        public static int chunkGetHeightMapMinimum(@Nonnull Chunk obj) {
            try {
                return (int) chunkGetHeightMapMinimum.invokeExact(obj);
            } catch (Throwable t) {
                throw propagate(t);
            }
        }
    
        public static void chunkSetHeightMapMinimum(@Nonnull Chunk obj, int value) {
            try {
                chunkSetHeightMapMinimum.invokeExact(obj, value);
            } catch (Throwable t) {
                throw propagate(t);
            }
        }
    }
    
    private static class WrappedChunk extends Chunk {
    
        private Chunk toWrap;
        
        public WrappedChunk(Chunk toWrap) {
            super(toWrap.getWorld(), toWrap.xPosition, toWrap.zPosition);
            this.toWrap = toWrap;
        }
    
        @Override
        protected void generateHeightMap() {
            UnderworldMethodHandles.chunkGenerateHeightMap(toWrap);
        }
        
        @Override
        protected void populateChunk(IChunkGenerator generator) {
            UnderworldMethodHandles.chunkPopulateChunk(toWrap, generator);
        }
    
        @Nullable
        public IBlockState setBlockState(BlockPos pos, IBlockState state)
        {
            int[] wrap_precipitationHeightMap = UnderworldMethodHandles.chunkGetPrecipitationHeightMap(toWrap);
            int[] wrap_heightMap = UnderworldMethodHandles.chunkGetHeightMap(toWrap);
            ExtendedBlockStorage[] wrap_storageArrays = toWrap.getBlockStorageArray();
            
            int chunkX = pos.getX() & 15;
            int chunkY = pos.getY();
            int chunkZ = pos.getZ() & 15;
            int columnIndex = chunkZ << 4 | chunkX;
        
            if (chunkY >= wrap_precipitationHeightMap[columnIndex] - 1)
            {
                wrap_precipitationHeightMap[columnIndex] = -999;
            }
        
            int currentHeight = wrap_heightMap[columnIndex];
            IBlockState oldState = this.getBlockState(pos);
        
            if (oldState == state)
            {
                return null;
            }
            else
            {
                Block newBlock = state.getBlock();
                Block oldBlock = oldState.getBlock();
                int oldOpacity = oldState.getLightOpacity(toWrap.getWorld(), pos); // Relocate old light value lookup here, so that it is called before TE is removed.
                ExtendedBlockStorage storage = wrap_storageArrays[chunkY >> 4];
                boolean createdNewTopStorage = false;
            
                if (storage == NULL_BLOCK_STORAGE)
                {
                    if (newBlock == Blocks.AIR)
                    {
                        return null;
                    }
                
                    storage = new ExtendedBlockStorage(chunkY >> 4 << 4, !toWrap.getWorld().provider.getHasNoSky());
                    wrap_storageArrays[chunkY >> 4] = storage;
                    createdNewTopStorage = chunkY >= currentHeight;
                }
            
                storage.set(chunkX, chunkY & 15, chunkZ, state);
            
                //if (block1 != block)
                {
                    if (!toWrap.getWorld().isRemote)
                    {
                        if (oldBlock != newBlock) //Only fire block breaks when the block changes.
                            oldBlock.breakBlock(toWrap.getWorld(), pos, oldState);
                        TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                        if (te != null && te.shouldRefresh(toWrap.getWorld(), pos, oldState, state)) toWrap.getWorld().removeTileEntity(pos);
                    }
                    else if (oldBlock.hasTileEntity(oldState))
                    {
                        TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                        if (te != null && te.shouldRefresh(toWrap.getWorld(), pos, oldState, state))
                            toWrap.getWorld().removeTileEntity(pos);
                    }
                }
            
                if (storage.get(chunkX, chunkY & 15, chunkZ).getBlock() != newBlock)
                {
                    return null;
                }
                else
                {
                    if (createdNewTopStorage)
                    {
                        this.generateSkylightMap();
                    }
                    else
                    {
                        int opacity = state.getLightOpacity(toWrap.getWorld(), pos);
                    
                        if (opacity > 0)
                        {
                            if (chunkY >= currentHeight)
                            {
                                this.relightBlock(chunkX, chunkY + 1, chunkZ);
                            }
                        }
                        else if (chunkY == currentHeight - 1)
                        {
                            this.relightBlock(chunkX, chunkY, chunkZ);
                        }
                    
                        if (opacity != oldOpacity && (opacity < oldOpacity || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0))
                        {
                            UnderworldMethodHandles.chunkPropagateSkylightOcclusion(toWrap, chunkX, chunkZ);
                        }
                    }
                
                    // If capturing blocks, only run block physics for TE's. Non-TE's are handled in ForgeHooks.onPlaceItemIntoWorld
                    if (!toWrap.getWorld().isRemote && oldBlock != newBlock && (!toWrap.getWorld().captureBlockSnapshots || newBlock.hasTileEntity(state)))
                    {
                        newBlock.onBlockAdded(toWrap.getWorld(), pos, state);
                    }
                
                    if (newBlock.hasTileEntity(state))
                    {
                        TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                    
                        if (te == null)
                        {
                            te = newBlock.createTileEntity(toWrap.getWorld(), state);
                            toWrap.getWorld().setTileEntity(pos, te);
                        }
                    
                        if (te != null)
                        {
                            te.updateContainingBlockInfo();
                        }
                    }
                
                    UnderworldMethodHandles.chunkSetIsModified(toWrap, true);
                    return oldState;
                }
            }
        }
    
        private int getBlockLightOpacity(int x, int y, int z)
        {
            IBlockState state = this.getBlockState(x, y, z); //Forge: Can sometimes be called before we are added to the global world list. So use the less accurate one during that. It'll be recalculated later
            return this.unloaded ? state.getLightOpacity() : state.getLightOpacity(toWrap.getWorld(), new BlockPos(x, y, z));
        }
        
        private void relightBlock(int x, int y, int z)
        {
            int[] wrap_heightMap = UnderworldMethodHandles.chunkGetHeightMap(toWrap);
            ExtendedBlockStorage[] wrap_storageArrays = toWrap.getBlockStorageArray();
    
            int currentHeight = wrap_heightMap[z << 4 | x] & 255;
            int topOpaqueBlock = currentHeight;
        
            if (y > currentHeight)
            {
                topOpaqueBlock = y;
            }
        
            while (topOpaqueBlock > 0 && this.getBlockLightOpacity(x, topOpaqueBlock - 1, z) == 0)
            {
                --topOpaqueBlock;
            }
        
            if (topOpaqueBlock != currentHeight)
            {
                toWrap.getWorld().markBlocksDirtyVertical(x + toWrap.xPosition * 16, z + toWrap.zPosition * 16, topOpaqueBlock, currentHeight);
                wrap_heightMap[z << 4 | x] = topOpaqueBlock;
                int worldX = this.xPosition * 16 + x;
                int worldZ = this.zPosition * 16 + z;
            
                if (!toWrap.getWorld().provider.getHasNoSky())
                {
                    if (topOpaqueBlock < currentHeight)
                    {
                        for (int inbetweenY = topOpaqueBlock; inbetweenY < currentHeight; ++inbetweenY)
                        {
                            ExtendedBlockStorage storage = wrap_storageArrays[inbetweenY >> 4];
                        
                            if (storage != NULL_BLOCK_STORAGE)
                            {
                                storage.setExtSkylightValue(x, inbetweenY & 15, z, 15);
                                toWrap.getWorld().notifyLightSet(new BlockPos((toWrap.xPosition << 4) + x, inbetweenY, (toWrap.zPosition << 4) + z));
                            }
                        }
                    }
                    else
                    {
                        for (int inbetweenY = currentHeight; inbetweenY < topOpaqueBlock; ++inbetweenY)
                        {
                            ExtendedBlockStorage storage = wrap_storageArrays[inbetweenY >> 4];
                        
                            if (storage != NULL_BLOCK_STORAGE)
                            {
                                storage.setExtSkylightValue(x, inbetweenY & 15, z, 0);
                                toWrap.getWorld().notifyLightSet(new BlockPos((toWrap.xPosition << 4) + x, inbetweenY, (toWrap.zPosition << 4) + z));
                            }
                        }
                    }
                
                    int skyLight = 15;
                
                    while (topOpaqueBlock > 0 && skyLight > 0)
                    {
                        --topOpaqueBlock;
                        int opacity = this.getBlockLightOpacity(x, topOpaqueBlock, z);
                    
                        if (opacity == 0)
                        {
                            opacity = 1;
                        }
                    
                        skyLight -= opacity;
                    
                        if (skyLight < 0)
                        {
                            skyLight = 0;
                        }
                    
                        ExtendedBlockStorage storage = wrap_storageArrays[topOpaqueBlock >> 4];
                    
                        if (storage != NULL_BLOCK_STORAGE)
                        {
                            storage.setExtSkylightValue(x, topOpaqueBlock & 15, z, skyLight);
                        }
                    }
                }
            
                int afterHeight = wrap_heightMap[z << 4 | x];
                int lightUpdateHeightMin = currentHeight;
                int lightUpdateHeightMax = afterHeight;
            
                if (afterHeight < currentHeight)
                {
                    lightUpdateHeightMin = afterHeight;
                    lightUpdateHeightMax = currentHeight;
                }
            
                if (afterHeight < UnderworldMethodHandles.chunkGetHeightMapMinimum(toWrap))
                {
                    UnderworldMethodHandles.chunkSetHeightMapMinimum(toWrap, afterHeight);
                }
            
                if (!toWrap.getWorld().provider.getHasNoSky())
                {
                    for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
                    {
                        this.updateSkylightNeighborHeight(worldX + enumfacing.getFrontOffsetX(), worldZ + enumfacing.getFrontOffsetZ(), lightUpdateHeightMin, lightUpdateHeightMax);
                    }
                
                    this.updateSkylightNeighborHeight(worldX, worldZ, lightUpdateHeightMin, lightUpdateHeightMax);
                }
    
                UnderworldMethodHandles.chunkSetIsModified(toWrap, true);
            }
        }
    
        private void updateSkylightNeighborHeight(int x, int z, int startY, int endY)
        {
            if (endY > startY && toWrap.getWorld().isAreaLoaded(new BlockPos(x, 0, z), 16))
            {
                for (int y = startY; y < endY; ++y)
                {
                    toWrap.getWorld().checkLightFor(EnumSkyBlock.SKY, new BlockPos(x, y, z));
                }
            
                UnderworldMethodHandles.chunkSetIsModified(toWrap, true);
            }
        }
        
        @Override
        public boolean isAtLocation(int x, int z) {
            return toWrap.isAtLocation(x, z);
        }
    
        @Override
        public int getHeight(BlockPos pos) {
            return toWrap.getHeight(pos);
        }
    
        @Override
        public int getHeightValue(int x, int z) {
            return toWrap.getHeightValue(x, z);
        }
    
        public int getBottomFilledSegment() {
            ExtendedBlockStorage extendedblockstorage = this.getFirstExtendedBlockStorage();
            return extendedblockstorage == null ? 0 : extendedblockstorage.getYLocation();
        }
    
        @Nullable
        private ExtendedBlockStorage getFirstExtendedBlockStorage()
        {
            ExtendedBlockStorage[] wrap_storageArrays = toWrap.getBlockStorageArray();
            for (int i = 0; i < wrap_storageArrays.length; i++)
            {
                if (wrap_storageArrays[i] != NULL_BLOCK_STORAGE)
                {
                    return wrap_storageArrays[i];
                }
            }
        
            return null;
        }
        
        @Override
        public int getTopFilledSegment() {
            return toWrap.getTopFilledSegment();
        }
    
        @Override
        public ExtendedBlockStorage[] getBlockStorageArray() {
            return toWrap.getBlockStorageArray();
        }
    
        @Override
        public void generateSkylightMap() {
            int[] wrap_precipitationHeightMap = UnderworldMethodHandles.chunkGetPrecipitationHeightMap(toWrap);
            int[] wrap_heightMap = UnderworldMethodHandles.chunkGetHeightMap(toWrap);
            ExtendedBlockStorage[] wrap_storageArrays = toWrap.getBlockStorageArray();
            
            int topSegment = this.getTopFilledSegment();
            UnderworldMethodHandles.chunkSetHeightMapMinimum(toWrap, Integer.MAX_VALUE);
    
            for (int x = 0; x < 16; ++x)
            {
                for (int y = 0; y < 16; ++y)
                {
                    wrap_precipitationHeightMap[x + (y << 4)] = -999;
            
                    for (int heightMapCheckY = topSegment + 16; heightMapCheckY > 0; --heightMapCheckY)
                    {
                        if (this.getBlockLightOpacity(x, heightMapCheckY - 1, y) != 0)
                        {
                            wrap_heightMap[y << 4 | x] = heightMapCheckY;
                    
                            if (heightMapCheckY < UnderworldMethodHandles.chunkGetHeightMapMinimum(toWrap))
                            {
                                UnderworldMethodHandles.chunkSetHeightMapMinimum(toWrap, heightMapCheckY);
                            }
                    
                            break;
                        }
                    }
            
                    if (!toWrap.getWorld().provider.getHasNoSky())
                    {
                        int skyLight = 15;
                        int yToCheck = topSegment + 16 - 1;
                
                        while (true)
                        {
                            int opacity = this.getBlockLightOpacity(x, yToCheck, y);
                    
                            if (opacity == 0 && skyLight != 15)
                            {
                                opacity = 1;
                            }
                    
                            skyLight -= opacity;
                    
                            if (skyLight > 0)
                            {
                                ExtendedBlockStorage extendedblockstorage = wrap_storageArrays[yToCheck >> 4];
                        
                                if (extendedblockstorage != NULL_BLOCK_STORAGE)
                                {
                                    extendedblockstorage.setExtSkylightValue(x, yToCheck & 15, y, skyLight);
                                    toWrap.getWorld().notifyLightSet(new BlockPos((this.xPosition << 4) + x, yToCheck, (this.zPosition << 4) + y));
                                }
                            }
                    
                            --yToCheck;
                    
                            if (yToCheck <= 0 || skyLight <= 0)
                            {
                                break;
                            }
                        }
                    }
                }
            }
    
            UnderworldMethodHandles.chunkSetIsModified(toWrap, true);
        }
    
        @Override
        public int getBlockLightOpacity(BlockPos pos) {
            return toWrap.getBlockLightOpacity(pos);
        }
    
        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return toWrap.getBlockState(pos);
        }
    
        @Override
        public IBlockState getBlockState(int x, int y, int z) {
            return toWrap.getBlockState(x, y, z);
        }
    
        @Override
        public int getLightFor(EnumSkyBlock p_177413_1_, BlockPos pos) {
            return toWrap.getLightFor(p_177413_1_, pos);
        }
    
        @Override
        public void setLightFor(EnumSkyBlock p_177431_1_, BlockPos pos, int value) {
            toWrap.setLightFor(p_177431_1_, pos, value);
        }
    
        @Override
        public int getLightSubtracted(BlockPos pos, int amount) {
            return toWrap.getLightSubtracted(pos, amount);
        }
    
        @Override
        public void addEntity(Entity entityIn) {
            toWrap.addEntity(entityIn);
        }
    
        @Override
        public void removeEntity(Entity entityIn) {
            toWrap.removeEntity(entityIn);
        }
    
        @Override
        public void removeEntityAtIndex(Entity entityIn, int index) {
            toWrap.removeEntityAtIndex(entityIn, index);
        }
    
        @Override
        public boolean canSeeSky(BlockPos pos) {
            return toWrap.canSeeSky(pos);
        }
    
        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType p_177424_2_) {
            return toWrap.getTileEntity(pos, p_177424_2_);
        }
    
        @Override
        public void addTileEntity(TileEntity tileEntityIn) {
            toWrap.addTileEntity(tileEntityIn);
        }
    
        @Override
        public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
            toWrap.addTileEntity(pos, tileEntityIn);
        }
    
        @Override
        public void removeTileEntity(BlockPos pos) {
            toWrap.removeTileEntity(pos);
        }
    
        @Override
        public void onChunkLoad() {
            toWrap.onChunkLoad();
        }
    
        @Override
        public void onChunkUnload() {
            toWrap.onChunkUnload();
        }
    
        @Override
        public void setChunkModified() {
            toWrap.setChunkModified();
        }
    
        @Override
        public void getEntitiesWithinAABBForEntity(@Nullable Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> p_177414_4_) {
            toWrap.getEntitiesWithinAABBForEntity(entityIn, aabb, listToFill, p_177414_4_);
        }
    
        @Override
        public <T extends Entity> void getEntitiesOfTypeWithinAAAB(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> p_177430_4_) {
            toWrap.getEntitiesOfTypeWithinAAAB(entityClass, aabb, listToFill, p_177430_4_);
        }
    
        @Override
        public boolean needsSaving(boolean p_76601_1_) {
            return toWrap.needsSaving(p_76601_1_);
        }
    
        @Override
        public Random getRandomWithSeed(long seed) {
            return toWrap.getRandomWithSeed(seed);
        }
    
        @Override
        public boolean isEmpty() {
            return toWrap.isEmpty();
        }
    
        @Override
        public void populateChunk(IChunkProvider chunkProvider, IChunkGenerator chunkGenrator) {
            toWrap.populateChunk(chunkProvider, chunkGenrator);
        }
    
        @Override
        public BlockPos getPrecipitationHeight(BlockPos pos) {
            return toWrap.getPrecipitationHeight(pos);
        }
    
        @Override
        public void onTick(boolean p_150804_1_) {
            toWrap.onTick(p_150804_1_);
        }
    
        @Override
        public boolean isPopulated() {
            return toWrap.isPopulated();
        }
    
        @Override
        public boolean isChunkTicked() {
            return toWrap.isChunkTicked();
        }
    
        @Override
        public ChunkPos getChunkCoordIntPair() {
            return toWrap.getChunkCoordIntPair();
        }
    
        @Override
        public boolean getAreLevelsEmpty(int startY, int endY) {
            return toWrap.getAreLevelsEmpty(startY, endY);
        }
    
        @Override
        public void setStorageArrays(ExtendedBlockStorage[] newStorageArrays) {
            toWrap.setStorageArrays(newStorageArrays);
        }
    
        @Override
        public void fillChunk(PacketBuffer buf, int p_186033_2_, boolean p_186033_3_) {
            toWrap.fillChunk(buf, p_186033_2_, p_186033_3_);
        }
    
        @Override
        public Biome getBiome(BlockPos pos, BiomeProvider provider) {
            return toWrap.getBiome(pos, provider);
        }
    
        @Override
        public byte[] getBiomeArray() {
            return toWrap.getBiomeArray();
        }
    
        @Override
        public void setBiomeArray(byte[] biomeArray) {
            toWrap.setBiomeArray(biomeArray);
        }
    
        @Override
        public void resetRelightChecks() {
            toWrap.resetRelightChecks();
        }
    
        @Override
        public void enqueueRelightChecks() {
            toWrap.enqueueRelightChecks();
        }
    
        @Override
        public void checkLight() {
            toWrap.checkLight();
        }
    
        @Override
        public boolean isLoaded() {
            return toWrap.isLoaded();
        }
    
        @Override
        public void setChunkLoaded(boolean loaded) {
            toWrap.setChunkLoaded(loaded);
        }
    
        @Override
        public World getWorld() {
            return toWrap.getWorld();
        }
    
        @Override
        public int[] getHeightMap() {
            return toWrap.getHeightMap();
        }
    
        @Override
        public void setHeightMap(int[] newHeightMap) {
            toWrap.setHeightMap(newHeightMap);
        }
    
        @Override
        public Map<BlockPos, TileEntity> getTileEntityMap() {
            return toWrap.getTileEntityMap();
        }
    
        @Override
        public ClassInheritanceMultiMap<Entity>[] getEntityLists() {
            return toWrap.getEntityLists();
        }
    
        @Override
        public boolean isTerrainPopulated() {
            return toWrap.isTerrainPopulated();
        }
    
        @Override
        public void setTerrainPopulated(boolean terrainPopulated) {
            toWrap.setTerrainPopulated(terrainPopulated);
        }
    
        @Override
        public boolean isLightPopulated() {
            return toWrap.isLightPopulated();
        }
    
        @Override
        public void setLightPopulated(boolean lightPopulated) {
            toWrap.setLightPopulated(lightPopulated);
        }
    
        @Override
        public void setModified(boolean modified) {
            toWrap.setModified(modified);
        }
    
        @Override
        public void setHasEntities(boolean hasEntitiesIn) {
            toWrap.setHasEntities(hasEntitiesIn);
        }
    
        @Override
        public void setLastSaveTime(long saveTime) {
            toWrap.setLastSaveTime(saveTime);
        }
    
        @Override
        public int getLowestHeight() {
            return toWrap.getLowestHeight();
        }
    
        @Override
        public long getInhabitedTime() {
            return toWrap.getInhabitedTime();
        }
    
        @Override
        public void setInhabitedTime(long newInhabitedTime) {
            toWrap.setInhabitedTime(newInhabitedTime);
        }
    
        @Override
        public void removeInvalidTileEntity(BlockPos pos) {
            toWrap.removeInvalidTileEntity(pos);
        }
    }
}
