/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import util.Camera;
import util.Material;
import util.Matrix4f;
import util.Mesh;
import util.MeshInstance;
import util.Shader;
import util.Util;

/**
 *
 * @author Erik
 */
public class Player {

	private static Mesh playerMesh;
	private static Material playerMat;

	private static Vector3f worldFront = new Vector3f(1, 0, 0);
	private static Vector3f worldNorth = new Vector3f(0, 1, 0);

	static {
		int dif = Util.loadTexture("container2.png");
		int spec = Util.loadTexture("container2_specular.png");
		playerMat = new Material(dif, spec);
		playerMesh = new Mesh("bunny.obj");
	}

	Camera camera;
	private ArrayList<ClickHandler> handlers;

	private Vector3f position;

	private Vector3f viewDir;

	private MeshInstance model;
	private float viewDirAngle;

	public Player(Vector3f position) {
		this.position = position;
		this.position.normalise();
		handlers = new ArrayList<>();
		model = new MeshInstance(playerMesh, playerMat);
		viewDir = new Vector3f(1, 0, 0);
		viewDirAngle = 0;
	}

	public void render(Shader shader) {
		model.setLocation(position);
		Vector3f rotationAxis = new Vector3f();
		rotationAxis = Vector3f.cross(worldNorth, position, rotationAxis);
		rotationAxis.normalise();

		Vector3f normPos = new Vector3f(position);
		normPos.normalise();

		float angle = (float) Math.acos(Vector3f.dot(normPos, worldNorth));

		Vector3f plainPos = new Vector3f(position.x, 0, position.z);
		plainPos.normalise();

		float baseRotationAngle = (float) Math.PI - (float) Math.acos(Vector3f.dot(plainPos, worldFront));

		if (Vector3f.cross(worldFront, plainPos, null).y > 0) {
			baseRotationAngle *= -1;
		}
		Matrix4f rot = new Matrix4f();
		rot.rotate(viewDirAngle, normPos);
		rot.rotate(angle, rotationAxis);
		rot.rotate(baseRotationAngle, worldNorth);

		model.setRotationMatrix(rot);
		model.setScale(new Vector3f(0.5f, 0.5f, 0.5f));
		model.render(shader);
	}

	public void teleportTo(Vector3f dest) {
		camera.setPosition(dest);
	}

	public void addHandler(ClickHandler h) {
		handlers.add(h);
	}

	/**
	 * fetches input events (like lookaround and walking) and updates the
	 * uniform values to also show this progress
	 *
	 * @param deltaTime
	 *            time passed since last frame
	 */

	public Vector3f getPosition() {
		return position;
	}

	public void update(float deltaTime) {

		int dx = 0, dy = 0;
		// movement
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			dx--;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			dx++;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			dy--;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			dy++;
		}
		viewDirAngle += dy * deltaTime;
		// position = new Vector3f(0.1f, (float)
		// Math.sin(System.currentTimeMillis() % (int) (3000f * 2f * Math.PI) /
		// 3000f), (float) Math.cos(System.currentTimeMillis() % (int) (3000f *
		// 2f * Math.PI) / 3000f));
		// viewDirAngle += deltaTime;
		Vector3f right = new Vector3f();
		right = Vector3f.cross(worldNorth, position, right);
		Vector3f front = new Vector3f();
		front = Vector3f.cross(right, position, front);

		Vector3f normPos = new Vector3f(position);
		normPos.normalise();
		Matrix4f rot = new Matrix4f();
		rot.rotate(viewDirAngle, normPos);

		viewDir = Util.vmMult(front, rot);
		viewDir.normalise();

		float speed = dx * deltaTime;
		Vector3f walkDir = new Vector3f(viewDir);
		walkDir.normalise();
		walkDir.scale(speed);
		position = Vector3f.add(position, walkDir, position);
		position.normalise();
	}

	public static interface ClickHandler {

		public void onClickEvent(boolean down, int mouseButton);
	}
}
