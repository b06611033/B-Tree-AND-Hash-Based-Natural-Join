import java.util.*;

public class HashJoin {
    private Block[] memory;
    private int[][] S;
    private int[][] R;
    private Map<Integer, List<int[]>> sMap;
    private Map<Integer, List<int[]>> rMap;
    private Set<Integer> sKeys;
    private Set<Integer> rKeys;
    private int IOs;

    public HashJoin(int rSize) {
        memory = new Block[15];
        for(int i = 0; i < 15; i++) memory[i] = new Block();
        S = new int[5000][2];
        R = new int[rSize][2];
        sMap = new HashMap<>();
        rMap = new HashMap<>();
        sKeys = new HashSet<>();
        rKeys = new HashSet<>();
        IOs = 0;
    }

    public void read(Block block) {
        IOs++;
        memory[14] = block;
    }

    // return the bucket/block number if it is full
    public int hashMemory(int[] tuple, int bIndex) {
        int hashKey = tuple[bIndex]%14;
        Block bucket = memory[hashKey];
        bucket.arr[bucket.currSize] = tuple;
        bucket.currSize++;
        if(bucket.currSize == 8) return hashKey;
        else return -1;
    }

    public void hashDisk(Block b, Map<Integer, List<int[]>> map, int key, int bIndex) {
        if(!map.containsKey(key)) map.put(key, new ArrayList<>());
        int[][] tuples = b.arr;
        for(int[] tuple: tuples) {
            if(tuple[bIndex] == 0) continue;
            map.get(key).add(tuple);
        }
    }

    public Block write(int blockNum) {
        IOs++;
        Block b = memory[blockNum];
        memory[blockNum] = new Block(); // clear old bucket block
        return b;
    }

    public void generateS() {
        Random rand = new Random();
        for(int i = 0; i < S.length; i++) {
            int random = rand.nextInt(40000)+10000;
            while(sKeys.contains(random)) random = rand.nextInt(40000)+10000;
            sKeys.add(random);
            S[i][0] = random;
            S[i][1] = random-10000;
        }
    }

    public void generateR() {
        Random rand = new Random();
        if(R.length == 1200) {
            for(int i = 0; i < R.length; i++) {
                int random = rand.nextInt(40000)+10000;
                while(rKeys.contains(random)) random = rand.nextInt(10000)+20000;
                rKeys.add(random);
                R[i][0] = random-10000;
                R[i][1] = random;
            }
        }
        else {
            for(int i = 0; i < R.length; i++) {
                int random = rand.nextInt(40000)+10000;
                while(rKeys.contains(random) || !sKeys.contains(random)) random = rand.nextInt(40000)+10000;
                rKeys.add(random);
                R[i][0] = random-10000;
                R[i][1] = random;
            }
        }
    }

    public static void main (String[] args) { 
        HashJoin hj = new HashJoin(1000);
        Block[] memory = hj.memory;
        //----send relation S to memory => hash => write back to disk----
        int[][] relationS = hj.S;
        hj.generateS();
        Map<Integer, List<int[]>> sMap = hj.sMap;
        int idx = 0;
        while(idx < relationS.length) {
            // send block to memory
            Block toMemoryBlock = new Block();
            for(int i = 0; i < 8; i++) {
                toMemoryBlock.arr[i] = relationS[idx];
                idx++;
            }
            toMemoryBlock.currSize = 8;
            hj.read(toMemoryBlock);
            // hash the tuples of the input block to buckets
            for(int[] tuple: memory[14].arr) {
                int blockNum = hj.hashMemory(tuple, 0);
                // if bucket block in memory is full, write back to disk
                if(blockNum != -1) {
                    Block fromMemoryBlock = hj.write(blockNum);
                    hj.hashDisk(fromMemoryBlock, sMap, blockNum, 0);
                }
            }
        }
        for(int i = 0; i < 14; i++) {
            if(memory[i].currSize > 0) {
                Block fromMemoryBlock = hj.write(i);
                hj.hashDisk(fromMemoryBlock, sMap, i, 0);
            }
        }
        int size = 0;
        for(int key: sMap.keySet()) {
            System.out.println("key: " + key);
            List<int[]> list = sMap.get(key);
            System.out.println("size: " + list.size());
            size += list.size();
        }
        System.out.println("size: " + size);
        System.out.println("number of IOs: " + hj.IOs);
        //----send relation R to memory => hash => write back to disk----
        hj.generateR();
        int[][] relationR = hj.R;
        Map<Integer, List<int[]>> rMap = hj.rMap;
        idx = 0;
        while(idx < relationR.length) {
            // send block to memory
            Block toMemoryBlock = new Block();
            for(int i = 0; i < 8; i++) {
                toMemoryBlock.arr[i] = relationR[idx];
                idx++;
            }
            toMemoryBlock.currSize = 8;
            hj.read(toMemoryBlock);
            // hash the tuples of the input block to buckets
            for(int[] tuple: memory[14].arr) {
                int blockNum = hj.hashMemory(tuple, 1);
                // if bucket block in memory is full, write back to disk
                if(blockNum != -1) {
                    Block fromMemoryBlock = hj.write(blockNum);
                    hj.hashDisk(fromMemoryBlock, rMap, blockNum, 1);
                }
            }
        }
        for(int i = 0; i < 14; i++) {
            if(memory[i].currSize > 0) {
                Block fromMemoryBlock = hj.write(i);
                hj.hashDisk(fromMemoryBlock, rMap, i, 1);
            }
        }
        size = 0;
        for(int key: rMap.keySet()) {
            System.out.println("key: " + key);
            List<int[]> list = rMap.get(key);
            System.out.println("size: " + list.size());
            size += list.size();
        }
        System.out.println("size: " + size);
        System.out.println("number of IOs: " + hj.IOs);

    }

    public static void print(int[][] arr) {
        for(int[] tuple: arr) {
            System.out.print("[" + tuple[0] + ", " + tuple[1] + "] ");
        }
    }

    static class Block {
        int[][] arr;
        int currSize;
        public Block(){
            arr = new int[8][2];
            currSize = 0;
        }
    }
}
