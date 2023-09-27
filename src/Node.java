import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable {
    private static final int MAX_ENTRIES = 1; //TODO ADD FILES
    private static final int MIN_ENTRIES = (int) (0.4 * MAX_ENTRIES);
    private final int level;
    private long blockID;
    private ArrayList<Entry> entries;

    public Node(int level) {
        this.level = level;
        this.blockID = RStarTree.getRootNodeBlockId();
        this.entries = new ArrayList<>();
    }

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

    public ArrayList<Node> splitNode()
    {
        ArrayList<Distribution> axisDistributions = pickAxis();
        return chooseIndex(axisDistributions);
    }

    private ArrayList<Distribution> pickAxis()
    {
        ArrayList<Distribution> axisDistributions = new ArrayList<>();
        double splitAxisPerimeterSum = Double.MAX_VALUE;
        for (int i = 0; i < 1; i++) //TODO ADD FILES
        {
            ArrayList<Entry> entriesUpperSort = new ArrayList<>();
            ArrayList<Entry> entreisLowerSort = new ArrayList<>();

            for (Entry entry : entries)
            {
                entreisLowerSort.add(entry);
                entriesUpperSort.add(entry);
            }

            entreisLowerSort.sort(new EntryCompare.EntryBoundCompare(entreisLowerSort,i,false));
            entriesUpperSort.sort(new EntryCompare.EntryBoundCompare(entriesUpperSort,i,true));

            ArrayList<ArrayList<Entry>> sortedEntries = new ArrayList<>();
            sortedEntries.add(entreisLowerSort);
            sortedEntries.add(entriesUpperSort);

            double perimeterSum = 0;
            ArrayList<Distribution> distributions = new ArrayList<>();
            for (ArrayList<Entry> sortedEntry : sortedEntries)
            {
                for (int j = 1; j <= MAX_ENTRIES-2*MIN_ENTRIES+2; j++) //TODO CHECK FOR "="???
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
