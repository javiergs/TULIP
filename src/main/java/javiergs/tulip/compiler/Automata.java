package javiergs.tulip.compiler;

import java.util.HashMap;

/**
 * Automata class represent a DFA.
 * This version is implemented with a HashMap to store the transition table.
 *
 * @author javiergs
 * @author UP students 2026 - team cmd
 * @version 1.0
 */
public class Automata {

  private final HashMap<String, String> table = new HashMap<>();
  private final HashMap<String, String> acceptStates = new HashMap<>();

  public void addTransition(String currentState, String inputSymbol, String nextState) {
    table.put(currentState + "/" + inputSymbol, nextState);
  }

  public String getNextState(String currentState, char inputSymbol) {
    return table.get(currentState + "/" + inputSymbol);
  }

  public void addAcceptState(String state, String name) {
    acceptStates.put(state, name);
  }

  public boolean isAcceptState(String name) {
    return acceptStates.containsKey(name);
  }

  public String getAcceptStateName(String state) {
    return acceptStates.get(state);
  }

  public void printTable() {
    System.out.println("Transition Table:");
    for (String state : table.keySet()) {
      String[] parts = state.split("/");
      System.out.println(parts[0] + " -> " + table.get(state) + " [label=\"" + parts[1] + "\"];");
    }
  }

}
