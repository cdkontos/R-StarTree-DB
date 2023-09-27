public class RStarTree {

    private int totalLevels;
    private boolean[] levelsInserted;
    private static final int ROOT_NODE_BLOCK_ID = 1;
    private static final int LEAF_LEVEL = 1;
    private static final int CHOOSE_SUBTREE_P_ENTRIES = 32;
    private static final int REINSERT_P_ENTRIES = (int) (0.30 * Node.getMaxEntries());

    RStarTree(boolean insertRecords)
    {

    }
    static int getRootNodeBlockId()
    {
        return ROOT_NODE_BLOCK_ID;
    }
    static int getLeafLevel() {
        return LEAF_LEVEL;
    }
}
