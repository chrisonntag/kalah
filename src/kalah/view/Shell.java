package kalah.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;
import kalah.exceptions.IllegalMoveException;
import kalah.model.Board;
import kalah.model.BoardImpl;
import kalah.model.players.Player;
import kalah.util.Utility;

/**
 * The Shell class represents a basic command line user interface, providing
 * commands for multiplying matrices.
 */
public final class Shell {

  private static Board game = null;
  private static int LEVEL = 3;
  private static int PITS_PER_PLAYER = 6;
  private static int SEEDS_PER_PIT = 4;
  private static Player OPENING_PLAYER = Player.HUMAN;

  private Shell() { }

  /**
   * This is the main method which instantiates the the BufferedReader
   * accepting user input.
   *
   * @param args          unused.
   * @throws IOException  thrown on input error.
   */
  public static void main(String[] args) throws IOException {
    BufferedReader reader
        = new BufferedReader(new InputStreamReader(System.in));
    execute(reader);
  }

  private static void execute(BufferedReader reader) throws IOException {
    boolean quit = false;

    while (!quit) {
      System.out.print("kalah> ");
      String input = reader.readLine();  // read one line
      if (input == null) {
        quit = true;
      } else {
        Scanner scanner = new Scanner(input);
        scanner.useDelimiter("\n");
        if (scanner.hasNext()) {
          String[] commands = scanner.next().split("\\s+");

          if (commands.length > 0) {
            if (evaluateCommand(commands[0],
                parseArgs(commands[0], commands))) {
              quit = true;
            }
          } else {
            System.out.println("Error! No valid command.");
          }
        } else {
          System.out.println("Error! No valid command.");
          showHelp();
        }

        scanner.close();
      }
    }
  }

  /**
   * Evaluates a user entered command.
   *
   * @param command   the user input command.
   * @param args      possible parameters.
   */
  private static boolean evaluateCommand(String command,
      HashMap<String, Integer> args) {
    boolean quit = false;
    if (args.get("error") != 0) {
      command = "error";
    }

    switch (Character.toUpperCase(command.toCharArray()[0])) {
      case 'N':
        PITS_PER_PLAYER = args.get("pits");
        SEEDS_PER_PIT = args.get("seeds");

        game = new BoardImpl(OPENING_PLAYER, PITS_PER_PLAYER,
            SEEDS_PER_PIT, LEVEL);
        break;
      case 'L':
        if (game != null) {
          LEVEL = args.get("level");
          game.setLevel(LEVEL);
        } else {
          System.out.println("Error! No game started yet.");
        }
        break;
      case 'M':
        if (game != null) {
          try {
            game = game.move(args.get("pit"));
          } catch (IllegalMoveException | IllegalArgumentException e) {
            System.out.printf(e.getMessage());
          }

          if (game.isGameOver()) {
            getWinner();
          } else if (game.getOpeningPlayer() == Player.MACHINE) {
            machineMove();
          } else {
            System.out.println(Utility.MACHINE_MISS);
          }
        } else {
          System.out.println("Error! No game started yet.");
        }
        break;
      case 'S':
        if (game != null) {
          Player lastPlayer = game.getOpeningPlayer();
          OPENING_PLAYER = Player.getOpponent(lastPlayer);

          game = new BoardImpl(OPENING_PLAYER, PITS_PER_PLAYER,
              SEEDS_PER_PIT, LEVEL);
        } else {
          System.out.println("Error! No game started yet.");
        }
        break;
      case 'P':
        if (game != null) {
          System.out.println(game);
        } else {
          System.out.println("Error! No game started yet.");
        }
        break;
      case 'H':
        showHelp();
        break;
      case 'Q':
        quit = true;
        break;
      case 'E':
        break;
      default:
        System.out.println("Error! You must enter a command.");
        break;
    }

    return quit;
  }

  /**
   * Checks if user input parameters are valid.
   *
   * @param command   a user input command.
   * @param args      possible parameters.
   * @return          a HashMap with all cleaned arguments.
   */
  private static HashMap<String, Integer> parseArgs(String command,
      String[] args) {
    HashMap<String, Integer> params = new HashMap<>();
    params.put("error", 0);

    switch (Character.toUpperCase(command.toCharArray()[0])) {
      case 'N':
        if (args.length == 3) {
          int pits = 0, seeds = 0;

          try {
            pits = Integer.parseInt(args[1]);
            seeds = Integer.parseInt(args[2]);
          } catch (NumberFormatException nfe) {
            System.out.println("Error! All arguments must "
                + "be numbers.");
            break;
          }

          params.put("pits", pits);
          params.put("seeds", seeds);
        } else {
          System.out.println("Error! Wrong number of arguments: "
              + "+2 integers expected.");
          params.put("error", 1);
        }
        break;
      case 'M':
        if (args.length == 2) {
          int pit = 0;

          try {
            pit = Integer.parseInt(args[1]);
          } catch (NumberFormatException nfe) {
            System.out.println("Error! All arguments must "
                + "be numbers.");
          }

          if (pit > 0) {
            params.put("pit", pit);
          } else {
            System.out.println("Error! Parameter must "
                + "be positive and not zero.");
            params.put("error", 1);
          }
        } else {
          System.out.println("Error! Wrong number of arguments: "
              + "+1 integers expected.");
          params.put("error", 1);
        }
        break;
      case 'L':
        if (args.length > 1) {
          try {
            int level = Integer.parseInt(args[1]);

            if (level >= 1 && level <= 7) {
              params.put("level", level);
            } else {
              System.out.println("Error! Level must be between 1 and 7");
              params.put("error", 1);
            }
          } catch (NumberFormatException nfe) {
            System.out.println("Error! The level must be a number.");
            params.put("error", 1);
          }
        } else {
          System.out.println("Error! You must specify a level.");
          params.put("error", 1);
        }
        break;
      default:
        break;
    }

    return params;
  }

  private static boolean isValid(int[] values) {
    boolean result = true;
    for (int num : values) {
      if (num < 1) {
        result = false;
      }
    }

    return result;
  }

  private static void machineMove() {
    game = game.machineMove();

    if (game.getOpeningPlayer() == Player.MACHINE) {
      while (game.getOpeningPlayer() == Player.MACHINE && !game.isGameOver()) {
        System.out.println(Utility.HUMAN_MISS);
        machineMove();
      }
    }

    if (game.isGameOver()) {
      getWinner();
    }
  }

  private static void getWinner() {
    if (game.getWinner() == null) {
      System.out.format(Utility.STALEMATE, game.getSeedsOfPlayer(Player.HUMAN));
    } else if (game.getWinner() == Player.HUMAN) {
      System.out.format(Utility.WIN, game.getSeedsOfPlayer(Player.HUMAN),
          game.getSeedsOfPlayer(Player.MACHINE));
    } else {
      System.out.format(Utility.LOOSE, game.getSeedsOfPlayer(Player.MACHINE),
          game.getSeedsOfPlayer(Player.HUMAN));
    }
  }

  /**
   * Prints a help text to the console, showing some useful
   * information on how to use this program.
   */
  private static void showHelp() {
    System.out.println("Mancala/Kalah – A game written in Java\n");
    System.out.println("\tnew <p> <s> \t\tStart a new game with <p> "
        + "pits and <s> seeds per pit.");
    System.out.println("\tlevel <i> \t\t\tSet the level 1-7.");
    System.out.println("\tmove <p> \t\t\t\t"
        + "Moves the seeds of pit <p>.");
    System.out.println("\tswitch\t\t\t\t\tStarts a new game and "
        + "lets the second player open the game.");
    System.out.println("\tprint\t\t\t\t\t\tPrints out the board.");
    System.out.println("\thelp\t\t\t\t\t\tShows this help message.");
    System.out.println("\tquit\t\t\t\t\t\tExit the program.");
  }

}

