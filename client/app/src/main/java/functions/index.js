const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

 exports.init_user = functions.database.ref('users/{userid}/').onCreate(event => {
	return event.data.ref.child('games').push("none");
});

exports.open_game = functions.database.ref('open_games/{gameid}/').onWrite(event=>{
   var running = event.data.child("running").val();
   var num_users = event.data.child("num_players").val();
   if (running == "false"){
            var num_users = event.data.child("num_players").val();
            var users = event.data.child("players").val();
       if (users != undefined && users != null && num_users != null && num_users != undefined){
            if (!Array.isArray(users)){
                users = Object.values( users );
            }
            if (users.length == num_users){
                //make visibility matrix and set running to true, copy game key to non-open games and wait for acknowledgements from all players
                initialize(users, event);
            }
            else if (users.length > num_users){
                var to_remove = users.length - num_users;
                for (var i = 0; i < to_remove; i++){
                    users.pop();
                    event.data.child("players").set(users);
                }
                initialize(users, event);
                //kick someone out, make visibility matrix and set running to true and wait for acknowledgements from all players
            }
       }
   }else{
       var responses = event.data.child("responses").val();
            if (responses != undefined && responses != null){
                if (!Array.isArray(responses)){
                    responses = Object.keys(responses);
                }
                if (responses.length == num_users){
                    //remove the game from open games
                   
                    event.ref.remove();
                }
            }
   }
});

//make visibility matrix and set running to true, copy game key to non-open games
function initialize(users, event){
    mat_style = event.data.child("visibility_matrix_style").val();
    if (mat_style == "ASSASSIN"){
        generate_assassin(users, event);
    }
    else if (mat_style == "HIDE_N_SEEK"){
        generate_hide_n_seek(users,event);
    }
    event.data.child("running").set("true");
    //copy game to regular games
    var game = admin.database().ref('games/');
    var gameid = event.params.gameid;
    game.child(gameid).child("boundary_center_point").set(event.data.child("boundary_center_point"));
    game.child(gameid).child("end_time").set(event.data.child("end_time"));
    game.child(gameid).child("users").set(event.data.child("players"));
    game.child(gameid).child("visibility").set(event.data.child("visibility"));
}

function generate_assassin(users, event){
    var sec = users.reduce((a,v)=>a.splice(Math.floor(Math.random() * a.length), 0, v) && a, []);
    var vis = {};
    for (var index in sec.length){
        vis[sec[index]] = users[index];
    }
    game.child("visibility").set(vis);
    return;
}

function generate_hide_n_seek(users, event){
    var j = Math.floor(Math.random() * users.length);
    var vis = {};
    for (var i = 0; i < users.length; i++){
        if (i == j){
            vis[users[i]] = users;
        }
        else{
            vis[users[i]] = ["none"];
        }
    }
    game.child("visibility").set(vis);
    return;
}
//create function for creating open games 
    //checks the num of players and the defined num players, when equal makes visibility matrix, and sets running to true
    //also copies the game key to the non-open games and wehn get acknowledgements from all players removes the game from open games