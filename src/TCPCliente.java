
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

class Message {
	int opCode;
	String map, response;
	Message(int _opCode, String _map, String _response){
		opCode = _opCode;
		map = _map;
		response = _response;
	}
	static Message parseMessage(String message){
		int opCode = Character.getNumericValue(message.charAt(0));
		int hifen = message.indexOf('-');
		String map = message.substring(1, hifen);
		int cont = 0;
		int pos = 0;
		String realMap = "";
		while ( cont < 9) {
			if(! (map.charAt(pos) == ' ' ) ) {
				realMap += map.charAt(pos) + " ";
				cont++;
				if(cont%3==0) realMap += "\n";
			}
			pos++;
		}
		
		String response = message.substring(hifen+1);	
		return new Message(opCode, realMap, response);
	}
}

class User{
	Scanner in = new Scanner(System.in);
	
	boolean incorrectInput(String res) {
		if(res.length() !=  3) return false;		
		if( res.charAt(1) != ' ') return false;
		if( Character.getNumericValue(res.charAt(0))>2 || Character.getNumericValue(res.charAt(0))<0 ) return false;
		if ( Character.getNumericValue(res.charAt(2))>2 || Character.getNumericValue(res.charAt(2))<0 ) return false;
		return true;
	}
	String readMark(){
		String res = "";
		System.out.println("Digite uma posição 'a b' da matriz para marcar\n->");
		res = in.nextLine();
		while (incorrectInput(res)) {
			System.out.println("Digite uma entrada valida\n->");
			res = in.nextLine();
		}
		return res;
	}

	void sendAlert(Message message){
		System.out.println("Jogo atual:\n"  + message.map  +"\n"+message.response);
	}
}

public class TCPCliente {
	Socket clientSocket;
	BufferedReader in;
	DataOutputStream out;
	User user;
	public TCPCliente() throws UnknownHostException, IOException {
		clientSocket = new Socket("localhost", 2000);
		in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream())  );
		out = new DataOutputStream( clientSocket.getOutputStream() );
		user = new User();
	}
	
	void sendData(String data) throws IOException{
		out.writeBytes(data+'\n');
	}
	
	@SuppressWarnings("unused")
	Message receiveData() throws IOException{
		String messageFromServer = null;
		while(messageFromServer==null || messageFromServer.length()==0) { // precisa de 4 leituras
			messageFromServer = in.readLine();
			messageFromServer += in.readLine();
			messageFromServer += in.readLine();
			messageFromServer += in.readLine();
			
		}
		Message message = Message.parseMessage(messageFromServer);
		return message;
	}
	
	void exitGame() throws IOException{
		in.close();
		out.close();
		clientSocket.close();
	}
	
	void clienteLoop() throws IOException{
		String initialMap = ". . .\n. . .\n. . .";
		System.out.println(initialMap);
		System.out.println("Espere por sua vez.");
		Message message;
		String mark;
		int opCode;
		while(true){
			message = this.receiveData();
			opCode = message.opCode;
			if(opCode==6 || opCode==0 || opCode == 5) { // marcou ocupado
				if(! (opCode==6))this.user.sendAlert(message);
				mark = this.user.readMark();
				this.sendData(mark);
			}
			else if(opCode == 1 || opCode==3 || opCode == 4){ //venceu, perdeu ou deu velha
				this.user.sendAlert(message);
				this.exitGame();
				break;
			}
			else if(opCode==2){ // jogou
				this.user.sendAlert(message);
			}			
		}
	}
	
	
	
	public static void main(String[] args) {
		try {
			TCPCliente tcpClient = new TCPCliente();
			tcpClient.clienteLoop();

		} catch(Exception e) {
			System.out.println("Erro: "  +e.getMessage());
			e.printStackTrace();
		}
	}
}
