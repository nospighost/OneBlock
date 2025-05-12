package de.Main.OneBlock;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class VoidGen extends ChunkGenerator {
    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, ChunkGenerator.BiomeGrid biome) {
        // Gib einen leeren Chunk zurück, damit keine Blöcke generiert werden
        ChunkData chunkData = createChunkData(world);
        return chunkData;

    }
}
