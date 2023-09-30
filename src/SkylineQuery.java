import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkylineQuery{

    public static List<Record> findSkyline(RStarTree rStarTree) {
        Node rootNode = rStarTree.getRoot();
        Map<Long, Record> recordMap = new HashMap<>(); // Create a map to associate record IDs with records
        Map<Long, Node> nodeMap = new HashMap<>(); // Create a map to associate node IDs with nodes
        return findSkyline(rootNode, new ArrayList<>(), recordMap, nodeMap);
    }

    private static List<Record> findSkyline(Node node, List<Record> currentSkyline, Map<Long, Record> recordMap, Map<Long, Node> nodeMap) {
        if (node.isLeaf()) {
            for (Entry entry : node.getEntries()) {
                Record record = entry.getRecord(recordMap); // Pass the record map to get the associated record
                if (isSkyline(record, currentSkyline)) {
                    currentSkyline.add(record);
                }
            }
        } else {
            for (Entry entry : node.getEntries()) {
                Long childNodeBlockID = entry.getChildNodeBlockID();
                Node childNode = nodeMap.get(childNodeBlockID); // Retrieve the child node from the node map
                if (childNode != null) {
                    findSkyline(childNode, currentSkyline, recordMap, nodeMap); // Pass the record map and node map recursively
                }
            }
        }
        return currentSkyline;
    }

    private static boolean isSkyline(Record record, List<Record> skyline) {
        for (Record skylineRecord : skyline) {
            if (record.dominates(skylineRecord)) {
                return false;
            }
        }
        return true;
    }

}
