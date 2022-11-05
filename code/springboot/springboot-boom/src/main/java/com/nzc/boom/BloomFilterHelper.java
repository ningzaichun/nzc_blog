package com.nzc.boom;
 
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;

public class BloomFilterHelper<T> {
 
    private int numHashFunctions;
 
    private int bitSize;
 
    private Funnel<T> funnel;
 
    public BloomFilterHelper(Funnel<T> funnel, int expectedInsertions, double fpp) {
        Preconditions.checkArgument(funnel != null, "funnel不能为空");
        this.funnel = funnel;
        // 计算bit数组长度
        bitSize = optimalNumOfBits(expectedInsertions, fpp);
        // 计算hash方法执行次数
        numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, bitSize);
    }


    /**
     *public <T> boolean mightContain(
     *         T object, Funnel<? super T> funnel, int numHashFunctions, LockFreeBitArray bits) {
     *       long bitSize = bits.bitSize();
     *       byte[] bytes = Hashing.murmur3_128().hashObject(object, funnel).getBytesInternal();
     *       long hash1 = lowerEight(bytes);
     *       long hash2 = upperEight(bytes);
     *
     *       long combinedHash = hash1;
     *       for (int i = 0; i < numHashFunctions; i++) {
     *         // Make the combined hash positive and indexable
     *         if (!bits.get((combinedHash & Long.MAX_VALUE) % bitSize)) {
     *           return false;
     *         }
     *         combinedHash += hash2;
     *       }
     *       return true;
     *     }
     * @param value
     * @return
     */
    public long[] murmurHashOffset(T value) {
//        int[] offset = new int[numHashFunctions];
//        long hash64 = Hashing.murmur3_128().hashObject(value, funnel).asLong();
//        int hash1 = (int) hash64;
//        int hash2 = (int) (hash64 >>> 32);
//        for (int i = 1; i <= numHashFunctions; i++) {
//            int nextHash = hash1 + i * hash2;
//            if (nextHash < 0) {
//                nextHash = ~nextHash;
//            }
//            offset[i - 1] = nextHash % bitSize;
//        }
//        return offset;
        long[] offset = new long[numHashFunctions];
        byte[] bytes = Hashing.murmur3_128().hashObject(value, funnel).asBytes();
        long hash1 = lowerEight(bytes);
        long hash2 = upperEight(bytes);
        long combinedHash = hash1;
        for (int i = 1; i <= numHashFunctions; i++) {
            long nextHash = hash1 + i * hash2;
            if (nextHash < 0) {
                nextHash = ~nextHash;
            }
            offset[i - 1] = nextHash % bitSize;
        }
        return offset;


    }
    private /* static */ long lowerEight(byte[] bytes) {
        return Longs.fromBytes(
                bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
    }

    private /* static */ long upperEight(byte[] bytes) {
        return Longs.fromBytes(
                bytes[15], bytes[14], bytes[13], bytes[12], bytes[11], bytes[10], bytes[9], bytes[8]);
    }
    /**
     * 计算bit数组长度
     * 同样是guava创建布隆过滤器中的计算bit数组长度方法
     */
    private int optimalNumOfBits(long n, double p) {
        if (p == 0) {
            // 设定最小期望长度
            p = Double.MIN_VALUE;
        }
        return (int) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }
 
    /**
     * 这里是从guava 中 copy 出来的
     * 就是guava 创建一个 布隆过滤器中的计算hash方法执行次数的方法
     */
    private int optimalNumOfHashFunctions(long n, long m) {
        int countOfHash = Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
        return countOfHash;
    }

}