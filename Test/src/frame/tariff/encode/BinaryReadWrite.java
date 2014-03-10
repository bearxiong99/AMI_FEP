package frame.tariff.encode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class BinaryReadWrite {

	private DataInputStream dis = null;

	private DataOutputStream dos = null;

	private String s_FilePath = "d:/bin.dat";

	private byte[] m_datapadding = { 0x00 }; //填充空白，以补足字节位数.

	public BinaryReadWrite() {
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {
		try {
			if (!new File(s_FilePath).exists()) {
				new File(s_FilePath).createNewFile();
			}
			dis = new DataInputStream(new FileInputStream(new File(s_FilePath)));
			dos = new DataOutputStream(new FileOutputStream(new File(s_FilePath)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeBinaryStream(String bytes) {
		try {
			if (dos != null) {
				for (int i = 0; i < 1; i++) {
					//write boolean value.
					dos.writeBoolean(true);
						
						//"C1 01 00 00 14 00 00 0D 00 00 FF 05 00010102021101010102030904020A0000090600000A0064FF120001"
					//dos.write(b);
					//dos.writeChars("\n");
//					dos.writeChars("C10100001400000D0000FF0500010102021101010102030904020A0000090600000A0064FF120001");
//					dos.writeChars("\n");
//					dos.writeChars("C10100001400000D0000FF08000101020809067765656B30301104110511021101110211041101");
//					dos.writeChars("\n");
//					dos.writeChars("C10100001400000D0000FF070001010203090673656173306E090C07DC0B0001FFFFFF0080000009077765656B303020");
//					dos.writeChars("\n");
//					dos.writeChars("C10100000B00000B0000FF0200010102031200010905056F0615031101");

					dos.writeChars(bytes);
				}
				dos.flush();
				dos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readBinaryStream() {
		try {
			if (dis != null) {
				while (dis.available() > 0) {
					System.out.println(dis.available());
					System.out.println(dis.readBoolean());
					char c = (char) dis.readChar();
					System.out.println(c);
					System.out.println(dis.readDouble());
					System.out.println(dis.readFloat());
					System.out.println(dis.readInt());
					System.out.println(dis.readLong());
					System.out.println(dis.readShort());
					System.out.println(dis.readUTF());
					System.out.println(dis.read(m_datapadding));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		BinaryReadWrite bin = new BinaryReadWrite();
		//bin.writeBinaryStream();
		//bin.readBinaryStream();
	}
}