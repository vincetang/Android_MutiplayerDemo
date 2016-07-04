var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var players = [];

server.listen(8080, function() {
	console.log("Server is now running...");
});

// socket.io is listening on port 8080. Tell it what to do when
// it gets a connection
io.on('connection', function(socket) {
	console.log("Player Connected!");

	// method calls
	socket.emit('socketID', { id: socket.id });

    // update player movements
	socket.on('playerMoved', function(data){
	    data.id = socket.id;
        for (var i = 0; i < players.length; i++) {
            if (players[i].id == socket.id) {
                players[i].x = data.x;
                players[i].y = data.y;
             }
         }
         socket.broadcast.emit('playerMoved', data);
	});


	//socket.broadcast.emit('getPlayers', players);
    socket.broadcast.emit('newPlayer', { id: socket.id }); // sends to everybody but the socket connecting
    // send list of players
	socket.emit('getPlayers', players);


    // disconnecting
	socket.on('disconnect', function(){
		console.log("Player Disconnected");
		// broadcast to all connected clients that this player left
		socket.broadcast.emit('playerDisconnected', { id : socket.id } );
		// remove player from players array
		for (var i = 0; i < players.length; i ++) {
		    if (players[i].id == socket.id) {
		        players.splice(i, 1);
		    }
		}
	});

	// add players
	players.push(new player(socket.id, 0, 0));

});

function player(id, x, y) {
    this.id = id;
    this.x = x;
    this.y = y;
}
