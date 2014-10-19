import java.io.*;
import java.net.*;

public class DispatchServer extends Thread {

	public Socket sock; // Socket name for reference
	PrintWriter pwrite; // Writing tool to talk to client
	int id = -1;
	@SuppressWarnings("unused")
	private Socket socket;
	int maxLog;
	String mainDirPath = System.getProperty("user.dir") + "/Groups/";

	public DispatchServer(Socket socket, int counter, int maxLog) {
		super("MasterServer");
		this.sock = socket;
		this.id = counter;
		this.maxLog = maxLog;
	}

	public void run() {
		OutputStream ostream = null;
		try {
			ostream = sock.getOutputStream();
		} catch (IOException e1) {
			System.err.println("couldn't get output stream.");
			e1.printStackTrace();
		}
		pwrite = new PrintWriter(ostream, true);
		if (id == -1) {
			pwrite.println("Server is too busy at the moment. Try again later. SERVER_COMMAND:EXIT");
			return;
		} else {
			new recieveThread(sock, pwrite, id, maxLog).start();
		}
	}

	public void sendMessage(String input) {
		pwrite.println(input);
		System.out.println("Sent: " + input);
	}
}

class recieveThread extends Thread {
	BufferedReader receiveRead;
	String currentUser;
	Socket sock;
	PrintWriter pwrite;
	int id;
	int maxLog;
	String mainDirPath = System.getProperty("user.dir") + "/Groups/";
	String total = "";

	public recieveThread(Socket socket, PrintWriter pwriter, int id, int maxLog) {
		this.sock = socket;
		this.pwrite = pwriter;
		this.id = id;
		this.maxLog = maxLog;
	}

	public void run() {
		String receiveMessage;
		InputStream istream = null;
		try {
			istream = sock.getInputStream();
			System.out.println("Established Input Stream");
		} catch (IOException e) {
			System.err.println("Couldn't establish input stream.");
			e.printStackTrace();
			System.exit(1);
		}
		receiveRead = new BufferedReader(new InputStreamReader(istream));

		boolean nameMessage = true;
		while (true) {
			try {
				if ((receiveMessage = receiveRead.readLine()) != null) {
					if (nameMessage) {
						System.out.println("name added");
						currentUser = receiveMessage;
						MasterServer.nameList[id] = currentUser;
						nameMessage = !nameMessage;
					} else {
						processMessage(receiveMessage, "normal");
						System.out.println("recieved: " + receiveMessage);
					}
				} else {
					removeOnline();
					return;
				}
			} catch (IOException e) {
				removeOnline();
				return;
			}
		}
	}

	private void sendMessage(String s) {
		pwrite.println(s);
	}

	private void processMessage(String input, String special) {
		String firstWord;
		if (input.contains(" ")) {
			firstWord = input.substring(0, input.indexOf(" "));
			input = input.substring((input.indexOf(" ") + 1));
		} else {
			firstWord = input;
		}

		// Start commands

		switch (firstWord) {
		
		case "/kill" :{
			pwrite.close();
			try {
				receiveRead.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				sock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		
		case "/addgroup": {
			new File(mainDirPath + input).mkdir();
			processMessage("/addmember " + input + " temp", "normal");
			break;
		}
		case "/deletegroup": {
			deleteDir(new File(mainDirPath + input));
			break;
		}
		case "/addmember": {
			firstWord = input.substring(0, input.indexOf(" "));
			input = input.substring((input.indexOf(" ") + 1));
			
			try {
				new File(mainDirPath + firstWord + "/" + input + ".txt").createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!input.equals("temp")){
				addMember(new File(mainDirPath + firstWord + "/"), input);
			}
			break;
		}
		case "/deletemember": {
			firstWord = input.substring(0, input.indexOf(" "));
			input = input.substring((input.indexOf(" ") + 1));
			deleteDir(new File(mainDirPath + firstWord + "/" + input + ".txt"));
			break;
		}
		case "/alterValues": {
			String temp1Temp = "";
			String temp1 = "";
			String lastWordTemp = "";
			String lastWord = "";
			firstWord = input.substring(0, input.indexOf(" "));
			input = input.substring((input.indexOf(" ") + 1));
			lastWordTemp = input.substring(0, input.indexOf(" "));
			lastWord = lastWordTemp.substring(lastWordTemp.indexOf(" ") + 1);
			input = input.substring(lastWord.length() + 1);
			temp1Temp = input.substring(0, input.indexOf(" "));
			temp1 = temp1Temp.substring(temp1Temp.indexOf(" ") + 1);
			input = input.substring(temp1.length() + 1);
			alterValues(new File(mainDirPath + firstWord + "/"), lastWord, temp1, input);
			break;
		}
		case "/update": {
			firstWord = input.substring(0, input.indexOf(" "));
			input = input.substring((input.indexOf(" ") + 1));
			
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(mainDirPath + "/" + firstWord + "/" + currentUser + ".txt");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			//Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		 
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					if(line.startsWith(input)){
						String temp = line.substring(line.indexOf(" ") + 1);
						sendMessage(temp);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		}
		case "/groups": {
			populateGroups(new File(mainDirPath), 0);
			if(total.length() > 0){
				total = total.substring(0, total.length() - 1);
			}
			sendMessage(total);
			total = "";
			break;
		}
		case "/members": {
			populateMembers(input);
			if(total.length() > 0){
				total = total.substring(0, total.length() - 1);
			}
			sendMessage(total);
			total = "";
			break;
		}
		default: {

			break;
		}
		}
		// End commands
	}
	
	private void populateMembers(String fileName){
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(mainDirPath + "/" + fileName + "/" + currentUser + ".txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		//Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				String temp = line.substring(0, line.indexOf(" "));
				total = total + temp + "|";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void populateGroups(final File folder, int depth) {
		if(folder.toPath().endsWith(".txt") && depth == 0){
			return;
		}
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            populateGroups(fileEntry, depth+1);
	        } 
	        else {
	            if(depth == 1 && fileEntry.getName().equals(currentUser + ".txt")){
	            	
	            	String temp = folder.getPath().substring(mainDirPath.length());
	            	
	            	total = total + temp + "|";
	            }
	        }
	    }
	}

	
	private void alterValues(File folder, String nameInc, String nameDec, String amountS){
		double amount = Double.parseDouble(amountS);
		for (final File fileEntry : folder.listFiles()) {
			if(fileEntry.getName().equals(nameInc + ".txt")){
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(fileEntry);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Construct BufferedReader from InputStreamReader
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			 
				String line = null;
				try {
					while ((line = br.readLine()) != null) {
						if(line.contains(nameDec)){
							String nameTemp = line.substring(0, line.indexOf(" "));
							String currAmountTemp = line.substring(line.indexOf(" ") + 1);
							double currAmount = Double.parseDouble(currAmountTemp);
							currAmount = currAmount + amount;
							try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(folder.getPath() + "/temp.txt", true)))) {
							    out.println(nameTemp + " " + currAmount);
							}catch (IOException e) {
							    //TODO
							}
						}
						else{
							try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(folder.getPath() + "/temp.txt", true)))) {
							    out.println(line);
							}catch (IOException e) {
							    //TODO
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				deleteDir(new File(folder.getPath() + "/" + nameInc));
				// File (or directory) with old name
			    File file = new File(folder.getPath() + "/" + "temp.txt");

			    // File (or directory) with new name
			    File file2 = new File(folder.getPath() + "/" + nameInc + ".txt");

			    // Rename file (or directory)
			    file.renameTo(file2);
				
			    try {
					new File(folder.getPath() + "/temp.txt").createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(fileEntry.getName().equals(nameDec + ".txt")){
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(fileEntry);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Construct BufferedReader from InputStreamReader
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			 
				String line = null;
				try {
					while ((line = br.readLine()) != null) {
						if(line.contains(nameInc)){
							String nameTemp = line.substring(0, line.indexOf(" "));
							String currAmountTemp = line.substring(line.indexOf(" ") + 1);
							double currAmount = Double.parseDouble(currAmountTemp);
							currAmount = currAmount - amount;
							try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(folder.getPath() + "/temp.txt", true)))) {
							    out.println(nameTemp + " " + currAmount);
							}catch (IOException e) {
							    //TODO
							}
						}
						else{
							try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(folder.getPath() + "/temp.txt", true)))) {
							    out.println(line);
							}catch (IOException e) {
							    //TODO
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				deleteDir(new File(folder.getPath() + "/" + nameDec));
				// File (or directory) with old name
			    File file = new File(folder.getPath() + "/" + "temp.txt");

			    // File (or directory) with new name
			    File file2 = new File(folder.getPath() + "/" + nameDec + ".txt");

			    // Rename file (or directory)
			    file.renameTo(file2);
				
			    try {
					new File(folder.getPath() + "/temp.txt").createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void addMember(final File folder, String name) {
		for (final File fileEntry : folder.listFiles()) {
			if(!fileEntry.getName().equals(name + ".txt") && !fileEntry.getName().equals("temp.txt")){
				try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(folder.getPath() + "/" +fileEntry.getName(), true)))) {
				    out.println(name + " 0.00");
				}catch (IOException e) {
				    
				}
				try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(folder.getPath() + "/" + name + ".txt", true)))) {
				    out.println(fileEntry.getName().substring(0, fileEntry.getName().indexOf('.')) + " 0.00");
				}catch (IOException e) {
				    
				}
			}
		}
	}

	private boolean deleteDir(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDir(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		return (directory.delete());
	}

	private void removeOnline() {
		MasterServer.nameList[id] = null;
		MasterServer.serverList[id] = null;
	}
}