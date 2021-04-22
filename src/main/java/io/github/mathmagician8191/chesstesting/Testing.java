package io.github.mathmagician8191.chesstesting;

import io.github.mathmagician8191.chessgame.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

public class Testing {
  public static void main(String[] args) throws InterruptedException, ExecutionException {
    //toggle existing tests
    boolean testPositions = true; //check move generation in the standard positions
    boolean timeEvaluation = false; //check how long evaluation takes
    boolean runMatch = false;
    boolean warmUp = false;
    int depth = 7; //maximum depth to test Set lower for quicker results if debugging
    int quiescenceDepth = 6;
    
    //extra positions to manually add
    Perft[] tests = new Perft[] {
      /*
      new Perft(new Game("fen-goes-here"),new int[]{perft-data})
      */
    };
    
    //engines to match against each other
    Engine opponent1 = new Engine("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",2,2,1,8,"qrbn",false,3,4,true);
    TestEngine opponent2 = new TestEngine("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",2,2,1,8,"qrbn",false,3,4,true);
    
    //add extra tests here - warm up if timing
    
    /*
    Test info:
    
    Perft.detectErrors(Engine[] games)
    will test each Engine up to it's depth with the stable and testing perft functions
    will log to the console and return false if any are found
    
    Perft perft = new Perft(Game game, int[] results)
    perft.findErrors(depth);
    will test the move generation against the position to find out if the perft matches known results
    will log to the console and return false if they don't match
    */
    
    //built in tests
    if (runMatch) {
      Match.runMatch(new int[]{0,0,0},opponent1,opponent2,new Game("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",2,2,1,8,"qrbn",false),200,new double[]{-5,5},0.05);
      //Match.runMatch(opponent1,opponent2,new Game("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",2,2,1,8,"qrbn"),5000);
    }
    
    Collection<Callable<Boolean>> tasks = new ArrayList<>();
    
    long positions = 0; //long gives more room for scaling
    
    for (Perft position : tests) {
      position.game.depth = depth;
      //calculate total positions expected
      int maxDepth = Math.min(position.results.length-1,depth);
      for (int i=1;i<=maxDepth;i++) {
        positions += position.results[i];
      }
      
      tasks.add(position);
    }
    if (testPositions) {
      for (Perft position : Perft.positions) {
        position.game.depth = depth;
        //calculate total positions expected
        int maxDepth = Math.min(position.results.length-1,depth);
        for (int i=1;i<=maxDepth;i++) {
          positions += position.results[i];
        }
        
        tasks.add(position);
      }
    }
    
    System.out.println("Positions to analyse: " + tasks.size());
    
    System.out.println("Total positions expected: " + positions);
    
    ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
    
    if (warmUp) {
      threadPool.invokeAll(tasks);
    }
    
    long startTime = System.currentTimeMillis();
    threadPool.invokeAll(tasks);
    long endTime = System.currentTimeMillis();
    System.out.println(endTime-startTime);
    
    
    Collection<Callable<Integer>> newTasks = new ArrayList<>();
    
    for (Perft position : tests) {
      newTasks.add(new Evaluation(new Engine(position.game,depth,quiescenceDepth)));
    }
    if (timeEvaluation) {
      for (Perft position : Perft.positions) {
        newTasks.add(new Evaluation(new Engine(position.game,depth,quiescenceDepth)));
      }
    }
    
    if (warmUp) {
      threadPool.invokeAll(newTasks);
    }
    
    startTime = System.currentTimeMillis();
    threadPool.invokeAll(newTasks);
    endTime = System.currentTimeMillis();
    System.out.println(endTime-startTime);
    
    threadPool.shutdown();
    
    System.out.println("All the tests have completed");
  }
}
