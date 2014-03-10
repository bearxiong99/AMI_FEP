import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;


public class FtpFileReader {

	private String username;
	
	private String password;
	
	private String ip;
	
	private String port;
	
	private FTPClient ftpClient;
	
	public FtpFileReader(String username,String passowrd,String ip,int port,String path){
		ftpClient = new FTPClient();
		try {
			ftpClient.connect(ip,port);
			ftpClient.login(username, passowrd);
			 if(path != null && path.length() > 0){
               ftpClient.changeWorkingDirectory(path);	//跳转到指定目录
			 }
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeServer() {
		if (ftpClient.isConnected()) {
			try {
				ftpClient.logout();
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String readFile(String fileName){
		String result = "";
		InputStream ins = null;
		try {
			ins = ftpClient.retrieveFileStream(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					ins));
			String inLine = reader.readLine();
			while (inLine != null) {
				result +=inLine;
				inLine = reader.readLine();
			}
			reader.close();
			if (ins != null) {
				ins.close();
			}
			ftpClient.getReply();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public final String getUsername() {
		return username;
	}

	public final void setUsername(String username) {
		this.username = username;
	}

	public final String getPassword() {
		return password;
	}

	public final void setPassword(String password) {
		this.password = password;
	}

	public final String getIp() {
		return ip;
	}

	public final void setIp(String ip) {
		this.ip = ip;
	}

	public final String getPort() {
		return port;
	}

	public final void setPort(String port) {
		this.port = port;
	}
	
	public static void main(String[] args) {
		FtpFileReader ftp = new FtpFileReader("hexing", "hexing", "172.16.251.239", 21, "ftp\\fep");
		System.out.println(ftp.readFile("PrepayMeterV02.bin"));
	}
}
