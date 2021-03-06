package ui;

//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
import ui.accessories.GameSettings;
import ui.screens.ScreenManager;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class TheMainClass 
{
	public static void main(String... args) throws Exception 
	{
//		try
//		{
//			File file = new File("logs/error log.txt");
//			FileOutputStream fos = new FileOutputStream(file);
//			PrintStream ps = new PrintStream(fos);
//			System.setErr(ps);
//		}
//		catch(Exception e)
//		{
//			System.err.println("Could not redirect the error stream.");
//		}
		
		GameSettings settings = new GameSettings();
		settings.loadSettings("game.ini");
		
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Pojahns Game Engine";
		cfg.width = 800;
		cfg.height = 600;
		cfg.vSyncEnabled = settings.vsync; 
		cfg.resizable = false;
		cfg.useGL30 = false; 
		cfg.backgroundFPS = settings.fps;
		cfg.foregroundFPS = settings.fps;
		cfg.addIcon("res/data/icon128x128.png", FileType.Internal);
		cfg.addIcon("res/data/icon32x32.png", FileType.Internal);  
		cfg.addIcon("res/data/icon16x16.png", FileType.Internal);
		
		new LwjglApplication(new ScreenManager(), cfg);
	}	
}
