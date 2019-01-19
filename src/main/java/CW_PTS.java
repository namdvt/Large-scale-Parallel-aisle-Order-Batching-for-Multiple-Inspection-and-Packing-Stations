import java.util.*;

/**
 * Created by Nguyen on 7/1/2017.
 */
public class CW_PTS implements Ordering {
    private List<Order> orderList;
    private List<Path> pathList;
    private int maxCapa;
    private int numAisle;

    public CW_PTS(List<Order> orderList, List<Path> pathList, int maxCapa, int numAisle) {
        this.orderList = orderList;
        this.pathList = pathList;
        this.maxCapa = maxCapa;
        this.numAisle = numAisle;
    }

    public Result getResult() {
        List<Batch> processingBatchList = new ArrayList<Batch>();
        List<Batch> batchList = new ArrayList<Batch>();
        long startTime = System.currentTimeMillis();

        // Execute ordering

        for (Order order : orderList) {
            processingBatchList.add(new Batch(order));
        }
        while (processingBatchList.size() > 0) {
            processingBatchList = getPairOfOrder_PTS(processingBatchList);
            if (processingBatchList == null) {
                break;
            }

            Collections.sort(processingBatchList, new Comparator<Batch>() {
                public int compare(Batch o1, Batch o2) {
                    return o2.getCapa() - o1.getCapa();
                }
            });

            //remove list
            List<Integer> removeList = new ArrayList<Integer>();

            for (int i = 0; i < processingBatchList.size() - 1; i++) {
                boolean f = true;
                for (int j = i + 1; j < processingBatchList.size(); j++) {
                    if (processingBatchList.get(i).getCapa() + processingBatchList.get(j).getCapa() < maxCapa) {
                        f = false;
                    }
                }
                if (f) {
                    removeList.add(i);
                }
            }

            // add to batchListResult
            try {
                for (int i = removeList.size() - 1; i >= 0; i--) {
                    batchList.add(processingBatchList.get(i));
                    processingBatchList.remove(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // add remain to batchListResult
            int totalCapaRemain = 0;
            for (Batch batch : processingBatchList) {
                totalCapaRemain += batch.getCapa();
            }
            if (totalCapaRemain <= maxCapa) {
                Batch lastBatch = new Batch();
                for (Batch batch : processingBatchList) {
                    lastBatch.addOrder(batch.getOrderList());
                }
                batchList.add(lastBatch);
                processingBatchList.removeAll(processingBatchList);
            }
        }
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;

        return new Result(Type.CW_PTS, batchList, executeTime);
    }

    private List<Batch> getPairOfOrder_PTS(List<Batch> processingBatchList) {
        int in = processingBatchList.size();

        List<Batch> pair = new ArrayList<Batch>();
        for (int i = 0; i < processingBatchList.size() - 1; i++) {
            for (int j = i + 1; j < processingBatchList.size(); j++) {
                if (countCapa(processingBatchList.get(i)) + countCapa(processingBatchList.get(j)) <= maxCapa) {
                    pair.add(new Batch(processingBatchList.get(i), processingBatchList.get(j)));
                }
            }
        }

        Batch selectedBatch = pair.get(0);
        int saving1 = getShortedAcceptedPathForOrders(selectedBatch.getOrderList()).getLength();
        for (Batch batch : pair) {

            int saving2 = getShortedAcceptedPathForOrders(batch.getOrderList()).getLength();
            if ((saving1 > saving2) || (saving1 == saving2 && selectedBatch.getOrderList().size() < batch.getOrderList().size())) {
                selectedBatch = batch;
                saving1 = saving2;
            }
        }


        Iterator i = processingBatchList.iterator();
        while (i.hasNext()) {
            Batch batch = (Batch) i.next();
            for (Order order : selectedBatch.getOrderList()) {
                if (batch.getOrderList().contains(order)) {
                    i.remove();
                    break;
                }
            }
        }
        processingBatchList.add(selectedBatch);

        return processingBatchList;
    }

    private Path getShortedAcceptedPathForOrders(List<Order> orders) {
        // Get shortest Path or Orders in Batch
        int[] shortestPath = {0, 0, 0, 0, 0, 0, 0, 0};
        for (Order order : orders) {
            for (int i = 0; i < numAisle; i++) {
                shortestPath[i] |= order.getPosition()[i];
            }
        }

        // Get accepted paths
        List<Path> acceptedPathList = new ArrayList<Path>();
        for (Path path : pathList) {
            boolean isAccepted = true;
            for (int i = 0; i < numAisle; i++) {
                if (shortestPath[i] > path.getPosition()[i]) {
                    isAccepted = false;
                    break;
                }
            }
            if (isAccepted) {
                acceptedPathList.add(path);
            }
        }

        // Get shortest form accepted paths and print
        Path shortestAcceptedPath = acceptedPathList.get(0);
        for (Path path : acceptedPathList) {
            if (shortestAcceptedPath.getLength() > path.getLength()) {
                shortestAcceptedPath = path;
            }
        }
        return shortestAcceptedPath;
    }

    public static int countCapa(Batch batch) {
        int capa = 0;
        for (Order order : batch.getOrderList()) {
            capa += order.getCapa();
        }
        return capa;
    }

}
