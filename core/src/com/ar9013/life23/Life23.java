package com.ar9013.life23;

import com.ar9013.life23.sprite.Starship;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Life23 extends ApplicationAdapter {

	private final float UPDATE_TIME = 1/60f;

	float timer ;

	SpriteBatch batch;
	private Socket socket;

	String TAG ="Life23";
	Starship player;
	Texture playerShip;
	Texture friendlyShip;

	HashMap<String,Starship> friendlyPLayers;

	@Override
	public void create () {
		batch = new SpriteBatch();

		playerShip = new Texture("playerShip2.png");
		friendlyShip = new Texture("playerShip.png");

		friendlyPLayers = new HashMap<String, Starship>();

		connectSocket();
		configSocketEvent();
	}

	public void handleInput(float dt){
		if(player != null){
			if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
				player.setPosition(player.getX() - (200*dt),player.getY());
			}else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
				player.setPosition(player.getX() + (200*dt),player.getY());
			}
		}
	}

	public  void  updateServer(float dt){
		timer += dt;
		if(timer >= UPDATE_TIME && player != null && player.isMoved()){
			JSONObject data = new JSONObject();

			try {
				data.put("x",player.getX());
				data.put("y",player.getY());
				socket.emit("playerMoved",data);

			}catch (JSONException e){
				Gdx.app.log(TAG,"Error sending update data.");
				e.printStackTrace();
			}

		}
	}

	@Override
	public void render () {
		handleInput(Gdx.graphics.getDeltaTime());
		updateServer(Gdx.graphics.getDeltaTime());


		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


		batch.begin();
		if (player != null){
			player.draw(batch);
		}

		for(HashMap.Entry<String,Starship> entry :friendlyPLayers.entrySet()){
			entry.getValue().draw(batch);
		}
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		playerShip.dispose();
		friendlyShip.dispose();


	}


	public void connectSocket(){
		try{
		socket = IO.socket("http://localhost:8080");
		socket.connect();
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public void configSocketEvent(){
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log(TAG,"Conected");
				player = new Starship(playerShip);

			}
		}).on("socketID", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String playerId = data.getString("id");
					Gdx.app.log(TAG,"My ID: "+playerId);
				}catch (JSONException ex){
					ex.printStackTrace();
					Gdx.app.log(TAG,"Error Getting ID");
				}

			}
		}).on("newPlayer", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];


				try {
					String playerId =data.getString("id");
					Gdx.app.log(TAG,"New Player Connect: "+playerId);
					friendlyPLayers.put(playerId,new Starship(friendlyShip));


				}catch (Exception ex){
					Gdx.app.log(TAG,"Error Getting New Player ID");
				}

			}
		}).on("Player Disconnect", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];


				try {
					String id =data.getString("id");
					friendlyPLayers.remove(id);


				}catch (Exception ex){
					Gdx.app.log(TAG,"Error Getting New Player ID");
				}

			}
		}).on("getPlayers", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONArray objects =(JSONArray) args[0];

				try {

					for(int i = 0 ;i<objects.length();i++)
					{
						Starship coolPLayer = new Starship(friendlyShip); // 建立 StarShip
						Vector2 position = new Vector2();
						position.x =  ((Double)objects.getJSONObject(i).getDouble("x")).floatValue();
						position.y =  ((Double)objects.getJSONObject(i).getDouble("y")).floatValue();
						coolPLayer.setPosition(position.x,position.y); //因為需要傳訴 float 型別，所以上面的強至轉換 Double 成 float

						friendlyPLayers.put(objects.getJSONObject(i).getString("id"),coolPLayer);
					}

				}catch (JSONException ex){
				ex.printStackTrace();
				}
			}
		}).on("playerMoved", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];


				try {
					String playerId = data.getString("id");
					Double x = data.getDouble("x");
					Double y = data.getDouble("y");

					if(friendlyPLayers.get(playerId) != null)
					{
						friendlyPLayers.get(playerId).setPosition(x.floatValue(),y.floatValue());
					}

				}catch (Exception ex){
					Gdx.app.log(TAG,"Error Getting New Player ID");
				}

			}
		});
	}



}
