package restaurantrush;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
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
            in = new Scanner(acceptAnyEnter(System.in));
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
            System.out.print(" Enter 1 or 2 > ");
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

    private static InputStream acceptAnyEnter(InputStream source) {
        return new FilterInputStream(source) {
            private boolean afterCarriageReturn;

            @Override
            public int read() throws IOException {
                while (true) {
                    int b = super.read();
                    if (b == '\n' && afterCarriageReturn) {
                        afterCarriageReturn = false;
                        continue;
                    }
                    afterCarriageReturn = b == '\r';
                    return afterCarriageReturn ? '\n' : b;
                }
            }

            @Override
            public int read(byte[] buffer, int offset, int length) throws IOException {
                while (true) {
                    int count = super.read(buffer, offset, length);
                    if (count <= 0) {
                        return count;
                    }
                    int kept = offset;
                    for (int i = offset; i < offset + count; i++) {
                        byte b = buffer[i];
                        if (b == '\n' && afterCarriageReturn) {
                            afterCarriageReturn = false;
                            continue;
                        }
                        afterCarriageReturn = b == '\r';
                        buffer[kept++] = afterCarriageReturn ? (byte) '\n' : b;
                    }
                    if (kept > offset) {
                        return kept - offset;
                    }
                }
            }
        };
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
