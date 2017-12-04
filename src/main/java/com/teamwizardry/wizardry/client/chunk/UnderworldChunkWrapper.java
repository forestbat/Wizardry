package com.teamwizardry.wizardry.client.chunk;

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
        int top = this.getTopFilledSegment();
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
        int top = this.getTopFilledSegment();
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
                            ExtendedBlockStorage extendedblockstorage = this.storageArrays[position >> 4];

                            if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                                extendedblockstorage.setSkyLight(x, position & 15, z, light);
                                this.world.notifyLightSet(new BlockPos((this.x << 4) + x, position, (this.z << 4) + z));
                            }
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
                    for (int subY = relightHeight; subY < bottomBlock; ++subY) {
                        ExtendedBlockStorage storage = this.storageArrays[subY >> 4];

                        if (storage != NULL_BLOCK_STORAGE) {
                            storage.setSkyLight(x, subY & 15, z, 0); // modified line
                            this.world.notifyLightSet(new BlockPos((this.x << 4) + x, subY, (this.z << 4) + z));
                        }
                    }
                } else {
                    for (int subY = bottomBlock; subY > relightHeight; ++subY) {
                        ExtendedBlockStorage storage = this.storageArrays[subY >> 4];

                        if (storage != NULL_BLOCK_STORAGE) {
                            storage.setSkyLight(x, subY & 15, z, 15); // modified line: reverse
                            this.world.notifyLightSet(new BlockPos((this.x << 4) + x, subY, (this.z << 4) + z));
                        }
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
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        return j <= this.heightMap[k << 4 | i];
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
            int oldOpacity = oldState.getLightOpacity(this.world, pos); // Relocate old light value lookup here, so that it is called before TE is removed.
            ExtendedBlockStorage storage = this.storageArrays[y >> 4];
            boolean flag = false;

            if (storage == NULL_BLOCK_STORAGE) {
                if (newBlock == Blocks.AIR) {
                    return null;
                }

                storage = new ExtendedBlockStorage(y >> 4 << 4, this.world.provider.hasSkyLight());
                this.storageArrays[y >> 4] = storage;
                flag = y >= height;
            }

            storage.set(chunkX, y & 15, chunkZ, state);

            if (!this.world.isRemote) {
                if (oldBlock != newBlock) //Only fire block breaks when the block changes.
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
                if (flag) {
                    this.generateSkylightMap();
                } else {
                    int newOpacity = state.getLightOpacity(this.world, pos);

                    if (newOpacity > 0) {
                        if (y >= height) {
                            this.relightBlock(chunkX, y - 1, chunkZ); // update below
                        }
                    } else if (y == height + 1) { // update below
                        this.relightBlock(chunkX, y, chunkZ);
                    }

                    if (newOpacity != oldOpacity && (newOpacity < oldOpacity || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                        this.propagateSkylightOcclusion(chunkX, chunkZ);
                    }
                }

                // If capturing blocks, only run block physics for TE's. Non-TE's are handled in ForgeHooks.onPlaceItemIntoWorld
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

    @Override
    public boolean checkLight(int x, int z) {
        int top = this.getTopFilledSegment();
        boolean opacityFlag = false;
        BlockPos.MutableBlockPos point = new BlockPos.MutableBlockPos((this.x << 4) + x, 0, (this.z << 4) + z);

        for (int y = 0; y < top + 16; ++y) {
            point.setPos(point.getX(), y, point.getZ());
            int opacity = this.getBlockLightOpacity(point);

            if (!opacityFlag && opacity > 0) {
                opacityFlag = true;
            } else if (opacityFlag && opacity == 0 && !this.world.checkLight(point)) {
                return false;
            }
        }

        int pointY = point.getY();
        for (int y = 0; y < pointY; y++) {
            point.setPos(point.getX(), y, point.getZ());

            if (this.getBlockState(point).getLightValue(this.world, point) > 0) {
                this.world.checkLight(point);
            }
        }

        return true;
    }

    @Override
    public void updateSkylightNeighborHeight(int x, int z, int startY, int endY) {
        if (endY < startY) {
            int temp = endY;
            endY = startY;
            startY = temp;
        }
        if (endY > startY && this.world.isAreaLoaded(new BlockPos(x, 0, z), 16)) {
            for (int y = startY; y < endY; ++y) {
                this.world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(x, y, z));
            }

            this.dirty = true;
        }
    }
}
