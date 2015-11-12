//Ong Mu Sen Jeremy A0108310Y

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.*;
import java.util.zip.*;

public class FileReceiver {
	public static void main(String args[]) throws Exception
    {
		if (args.length != 1) {
			System.err.println("Usage: FileReceiver <port>");
			System.exit(-1);
		}
		int port = Integer.parseInt(args[0]);
		File file = new File("");
		DatagramSocket socket = new DatagramSocket(port);
		byte[] obt = new byte[1000];
		byte[] sending = new byte[12];
		int seq = -1;
		DatagramPacket pkt = new DatagramPacket(obt, obt.length);
		DatagramPacket ack;
		ByteBuffer b = ByteBuffer.wrap(obt);
		ByteBuffer c = ByteBuffer.wrap(sending);
		CRC32 crc = new CRC32();
		while(true){
			pkt.setLength(obt.length);
			socket.receive(pkt);
			b.rewind();
			long csum = b.getLong(0);
			crc.reset();
			crc.update(obt, 8, obt.length - 8);
			long compare = crc.getValue();
			int current = b.getInt(8);
			if(compare != csum || current != seq){
				c.rewind();
				c.putLong(0);
				c.putInt(seq-1);
				crc.reset();
				crc.update(sending, 8, sending.length - 8);
				csum = crc.getValue();
				c.rewind();
				c.putLong(csum);
				ack = new DatagramPacket(sending, sending.length, pkt.getSocketAddress());
				socket.send(ack);
				continue;
			}
			else{
				c.rewind();
				c.putLong(0);
				c.putInt(seq);
				crc.reset();
				crc.update(sending, 8, sending.length - 8);
				csum = crc.getValue();
				c.rewind();
				c.putLong(csum);
				ack = new DatagramPacket(sending, sending.length, pkt.getSocketAddress());
				socket.send(ack);
				seq++;
			}
			int type = b.getInt(12);
			if(type == 1){
				int length = b.getInt(16);
				byte[] name = new byte[length];
				for(int j = 0; j < length; j++){
					name[j] = obt[20 + j];
				}
				file = new File(new String(name));
				FileOutputStream fileOutput = new FileOutputStream(file, true);
			}
			else if(type == 0){
				int length = b.getInt(16);
				byte[] data = new byte[length];
			//	System.out.println(length);
				for(int j = 0; j < length; j++){
					data[j] = obt[20 + j];
				}
				FileOutputStream fileOuput =  new FileOutputStream(file, true); 
			    fileOuput.write(data);
			    fileOuput.close();
			}
		}
    }
}
