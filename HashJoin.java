import java.util.*;

public class HashJoin {
    private Block[] memory;
    private int[][] S;
    private int[][] R;
    private Map<Integer, List<int[]>> sMap;
    private Map<Integer, List<int[]>> rMap;
    private Set<Integer> sKeys;
    private int IOs;
    private List<List<Integer>> joinedTable;

    public HashJoin(int rSize) {
        memory = new Block[15];
        for(int i = 0; i < 15; i++) memory[i] = new Block();
        S = new int[5000][2];
        R = new int[rSize][2];
        sMap = new HashMap<>();
        rMap = new HashMap<>();
        sKeys = new HashSet<>();
        IOs = 0;
        joinedTable = new ArrayList<>();
    }

    public void read(Block block, int blockNum) {
        IOs++;
        memory[blockNum] = block;
    }

    // return the bucket/block number if it is full
    public int hashInMemory(int[] tuple, int bIndex) {
        int hashKey = tuple[bIndex]%14;
        Block bucket = memory[hashKey];
        bucket.arr[bucket.currSize] = tuple;
        bucket.currSize++;
        if(bucket.currSize == 8) return hashKey;
        else return -1;
    }

    public void hashInDisk(Block b, Map<Integer, List<int[]>> map, int key, int bIndex) {
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
                R[i][0] = random-5000;
                R[i][1] = random;
            }
        }
        else {
            for(int i = 0; i < R.length; i++) {
                int random = rand.nextInt(40000)+10000;
                while(!sKeys.contains(random)) random = rand.nextInt(40000)+10000;
                R[i][0] = random-5000;
                R[i][1] = random;
            }
        }
    }

    public void clear() {
        for(int i = 0; i < 15; i++) memory[i] = new Block();
    }

    public void join() {
        int[][] sTuples = memory[14].arr;
        for(int[] sTuple: sTuples) {
            int B1 = sTuple[0];
            int C = sTuple[1];
            boolean reachedEnd = false;
            if(B1 == 0) break; // key will not be 0
            for(int i = 0; i <= 13; i++) {
                int[][] rTuples = memory[i].arr;
                for(int[] rTuple: rTuples) {
                    int A = rTuple[0];
                    int B2 = rTuple[1];
                    if(B2 == 0) {
                        reachedEnd = true;
                        break;
                    }
                    if(B1 == B2) {
                        // System.out.println("Natural Joining");
                        // System.out.println("Tuple from S: " + "[" + B1 + ", " + C + "] ");
                        // System.out.println("Tuple from R: " + "[" + A + ", " + B2 + "] ");
                        // System.out.println("Tuple in joined table: " + "[" + A + ", " + B2 + ", " + C + "] ");
                        joinedTable.add(new ArrayList<>(Arrays.asList(A, B1, C)));
                    }
                }
                if(reachedEnd) break;
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
        hashToBucket(hj, relationS, sMap, 0, memory);
        //----send relation R to memory => hash => write back to disk----
        hj.generateR();
        int[][] relationR = hj.R;
        Map<Integer, List<int[]>> rMap = hj.rMap;
        hashToBucket(hj, relationR, rMap, 1, memory);
        //----Natural Join----
        for(int i = 0; i <= 13; i++) {
            hj.clear();
            List<int[]> rBucket = rMap.get(i);
            int idx = 0;
            int blockNum = 0;
            while(idx < rBucket.size()) {
                Block toMemoryBlock = new Block();
                for(int j = 0; j < 8; j++) {
                    toMemoryBlock.arr[j] = rBucket.get(idx);
                    idx++;
                    toMemoryBlock.currSize++;
                    if(idx == rBucket.size()) break;
                }
                hj.read(toMemoryBlock, blockNum);
                blockNum++;
            }
            List<int[]> sBucket = sMap.get(i);
            idx = 0;
            while(idx < sBucket.size()) {
                Block toMemoryBlock = new Block();
                for(int j = 0; j < 8; j++) {
                    toMemoryBlock.arr[j] = sBucket.get(idx);
                    idx++;
                    toMemoryBlock.currSize++;
                    if(idx == sBucket.size()) break;
                }
                hj.read(toMemoryBlock, 14);
                hj.join();
            }
        }
        List<List<Integer>> joinedTable = hj.joinedTable;
        System.out.println("Joined table has size of: " + joinedTable.size());
        Set<Integer> randomPickedBValues = new HashSet<>();
        Random rand = new Random();
        for(int i = 0; i < 20; i++) {
            int random = rand.nextInt(relationR.length);
            while(randomPickedBValues.contains(relationR[random][1])) random = rand.nextInt(relationR.length);
            randomPickedBValues.add(relationR[random][1]);
        }
        System.out.println("Tuples with randomly picked B values in joined table");
        for(List<Integer> tuple: joinedTable) {
            int B = tuple.get(1);
            if(randomPickedBValues.contains(B))System.out.println(tuple);
        }
        System.out.println("total number of IOs: " + hj.IOs);
    }

    public static void hashToBucket(HashJoin hj, int[][] relation, Map<Integer, List<int[]>> map, int bIndex, Block[] memory) {
        int idx = 0;
        while(idx < relation.length) {
            // send block to memory
            Block toMemoryBlock = new Block();
            for(int i = 0; i < 8; i++) {
                toMemoryBlock.arr[i] = relation[idx];
                idx++;
            }
            toMemoryBlock.currSize = 8;
            hj.read(toMemoryBlock, 14);
            // hash the tuples of the input block to buckets
            for(int[] tuple: memory[14].arr) {
                int blockNum = hj.hashInMemory(tuple, bIndex);
                // if bucket block in memory is full, write back to disk
                if(blockNum != -1) {
                    Block fromMemoryBlock = hj.write(blockNum);
                    hj.hashInDisk(fromMemoryBlock, map, blockNum, bIndex);
                }
            }
        }
        for(int i = 0; i < 14; i++) {
            if(memory[i].currSize > 0) {
                Block fromMemoryBlock = hj.write(i);
                hj.hashInDisk(fromMemoryBlock, map, i, 0);
            }
        }
        int size = 0;
        for(int key: map.keySet()) {
            List<int[]> list = map.get(key);
            System.out.println("key: " + key + " size: " + list.size());
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
