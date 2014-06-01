

import java.awt.Paint;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

class PlayerData {
	Player p;
	Socket s;
	BufferedReader in;
	DataOutputStream out;
	PlayerData(Player _p, Socket _s){
		s = _s;
		p = _p;
	}
	void sendData (String data) throws IOException{
		out = new DataOutputStream(  s.getOutputStream() );
		out.writeBytes(data + '\n');
	}
	String receiveData() throws IOException{
		in = new BufferedReader( new InputStreamReader(s.getInputStream()));
		return in.readLine();
	}
}

public class TCPServer {
	final static String RES_OCCUPED = "Jogue outra posicao, esta ja esta OCUPADA!!!";
	final static String RES_WIN = "Voce venceu, parabens!";
	final static String RES_PLAYED = "Boa jogada, agora espere por sua vez.";
	final static String RES_LOOSE = "Voce perdeu, talvez na proxima!";
	final static String RES_DRAW = "Eita, deu velha!!!";
	final static String RES_OPONENTPLAY = "Olha la onde o seu oponente jogou!";
	final static String RES_NONE = "...";
	ServerSocket welcomeSocket;
	PlayerData p1, p2;
	Game g;
	
	public TCPServer() throws IOException { 
		welcomeSocket = new ServerSocket(2000);
	}
	
	void waitForPlayers() throws IOException{
		Player player1, player2;
		Socket s;
		// player 1
		s = welcomeSocket.accept();
		player1 = new Player('X');
		p1 = new PlayerData(player1, s);
		System.out.println("Player 1 conectado");
		// player 2
		s = welcomeSocket.accept();
		player2 = new Player('O');
		p2 = new PlayerData(player2, s);
		System.out.println("Player 2 conectado");
		g = new Game(player1, player2);
	}
	
	void exitGame() throws IOException{
		p1.s.close();
		p2.s.close();
		welcomeSocket.close();
	}
	
	/**
		true se o jogo acabou
	 * */
	boolean first = true;
	boolean mark() throws IOException{
		PlayerData p = (g.turn==0 ? p1 : p2); // playing
		PlayerData pSec =  (g.turn ==0 ? p2 : p1);  // waiting
		
		if(first) {
			p.sendData("6"+g.actualMatrix()+"-"+RES_NONE);
			first = false;
			return false;
		}
		
		String data = p.receiveData();
		int i = Character.getNumericValue(data.charAt(0));
		int j = Character.getNumericValue(data.charAt(2));
		boolean endGame = false;
		int response = g.mark(i, j);
		String responseData = ""+response+ g.actualMatrix() + '-';
		String responsePlayerSecData = "";
		if(response == 0 ) { // marcou onde ja tinha
			responseData += RES_OCCUPED;
			p.sendData(responseData);
		}
		else if (response ==1 ){ // acabou o jogo venceu
			responseData += RES_WIN;
			p.sendData(responseData);
			responsePlayerSecData = "4"+ g.actualMatrix() + '-' + RES_LOOSE;
			pSec.sendData(responsePlayerSecData);
			endGame = true;
		}
		else if(response==3){ // velha
			responseData += RES_DRAW;
			p.sendData(responseData);
			responsePlayerSecData = "3"+g.actualMatrix()+'-'+RES_DRAW;
			pSec.sendData(responsePlayerSecData);
			return true;
		}
		else if(response==2){ // o jogo continua
			responseData += RES_PLAYED;
			p.sendData(responseData);
			responsePlayerSecData = "5" + g.actualMatrix()+'-' + RES_OPONENTPLAY;
			pSec.sendData(responsePlayerSecData);
		}
		return endGame;
	}
	
	void serverLoop() throws IOException{
		boolean endGame;
		while(true) {
			endGame = mark();
			if(endGame) break;
		}
	}
	

	public static void main(String[] args) {
		try {
			TCPServer tcpServer = new TCPServer();
			tcpServer.waitForPlayers();
			tcpServer.serverLoop();
			tcpServer.exitGame();
			
		} catch (IOException e) {
			System.out.println("Erro: " + e.getMessage());
			e.printStackTrace();
		}

		
		
	}
	
}
