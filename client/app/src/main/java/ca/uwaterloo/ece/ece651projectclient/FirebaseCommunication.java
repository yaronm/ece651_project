package ca.uwaterloo.ece.ece651projectclient;

/**
 * Created by yy on 2017-10-17.
 */

import android.location.Location;
import android.util.Log;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Set;

import static android.R.attr.src;
import static ca.uwaterloo.ece.ece651projectclient.GameState.JOINING;
import static ca.uwaterloo.ece.ece651projectclient.GameState.RUNNING;
import static ca.uwaterloo.ece.ece651projectclient.GameState.UNINITIALIZED;

public class FirebaseCommunication {
    private DatabaseReference mDatabase;
    private String userId;
    private Blackboard bb;
    private List<Map<String, ValueEventListener>> listeners;
    private ValueEventListener userlistener;
    private ValueEventListener vis_list;
    private List<String> visible_to_me;
    private ValueEventListener user_list_listener;
    private String old_game;
    private boolean join_custom;

    /*
    Constructor for this class. The class provides communication with the firebase server based on data
    in a blackboard object.
    The constructure initializes the database (and listener tracking), and subscribes to the username
    and location objects of the blackboard.
     */
    public FirebaseCommunication(Blackboard bb) {
        //user name must exist before this can be created
        this.bb = bb;
        join_custom = false;
        listeners = new ArrayList<>();
        visible_to_me = new ArrayList<>();
        vis_list = null;
        userlistener = null;
        user_list_listener = null;

        // get access to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //need to register to be notified with Blackboard Observer
        bb.userName().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                changeUser();
            }
        });
        bb.userLocation().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                newLocation();
            }
        });
        /*bb.currentGameId().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                gameSelected();
            }
        });*/
        bb.gameState().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                gameStateReaction();
            }
        });
    }

    private void gameStateReaction(){
        GameState curr_state = bb.gameState().value();
        switch(curr_state){
            case CREATING: createGame();
                break;
            case JOINING: gameSelected();
            default: break;
        }
    }

    //User centric functions

    /*
    actually changes the user, only creates the user if the user does not exist.
    sets up a listener on the game child of the current user
     */
    private void changeUser() { //to be changed when change to implementation with authentication
        deleteUser(); //in case changing the user instead of just creating the user
        userId = bb.userName().value();//this variable allows us to delete old listeners

        if (!userId.equals("") && userId != null && !userId.equals("none")) {
            mDatabase.child("users").child(userId).child("connection").setValue("connected");//database knows that this is the user logged in as
            if (bb.userLocation().value() != null) {
                newLocation();
            }

            //wait for someone to add us to a game
            userlistener = mDatabase.child("users").child(userId).child("games").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null)
                        Log.d("games changed", "games_changed");
                        //bb.associatedGames().set(((Map<String,String>)dataSnapshot.getValue()).values());//may need to change
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //do nothing
                }
            });
        }
    }

    /*
    deletes the current user listener object and reinitializes this and the userId to null
     */
    private void deleteUser() {
        if (userlistener != null && userId != null && !userId.equals("")) {
            mDatabase.child("users").child(userId).child("connection").setValue("disconnected");
            mDatabase.child("users").child(userId).child("games").removeEventListener(userlistener);
            userlistener = null;
            userId = null;
        }
    }

    //Game centric functions

    /*
    creates the game data structure in the firebase database. The only mandatory argument is the userIds.
    All other argumetns can be null. userIds is a list of all players in the game (including this user).
    visibilities is a map of which player can see which other players. If the player cannot see anyone
    the value of that player's entry must be 'none'. If null is given, the function sets a random player
    able to see all other players.
    initial_tags, end and boundary are not currently used in game logic, but the database just stores
    them for each player. The default initial_tags value is 0, end is 1 hour from start and boundary is
    0.
     */
    private void createGame() {
        VisibilityMatrixType VMat = bb.visibilityMatrixType().value();//may need to change names
        Map<String, Set<String>> visibilities = bb.visibilityMatrix().value();
        List<String> userIds = (ArrayList<String>)bb.othersNames().value();
        String curr_user = bb.userName().value();
        Map<String, Integer> initial_tags = new HashMap<>();
        Date end = new Date();
        Circle boundary;

        if (curr_user != null && !curr_user.equals("") && !curr_user.equals("none"))
            userIds.add(curr_user);
        else{
            bb.gameState().set(UNINITIALIZED);
            return;
        }
        int num_players = bb.numberOfPlayers().value();
        if (num_players > userIds.size()){
            createOpenGame();
            return;
        }
        if (visibilities == null) {// initialize the visibilities map if not initialized with random
            //vector.
            switch(VMat){
                case HIDE_N_SEEK: visibilities = hideNseekVisGenerator();
                    break;
                case ASSASSIN: visibilities = AssassinVisGenerator();
                    break;
                default:	bb.gameState().set(UNINITIALIZED);//may need to change
                    return;
            }
        }
        //initialize initial tag for each user to 0
        for (String user : userIds) {
            initial_tags.put(user, 0);
        }


        //initialize time limit to 1 hour from now
        end.setTime(end.getTime() + 3600000); //eventually change to use Blackboard

        //initialize to non-existent circle
        boundary = new Circle(0, 0, 0); //eventually change to use Blackboard

        //create tree in database for this game with unique identifier
        DatabaseReference game = mDatabase.child("games").push();
        game.child("users").setValue(userIds);
        game.child("visibility").setValue(visibilities);
        game.child("tags").setValue(initial_tags);
        game.child("end_time").setValue(end);
        game.child("boundary_center_point").setValue(boundary.get_midpoint());
        game.child("boundary_radius").setValue(boundary.get_radius());
        //List <String> games = bb.associatedGames.value();
        //games.add(game.getKey());
        //bb.associatedGames.set(games);
        DatabaseReference new_game = mDatabase.child("users").child(userId).child("games").push();
        Map <String, Object> new_game_map = new HashMap<>();
        new_game_map.put(new_game.getKey(), game.getKey());
        mDatabase.child("users").child(userId).child("games").updateChildren(new_game_map);
        //invite other users to the game
        for (String user : userIds) {
            if (!user.equals(curr_user)) {
                new_game = mDatabase.child("users").child(user).child("games").push();
                new_game_map.clear();
                new_game_map.put(new_game.getKey(), game.getKey());
                mDatabase.child("users").child(user).child("games").updateChildren(new_game_map);

            }
        }

        bb.currentGameId().set(game.getKey());
        bb.gameState().set(GameState.JOINING);
    }
    /*
    creates an open game
     */
    private void createOpenGame(){
        Date end = new Date();
        end.setTime(end.getTime() + 3600000);//to remove
        //need to do
        DatabaseReference game;
        int num_players = bb.numberOfPlayers().value();
        List<String> userIds = (ArrayList<String>)bb.othersNames().value();
        String curr_user = bb.userName().value();
        //Circle boundary = bb.boundary().value();
        VisibilityMatrixType VMat = bb.visibilityMatrixType().value();//may need to change names

        if (num_players == 0 || VMat == null){
            bb.gameState().set(UNINITIALIZED);
            return;
        }

        if (curr_user != null && !curr_user.equals("") && !curr_user.equals("none"))
            userIds.add(curr_user);
        else{
            bb.gameState().set(UNINITIALIZED);
            return;
        }

        game = mDatabase.child("open_games").push();
        game.child("num_players").setValue(num_players);
        game.child("players").setValue(userIds);
        //game.child("boundary_center_point").setValue(boundary.get_midpoint());
        //game.child("boundary_radius").setValue(boundary.get_radius());
        game.child("boundary_center_point").setValue(new Location(" "));//to remove
        game.child("boundary_radius").setValue(0);//to remove
        game.child("end_time").setValue(end);
        game.child("visibility_matrix_style").setValue(VMat.toString());
        game.child("running").setValue("false");
        Map <String, Object> game_key = new HashMap<>();
        game_key.put(game.getKey(), "true");
        for (String user: userIds){
            mDatabase.child("users").child(user).child("open_games").updateChildren(game_key);
        }

        join_custom = true;
        bb.currentGameId().set(game.getKey());
        bb.gameState().set(JOINING);


        //	go to state where join the game once it running becomes true
    }

    /*
    adds the custom game to our custom games list
    If we are not in the custom game's user list adds our username
     to it
     */
    private void joinCustomGame(){
        final String curr_user = bb.userName().value();
        final String game_id = bb.currentGameId().value();
        if (curr_user == null || curr_user.equals("") || curr_user.equals("none") || game_id == null
                || game_id.equals("") || game_id.equals("none")){
            bb.gameState().set(UNINITIALIZED);
            return;
        }



        mDatabase.child("open_games").child(game_id).addValueEventListener
                ((new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("running").getValue().equals("false")) {
                            Collection<Object> players = ((Map<String, Object>) dataSnapshot.child("players").getValue()).values();
                            Map<String, Object> game_key = new HashMap<>();
                            game_key.put(game_id, "true");

                            if (!players.contains(curr_user) && players.size() < (int) dataSnapshot.child("num_players").getValue()) {
                                Map<String, Object> player_key = new HashMap<>();
                                player_key.put(curr_user, "true");
                                mDatabase.child("open_games").child(game_id).child("players").updateChildren(player_key);
                                mDatabase.child("users").child(curr_user).child("open_games").updateChildren(game_key);
                            }
                        }else{
                            Collection<Object> players = ((Map<String, Object>) dataSnapshot.child("players").getValue()).values();
                            boolean added = false;
                            if (players.contains(curr_user)) {
                                DatabaseReference new_game = mDatabase.child("users").child(curr_user).child("games").push();
                                Map<String, Object> new_game_key = new HashMap<>();
                                new_game_key.put(new_game.getKey(), dataSnapshot.getKey());
                                mDatabase.child("users").child(curr_user).child("games").updateChildren(new_game_key);
                                added = true;
                                Map<String, Object> response = new HashMap<>();
                                response.put(curr_user, "true");
                                mDatabase.child("open_games").child(game_id).child("responses").updateChildren(response);
                            }

                            mDatabase.child("users").child(curr_user).child("open_games").child(dataSnapshot.getKey()).setValue(null);
                            join_custom = false;
                            if (added){
                                gameSelected();
                            }else{
                                bb.gameState().set(UNINITIALIZED);
                            }
                            return;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //do nothing
                    }
                }));

    }

    /*
    updates the listeners based on the game that the user has selected
    */
    private void gameSelected() {
        if (!join_custom) {
            String curr_game = bb.currentGameId().value();
            if (old_game != null && !old_game.equals("none") && old_game.equals("")) {
                if (user_list_listener != null) {
                    mDatabase.child("games").child(old_game).child("users").removeEventListener(user_list_listener);
                }
                vis_listclean(old_game);
            }
            setup_game_listeners(curr_game);
            old_game = curr_game;
        }else{
            joinCustomGame();
        }
    }

    //Visibility Matrix Makers

    /*
     * creates a visibility matrix using the hideNseek model
     * with a random seeker, based on the othersNames
     * and username fields in the blackboard
    */
    private Map<String, Set<String>> hideNseekVisGenerator(){
        Map<String, Set<String>> visibilities = new HashMap<>();
        List<String> userIds = (ArrayList<String>)bb.othersNames().value();
        Set<String> none_str = new HashSet<>();

        userIds.add(bb.userName().value());
        Random r = new Random();
        none_str.add("none");
        int j = r.nextInt(userIds.size());//select seeker at random
        for (int i = 0; i < userIds.size(); i++) {
            if (i != j) {
                visibilities.put(userIds.get(i), none_str);//hider
            } else {
                Set<String> vis = new HashSet<>((List<String>)(((ArrayList<String>) userIds).clone()));
                vis.remove(j);//can't see self
                visibilities.put(userIds.get(j), vis);//seeker
            }
        }
        return visibilities;
    }

    /*
     * creates a visibility matrix using the Assassins model
     * with a random seeker, based on the othersNames
     * and username fields in the blackboard
    */
    private Map<String, Set<String>> AssassinVisGenerator(){
        Map<String, Set<String>> visibilities = new HashMap<>();
        List<String> userIds = (ArrayList<String>)bb.othersNames().value();
        userIds.add(bb.userName().value());
        List<String> targets = (List<String>)(((ArrayList<String>)userIds).clone());
        Collections.shuffle(targets);
        for (int i = 0; i < userIds.size(); i++) {
            Set<String> vis_list = new HashSet<>();
            vis_list.add(targets.get(i));
            visibilities.put(userIds.get(i), vis_list);
        }
        return visibilities;
    }

    //Listener creating methods

    /*
    sets up any game dependent listeners.
    curr_game is the game id to use for the listener tree
     */
    private void setup_game_listeners(String curr_game){
        if (!curr_game.equals("none") && !curr_game.equals("") && curr_game != null) {
            user_list_listener = mDatabase.child("games").child(curr_game).child("users").addValueEventListener
                    ((new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<String> vis = (ArrayList<String>) dataSnapshot.getValue();
                            Set vis_set = new HashSet();
                            vis_set.addAll(vis);
                            vis_set.remove(userId);
                            bb.othersNames().set(vis_set);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //do nothing
                        }
                    }));
            vis_list = mDatabase.child("games").child(curr_game).child("visibility").child(userId).
                    addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<String> vis = (ArrayList<String>) dataSnapshot.getValue();
                            if (vis != null && !vis.get(0).equals("none")) {//only do this if can see someone
                                visible_to_me.addAll(vis);

                                remove_location_listeners();//clean locations listening to since our
                                //visibility matrix has changed and replace with new listeners
                                setup_location_listeners();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //do nothing
                        }
                    });
            bb.gameState().set(RUNNING);
        }
    }

    /*
creates a listener on the locations of all users we can see
 */
    private void setup_location_listeners() {
        for (String Wat : visible_to_me) {
            Map<String, ValueEventListener> curr = new HashMap<>();
            curr.put(Wat, mDatabase.child("users").child(Wat).child("location").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Location loc_obj = new Location(" ");
                    List<Double> loc_data = null;
                    if (((ArrayList)dataSnapshot.getValue()).get(0) instanceof Double){
                        loc_data = (ArrayList<Double>) dataSnapshot.getValue();
                    }
                    else if (((ArrayList)dataSnapshot.getValue()).get(0) instanceof Integer){
                        loc_data = new ArrayList<>();
                        loc_data.add((double)((ArrayList)dataSnapshot.getValue()).get(0));
                        loc_data.add((double)((ArrayList)dataSnapshot.getValue()).get(1));
                    }
                    loc_obj.setLatitude(loc_data.get(0));
                    loc_obj.setLongitude(loc_data.get(1));
                    set_other_location(dataSnapshot.getRef().getParent().getKey(), loc_obj);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //do nothing
                }
            }));
            listeners.add(curr);
        }
    }

    //Listener removal Methods

    /*
     removes all current location listeners
    */
    private void remove_location_listeners() {
        for (Map<String, ValueEventListener> list : listeners) {
            Set<String> key = list.keySet();
            for (String k : key) {
                mDatabase.child("users").child(k).child("location").removeEventListener(list.get(k));
            }
        }
        listeners.clear();
    }

    /*removes all listeners on visibility tags*/
    private void vis_listclean(String gameId){
        if (vis_list != null) {
            mDatabase.child("games").child(gameId).child("visibility").child(userId).
                    removeEventListener(vis_list);
        }
        vis_list = null;

    }

    //Location Methods

    /**
     * Uploads user name and location from the local blackboard to the Firebase server. If either
     * user name or location is null, this method does nothing.
     */
    private void newLocation() {
        // get the most up-to-date user name and location directly from the blackboard
        String userName = bb.userName().value();
        Location userLocation = bb.userLocation().value();
        // check that both user name and location are non-null
        if (userName != null && userLocation != null) {
            // construct location latitude/longitude pair for server upload
            List<Double> location = new ArrayList<>();
            location.add(userLocation.getLatitude());
            location.add(userLocation.getLongitude());
            // send the updated location to the server
            mDatabase.child("users").child(userName).child("location").setValue(location);
        }
    }

    /*
    updates the blackboard with the new location of user W
     */
    private void set_other_location(String W, Location loc_obj) {
        Map<String, Location> old_locations = bb.othersLocations().value();
        old_locations.put(W, loc_obj);
        bb.othersLocations().set(old_locations);
    }
}
