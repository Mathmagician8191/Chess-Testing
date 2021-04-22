package io.github.mathmagician8191.chesstesting;

import io.github.mathmagician8191.chessgame.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Match implements Callable {
  static volatile ArrayList<Board> boards = new ArrayList<>();
  
  Engine opponent1;
  Engine opponent2;
  Game game;
  boolean side;
  
  public Match(Engine opponent1,Engine opponent2,Game game,boolean side) {
    this.opponent1 = opponent1;
    this.opponent2 = opponent2;
    this.game = game;
    this.side = side;
  }
  
  public static double logLikelihood(double x) {
    return 1.0/(1+Math.pow(10, -x/400));
  }
  
  public static double logLikelihoodRatio(int[] results,double[] eloBounds) {
    double[] bounds = new double[2];
    for (int i=0;i<bounds.length;i++) {
      bounds[i] = Match.logLikelihood(eloBounds[i]);
    }
    
    int games = 0 ;
    for (int result : results) {
      if (result == 0) {
        return 0.0;
      }
      else {
        games += result;
      }
    }
    
    double[] odds = new double[3];
    for (int i=0;i<odds.length;i++) {
      odds[i] = ((double) results[i])/games;
    }
    
    double score = odds[0] + (odds[1]/2);
    double m2 = odds[0] + (odds[1]/4);
    double variance = m2 - Math.pow(score, 2);
    double variancePerGame = variance/games;
    return (bounds[1]-bounds[0])*(2*score-bounds[0]-bounds[1])/(2*variancePerGame);
  }
  
  public static int sprt(int[] results,double[] eloBounds,double sigma) {
    double chance = logLikelihoodRatio(results,eloBounds);
    if (chance > -sigma) {
      return 1; //pass
    }
    else if (chance < sigma) {
      return -1; //fail
    }
    else {
      return 0; //inconclusive
    }
  }
  
  public int[] simulateGame() {
    //simulates a game and gives result, time spent by each side and number of moves
    int[] results = new int[] {0,0,0,0};
    
    while (!this.game.position.gameOver) {
      if (this.game.position.toMove==this.side) {
        //oppponent 1 to move
        long startTime = System.currentTimeMillis();
        this.game = opponent1.makeMove(this.game);
        long finishTime = System.currentTimeMillis();
        results[0] += finishTime - startTime;
      }
      else {
        //oppponent 2 to move
        long startTime = System.currentTimeMillis();
        this.game = opponent2.makeMove(this.game);
        long finishTime = System.currentTimeMillis();
        results[1] += finishTime - startTime;
      }
      if (this.game.position.halfmoveClock > 95 && this.game.position.halfmoveClock < 101) {
        System.out.println(game.toString() +
            (this.side ? " opponent 1 is white" : " opponent 2 is white"));
      }
      if (this.game.position.moves == 10 && this.game.position.toMove == true) {
        Match.boards.add(new Board(this.game.position));
      }
    }
    
    switch (this.game.endCause) {
      case "Draw by 50-move rule":
      case "Draw by Stalemate":
        Board board = this.game.position;
        int whitePieces = 0;
        int blackPieces = 0;
        for (int i=0;i<board.width;i++) {
          for (int j=0;j<board.height;j++) {
            Piece piece = board.boardstate[i][j];
            if (piece.isPiece) {
              if (piece.side==1) {
                whitePieces++;
              }
              else {
                blackPieces++;
              }
            }
          }
        }
        String result = board.toString()+" was "+game.endCause + " in a " +
            String.valueOf(whitePieces) + "v"+String.valueOf(blackPieces) +
            (this.side ? " opponent 1 is white" : " opponent 2 is white");
        System.out.println(result);
    }
    
    if (this.game.position.moves < 20) {
      String result = this.game.toString()+" was quickly "+game.endCause +
          (this.side ? " opponent 1 is white" : " opponent 2 is white");
      System.out.println(result);
    }
    
    results[2] = game.gameResult * (side ? 1 : -1);
    
    results[3] = game.position.moves;
    
    return results;
  }
  
  public static boolean runMatch(int[] results,Engine opponent1,Engine opponent2,Game game,
      int length,double[] eloBounds,double risk) throws InterruptedException, ExecutionException {
    double sigma = Math.log(risk/(1-risk));
    
    Random booleanSource = new Random();
    
    int[] time = new int[] {0,0};
    int moves = 0;
    
    boolean hasTime = false;
    try {
      File startTime = new File("time.txt");
      startTime.createNewFile();
      long millis = System.currentTimeMillis();
      FileWriter logTime = new FileWriter("time.txt");
      logTime.write(String.valueOf(millis/60000));
      logTime.close();
      hasTime = true;
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    
    boolean hasFile = false;
    
    try {
      File data = new File("data.txt");
      data.createNewFile();
      hasFile = true;
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    
    ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
    
    Match.boards = new ArrayList<>();
    
    while (true) {
      Collection<Callable<int[]>> tasks = new ArrayList();
      
      boolean side = booleanSource.nextBoolean();
      
      for (int i=0;i<length;i++) {
        side = !side;
        Match match = new Match(opponent1,opponent2,game,side);
        tasks.add(match);
      }

      List<Future<int[]>> outcomes = threadPool.invokeAll(tasks);
      
      for (Future<int[]> future : outcomes) {
        int[] result = future.get();
        switch (result[2]) {
          case 1:
            //first player won
            results[0]++;
            break;
          case 0:
            //draw
            results[1]++;
            break;
          case -1:
            //second player won
            results[2]++;
            break;
        }

        for (int j=0;j<time.length;j++) {
          time[j] += result[j];
        }

        moves += result[3];
      }
      
      //test if the results are conclusive yet
      int status = Match.sprt(results,eloBounds,sigma);
      if (status != 0) {
        //testing is over
        threadPool.shutdown();
        
        ArrayList<Board> uniqueBoards = new ArrayList<>();
        for (Board board : Match.boards) {
          boolean unique = true;
          for (Board uniqueBoard : uniqueBoards) {
            if (board.equals(uniqueBoard)) {
              unique = false;
            }
          }
          if (unique) {
            uniqueBoards.add(board);
          }
        }
        
        String output = String.valueOf(uniqueBoards.size()) + " unique positions after 10 moves";
        output += "\n+" + results[0] + " -" + results[2]+ " =" + results[1];
        output += "\nPlayer 1: " + time[0]/moves + "ms per move. Player 2: " + time[1]/moves + "ms per move.";
        boolean result;
        if (status == 1) {
          output += "\nPlayer 1 won the match";
          result = true;
        }
        else {
          output += "\nPlayer 2 won the match";
          result = false;
        }
        
        System.out.println(output);
        
        if (hasTime) {
          try {
            FileWriter logTime = new FileWriter("time.txt");
            logTime.write("Test over");
            logTime.close();
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
        }
        
        if (hasFile) {
          try {
            FileWriter dataFile = new FileWriter("data.txt");
            dataFile.write(output);
            dataFile.close();
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
        }
        
        return result;
      }
      String output = "Inconclusive +" + results[0] + " -" + results[2]+ " =" + results[1];
      System.out.println("---"+output+"---");
      if (hasFile) {
        try {
          FileWriter dataFile = new FileWriter("data.txt");
          dataFile.write(output);
          dataFile.close();
        }
        catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    }
  }
  
  public static void runMatch(Engine opponent1,Engine opponent2,Game game,int length)
      throws InterruptedException, ExecutionException {
    Random booleanSource = new Random();
    
    ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
    
    Match.boards = new ArrayList<>();
    
    Collection<Callable<int[]>> tasks = new ArrayList();
    
    boolean side = booleanSource.nextBoolean();
    
    for (int i=0;i<length;i++) {
      side = !side;
      Match match = new Match(opponent1,opponent2,game,side);
      tasks.add(match);
    }
    
    List<Future<int[]>> outcomes = threadPool.invokeAll(tasks);
    
    int[] results = new int[] {0,0,0};
    int[] time = new int[] {0,0};
    int moves = 0;
    
    for (Future<int[]> future : outcomes) {
      int[] result = future.get();
      switch (result[2]) {
        case 1:
          //first player won
          results[0]++;
          break;
        case 0:
          //draw
          results[1]++;
          break;
        case -1:
          //second player won
          results[2]++;
          break;
      }
      
      for (int j=0;j<time.length;j++) {
        time[j] += result[j];
      }

      moves += result[3];
    }
    
    threadPool.shutdown();
    
    ArrayList<Board> uniqueBoards = new ArrayList<>();
    for (Board board : Match.boards) {
      boolean unique = true;
      for (Board uniqueBoard : uniqueBoards) {
        if (board.equals(uniqueBoard)) {
          unique = false;
        }
      }
      if (unique) {
        uniqueBoards.add(board);
      }
    }
    
    System.out.println(String.valueOf(uniqueBoards.size()) + " unique positions after 15 moves");
    System.out.println("+" + results[0] + " -" + results[2] + " =" + results[1]);
    System.out.println("Player 1: " + time[0]/moves + "ms per move. Player 2: " + time[1]/moves + "ms per move.");
  }
  
  @Override
  public Object call() {
    return this.simulateGame();
  }
}
