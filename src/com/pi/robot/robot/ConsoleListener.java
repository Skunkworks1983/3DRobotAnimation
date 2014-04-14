package com.pi.robot.robot;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ConsoleListener {
	private static final int PORT_IN = 6666;
	private static final int PORT_OUT = 6668;
	private static final int RECIEVE_BUFFER_SIZE = 4096;
	private final InetAddress destinationAddress;
	private DatagramSocket sockIn;
	private DatagramSocket sockOut;

	public ConsoleListener(int teamNumber) throws IOException {
		destinationAddress = InetAddress.getByName("10."
				+ (int) (teamNumber / 100) + "." + (teamNumber % 100) + ".2");
		sockIn = new DatagramSocket(PORT_IN);
		sockOut = new DatagramSocket(PORT_OUT);
		sockOut.setBroadcast(true);
	}

	public void sendCommand(String s) throws IOException {
		byte[] raw = s.concat("\n").getBytes();
		DatagramPacket pack = new DatagramPacket(raw, raw.length);
		pack.setSocketAddress(new InetSocketAddress(destinationAddress,
				PORT_OUT));
		sockOut.send(pack);
	}

	public String read() throws IOException {
		DatagramPacket read = new DatagramPacket(new byte[RECIEVE_BUFFER_SIZE],
				RECIEVE_BUFFER_SIZE);
		sockIn.receive(read);
		return new String(read.getData(), read.getOffset(), read.getLength());
	}
}