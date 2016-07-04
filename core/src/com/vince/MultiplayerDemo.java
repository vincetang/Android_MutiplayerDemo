package com.vince;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.vince.sprites.Cat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MultiplayerDemo extends ApplicationAdapter {
	public static final int V_WIDTH = 400;
	public static final int V_HEIGHT = 208;
	public static final float PPM = 100;

	private final float UPDATE_TIME = 1/60f; //60fps
	float timer;

	SpriteBatch batch;
	Cat player;
	Texture playerCat;
	Texture friendlyCat;
	HashMap<String, Cat> friendlyPlayers;

	private Socket socket;


	public void updateServer(float dt) {
		timer += dt;
		if (timer >= UPDATE_TIME && player != null && player.hasMoved()) {
			JSONObject data = new JSONObject();
			try {
				data.put("x", player.getX());
				data.put("y", player.getY());
				socket.emit("playerMoved", data);
			} catch (JSONException e) {
				Gdx.app.log("SocketIO", "Error sending update data");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void create () {
		batch = new SpriteBatch();
		playerCat = new Texture("gfx/cat.png");
		friendlyCat = new Texture("gfx/cat2.png");

		friendlyPlayers = new HashMap<String, Cat>();

		connectSocket();
		configSocketEvents();
	}

	public void handleInput(float dt) {
		if (player != null) {
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
				player.setPosition(player.getX() + (-200 * dt), 0);
			} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				player.setPosition(player.getX() + (200 * dt), 0);
			}
		}
	}
	@Override
	public void render () {

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// update player movements
		updateServer(Gdx.graphics.getDeltaTime());

		handleInput(Gdx.graphics.getDeltaTime());
		batch.begin();
		if (player != null)
			player.draw(batch);

		for (HashMap.Entry<String, Cat> entry : friendlyPlayers.entrySet()) {
			entry.getValue().draw(batch);
		}

		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		playerCat.dispose();
		friendlyCat.dispose();
	}


	/* Socket IO Methods */
	public void connectSocket() {
		try {
			socket = IO.socket("http://localhost:8080");
			socket.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void configSocketEvents() {
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log("SocketIO", "Connected");
				player = new Cat(playerCat);
			}
		});

		socket.on("socketID", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String id = data.getString("id");
					Gdx.app.log("SocketIO", "My ID: " + id);
				} catch (JSONException e) {
					e.printStackTrace();
					Gdx.app.log("SocketIO", "Error getting ID");
				}
			}
		});

		socket.on("newPlayer", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String playerId = data.getString("id");
					Gdx.app.log("SocketIO", "New Player connected: " + playerId);
					friendlyPlayers.put(playerId, new Cat(friendlyCat));
				} catch (JSONException e) {
					e.printStackTrace();
					Gdx.app.log("SocketIO", "Error getting new player ID");
				}
			}
		});

		socket.on("playerDisconnected", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String id = data.getString("id");
					Gdx.app.log("SocketIO", "removed player");
					friendlyPlayers.remove(id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		socket.on("getPlayers", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONArray players = (JSONArray) args[0];
				try {
					for (int i = 0; i < players.length(); i++) {
						Cat coopPlayer = new Cat(friendlyCat);
						Vector2 position = new Vector2();
						position.x = ((Double) players.getJSONObject(i).getDouble("x")).floatValue();
						position.y = ((Double) players.getJSONObject(i).getDouble("y")).floatValue();
						coopPlayer.setPosition(position.x, position.y);
						Gdx.app.log("SocketIO:", "added player");
						friendlyPlayers.put(players.getJSONObject(i).getString("id"), coopPlayer);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		socket.on("playerMoved", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String playerId = data.getString("id");
					Double x = data.getDouble("x");
					Double y = data.getDouble("y");

					if (friendlyPlayers.get(playerId) != null) {
						friendlyPlayers.get(playerId).setPosition(x.floatValue(), y.floatValue());
					}
				} catch (JSONException e) {
					Gdx.app.log("SocketIO", " Error retrieving playerMoved data");
					e.printStackTrace();
				}
			}
		});

	}
}
