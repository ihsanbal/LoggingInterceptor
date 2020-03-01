package ihsanbal.com.logginginterceptor.model

import androidx.collection.SparseArrayCompat

/**
 * @author ihsan on 09/02/2017.
 */
class Body {
    var sparseArray: SparseArrayCompat<Int> = SparseArrayCompat(3)

    init {
        sparseArray.put(0, 1)
        sparseArray.put(1, 2)
        sparseArray.put(2, 3)
    }
}