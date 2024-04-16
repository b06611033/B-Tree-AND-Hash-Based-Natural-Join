import java.util.*;

public class BplusTree {
    private Node root;
    private int order;
    public BplusTree( Set<Integer> records, int order) {
        this.order = order;
        root = new Node(true);
        for(int record: records) {
            insert(root, record);
        }
    }

    private static Sibling insert(Node curr, int record) {
        // current node is leaf
        int order = 4;
        if(curr.isLeaf) {
            insertToLeaf(curr, record);
            // no overflow
            if(curr.keys.size() <= order) {
                return null;
            }
            else {
                // create new leaf
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
                newNode.pointers = pointers;
                return new Sibling(newNode, firstSecondHalfKey);
            }
        }
        return null;
    }

    private static void insertToLeaf(Node curr, int record) {
        int index = 0;
        List<Integer> data = curr.keys;
        while(index < data.size()) {
            if(data.get(index) == null || data.get(index) > record) break;
            index++;
        }
        data.add(index, record);
    }



    private static Set<Integer> generate(int num, int low) {
        Set<Integer> records = new HashSet<>();
        int curr = low;
        for(int i = 0; i < num; i++) {
            records.add(curr);
            curr += 10;
        }
        return records;
    }

    public static void main (String[] args) {
        Set<Integer> records = generate(10000, 100000);
        int order = 13;
        Node n = new Node(true);
        insert(n, 0);
        System.out.println(n.keys);
        insert(n, 4);
        System.out.println(n.keys);

        Sibling s = insert(n, 3);
        System.out.println(s);
        System.out.println(n.keys);

        s = insert(n, 8);
        System.out.println(s);
        System.out.println(n.keys);

        s = insert(n, 6);
        System.out.println(s);
        System.out.println(n.keys);
        System.out.println(s.n.keys);

        s = insert(n, 2);
        System.out.println(s);
        System.out.println(n.keys);
    }


    static class Node {
        List<Integer> keys;
        List<Node> pointers;
        boolean isLeaf;
        public Node(boolean isLeaf) {
            keys = new LinkedList<>();
            pointers = new LinkedList<>();
            this.isLeaf = isLeaf;
        }
    }

    static class Sibling {
        Node n;
        int key;
        public Sibling(Node n, int key) {
            this.n = n;
            this.key = key;
        }
    }
}