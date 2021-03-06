package ui.screens;

import game.core.Engine;
import game.core.GameObject.Event;
import game.core.Stage;
import game.essentials.HighScore;
import ui.accessories.GameSettings;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class ScreenManager extends Game
{
	enum Task
	{
		OPEN_MENU,
		OPEN_CONTROLLER_CREATOR,
		OPEN_STAGE_CHOOSER,
		OPEN_STATS
	}
	
	private Screen splash, menu, controllerCreator, statsScreen, selectStage;
	
	@Override
	public void create() 
	{
		splash = new Splash(this);
		menu = new MainMenu(this);
		controllerCreator = new ControllerCreator(this);
		statsScreen = new Stats(this);
		selectStage = new SelectStage(this);
		
		setScreen(splash);
	}
	
	/**
	 * Called by a {@code Screen} when its done.
	 * @param task What to do next.
	 */
	public void nextTask(Task task)
	{
		switch(task)
		{
			case OPEN_MENU:
				setScreen(menu);
				break;
			case OPEN_CONTROLLER_CREATOR:
				setScreen(controllerCreator);
				break;
			case OPEN_STATS:
				setScreen(statsScreen);
				break;
			case OPEN_STAGE_CHOOSER:
				setScreen(selectStage);
				break;
		}
	}
	
	public void startGame(Stage stage, HighScore replay)
	{
		GameSettings settings = new GameSettings();
		settings.loadSettings("game.ini");
		
		if(replay != null)
			stage.setMeta(replay.meta);
		
		Engine engine = Engine.constructEngine(stage, replay == null ? null : replay.replays);
		engine.clearEachFrame = settings.clearEachFrame;
		engine.showFps(settings.showFps);
		engine.saveReplays = settings.saveReplays;
		engine.masterVolume = settings.masterVolume;
		engine.setGlobalScale(settings.masterZoom);
		engine.setExitEvent(new Event()
		{
			@Override
			public void eventHandling() 
			{
				Gdx.graphics.setDisplayMode(800, 600, false);
				setScreen(menu);
				System.gc();
			}
		});
		
		setScreen(engine);
	}
}
