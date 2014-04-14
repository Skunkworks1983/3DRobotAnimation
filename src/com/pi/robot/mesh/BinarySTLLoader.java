package com.pi.robot.mesh;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.pi.math.Vector3D;

public class BinarySTLLoader {
	ArrayList<MeshVertex> verticies;
	ArrayList<Integer> indicies;
	boolean mergeVerticies = true;
	Color defaultColor;

	private void readFacetB(ByteBuffer in, int index) throws IOException {
		Vector3D normal = new Vector3D(in.getFloat(), in.getFloat(),
				in.getFloat());
		List<MeshVertex> points = new ArrayList<MeshVertex>(3);
		for (int i = 0; i < 3; i++) {
			Vector3D pos = new Vector3D(in.getFloat(), in.getFloat(),
					in.getFloat());
			MeshVertex mv = new MeshVertex(pos, normal, new float[] { 0, 0 });
			points.add(mv);
		}
		Vector3D u = points.get(0).getPosition().clone()
				.subtract(points.get(1).getPosition()).normalize();
		Vector3D v = points.get(2).getPosition().clone()
				.subtract(points.get(1).getPosition()).normalize();
		normal = Vector3D.crossProduct(u, v).normalize();

		byte attribA = in.get();
		byte attribB = in.get();

		Color custom = null;
		if ((attribA & 0x40) == 0x40) {
			// Custom color
			int attrib = ((attribB << 8) & 0xFF00) | (attribA & 0xFF);
			int red = (attrib & 0x1F) * 8;
			int green = ((attrib >> 5) & 0x1F) * 8;
			int blue = ((attrib >> 10) & 0x1F) * 8;
			if (red >= 0 && blue >= 0 && green >= 0 && red <= 255
					&& blue <= 255 && green <= 255) {
				custom = new Color(red, green, blue,
						defaultColor != null ? defaultColor.getAlpha() : 255);
			}
		}
		for (MeshVertex mv : points) {
			mv.normal = normal;
			if (custom != null) {
				mv.color = new FloatBufferColor(custom.getRed() / 255f,
						custom.getGreen() / 255f, custom.getBlue() / 255f,
						custom.getAlpha() / 255f);
			}
		}

		for (MeshVertex mv : points) {
			int idx = -1;
			if (mergeVerticies) {
				int s = verticies.subList(Math.max(verticies.size() - 1000, 0),
						verticies.size()).lastIndexOf(mv);
				if (s >= 0) {
					idx = s + Math.max(verticies.size() - 1000, 0);
				}
			}
			if (idx < 0) {
				indicies.add(verticies.size());
				verticies.add(mv);
			} else {
				indicies.add(idx);
			}
		}
	}

	private void loadBinarySTL(File f) throws IOException {
		FileInputStream data;
		ByteBuffer dataBuffer;
		byte[] info = new byte[80];
		byte[] facesCount = new byte[4];
		byte[] ioBuffer;
		int numberOfFaces;
		data = new FileInputStream(f);
		if (80 != data.read(info)) {
			System.out.println("Format Error: 80 bytes expected");
		} else {
			String header = new String(info);
			{
				int color = header.indexOf("COLOR=");
				if (color > 0) {
					ByteBuffer headerBuffer = ByteBuffer.wrap(info);
					byte[] str = new byte[color + 6];
					headerBuffer.get(str);
					defaultColor = new Color(headerBuffer.get() & 0xFF,
							headerBuffer.get() & 0xFF,
							headerBuffer.get() & 0xFF,
							headerBuffer.get() & 0xFF);
				}
			}
			data.read(facesCount);
			dataBuffer = ByteBuffer.wrap(facesCount);
			dataBuffer.order(ByteOrder.nativeOrder());
			numberOfFaces = dataBuffer.getInt();
			ioBuffer = new byte[50 * numberOfFaces];
			data.read(ioBuffer);
			dataBuffer = ByteBuffer.wrap(ioBuffer);
			dataBuffer.order(ByteOrder.nativeOrder());
			verticies = new ArrayList<MeshVertex>(numberOfFaces * 3);
			indicies = new ArrayList<Integer>(numberOfFaces * 3);
			int perc = 0;
			for (int i = 0; i < numberOfFaces; i++) {
				int pp = (int) (100 * i / (float) numberOfFaces);
				if (pp > perc) {
					System.out.print("\r" + pp);
					perc = pp;
				}
				try {
					readFacetB(dataBuffer, i);
				} catch (IOException e) {
					System.out.println("Format Error: iteration number " + i);
				}
			}
			indicies.trimToSize();
			verticies.trimToSize();
		}
		if (mergeVerticies) {
			System.out.println("Verticies: " + verticies.size()
					+ ", Indicies: " + indicies.size());
		}
		data.close();
	}

	public static Mesh loadSTL(File f) throws IOException {
		BinarySTLLoader loader = new BinarySTLLoader();
		loader.loadBinarySTL(f);
		Mesh m = new Mesh(loader.verticies, loader.indicies, 3);
		if (loader.defaultColor != null) {
			m.defaultColor = new FloatBufferColor(
					loader.defaultColor.getRed() / 255f,
					loader.defaultColor.getGreen() / 255f,
					loader.defaultColor.getBlue() / 255f,
					loader.defaultColor.getAlpha() / 255f);
		}
		return m;
	}
}