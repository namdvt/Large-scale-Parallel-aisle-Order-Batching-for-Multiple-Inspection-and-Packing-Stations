import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Nguyen on 7/1/2017.
 */
public class Result {

        Type type;
        List<Batch> batchListResult = new ArrayList<Batch>();
        double totalLength;
        long executeTime;

        public Result(Type type, List<Batch> batchListResult, long executeTime) {
            this.type = type;
            this.batchListResult = batchListResult;
            this.totalLength = 0;
            this.executeTime = executeTime;
        }

        public Result(Type type, List<Batch> batchListResult, long executeTime, double totalLength) {
            this.type = type;
            this.batchListResult = batchListResult;
            this.totalLength = totalLength;
            this.executeTime = executeTime;
        }

        public List<Batch> getBatchListResult() {
            return batchListResult;
        }

        public double getTotalLength(List<Path> pathList) {
            if (totalLength != 0) {
                return this.totalLength;
            } else {
                for (Batch batch : batchListResult) {
                    int[] batchPath = {0, 0, 0, 0, 0, 0, 0, 0};

                    // Obtain batch Path
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
                    this.totalLength += path.getLength();
                    batch.setPath(path);
                }
                return totalLength;
            }
        }

        public long getExecuteTime() {
            return executeTime;
        }

        public Type getType() {
            return type;
        }

}
