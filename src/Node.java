import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * This class implements the node of the tree and the methods it uses.
 * @author Christos Kontos
 */
public class Node implements Serializable {
    private static final int MAX_ENTRIES = FilesHelper.calculateMaxEntriesInNode(); // The maximum entries that a Node can fit based on the file parameters.
    private static final int MIN_ENTRIES = (int) (0.4 * MAX_ENTRIES); // Setting m to 40%.
    private int level; // The level of the tree that this Node is located at.
    private long blockID; // The unique ID of the file block that this Node refers to.
    private ArrayList<Entry> entries; // The ArrayList with the Entries of the Node.
    private Long childNodeBlockID; //The id of the child node of this node.

    /**
     * The constructor for the root with the level as a parameter which creates a new empty ArrayList for the node.
     * @param level the level of the node.
     */
    public Node(int level) {
        this.level = level;
        this.blockID = RStarTree.getRootNodeBlockId();
        this.entries = new ArrayList<>();
    }

    /**
     * The constructor of the node with the level and entries as parameters.
     * @param level the level of the node.
     * @param entries the entries of the node.
     */
    public Node(int level, ArrayList<Entry> entries)
    {
        this.level = level;
        this.entries = entries;
    }
    public void setBlockID(long blockID) {
        this.blockID = blockID;
    }

    public void setEntries(ArrayList<Entry> entries) {
        this.entries = entries;
    }

    public int getLevel() {
        return level;
    }

    public boolean isLeaf() {
        return level == RStarTree.getLeafLevel();
    }

    void setChildNodeBlockID(Long childNodeBlockID) {
        this.childNodeBlockID = childNodeBlockID;
    }

    public Long getChildNodeBlockID() {
        return childNodeBlockID;
    }

    public Node getChildNode(Map<Long, Node> nodeMap) {
        if (nodeMap != null && nodeMap.containsKey(childNodeBlockID)) {
            // Retrieve and return the Node object from the map based on childNodeBlockID
            return nodeMap.get(childNodeBlockID);
        } else {
            // Return null or handle the case where the Node is not found
            return null;
        }
    }

    public long getBlockID()
    {
        return blockID;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public static int getMaxEntries()
    {
        return MAX_ENTRIES;
    }

    public void addEntry(Entry entry)
    {
        entries.add(entry);
    }

    /**
     * This method is used to split the node into a distribution of nodes
     * and calls the chooseIndex function to return the split nodes as an ArrayList of Nodes.
     * @return calls the chooseIndex function to return the split nodes as an ArrayList of Nodes.
     */
    public ArrayList<Node> splitNode()
    {
        ArrayList<Distribution> axisDistributions = pickAxis();
        return chooseIndex(axisDistributions);
    }

    /**
     * This method is used to choose the axis in which the bounding box will be split.
     * @return an ArrayList of the distributions made along the chosen axis.
     */
    private ArrayList<Distribution> pickAxis()
    {
        ArrayList<Distribution> axisDistributions = new ArrayList<>();
        double splitAxisPerimeterSum = Double.MAX_VALUE;
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            ArrayList<Entry> entriesUpperSort = new ArrayList<>();
            ArrayList<Entry> entriesLowerSort = new ArrayList<>();

            for (Entry entry : entries)
            {
                entriesLowerSort.add(entry);
                entriesUpperSort.add(entry);
            }

            entriesLowerSort.sort(new EntryCompare.EntryBoundCompare(entriesLowerSort,i,false));
            entriesUpperSort.sort(new EntryCompare.EntryBoundCompare(entriesUpperSort,i,true));

            ArrayList<ArrayList<Entry>> sortedEntries = new ArrayList<>();
            sortedEntries.add(entriesLowerSort);
            sortedEntries.add(entriesUpperSort);

            double perimeterSum = 0;
            ArrayList<Distribution> distributions = new ArrayList<>();
            for (ArrayList<Entry> sortedEntry : sortedEntries)
            {
                for (int j = 1; j <= MAX_ENTRIES-2*MIN_ENTRIES+2; j++)
                {
                    ArrayList<Entry> groupA = new ArrayList<>();
                    ArrayList<Entry> groupB = new ArrayList<>();

                    for (int k = 0; k < (MIN_ENTRIES-1)+j; k++)
                    {
                        groupA.add(sortedEntry.get(k));
                    }
                    for (int k = (MIN_ENTRIES-1)+j; k < entries.size(); k++)
                    {
                        groupB.add(sortedEntry.get(k));
                    }

                    BoundingBox groupABox = new BoundingBox(Bounds.findMinBounds(groupA));
                    BoundingBox groupBBox = new BoundingBox(Bounds.findMinBounds(groupB));

                    Distribution distribution = new Distribution(new DistributionGroup(groupA,groupABox), new DistributionGroup(groupB,groupBBox));
                    distributions.add(distribution);
                    perimeterSum += groupABox.getPerimeter() + groupBBox.getPerimeter();
                }

                if(splitAxisPerimeterSum > perimeterSum)
                {
                    splitAxisPerimeterSum = perimeterSum;
                    axisDistributions = distributions;
                }
            }
        }
        return axisDistributions;
    }

    /**
     * This method chooses the distribution with the minimum overlap value along the chosen split axis
     * and the returns the 2 nodes that occurred from the split.
     * @param axisDistributions the distributions made along the split axis.
     * @return the 2 nodes that occurred from the split.
     */
    private ArrayList<Node> chooseIndex(ArrayList<Distribution> axisDistributions)
    {
        if(axisDistributions.size() == 0)
        {
            throw new IllegalArgumentException("Wrong group size.");
        }

        double minOverlapVal = Double.MAX_VALUE;
        double minAreaVal = Double.MAX_VALUE;
        int bestIndex = 0;
        for (int i = 0; i < axisDistributions.size(); i++)
        {
            DistributionGroup distributionGroupA = axisDistributions.get(i).getFirstGroup();
            DistributionGroup distributionGroupB = axisDistributions.get(i).getSecondGroup();
            double overlap = BoundingBox.calcOverlapVal(distributionGroupA.getBoundingBox(),distributionGroupB.getBoundingBox());
            if(minOverlapVal > overlap)
            {
                minOverlapVal = overlap;
                minAreaVal = distributionGroupA.getBoundingBox().getArea() + distributionGroupB.getBoundingBox().getArea();
                bestIndex = i;
            }
            else if (minOverlapVal == overlap)
            {
                double area = distributionGroupA.getBoundingBox().getArea() + distributionGroupB.getBoundingBox().getArea();
                if(minAreaVal > area)
                {
                    minAreaVal = area;
                    bestIndex = i;
                }
            }
        }
        ArrayList<Node> splitNodes = new ArrayList<>();
        DistributionGroup groupA = axisDistributions.get(bestIndex).getFirstGroup();
        DistributionGroup groupB = axisDistributions.get(bestIndex).getSecondGroup();
        splitNodes.add(new Node(level,groupA.getEntries()));
        splitNodes.add(new Node(level,groupB.getEntries()));
        return splitNodes;
    }
}
