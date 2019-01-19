/**
 * Created by Nguyen on 6/18/2017.
 */
public enum Type {
    FIFO_PTS,
    FIFO_SWP,
    SEED_PTS,
    SEED_SWP,
    CW_PTS,
    CW_SWP,
    CPLEX_PTS,
    CPLEX_SWP,
    HYBRID_SWP,
    HYBRID_PTS,
    CPLEX_SWP_DUP,
    CPLEX_PTS_DUP,
    CPLEX_PTS_LB,
    CPLEX_SWP_LB,
    CPLEX_SWP_DUP_LB,
    CPLEX_PTS_DUP_LB,
    CPLEX_PTS_LLB,
    CPLEX_SWP_LLB,
    CPLEX_SWP_DUP_LLB,
    CPLEX_PTS_DUP_LLB;

    /**
     * Created by Nguyen on 7/1/2017.
     */
    public static interface Ordering {
        Result getResult();
    }
}
