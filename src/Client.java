import java.io.*;
import java.net.*;

public class Client {

	public static void main(String[] args) throws Exception {
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);
		Socket sock = new Socket(hostName, portNumber);
		BufferedReader keyRead = new BufferedReader(new InputStreamReader(
				System.in));
		OutputStream ostream = sock.getOutputStream();
		PrintWriter pwrite = new PrintWriter(ostream, true);
		// LoginGUI curr = new LoginGUI(pwrite);
		new recieveThread2(sock).start();
		// while (!curr.getStatus()) {
		// curr.logInGUI();
		// }
		String sendMessage;
		while (true) {
			sendMessage = keyRead.readLine();
			pwrite.println(sendMessage);
			System.out.flush();
		}
	}
}

class recieveThread2 extends Thread {
	Socket sock;

	// LoginGUI wGUI;
	public recieveThread2(Socket sock) {
		this.sock = sock;
		// this.wGUI = curr;
	}

	public void run() {
		String receiveMessage;
		InputStream istream = null;
		try {
			istream = sock.getInputStream();
		} catch (IOException e) {
			System.err.println("Couldn't establish input stream.");
			e.printStackTrace();
			System.exit(1);
		}
		BufferedReader receiveRead = new BufferedReader(new InputStreamReader(
				istream));
		while (true) {
			try {
				if ((receiveMessage = receiveRead.readLine()) != null) {

					System.out.println(receiveMessage);

				}
			} catch (IOException e) {
				System.out
						.println("Server dissconnected... Killing current application.");
				System.exit(1);
			}
		}
	}
}