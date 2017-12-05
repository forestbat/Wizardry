package com.teamwizardry.wizardry.common.world.underworld;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author WireSegal
 *         Created at 9:42 PM on 12/3/17.
 */
public class UnderworldChunkWrapper extends Chunk {

    public UnderworldChunkWrapper(Chunk chunk) {
        super(chunk.world, chunk.x, chunk.z);
        this.storageArrays = chunk.storageArrays;
        this.blockBiomeArray = chunk.blockBiomeArray;
        this.precipitationHeightMap = chunk.precipitationHeightMap;
        this.updateSkylightColumns = chunk.updateSkylightColumns;
        this.loaded = chunk.loaded;
        this.world = chunk.world;
        this.heightMap = chunk.heightMap;
        this.x = chunk.x;
        this.z = chunk.z;
        this.isGapLightingUpdated = chunk.isGapLightingUpdated;
        this.tileEntities = chunk.tileEntities;
        this.entityLists = chunk.entityLists;
        this.isTerrainPopulated = chunk.isTerrainPopulated;
        this.isLightPopulated = chunk.isLightPopulated;
        this.ticked = chunk.ticked;
        this.dirty = chunk.dirty;
        this.hasEntities = chunk.hasEntities;
        this.lastSaveTime = chunk.lastSaveTime;
        this.heightMapMinimum = chunk.heightMapMinimum;
        this.inhabitedTime = chunk.inhabitedTime;
        this.queuedLightChecks = chunk.queuedLightChecks;
        this.tileEntityPosQueue = chunk.tileEntityPosQueue;
        this.unloadQueued = chunk.unloadQueued;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void generateHeightMap() {
        int top = this.getBottomFilledSegment();
        this.heightMapMinimum = -1; // modified line: maximum not minimum

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                this.precipitationHeightMap[x + (z << 4)] = -999;

                for (int y = 0; y < top + 16; y++) { // modified line: reversed iteration direction
                    if (this.getBlockLightOpacity(x, y + 1, z) != 0) { // modified line: + not -
                        this.heightMap[z << 4 | x] = y;

                        if (y > this.heightMapMinimum) { // modified line: maximum not minimum
                            this.heightMapMinimum = y;
                        }

                        break;
                    }
                }
            }
        }

        this.dirty = true;
    }

    @Override
    public void generateSkylightMap() {
        int top = this.getBottomFilledSegment();
        this.heightMapMinimum = -1; // modified line: maximum not minimum

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                this.precipitationHeightMap[x + (z << 4)] = -999;

                for (int y = 0; y < top + 16; y++) { // modified line: reversed iteration direction
                    if (this.getBlockLightOpacity(x, y + 1, z) != 0) { // modified line: + not -
                        this.heightMap[z << 4 | x] = y;

                        if (y > this.heightMapMinimum) { // modified line: maximum not minimum
                            this.heightMapMinimum = y;
                        }

                        break;
                    }
                }

                if (this.world.provider.hasSkyLight()) {
                    int light = 15;
                    int position = 0; // modified line

                    while (true) {
                        int opacity = this.getBlockLightOpacity(x, position, z);

                        if (opacity == 0 && light != 15) {
                            opacity = 1;
                        }

                        light -= opacity;

                        if (light > 0) {
                            setSkyLightAtForce(x, position, z, light);
                        }

                        ++position; // modified line

                        if (position > 255 || light <= 0) {
                            break;
                        }
                    }
                }
            }
        }

        this.dirty = true;
    }

    public void setSkyLightAt(int subX, int absY, int subZ, int light) {
        ExtendedBlockStorage storage = this.storageArrays[absY >> 4];

        if (storage != NULL_BLOCK_STORAGE) {
            storage.setSkyLight(subX, absY & 15, subZ, light);
            this.world.notifyLightSet(new BlockPos((this.x << 4) + subX, absY, (this.z << 4) + subZ));
        }
    }

    public void setSkyLightAtForce(int subX, int absY, int subZ, int light) {
        ExtendedBlockStorage storage = this.storageArrays[absY >> 4];

        if (storage == NULL_BLOCK_STORAGE) {
            storage = new ExtendedBlockStorage(absY >> 4 << 4, this.world.provider.hasSkyLight());
            this.storageArrays[absY >> 4] = storage;
        }

        storage.setSkyLight(subX, absY & 15, subZ, light);
        this.world.notifyLightSet(new BlockPos((this.x << 4) + subX, absY, (this.z << 4) + subZ));
    }

    @Override
    public void relightBlock(int x, int y, int z) {
        int bottomBlock = this.heightMap[z << 4 | x] & 255;
        int relightHeight = bottomBlock;

        if (y < bottomBlock) { // modified to swap direction
            relightHeight = y;
        }

        while (relightHeight < 255 && this.getBlockLightOpacity(x, relightHeight + 1, z) == 0) { // modified to swap
            ++relightHeight;
        }

        if (relightHeight != bottomBlock) {
            this.world.markBlocksDirtyVertical(x + this.x * 16, z + this.z * 16, relightHeight, bottomBlock);
            this.heightMap[z << 4 | x] = relightHeight;
            int realX = this.x * 16 + x;
            int realZ = this.z * 16 + z;

            if (this.world.provider.hasSkyLight()) {
                if (relightHeight < bottomBlock) {
                    for (int absY = relightHeight; absY < bottomBlock; ++absY) {
                        setSkyLightAt(x, absY, z, 0);
                    }
                } else {
                    for (int absY = bottomBlock; absY > relightHeight; ++absY) {
                        setSkyLightAt(x, absY, z, 15);
                    }
                }

                int light = 15;

                while (relightHeight < 15 && light > 0) {
                    ++relightHeight;
                    int opacity = this.getBlockLightOpacity(x, relightHeight, z);

                    if (opacity == 0) {
                        opacity = 1;
                    }

                    light -= opacity;

                    if (light < 0) {
                        light = 0;
                    }

                    ExtendedBlockStorage storage = this.storageArrays[relightHeight >> 4];

                    if (storage != NULL_BLOCK_STORAGE) {
                        storage.setSkyLight(x, relightHeight & 15, z, light);
                    }
                }
            }

            int newRelight = this.heightMap[z << 4 | x];
            int height = bottomBlock;
            int lowerBound = newRelight;

            if (newRelight > bottomBlock) { // modified
                height = newRelight;
                lowerBound = bottomBlock;
            }

            if (newRelight > this.heightMapMinimum) { // modified
                this.heightMapMinimum = newRelight;
            }

            if (this.world.provider.hasSkyLight()) {
                for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                    this.updateSkylightNeighborHeight(realX + enumfacing.getFrontOffsetX(), realZ + enumfacing.getFrontOffsetZ(), lowerBound, height);
                }

                this.updateSkylightNeighborHeight(realX, realZ, lowerBound, height);
            }

            this.dirty = true;
        }
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        int subX = pos.getX() & 15;
        int absY = pos.getY();
        int subZ = pos.getZ() & 15;
        return absY <= this.heightMap[subZ << 4 | subX];
    }

    @Nullable
    public IBlockState setBlockState(BlockPos pos, @Nonnull IBlockState state) {
        int chunkX = pos.getX() & 15;
        int y = pos.getY();
        int chunkZ = pos.getZ() & 15;
        int inlinePosition = chunkZ << 4 | chunkX;

        if (y >= this.precipitationHeightMap[inlinePosition] - 1) {
            this.precipitationHeightMap[inlinePosition] = -999;
        }

        int height = this.heightMap[inlinePosition];
        IBlockState oldState = this.getBlockState(pos);

        if (oldState == state) {
            return null;
        } else {
            Block newBlock = state.getBlock();
            Block oldBlock = oldState.getBlock();
            int oldOpacity = oldState.getLightOpacity(this.world, pos);
            ExtendedBlockStorage storage = this.storageArrays[y >> 4];
            boolean yShifted = false;

            if (storage == NULL_BLOCK_STORAGE) {
                if (newBlock == Blocks.AIR) {
                    return null;
                }

                storage = new ExtendedBlockStorage(y >> 4 << 4, this.world.provider.hasSkyLight());
                this.storageArrays[y >> 4] = storage;
                yShifted = y < height;
            }

            storage.set(chunkX, y & 15, chunkZ, state);

            if (!this.world.isRemote) {
                if (oldBlock != newBlock)
                    oldBlock.breakBlock(this.world, pos, oldState);
                TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                if (te != null && te.shouldRefresh(this.world, pos, oldState, state))
                    this.world.removeTileEntity(pos);
            } else if (oldBlock.hasTileEntity(oldState)) {
                TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                if (te != null && te.shouldRefresh(this.world, pos, oldState, state))
                    this.world.removeTileEntity(pos);
            }

            if (storage.get(chunkX, y & 15, chunkZ).getBlock() != newBlock) {
                return null;
            } else {
                if (yShifted) {
                    this.generateSkylightMap();
                } else {
                    int newOpacity = state.getLightOpacity(this.world, pos);

                    if (newOpacity > 0) {
                        if (y <= height) { // flip
                            this.relightBlock(chunkX, y - 1, chunkZ); // update below
                        }
                    } else if (y == height + 1) { // update below
                        this.relightBlock(chunkX, y, chunkZ);
                    }

                    if (newOpacity != oldOpacity && (newOpacity < oldOpacity || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                        this.propagateSkylightOcclusion(chunkX, chunkZ);
                    }
                }

                if (!this.world.isRemote && oldBlock != newBlock && (!this.world.captureBlockSnapshots || newBlock.hasTileEntity(state))) {
                    newBlock.onBlockAdded(this.world, pos, state);
                }

                if (newBlock.hasTileEntity(state)) {
                    TileEntity newTile = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

                    if (newTile == null) {
                        newTile = newBlock.createTileEntity(this.world, state);
                        this.world.setTileEntity(pos, newTile);
                    }

                    if (newTile != null) {
                        newTile.updateContainingBlockInfo();
                    }
                }

                this.dirty = true;
                return oldState;
            }
        }
    }

    @Nullable
    public ExtendedBlockStorage getBottomExtendedBlockStorage()
    {
        for (ExtendedBlockStorage storage : storageArrays)
        {
            if (storage != NULL_BLOCK_STORAGE)
            {
                return storage;
            }
        }

        return null;
    }

    public int getBottomFilledSegment() {
        ExtendedBlockStorage storage = this.getBottomExtendedBlockStorage();
        return storage == null ? 0 : storage.getYLocation();
    }

    @Override
    public boolean checkLight(int x, int z) {
        int top = this.getBottomFilledSegment();
        BlockPos.MutableBlockPos point = new BlockPos.MutableBlockPos((this.x << 4) + x, 0, (this.z << 4) + z);

        for (int y = 0; y < top + 16; y++) {
            point.setPos(point.getX(), y, point.getZ());

            if (this.getBlockState(point).getLightValue(this.world, point) > 0) {
                this.world.checkLight(point);
            }
        }

        return true;
    }

    @Override
    public void updateSkylightNeighborHeight(int x, int z, int startY, int endY) {
        if (endY < startY && this.world.isAreaLoaded(new BlockPos(x, 0, z), 16)) {
            for (int y = endY; y < startY; ++y) {
                this.world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(x, y, z));
            }

            this.dirty = true;
        }
    }
}
