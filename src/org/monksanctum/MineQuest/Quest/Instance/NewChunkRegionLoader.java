package org.monksanctum.MineQuest.Quest.Instance;

import java.io.DataInput;
import java.io.File;
import java.util.Random;

import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.ChunkLoader;
import net.minecraft.server.ChunkRegionLoader;
import net.minecraft.server.ConvertProgressUpdater;
import net.minecraft.server.Convertable;
import net.minecraft.server.EntityTracker;
import net.minecraft.server.IWorldAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NBTCompressedStreamTools;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.RegionFileCache;
import net.minecraft.server.World;
import net.minecraft.server.WorldLoaderServer;
import net.minecraft.server.WorldManager;
import net.minecraft.server.WorldServer;
import net.minecraft.server.WorldSettings;

import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.LongHashtable;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.monksanctum.MineQuest.MineQuest;

/**
 * The complete idea for this system came from tehbeard.
 * 
 * @author jmonk
 *
 */
public class NewChunkRegionLoader extends ChunkRegionLoader {
	
	public static CraftWorld createWorld(String name, Environment environment, int instance) {
		long seed = (new Random()).nextLong();
        File folder = new File(name);
        CraftWorld world = (CraftWorld) MineQuest.getSServer().getWorld(name);

		if (world != null) {
			((NewChunkRegionLoader) world.getHandle().worldProvider.getChunkProvider()).reloadChunks(world.getHandle(), name + instance);
			return world;
		}

        if ((folder.exists()) && (!folder.isDirectory())) {
            throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
        }

        Convertable converter = new WorldLoaderServer(folder);
        MinecraftServer console = ((CraftServer)MineQuest.getSServer()).getServer();
        if (converter.isConvertable(name)) {
            MineQuest.log("Converting world '" + name + "'");
            converter.convert(name, new ConvertProgressUpdater(console));
        }

        int dimension = 200 + console.worlds.size();
        WorldServer internal = new WorldServer(console, new NewServerNBTManager(new File("."), instance, name, true), name, dimension, new WorldSettings(seed, MineQuest.getSServer().getDefaultGameMode().getValue(), true, false, null), environment, null);
        internal.worldMaps = console.worlds.get(0).worldMaps;

        internal.tracker = new EntityTracker(console, internal);
        internal.addIWorldAccess((IWorldAccess) new WorldManager(console, internal));
        internal.difficulty = 1;
        internal.setSpawnFlags(true, true);
        console.worlds.add(internal);

        MineQuest.getSServer().getPluginManager().callEvent(new WorldInitEvent(internal.getWorld()));
        System.out.print("Preparing start region for level " + (console.worlds.size() -1) + " (Seed: " + internal.getSeed() + ")");

        short short1 = 196;
        long i = System.currentTimeMillis();
        for (int j = -short1; j <= short1; j += 16) {
            for (int k = -short1; k <= short1; k += 16) {
                long l = System.currentTimeMillis();

                if (l < i) {
                    i = l;
                }

                if (l > i + 1000L) {
                    int i1 = (short1 * 2 + 1) * (short1 * 2 + 1);
                    int j1 = (j + short1) * (short1 * 2 + 1) + k + 1;

                    System.out.println("Preparing spawn area for " + name + ", " + (j1 * 100 / i1) + "%");
                    i = l;
                }

                ChunkCoordinates chunkcoordinates = internal.getSpawn();
                internal.chunkProviderServer.getChunkAt(chunkcoordinates.x + j >> 4, chunkcoordinates.z + k >> 4);

                while (internal.updateLights());
            }
        }
        MineQuest.getSServer().getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
        return internal.getWorld();
	}

	private File file2;

	public NewChunkRegionLoader(File file, File file2) {
		super(file2);
		this.file2 = file2;
	}

	@Override
    public Chunk a(World world, int i, int j)
    {
        java.io.DataInputStream datainputstream = RegionFileCache.b(file2, i, j);
        NBTTagCompound nbttagcompound;
        if(datainputstream != null)
        {
            nbttagcompound = NBTCompressedStreamTools.a((DataInput)datainputstream);
        } else
        {
            return null;
        }
        if(!nbttagcompound.hasKey("Level"))
        {
            System.out.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is missing level data, skipping").toString());
            return null;
        }
        if(!nbttagcompound.getCompound("Level").hasKey("Blocks"))
        {
            System.out.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is missing block data, skipping").toString());
            return null;
        }
        Chunk chunk = ChunkLoader.a(world, nbttagcompound.getCompound("Level"));
        if(!chunk.a(i, j))
        {
            System.out.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is in the wrong location; relocating. (Expected ").append(i).append(", ").append(j).append(", got ").append(chunk.x).append(", ").append(chunk.z).append(")").toString());
            nbttagcompound.setInt("xPos", i);
            nbttagcompound.setInt("zPos", j);
            chunk = ChunkLoader.a(world, nbttagcompound.getCompound("Level"));
        }
        chunk.h();
        return chunk;
    }

	@Override
	public void a(World arg0, Chunk arg1) {
		
	}

	private void reloadChunks(WorldServer world, String name) {
		world.chunkProviderServer.chunks = new LongHashtable<Chunk>();

        short short1 = 196;
        long i = System.currentTimeMillis();
        for (int j = -short1; j <= short1; j += 16) {
            for (int k = -short1; k <= short1; k += 16) {
                long l = System.currentTimeMillis();

                if (l < i) {
                    i = l;
                }

                if (l > i + 1000L) {
                    int i1 = (short1 * 2 + 1) * (short1 * 2 + 1);
                    int j1 = (j + short1) * (short1 * 2 + 1) + k + 1;

                    System.out.println("Preparing spawn area for " + name + ", " + (j1 * 100 / i1) + "%");
                    i = l;
                }

                ChunkCoordinates chunkcoordinates = world.getSpawn();
                world.chunkProviderServer.getChunkAt(chunkcoordinates.x + j >> 4, chunkcoordinates.z + k >> 4);

                while (world.updateLights());
            }
        }
	}
//
}
