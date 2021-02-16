import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class Server 
{
	private static ServerSocket listener;
	
	
	public static boolean validateIPAddress(final String ip) { //Source du code de cette fonction : https://stackoverflow.com/a/30691451
		   String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
		   return ip.matches(PATTERN);
	}
	
 public static void main (String[] args) throws IOException 
 {
	 
	 int clientNumber =0;

	 Scanner scannerPort = new Scanner(System.in);
	 Scanner scannerAddress = new Scanner(System.in);
	 
	 int serverPort;
	 String serverAddress;
	 
	 while (true)
		{
			System.out.println("Entrez un numero de port entre 5000 et 5050");
			serverPort = Integer.parseInt(scannerPort.nextLine());
			if (serverPort >= 5000 && serverPort <= 5050)
				break;
		}
	 
	 
	 while (true)
		{
			System.out.println("Entrez une adresse IP valide");
			serverAddress = scannerAddress.nextLine();
			if (validateIPAddress(serverAddress))
				break;
		}

	 
	 
	 listener = new ServerSocket();
	 listener.setReuseAddress(true);
	 InetAddress serverIP = InetAddress.getByName(serverAddress);
	 
	 listener.bind(new InetSocketAddress(serverIP, serverPort));
	 
	 try
	 {
		 while (true)
		 {
			 new ClientHandler(listener.accept(),clientNumber++).start();
		 }
	 }
	 finally
	 {
	 	listener.close();
	 }
 
 }
	 
	  
 private static class ClientHandler extends Thread
 {
	 private Socket socket;
	 private int clientNumber;
	 final int TAILLE_BUFFER = 8024;
	 
	 public ClientHandler (Socket socket, int clientNumber)
	 {
		 this.socket = socket;
		 this.clientNumber = clientNumber;
		 System.out.println("New connection with client#"+ clientNumber + " at " + socket);
	 }
	 
	 public void run()
	 {
		 try
		 {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("La connection au serveur a ete etablie");
			Boolean boucleCommandes = true;
			while (boucleCommandes)
			{
				//Lit la commande du client
				DataInputStream in = new DataInputStream(socket.getInputStream());
				String commande = in.readUTF();
				
				if (commande == "exit") 
				{
					boucleCommandes = false;
				}
				
				else if (commande.startsWith("mkdir")) {
					String nomFichier = commande.substring(6);
					File file = new File(System.getProperty("user.dir")+"\\"+nomFichier);
					if (file.mkdir()) {
						String dossierCree = String.format("Le dossier %s a ete cree", nomFichier);
						out.writeUTF(dossierCree);
					}
					else
						out.writeUTF("Le dossier existe deja");
				}
				
				
				else if (commande.startsWith("upload"))
				{
					InputStream inFichier = socket.getInputStream();
					String nomFichier = commande.substring(7);
					OutputStream outFichier = new FileOutputStream(nomFichier);
					byte[] buffer = new byte[16*1024];

			        int count;
			        while ((count = inFichier.read(buffer)) > 0) {
			            outFichier.write(buffer, 0, count);
			        }
			        
			        outFichier.flush();
				}
				
				else if (commande.startsWith("download"))
				{
					String nomFichier = commande.substring(9);

					File fichier = new File(nomFichier);
				    long length = fichier.length();
				    byte[] buffer = new byte[16 * 1024];
				    InputStream inFichier = new FileInputStream(fichier);
				    OutputStream outFichier = socket.getOutputStream();
				        
				    int count;
				    while ((count = inFichier.read(buffer)) > 0) {
				        outFichier.write(buffer, 0, count);
				     }
				    
				   outFichier.flush();
				}
				
				else if (commande.equals("ls")) {
					File f = new File(System.getProperty("user.dir"));
					File[] listOfFiles = f.listFiles();
					out.write(listOfFiles.length);
					for (int i = 0; i < listOfFiles.length; i++) {
					  if (listOfFiles[i].isFile()) {
					    out.writeUTF("[File] " + listOfFiles[i].getName());
					  } else if (listOfFiles[i].isDirectory()) {
					    out.writeUTF("[Folder] " + listOfFiles[i].getName());
					  }
					}
				}
				
				else if (commande.startsWith("cd")) {
					File f = new File(System.getProperty("user.dir"));
					if(commande.contains("...")) {
						System.setProperty("user.dir", f.getParent());
						File f2 = new File(System.getProperty("user.dir"));
						out.writeUTF("Vous êtes dans le dossier " + f2.getName());
					}
					else {
						String nomDossier = commande.substring(3);
						String path = f.getAbsolutePath();
						System.setProperty("user.dir", path + "\\" + nomDossier);
						out.writeUTF("Vous êtes dans le dossier " + nomDossier);
					}
				}
				
			}
		
			 
		 } catch (IOException e)
		 {
			 System.out.println("Error handling client#" + clientNumber + ": " + e);
		 }
		 
		 
		 finally
		 {
			 try 
			 {
				 socket.close();
			 }
			 catch (IOException e)
			 {
				 System.out.println("Couldn't close a socket");
				
			 }
			 System.out.println("Connection with client closed");
		 }
		  
	 	}
 	} 
 }
 
