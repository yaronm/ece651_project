package ca.uwaterloo.ece.ece651projectclient;

/**
 * A class representing a pair of polar coordinates.
 */
public class PolarCoordinates {

    /**
     * Constructs a new pair of polar coordinates.
     *
     * @param rho the radial coordinate or the distance from the reference point
     * @param phi the angular coordinate or the angle/bearing from the reference direction
     */
    public PolarCoordinates(float rho, float phi) {
        setRho(rho);
        setPhi(phi);
    }

    private float rho;

    /**
     * @return the radial coordinate
     */
    public float getRho() {
        return rho;
    }

    /**
     * Sets the radial coordinate.
     *
     * @param rho the radial coordinate or the distance from the reference point
     */
    public void setRho(float rho) {
        this.rho = rho;
    }

    private float phi;

    /**
     * @return the angular coordinate
     */
    public float getPhi() {
        return phi;
    }

    /**
     * Sets the angular coordinate.
     *
     * @param phi the angular coordinate or the angle/bearing from the reference direction
     */
    public void setPhi(float phi) {
        this.phi = phi;
    }

    @Override
    public String toString() {
        return getRho() + ", " + getPhi();
    }

}
