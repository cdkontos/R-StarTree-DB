import java.util.ArrayList;
// Class used for executing queries without the use of an index
abstract class SeqScanQuery {
    // Returns the ids of the query's records
    abstract ArrayList<Long> getQueryRecordIds();
}
