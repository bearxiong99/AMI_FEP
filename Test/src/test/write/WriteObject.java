package test.write;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.hx.dlms.DlmsData;

public class WriteObject {
	public static void main(String[] args) {
		DlmsData dd = new DlmsData();
		dd.setVisiableString("cmnet");
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;  
		try {
			oos = new ObjectOutputStream(new FileOutputStream("ab.ser"));
			oos.writeObject(dd);
			ois = new ObjectInputStream(new FileInputStream("ab.ser"));
			DlmsData result = (DlmsData) ois.readObject();
			System.out.println(result.getVisiableString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}  

	}
}
