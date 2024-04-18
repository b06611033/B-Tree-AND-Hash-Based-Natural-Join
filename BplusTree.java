import java.util.*;

public class BplusTree {
    private static Node root;
    private int order;
    public BplusTree( List<Integer> records, int order) {
        this.order = order;
        root = new Node(true);
        for(int record: records) {
            insert(root, record, false);
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
        for(int i = 0; i < keys.size(); i++) {
            if(keys.get(i) > record) {
                belowMin = delete(pointers.get(i), record);
                child = pointers.get(i);
                sibling = pointers.get(i+1);
                break;
            }
            if(i == keys.size()-1)  {
                belowMin = delete(pointers.get(i+1), record);
                child = pointers.get(i+1);
            }
        }
        if(!belowMin) return false;
        else if(child != null && sibling.keys.size() > (order+1)/2){
            System.out.println("Moving child sibling key to child");
            System.out.println("Child keys before redistribution: " + child.keys);
            System.out.println("Child sibling keys before redistribution: " + sibling.keys);
            System.out.println("Current node keys before redistribution: " + curr.keys);
        }
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
        System.out.println("-----searching data---------");
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
        System.out.println("-----range searching data---------");
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
    public void printLeaf() {
        System.out.println("-----printing leaf nodes---------");
        Node curr = root;
        while(!curr.isLeaf) curr = curr.pointers.get(0);
        int count = 1;
        while(curr.pointers.size() > 0) {
            System.out.println("current node is: " + curr);
            System.out.println("keys in the node are: " + curr.keys);
            curr = curr.pointers.get(0);
            count++;
        }
        System.out.println("current node is: " + curr);
        System.out.println("keys in the node are: " + curr.keys);
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
        // sparse tree: insert record from small to large
        // dense tree: ??
        //List<Integer> records = new ArrayList<>(generate(10000, 100000));
        List<Integer> records = new ArrayList<>(Arrays.asList(0,2,8,3,4,7,11,9,5,6,1,10,14,12,13));
        BplusTree bt = new BplusTree(records, 4);
        bt.insert(bt.getRoot(), 15, true);
        System.out.println("---------------------------------");
        bt.insert(bt.getRoot(), 199999, true);
        System.out.println("---------------------------------");
        bt.insert(bt.getRoot(), 155001, true);
        System.out.println("---------------------------------");
        bt.printLeaf();
        //System.out.println(bt.search(7));
        //System.out.println(bt.rangeSearch(154999, 156580));
        bt.delete(bt.getRoot(), 5);
        System.out.println("---------------------------------");
        bt.delete(bt.getRoot(), 4);
        System.out.println("---------------------------------");
        bt.delete(bt.getRoot(), 1);
        System.out.println("---------------------------------");
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