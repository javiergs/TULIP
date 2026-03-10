package javiergs.tulip.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Lexer class to analyze the input file
 * This one is an initial version that uses a DFA to recognize binary numbers
 *
 * @author javiergs
 * @author UP students 2026 - team cmd
 * @version 0.1
 */
public class Lexer {

  private File file;
  private Automata dfa;
  private Vector<Token> tokens;
  private boolean insideBlockComment; // Para hacer seguimiento de los comentarios de linea
  private final Set<String> keywords = new HashSet<>(Arrays.asList(
      // English keywords
      "int", "float", "double", "char", "boolean", "if", "else", "while", "for", "switch", "case", "default",
      "break", "continue", "return", "true", "false", "string", "class", "public", "private", "static",
      "void", "new", "null", "import", "package", "try", "catch", "finally", "throw", "throws", "print",
      // Spanish equivalents
      "intento", "entero", "flotante", "doble", "caracter", "booleano", "si", "sino", "mientras", "para",
      "caso", "predeterminado", "romper", "continuar", "retornar", "verdadero", "falso", "cadena", "clase", "publico", "privado",
      "estatico", "vacio", "nuevo", "null", "importar", "paquete", "intentar", "capturar", "finalmente", "lanzar", "imprimir"
  ));

  public Lexer(File file) {
    this.file = file;
    tokens = new Vector<>();
    dfa = new Automata();
    insideBlockComment = false;
    //Binary numbers
    dfa.addTransition("s0", "0", "s1");
    dfa.addTransition("s1", "b", "s2");
    dfa.addTransition("s1", "B", "s2");
    dfa.addTransition("s2", "0", "s3");
    dfa.addTransition("s2", "1", "s3");
    dfa.addTransition("s3", "0", "s3");
    dfa.addTransition("s3", "1", "s3");
    //Decimales
    AddNumberTransitions("s0", "Is1", "123456789");
    AddNumberTransitions("Is1", "Is1", "0123456789");
    //Octales
    AddNumberTransitions("s1", "Os2", "01234567");
    AddNumberTransitions("Os2", "Os2", "01234567");
    //Hexadecimales
    dfa.addTransition("s1", "x", "Hs2");
    dfa.addTransition("s1", "X", "Hs2");
    AddNumberTransitions("Hs2", "Hs3", "0123456789ABCDEFabcdef");
    AddNumberTransitions("Hs3", "Hs3", "0123456789ABCDEFabcdef");
    //Flotantes (Segun yo esta bien pero no se como se prueba)
    dfa.addTransition("s0", ".", "Fds1");
    AddNumberTransitions("Fds1", "Fs2", "0123456789");
    dfa.addTransition("s1", ".", "Fs2");
    dfa.addTransition("Is1", ".", "Fs2");
    AddNumberTransitions("Is1", "Fs3", "eE");
    AddNumberTransitions("s1", "Fs3", "eE");
    AddNumberTransitions("Fs2", "Fs2", "0123456789");
    AddNumberTransitions("Fs2", "Fs3", "eE");
    AddNumberTransitions("Fs3", "Fs4", "+-");
    AddNumberTransitions("Fs4", "Fs5", "0123456789");
    AddNumberTransitions("Fs3", "Fs5", "0123456789");
    AddNumberTransitions("Fs5", "Fs5", "0123456789");
    //Delimitadores
    AddNumberTransitions("s0", "Ids1", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$_");
    AddNumberTransitions("Ids1", "Ids1", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$_0123456789");
    //Añadimso los estados de aceptacion
    dfa.addAcceptState("s1", "INTEGER");
    dfa.addAcceptState("Is1", "INTEGER");
    dfa.addAcceptState("s3", "BINARY");
    dfa.addAcceptState("Os2", "OCTAL");
    dfa.addAcceptState("Hs3", "HEXADECIMAL");
    dfa.addAcceptState("Fs2", "FLOAT");
    dfa.addAcceptState("Fs5", "FLOAT");
    dfa.addAcceptState("Ids1", "IDENTIFIER");
  }

  private void AddNumberTransitions(String to, String from, String valuesToAdd) {
    //String valuesToAdd = "0123456789ABCDEFabcdef";
    for (char c : valuesToAdd.toCharArray()) {
      dfa.addTransition(to, String.valueOf(c), from);
    }
  }

  public void run() throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    while ((line = reader.readLine()) != null) {
      algorithm(line);
    }
  }

  private void algorithm(String line) {
    String currentState = "s0";
    String nextState;
    String string = "";
    int index = 0;
    boolean insideString = false;
    boolean insideChar = false;
    while (index < line.length()) {
      char currentChar = line.charAt(index);
      char nextChar = currentChar;
      //Obtenemos el char siguiente solo si no estamos en el ultimo de la linea
      if (index < line.length() - 1) {
        nextChar = line.charAt(index + 1);
      }
      //Verificamos primero si el char es ", que no este dentro de un string ya y que no este dentro de un bloque de codigo
      if (currentChar == '"' && !insideString && !insideBlockComment) {
        //Guardamos tokens que se estaban procesando
        if (dfa.isAcceptState(currentState)) {
          String stateName = dfa.getAcceptStateName(currentState);
          emitTokens(string, stateName);
        } else if (currentState != "s0" && !string.isEmpty()) {
          emitTokens(string, "ERROR");
        }
        //Reiniciamos estados e iniciamos el string
        currentState = "s0";
        insideString = true;
        string = "\"";
        index++;
        continue;
      } else if (isSingleQuote(currentChar) && !insideChar && !insideString && !insideBlockComment) {
        // Procesamiento normal
        if (dfa.isAcceptState(currentState)) {
          String stateName = dfa.getAcceptStateName(currentState);
          emitTokens(string, stateName);
        } else if (!currentState.equals("s0") && !string.isEmpty()) {
          emitTokens(string, "ERROR");
        }
        currentState = "s0";
        insideChar = true;
        string = "" + currentChar;
        index++;
        continue;
      }
      //Obtenemos all lo que exista dentro de un string
      if (insideString) {
        string += currentChar;
        //Verificar el fin del string
        if (currentChar == '"') {
          //Aplicamos una regla estanadar modulo 2
          //Con un numero par, escapa, con un numero impar no escapa
          int backslashCount = 0;
          int checkIndex = index - 1;
          // Contar backslashes consecutivos hacia atrás
          while (checkIndex >= 0 && line.charAt(checkIndex) == '\\') {
            backslashCount++;
            checkIndex--;
          }
          boolean isEscaped = (backslashCount % 2 == 1);
          if (!isEscaped) {
            emitTokens(string, "STRING");
            insideString = false;
            string = "";
            currentState = "s0";
          }
        }
        index++;
        continue;
      } else if (insideChar) {
        if (!isSingleQuote(currentChar)) {
          string += currentChar;
          index++;
          continue;
        } else {
          // currentChar es ' (recta o curva)
          char openingQuote = string.charAt(0);
          char closingQuote = currentChar;
          // VALIDACIÓN: Verificar que la comilla de apertura y cierre sean del mismo tipo
          if (openingQuote != closingQuote) {
            // Comillas de diferente tipo (recta vs curva) = ERROR
            emitTokens(string + currentChar, "ERROR");
            insideChar = false;
            string = "";
            currentState = "s0";
            index++;
            continue;
          }
          // Si la comilla de apertura es curva (ambas son curvas) = ERROR
          if (openingQuote == '\u2019') {
            emitTokens(string + currentChar, "ERROR");
            insideChar = false;
            string = "";
            currentState = "s0";
            index++;
            continue;
          }
          String content = string.substring(1); // Removemos el ' inicial
          // Validar contenido vacío (caso de '')
          if (content.isEmpty()) {
            emitTokens(string + currentChar, "ERROR");
            insideChar = false;
            string = "";
            currentState = "s0";
            index++;
            continue;
          }
          // Solo si termina con UN SOLO backslash (número impar) entonces el ' está escapado
          int backslashCount = 0;
          int checkIndex = content.length() - 1;
          while (checkIndex >= 0 && content.charAt(checkIndex) == '\\') {
            backslashCount++;
            checkIndex--;
          }
          // Si hay número impar de backslashes al final, el ' está escapado
          if (backslashCount > 0 && backslashCount % 2 == 1) {
            string += currentChar; // Añadimos el ' al contenido
            index++;
            continue;
          }
          // Validar contenido de 1 caracter
          if (content.length() == 1) {
            emitTokens(string + currentChar, "CHARACTER");
          }
          // Validar secuencia Unicode \\uXXXX (6 caracteres: \\uXXXX)
          else if (content.length() == 6 && content.startsWith("\\u")) {
            boolean validHex = true;
            for (int i = 2; i < 6; i++) {
              char c = content.charAt(i);
              if (!((c >= '0' && c <= '9') ||
                  (c >= 'A' && c <= 'F') ||
                  (c >= 'a' && c <= 'f'))) {
                validHex = false;
                break;
              }
            }
            if (validHex) {
              emitTokens(string + currentChar, "CHARACTER");
            } else {
              emitTokens(string + currentChar, "ERROR");
            }
          }
          // Validar secuencia de escape válida (longitud 2)
          else if (content.length() == 2 && content.charAt(0) == '\\') {
            char escapeChar = content.charAt(1);
            if (escapeChar == 'n' || escapeChar == 't' || escapeChar == 'r' ||
                escapeChar == 'f' || escapeChar == 'b' || escapeChar == '\'' ||
                escapeChar == '"' || escapeChar == '\\') {
              emitTokens(string + currentChar, "CHARACTER");
            } else {
              // Escape inválido como '\x', '\a', '\w'
              emitTokens(string + currentChar, "ERROR");
            }
          }
          // Cualquier otra cosa es error
          else {
            emitTokens(string + currentChar, "ERROR");
          }
          insideChar = false;
          string = "";
          currentState = "s0";
          index++;
          continue;
        }
      }
      //Verificamos si all esta dentro de un comentario de bloque
      if (!insideBlockComment) {
        // CASO ESPECIAL: +/- después de 'e' en exponente
        if (currentState != null && currentState.equals("Fs3") &&
            (currentChar == '+' || currentChar == '-')) {
          nextState = dfa.getNextState(currentState, currentChar);
          string = string + currentChar;
          currentState = nextState;
          index++;
          continue;
        }
        if (!(isOperator(currentChar) || isDelimiter(currentChar) || isSpace(currentChar))) {
          nextState = dfa.getNextState(currentState, currentChar);
          string = string + currentChar;
          currentState = nextState;
        } else {
          if (dfa.isAcceptState(currentState)) {
            String stateName = dfa.getAcceptStateName(currentState);
            emitTokens(string, stateName);
          } else if (currentState != "s0") {
            emitTokens(string, "ERROR");
          }
          //Validamos que sea comentario de linea y que no sea el ultimo caracter
          if (index < line.length() - 1 && isLineComment(currentChar, nextChar)) {
            //Movemos el indice al final y pasamos directamente a la siguiente linea
            index = line.length();
            //Si no es comentario de linea validamos que sea de bloque y que no sea el ulyimo caracter
          } else if (index < line.length() - 1 && isStartBlockComment(currentChar, nextChar)) {
            insideBlockComment = true;
            index++;
          } else {
            //Validamos que es un operador
            if (isOperator(currentChar)) {
              //Validamos si es un operador doble y si no estas en el ultimo caracter
              if (index < line.length() - 1 && isCompoundOperator(currentChar, nextChar)) {
                emitTokens("" + currentChar + nextChar, "OPERATOR");
                index++;
              } else {
                emitTokens(currentChar + "", "OPERATOR");
              }
            } else if (isDelimiter(currentChar)) {
              emitTokens(currentChar + "", "DELIMITER");
            }
          }
          currentState = "s0";
          string = "";
        }
      } else {
        //Validamos que sea el final del comentario de bloque y no sea el ultimo caracter
        if (index < line.length() - 1 && isEndBlockComment(currentChar, nextChar)) {
          insideBlockComment = false;
          index++;
        }
      }
      index++;
    }
    //Regresamos error en strings no cerrados
    if (insideString) {
      emitTokens(string, "ERROR");
    }
    if (insideChar) {
      emitTokens(string, "ERROR");
      insideChar = false;
    }
    // last word
    if (dfa.isAcceptState(currentState)) {
      String stateName = dfa.getAcceptStateName(currentState);
      emitTokens(string, stateName);
    } else if (currentState != "s0") {
      emitTokens(string, "ERROR");
    }
  }

  private boolean isSpace(char c) {
    return c == ' ' || c == '\t' || c == '\n' || c == '\u00A0';
  }

  private boolean isDelimiter(char c) {
    return c == ',' || c == ';' || c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == ':';
  }

  private boolean isOperator(char c) {
    return c == '=' || c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '!' || c == '<' || c == '>' || c == '&' || c == '|' || c == '?';
  }

  //Validamos los operadores compuestos
  private boolean isCompoundOperator(char c1, char c2) {
    String op = "" + c1 + c2;
    return op.equals("++") || op.equals("--") || op.equals("==") || op.equals("!=")
        || op.equals("<=") || op.equals(">=") || op.equals("&&") || op.equals("||")
        || op.equals("+=") || op.equals("-=") || op.equals("*=") || op.equals("/=");
  }

  //Validamos los comentarios de linea
  private boolean isLineComment(char c, char c1) {
    return c == '/' && c1 == '/';
  }

  //validamos el final de los comentarios de bloque
  private boolean isEndBlockComment(char c, char c1) {
    return c == '*' && c1 == '/';
  }

  //Validamos el inicio de los comentarios de bloque
  private boolean isStartBlockComment(char c, char c1) {
    return c == '/' && c1 == '*';
  }

  private boolean isSingleQuote(char c) {
    return c == '\'' || c == '\u2019';
  }

  private void emitTokens(String lexeme, String stateName) {
    if (lexeme == null || lexeme.isEmpty()) return;

    if (stateName == "IDENTIFIER") {
      String key = lexeme.toLowerCase();
      if (keywords.contains(key)) {
        tokens.add(new Token(lexeme, "KEYWORD"));
      } else {
        tokens.add(new Token(lexeme, "IDENTIFIER"));
      }
    } else {
      tokens.add(new Token(lexeme, stateName));
    }
  }

  public void printTokens() {
    for (Token token : tokens) {
      System.out.printf("%10s\t|\t%s\n", token.getValue(), token.getType());
    }
  }

  public Vector<Token> getTokens() {
    return tokens;
  }

}
