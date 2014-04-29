package com.pi.robot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.pi.math.TransMatrix;
import com.pi.math.Vector3D;
import com.pi.robot.mesh.BinarySTLLoader;
import com.pi.robot.mesh.Mesh;
import com.pi.robot.mesh.MeshVertex;

public class Skeleton {
	public static boolean COMPLEX = false;

	private List<Bone> rootBones = new ArrayList<Bone>();
	private Map<Integer, Bone> bones = new HashMap<Integer, Bone>();

	@SuppressWarnings({ "deprecation", "resource" })
	public Skeleton(File f) throws IOException {
		InputStream in = null;
		if (f.exists()) {
			in = new FileInputStream(f);
		} else {
			in = Skeleton.class.getResourceAsStream("/" + f.getName());
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		while (true) {
			String s = r.readLine();
			if (s == null) {
				break;
			}
			s = s.trim();
			if (s.startsWith("#")) {
				continue;
			}
			if (s.contains("#")) {
				s = s.substring(0, s.indexOf('#'));
			}
			String[] chunks = s.split("\t");
			if (chunks.length >= 3) {
				try {
					int id = Integer.valueOf(chunks[0]);
					int parent = Integer.valueOf(chunks[1]);
					String[] start = chunks[2].split(",");
					Vector3D startPos = new Vector3D(Float.valueOf(start[0]),
							Float.valueOf(start[1]), Float.valueOf(start[2]));
					Vector3D endPos = startPos.clone();
					endPos.y += 10.0;
					Bone b;
					if (parent < 0) {
						b = new Bone(id, startPos, endPos);
						rootBones.add(b);
						b.calculate();
					} else {
						b = new Bone(id, bones.get(parent), startPos, endPos);
					}
					if (chunks.length >= 4
							&& !chunks[3].equalsIgnoreCase("null")) {
						File file = new File(chunks[3]);
						// if (!file.exists()
						// || Skeleton.class.getResource("/"
						// + file.getName() + ".out.gz") == null) {
						// String main = file.getAbsolutePath().substring(0,
						// file.getAbsolutePath().lastIndexOf('.'));
						// String ext = file.getAbsolutePath().substring(
						// file.getAbsolutePath().lastIndexOf('.'));
						// file = new File(main + "-"
						// + (COMPLEX ? "complex" : "simple") + ext);
						// }
						File cache = new File(file.getAbsolutePath()
								+ ".out.gz");
						MeshVertex.compareNormals = chunks.length > 4
								&& chunks[4].equalsIgnoreCase("nrmcmp");
						if (cache.exists()
								|| Skeleton.class.getResource("/"
										+ cache.getName()) != null) {
							System.out.println("Loading cache: "
									+ cache.getAbsolutePath());
							if (cache.exists()) {
								b.mesh = Mesh.loadCache(cache.toURL());
							} else {
								b.mesh = Mesh.loadCache(Skeleton.class
										.getResource("/" + cache.getName()));
							}
							System.out.println("Loaded cache: "
									+ cache.getAbsolutePath());
						} else {
							System.out.println("Loading file: "
									+ file.getAbsolutePath());
							b.mesh = BinarySTLLoader.loadSTL(file);
							b.mesh.generateBuffers();
							b.mesh.saveCache(cache);
							System.out.println("Loaded: "
									+ file.getAbsolutePath());
						}
						TransMatrix m = new TransMatrix();
						int i = MeshVertex.compareNormals ? 5 : 4;
						while (i < chunks.length) {
							String[] rot = chunks[i].split(",");
							if (rot.length == 3) {
								m.identity().setTranslation(
										Float.valueOf(rot[0]),
										Float.valueOf(rot[1]),
										Float.valueOf(rot[2]));
							} else if (rot.length == 4
									&& rot[0].equals("scale")) {
								m.identity().setScale(Float.valueOf(rot[1]),
										Float.valueOf(rot[2]),
										Float.valueOf(rot[3]));
							} else {
								m.identity().setRotation(
										Float.valueOf(rot[1]),
										Float.valueOf(rot[2]),
										Float.valueOf(rot[3]),
										(float) (Float.valueOf(rot[0])
												* Math.PI / 180.0));
							}
							b.mesh.apply(m);
							i++;
						}
						// if (b.mesh.getIndicies() != null) {
						// System.out.println("Regen buffer for: " +
						// file.getName());
						b.mesh.generateBuffers();
						// }
					}
					b.calculate();
					bones.put(id, b);
				} catch (Exception e) {
					System.out.println("Invalid bone line: \"" + s + "\"");
					e.printStackTrace();
				}
			} else {
				System.out.println(Arrays.toString(chunks));
			}
		}
		r.close();
		try {
			in.close();
		} catch (Exception e) {
		}
	}

	public void calculate() {
		for (Bone rootBone : this.rootBones) {
			rootBone.calculateRecursive();
		}
	}

	public Iterator<Entry<Integer, Bone>> getBones() {
		return bones.entrySet().iterator();
	}

	public List<Bone> getRootBone() {
		return rootBones;
	}

	public Bone getBone(int i) {
		return bones.get(i);
	}
}
