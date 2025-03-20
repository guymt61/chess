package client;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.print(SET_TEXT_UNDERLINE + SET_TEXT_COLOR_MAGENTA);
        System.out.println(WHITE_KING + "Welcome to the chess client. Sign in to start." + BLACK_KING);
        System.out.print(RESET_TEXT_UNDERLINE);
        System.out.print(SET_TEXT_COLOR_BLUE + client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("Thank you for using the chess client!")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(SET_TEXT_COLOR_RED + msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + client.statusDisplay() + ">>> " + SET_TEXT_COLOR_GREEN);
    }

}