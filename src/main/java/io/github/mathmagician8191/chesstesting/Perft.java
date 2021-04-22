package io.github.mathmagician8191.chesstesting;

import io.github.mathmagician8191.chessgame.Game;
import io.github.mathmagician8191.chessgame.Board;
import io.github.mathmagician8191.chessgame.Piece;
import io.github.mathmagician8191.chessgame.Engine;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class Perft implements Callable {
  //positions for standard chess with results included
  static Perft[] positions = new Perft[] {
    /*
    standard chess positions taken from https://www.chessprogramming.org/Perft_Results
    variant positions may be wrong if a move validation bug exists
    */
    new Perft(new Game("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",2,2,1,8,"qrbn",false),new int[]{1,6,264,9467,422333,15833292,706045033}),
    new Perft(new Game("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",2,2,1,8,"qrbn",false),new int[]{1,48,2039,97862,4085603,193690690}),
    new Perft(new Game("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",2,2,1,8,"qrbn",false),new int[]{1,14,191,2812,43238,674624,11030083,178633661}),
    new Perft(new Game("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10",2,2,1,8,"qrbn",false),new int[]{1,46,2079,89890,3894594,164075551}),
    new Perft(new Game("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",2,2,1,8,"qrbn",false),new int[]{1,20,400,8902,197281,4865609,119060324}),
    new Perft(new Game("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",2,2,1,8,"qrbn",true),new int[]{1,39,1519,63034,2598922,112776461}),
    new Perft(new Game("rnabqkbcnr/pppppppppp/10/10/10/10/10/10/PPPPPPPPPP/RNABQKBCNR w KQkq - 0 1",2,3,1,10,"qcarbn",false),new int[]{1,38,1444,60046,2486600,111941832}),
    new Perft(new Game("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",2,2,1,8,"qrbn",false),new int[]{1,44,1486,62379,2103487,89941194}),
    new Perft(new Game("wlfxkflw/pppppppp/8/8/8/8/PPPPPPPP/WLFXKFLW w - - 0 1",2,2,1,8,"xlwf",false),new int[]{1,20,400,8364,173199,3739062,80163965}),
    new Perft(new Game("ciamkaic/pppppppp/8/8/8/8/PPPPPPPP/CIAMKAIC w - - 0 1",2,2,1,8,"mcai",false),new int[]{1,32,1026,38132,1401550,56909620}),
    new Perft(new Game("hixakxih/pppppppp/8/8/8/8/PPPPPPPP/HIXAKXIH w - - 0 1",2,2,1,8,"ahix",false),new int[]{1,30,899,29509,958995,33463252}),
    new Perft(new Game("rnabqkbcnr/pppppppppp/10/10/10/10/PPPPPPPPPP/RNABQKBCNR w KQkq - 0 1",2,2,1,10,"qcarbn",false),new int[]{1,28,784,25228,805128,28741319}),
    new Perft(new Game("rrrqkrrr/bbbbbbbb/nnnnnnnn/pppppppp/PPPPPPPP/NNNNNNNN/BBBBBBBB/RRRQKRRR w KQkq - 0 1",4,1,1,8,"qrbn",false),new int[]{1,28,778,21974,617017,17962678}),
    new Perft(new Game("nnnnknnn/pppppppp/8/8/8/8/PPPPPPPP/NNNNKNNN w - - 0 1",2,2,1,8,"iznl",false),new int[]{1,28,784,21958,614381,17398402}),
    new Perft(new Game("rhbicqkaibhr/lfwnxzzxnwfl/pppppppppppp/12/12/12/12/12/12/PPPPPPPPPPPP/LFWNXZZXNWFL/RHBICQKAIBHR w KQkq - 0 1",3,3,1,12,"qcahirbzxnlwf",false),new int[]{1,50,2500,129386,6685727}),
    new Perft(new Game("qkbnr/ppppp/5/5/PPPPP/QKBNR w Kk - 0 1",2,1,1,5,"qrbn",false),new int[]{1,7,49,457,4065,44137,476690,5914307}),
    new Perft(new Game("rnobqkbonr/pppppppppp/10/10/10/10/10/10/PPPPPPPPPP/RNOBQKBONR w KQkq - 0 1",2,3,1,10,"qcarbno",false),new int[]{1,154,23632,3612238}),
    new Perft(new Game("rnabmkbcnr/pppppppppp/10/10/10/10/10/10/PPPPPPPPPP/RNABMKBCNR w KQkq - 0 1",2,3,1,10,"mqcarbn",false),new int[]{1,40,1600,71502,3178819}),
  };
  
  static Perft[] variant = new Perft[] {
    
  };
  
  Engine game;
  int[] results;
  
  public Perft(Engine game, int[] results) {
    //represents a test of perft against known results
    this.game = game;
    this.results = results;
  }
  
  public Perft(Game game, int[] results) {
    this.game = new Engine(game,0,0);
    this.results = results;
  }
  
  public static int perft(Game game, int depth) {
    //known good perft, used to validate fairy piece moves
    int result = 0;
    
    Board board = game.position;
    if (depth > 1) {
      //keep searching
      if (board.gameOver) {
        return 0;
      }
      for (int i=0;i<board.width;i++) {
        for (int j=0;j<board.height;j++) {
          Piece piece = board.boardstate[i][j];
          if (piece.isPiece && piece.side == (board.toMove ? 1 : -1)) {
            for (int k=0;k<board.width;k++) {
              for (int l=0;l<board.height;l++) {
                if (board.isMoveValid(new int[] {i,j}, new int[] {k,l})) {
                  Game newGame = new Game(game);
                  newGame.makeMove(new int[] {i,j}, new int[] {k,l});
                  if (newGame.position.promotionAvailable) {
                    //check all promotion options
                    String options = newGame.promotionOptions;
                    for (int m=0, length=options.length();m<length;m++) {
                      char letter = options.charAt(m);
                      Game newerGame = new Game(newGame);
                      newerGame.promotePiece(letter);
                      result += Perft.perft(newerGame,depth-1);
                    }
                  }
                  else {
                    result += Perft.perft(newGame,depth-1);
                  }
                }
              }
            }
          }
        }
      }
    }
    else {
      //count moves of last node
      for (int i=0;i<board.width;i++) {
        for (int j=0;j<board.height;j++) {
          Piece piece = board.boardstate[i][j];
          if (piece.isPiece && piece.side == (board.toMove ? 1 : -1)) {
            for (int k=0;k<board.width;k++) {
              for (int l=0;l<board.height;l++) {
                if (board.isMoveValid(new int[] {i,j}, new int[] {k,l})) {
                  Game newGame = new Game(game);
                  newGame.makeMove(new int[] {i,j}, new int[] {k,l});
                  if (newGame.position.promotionAvailable) {
                    //check all promotion options
                    String options = newGame.promotionOptions;
                    result += options.length();
                  }
                  else {
                    result += 1;
                  }
                }
              }
            }
          }
        }
      }
    }
    
    return result;
  }
  
  public static int perftTest(Engine game, int depth) {
    /*
    To validate that the move generation works properly
    can be validated against the stable perft in unknown positions or against known results
    */
    int result = 0;
    
    if (depth > 1) {
      ArrayList<Engine> games = game.getMoves();
      for (Engine newGame : games) {
        result += Perft.perftTest(newGame,depth-1);
      }
    }
    else {
      ArrayList<Engine> games = game.getMoves();
      return games.size();
    }
    
    return result;
  }
  
  public static int comparePerft(Engine game, int depth) {
    for (int i=1;i<=depth;i++) {
      int stableResult = Perft.perft(game,i);
      int result = Perft.perftTest(game, i);
      if (result != stableResult) {
        return i;
      }
    }
    return 0;
  }
  
  public static int[] comparePerft(Engine[] games) {
    int size = games.length;
    for (int i=0;i<size;i++) {
      Engine game = games[i];
      int result = Perft.comparePerft(game,game.depth);
      if (result != 0) {
        return new int[] {i,result};
      }
    }
    return new int[] {0,0};
  }
  
  public static boolean detectErrors(Engine[] games) {
    int[] result = Perft.comparePerft(games);
    if (result[0] != 0) {
      System.out.println("There is a problem with your move generation for position " + games[result[0]].toString());
      return false;
    }
    return true;
  }
  
  public int runTest(int maxDepth) {
    int depth = Math.min(this.results.length-1,maxDepth);
    for (int i=1;i<=depth;i++) {
      int result = Perft.perftTest(this.game, i);
      if (!(result == results[i])) {
        System.out.println("Got " + result + " instead of " + results[i]);
        return i;
      }
    }
    return 0;
  }
  
  public boolean findErrors(int maxDepth) {
    int result = this.runTest(maxDepth);
    if (result != 0) {
      System.out.println("There is a problem with your move generation for position " + this.game.toString());
      return false;
    }
    return true;
  }
  
  @Override
  public Object call() {
    return this.findErrors(this.game.depth);
  }
}
