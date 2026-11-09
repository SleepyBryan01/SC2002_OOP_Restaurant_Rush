package restaurantrush;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import restaurantrush.controller.GameConfig;
import restaurantrush.controller.GameController;
import restaurantrush.view.ConsoleView;

public class Main {
    public static void main(String[] args) {
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
        }
        ConsoleView view = new ConsoleView(in, System.out, scripted);
        new GameController(GameConfig.baseSetting(), view).play();
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
}
