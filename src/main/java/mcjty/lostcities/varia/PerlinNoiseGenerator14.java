package mcjty.lostcities.varia;

import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.noise.SimplexNoiseSampler;

// @todo 1.15 copy from 1.14: use the 1.15 version!
public class PerlinNoiseGenerator14 {
   private final SimplexNoiseSampler[] noiseLevels;
   private final int levels;

   public PerlinNoiseGenerator14(long seed, int levelsIn) {
      this.levels = levelsIn;
      this.noiseLevels = new SimplexNoiseSampler[levelsIn];

      for(int i = 0; i < levelsIn; ++i) {
         this.noiseLevels[i] = new SimplexNoiseSampler(new CheckedRandom(seed));
      }

   }

   public double getValue(double x, double z) {
      return this.noiseAt(x, z, false);
   }

   public double noiseAt(double x, double y, boolean useNoiseOffsets) {
      double d0 = 0.0D;
      double d1 = 1.0D;

      for(int i = 0; i < this.levels; ++i) {
         d0 += this.noiseLevels[i].sample(x * d1 + (useNoiseOffsets ? this.noiseLevels[i].originX : 0.0D), y * d1 + (useNoiseOffsets ? this.noiseLevels[i].originY : 0.0D)) / d1;
         d1 /= 2.0D;
      }

      return d0;
   }

   public double getSurfaceNoiseValue(double x, double y, double z, double scale) {
      return this.noiseAt(x, y, true) * 0.55D;
   }
}