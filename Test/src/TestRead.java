import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class TestRead {
	public static void main(String[] args) {
		TestRead r = new TestRead();
		r.readFile("C:\\Users\\Administrator\\Desktop\\HXFM12BHCLSPIF00301V10.bin");
	}
	
	public byte[] readFile(String remoteFile) {
	       try {
	           File file = new File(remoteFile);
	           FileInputStream is = new FileInputStream(remoteFile);
	           int size = (int) file.length();
	           byte[] bytes = getBytes(is,size);
	           System.out.println(bytes.length/198);
	           is.close();
	           return bytes;
	       } catch (IOException ex) {
	        }
	       return null;
	   }
	 
	 
	    private byte[] getBytes(InputStream inputStream,int size) {
	       byte[] bytes = new byte[size];
	       try {
	            inputStream.read(bytes);
	           return bytes;
	       } catch (Exception e) {
	           e.printStackTrace();
	       }
	       return null;
	    }
}
