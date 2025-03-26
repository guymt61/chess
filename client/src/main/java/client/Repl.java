package client;

import client.websocket.NotificationHandler;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
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

    public void notify(ServerMessage serverMessage) {
        switch (serverMessage.getServerMessageType()) {
            case ERROR -> handleError(serverMessage);
            case LOAD_GAME -> handleLoadGame(serverMessage);
            case NOTIFICATION -> handleNotification(serverMessage);
        }
        printPrompt();
    }

    private void handleLoadGame(ServerMessage serverMessage) {
        System.out.println(client.updateGame(serverMessage.getGame()));
    }

    private void handleError(ServerMessage serverMessage) {
        System.out.println(SET_TEXT_COLOR_RED + serverMessage.getErrorMessage());
    }

    private void handleNotification(ServerMessage serverMessage) {
        System.out.println(SET_TEXT_COLOR_GREEN + serverMessage.getMessage());
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + client.statusDisplay() + ">>> " + SET_TEXT_COLOR_GREEN);
    }

}