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

import javax.annotation.Nullable;

/**
 * @author WireSegal
 *         Created at 9:42 PM on 12/3/17.
 */
public class UnderworldChunkWrapper extends Chunk {

    public UnderworldChunkWrapper(Chunk chunk) {
        super(chunk.getWorld(), chunk.x, chunk.z);
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

    @Override
    public int getTopFilledSegment() {
        return 240;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void generateHeightMap() {
        int i = this.getTopFilledSegment();
        this.heightMapMinimum = -1; // modified line: maximum not minimum

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                this.precipitationHeightMap[j + (k << 4)] = -999;

                for (int l = 0; l < i + 16; l++) { // modified line: reversed iteration direction
                    if (this.getBlockLightOpacity(j, l + 1, k) != 0) { // modified line: + not -
                        this.heightMap[k << 4 | j] = l;

                        if (l > this.heightMapMinimum) { // modified line: maximum not minimum
                            this.heightMapMinimum = l;
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
        int i = this.getTopFilledSegment();
        this.heightMapMinimum = -1; // modified line: maximum not minimum

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                this.precipitationHeightMap[j + (k << 4)] = -999;

                for (int l = 0; l < i + 16; l++) { // modified line: reversed iteration direction
                    if (this.getBlockLightOpacity(j, l + 1, k) != 0) { // modified line: + not -
                        this.heightMap[k << 4 | j] = l;

                        if (l > this.heightMapMinimum) { // modified line: maximum not minimum
                            this.heightMapMinimum = l;
                        }

                        break;
                    }
                }

                 {
                    int k1 = 15;
                    int i1 = 0; // modified line

                    while (true) {
                        int j1 = this.getBlockLightOpacity(j, i1, k);

                        if (j1 == 0 && k1 != 15) {
                            j1 = 1;
                        }

                        k1 -= j1;

                        if (k1 > 0) {
                            ExtendedBlockStorage extendedblockstorage = this.storageArrays[i1 >> 4];

                            if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                                extendedblockstorage.setSkyLight(j, i1 & 15, k, k1);
                                this.world.notifyLightSet(new BlockPos((this.x << 4) + j, i1, (this.z << 4) + k));
                            }
                        }

                        ++i1; // modified line

                        if (i1 > 255 || k1 <= 0) {
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
        int i = this.heightMap[z << 4 | x] & 255;
        int j = i;

        if (y < i) { // modified to swap direction
            j = y;
        }

        while (j < 255 && this.getBlockLightOpacity(x, j + 1, z) == 0) { // modified to swap
            ++j;
        }

        if (j != i) {
            this.world.markBlocksDirtyVertical(x + this.x * 16, z + this.z * 16, j, i);
            this.heightMap[z << 4 | x] = j;
            int k = this.x * 16 + x;
            int l = this.z * 16 + z;

             {
                if (j < i) {
                    for (int j1 = j; j1 < i; ++j1) {
                        ExtendedBlockStorage extendedblockstorage2 = this.storageArrays[j1 >> 4];

                        if (extendedblockstorage2 != NULL_BLOCK_STORAGE) {
                            extendedblockstorage2.setSkyLight(x, j1 & 15, z, 0); // modified line
                            this.world.notifyLightSet(new BlockPos((this.x << 4) + x, j1, (this.z << 4) + z));
                        }
                    }
                } else {
                    for (int i1 = i; i1 > j; ++i1) {
                        ExtendedBlockStorage extendedblockstorage = this.storageArrays[i1 >> 4];

                        if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                            extendedblockstorage.setSkyLight(x, i1 & 15, z, 15); // modified line: reverse
                            this.world.notifyLightSet(new BlockPos((this.x << 4) + x, i1, (this.z << 4) + z));
                        }
                    }
                }

                int k1 = 15;

                while (j < 255 && k1 > 0) {
                    ++j;
                    int i2 = this.getBlockLightOpacity(x, j, z);

                    if (i2 == 0) {
                        i2 = 1;
                    }

                    k1 -= i2;

                    if (k1 < 0) {
                        k1 = 0;
                    }

                    ExtendedBlockStorage extendedblockstorage1 = this.storageArrays[j >> 4];

                    if (extendedblockstorage1 != NULL_BLOCK_STORAGE) {
                        extendedblockstorage1.setSkyLight(x, j & 15, z, k1);
                    }
                }
            }

            int l1 = this.heightMap[z << 4 | x];
            int j2 = i;
            int k2 = l1;

            if (l1 > i) { // modified
                j2 = l1;
                k2 = i;
            }

            if (l1 > this.heightMapMinimum) { // modified
                this.heightMapMinimum = l1;
            }

             {
                for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                    this.updateSkylightNeighborHeight(k + enumfacing.getFrontOffsetX(), l + enumfacing.getFrontOffsetZ(), k2, j2);
                }

                this.updateSkylightNeighborHeight(k, l, k2, j2);
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
    public IBlockState setBlockState(BlockPos pos, IBlockState state) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        int l = k << 4 | i;

        if (j >= this.precipitationHeightMap[l] - 1) {
            this.precipitationHeightMap[l] = -999;
        }

        int i1 = this.heightMap[l];
        IBlockState iblockstate = this.getBlockState(pos);

        if (iblockstate == state) {
            return null;
        } else {
            Block block = state.getBlock();
            Block block1 = iblockstate.getBlock();
            int k1 = iblockstate.getLightOpacity(this.world, pos); // Relocate old light value lookup here, so that it is called before TE is removed.
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];
            boolean flag = false;

            if (extendedblockstorage == NULL_BLOCK_STORAGE) {
                if (block == Blocks.AIR) {
                    return null;
                }

                extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, this.world.provider.hasSkyLight());
                this.storageArrays[j >> 4] = extendedblockstorage;
                flag = j >= i1;
            }

            extendedblockstorage.set(i, j & 15, k, state);

            //if (block1 != block)
            {
                if (!this.world.isRemote) {
                    if (block1 != block) //Only fire block breaks when the block changes.
                        block1.breakBlock(this.world, pos, iblockstate);
                    TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                    if (te != null && te.shouldRefresh(this.world, pos, iblockstate, state))
                        this.world.removeTileEntity(pos);
                } else if (block1.hasTileEntity(iblockstate)) {
                    TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                    if (te != null && te.shouldRefresh(this.world, pos, iblockstate, state))
                        this.world.removeTileEntity(pos);
                }
            }

            if (extendedblockstorage.get(i, j & 15, k).getBlock() != block) {
                return null;
            } else {
                if (flag) {
                    this.generateSkylightMap();
                } else {
                    int j1 = state.getLightOpacity(this.world, pos);

                    if (j1 > 0) {
                        if (j >= i1) {
                            this.relightBlock(i, j - 1, k); // update below
                        }
                    } else if (j == i1 - 1) {
                        this.relightBlock(i, j, k);
                    }

                    if (j1 != k1 && (j1 < k1 || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                        this.propagateSkylightOcclusion(i, k);
                    }
                }

                // If capturing blocks, only run block physics for TE's. Non-TE's are handled in ForgeHooks.onPlaceItemIntoWorld
                if (!this.world.isRemote && block1 != block && (!this.world.captureBlockSnapshots || block.hasTileEntity(state))) {
                    block.onBlockAdded(this.world, pos, state);
                }

                if (block.hasTileEntity(state)) {
                    TileEntity tileentity1 = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

                    if (tileentity1 == null) {
                        tileentity1 = block.createTileEntity(this.world, state);
                        this.world.setTileEntity(pos, tileentity1);
                    }

                    if (tileentity1 != null) {
                        tileentity1.updateContainingBlockInfo();
                    }
                }

                this.dirty = true;
                return iblockstate;
            }
        }
    }

    @Override
    public boolean checkLight(int x, int z) {
        int i = this.getTopFilledSegment();
        boolean flag = false;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos((this.x << 4) + x, 0, (this.z << 4) + z);

        for (int j = 0; j < i + 16; ++j)
        {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), j, blockpos$mutableblockpos.getZ());
            int k = this.getBlockLightOpacity(blockpos$mutableblockpos);

            if (!flag && k > 0)
            {
                flag = true;
            }
            else if (flag && k == 0 && !this.world.checkLight(blockpos$mutableblockpos))
            {
                return false;
            }
        }

        int lm = blockpos$mutableblockpos.getY();
        for (int l = 0; l < lm; l++)
        {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), l, blockpos$mutableblockpos.getZ());

            if (this.getBlockState(blockpos$mutableblockpos).getLightValue(this.world, blockpos$mutableblockpos) > 0)
            {
                this.world.checkLight(blockpos$mutableblockpos);
            }
        }

        return true;
    }
}
