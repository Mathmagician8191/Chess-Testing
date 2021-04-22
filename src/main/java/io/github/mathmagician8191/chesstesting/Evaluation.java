package io.github.mathmagician8191.chesstesting;

import io.github.mathmagician8191.chessgame.*;
import java.util.concurrent.Callable;

public class Evaluation implements Callable {
  Engine engine;
  
  public Evaluation(Engine engine) {
    this.engine = engine;
  }
  
  @Override
  public Object call() {
    return this.engine.getScore(this.engine.depth);
  }
}
