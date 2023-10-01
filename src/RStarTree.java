import java.util.ArrayList;
import java.util.Collections;

public class RStarTree {

    private int totalLevels;
    private boolean[] levelsInserted;
    private static final int ROOT_NODE_BLOCK_ID = 1;
    private static final int LEAF_LEVEL = 1;
    private static final int CHOOSE_SUBTREE_P_ENTRIES = 32;
    private static final int REINSERT_P_ENTRIES = (int) (0.30 * Node.getMaxEntries());

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
                long zValue1 = getZvalue(o1);
                long zValue2 = getZvalue(o2);
                return Long.compare(zValue1,zValue2);
            });

            long startTreeTime = System.nanoTime();
            insertRecord(records);
            long stopTreeTime = System.nanoTime();
            System.out.println("Time taken for R*Tree Bulk Loading: " + (double) (stopTreeTime - startTreeTime) / 1000000 + " ms");
        }

    }

    private long getZvalue(Record record)
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

    // Query which returns the ids of the Records that are inside the given searchBoundingBox
    ArrayList<Long> getBoundingBoxData(BoundingBox searchBoundingBox){
        Query query = new BoundingBoxRangeQuery(searchBoundingBox);
        return query.getQueryRecordIds(FilesHelper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }

    // Query which returns the ids of the K Records that are closer to the given point
    ArrayList<Long> getNearestNeighbours(ArrayList<Double> searchPoint, int k){
        Query query = new NearestNeighbourQuery(searchPoint,k);
        return query.getQueryRecordIds(FilesHelper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }
    ArrayList<Long> getSkyline(ArrayList<Double> queryPoint) {
        Query query = new SkylineQuery(queryPoint);
        return query.getQueryRecordIds(FilesHelper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }
    private void deleteRecord(Record record, long datafileBlockId, Node node)
    {
        ArrayList<Bounds> dimensionBounds = new ArrayList<>();
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            dimensionBounds.add(new Bounds(record.getCoordinate(i),record.getCoordinate(i)));
        }
        LeafEntry leafEntry = new LeafEntry(record.getId(),datafileBlockId,dimensionBounds);
        Node leafNode = findLeaf(leafEntry,node);

    }

    private Node findLeaf(Entry entry, Node node)
    {
        ArrayList<Entry> entries = node.getEntries();
        if(node.getLevel() != LEAF_LEVEL)
        {
            for (Entry entry1 : entries)
            {
                if(BoundingBox.checkBoxOverlap(entry1.getBoundingBox(),entry.getBoundingBox()))
                {
                    Node child = FilesHelper.readIndexFileBlock(entry1.getChildNodeBlockID());
                    findLeaf(entry1,child);
                }
            }
        }
        else
        {
            for (int i = 0; i < FilesHelper.getDataDimensions(); i++) {
                for (Entry entry1 : entries)
                {
                    int lower = Double.compare(entry1.getBoundingBox().getBounds().get(i).getLower(),entry.getBoundingBox().getBounds().get(i).getLower());
                    int upper = Double.compare(entry1.getBoundingBox().getBounds().get(i).getUpper(),entry.getBoundingBox().getBounds().get(i).getUpper());
                    if(lower == 0 && upper == 0)
                    {
                        return node;
                    }
                }
            }
        }
        return null;
    }
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
