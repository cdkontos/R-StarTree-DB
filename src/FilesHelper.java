import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class FilesHelper {
    private static final String DELIMITER = ","; // The separator of the strings in the csv file
    private static final String PATH_TO_CSV = "map.csv";
    static final String PATH_TO_DATAFILE = "datafile.dat";
    private static final String PATH_TO_INDEXFILE = "indexfile.dat";
    private static final int BLOCK_SIZE = 32 * 1024; // Block size: 32KB
    private static int dataDimensions; // The data's used dimensions
    private static int totalBlocksInDatafile;  // The total blocks written in the datafile
    private static int totalBlocksInIndexFile; // The total blocks written in the indexfile
    private static int totalLevelsOfTreeIndex; // The total levels of the R* tree


    static String getPathToCsv() {return PATH_TO_CSV;}

    static String getDELIMITER() {return DELIMITER;}

    static int getDataDimensions() {return dataDimensions;}

    static int getTotalBlocksInDatafile() {return totalBlocksInDatafile;}

    static int getTotalBlocksInIndexFile() {
        return totalBlocksInIndexFile;
    }

    static int getTotalLevelsOfTreeIndex() {return totalLevelsOfTreeIndex;}

    // Serializing a serializable Object to byte array
    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    // Deserializing a byte array to a serializable Object
    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    // Metadata methods

    // Reads the data from the given file path
    // Reads Block 0, which contains metadata information about the file
    // Returns an ArrayList<Integer> containing metadata values
    private static ArrayList<Integer> readMetaDataBlock(String pathToFile){
        try {
            // Open the file for reading and create input streams
            RandomAccessFile raf = new RandomAccessFile(new File(pathToFile), "rw");
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);

            // Create a byte array to hold the entire block
            byte[] block = new byte[BLOCK_SIZE];

            // Read the entire block from the file into the 'block' array
            // Verify that the block size read matches the expected size
            if (bis.read(block, 0, BLOCK_SIZE) != BLOCK_SIZE) {
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");
            }

            // Serialize an integer to determine the size of the 'goodPutLength' in bytes
            byte[] goodPutLengthInBytes = serialize(new Random().nextInt());

            // Copy a portion of the 'block' array to get the serialized 'goodPutLength'
            // This portion is the same size as the serialized integer
            System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

            // Deserialize the 'goodPutLength' to obtain the actual data size
            byte[] dataInBlock = new byte[(Integer) deserialize(goodPutLengthInBytes)];

            // Copy the remaining data from the 'block' array to 'dataInBlock'
            // This data represents the metadata values
            System.arraycopy(block, goodPutLengthInBytes.length, dataInBlock, 0, dataInBlock.length);

            // Deserialize the 'dataInBlock' to obtain the ArrayList of Integer metadata values
            return (ArrayList<Integer>) deserialize(dataInBlock);

        } catch (Exception e) {
            // Handle any exceptions that may occur during the process
            e.printStackTrace();
        }

        // Return null in case of errors
        return null;
    }

    // Updates the metadata block in the specified file (block size, data dimensions, total block0 blocks)
    private static void updateMetaDataBlock(String pathToFile) {
        try {
            // Create an ArrayList to store metadata values
            ArrayList<Integer> dataFileMetaData = new ArrayList<>();

            // Add metadata values to the ArrayList
            dataFileMetaData.add(dataDimensions); // Data dimensions
            dataFileMetaData.add(BLOCK_SIZE); // Block size

            // Depending on the file type, update different metadata values
            if (pathToFile.equals(PATH_TO_DATAFILE)) {
                // Increment and add the total blocks in the data file
                dataFileMetaData.add(++totalBlocksInDatafile);
            } else if (pathToFile.equals(PATH_TO_INDEXFILE)) {
                // Increment and add the total blocks in the index file
                dataFileMetaData.add(++totalBlocksInIndexFile);

                // Add the total levels of the tree index
                dataFileMetaData.add(totalLevelsOfTreeIndex);
            }

            // Serialize the metadata ArrayList and its size to bytes
            byte[] metaDataInBytes = serialize(dataFileMetaData);
            // Calculate the length of 'metaDataInBytes'
            int metaDataLength = metaDataInBytes.length;

            // Check if the metadata length exceeds the block size
            if (metaDataLength > BLOCK_SIZE) {
                throw new IllegalArgumentException("Metadata length exceeds block size");
            }
            byte[] goodPutLengthInBytes = serialize(metaDataLength);

            // Create a byte array to hold the entire block
            byte[] block = new byte[BLOCK_SIZE];

            // Copy the serialized metadata and its length to the 'block'
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(metaDataInBytes, 0, block, goodPutLengthInBytes.length, metaDataInBytes.length);

            // Open the specified file for writing
            RandomAccessFile f = new RandomAccessFile(new File(pathToFile), "rw");

            // Write the updated metadata block to the file
            f.write(block);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // Datafile methods

    // Reads and then adds the data from the map.csv to the datafile
    // Calculates the number of total blocks in the datafile
    static void initializeDataFile(int dataDimensions, boolean makeNewDataFile) {
        try {
            // Checks if a datafile already exists, initialize the metaData from the metadata block (block 0 of the file)
            // If it already exists, initialize the variables with the values of the dimensions, block size, and total blocks of the data file
            if (!makeNewDataFile && Files.exists(Paths.get(PATH_TO_DATAFILE))) {
                ArrayList<Integer> dataFileMetaData = readMetaDataBlock(PATH_TO_DATAFILE);
                if (dataFileMetaData == null)
                    throw new IllegalStateException("Could not read datafile's Meta Data Block properly");
                FilesHelper.dataDimensions = dataFileMetaData.get(0);
                if (FilesHelper.dataDimensions <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                if (dataFileMetaData.get(1) != BLOCK_SIZE)
                    throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");
                totalBlocksInDatafile = dataFileMetaData.get(2);
                if (totalBlocksInDatafile < 0)
                    throw new IllegalStateException("The total blocks of the datafile cannot be a negative number");
            }
            // Else initialize a new datafile
            else {
                Files.deleteIfExists(Paths.get(PATH_TO_DATAFILE)); // Resetting/Deleting dataFile data
                FilesHelper.dataDimensions = dataDimensions;
                if (FilesHelper.dataDimensions <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                updateMetaDataBlock(PATH_TO_DATAFILE);

                // Create a list to hold record bytes for the current block
                List<byte[]> blockRecords = new ArrayList<>();
                BufferedReader csvReader = new BufferedReader(new FileReader(PATH_TO_CSV)); // BufferedReader used to read the data from the CSV file
                String stringRecord; // String used to read each line (row) of the CSV file
                int maxRecordsInBlock = calculateMaxRecordsInBlock();

                while ((stringRecord = csvReader.readLine()) != null) {
                    byte[] recordBytes = stringRecord.getBytes(); // Convert the CSV record to bytes

                    if (blockRecords.size() == maxRecordsInBlock) {
                        // Write the block to the data file
                        writeDataFileBlock(PATH_TO_DATAFILE, blockRecords);
                        blockRecords = new ArrayList<>();
                    }
                    blockRecords.add(recordBytes);
                }
                csvReader.close();

                if (!blockRecords.isEmpty()) {
                    // Write the remaining records as the final block
                    writeDataFileBlock(PATH_TO_DATAFILE, blockRecords);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Calculates and return an integer which represents the maximum number of records a block of BLOCK_SIZE can have
    private static int calculateMaxRecordsInBlock() {
        ArrayList<Record> blockRecords = new ArrayList<>();
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            ArrayList<Double> coordinateForEachDimension = new ArrayList<>();
            for (int d = 0; d < FilesHelper.dataDimensions; d++)
                coordinateForEachDimension.add(0.0);
            Record record = new Record(0, coordinateForEachDimension);
            blockRecords.add(record);
            byte[] recordInBytes = new byte[0];
            byte[] goodPutLengthInBytes = new byte[0];
            try {
                recordInBytes = serialize(blockRecords);
                goodPutLengthInBytes = serialize(recordInBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (goodPutLengthInBytes.length + recordInBytes.length > BLOCK_SIZE)
                break;
        }
        return i;
    }

    // Used for writing and saving an array of records as a new block of bytes in the datafile
    static void writeDataFileBlock(String dataFilePath, List<byte[]> records) {
        try {

            // Serialize the list of records and its length to bytes
            byte[] recordInBytes = serialize(records);
            byte[] goodPutLengthInBytes = serialize(recordInBytes.length);

            // Create a block of bytes to write
            byte[] block = new byte[BLOCK_SIZE];

            // Copy the length of the record to the block
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);

            // Copy the serialized record data to the block
            System.arraycopy(recordInBytes, 0, block, goodPutLengthInBytes.length, recordInBytes.length);

            // Open the data file in append mode
            FileOutputStream fos = new FileOutputStream(dataFilePath, true);
            BufferedOutputStream bout = new BufferedOutputStream(fos);

            // Write the block to the data file
            bout.write(block);

            bout.close();
            fos.close();

            // Update metadata block in the data file
            updateMetaDataBlock(dataFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a specific block of data from the data file.
     *
     * @param blockId The identifier of the block to read.
     * @return An ArrayList of Record objects containing the data from the specified block.
     */
    static ArrayList<Record> readDataFileBlock(int blockId) {
        try {
            // Open a RandomAccessFile for reading the data file.
            RandomAccessFile raf = new RandomAccessFile(new File(PATH_TO_DATAFILE), "rw");

            // Create a FileInputStream and a BufferedInputStream to efficiently read from the file.
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);

            // Seek to the specified block within the file by calculating the byte offset.
            raf.seek((long) blockId * BLOCK_SIZE);

            // Read the block of data from the file into a byte array called 'block'.
            byte[] block = new byte[BLOCK_SIZE];

            // Ensure that the read block is of the expected size (BLOCK_SIZE).
            if (bis.read(block, 0, BLOCK_SIZE) != BLOCK_SIZE) {
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");
            }

            // Serialize a random integer to determine the size of the metadata (goodPutLength).
            byte[] goodPutLengthInBytes = serialize(new Random().nextInt());

            // Copy the serialized metadata size from the beginning of the block.
            System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

            // Calculate the size of the serialized records based on goodPutLength.
            byte[] recordsInBlock = new byte[(Integer) deserialize(goodPutLengthInBytes)];

            // Copy the serialized records from the block.
            System.arraycopy(block, goodPutLengthInBytes.length, recordsInBlock, 0, recordsInBlock.length);

            // Deserialize the recordsInBlock byte array into an ArrayList of Record objects.
            return (ArrayList<Record>) deserialize(recordsInBlock);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null in case of errors or if the block could not be read.
    }




    // Indexfile methods


    // Calculates and returns an estimate of the maximum number of entries (records) that can fit
    // within a block of size BLOCK_SIZE. This estimation is based on the size of serialized entries.
    static int calculateMaxEntriesInNode() {
        // Create an array to store random entries
        ArrayList<Entry> randomEntries = new ArrayList<>();

        int i; // Initialize a counter for the loop

        // Start a loop to generate random entries until the cumulative size exceeds BLOCK_SIZE
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            // Create random bounds for each dimension and a child node ID
            ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
            for (int d = 0; d < FilesHelper.dataDimensions; d++)
                boundsForEachDimension.add(new Bounds(0.0, 0.0));

            // Create a random leaf entry with bounds and child node ID
            Entry entry1 = new LeafEntry(new Random().nextLong(), new Random().nextLong(), boundsForEachDimension);
            entry1.setChildNodeBlockId(new Random().nextLong());

            // Serialize the entry into bytes
            byte[] nodeInBytes = new byte[0];
            byte[] goodPutBytes = new byte[0];
            try {
                nodeInBytes = serialize(new Node(new Random().nextInt(), randomEntries));
                goodPutBytes = serialize(nodeInBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Check if adding this entry exceeds the block size
            if (goodPutBytes.length + nodeInBytes.length > BLOCK_SIZE)
                break;
        }
        return i;
    }


    // Updates the metadata block in the indexFile with an increased level of the tree index.
    // This method saves the current data dimensions, block size, total blocks in the index file,
    // and increments the total levels of the tree index. It is typically used to update the
    // R* tree index metadata when a new level is added.
    private static void updateLevelsOfTreeInIndexFile() {
        try {
            // Create an ArrayList to store metadata values
            ArrayList<Integer> dataFileMetaData = new ArrayList<>();

            // Add the current data dimensions, block size, total blocks in the index file,
            // and increment the total levels of the tree index
            dataFileMetaData.add(dataDimensions);
            dataFileMetaData.add(BLOCK_SIZE);
            dataFileMetaData.add(totalBlocksInIndexFile);
            dataFileMetaData.add(++totalLevelsOfTreeIndex);

            // Serialize the metadata into bytes
            byte[] metaDataInBytes = serialize(dataFileMetaData);

            // Serialize the length of metadata bytes for later retrieval
            byte[] goodPutLengthInBytes = serialize(metaDataInBytes.length);

            // Create a block of size BLOCK_SIZE and copy serialized metadata into it
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);
            System.arraycopy(metaDataInBytes, 0, block, goodPutLengthInBytes.length, metaDataInBytes.length);

            // Open the indexFile in read-write mode
            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_INDEXFILE), "rw");

            // Write the updated metadata block to the indexFile
            f.write(block);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Initializes the indexFile,which stores metadata for the R* tree index,
     * calculating the total number of blocks in the index file and reads the metadata considering that the file exists
     * If the file doesn't exist, a new indexFile with the specified data dimensions is created
     *
     * @param dataDimensions: The number of dimensions in the data.
     * @param makeNewDataFile: A flag indicating whether to create a new indexFile or use an existing one.
     */
    static void initializeIndexFile(int dataDimensions, boolean makeNewDataFile) {
        try {
            // Checks if an indexFile already exists, and if so, reads its metadata block (block 0).
            // Initializes the data dimensions, block size, total blocks, and levels (height) of the R* tree index.
            if (!makeNewDataFile && Files.exists(Paths.get(PATH_TO_INDEXFILE))) {
                ArrayList<Integer> indexFileMetaData = readMetaDataBlock(PATH_TO_INDEXFILE);

                // Ensure that the metadata block is read properly
                if (indexFileMetaData == null)
                    throw new IllegalStateException("Could not read indexFile's Meta Data Block properly");

                // Set data dimensions, block size, total blocks, and levels of the tree index
                FilesHelper.dataDimensions = indexFileMetaData.get(0);
                if (FilesHelper.dataDimensions  <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");

                // Check if block size matches
                if (indexFileMetaData.get(1) != BLOCK_SIZE)
                    throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");

                totalBlocksInIndexFile = indexFileMetaData.get(2);
                if (totalBlocksInIndexFile  < 0)
                    throw new IllegalStateException("The total blocks of the index file cannot be a negative number");

                totalLevelsOfTreeIndex = indexFileMetaData.get(3);
                if (totalLevelsOfTreeIndex  < 0)
                    throw new IllegalStateException("The total index's tree levels cannot be a negative number");
            }
            // If the indexFile does not exist or a new one is to be created, initialize a new indexFile.
            else {
                // Reset or delete the existing index file data
                Files.deleteIfExists(Paths.get(PATH_TO_INDEXFILE));

                // Set data dimensions and initialize the total levels of the tree index (root level)
                FilesHelper.dataDimensions = dataDimensions;
                totalLevelsOfTreeIndex = 1; // The root (top level) will always have the highest level

                if (FilesHelper.dataDimensions  <= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");

                // Update the metadata block for the new indexFile
                updateMetaDataBlock(PATH_TO_INDEXFILE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Used for writing and saving the provided R* tree node as a new block of bytes in the indexfile
     * This method works in an append-only fashion, adding the node to the end of the file.
     * @param node: The R* tree node to be written to the indexFile.
     */
    static void writeNewIndexFileBlock(Node node) {
        try {
            // Serialize the node into bytes
            byte[] nodeInBytes = serialize(node);

            // Serialize the length of the node's byte representation
            byte[] goodPutLengthInBytes = serialize(nodeInBytes.length);

            // Create a byte block to store the serialized node
            byte[] block = new byte[BLOCK_SIZE];

            // Copy the length of the serialized node into the block
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);

            // Copy the serialized node data into the block
            System.arraycopy(nodeInBytes, 0, block, goodPutLengthInBytes.length, nodeInBytes.length);

            // Open the indexFile for appending
            FileOutputStream fos = new FileOutputStream(PATH_TO_INDEXFILE, true);
            BufferedOutputStream bout = new BufferedOutputStream(fos);

            // Write the block to the indexFile
            bout.write(block);

            // Update the metadata block in the indexFile to reflect the changes
            updateMetaDataBlock(PATH_TO_INDEXFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Updates the indexFile block with the provided serialized R* tree node data.
     * If the node's block ID is the root node's block ID and the given totalLevelsOfTreeIndex
     * differs from the current totalLevelsOfTreeIndex, it increases the totalLevelsOfTreeIndex by one.
     * @param node: The R* tree node containing the updated data.
     * @param totalLevelsOfTreeIndex: The total levels (height) of the R* tree index.
     */
    static void updateIndexFileBlock(Node node, int totalLevelsOfTreeIndex) {
        try {
            // Serialize the provided R* tree node into bytes
            byte[] nodeInBytes = serialize(node);

            // Serialize the length of the serialized node
            byte[] goodPutLengthInBytes = serialize(nodeInBytes.length);

            // Create a byte block to store the serialized node
            byte[] block = new byte[BLOCK_SIZE];

            // Copy the length of the serialized node into the block
            System.arraycopy(goodPutLengthInBytes, 0, block, 0, goodPutLengthInBytes.length);

            // Copy the serialized node data into the block
            System.arraycopy(nodeInBytes, 0, block, goodPutLengthInBytes.length, nodeInBytes.length);

            // Open the indexFile for read and write operations
            RandomAccessFile f = new RandomAccessFile(new File(PATH_TO_INDEXFILE), "rw");

            // Move the file pointer to the position of the node's block in the indexFile
            f.seek(node.getBlockID() * BLOCK_SIZE);

            // Write the block containing the updated node data to the indexFile
            f.write(block);
            f.close();

            // If the updated node is the root node and the totalLevelsOfTreeIndex has changed,
            // update the totalLevelsOfTreeIndex in the indexFile
            if (node.getBlockID() == RStarTree.getRootNodeBlockId() && FilesHelper.totalLevelsOfTreeIndex != totalLevelsOfTreeIndex)
                updateLevelsOfTreeInIndexFile();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Reads an indexFile block specified by its block ID and returns the deserialized R* tree node.
     * @param blockId: The unique identifier of the block to be read from the indexFile.
     * @return A deserialized R* tree node containing the data read from the block, or null if an error occurs.
    */
     static Node readIndexFileBlock(long blockId) {
        try {
            // Open the indexFile for read and write operations
            RandomAccessFile raf = new RandomAccessFile(new File(PATH_TO_INDEXFILE), "rw");

            // Create an input stream for reading from the indexFile
            FileInputStream fis = new FileInputStream(raf.getFD());

            // Create a buffered input stream for efficient reading
            BufferedInputStream bis = new BufferedInputStream(fis);

            // Move the file pointer to the position of the specified block in the indexFile
            raf.seek(blockId * BLOCK_SIZE);

            // Create a byte array to store the block data
            byte[] block = new byte[BLOCK_SIZE];

            // Read the block data from the indexFile into the byte array
            if (bis.read(block, 0, BLOCK_SIZE) != BLOCK_SIZE)
                throw new IllegalStateException("Block size read was not of " + BLOCK_SIZE + " bytes");

            // Serialize an integer to determine the size of the "goodPutLength" in bytes
            byte[] goodPutLengthInBytes = serialize(new Random().nextInt());

            // Copy the "goodPutLength" bytes from the block data
            System.arraycopy(block, 0, goodPutLengthInBytes, 0, goodPutLengthInBytes.length);

            // Create a byte array to store the deserialized node data
            byte[] nodeInBytes = new byte[(Integer) deserialize(goodPutLengthInBytes)];

            // Copy the deserialized node data from the block data
            System.arraycopy(block, goodPutLengthInBytes.length, nodeInBytes, 0, nodeInBytes.length);

            // Deserialize the node data to reconstruct the R* tree node
            return (Node) deserialize(nodeInBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }







}
