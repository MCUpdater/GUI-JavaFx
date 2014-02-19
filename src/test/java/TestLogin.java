import javafx.application.Application;
import javafx.stage.Stage;
import org.mcupdater.gui.LoginDialog;
import org.mcupdater.settings.Profile;

/**
 * Created by sbarbour on 1/28/14.
 */
public class TestLogin extends Application
{
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Profile test = LoginDialog.doLogin(null, "testUser");
		System.out.println(test.getStyle() + " - " + test.getName() + " - " + test.getAccessToken());
	}
}
