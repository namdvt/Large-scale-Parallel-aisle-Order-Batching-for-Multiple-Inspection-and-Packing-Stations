import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Nguyen on 7/1/2017.
 */
public class CW_SWP implements Ordering {
    private List<Order> orderList;
    private List<Path> pathList;
    private int maxOrder;
    private int minNumberOfOrder = 0;
    private int numAisle;

    public CW_SWP(List<Order> orderList, List<Path> pathList, int maxOrder, int numAisle) {
        this.orderList = orderList;
        this.pathList = pathList;
        this.maxOrder = maxOrder;
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
            processingBatchList = getPairOfOrder_SWP(processingBatchList);
            if (processingBatchList == null) {
                break;
            }
            if (processingBatchList.size() == 1 && processingBatchList.get(0).getOrderList().size() <= maxOrder) {
                batchList.add(processingBatchList.get(0));
                break;

            }
            Iterator i = processingBatchList.iterator();
            while (i.hasNext()) {
                Batch batch = (Batch) i.next();
                if (batch.getOrderList().size() == maxOrder) {
                    batchList.add(batch);
                    i.remove();
                }
            }
        }
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;

        return new Result(Type.CW_SWP, batchList, executeTime);
    }

    private List<Batch> getPairOfOrder_SWP(List<Batch> processingBatchList) {
        int in = processingBatchList.size();
        // Check number of orders, if <=maxOrder return only one bach
        int num = 0;
        for (Batch batch : processingBatchList) {
            num += batch.getOrderList().size();
        }
        if (num <= maxOrder) {
            Batch lastBatch = new Batch();
            for (Batch batch : processingBatchList) {
                lastBatch.addOrder(batch.getOrderList());
            }
            List<Batch> listWithOneLastBach = new ArrayList<Batch>();
            listWithOneLastBach.add(lastBatch);
            return listWithOneLastBach;
        }


        List<Batch> pair = new ArrayList<Batch>();
        for (int i = 0; i < processingBatchList.size() - 1; i++) {
            for (int j = i + 1; j < processingBatchList.size(); j++) {
                if (processingBatchList.get(i).getOrderList().size()
                        + processingBatchList.get(j).getOrderList().size() <= maxOrder
                        && processingBatchList.get(i).getOrderList().size()
                        + processingBatchList.get(j).getOrderList().size() > minNumberOfOrder) {
                    pair.add(new Batch(processingBatchList.get(i), processingBatchList.get(j)));
                }
            }
        }

        if (pair.size() == 0) {
            Batch separatedBatch = processingBatchList.get(0);
            for (Batch batch : processingBatchList) {
                if (batch.getOrderList().size() < separatedBatch.getOrderList().size()) {
                    separatedBatch = batch;
                }
            }

            minNumberOfOrder = 2;
            processingBatchList.remove(separatedBatch);

            for (Order order : separatedBatch.getOrderList()) {
                processingBatchList.add(new Batch(order));
            }

            return processingBatchList;
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
        int[] shortestPath = {0, 0, 0, 0, 0, 0};
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

}
