import java.util.*;

public class BplusTree {
    private static Node root;
    private int order;
    public BplusTree( List<Integer> records, int order, boolean dense, boolean debug) {
        this.order = order;
        Collections.sort(records);
        if(!dense) {
            root = new Node(true);
            for(int record: records) {
                insert(root, record, false);
            }
            return;
        }
        //------------------Dense Tree----------------------------
        List<Node> currNode = new ArrayList<>(); // store nodes created at the current level
        List<Node> prevNode = new ArrayList<>(); // store nodes of previous level
        List<Integer> prevMin = new ArrayList<>(); // store minimum value in each previous level nodes subtree
        List<Integer> currMin = new ArrayList<>(); // store minimum value in each current level nodes subtree
        int size = records.size();
        int num = order;
        while(true) {
        // Calculating number of full nodes & non-full nodes for current level
            System.out.println("------------new level--------");
            if(prevNode.size() != 0) {
                size = prevNode.size();
                num = order+1;
            }
            int fullNodeNum = size/num;
            int halfNodeNum = 0;
            int remainder = size%num;
            if(size < num) {
                halfNodeNum = 1;
            }
            else if(prevNode.size()==0) {
                int minKey = (order+1)/2;
                if(remainder != 0 && remainder < minKey) {
                    fullNodeNum--;
                    halfNodeNum = 2;
                }
                else if(remainder != 0) halfNodeNum++;
            }
            else {
                int minPointers = (order+1)/2;
                if(order%2 == 0) minPointers++;
                if(remainder != 0 && remainder < minPointers) {
                    fullNodeNum--;
                    halfNodeNum = 2;
                }
                else if(remainder != 0) halfNodeNum++;
            }
            if(debug) {
                System.out.println("remainder: " + remainder);
                System.out.println("full node number: " + fullNodeNum);
                System.out.println("half node number: " + halfNodeNum);
            }
        //-------------Creating nodes for current level-------------------
            int count = 0;
            if(prevNode.size()==0) {
                Node next = new Node(true);
                for(int i = 0; i < fullNodeNum; i++) {
                    Node curr = next;
                    for(int j = 0; j < order; j++) {
                        curr.keys.add(records.get(count));
                        count++;
                    }
                    next = new Node(true);
                    if(remainder > 0 || i < fullNodeNum-1) curr.pointers.add(next);
                    currNode.add(curr);
                    currMin.add(curr.keys.get(0));
                }
                if(halfNodeNum == 1) {
                    Node curr = next;
                    for(int j = 0; j < remainder; j++) {
                        curr.keys.add(records.get(count));
                        count++;
                    }
                    currNode.add(curr);
                    currMin.add(curr.keys.get(0));
                }
                else if(halfNodeNum == 2) {
                    int firstNodeSize = (order+remainder)/2;
                    int secondNodeSize = firstNodeSize + (order+remainder)%2;
                    Node curr = next;
                    for(int j = 0; j < firstNodeSize; j++) {
                        curr.keys.add(records.get(count));
                        count++;
                    }
                    next = new Node(true);
                    curr.pointers.add(next);
                    currNode.add(curr);
                    currMin.add(curr.keys.get(0));
                    curr = next;
                    for(int j = 0; j < secondNodeSize; j++) {
                        curr.keys.add(records.get(count));
                        count++;
                    }
                    currNode.add(curr);
                    currMin.add(curr.keys.get(0));
                }
            }
            else {
                for(int i = 0; i < fullNodeNum; i++) {
                    int min = Integer.MAX_VALUE;
                    Node curr = new Node(false);
                    for(int j = 0; j < num; j++) {
                        curr.pointers.add(prevNode.get(count));
                        min = Math.min(min, prevMin.get(count));
                        if(j < num-1) {
                            curr.keys.add(prevMin.get(count+1));
                        }
                        count++;
                    }
                    currMin.add(min);
                    currNode.add(curr);
                }
                if(halfNodeNum == 1) {
                    int min = Integer.MAX_VALUE;
                    Node curr = new Node(false);
                    for(int j = 0; j < remainder; j++) {
                        curr.pointers.add(prevNode.get(count));
                        min = Math.min(min, prevMin.get(count));
                        if(j < remainder-1) {
                            curr.keys.add(prevMin.get(count+1));
                        }
                        count++;
                    }
                    currMin.add(min);
                    currNode.add(curr);
                }
                else if(halfNodeNum == 2) {
                    int firstNodeSize = (num+remainder)/2;
                    int secondNodeSize = firstNodeSize + (num+remainder)%2;
                    int min = Integer.MAX_VALUE;
                    Node curr = new Node(false);
                    for(int j = 0; j < firstNodeSize; j++) {
                        curr.pointers.add(prevNode.get(count));
                        min = Math.min(min, prevMin.get(count));
                        if(j < firstNodeSize-1) {
                            curr.keys.add(prevMin.get(count+1));
                        }
                        count++;
                    }
                    currMin.add(min);
                    currNode.add(curr);
                    min = Integer.MAX_VALUE;
                    curr = new Node(false);
                    for(int j = 0; j < secondNodeSize; j++) {
                        curr.pointers.add(prevNode.get(count));
                        min = Math.min(min, prevMin.get(count));
                        if(j < secondNodeSize-1) {
                            curr.keys.add(prevMin.get(count+1));
                        }
                        count++;
                    }
                    currMin.add(min);
                    currNode.add(curr);
                }
            }
        //--------------Check if the current level is the root------------------
            if(debug) {
                for(Node n: currNode) {
                    System.out.println("node: " + n);
                    System.out.println("node keys: " + n.keys);
                    System.out.println("node pointers: " + n.pointers);
                }
                for(int m: currMin) {
                    System.out.println("min: " + m);
                }
            }
            if(currNode.size()==1) {
                root = currNode.get(0);
                break;
            }
            prevNode = currNode;
            prevMin = currMin;
            currNode = new ArrayList<>();
            currMin = new ArrayList<>();
        }
    }

    // ----------------------------------------------------- INSERT -------------------------------------------------------------------------
    private Sibling insert(Node curr, int record, boolean debug) {
        // current node is leaf
        if(curr.isLeaf) {
            insertToLeaf(curr, record, debug);
            // no overflow
            if(curr.keys.size() <= order) {
                if(debug) {
                    System.out.println("no overflow in leaf");
                    System.out.println("current leaf node keys: " + curr.keys);
                }
                return null;
            }
            else {
                // create new leaf
                if(debug) {
                    System.out.println("splitting leaf node due to overflow");
                    System.out.println("leaf node before splitting: " + curr.keys);
                }
                Node newNode = new Node(true);
                int index = (order+1)/2;
                List<Integer> newKeys = new LinkedList<>();
                List<Integer> keys = curr.keys;
                int keyForParent = keys.get(index);
                while(keys.size() > index) {
                    newKeys.add(keys.get(index));
                    keys.remove(index);
                }
                List<Node> newPointers = new LinkedList<>();
                List<Node> pointers = curr.pointers;
                // leaf node only has at most 1 pointer pointing to the right leaf 
                if(pointers.size() > 0) {
                    newPointers.add(pointers.get(0));
                    pointers.remove(0); 
                }
                pointers.add(newNode); // points to splitted node at right
                newNode.keys = newKeys;
                newNode.pointers = newPointers;
                if(debug) {
                    System.out.println("leaf node after splitting: " + curr);
                    System.out.println("sibling leaf node after splitting: " + newNode);
                    System.out.println("leaf node keys after splitting: " + curr.keys);
                    System.out.println("sibling leaf node keys after splitting: " + newNode.keys);
                    System.out.println("leaf node points to after splitting: " + curr.pointers.get(0));
                    System.out.println("sibling leaf node points to after splitting: " + newNode.pointers);
                }
                if(curr == root) createRoot(curr, newNode, keyForParent, debug);
                return new Sibling(newNode, keyForParent);
            }
        }
        // node is not a leaf
        if(debug) {
            System.out.println("current non-leaf node: " + curr);
            System.out.println("current non-leaf node keys: " + curr.keys);
        }
        List<Integer> keys = curr.keys;
        List< Node> pointers = curr.pointers;
        Sibling newChild = null;
        for(int i = 0; i < keys.size(); i++) {
            if(keys.get(i) > record) {
                newChild = insert(pointers.get(i), record, debug);
                break;
            }
            if(i == keys.size()-1)  newChild = insert(pointers.get(i+1), record, debug);
        }
        if (newChild == null) return null;
        else if(keys.size() < order) {
            insertToNonLeaf(curr, newChild, debug);
            return null;
        }
        else {
            // create new non-leaf node
            insertToNonLeaf(curr, newChild, debug);
            if(debug) {
                System.out.println("splitting non-leaf node due to overflow");
                System.out.println("non-leaf node before splitting: " + curr);
                System.out.println("non-leaf node keys before splittinh: " + curr.keys);
            }
            Node newNode = new Node(false);
            int middleKeyIndex = (order+1)/2-1;
            if((order+1)%2 == 1) middleKeyIndex++;
            int keyForParent = keys.get(middleKeyIndex);
            List<Integer> newKeys = new LinkedList<>();
            int index = middleKeyIndex;
            // add all the second half keys to the new node (except the middle key)
            while(keys.size() > index) {
                if(keys.get(index) != keyForParent) newKeys.add(keys.get(index));
                keys.remove(index);
            }
            List<Node> newPointers = new LinkedList<>();
            index = middleKeyIndex+1;
            while(pointers.size() > index) {
                newPointers.add(pointers.get(index));
                pointers.remove(index); 
            }
            newNode.keys = newKeys;
            newNode.pointers = newPointers;
            if(debug) {
                System.out.println("non-leaf node after splitting: " + curr);
                System.out.println("sibling non-leaf node after splitting: " + newNode);
                System.out.println("non-leaf node keys after splitting: " + curr.keys);
                System.out.println("sibling non-leaf node keys after splitting: " + newNode.keys);
                System.out.println("non-leaf node points to after splitting: " + curr.pointers);
                System.out.println("sibling non-leaf node points to after splitting: " + newNode.pointers);
            }
            if(curr == root) createRoot(curr, newNode, keyForParent, debug);
            return new Sibling(newNode, keyForParent);
        }
    }

    private void insertToNonLeaf(Node curr, Sibling s, boolean debug) {
        if(debug) {
            System.out.println("inserting to non-leaf");
            System.out.println("non-leaf node keys before insertion: " + curr.keys);
        }
        int index = 0;
        List<Integer> keys = curr.keys;
        while(index < keys.size()) {
            if(keys.get(index) == null || keys.get(index) > s.key) break;
            index++;
        }
        keys.add(index, s.key);
        List<Node> pointers = curr.pointers;
        pointers.add(index+1, s.n);
        if(debug) {
            System.out.println("non-leaf node keys after insertion: " + curr.keys);
            System.out.println("non-leaf node pointers after insertion: " + curr.pointers);
        }
    }

    private void insertToLeaf(Node curr, int record, boolean debug) {
        if(debug) {
            System.out.println("inserting to leaf");
            System.out.println("leaf node keys before insertion: " + curr.keys);
        }
        int index = 0;
        List<Integer> keys = curr.keys;
        while(index < keys.size()) {
            if(keys.get(index) == null || keys.get(index) > record) break;
            index++;
        }
        keys.add(index, record);
        if(debug) {
            System.out.println("leaf node keys after insertion: " + curr.keys);
            System.out.println("leaf node pointers after insertion: " + curr.pointers);
        }
    }


    private void createRoot(Node curr, Node newNode, int firstSecondHalfKey, boolean debug) {
        if(debug) System.out.println("Creating new root");
        Node newRoot = new Node(false);
        newRoot.keys.add(firstSecondHalfKey);
        newRoot.pointers.add(curr);
        newRoot.pointers.add(newNode);
        root = newRoot;
        if(debug) {
            System.out.println("new root: " + root);
            System.out.println("root node keys: " + root.keys);
            System.out.println("root node points to children 1: " + root.pointers.get(0));
            System.out.println("root node points to children 2: " + root.pointers.get(1));
        }
    }

    // ----------------------------------------------------- DELETE -------------------------------------------------------------------------

    private boolean delete(Node curr, int record) {
        if(curr.isLeaf) {
            deleteAtLeaf(curr, record);
            if(curr == root || curr.keys.size() >= (order+1)/2) {
                System.out.println("leaf half full OR leaf is root, NO PROBLEM!");
                return false;
            }
            else {
                System.out.println("leaf less than half full, NEED REDISTRIBUTION!");
                return true;
            }
        }
        // node is not a leaf
        System.out.println("current non-leaf node: " + curr);
        System.out.println("current non-leaf node keys: " + curr.keys);
        List<Integer> keys = curr.keys;
        List<Node> pointers = curr.pointers;
        boolean belowMin = false;
        Node child = null;
        Node sibling = null;
        int currKeyIndex = -1;
        for(int i = 0; i < keys.size(); i++) {
            if(keys.get(i) > record) {
                belowMin = delete(pointers.get(i), record);
                child = pointers.get(i);
                sibling = pointers.get(i+1);
                currKeyIndex = i;
                break;
            }
            if(i == keys.size()-1)  {
                belowMin = delete(pointers.get(i+1), record);
                child = pointers.get(i+1);
            }
        }
        if(!belowMin) return false;
        else if(sibling != null && sibling.keys.size() > (order+1)/2){
            System.out.println("Moving child sibling key to child");
            System.out.println("Child keys before redistribution: " + child.keys);
            System.out.println("Child sibling keys before redistribution: " + sibling.keys);
            System.out.println("Current node keys before redistribution: " + curr.keys);
            int firstSiblingKey = sibling.keys.get(0);
            if(child.isLeaf) {
                System.out.println("Redistributing at leaves");
                child.keys.add(firstSiblingKey);
                sibling.keys.remove(0);
                curr.keys.set(currKeyIndex, sibling.keys.get(0));
            }
            else {
                System.out.println("Redistributing at non-leaves");
                int currKey = curr.keys.get(currKeyIndex);
                curr.keys.set(currKeyIndex, firstSiblingKey);
                sibling.keys.remove(0);
                Node firstSiblingPointer = sibling.pointers.get(0);
                child.pointers.add(firstSiblingPointer);
                sibling.pointers.remove(0);
                child.keys.add(currKey);
            }
            System.out.println("Child keys after redistribution: " + child.keys);
            System.out.println("Child sibling keys after redistribution: " + sibling.keys);
            System.out.println("Current node keys after redistribution: " + curr.keys);
            return false;
        }
        else System.out.println("needs coalescing, but generally not implemented");
        return false;
    }

    private void deleteAtLeaf(Node curr, int record) {
        System.out.println("deleting key at leaf node: " + curr);
        System.out.println("keys before deleting at leaf node: " + curr.keys);
        int index = 0;
        List<Integer> keys = curr.keys;
        while(index < keys.size()) {
            if(keys.get(index) == record) {
            // don't need to handle pointers because leaf node keys are directly data
                keys.remove(index);
                break;
            }
            index++;
        }
        System.out.println("keys after deleting at leaf node: " + curr.keys);
    }

    // ----------------------------------------------------- SEARCH & RANGE SEARCH -------------------------------------------------------------------------
    private String search(int key) {
        Node curr = root;
        while(!curr.isLeaf) {
            System.out.println("current non-leaf node is: " + curr);
            System.out.println("keys of current non-leaf node are: " + curr.keys);
            List<Integer> keys = curr.keys;
            List<Node> pointers = curr.pointers;
            boolean found = false;
            for(int i = 0; i < keys.size(); i++) {
                if(key < keys.get(i)) {
                    curr = pointers.get(i);
                    found = true;
                    break;
                }
            }
            if(!found) curr = pointers.get(pointers.size()-1);
        }
        System.out.println("current leaf node is: " + curr);
        System.out.println("keys in leaf node are: " + curr.keys);
        for(int k: curr.keys) {
            if(k == key) {
                return ("data is: " + k + ", found in node: " + curr);
            }
        }
        return "Data not found!!!";
    }

    private List<Integer> rangeSearch(int low, int high) {
        Node curr = root;
        while(!curr.isLeaf) {
            System.out.println("current non-leaf node is: " + curr);
            System.out.println("keys of current non-leaf node are: " + curr.keys);
            List<Integer> keys = curr.keys;
            List<Node> pointers = curr.pointers;
            boolean found = false;
            for(int i = 0; i < keys.size(); i++) {
                if(low < keys.get(i)) {
                    curr = pointers.get(i);
                    found = true;
                    break;
                }
            }
            if(!found) curr = pointers.get(pointers.size()-1);
        }
        List<Integer> data = new LinkedList<>();
        while(curr != null) {
            System.out.println("current leaf node is: " + curr);
            System.out.println("keys in leaf node are: " + curr.keys);
            for(int k: curr.keys) {
                if(k >= low && k <= high) {
                    data.add(k);
                }
                else if(k > high) {
                    return data;
                }
            }
            if(curr.pointers.size() == 0) return data;
            else curr = curr.pointers.get(0);
        }
        return data;
    }

    // ----------------------------------------------------- HELPER FUNCTIONS -------------------------------------------------------------------------
    public void printLeaf(boolean debug) {
        System.out.println("-----printing leaf node information---------");
        Node curr = root;
        while(!curr.isLeaf) curr = curr.pointers.get(0);
        int count = 1;
        while(curr.pointers.size() > 0) {
            if(debug) {
                System.out.println("current node is: " + curr);
                System.out.println("keys in the node are: " + curr.keys);
            }
            curr = curr.pointers.get(0);
            count++;
        }
        if(debug) {
            System.out.println("current node is: " + curr);
            System.out.println("keys in the node are: " + curr.keys);
        }
        System.out.println("total number of leaves: " + count);
        System.out.println("-----finished printing---------");
    }

    public Node getRoot() {
        return root;
    }

     //GENERATE RECORDS
     private static Set<Integer> generate(int num, int low) {
        Set<Integer> records = new HashSet<>();
        int curr = low;
        for(int i = 0; i < num; i++) {
            records.add(curr);
            curr += 10;
        }
        return records;
    }

    // ----------------------------------------------------- MAIN FUNCTION -------------------------------------------------------------------------
    public static void main (String[] args) {
        // (a)
        List<Integer> records = new ArrayList<>(generate(10000, 100000));
        Set<Integer> keys = new HashSet<>(records);
        // (b)
        BplusTree bt1 = new BplusTree(records, 13, false, false);
        bt1.printLeaf(false);
        BplusTree bt2 = new BplusTree(records, 13, true, false);
        bt2.printLeaf(false);
        BplusTree bt3 = new BplusTree(records, 24, false, false);
        bt3.printLeaf(false);
        BplusTree bt4 = new BplusTree(records, 24, true, false);
        bt4.printLeaf(false);
        // (c1)
        Random rand = new Random();
        for(int i = 0; i < 2; i++) {
            System.out.println("-------------------------INSERT--------------------------");
            int random = rand.nextInt(100000)+100000;
            while(keys.contains(random)) random = rand.nextInt(100000)+100000;
            System.out.println("inserting: " + random);
            bt1.insert(bt1.getRoot(), random, true);
            keys.add(random);
        }
        // (c2)
        for(int i = 0; i < 2; i++) {
            System.out.println("-------------------------DELETE--------------------------");
            int random = rand.nextInt(100000)+100000;
            while(!keys.contains(random)) random = rand.nextInt(100000)+100000;
            System.out.println("deleting: " + random);
            bt1.delete(bt1.getRoot(), random);
            keys.remove(random);
        }
        // (c3)
        for(int i = 0; i < 2; i++) {
            System.out.println("-------------------------INSERT--------------------------");
            int random = rand.nextInt(100000)+100000;
            while(keys.contains(random)) random = rand.nextInt(100000)+100000;
            System.out.println("inserting: " + random);
            bt1.insert(bt1.getRoot(), random, true);
            keys.add(random);
        }
        for(int i = 0; i < 3; i++) {
            System.out.println("-------------------------DELETE--------------------------");
            int random = rand.nextInt(100000)+100000;
            while(!keys.contains(random)) random = rand.nextInt(100000)+100000;
            System.out.println("deleting: " + random);
            bt1.delete(bt1.getRoot(), random);
            keys.remove(random);
        }
        // (c4)
        for(int i = 0; i < 3; i++) {
            System.out.println("-------------------------SEARCH--------------------------");
            int random = rand.nextInt(100000)+100000;
            while(!keys.contains(random)) random = rand.nextInt(100000)+100000;
            System.out.println("searching: " + random);
            System.out.println(bt1.search(random));
        }
        // for(int i = 0; i < 2; i++) {
        //     System.out.println("-------------------------RANGE SEARCH--------------------------");
        //     int random1 = rand.nextInt(100000)+100000;
        //     int random2 = rand.nextInt(100000)+100000;
        //     while(random1 == random2) random2 = rand.nextInt(100000)+100000;
        //     int low = Math.min(random1, random2);
        //     int high = Math.max(random1, random2);
        //     System.out.println("range searching from " + low + " to " + high);
        //     System.out.println(bt1.rangeSearch(low, high));
        // }
        // bt.printLeaf(false);
        // System.out.println("---------------------------------------------------------------------------------------");
        // System.out.println(bt.search(104500));
        // System.out.println("---------------------------------------------------------------------------------------");
        // bt.insert(bt.getRoot(), 15, true);
        // System.out.println("---------------------------------------------------------------------------------------");
        // bt.insert(bt.getRoot(), 199999, true);
        // System.out.println("---------------------------------------------------------------------------------------");
        // bt.insert(bt.getRoot(), 155001, true);
        // System.out.println("---------------------------------------------------------------------------------------");
        // System.out.println(bt.rangeSearch(154999, 156580));
        // System.out.println("---------------------------------------------------------------------------------------");
        // bt.delete(bt.getRoot(), 100010);
        // System.out.println("---------------------------------------------------------------------------------------");
        // bt.delete(bt.getRoot(), 145000);
        // System.out.println("---------------------------------------------------------------------------------------");
        // bt.delete(bt.getRoot(), 178000);
        // System.out.println("---------------------------------------------------------------------------------------");
        // bt.delete(bt.getRoot(), 199999);
        // System.out.println("---------------------------------------------------------------------------------------");       
    }

    // ----------------------------------------------------- CLASSES -------------------------------------------------------------------------
    class Node {
        List<Integer> keys;
        List<Node> pointers;
        boolean isLeaf;
        public Node(boolean isLeaf) {
            keys = new LinkedList<>();
            pointers = new LinkedList<>();
            this.isLeaf = isLeaf;
        }
    }

    class Sibling {
        Node n;
        int key;
        public Sibling(Node n, int key) {
            this.n = n;
            this.key = key;
        }
    }

    // ----------------------------------------------------- TESTS -------------------------------------------------------------------------

}