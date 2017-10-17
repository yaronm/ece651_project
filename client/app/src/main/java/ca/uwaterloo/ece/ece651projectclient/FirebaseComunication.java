package ca.uwaterloo.ece.ece651projectclient;

/**
 * Created by yy on 2017-10-17.
 */
import android.location.Location;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

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
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class FirebaseComunication {
    private DatabaseReference mDatabase;
    String userId;
    Blackboard bb;
    ArrayList<ValueEventListener> listeners;

    public FirebaseComunication(Blackboard bb){
        this.bb = bb;
        //need to register to be notified with Blackboard Observer

        // get access to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();


    }

    protected void createUser(){ //to be changed when change to implementation with authentication
        MessageDigest md;
        userId = ""; //to be changed when get blackboard interface
        try {
            md = MessageDigest.getInstance("SHA");
            md.update(bb.getUsername().getBytes());
            byte messageDigest[] = md.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            userId = hexString.toString();
        }catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (userId != "") {
            ArrayList<Double> loc= new ArrayList<Double>();
            loc.add(bb.getLocation().getLatitude());
            loc.add(bb.getLocation().getLongitude());

            mDatabase.child("users").child(userId).child("location").setValue(loc);
            mDatabase.child("users").child(userId).child("tags").setValue(0);
        }
    }


    protected void createGame(ArrayList<String> userIds, Map<String,ArrayList<String>> visibilities,
                           Map<String,Integer> initial_tags, Date end, Circle boundary){
        if (visibilities == null){
            Random r = new Random();
            int j = r.nextInt(userIds.size());
            for (int i = 0; i < userIds.size(); i ++){
                if (i != j){
                    visibilities.put(userIds.get(i), null);
                }
                else{
                    ArrayList<String> vis = (ArrayList<String>)userIds.clone();
                    vis.remove(j);
                    visibilities.put(userIds.get(j), vis);
                }
            }
        }
        if (initial_tags == null){
            for (String user:userIds){
                initial_tags.put(user,0);
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

        // need to somehow share the gameID with other clients and possibly set up joining.
        // probably use cloud messaging for this
        join_game(game.getKey());
    }
    //write function that reacts to blackboard state change
    protected void New_location(){
        mDatabase.child("users").child(userId).child("location").setValue(bb.getLocation());
    }
    protected void join_game(String gameId) {

        mDatabase.child("games").child(gameId).child("visibility").child(userId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        bb.setUsernames((Set<String>)dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //do nothing
                    }
                });
        for (String Wat : bb.getUsernames()){
            listeners.add(mDatabase.child("users").child(userId).child("location").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Location> loc = new HashMap<String, Location>();
                    Location loc_obj= new Location(" ");
                    ArrayList<Double> loc_data = (ArrayList<Double>)dataSnapshot.getValue();
                    loc_obj.setLatitude(loc_data.get(0));
                    loc_obj.setLongitude(loc_data.get(1));
                    loc.put(userId, loc_obj);
                    bb.setLocations(loc);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //do nothing
                }
            }));
        }
        bb.setGameId(gameId);
    }

    public void get_tagged(String UID){
        for (ValueEventListener list: listeners){
            mDatabase.removeEventListener(list);
        }//tbc
    }
//need to write code for tags
    //need to set these functions to actually be called

    ////////////////////////////////////////////////////////
//////everything below this is to be changed////////////
///////////////////////////////////////////////////////

}

