import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static Socket socket;
	 final static int TAILLE_BUFFER = 4*1024;
	
	public static boolean validerIPAdresse(final String ip) { //Source du code de cette fonction : https://stackoverflow.com/a/30691451
		   String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
		   return ip.matches(PATTERN);
	}
	
	
	public static void main (String[] args) throws IOException {		 
		 Scanner scannerPort = new Scanner(System.in);
		 Scanner scannerAddress = new Scanner(System.in);
		 
		 int port;
		 String serverAddress;
		 
		 // Validation du numero de port et de l'adresse IP
		 while (true)
			{
				System.out.println("Entrez un numero de port entre 5000 et 5050");
				port = Integer.parseInt(scannerPort.nextLine());
				if (port >= 5000 && port <= 5050)
					break;
			}
		
		 while (true)
			{
				System.out.println("Entrez une adresse IP valide");
				serverAddress = scannerAddress.nextLine();
				if (validerIPAdresse(serverAddress))
					break;
			}
		
		socket=new Socket(serverAddress,port);
		
		
		//Confirmation du serveur que la connection client-serveur est établie
		DataInputStream in= new DataInputStream(socket.getInputStream());
		System.out.println(in.readUTF());
		  
		Scanner commandeScanner = new Scanner(System.in);
		Boolean boucleCommandes =true;
		while (boucleCommandes)
		{
			System.out.println("Veuillez entrer une commande :");
			String commande = commandeScanner.nextLine();
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			
			if (commande.equals("exit"))
			{
				out.writeUTF(commande);
				boucleCommandes = false;
			}
			else if (commande.contains("mkdir"))
			{
				out.writeUTF(commande); 
				System.out.println(in.readUTF());
			}

			else if (commande.startsWith("upload"))
			{
				String nomFichier = commande.substring(7);
				out.writeUTF(commande);
				
				int bytes =0;
			
				File fichier = new File(nomFichier);
				FileInputStream fileInput = new FileInputStream(fichier);
				
				out.writeLong(fichier.length());
				
				//Separe le fichier en  morceaux
			    byte[] buffer = new byte[TAILLE_BUFFER];
			    
			    while ((bytes=fileInput.read(buffer))!=-1){
		            out.write(buffer,0,bytes);
		            out.flush();
		        }
			    
			    fileInput.close();
			    System.out.println(in.readUTF());
			    
			}	
			
			else if (commande.startsWith("download"))
			{
				int bytes =0;
				String nomFichier = commande.substring(9);
				FileOutputStream fileOutput = new FileOutputStream(nomFichier);
				
				out.writeUTF(commande);
				
				long tailleFichier = in.readLong();
		        
				 byte[] buffer = new byte[TAILLE_BUFFER];
			        while (tailleFichier > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, tailleFichier))) != -1) {
			            fileOutput.write(buffer,0,bytes);
			            tailleFichier -= bytes;     
			        }
			        
			        fileOutput.close();
			        System.out.println(in.readUTF());
			}
			
			else if (commande.equals("ls"))
			{
				out.writeUTF(commande);
				int nbObjets = in.read();
				for(int i=0; i < nbObjets; i++) {
					System.out.println(in.readUTF());
				}
			}
			
			else if (commande.startsWith("cd")) {
				out.writeUTF(commande);
				System.out.println(in.readUTF());
			}
		
			
		}
		socket.close();
		System.out.println("Vous avez été déconnecté avec succès");
			
	 }	
}