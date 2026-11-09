package restaurantrush.view;

import java.util.List;

import restaurantrush.controller.GameConfig;
import restaurantrush.controller.GameResult;
import restaurantrush.controller.TurnStep;
import restaurantrush.model.Menu;
import restaurantrush.model.Restaurant;
import restaurantrush.staff.Staff;
import restaurantrush.staff.StaffCommand;
import restaurantrush.staff.TaskOption;

public interface GameView {
    void showWelcome(GameConfig config, Menu menu);

    void showTurnHeader(int turn, int maxTurns);

    void showStep(TurnStep step);

    void showStatus(Restaurant restaurant, int turn, List<Staff> staff);

    StaffCommand askCommand(Staff staff, List<TaskOption> options);

    void showEvent(String message);

    void showRejected(Staff staff, String reason);

    void showTurnEnd(Restaurant restaurant, int turn, int maxTurns);

    void showResult(GameResult result);
}
