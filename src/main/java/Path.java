/**
 * Created by nam on 12/6/2016.
 */
public class Path {
    private int depot;
    private int[] position;
    private int length;

    public Path(int depot, int[] position, int length) {
        this.depot = depot;
        this.position = position;
        this.length = length;
    }

    public int[] getPosition() {
        return position;
    }

    public int getLength() {
        return length;
    }

    public String printPosition() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < position.length; i++) {
            sb.append(" " + position[i]);
        }
        sb.append(" Length: " + this.length);
        return sb.toString();
    }

    public int getDepot() {
        return depot;
    }
}
