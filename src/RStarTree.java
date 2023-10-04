import java.util.ArrayList;
import java.util.Collections;

/**
 *This class implements the RStarTree as well as the different methods it uses.
 * @author Christos Kontos
 */
public class RStarTree {

    private int totalLevels; // The total levels of the tree, increasing the size starting with the root, the root (top level) will always have the highest level.
    private boolean[] levelsInserted; // Used for information on which levels have already called overflow on data insertion.
    private static final int ROOT_NODE_BLOCK_ID = 1; // Root node will always have 1 as its ID, in order to identify which block has the root Node.
    private static final int LEAF_LEVEL = 1; // Constant leaf level 1, since we are increasing the level from the root, the root (top level) will always have the highest level.
    private static final int CHOOSE_SUBTREE_P_ENTRIES = 32;
    private static final int REINSERT_P_ENTRIES = (int) (0.30 * Node.getMaxEntries()); // Setting p to 30% of max entries.

    /**
     * This is the constructor for the normal RStar Tree.
     * It uses a boolean parameter to identify whether to create a new tree in the data files or not.
     * @param insertRecords boolean that is used to signal the making of a new tree or not.
     */
    public RStarTree(boolean insertRecords)
    {
        this.totalLevels = FilesHelper.getTotalLevelsOfTreeIndex();
        if(insertRecords)
        {
            FilesHelper.writeNewIndexFileBlock(new Node(1));

            for (int i = 1; i < FilesHelper.getTotalBlocksInDatafile(); i++)
            {
                ArrayList<Record> records = FilesHelper.readDataFileBlock(i);
                if(records!=null)
                {
                    for (Record record : records)
                    {
                        insertRecord(record,i);
                    }
                }
                else
                    throw new IllegalStateException("Couldn't read records from datafile properly.");
            }
        }

    }

    /**
     * This is the constructor for the bulk loaded RStar Tree
     * It uses two boolean parameters to identify whether to create a new tree in the data files or not
     * and to differentiate it from the other constructor with the bulk parameter.
     * @param insertRecords boolean that is used to signal the making of a new tree or not.
     * @param bulk boolean that is used to differentiate this constructor as the bulk load one.
     */
    public RStarTree(boolean insertRecords, boolean bulk)
    {
        this.totalLevels = FilesHelper.getTotalLevelsOfTreeIndex();
        if(insertRecords)
        {
            FilesHelper.writeNewIndexFileBlock(new Node(1));
            ArrayList<Record> records = new ArrayList<>();
            for (int i = 1; i < FilesHelper.getTotalBlocksInDatafile(); i++) {
                for (Record record : FilesHelper.readDataFileBlock(i)) {
                    records.add(record);
                }
            }
            ArrayList<Long> zValues = new ArrayList<>();
            for (Record record : records)
            {
                ArrayList<Double> coordinates = new ArrayList<>();
                for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
                {
                    coordinates.add(record.getCoordinate(i));
                }
                zValues.add(interleaveBits(coordinates));
            }
            Collections.sort(records, (o1, o2) -> {
                long zValue1 = getZValue(o1);
                long zValue2 = getZValue(o2);
                return Long.compare(zValue1,zValue2);
            });

            long startTreeTime = System.nanoTime();
            insertRecord(records);
            long stopTreeTime = System.nanoTime();
            System.out.println("Time taken for R*Tree Bulk Loading: " + (double) (stopTreeTime - startTreeTime) / 1000000 + " ms");
        }

    }

    /**
     * This method is used to get the ZValue of the record given by calling the interleaveBits function.
     * @param record the record of which we want the ZValue.
     * @return this returns the ZValue of the record by calling the interleaveBits function with its coordinates.
     */
    private long getZValue(Record record)
    {
        ArrayList<Double> coordinates = new ArrayList<>();
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            coordinates.add(record.getCoordinate(i));
        }
        return interleaveBits(coordinates);
    }

    static int getRootNodeBlockId()
    {
        return ROOT_NODE_BLOCK_ID;
    }
    static int getLeafLevel() {
        return LEAF_LEVEL;
    }

    Node getRoot()
    {
        return FilesHelper.readIndexFileBlock(ROOT_NODE_BLOCK_ID);
    }

    /**
     * This method is used to insert a record into the normal RStarTree.
     * It calls the insert function to insert the records in the tree as new LeafEntries.
     * @param record the record we want to insert into the tree.
     * @param datafileBlockID the dataFileBlockID of the record.
     */
    private void insertRecord(Record record, long datafileBlockID)
    {
        ArrayList<Bounds> dimensionBounds = new ArrayList<>();
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            dimensionBounds.add(new Bounds(record.getCoordinate(i),record.getCoordinate(i)));
        }
        levelsInserted = new boolean[totalLevels];
        insert(null,null, new LeafEntry(record.getId(), datafileBlockID, dimensionBounds), LEAF_LEVEL);
    }

    /**
     * This method is used to insert the records into the bulk loaded RStarTree.
     * It calls the insert function to insert the records in the tree as new LeafEntries.
     * @param records the records we want to insert into the tree.
     */
    private void insertRecord(ArrayList<Record> records)
    {
        int sum = 0;
        for (int i = 1; i < FilesHelper.getTotalBlocksInDatafile(); i++)
        {
            ArrayList<Record> size = FilesHelper.readDataFileBlock(i);
            for (int s = sum ; s < sum + size.size() ; s++)
            {
                ArrayList<Bounds> dimensionBounds = new ArrayList<>();
                for (int j = 0; j < FilesHelper.getDataDimensions(); j++)
                {
                    dimensionBounds.add(new Bounds(records.get(s).getCoordinate(j),records.get(s).getCoordinate(j)));
                }
                levelsInserted = new boolean[totalLevels];
                insert(null,null, new LeafEntry(records.get(s).getId(), i, dimensionBounds), LEAF_LEVEL);
            }
            sum += size.size();
        }
    }

    /**
     * This method inserts the entries into the tree and adjusts it.
     * @param parentN this is the parent node of the node the entry is in.
     * @param parentE this is the parent entry of the entry we want to insert.
     * @param data  this is the entry we want to insert into the tree.
     * @param level this is the level of the tree the entry will be inserted into.
     * @return this method either returns null cause of recursion to signal that it has finished,
     * or it calls the overflow function to balance the tree.
     */
    private Entry insert(Node parentN, Entry parentE, Entry data, int level)
    {
        Node child;
        long readID;

        if(parentE == null)
        {
            readID = ROOT_NODE_BLOCK_ID;
        }
        else
        {
            parentE.adjustBoxEntry(data);
            FilesHelper.updateIndexFileBlock(parentN,totalLevels);
            readID = parentE.getChildNodeBlockID();
        }

        child = FilesHelper.readIndexFileBlock(readID);
        if(child == null)
        {
            throw new IllegalStateException("The node read is null.");
        }

        if(child.getLevel() == level)
        {
            child.addEntry(data);
            FilesHelper.updateIndexFileBlock(child,totalLevels);
        }
        else
        {
            Entry bestEntry = pickSubTree(child,data.getBoundingBox(),level);
            Entry newEntry = insert(child,bestEntry,data,level);
            child = FilesHelper.readIndexFileBlock(readID);
            if(child == null)
            {
                throw new IllegalStateException("The Node block is null.");
            }
            if(newEntry != null)
            {
                child.addEntry(newEntry);
                FilesHelper.updateIndexFileBlock(child,totalLevels);
            }
            else
            {
                FilesHelper.updateIndexFileBlock(child,totalLevels);
                return null;
            }
        }

        if(child.getEntries().size() > Node.getMaxEntries())
        {
            return overflow(parentN,parentE,child);
        }
        return null;
    }

    /**
     * This method is used to pick the subtree an entry will end up in.
     * @param node the node of the entry we want to move.
     * @param boundingBox the bounding box of the entry.
     * @param level the level of the node the entry is in.
     * @return this returns the best entry to be moved.
     */
    private Entry pickSubTree(Node node, BoundingBox boundingBox, int level)
    {
        Entry bestEntry;

        if(node.getLevel() == level+1)
        {
            if(Node.getMaxEntries() > (CHOOSE_SUBTREE_P_ENTRIES*2)/3 && node.getEntries().size() > CHOOSE_SUBTREE_P_ENTRIES)
            {
                ArrayList<EntryAreaEnlargement> entryAreaEnlargements = new ArrayList<>();
                for (Entry entry : node.getEntries())
                {
                    BoundingBox newBoundingBox = new BoundingBox(Bounds.findMinBounds(entry.getBoundingBox(),boundingBox));
                    double areaEnlargement = newBoundingBox.getArea() - entry.getBoundingBox().getArea();
                    entryAreaEnlargements.add(new EntryAreaEnlargement(entry,areaEnlargement));
                }
                entryAreaEnlargements.sort(EntryAreaEnlargement::compareTo);
                ArrayList<Entry> sortedEntries = new ArrayList<>();
                for (EntryAreaEnlargement entryAreaEnlargement : entryAreaEnlargements)
                {
                    sortedEntries.add(entryAreaEnlargement.getEntry());
                }
                bestEntry = Collections.min(sortedEntries.subList(0, CHOOSE_SUBTREE_P_ENTRIES), new EntryCompare.EntryEnlargementOverlapCompare(sortedEntries.subList(0, CHOOSE_SUBTREE_P_ENTRIES),boundingBox,node.getEntries()));
                return bestEntry;
            }

            bestEntry = Collections.min(node.getEntries(), new EntryCompare.EntryEnlargementOverlapCompare(node.getEntries(),boundingBox,node.getEntries()));
            return bestEntry;
        }
        ArrayList<EntryAreaEnlargement> entryAreaEnlargements = new ArrayList<>();
        for (Entry entry : node.getEntries())
        {
            BoundingBox newBox = new BoundingBox(Bounds.findMinBounds(entry.getBoundingBox(),boundingBox));
            double areaEnlargement = newBox.getArea() - entry.getBoundingBox().getArea();
            entryAreaEnlargements.add(new EntryAreaEnlargement(entry,areaEnlargement));
        }
        bestEntry = Collections.min(entryAreaEnlargements,EntryAreaEnlargement::compareTo).getEntry();
        return bestEntry;
    }

    /**
     * This method is used in the case a node overflows, and it has to be split.
     * @param parentN the parent node of the node that needs to be split.
     * @param parentE the entry of the parent node.
     * @param childN the child node to be split.
     * @return returns either null or an entry from the new split node.
     */
    private Entry overflow(Node parentN, Entry parentE, Node childN)
    {
        if(childN.getBlockID() != ROOT_NODE_BLOCK_ID && !levelsInserted[childN.getLevel()-1])
        {
            levelsInserted[childN.getLevel()-1] = true;
            reInsert(parentN,parentE,childN);
            return null;
        }
        ArrayList<Node> splitN = childN.splitNode();
        if(splitN.size()!=2)
        {
            throw new IllegalStateException("The number of split nodes cannot be anything other than 2");
        }
        childN.setEntries(splitN.get(0).getEntries());
        Node splitNode = splitN.get(1);

        if(childN.getBlockID() != ROOT_NODE_BLOCK_ID)
        {
            FilesHelper.updateIndexFileBlock(childN,totalLevels);
            splitNode.setBlockID(FilesHelper.getTotalBlocksInIndexFile());
            FilesHelper.writeNewIndexFileBlock(splitNode);

            parentE.adjustBoxEntries(childN.getEntries());
            FilesHelper.updateIndexFileBlock(parentN,totalLevels);
            return new Entry(splitNode);
        }

        childN.setBlockID(FilesHelper.getTotalBlocksInIndexFile());
        FilesHelper.writeNewIndexFileBlock(childN);
        splitNode.setBlockID(FilesHelper.getTotalBlocksInIndexFile());
        FilesHelper.writeNewIndexFileBlock(splitNode);

        ArrayList<Entry> newRooEntries = new ArrayList<>();
        newRooEntries.add(new Entry(childN));
        newRooEntries.add(new Entry(splitNode));
        Node newRoot = new Node(++totalLevels,newRooEntries);
        newRoot.setBlockID(ROOT_NODE_BLOCK_ID);
        FilesHelper.updateIndexFileBlock(newRoot,totalLevels);
        return null;
    }

    /**
     * This method is used to reinsert nodes after the overflow function is finished.
     * @param parentN the parent node.
     * @param parentE the entry of the parent node.
     * @param childN the child node.
     */
    private void reInsert(Node parentN, Entry parentE, Node childN)
    {
        if(childN.getEntries().size() != Node.getMaxEntries() +1)
        {
            throw new IllegalStateException("Cannot use reinsert for node with entries lower than M+1");
        }

        childN.getEntries().sort(new EntryCompare.EntryDistanceCenterCompare(childN.getEntries(),parentE.getBoundingBox()));
        ArrayList<Entry> removedEntries = new ArrayList<>(childN.getEntries().subList(childN.getEntries().size()-REINSERT_P_ENTRIES,childN.getEntries().size()));

        for (int i = 0; i < REINSERT_P_ENTRIES; i++)
        {
            childN.getEntries().remove(childN.getEntries().size()-1);
        }

        parentE.adjustBoxEntries(childN.getEntries());
        FilesHelper.updateIndexFileBlock(parentN,totalLevels);
        FilesHelper.updateIndexFileBlock(childN,totalLevels);

        if(removedEntries.size() != REINSERT_P_ENTRIES)
        {
            throw new IllegalStateException("Entries for reinsert are not the same amount as the removed ones.");
        }
        for (Entry removedEntry : removedEntries) {
            insert(null,null,removedEntry,childN.getLevel());
        }
    }

    /**
     * Query which returns the ids of the Records that are inside the given searchBoundingBox.
     * @param searchBoundingBox the bounding box we want to search in.
     * @return the ids of the Records that are inside the given searchBoundingBox
     */
    ArrayList<Long> getBoundingBoxData(BoundingBox searchBoundingBox){
        Query query = new BoundingBoxRangeQuery(searchBoundingBox);
        return query.getQueryRecordIds(FilesHelper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }

    /**
     * Query which returns the ids of the K Records that are closer to the given point.
     * @param searchPoint the point around which we will find the neighbours.
     * @param k the amount of neighbours.
     * @return the ids of the K Records that are closer to the given point.
     */
    ArrayList<Long> getNearestNeighbours(ArrayList<Double> searchPoint, int k){
        Query query = new NearestNeighbourQuery(searchPoint,k);
        return query.getQueryRecordIds(FilesHelper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }

    /**
     * Query which returns the skyline of the points given.
     * NOT IMPLEMENTED
     * //@param queryPoint the points from which we want to find the skyline.
     * @return the ids of the records that form the skyline.
     */
    ArrayList<Long> getSkyline() {
        Query query = new SkylineQuery();
        return query.getQueryRecordIds(FilesHelper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }

    /**
     * This method calculates the interleaveBits for the ZValue.
     * @param coordinates the coordinates of the record from which the ZValue will be calculated.
     * @return the ZValue.
     */
    public static long interleaveBits(ArrayList<Double> coordinates)
    {
        int numDimensions = coordinates.size();
        int numBits = Double.SIZE;
        long zValue = 0;
        for (int i = 0; i < numBits; i++) {
            for (int j = 0; j < numDimensions; j++) {
                long coordinateBits = Double.doubleToRawLongBits(coordinates.get(j));
                int coordinateBit = (int)((coordinateBits >> i) & 1L);
                zValue |= (coordinateBit << (i * numDimensions + j));
            }
        }
        return zValue;
    }



}
