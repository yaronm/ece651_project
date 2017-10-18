package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by yy on 2017-10-18.
 */
public class FirebaseComunicationTest {
    private ConcreteBlackboard blackboard;
    public FirebaseComunication fb;

    @Test
    public void createGame() throws Exception {
        blackboard = new ConcreteBlackboard();

        blackboard.userName().set("test");

        Location loc = new Location(" ");
        loc.setLatitude(-0.193805);
        loc.setLongitude(-78.467102);
        blackboard.userLocation().set(loc);
        fb = new FirebaseComunication(blackboard);


        ArrayList<String> userIds = new ArrayList<>();
        userIds.add("3f64f35a8a19d4027c06d5c2e79b9ad526ef9");
        userIds.add("c6c7d29c4fe187d426cdab137016721518369");
        userIds.add("fb7922595ad9c210c0f3ce773f0cc8b9d8e21f3");
        userIds.add("40e3e87e1bba2e37a3e76e64364138a2159cac");
        userIds.add("a94a8fe5ccb19ba61c4c873d391e987982fbbd3");
        Map<String,ArrayList<String>> visibilities = new HashMap<String, ArrayList<String>>();
        ArrayList<String> vis = new ArrayList<String>();
        vis.add("3f64f35a8a19d4027c06d5c2e79b9ad526ef9");
        vis.add("c6c7d29c4fe187d426cdab137016721518369");
        vis.add("fb7922595ad9c210c0f3ce773f0cc8b9d8e21f3");
        vis.add("40e3e87e1bba2e37a3e76e64364138a2159cac");
        visibilities.put("a94a8fe5ccb19ba61c4c873d391e987982fbbd3", vis);
        ArrayList<String> vis2 = new ArrayList<String>();
        vis2.add("none");
        visibilities.put("3f64f35a8a19d4027c06d5c2e79b9ad526ef9", vis2);
        visibilities.put("c6c7d29c4fe187d426cdab137016721518369", vis2);
        visibilities.put("fb7922595ad9c210c0f3ce773f0cc8b9d8e21f3", vis2);
        visibilities.put("40e3e87e1bba2e37a3e76e64364138a2159cac", vis2);
        fb.createGame(userIds, visibilities, null, null, null);
        loc = new Location(" ");
        loc.setLatitude(0);
        loc.setLongitude(0);
        blackboard.userLocation().set(loc);
       while(true){
           Log.println(Log.INFO,"yaron","waiting");
       }
    }

}