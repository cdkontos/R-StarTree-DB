import java.io.Serializable;
import java.util.ArrayList;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
/**
 * A class representing a bounding box in an n-dimensional space. This bounding box is used for spatial indexing and querying.
 * @author Christos Kontos
 */
public class BoundingBox implements Serializable {
    private final ArrayList<Bounds> bounds;
    private Double area;
    private Double perimeter;
    private ArrayList<Double> center;

    /**
     * Constructs a bounding box with the given bounds in each dimension.
     * @param bounds An ArrayList of Bounds, where each Bounds object represents the range of a dimension.
     */
    public BoundingBox(ArrayList<Bounds> bounds) {
        this.bounds = bounds;
        this.area = getArea();
        this.perimeter = getPerimeter();
        this.center = getCenter();
    }

    /**
     * Gets the bounds of this bounding box.
     * @return An ArrayList of Bounds, where each Bounds object represents the range of a dimension.
     */
    public ArrayList<Bounds> getBounds() {
        return bounds;
    }

    /**
     * Calculates and retrieves the area of the bounding box.
     * @return The calculated area of the bounding box.
     */
    public Double getArea() {
        if(area == null)
            area = calcArea();
        return area;
    }

    /**
     * Calculates and retrieves the perimeter of the bounding box.
     * @return The calculated perimeter of the bounding box.
     */
    public Double getPerimeter() {
        if(perimeter == null)
            perimeter = calcPerimeter();
        return perimeter;
    }

    /**
     * Calculates and retrieves the center coordinates of the bounding box.
     * @return An ArrayList containing the center coordinates for each dimension.
     */
    public ArrayList<Double> getCenter() {
        if(center == null)
            center = calcCenter();
        return center;
    }


    private double calcArea()
    {
        double product = 1;
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            product = product * (bounds.get(i).getUpper() - bounds.get(i).getLower());
        }
        return abs(product);
    }
    private double calcPerimeter()
    {
        double sum = 0;
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            sum += abs(bounds.get(i).getUpper() - bounds.get(i).getLower());
        }
        return sum;
    }
    private ArrayList<Double> calcCenter()
    {
        ArrayList<Double> centers = new ArrayList<>();
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            centers.add((bounds.get(i).getUpper() + bounds.get(i).getLower())/2);
        }
        return centers;
    }

    /**
     * Finds the minimum distance from a point to the bounding box.
     * @param point The point for which the minimum distance is calculated.
     * @return The minimum distance from the point to the bounding box.
     */
    double findMinPointDistance(ArrayList<Double> point)
    {
        double minDist = 0;
        double rd;
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            if(getBounds().get(i).getLower() > point.get(i))
            {
                rd = getBounds().get(i).getLower();
            }
            else if (getBounds().get(i).getUpper() < point.get(i))
            {
                rd = getBounds().get(i).getUpper();
            }
            else
                rd = point.get(i);

            minDist += Math.pow(point.get(i) - rd,2);
        }
        return sqrt(minDist);
    }

    /**
     * Checks if two bounding boxes overlap.
     * @param boundingBoxA The first bounding box.
     * @param boundingBoxB The second bounding box.
     * @return True if the two bounding boxes overlap, otherwise false.
     */
    static boolean checkBoxOverlap(BoundingBox boundingBoxA, BoundingBox boundingBoxB)
    {
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            double overlapD = Math.min(boundingBoxA.getBounds().get(i).getUpper(), boundingBoxB.getBounds().get(i).getUpper())
                    - Math.max(boundingBoxA.getBounds().get(i).getLower(),boundingBoxB.getBounds().get(i).getLower());

            if(overlapD < 0) //TODO CHECK "="???
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the overlap value between two bounding boxes.
     * @param boundingBoxA The first bounding box.
     * @param boundingBoxB The second bounding box.
     * @return The calculated overlap value between the two bounding boxes.
     */
    static double calcOverlapVal(BoundingBox boundingBoxA, BoundingBox boundingBoxB)
    {
        double overlapVal = 1;
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            double overlapD = Math.min(boundingBoxA.getBounds().get(i).getUpper(), boundingBoxB.getBounds().get(i).getUpper())
                    - Math.max(boundingBoxA.getBounds().get(i).getLower(),boundingBoxB.getBounds().get(i).getLower());

            if(overlapD <= 0) //TODO CHECK "="???
            {
                return 0;
            }
            else
                overlapVal = overlapD*overlapVal;
        }
        return overlapVal;
    }

    /**
     * Calculates the distance between the centers of two bounding boxes.
     * @param boundingBoxA The first bounding box.
     * @param boundingBoxB The second bounding box.
     * @return The distance between the centers of the two bounding boxes.
     */
    static double findBoundBoxDist(BoundingBox boundingBoxA, BoundingBox boundingBoxB)
    {
        double dist = 0;
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            dist += Math.pow(boundingBoxA.getCenter().get(i) - boundingBoxB.getCenter().get(i),2);
        }
        return sqrt(dist);
    }
}
