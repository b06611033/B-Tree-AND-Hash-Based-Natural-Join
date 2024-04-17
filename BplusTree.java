import java.util.*;

public class BplusTree {
    private static Node root;
    private int order;
    public BplusTree( Set<Integer> records, int order) {
        this.order = order;
        root = new Node(true);
        for(int record: records) {
            System.out.println("inserting: " + record);
            insert(root, record);
            System.out.println("--------------------------------------");
        }
    }

    private Sibling insert(Node curr, int record) {
        // current node is leaf
        if(curr.isLeaf) {
            insertToLeaf(curr, record);
            // no overflow
            if(curr.keys.size() <= order) {
                System.out.println("no overflow in leaf");
                System.out.println("current leaf node keys: " + curr.keys);
                return null;
            }
            else {
                // create new leaf
                System.out.println("splitting due to overflow");
                Node newNode = new Node(true);
                int index = (order+1)/2;
                List<Integer> newKeys = new LinkedList<>();
                List<Integer> keys = curr.keys;
                int firstSecondHalfKey = keys.get(index);
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
                System.out.println("current leaf node: " + curr);
                System.out.println("sibling leaf node: " + newNode);
                System.out.println("current leaf node keys: " + curr.keys);
                System.out.println("sibling leaf node keys: " + newNode.keys);
                System.out.println("current leaf node points to : " + curr.pointers.get(0));
                System.out.println("sibling leaf node points to: " + newNode.pointers);

                if(curr == root) createRoot(curr, newNode, firstSecondHalfKey);
                return new Sibling(newNode, firstSecondHalfKey);
            }
        }
        // node is not a leaf
        List<Integer> keys = curr.keys;
        List< Node> pointers = curr.pointers;
        Sibling newChild = null;
        for(int i = 0; i < keys.size(); i++) {
            if(keys.get(i) > record) {
                newChild = insert(pointers.get(i), record);
                break;
            }
            if(i == keys.size()-1)  newChild = insert(pointers.get(i+1), record);
        }
        if (newChild == null) return null;
        else if(keys.size() < order) {
            insertToNonLeaf(curr, newChild);
            return null;
        }
        return null;
    }

    private void insertToNonLeaf(Node curr, Sibling s) {
        System.out.println("inserting to non-leaf");
        int index = 0;
        List<Integer> keys = curr.keys;
        while(index < keys.size()) {
            if(keys.get(index) == null || keys.get(index) > s.key) break;
            index++;
        }
        keys.add(index, s.key);
        List<Node> pointers = curr.pointers;
        pointers.add(index+1, s.n);
        System.out.println("current non-leaf node keys: " + curr.keys);
        System.out.println("current non-leaf node pointers: " + curr.pointers);
    }

    private void insertToLeaf(Node curr, int record) {
        int index = 0;
        List<Integer> keys = curr.keys;
        while(index < keys.size()) {
            if(keys.get(index) == null || keys.get(index) > record) break;
            index++;
        }
        keys.add(index, record);
    }

    private void createRoot(Node curr, Node newNode, int firstSecondHalfKey) {
        System.out.println("Creating new root");
        Node newRoot = new Node(false);
        newRoot.keys.add(firstSecondHalfKey);
        newRoot.pointers.add(curr);
        newRoot.pointers.add(newNode);
        root = newRoot;
        System.out.println("new root: " + root);
        System.out.println("root node keys: " + root.keys);
        System.out.println("root node points to children 1: " + root.pointers.get(0));
        System.out.println("root node points to children 2: " + root.pointers.get(1));
    }


    //MAIN FUNCTION
    public static void main (String[] args) {
        //Set<Integer> records = generate(10000, 100000);
        //int order = 13;
        Set<Integer> records = new HashSet<>(Arrays.asList(0,1,2,3,4,5,6,7,8,9,10,11));
        BplusTree bt = new BplusTree(records, 4);
        // bt.insert(root, 2);
        // bt.insert(root, 5);
        // bt.insert(root, 1);
        // bt.insert(root, 7);
        // bt.insert(root, 6);
        // bt.insert(root, 9);
        // bt.insert(root, 4);
        // bt.insert(root, 3);
        // bt.insert(root, 10);
    }

    //CLASSES
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

}