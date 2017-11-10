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
        console.log("num users:");
        console.log(num_users);
        var users = event.data.child("players").val();
       if (users != undefined && users != null && num_users != null && num_users != undefined){
            if (!Array.isArray(users)){
                users = Object.keys( users ).map(function(key){
                    return users[key];
                });
            }
            console.log ("user length");
            console.log(users.length);
            for (var u in users){
                console.log(users[u]);
            }
            if (users.length == num_users){
                //make visibility matrix and set running to true, copy game key to non-open games and wait for acknowledgements from all players
                initialize(users, event);
            }
            else if (users.length > num_users){
                var players = event.data.child("players").val();
                var keys = Object.keys(players).sort();
                var to_remove = users.length - num_users;
                for (var i = 0; i < to_remove; i++){
                    delete players[keys[keys.length-i-1]];
                    
                }
                event.data.child("players").ref.set(players);
                var play_vals = Object.keys( players ).map(function(key){
                    return players[key];
                });
                initialize(play_vals, event);
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

exports.tag_action = functions.database.ref('games/{gameid}/tags/{tagger}/{tagged}/').onWrite(event=>{
    var tagged = event.data.val();
    var tagger = event.params.tagger;
    var vis_ref = admin.database().ref('/games').child(event.params.gameid).child('visibility');


    vis_ref.transaction(function (vis){
        if (vis){
          console.log(vis);
          var tagged_targs = vis[tagged];
          for (var targ in tagged_targs){
              console.log(targ);
              vis[tagger][targ] = 1;
          }
          console.log('deleting');
          for (var v in vis){
              console.log('check');
              console.log(v);
            if (vis[v][tagged] != undefined){
                delete (vis[v][tagged]);
                console.log(v);
            }
          }
          delete vis[tagged];
          return vis;
        }
        return vis;
    });
});

function initialize(users, event){
    console.log("initialize");
    mat_style = event.data.child("visibility_matrix_style").val();
    if (mat_style == "ASSASSIN"){
        generate_assassin(users, event);
    }
    else if (mat_style == "HIDE_N_SEEK"){
        generate_hide_n_seek(users,event);
    }
    event.data.child("running").ref.set("true");
    //copy game to regular games
    var game = admin.database().ref('games/');
    var gameid = event.params.gameid;
    game.child(gameid).child("boundary_center_point").set(event.data.child("boundary_center_point"));
    game.child(gameid).child("end_time").set(event.data.child("end_time"));
    game.child(gameid).child("users").set(event.data.child("players"));
    game.child(gameid).child("visibility").set(event.data.child("visibility"));
}

function generate_assassin(users, event){
    console.log("generate_assassin");
    var sec = users.reduce((a,v)=>a.splice(Math.floor(Math.random() * a.length), 0, v) && a, []);
    var vis = new Object();
    var redo = 1;
    while (redo == 1){
        for (var index = 0; index < sec.length; index ++){
            if (sec[index] == users[index]){
                sec = sec.reduce((a,v)=>a.splice(Math.floor(Math.random() * a.length), 0, v) && a, []);
                vis = {};
                redo = 1;
                break;
            }
            vis[sec[index]] = new Object();
            var targ = users[index];
            vis[sec[index]][targ] = 1;
            redo = 0;
        }
    }
    
    return event.data.child("visibility").ref.set(vis);;
}

function generate_hide_n_seek(users, event){
    var j = Math.floor(Math.random() * users.length);
    var vis = new Object();
    vis[users[j]] = new Object();
    for (var i = 0; i < users.length; i++){
        if (i != j){
            vis[users[j]][users[i]]=1;
            vis[users[i]] = new Object();
            vis[users[i]].none = 1;
        }
    }
    
    return event.data.child("visibility").ref.set(vis);;
}
//create function for creating open games 
    //checks the num of players and the defined num players, when equal makes visibility matrix, and sets running to true
    //also copies the game key to the non-open games and wehn get acknowledgements from all players removes the game from open games