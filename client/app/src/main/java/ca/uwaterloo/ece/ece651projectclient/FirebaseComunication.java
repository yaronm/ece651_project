package ca.uwaterloo.ece.ece651projectclient;

/**
 * Created by yy on 2017-10-17.
 */
import android.location.Location;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Set;

public class FirebaseComunication {
    private DatabaseReference mDatabase;
    private String userId;
    private ConcreteBlackboard bb;
    private ArrayList<ValueEventListener> listeners;

    public FirebaseComunication(ConcreteBlackboard bb){
        //user name must exist before this can be created
        this.bb = bb;
        listeners = new ArrayList<>();
        // get access to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //need to register to be notified with Blackboard Observer
        bb.userName().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                createUser();
                mDatabase.child("users").child(userId).child("games").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        join_game((String)dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //do nothing
                    }
                });

            }
        });
        bb.userLocation().addObserver(new Observer(){
            @Override
            public void update(Observable observable, Object o) {
                New_location();
            }
        });


    }

    protected void createUser(){ //to be changed when change to implementation with authentication
        MessageDigest md;
        userId = ""; //to be changed when get blackboard interface
        try {
            md = MessageDigest.getInstance("SHA");
            md.update(bb.userName().value().getBytes());
            byte messageDigest[] = md.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            userId = hexString.toString();
        }catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (!userId.equals("")) {
            ArrayList<Double> loc= new ArrayList<>();
            //ArrayList<String> games = new ArrayList<String>(); use if decide to enable multiple games
            //games.add("none");
            loc.add(bb.userLocation().value().getLatitude());
            loc.add(bb.userLocation().value().getLongitude());

            mDatabase.child("users").child(userId).child("location").setValue(loc);
            //mDatabase.child("users").child(userId).child("tags").setValue(0);
            mDatabase.child("users").child(userId).child("games").setValue("none");
        }
    }


    protected void createGame(ArrayList<String> userIds, Map<String,ArrayList<String>> visibilities,
                              Map<String,Integer> initial_tags, Date end, Circle boundary){
        ArrayList<String> none_str = new ArrayList<String>();
        none_str.add("none");
        if (visibilities == null){
            visibilities = new HashMap<String, ArrayList<String>>();
            Random r = new Random();
            int j = r.nextInt(userIds.size());
            for (int i = 0; i < userIds.size(); i ++){
                if (i != j){

                    visibilities.put(userIds.get(i), none_str);
                }
                else{
                    ArrayList<String> vis = (ArrayList<String>)userIds.clone();
                    vis.remove(j);
                    visibilities.put(userIds.get(j), vis);
                }
            }
        }
        if (initial_tags == null){
            initial_tags = new HashMap<String,Integer>();
            for (String user:userIds){
                initial_tags.put(user, 0);
            }
        }
        if (end == null){
            end = new Date();
            end.setTime(end.getTime()+3600000); //eventually change to use Blackboard
        }
        if (boundary == null){
            boundary = new Circle(0, 0, 0); //eventually change to use Blackboard
        }
        DatabaseReference game = mDatabase.child("games").push();
        game.child("users").setValue(userIds);
        game.child("visibility").setValue(visibilities);
        game.child("tags").setValue(initial_tags);
        game.child("end_time").setValue(end);
        game.child("boundary_center_point").setValue(boundary.get_midpoint());
        game.child("boundary_radius").setValue(boundary.get_radius());

        mDatabase.child("users").child(userId).child("games").setValue(game.getKey());
        for (String user:userIds){
            mDatabase.child("users").child(user).child("games").setValue(game.getKey());
        }

        // need to somehow share the gameID with other clients and possibly set up joining.
        // probably use cloud messaging for this
        join_game(game.getKey());
    }
    //write function that reacts to blackboard state change
    protected void New_location(){
        ArrayList<Double> loc = new ArrayList<Double>();
        loc.add(bb.userLocation().value().getLatitude());
        loc.add(bb.userLocation().value().getLongitude());
        mDatabase.child("users").child(userId).child("location").setValue(loc);
    }

    protected void join_game(String gameId) {
        if (bb.currentGameId().value() == "none" || !bb.currentGameId().value().equals(gameId)) {
            mDatabase.child("games").child(gameId).child("visibility").child(userId).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<String> vis = (ArrayList<String>) dataSnapshot.getValue();
                            if ( vis != null && !vis.get(0).equals("none")) {
                                Set vis_set = new HashSet(vis);
                                bb.othersNames().set(vis_set);
                                setup_listeners();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //do nothing
                        }
                    });

            bb.currentGameId().set(gameId);
        }
    }
    private void setup_listeners(){
        for (String Wat : bb.othersNames().value()) {
            listeners.add(mDatabase.child("users").child(Wat).child("location").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Location loc_obj = new Location(" ");
                    ArrayList<Double> loc_data = (ArrayList<Double>) dataSnapshot.getValue();
                    loc_obj.setLatitude(loc_data.get(0));
                    loc_obj.setLongitude(loc_data.get(1));
                    set_other_location(dataSnapshot.getRef().getParent().getKey(), loc_obj);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //do nothing
                }
            }));
        }
    }
    private void set_other_location(String W, Location loc_obj){
        Map<String, Location> old_locations = bb.othersLocations().value();
        old_locations.put(W, loc_obj);
        bb.othersLocations().set(old_locations);
    }

   /* public void get_tagged(String UID){
        for (ValueEventListener list: listeners){
            mDatabase.removeEventListener(list);
        }//tbc
    }*/
//need to write code for tags
    //need to set these functions to actually be called


}

