package com.pi.robot.mesh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.pi.math.Vector3D;
import com.pi.robot.mesh.MeshMaterial.Illumination;

public class WavefrontLoader {
	public static Mesh loadWavefrontObject(File f) throws IOException {
		Map<String, MeshMaterial> matlLibrary = new HashMap<String, MeshMaterial>();
		List<Vector3D> positions = new ArrayList<Vector3D>();
		List<Vector3D> normals = new ArrayList<Vector3D>();
		List<float[]> textures = new ArrayList<float[]>();
		List<MeshVertex> verticies = new ArrayList<MeshVertex>();
		List<Integer> indicies = new ArrayList<Integer>();
		BufferedReader r = new BufferedReader(new FileReader(f));
		int polygonSize = -1;
		while (true) {
			String line = r.readLine();
			if (line == null) {
				break;
			}
			String[] parts = line.split(" ");
			try {
				if (parts[0].equalsIgnoreCase("v") && parts.length >= 4) {
					positions.add(new Vector3D(Float.valueOf(parts[1]) * 2 + 1,
							Float.valueOf(parts[2]) * 2 - 3.2f, Float
									.valueOf(parts[3]) * 2));
				} else if (parts[0].equalsIgnoreCase("vn") && parts.length >= 4) {
					normals.add(new Vector3D(Float.valueOf(parts[1]), Float
							.valueOf(parts[2]), Float.valueOf(parts[3])));
				} else if (parts[0].equalsIgnoreCase("vt") && parts.length >= 3) {
					textures.add(new float[] { Float.valueOf(parts[1]),
							Float.valueOf(parts[2]) });
				} else if (parts[0].equalsIgnoreCase("f")) {
					if (polygonSize == -1) {
						polygonSize = parts.length - 1;
					}
					if (polygonSize != parts.length - 1) {
						throw new RuntimeException(
								"Inconsistent polygon size: "
										+ (parts.length - 1) + " expected "
										+ polygonSize);
					}
					for (int i = 1; i < parts.length; i++) {
						String[] vParts = parts[i].split("\\/");
						verticies.add(new MeshVertex(positions.get(Integer
								.valueOf(vParts[0]) - 1), normals.get(Integer
								.valueOf(vParts[2]) - 1), textures.get(Integer
								.valueOf(vParts[1]) - 1)));
						indicies.add(verticies.size() - 1);
					}
				} else if (parts[0].equalsIgnoreCase("mtllib")) {
					Map<String, MeshMaterial> mAdd = loadWavefrontMaterials(new File(
							f.getParentFile(), parts[1]));
					for (Entry<String, MeshMaterial> m : mAdd.entrySet()) {
						matlLibrary.put(m.getKey(), m.getValue());
					}
				} else if (parts[0].equalsIgnoreCase("usemtl")) {
					// TODO use materials
				}
			} catch (NumberFormatException e) {
			}
		}
		r.close();
		return new Mesh(verticies, indicies, polygonSize);
	}

	public static Map<String, MeshMaterial> loadWavefrontMaterials(File f)
			throws IOException {
		Map<String, MeshMaterial> library = new HashMap<String, MeshMaterial>();
		BufferedReader r = new BufferedReader(new FileReader(f));
		MeshMaterial active = null;
		String activeName = null;
		while (true) {
			String line = r.readLine();
			if (line == null) {
				break;
			}
			String[] parts = line.split(" ");
			if (parts[0].equalsIgnoreCase("newmtl")) {
				if (activeName != null && active != null) {
					library.put(activeName, active);
				}
				activeName = parts[1];
				active = new MeshMaterial();
			} else if (parts[0].equalsIgnoreCase("Ns")) {
				active.specularCoefficient = Float.valueOf(parts[1]);
			} else if (parts[0].equalsIgnoreCase("Ka")) {
				active.ambient.set(Float.valueOf(parts[1]),
						Float.valueOf(parts[2]), Float.valueOf(parts[3]));
			} else if (parts[0].equalsIgnoreCase("Kd")) {
				active.diffuse.set(Float.valueOf(parts[1]),
						Float.valueOf(parts[2]), Float.valueOf(parts[3]));
			} else if (parts[0].equalsIgnoreCase("Ks")) {
				active.specular.set(Float.valueOf(parts[1]),
						Float.valueOf(parts[2]), Float.valueOf(parts[3]));
			} else if (parts[0].equalsIgnoreCase("Ni")) {

			} else if (parts[0].equalsIgnoreCase("d")
					|| parts[0].equalsIgnoreCase("Tr")) {
				active.transparency = Float.valueOf(parts[1]);
			} else if (parts[0].equalsIgnoreCase("illum")) {
				active.illum = Illumination.values()[Integer.valueOf(parts[1])];
			}
		}
		r.close();
		return library;
	}
}
