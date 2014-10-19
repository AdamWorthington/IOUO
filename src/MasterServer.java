import java.net.*;
import java.io.*;

import javax.swing.JFrame;

public class MasterServer{
	
	static final int maxLog = 100;
	static DispatchServer serverList[] = new DispatchServer[maxLog]; 
	static String nameList[] = new String[maxLog];

	public static void main(String[] args) throws IOException {
		int checkLogin = 0;
		if (args.length != 1) {
			System.err.println("Usage: java MasterServer <port number>");
			System.exit(1);
		}
		int portNumber = Integer.parseInt(args[0]);
		new recieveThreadMain(maxLog).start();

		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
			while (true) {
				DispatchServer curr = new DispatchServer(serverSocket.accept(), checkLogin = assignLogin(), maxLog);
				curr.start();
				System.out.println("starting connection...");
				if(checkLogin >= 0){
					serverList[checkLogin] = curr;
				}
			}
		} catch (IOException e) {
			System.err.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}	
	}
	public static int assignLogin(){
		for(int i = 0; i < maxLog; i++){
			if(MasterServer.nameList[i] == null){
				return i;
			}
		}
		return -1;
	}
}

class recieveThreadMain extends Thread {

	int maxLog;
	public JFrame frame;
	public recieveThreadMain(int maxLog){
		this.maxLog = maxLog;
	}
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line;
		
		
		try {
			while ((line = reader.readLine()) != null) {
				processMessage(line);
			}
		} catch (IOException e) {
			print("Couldn't read the next line", 1);
			e.printStackTrace();
		}
	}
	
	public void print(String s, int type){
		if(type == 1){
			System.err.println(s);
		}
		else{
			System.out.println(s);
		}
		
	}

	private void processMessage(String line) {
		String instruction = "";
		int pos = 0;
		if(line.contains(" ")){
			pos = line.indexOf(" ");
			instruction = line.substring(0, pos);
		}
		else{
			instruction = line;
		}
		
		switch (instruction) {
		
		case "/msg": {
			
			line = line.substring(pos + 1);
			pos = line.indexOf(" ");
			String name = line.substring(0, pos);
			line = line.substring(pos + 1);
			boolean check = true;
			for (int i = 0; i < maxLog; i++) {
				if (MasterServer.nameList[i] != null) {
					if (MasterServer.nameList[i].equalsIgnoreCase(name)) {
					
						MasterServer.serverList[i].sendMessage("[SERVER]: " + line);
						check = false;
					}
				}
			}
			if(check){
				print("Couldn't find user: " + name, 0);
			}
			break;
		}
		case "/msg2": {
			line = line.substring(pos + 1);
			pos = line.indexOf(" ");
			String name = line.substring(0, pos);
			line = line.substring(pos + 1);
			MasterServer.serverList[Integer.parseInt(name)].sendMessage(line);
			break;
		}
		case "/slots": {
			for(int i = 0; i < maxLog; i++){
				if(MasterServer.serverList[i] != null){
					System.out.println("Position " + i + " is in use.");
				}
			}
			break;
		}

		case "/broadcast": {
			line = line.substring(pos + 1);
			
			for(int i = 0; i < maxLog; i++){
				if(MasterServer.serverList[i] != null){
					MasterServer.serverList[i].sendMessage("[SERVER BROADCAST]: " + line);
				}
			}
			break;
		}
		default: {
			print("The command \"" + instruction + "\" was not recognized.", 0);
			print("Please use the syntax \"/<command> <target> <info>\"", 0);
			
		}
		}
	}
}