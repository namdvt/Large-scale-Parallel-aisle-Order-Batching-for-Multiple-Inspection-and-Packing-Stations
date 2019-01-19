import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nam on 1/11/2017.
 */
public class Order{
    private int id;
    private int[] position;
    private int[] position80;
    private List<Path> acceptedPathList;
    private int diff;
    private Path shortestPath;
    private int capa;
    private int cost;
    private double relatedness;

    public Order(int id) {
        this.id = id;
        acceptedPathList = new ArrayList<Path>();
    }

    public Order(int id, int[] position) {
        this.id = id;
        this.position = position;
        acceptedPathList = new ArrayList<Path>();
    }

    public Order(int id, int[] position, int[] position80) {
        this.id = id;
        this.position = position;
        this.position80 = position80;
        acceptedPathList = new ArrayList<Path>();
    }

    public Order(int id, int[] position, int[] position80, int capa) {
        this.id = id;
        this.position = position;
        this.position80 = position80;
        this.capa = capa;
    }

    public Order(int id, int[] position, int capa) {
        this.id = id;
        this.position = position;
        this.capa = capa;
    }

    public Order(int[] position) {
        this.position = position;
    }

    public int[] getPosition() {
        return position;
    }

    public List<Path> getAcceptedPathList() {
        return acceptedPathList;
    }

    public void setAcceptedPathList(List<Path> acceptedPathList) {
        this.acceptedPathList = acceptedPathList;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    public Path getShortestPath() {
        if (this.acceptedPathList.size() == 0) {
            return null;
        }
        if (this.shortestPath != null) {
            return this.shortestPath;
        } else {
            Path pathWithMinLength = acceptedPathList.get(0);
            for (Path path : acceptedPathList) {
                if (pathWithMinLength.getLength() > path.getLength()) {
                    pathWithMinLength = path;
                }
            }
            this.shortestPath = pathWithMinLength;
            return pathWithMinLength;
        }
    }

    public int getCapa() {
        return capa;
    }

    public void setCapa(int capa) {
        this.capa = capa;
    }

    public int getId() {
        return this.id;
    }

    public int[] getPosition80() {
        return position80;
    }

    public void setPosition80(int[] position80) {
        this.position80 = position80;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        return id == order.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public double getRelatedness() {
        return relatedness;
    }

    public void setRelatedness(double relatedness) {
        this.relatedness = relatedness;
    }
}
