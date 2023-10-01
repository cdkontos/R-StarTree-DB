import java.io.*;
import java.util.ArrayList;

/**
 * Multiple2DQueries is a class for testing range and k-nearest neighbor (KNN) queries
 * on a two-dimensional dataset using both R* tree and sequential scan methods.
 * It measures query execution times and records the results in CSV files.
 *
 * @author Akompian Georgios
 */
public class Multiple2DQueries {
    /**
     * Main method for executing range and KNN queries and recording the results in CSV files.
     *
     * @param args The command-line arguments (not used in this application).
     */
    public static void main(String[] args){

        RStarTree rStarTree = new RStarTree(false);
        FilesHelper.initializeDataFile(2, false);

        ArrayList<Double> centerPoint = new ArrayList<>(); // ArrayList with the coordinates of an approximate center point
        centerPoint.add(22.2121); // Coordinate of second dimension
        centerPoint.add(37.4788); // Coordinate of first dimension

        double rangeIncrement = 0.000055; // How much the interval and radius increases each time

        // Range Query Data
        ArrayList<Double> rStarRangeQueryTimes = new ArrayList<>();
        ArrayList<Double> seqScanRangeQueryTimes = new ArrayList<>();
        ArrayList<Double> areaOfRectangles = new ArrayList<>();
        ArrayList<Integer> rangeQueryRecords = new ArrayList<>();

        // KNN Query Data
        ArrayList<Double> knnRStarTimes = new ArrayList<>();
        ArrayList<Double> knnSeqScanTimes = new ArrayList<>();

        int i = 1;
        while(i < 10000){
            // Taking values for every 100 samples
            if(i%100 == 0){

                //Range Query
                ArrayList<Bounds> queryBounds = new ArrayList<>();
                queryBounds.add(new Bounds(centerPoint.get(0) - i*rangeIncrement, centerPoint.get(0) + i*rangeIncrement));
                queryBounds.add(new Bounds(centerPoint.get(1) - i*rangeIncrement, centerPoint.get(1) + i*rangeIncrement));

                // R star Range Query
                long startRangeQueryTime = System.nanoTime();
                rangeQueryRecords.add(rStarTree.getBoundingBoxData(new BoundingBox(queryBounds)).size());
                long stopRangeQueryTime = System.nanoTime();
                rStarRangeQueryTimes.add((double) (stopRangeQueryTime - startRangeQueryTime) / 1000000);

                // Sequential Scan - Range Query
                SeqScanBoundingBoxRangeQuery sequentialScanBoundingBoxRangeQuery = new SeqScanBoundingBoxRangeQuery(new BoundingBox(queryBounds));
                long startSeqRangeQueryTime = System.nanoTime();
                sequentialScanBoundingBoxRangeQuery.getQueryRecordIds();
                long stopSeqRangeQueryTime = System.nanoTime();
                seqScanRangeQueryTimes.add((double) (stopSeqRangeQueryTime - startSeqRangeQueryTime) / 1000000);
                areaOfRectangles.add(new BoundingBox(queryBounds).getArea());
            }
            i++;
        }

        i = 1;
        while(i <= 300000){
            // Taking values for every 1000 samples
            if(i%1000 == 0) {
                // Knn R Star Query
                long startKNNTime = System.nanoTime();
                rStarTree.getNearestNeighbours(centerPoint, i);
                long stopKNNTime = System.nanoTime();
                knnRStarTimes.add((double) (stopKNNTime - startKNNTime) / 1000000);

                // Knn Sequential Scan Query
                SeqScanQuery seqNearestNeighboursQuery = new SeqNearestNeighbourQuery(centerPoint, i);
                long startSeqKNNTime = System.nanoTime();
                seqNearestNeighboursQuery.getQueryRecordIds();
                long stopSequentialKNNTime = System.nanoTime();
                knnSeqScanTimes.add((double) (stopSequentialKNNTime - startSeqKNNTime) / 1000000);
            }
            i++;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("rangeQueryResults.csv"))) {
            String tagString = "Rectangle's Area" +
                    ',' +
                    "Returned Records" +
                    ',' +
                    "R* Time(ms)" +
                    ',' +
                    "Sequential Scan Time(ms)" +
                    '\n';
            writer.write(tagString);

            // Range Query File creation
            int j = 0;
            while(j < rStarRangeQueryTimes.size()){
                writer.write(String.format("%.5f", areaOfRectangles.get(j))+ "," + rangeQueryRecords.get(j) +"," +rStarRangeQueryTimes.get(j)+ "," + seqScanRangeQueryTimes.get(j) + "\n");
                j++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("knnQueryResults.csv"))) {
            String tagString = "K" +
                    ',' +
                    "R* Time(ms)" +
                    ',' +
                    "Sequential Scan Time(ms)" +
                    '\n';
            writer.write(tagString);

            // Knn Query File creation
            int j = 0;
            while(j < knnRStarTimes.size()){
                writer.write((j + 1)*1000 + "," + knnRStarTimes.get(j)+ "," + knnSeqScanTimes.get(j) + "\n");
                j++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

