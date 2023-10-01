import java.util.ArrayList;

/**
 * Query is an abstract class used for executing queries with the use of the RStarTree.
 * Subclasses of Query implement specific types of queries.
 *
 *  Akompian Georgios
 */
abstract class Query {
    /**
     * Returns the IDs of the query's records based on the provided R-tree node.
     *
     * @param node The R-tree node to start the query from.
     * @return ArrayList of qualifying record IDs.
     */
    abstract ArrayList<Long> getQueryRecordIds(Node node);
}
