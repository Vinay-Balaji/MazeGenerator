import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javalib.impworld.*;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.RectangleImage;
import tester.Tester;

// IMPORTANT!!!
// Press "1" to do breadth-first search
// Press "2" to do depth-first search

// Extra Credit:
// Allow the user the ability to start a new maze 
// without restarting the program by pressing "r"

// the edge class that represents an edge 
class Edge {
  Node prev;
  Node next;
  int number;
  boolean under;

  // TEMPLATE
  /* 
   * FIELDS:
   * ... this.prev ...                      -- Node
   * ... this.next ...                      -- Node
   * ... this.number ...                    -- int
   * ... this.under ...                     -- boolean
   * 
   * METHODS: 
   * ... this.hashCode() ...                -- int
   * ... this.equals(Object) ...            -- boolean
   * 
   * METHODS OF FIELDS: 
   * ... this.prev.hashCode() ...           -- int
   * ... this.next.hashCode() ...           -- int
   * ... this.prev.equals() ...             -- boolean
   * ... this.next.equals() ...             -- boolean
   */

  // Constructor
  Edge(Node from, Node to, int weight, boolean bottom) {
    this.prev = from;
    this.next = to;
    this.number = weight;
    this.under = bottom;
  }

  Edge(Node from, Node to, int weight) {
    this(from, to, weight, true);
  }

  @Override
  //this function creates a hashCode
  public int hashCode() {
    int hashProduct = prev.hashCode() + next.hashCode();
    return hashProduct;
  }

  @Override
  // checks between objects to determine if they are equal 
  public boolean equals(Object other) {
    boolean equal = !(other instanceof Edge);
    if (equal) {
      return false;
    }
    Edge that = (Edge) other;
    boolean checkPrev = this.prev.equals(that.prev);
    boolean checknext = this.next.equals(that.next);
    boolean checknumber = this.number == that.number;
    boolean checkunder = this.under == that.under;
    return checkPrev && checknext && checknumber && checkunder;
  }
}

// class that represents the maze
class Maze extends World {

  // initial variables
  int width;
  int height;
  int placesTouched;
  int wrongPaths;
  int initWidth;
  int initHeight;
  static int range = 50;
  static int w = 400;
  static int h = 400;
  HashMap<Node, Node> hashKey;
  ArrayList<Edge> edgesIntree;
  ArrayList<Edge> worklist;
  ArrayList<Node> nodeNext;
  ArrayList<Color> colorNext;
  HashMap<Integer, Integer> representatives;
  ArrayList<ArrayList<Node>> listOfNodes;
  WorldScene scene = new WorldScene(Maze.h, Maze.w);

  // Template
  /* 
   * FIELDS: 
   * ... this.width ...                              -- int
   * ... this.height ...                             -- int
   * ... this.placesTouched ...                      -- int
   * ... this.wrongPaths ...                         -- int
   * ... this.initWidth ...                          -- int
   * ... this.initHeight ...                         -- int 
   * ... this.range ...                              -- int
   * ... this.w ...                                  -- int
   * ... this.h ...                                  -- int
   * ... this.hashKey ...                            -- HashMap
   * ... this.edgesIntree ...                        -- ArrayList<Edge>
   * ... this.worklist ...                           -- ArrayList<Edge>
   * ... thhis.nodeNext ...                          -- ArrayList<Node>
   * ... this.colorNext ...                          -- ArrayList<Color>
   * ... this.representatives ...                    -- HashMap
   * ... this.listOfNodes ...                        -- ArrayList
   * ... this.scene ...                              -- WorldScene
   * METHODS: 
   * ... this.initialize(Random) ...                 -- void
   * ... this.breadFirst() ...                       -- HashMap
   * ... this.depthFirst() ...                       -- HashMap
   * ... this.generateGrid() ...                     -- void
   * ... this.categorize() ...                       -- void
   * ... this.initialize() ...                       -- void
   * ... this.find(int) ...                          -- int
   * ... this.union(int int) ...                     -- void
   * ... this.generateTable(Random) ...              -- void
   * ... this.algo() ...                             -- void
   * ... this.createMaze() ...                       -- void
   * ... this.tileDraw(int int) ...                  -- void
   * ... this.onKeyEvent(String) ...                 -- void
   * ... this.onTick() ...                           -- void
   * ... this.look(boolean) ...                         -- HashMap
   * ... this.reconstruct(HashMap Node) ...          -- HashMap
   * ... this.makeScene() ...                        -- WorldScene
   */

  // Constructor
  Maze(int width, int height, Random rand) {
    this.width = width;
    this.height = height;
    this.initWidth = Maze.w / this.width;
    this.initHeight = Maze.h / this.height;
    this.reset(rand);
  }

  Maze(int width, int height) {
    this(width, height, new Random());
  }

  Maze() {
    this(25, 25);
  }

  // initialize variables
  void reset(Random rand) {
    this.placesTouched = 0;
    this.wrongPaths = 0;
    this.representatives = new HashMap<Integer, Integer>();
    this.edgesIntree = new ArrayList<Edge>();
    this.worklist = new ArrayList<Edge>();
    this.listOfNodes = new ArrayList<ArrayList<Node>>();
    this.nodeNext = new ArrayList<Node>();
    this.colorNext = new ArrayList<Color>();
    this.hashKey = new HashMap<Node, Node>();
    this.generateTable(rand);
    this.categorize();
    this.initialize();
    this.algo();
    this.createMaze();
  }

  // HashMap of bread-first Look
  HashMap<Node, Node> breadFirst() {
    return this.look(false);
  }

  // HashMap of depth-first Look
  HashMap<Node, Node> depthFirst() {
    return this.look(true);
  }
  
  // creates random grid
  void generateGrid() {
    this.generateTable(new Random());
  }

  // this function sorts all of the edges in the worklist
  void categorize() {
    int i = 0;
    while (i < this.worklist.size()) {
      int j = this.worklist.size() - 1;
      while (j > i) {
        int inumber = this.worklist.get(i).number;
        int jnumber = this.worklist.get(j).number;
        if (jnumber < inumber) {
          Edge that = this.worklist.get(j);
          Edge other = this.worklist.get(i);
          this.worklist.set(i, that);
          this.worklist.set(j, other);
        }
        j = j - 1;
      }
      i = i + 1;
    }
  }

  // reset hash map
  void initialize() {
    int y = 0;
    while (y < this.height) {
      int x = 0;
      while (x < this.width) {
        int getlistOfNodes = this.listOfNodes.get(y).get(x).hashCode();
        this.representatives.put(getlistOfNodes,getlistOfNodes);
        x = x + 1;
      }
      y = y + 1;
    }
  }

  // this function finds reps 
  int find(int key) {
    int getnum = this.representatives.get(key);
    boolean check = getnum == key;
    if (!check) {
      return find(this.representatives.get(key));
    }
    return key;
  }

  // unionizes two integer sets
  void union(int fromKey, int toKey) {
    this.representatives.replace(fromKey, toKey);
  }

  // generates a grid like table of nodes
  void generateTable(Random rand) {
    int i = 0;
    while (i < this.height) {
      this.listOfNodes.add(new ArrayList<Node>());
      int j = 0;
      while (j < this.width) {
        String s = (String.valueOf(j * 1));
        String k = String.valueOf(-1000 * i).concat(s);
        int takeValue = Integer.valueOf(k);
        this.listOfNodes.get(i).add(j, new Node(j, i, takeValue, new ArrayList<Edge>()));
        j = j + 1;
      }
      i = i + 1;
    }
    int y = 0;
    while (y < this.height) {
      int x = 0;
      while (x < this.width) {
        boolean checkHeight = this.height >= y + 2;
        if (checkHeight) {
          Node prev1 = this.listOfNodes.get(y).get(x);
          Node next1 = this.listOfNodes.get(y + 1).get(x);
          int number1 = rand.nextInt(Maze.range);
          Edge under = new Edge(prev1, next1, number1);
          this.worklist.add(under);
          Node getDim = this.listOfNodes.get(y).get(x);
          getDim.e.add(under);
        }
        boolean checkWidth = this.width >= x + 2;
        if (checkWidth) {
          Node prev2 = this.listOfNodes.get(y).get(x);
          Node next2 = this.listOfNodes.get(y).get(x + 1);
          int number2 = rand.nextInt(Maze.range);
          Edge next = new Edge(prev2, next2, number2, false);
          this.worklist.add(next);
          Node getDim = this.listOfNodes.get(y).get(x);
          getDim.e.add(next);
        }
        x = x + 1;
      }
      y = y + 1;
    }
    this.listOfNodes.get(0).get(0).c = Color.GREEN;
    this.listOfNodes.get(this.height - 1).get(this.width - 1).c = Color.MAGENTA;
  }

  // implementation of kruskal's algorithm
  void algo() {
    int num = 0;
    int area = this.height * this.width;
    while (num < this.worklist.size()
        && area > this.edgesIntree.size() - 1) {
      Edge min = this.worklist.get(num);
      int minFrom = min.prev.hashCode();
      int minTo = min.next.hashCode();
      boolean isTrue = this.find(minFrom) == this.find(minTo);
      if (!(isTrue)) {
        this.edgesIntree.add(min);
        int i = 0;
        ArrayList<Edge> edge = min.prev.e;
        while (i < edge.size()) {
          boolean check = edge.get(i).equals(min);
          if (check) {
            Edge change = edge.remove(i);
            Edge changeTo = new Edge(change.next, change.prev, change.number);

            ArrayList<Edge> edgeFrom = min.prev.eMult;
            ArrayList<Edge> edgeTo = min.next.eMult;
            edgeFrom.add(change);
            edgeTo.add(changeTo);
            i = i - 1;
          }
          i = i + 1;
        }
        this.union(this.find(min.prev.hashCode()), this.find(min.next.hashCode()));
      }
      num = num + 1;
    }
  }

  //function that creates the maze
  void createMaze() {
    int y = 0;
    while (y < this.height) {
      int x = 0; 
      while (x < this.width) {
        this.tileDraw(y, x);
        x = x + 1;
      }
      y = y + 1;
    }
  }

  // this function draws each tile with a provided width and height
  void tileDraw(int y, int x) {
    int halfWidth = this.initWidth / 2;
    int halfHeight = this.initHeight / 2;
    int realWidth = this.initWidth * x / 2;
    int realHeight = this.initHeight * y / 2;
    int with = realWidth + halfWidth;
    int hight = realHeight + halfHeight;
    this.scene = this.listOfNodes.get(y).get(x).addAll(this.initWidth / 2, this.initHeight / 2, 
        with, hight, this.scene);
  }

  //onKeyEvent() function 
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.reset(new Random());
    }
    if (key.equals("1")) {
      this.hashKey = this.depthFirst();
    }
    if (key.equals("2")) {
      this.hashKey = this.breadFirst();
    }
  }

  //onTick() function
  public void onTick() {
    boolean check = this.nodeNext.size() > 0 && this.colorNext.size() > 0;
    if (check) {
      Node other = this.nodeNext.remove(0);
      other.c = this.colorNext.remove(0);
      this.tileDraw(other.yPos, other.xPos);
    } else {
      this.nodeNext.clear();
      this.colorNext.clear();
    }
  }

  // function that handles all Looking operations in the program
  HashMap<Node, Node> look(boolean check) {
    ArrayDeque<Node> nextLook = new ArrayDeque<Node>();
    ArrayList<Node> alreadyLooked = new ArrayList<Node>();
    HashMap<Integer, Edge> previous = new HashMap<Integer, Edge>();
    if (!check) {
      nextLook.addFirst(this.listOfNodes.get(0).get(0));
    } else {
      nextLook.addLast(this.listOfNodes.get(0).get(0));
    }
    Node node;
    boolean crash;
    int num;
    while (nextLook.size() > 0) {
      this.placesTouched = placesTouched + 1;
      node = nextLook.getFirst();
      this.nodeNext.add(node);
      this.colorNext.add(Color.GREEN);

      if (alreadyLooked.contains(node)) {
        nextLook.removeFirst();
        this.wrongPaths = wrongPaths + 1;
        int removed = this.nodeNext.size() - 1;
        this.nodeNext.remove(removed);
        this.colorNext.remove(removed);
      } else if (node.equals(this.listOfNodes.get(height - 1).get(width - 1))) {
        return this.reconstruct(previous, node);
      }
      else {
        int i = 0;
        while (i < node.eMult.size()) {
          crash = true;
          boolean isTrue = alreadyLooked.contains(node.eMult.get(i).next);
          if (!(isTrue)) {
            if (check) {
              nextLook.addFirst(node.eMult.get(i).next);
            } else {
              nextLook.addLast(node.eMult.get(i).next);
            }
          }
          num = node.eMult.get(i).next.hashCode();

          int j = 0;
          while (j < alreadyLooked.size()) {
            boolean isTrue2 = alreadyLooked.get(j).hashCode() == num;
            if (isTrue2) {
              crash = false;
            }
            j = j + 1;
          }
          if (crash) {
            previous.put(num, node.eMult.get(i));
          }
          i = i + 1;
        }
        alreadyLooked.add(node);
      }
    }
    return null;
  }

  // This function reconstructs the shortest path after being passed in a 
  // hashmap of all the edges and nodes
  HashMap<Node, Node> reconstruct(HashMap<Integer, Edge> prevEdge, Node to) {
    HashMap<Node, Node> solution = new HashMap<Node, Node>();
    Node node = this.listOfNodes.get(height - 1).get(width - 1);
    int num = node.hashCode();
    Node input2 = this.listOfNodes.get(0).get(0);

    while (!node.equals(input2)) {
      this.nodeNext.add(node);

      this.colorNext.add(Color.RED);
      Node input = prevEdge.get(num).next;
      solution.put(node, input);
      node = prevEdge.get(num).prev;
      num = node.hashCode();
    }

    this.nodeNext.add(input2);
    this.colorNext.add(Color.RED);
    solution.put(node, input2);
    return solution;
  }

  // makeScene() function which returns the scene
  @Override
  public WorldScene makeScene() {
    return this.scene;
  }
}

// the node class
class Node {
  static int b = 1;
  Color c;
  int xPos;
  int yPos;
  ArrayList<Edge> e; 
  ArrayList<Edge> eMult;
  int num;

  // TEMPLATE 
  /*
   * FIELDS: 
   * ... this.b ...                                      -- int
   * ... this.c ...                                      -- Color
   * ... this.xPos ...                                   -- int
   * ... this.yPos ...                                   -- int
   * ... this.e ...                                      -- ArrayList<Edge>
   * ... this.eMult ...                                  -- ArrayList<Edge>
   * ... this.num ...                                    -- int
   * 
   * METHODS:
   * ... this.equals() ...                               -- boolean
   * ... this.addAll(int, int, int, int, WorldScene)...  -- WorldScene
   * ... this.hashCode() ...                             -- int
   */

  // constructor
  Node(int y, int x, int num, ArrayList<Edge> e, Color c) {
    this.xPos = y;
    this.yPos = x;
    this.num = num;
    this.e = e;
    this.c = c;
    this.eMult = new ArrayList<Edge>();
  }

  Node(int y, int x, int key, ArrayList<Edge> edges) {
    this(y, x, key, edges, Color.lightGray);
  }

  @Override
  // this function overrides the hashcode
  public boolean equals(Object that) {
    boolean equal = !(that instanceof Node);
    if (equal) {
      return false;
    }
    Node other = (Node) that;
    return this.num == other.num;
  }

  // add all squares to the worldScene
  public WorldScene addAll(int w, int h, int x, int y, WorldScene s) {
    RectangleImage rec1 = new RectangleImage(w, h, OutlineMode.SOLID, this.c);
    s.placeImageXY(rec1, x, y);
    int i = 0;
    while (i < this.e.size()) {
      boolean check = this.e.get(i).under;
      if (check) {
        RectangleImage rec2 = new RectangleImage(w, Node.b, OutlineMode.SOLID, Color.WHITE);
        int heightHalf = h / 2;
        s.placeImageXY(rec2, x, y + heightHalf);
      } else {
        int widthHalf = w / 2;
        RectangleImage rec3 = new RectangleImage(Node.b, h, OutlineMode.SOLID, Color.WHITE);
        s.placeImageXY(rec3, x + widthHalf, y);
      }
      i = i + 1;
    }
    return s;
  }

  @Override
  // this function returns the numbers from the hashcode sequence
  public int hashCode() {
    return this.num;
  }
}

// Examples class
class Examples {
  Random r;
  Maze maze;
  ArrayList<Edge> listOfEdges1;
  ArrayList<Edge> listOfEdges2;
  ArrayList<Edge> listOfEdges3;
  ArrayList<ArrayList<Node>> listOfNodes;
  ArrayList<Edge> testworklist;

  Node node1;
  Node node2;
  Node node3;
  Edge edge1;
  Edge edge2;
  ArrayList<Edge> arrayListofEdges;

  // mazeDraw() method test
  void testMazeDraw(Tester t) {
    Maze maze = new Maze();
    maze.bigBang(maze.w, maze.h, 1.0 / 56.0);
  }

  // data used
  void init() {
    r = new Random(0);
    maze = new Maze(2, 2, r);
    this.listOfEdges1 = new ArrayList<Edge>();
    this.listOfEdges1.add(new Edge(null, null, 15));
    this.listOfEdges1.add(new Edge(null, null, 3));
    Node node1 = new Node(0, 0, 0, listOfEdges1);
    node1.c = Color.GREEN;
    this.listOfEdges2 = new ArrayList<Edge>();
    this.listOfEdges2.add(new Edge(null, null, 41));
    Node node2 = new Node(1, 0, 1, listOfEdges2);
    this.listOfEdges3 = new ArrayList<Edge>();
    this.listOfEdges3.add(new Edge(null, null, 11));
    Node node3 = new Node(0, 1, -10000, listOfEdges3);
    Node node4 = new Node(1, 1, -10001, new ArrayList<Edge>());
    node4.c = Color.MAGENTA;
    this.listOfEdges1.get(0).prev = node1;
    this.listOfEdges1.get(0).next = node3;
    this.listOfEdges1.get(1).under = false;
    this.listOfEdges1.get(1).prev = node1;
    this.listOfEdges1.get(1).next = node2;
    this.listOfEdges2.get(0).prev = node2;
    this.listOfEdges2.get(0).next = node4;
    this.listOfEdges3.get(0).under = false;
    this.listOfEdges3.get(0).prev = node3;
    this.listOfEdges3.get(0).next = node4;
    this.listOfNodes = new ArrayList<ArrayList<Node>>();
    this.listOfNodes.add(new ArrayList<Node>());
    this.listOfNodes.get(0).add(node1);
    this.listOfNodes.get(0).add(node2);
    this.listOfNodes.add(new ArrayList<Node>());
    this.listOfNodes.get(1).add(node3);
    this.listOfNodes.get(1).add(node4);
    this.testworklist = new ArrayList<Edge>();
    this.testworklist.addAll(this.listOfEdges1);
    this.testworklist.addAll(this.listOfEdges2);
    this.testworklist.addAll(this.listOfEdges3);
    this.arrayListofEdges = new ArrayList<Edge>();
    this.node1 = new Node(0, 0, 200, this.arrayListofEdges, Color.BLUE);
    this.node2 = new Node(1, 0, 200, this.arrayListofEdges, Color.BLUE);
    this.node3 = new Node(1, 1, 350, this.arrayListofEdges, Color.RED);
    this.edge1 = new Edge(this.node1, this.node2, 20, false);
    this.edge2 = new Edge(this.node1, this.node2, 20, false);
  }

  //initialize() method test
  void testinitialize(Tester t) {
    this.init();
    this.maze.representatives.clear();
    this.maze.initialize();
    HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
    hash.put(this.listOfNodes.get(0).get(0).hashCode(), 
        this.listOfNodes.get(0).get(0).hashCode());
    hash.put(this.listOfNodes.get(0).get(1).hashCode(), 
        this.listOfNodes.get(0).get(1).hashCode());
    hash.put(this.listOfNodes.get(1).get(0).hashCode(), 
        this.listOfNodes.get(1).get(0).hashCode());
    hash.put(this.listOfNodes.get(1).get(1).hashCode(), 
        this.listOfNodes.get(1).get(1).hashCode());
    t.checkExpect(this.maze.representatives, hash);
  }

  // find() method test
  void testfind(Tester t) {
    this.init();
    t.checkExpect(this.maze.find(0), -10001);
    t.checkExpect(this.maze.find(1), -10001);
    this.maze.initialize();
    t.checkExpect(this.maze.find(0), 0);
    t.checkExpect(this.maze.find(1), 1);
  }

  // union() method test
  void testUnion(Tester t) {
    this.init();
    this.maze.union(0, 10);
    t.checkExpect(this.maze.representatives.get(0), 10);
    t.checkExpect(this.maze.representatives.get(1), -10001);
    this.maze.union(1, -100);
    t.checkExpect(this.maze.representatives.get(0), 10);
    t.checkExpect(this.maze.representatives.get(1), -100);
  }

  //generateTable() method test
  void testgenerateTable(Tester t) {
    this.init();
    this.maze.listOfNodes = new ArrayList<ArrayList<Node>>();
    this.maze.worklist = new ArrayList<Edge>();
    this.maze.generateTable(r);
    t.checkExpect(this.maze.listOfNodes, this.listOfNodes);
    t.checkExpect(this.maze.worklist, this.testworklist);
  }

  //algo() method test
  void testalgo(Tester t) {
    this.init();
    this.maze.algo();
    t.checkExpect(this.maze.edgesIntree.size(), 3);
  }

  //equalsNode() method test
  boolean testEqualsNode(Tester t) {
    this.init();
    return t.checkExpect(this.node2.equals(this.node1), true);
  }

  //equals() method test
  boolean testEqualsEdge(Tester t) {
    this.init();
    return t.checkExpect(this.edge1.equals("Test"), false)
        && t.checkExpect(this.edge1.equals(this.edge2), true);
  }

  //hasCodeNode() method test
  boolean testHashCodeNode(Tester t) {
    this.init();
    return t.checkExpect(this.node1.hashCode(), 200) 
        && t.checkExpect(this.node2.hashCode(), 200)
        && t.checkExpect(this.node3.hashCode(), 350);
  }

  // hashCode() method test
  boolean testHashCodeEdge(Tester t) {
    this.init();
    return t.checkExpect(this.edge1.hashCode(), 400) 
        && t.checkExpect(this.edge2.hashCode(), 400);
  }

  // addAll() method test
  boolean testaddAll(Tester t) {
    this.init();
    WorldScene worldScene = new WorldScene(400, 400);
    WorldScene newWorldScene = new WorldScene(400, 400);
    RectangleImage rec1 = new RectangleImage(50, 50, OutlineMode.SOLID, Color.BLUE);
    newWorldScene.placeImageXY(rec1, 30, 30);

    WorldScene worldScene1 = new WorldScene(900, 900);
    WorldScene newWorldScene1 = new WorldScene(900, 900);
    RectangleImage rec2 = new RectangleImage(30, 30, OutlineMode.SOLID, Color.BLUE);
    newWorldScene1.placeImageXY(rec2, 20, 20);
    return t.checkExpect(this.node1.addAll(50, 50, 30, 30, worldScene), newWorldScene)
        && t.checkExpect(this.node2.addAll(30, 30, 20, 20, worldScene1), newWorldScene1);
  }

  // CreateMaze() method test
  void testCreateMaze(Tester t) {
    this.init();

    WorldScene s = new WorldScene(400, 400);
    s.placeImageXY(new RectangleImage(200, 200, OutlineMode.SOLID, Color.GREEN), 100, 100);
    s.placeImageXY(new RectangleImage(1, 200, OutlineMode.SOLID, Color.WHITE), 200, 100);
    s.placeImageXY(new RectangleImage(200, 200, OutlineMode.SOLID, Color.lightGray), 300, 100);
    s.placeImageXY(new RectangleImage(200, 200, OutlineMode.SOLID, Color.lightGray), 100, 300);
    s.placeImageXY(new RectangleImage(200, 200, OutlineMode.SOLID, Color.MAGENTA), 300, 300);
    s.placeImageXY(new RectangleImage(200, 200, OutlineMode.SOLID, Color.GREEN), 100, 100);
    s.placeImageXY(new RectangleImage(1, 200, OutlineMode.SOLID, Color.WHITE), 200, 100);
    s.placeImageXY(new RectangleImage(200, 200, OutlineMode.SOLID, Color.lightGray), 300, 100);
    s.placeImageXY(new RectangleImage(200, 200, OutlineMode.SOLID, Color.lightGray), 100, 300);
    s.placeImageXY(new RectangleImage(200, 200, OutlineMode.SOLID, Color.MAGENTA), 300, 300);

    int newWidth = Maze.w / this.maze.width;
    int newHeight = Maze.h / this.maze.height;
    int j = 0;
    while (j < this.maze.height) {
      int i = 0;
      while (i < this.maze.width) {
        this.maze.scene = this.maze.listOfNodes.get(j).get(i).addAll(newWidth,
            newHeight, newWidth * i + (newWidth / 2), (newHeight * j + newHeight / 2),
            this.maze.scene);
        i = i + 1;
      }
      j = j + 1;
    }
    t.checkExpect(this.maze.makeScene(), s);
  }

  //OnKey() Method test
  void testOnKeyEvent(Tester t) {
    this.init();
    this.maze.onKeyEvent("1");
    t.checkExpect(this.maze.placesTouched, 3);
    t.checkExpect(this.maze.wrongPaths, 0);
    this.maze.onKeyEvent("2");
    t.checkExpect(this.maze.placesTouched, 8);
    t.checkExpect(this.maze.wrongPaths, 2);
  }

  //OnTick() Method test
  void testOnTick(Tester t) {
    this.init();
    t.checkExpect(this.maze.nodeNext.size(), 0);
  }

  // Look() method test
  void testLook(Tester t) {
    this.init();
    this.maze.look(true);
    t.checkExpect(this.maze.hashKey, new HashMap<Node, Node>());
  }

  // makeScene() Method test
  boolean testMakeScene(Tester t) {
    this.init();
    return t.checkExpect(this.maze.makeScene(), this.maze.scene);
  }
}
