//Ong Mu Sen Jeremy A0108310Y

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.*;
import java.util.zip.*;

public class FileSender {
	public static void main(String args[]) throws Exception
	{
		if (args.length != 4) {
			System.err.println("Usage: FileSender <host> <port> <source> <destination>");
			System.exit(-1);
		}
		InetSocketAddress add = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
		File file = new File(args[2]);
		DatagramSocket socket = new DatagramSocket();
		DatagramPacket pkt;
		DatagramPacket rec;
		BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));
		CRC32 crc = new CRC32();
		byte[] reading = new byte[980];
		byte[] sending = new byte[1000];
		byte[] obt = new byte[12];
		ByteBuffer b = ByteBuffer.wrap(sending);

		//sending file name
		byte[] dest = args[3].getBytes();
		for(int i = 0; i < dest.length; i++){
			sending[i+20] = dest[i];
		}
		int seq = -1;
		b.rewind();
		b.putLong(0);
		b.putInt(-1);
		b.putInt(1);
		b.putInt(dest.length);
		crc.reset();
		crc.update(sending,  8,  sending.length - 8);
		long csum = crc.getValue();
		b.rewind();
		b.putLong(csum);
		pkt = new DatagramPacket(sending, sending.length, add);
		rec = new DatagramPacket(obt, obt.length);
		ByteBuffer c = ByteBuffer.wrap(obt);
		socket.send(pkt);

		socket.setSoTimeout(2);
		while(true){
			try{
				rec.setLength(obt.length);
				socket.receive(rec);
			} catch(SocketTimeoutException e){
		//		System.out.println("timeout");
				socket.send(pkt);
				continue;
			}
			c.rewind();
			Long compare = c.getLong(0);
			crc.reset();
			crc.update(obt, 8, obt.length - 8);
			csum = crc.getValue();
			int current = c.getInt(8);
		//	System.out.println("checksums are " + compare + " " + csum);
		//	System.out.println("Packet number are " + current + " " + seq);
			if(compare != csum || current != seq){
		//		System.out.println("wrong");
				socket.send(pkt);
				socket.setSoTimeout(2);
				continue;
			}
			else{
				seq++;
				break;
			}
		}
		int size = (int) file.length();
		int last = size % 980;
		int num = (int) Math.ceil((double)(size / 980));
		b = ByteBuffer.wrap(sending);
		int bit;
		while((bit = reader.read(reading)) != 1){
			b.rewind();
			b.putLong(0);
			b.putInt(seq);
			b.putInt(0);
			if(num != seq){
				b.putInt(980);
				for(int w = 0; w < reading.length; w++){
					sending[20 + w] = reading[w];
				}
			}
			else{
				b.putInt(last);
				for(int q = 0; q < last; q++){
					sending[20 + q] = reading[q];
				};
			}
			crc.reset();
			crc.update(sending,  8,  sending.length - 8);
			csum = crc.getValue();
			b.rewind();
			b.putLong(csum);
			pkt = new DatagramPacket(sending, sending.length, add);
			c = ByteBuffer.wrap(obt);
			socket.send(pkt);
			socket.setSoTimeout(2);
			while(true){
				try{
					rec.setLength(obt.length);
					socket.receive(rec);
				} catch(SocketTimeoutException e){
			//		System.out.println("timeout");
					socket.send(pkt);
					continue;
				}
				c.rewind();
				Long compare = c.getLong(0);
				crc.reset();
				crc.update(obt, 8, obt.length - 8);
				csum = crc.getValue();
				int current = c.getInt(8);
			//	System.out.println("The checksum " + compare + " " + csum);
			//	System.out.println("The packet " + current + " " + seq);
				if(compare != csum || current != seq){
			//		System.out.println("wronger");
					socket.send(pkt);
					socket.setSoTimeout(2);
					continue;
				}
				else{
					seq++;
					break;
				}
			}
			if(seq > num){
				break;
			}
		}
		socket.close();
		reader.close();
	}
}
