import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nam on 8/6/2017.
 */
public class Calculation {
    public static Path getPath(Batch batch, List<Path> pathList) {
        int[] batchPath = {0, 0, 0, 0, 0, 0};
        for (Order order : batch.getOrderList()) {
            for (int i = 0; i < 6; i++) {
                batchPath[i] |= order.getPosition()[i];
            }
        }

        // find accepted path for bach path
        List<Path> acceptedPathList = new ArrayList<Path>();
        for (Path path : pathList) {
            boolean isAccepted = true;
            for (int i = 0; i < 6; i++) {
                if (batchPath[i] > path.getPosition()[i]) {
                    isAccepted = false;
                    break;
                }
            }
            if (isAccepted) {
                acceptedPathList.add(path);
            }
        }

        // find path which shortest
        Collections.sort(acceptedPathList, new Comparator<Path>() {
            public int compare(Path o1, Path o2) {
                return o1.getLength() - o2.getLength();
            }
        });
        Path path = acceptedPathList.get(0);
        batch.setPath(path);
        return path;

    }

    public static int getTotalLength(List<Batch> batchList, List<Path> pathList) {
        int totalLength = 0;
        for (Batch batch : batchList) {
            totalLength += getPath(batch, pathList).getLength();
        }
        return totalLength;
    }
}
