package restaurantrush;

import java.io.FileNotFoundException;
import java.io.File;
import java.util.Scanner;

import restaurantrush.controller.GameConfig;
import restaurantrush.controller.GameController;
import restaurantrush.view.ConsoleView;
import restaurantrush.view.SwingView;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("--gui")) {
            startWindow();
            return;
        }
        Scanner in;
        boolean scripted = args.length > 0;
        if (scripted) {
            try {
                in = new Scanner(new File(args[0]));
            } catch (FileNotFoundException e) {
                System.err.println("Script file not found: " + args[0]);
                return;
            }
        } else {
            in = new Scanner(System.in);
            if (askForWindow(in)) {
                startWindow();
                return;
            }
        }
        ConsoleView view = new ConsoleView(in, System.out, scripted);
        new GameController(GameConfig.baseSetting(), view).play();
    }

    private static boolean askForWindow(Scanner in) {
        System.out.println(" RESTAURANT RUSH - how would you like to play?");
        System.out.println("   1) Terminal Game Play");
        System.out.println("   2) Window Game Play (Optional Feature)");
        while (true) {
            System.out.print(" Choose 1 or 2 (Enter = 1) > ");
            if (!in.hasNextLine()) {
                System.out.println();
                return false;
            }
            String choice = in.nextLine().trim();
            if (choice.isEmpty() || choice.equals("1")) {
                System.out.println();
                return false;
            }
            if (choice.equals("2")) {
                System.out.println(" Opening the game window...");
                return true;
            }
            System.out.println(" Please type 1 or 2.");
        }
    }

    private static void startWindow() throws Exception {
        SwingView view = SwingView.create();
        Runnable newGame = () -> {
            Thread game = new Thread(() -> new GameController(GameConfig.baseSetting(), view).play(), "game");
            game.setDaemon(true);
            game.start();
        };
        view.setRestartAction(() -> {
            view.reset();
            newGame.run();
        });
        newGame.run();
    }
}
