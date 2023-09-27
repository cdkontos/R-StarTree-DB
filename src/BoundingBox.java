import java.io.Serializable;
import java.util.ArrayList;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class BoundingBox implements Serializable {
    private final ArrayList<Bounds> bounds;
    private Double area;
    private Double perimeter;
    private ArrayList<Double> center;

    public BoundingBox(ArrayList<Bounds> bounds) {
        this.bounds = bounds;
        this.area = getArea();
        this.perimeter = getPerimeter();
        this.center = getCenter();
    }

    public ArrayList<Bounds> getBounds() {
        return bounds;
    }

    public Double getArea() {
        if(area == null)
            area = calcArea();
        return area;
    }

    public Double getPerimeter() {
        if(perimeter == null)
            perimeter = calcPerimeter();
        return perimeter;
    }

    public ArrayList<Double> getCenter() {
        if(center == null)
            center = calcCenter();
        return center;
    }

    private double calcArea()
    {
        double product = 1;
        for (int i = 0; i < 1; i++) //TODO ADD FILES
        {
            product = product * (bounds.get(i).getUpper() - bounds.get(i).getLower());
        }
        return abs(product);
    }
    private double calcPerimeter()
    {
        double sum = 0;
        for (int i = 0; i < 1; i++) //TODO ADD FILES
        {
            sum += abs(bounds.get(i).getUpper() - bounds.get(i).getLower());
        }
        return sum;
    }
    private ArrayList<Double> calcCenter()
    {
        ArrayList<Double> centers = new ArrayList<>();
        for (int i = 0; i < 1; i++) //TODO ADD FILES
        {
            centers.add((bounds.get(i).getUpper() + bounds.get(i).getLower())/2);
        }
        return centers;
    }

    boolean checkPointOverlap(ArrayList<Double> point, double radius)
    {
        return findMinPointDistance(point) <= radius;
    }
    double findMinPointDistance(ArrayList<Double> point)
    {
        double minDist = 0;
        double rd;
        for (int i = 0; i < 1; i++) //TODO ADD FILES
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

    static boolean checkBoxOverlap(BoundingBox boundingBoxA, BoundingBox boundingBoxB)
    {
        for (int i = 0; i < 1; i++) //TODO ADD FILES
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

    static double calcOverlapVal(BoundingBox boundingBoxA, BoundingBox boundingBoxB)
    {
        double overlapVal = 1;
        for (int i = 0; i < 1; i++) //TODO ADD FILES
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

    static double findBoundBoxDist(BoundingBox boundingBoxA, BoundingBox boundingBoxB)
    {
        double dist = 0;
        for (int i = 0; i < 1; i++) //TODO ADD FILES
        {
            dist += Math.pow(boundingBoxA.getCenter().get(i) - boundingBoxB.getCenter().get(i),2);
        }
        return sqrt(dist);
    }
}
