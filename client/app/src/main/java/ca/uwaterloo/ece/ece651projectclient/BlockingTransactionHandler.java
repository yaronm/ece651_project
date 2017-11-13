package ca.uwaterloo.ece.ece651projectclient;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Transaction.Handler;

/**
 * A transaction handler that (1) buffers whether the transaction is committed and (2) blocks on
 * retrieval of said committed state.
 */
public abstract class BlockingTransactionHandler implements Handler {
    volatile boolean isCompleted = false;
    boolean committed;

    /**
     * Gets whether the transaction was committed. Blocks if the transaction is not yet complete.
     *
     * @return whether the transaction was committed
     */
    public synchronized boolean isCommitted() {
        if (!isCompleted) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return committed;
    }

    @Override
    public synchronized void onComplete(DatabaseError databaseError, boolean committed,
                           DataSnapshot dataSnapshot) {
        this.committed = committed;
        isCompleted = true;
        notifyAll();
    }
}
