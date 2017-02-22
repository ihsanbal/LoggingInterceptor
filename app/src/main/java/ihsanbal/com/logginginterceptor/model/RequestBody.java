package ihsanbal.com.logginginterceptor.model;

import android.support.v4.util.SparseArrayCompat;

/**
 * @author ihsan on 09/02/2017.
 */

public class RequestBody {
    private String header = "header";

    public SparseArrayCompat<Integer> sparseArray;

    public RequestBody() {
        sparseArray = new SparseArrayCompat<>(3);
        sparseArray.put(0, 1);
        sparseArray.put(1, 2);
        sparseArray.put(2, 3);
    }
}
