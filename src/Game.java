

public class Game{
	char matrix[][] = new char[3][3];
	static int desloc[][] = { {-1, 0}, {-1, 1}, {0,1}, {1,1}, {1,0}, {1,-1}, {0,-1} , {-1,-1} };
	Player a, b;
	int turn;
	
	public Game(Player _a, Player _b) {
		turn = 0;
		a=_a; b=_b;
		for(int i=0; i<3; i++) for(int j=0;j<3; j++) matrix[i][j]='.';
			
		
	}
	
	public boolean verifyDraw(){
		for(int i=0; i < 3; i++) for(int j=0; j<3;j++) if(matrix[i][j]=='.') return false;
		return true;
	}
	
	public boolean verifyEndGame(int r, int c, char search) {
		int cont =0,  auxR=r, auxC=c;
		if(matrix[0][2] == 'X') {
			auxR = r;
		}
		for(int i=0; i < 8; i++) {
			int dx = desloc[i][0], dy = desloc[i][1]; 
			r=auxR; c=auxC;
			cont=0;
			while( r <= 2 && r >= 0 && c <= 2 && c >= 0 ) {
				cont +=  (matrix[r][c] == search) ? 1 : 0;
				r+=dx; c+=dy;
			}
			if(cont>=3) return true;
		}
		return false;
	}
	
	public String actualMatrix(){
		String s = "";
		for(int i =0; i < 3; i++){
			for(int j=0; j<3; j++){
				s += matrix[i][j]+" ";
			}
			s+="\n";
		}
		return s;
	}
	
	/**
	 * <p> 0 = marcou onde ja tinha
	 * <p> 1 = acabou o jogo
	 * <p> 2 = nao acabou o jogo
	 * <p> 3 = empate
	 * 
	 * */
	public int mark(int i, int j) {
		
		if(matrix[i][j] == '.') {
			Player p = (turn==0) ? a : b;
			matrix[i][j] = p.mark;
			turn++;
			turn%=2;
			if(verifyEndGame(i, j, matrix[i][j])) {p.score++; return 1;}
			else {
				if(verifyDraw()) { // draw
					return 3;
				}
				else return 2;
			}
		}
		return 0;
	}
	
	
	
	public static void main(String[] args) {
		Player a = new Player('X');
		Player b = new Player('0');
		Game g = new Game(a, b);
	}
}
