package ca.uwaterloo.ece.ece651projectclient;

/**
 * A interface for an object to receive notifications when an observed blackboard is updated.
 */
public interface BlackboardObserver {

    /**
     * Notifies this object that a blackboard that it is observing has been updated.
     *
     * @param blackboard the blackboard that has been updated
     */
    public void onUpdate(Blackboard blackboard);

}
