package waldinet.towers_of_the_wild_reworked.structure;

import net.minecraft.structure.MarginedStructureStart;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import waldinet.towers_of_the_wild_reworked.TowersOfTheWildReworked;
import waldinet.towers_of_the_wild_reworked.utils.StructUtils;

/**
 * Helper class to move all the IDs from former Structure classes
 */
public abstract class AbstractTowerStructure extends StructureFeature<StructurePoolFeatureConfig>
{
    private static int _structureSize;
    final boolean modifyBoundingBox;
    final boolean surface;

    //#region Constructor
    public AbstractTowerStructure(int structureSize)
    {
        this(structureSize, false, true);
    }

    public AbstractTowerStructure(int structureSize, boolean modifyBoundingBox, boolean surface)
    {
        super(StructurePoolFeatureConfig.CODEC);
        _structureSize = structureSize;
        this.modifyBoundingBox = modifyBoundingBox;
        this.surface = surface;
    }
    //#endregion

    @Override
    public StructureStartFactory<StructurePoolFeatureConfig> getStructureStartFactory()
    {
        return AbstractTowerStructure.Start::new;
    }

    /**
     * Checks if this structure can <em>actually</em> be placed at a potential structure position determined via
     * {@link #getStartChunk}. Specific structures override this method to reduce the spawn probability or
     * restrict the spawn in some other way.
     */
    @SuppressWarnings("ObjectAllocationInLoop")
    @Override
    protected boolean shouldStartAt(
        ChunkGenerator chunkGenerator,
        BiomeSource biomeSource,
        long worldSeed,
        ChunkRandom random,
        ChunkPos pos,
        Biome biome,
        ChunkPos chunkPos,
        StructurePoolFeatureConfig config,
        HeightLimitView world
    ) {
        if (! isFlatTerrain(chunkGenerator, chunkPos, world)) {
            return false;
        }

        StructureConfig configDerelictGrass = chunkGenerator.getStructuresConfig().getForType(TowersOfTheWildReworked.DERELICT_GRASS_TOWER);
        StructureConfig configDerelict = chunkGenerator.getStructuresConfig().getForType(TowersOfTheWildReworked.DERELICT_TOWER);

        // Check chunks if there already are towers present
        for (int chunkX = pos.x; chunkX <= pos.x; ++chunkX) {
            for (int chunkZ = pos.z; chunkZ <= pos.z; ++chunkZ) {
                // Derelict Grass
                if (configDerelictGrass != null) {
                    ChunkPos possibleDerelictGrassPos = TowersOfTheWildReworked.DERELICT_GRASS_TOWER.getStartChunk(configDerelictGrass, worldSeed, random, chunkX, chunkZ);
                    if (chunkX == possibleDerelictGrassPos.x && chunkZ == possibleDerelictGrassPos.z && this != TowersOfTheWildReworked.DERELICT_GRASS_TOWER) {
                        return false;
                    }
                }

                // Derelict
                if (configDerelict != null) {
                    ChunkPos possibleDerelictPos = TowersOfTheWildReworked.DERELICT_TOWER.getStartChunk(configDerelict, worldSeed, random, chunkX, chunkZ);
                    if (chunkX == possibleDerelictPos.x && chunkZ == possibleDerelictPos.z && this != TowersOfTheWildReworked.DERELICT_TOWER) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    protected boolean isFlatTerrain(ChunkGenerator chunkGenerator, ChunkPos chunkPos, HeightLimitView world)
    {
        int offset = _structureSize;
        int xStart = chunkPos.x * 16;
        int zStart = chunkPos.z * 16;

        Heightmap.Type type = Heightmap.Type.WORLD_SURFACE_WG;

        int i1 = chunkGenerator.getHeight(xStart, zStart, type, world);
        int j1 = chunkGenerator.getHeight(xStart, zStart + offset, type, world);
        int k1 = chunkGenerator.getHeight(xStart + offset, zStart, type, world);
        int l1 = chunkGenerator.getHeight(xStart + offset, zStart + offset, type, world);
        int minHeight = Math.min(Math.min(i1, j1), Math.min(k1, l1));
        int maxHeight = Math.max(Math.max(i1, j1), Math.max(k1, l1));

        return Math.abs(maxHeight - minHeight) <= 4;
    }

    public static class Start extends MarginedStructureStart<StructurePoolFeatureConfig>
    {
        //#region Constructor
        public Start(StructureFeature<StructurePoolFeatureConfig> structureFeature, ChunkPos chunkPos, int i, long l)
        {
            super(structureFeature, chunkPos, i, l);
        }
        //#endregion

        @Override
        public void init(
            DynamicRegistryManager registryManager,
            ChunkGenerator chunkGenerator,
            StructureManager manager,
            ChunkPos pos,
            Biome biome,
            StructurePoolFeatureConfig config,
            HeightLimitView world
        ) {
            AbstractTowerStructure structure = (AbstractTowerStructure) this.getFeature();

            int x = pos.x * 16;
            int z = pos.z * 16;
            int y = chunkGenerator.getHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG, world);;

            StructUtils.initPools();
            StructurePoolBasedGenerator.generate(
                registryManager,
                config,
                PoolStructurePiece::new,
                chunkGenerator,
                manager,
                new BlockPos(x, y, z),
                this,
                this.random,
                structure.modifyBoundingBox,
                structure.surface,
                world
            );
            this.setBoundingBoxFromChildren();
        }
    }
}