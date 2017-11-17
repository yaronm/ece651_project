package ca.uwaterloo.ece.ece651projectclient;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * A concrete value event listener that (1) buffers received data snapshots and (2) blocks on
 * retrieval of said data snapshots until the initial snapshot is received.
 *
 * @see ValueEventListener
 */
public class BlockingValueEventListener implements ValueEventListener {
    volatile boolean initialSnapshotReceived = false;
    DataSnapshot snapshot;

    /**
     * Gets the current data snapshot. Blocks if none has yet been received.
     *
     * @return the current data snapshot
     */
    public synchronized DataSnapshot getSnapshot() {
        if (!initialSnapshotReceived) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return snapshot;
    }

    @Override
    public synchronized void onDataChange(DataSnapshot dataSnapshot) {
        snapshot = dataSnapshot;
        initialSnapshotReceived = true;
        this.notifyAll();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {}

}
