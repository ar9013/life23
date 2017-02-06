var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var players = [];// 建立空的玩家陣列

server.listen(8080,function(){
  console.log("Server is now running");
});

io.on('connection',function(socket) {
  console.log("Player connection"); //  登入 印出訊息
  socket.emit('socketID',{ id:socket.id}); // 送id 給 client
  socket.emit('getPlayers',players); // 傳 玩家陣列給使用者
  socket.broadcast.emit('newPlayer',{ id:socket.id});
	
socket.on('playerMoved',function(data){ // 收
	data.id = socket.id;
socket.broadcast.emit('playerMoved',data); //傳送
console.log("playerMoved: "+
"ID: "+data.id+
"X: "+data.x+
"Y: "+data.y);
for(var i = 0;i<players.lenght; i++)
   {
	if(players[i].id == data.id)
	{
	players[i].x = data.x;
	players[i].y = data.y;
	}
   }
});

  socket.on('disconnect',function(){
    console.log("Player disconnect");

	socket.broadcast.emit('Player Disconnect',{ id:socket.id});
	for(var i =0;i<players.lenght;i++){
		if(players[i].id == socket.id){
			players.splice(i,1);
		}
	}

  });
	players.push(new player(socket.id,0,0));
});

function player(id,x,y){
	this.id = id;
	this.x =x;
	this.y = y;
}
