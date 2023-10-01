import java.util.ArrayList;
/**
 * The SeqScanQuery class is an abstract class used for executing queries without the use of an index.
 * Subclasses of this class implement specific query types and provide methods to return the IDs of
 * records that satisfy the query criteria.
 *
 * @author Akompian Georgios
 */

abstract class SeqScanQuery {
    /**
     * Returns the IDs of the query's records that satisfy the query criteria.
     *
     * @return The IDs of the records that meet the query criteria.
     */
    abstract ArrayList<Long> getQueryRecordIds();
}
