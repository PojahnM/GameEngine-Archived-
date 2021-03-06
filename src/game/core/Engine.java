package game.core;

import game.core.GameObject.Event;
import game.core.MainCharacter.CharacterState;
import game.essentials.CameraEffect;
import game.essentials.Controller;
import game.essentials.Controller.PressedButtons;
import game.essentials.GFX;
import game.essentials.HighScore;
import game.essentials.Image2D;
import game.essentials.SoundBank;
import game.essentials.Utilities;

import java.awt.Dimension;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import kuusisto.tinysound.TinySound;

import org.lwjgl.opengl.GL11;

import pjjava.misc.OtherMath;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

/**
 * The Engine class is the core of the game. All calculations and controls and the required function calls are being called from here.
 * @author Pojahn Moradi
 */
public final class Engine implements Screen
{
	public enum Direction{N,NE,E,SE,S,SW,W,NW};
	
	/**
	 * The default delta value.
	 */
	public static final float DELTA = 1.0f / 60.0f;
	
	/**
	 * The constant that represent solid tile.
	 */
	public static final byte SOLID 		    = 0;
	
	/**
	 * The constant that represent hollow tile.
	 */
	public static final byte HOLLOW 		= 1;
	
	/**
	 * The constant that represent the starting position.
	 */
	public static final byte START_POSITION = 2;
	
	/**
	 * The constant that represent lethal tile.
	 */
	public static final byte LETHAL		    = 3;
	
	/**
	 * The constant that represent the goal.
	 */
	public static final byte GOAL			= 4;
	
	/**
	 * Custom tile type 1. The behavior of the tile is stage specific.
	 */
	public static final byte AREA_TRIGGER_0 = 5;
	
	/**
	 * Custom tile type 2. The behavior of the tile is stage specific.
	 */
	public static final byte AREA_TRIGGER_1 = 6;
	
	/**
	 * Custom tile type 3. The behavior of the tile is stage specific.
	 */
	public static final byte AREA_TRIGGER_2 = 7;
	
	/**
	 * Custom tile type 4. The behavior of the tile is stage specific.
	 */
	public static final byte AREA_TRIGGER_3 = 8;
	
	/**
	 * Custom tile type 5. The behavior of the tile is stage specific.
	 */
	public static final byte AREA_TRIGGER_4 = 9;
	
	/**
	 * Custom tile type 6. The behavior of the tile is stage specific.
	 */
	public static final byte AREA_TRIGGER_5 = 10;
	
	/**
	 * Custom tile type 7. The behavior of the tile is stage specific.
	 */
	public static final byte AREA_TRIGGER_6 = 11;
	
	/**
	 * Custom tile type 8. The behavior of the tile is stage specific.
	 */
	public static final byte AREA_TRIGGER_7 = 12;
	
	/**
	 * Custom tile type 9. The behavior of the tile is stage specific.
	 */
	public static final byte AREA_TRIGGER_8 = 13;
	
	/**
	 * Custom tile type 10. The behavior of the tile is stage specific.
	 */
	public static final byte AREA_TRIGGER_9 = 14;
	/**
	 * The color that represent hollow tile(RGB:125,125,125).
	 */
	public static final Color GRAY 	  	  = Color.valueOf("7d7d7dff");
	/**
	 * The color that represent solid tile(RGB:90,90,90).
	 */
	public static final Color DARK_GRAY   = Color.valueOf("5a5a5aff");
	/**
	 * The color that represent the goal(RGB:255,0,0).
	 */
	public static final Color RED		  = Color.valueOf("ff0000ff");
	/**
	 * The color that represent the starting point(RGB:0,0,255).
	 */
	public static final Color BLUE        = Color.valueOf("0000ffff");
	/**
	 * The color that represent lethal tile(RGB:255,255,0).
	 */
	public static final Color YELLOW      = Color.valueOf("ffff00ff");
	/**
	 * The color that represent AREA_TRIGGER_0(RGB:0,255,0).
	 */
	public static final Color GREEN_0	  = Color.valueOf("00ff00ff");
	/**
	 * The color that represent AREA_TRIGGER_1(RGB:0,220,0).
	 */
	public static final Color GREEN_1	  = Color.valueOf("00dc00ff");
	/**
	 * The color that represent AREA_TRIGGER_2(RGB:0,190,0).
	 */
	public static final Color GREEN_2	  = Color.valueOf("00be00ff");
	/**
	 * The color that represent AREA_TRIGGER_3(RGB:0,160,0).
	 */
	public static final Color GREEN_3	  = Color.valueOf("00a000ff");
	/**
	 * The color that represent AREA_TRIGGER_4(RGB:0,130,0).
	 */
	public static final Color GREEN_4	  = Color.valueOf("008200ff");
	/**
	 * The color that represent AREA_TRIGGER_5(RGB:0,100,0).
	 */
	public static final Color GREEN_5	  = Color.valueOf("006400ff");
	/**
	 * The color that represent AREA_TRIGGER_6(RGB:0,70,0).
	 */
	public static final Color GREEN_6	  = Color.valueOf("004600ff");
	/**
	 * The color that represent AREA_TRIGGER_7(RGB:0,40,0).
	 */
	public static final Color GREEN_7	  = Color.valueOf("002800ff");
	/**
	 * The color that represent AREA_TRIGGER_8(RGB:0,10,0).
	 */
	public static final Color GREEN_8	  = Color.valueOf("000a00ff");
	/**
	 * The color that represent AREA_TRIGGER_9(BLACK)(RGB:0,0,0).
	 */
	public static final Color GREEN_9	  = Color.valueOf("000000ff");
	
	/**
	 * Default graphics used by laser firing entities. Can of course be changed.
	 */
	public static Image2D[] LASER_BEAM, LASER_BEGIN, LASER_IMPACT, LASER_CHARGE;
	
	/**
	 * The font of the timer.
	 */
	public BitmapFont timeFont, fpsFont;
	
	/**
	 * The font color of the timer.
	 */
	public Color timeColor = new Color(0,0,0,255);
	
	/**
	 * The color of the text that shows up upon death.
	 */
	public Color deathTextColor = new Color(0,0,0,255);
	
	/**
	 * The tinting color to fade to upon victory. Set it to {@code defaultTint} to disable.
	 */
	public Color wintTint = Color.valueOf("ff00ffff");
	
	/**
	 * The default tint color.
	 */
	public final Color defaultTint = Color.valueOf("fffffffe");
	
	/**
	 * Whether or not to clear the container every frame.
	 */
	public boolean clearEachFrame = true;
	
	/**
	 * Whether or not to save replays upon victory and death.
	 */
	public boolean saveReplays = true;
	
	private static int DELTA_VALUE = 0;
	
	/**
	 * The state of the game can be manipulated with the help of these enums.
	 * @author Pojahn Moradi
	 */
	public enum GameState {ONGOING, ENDED, COMPLETED, PAUSED};
	
	/**
	 * The translation X of the world.
	 */
	public float tx;
	
	/**
	 * The translation Y of the world.
	 */
	public float ty;
	
	/**
	 * The zoom factor of the world.
	 */
	public float zoom;
	
	/**
	 * The angle (in degrees) to rotate the world by. 
	 */
	public float angle;
	
	/**
	 * The padding in pixels to use when zooming out.
	 */
	public int zoomPadding = 20;
	
	/**
	 * The amount of milliseconds that have passed since last death or first start.
	 */
	public int elapsedTime;
	
	/**
	 * The master volume.
	 */
	public double masterVolume = 1.0;
	
	List<GameObject> focusObjs;
	Stage stage;
	Dimension viewport;
	private List<List<PressedButtons>> replays;
	private GameState globalState;
	private boolean showFps, justRestarted, playReplay, showingDialog, replayHelp, crashed, checkpoint, flipY;
	private float prevTX, prevTY;
	private double windowScale;
	private int fpsWriterCounter, fps;
	private SpriteBatch batch;
	private OrthographicCamera camera, hudCamera;
	private Color currTint;
	private Event exitEvent;
	private com.badlogic.gdx.scenes.scene2d.Stage gui;
	private Skin skin;
	private Texture errorIcon;
	private static boolean instanceCreates;
	
	/**
	 * Only one instance can of the {@code Engine} can exist at a time. An instance is considered "freed" when the {@code dispose} function is called.
	 * @param stage The stage to play.
	 * @param replay The replay to watch. If null is set, you will play the stage rather than watching a replay.
	 */
	public static Engine constructEngine(Stage stage, List<List<PressedButtons>> replays)
	{
		if(instanceCreates)
			return null;
		else
		{
			instanceCreates = true;
			return new Engine(stage, replays);
		}
	}
	
	/**
	 * Private constructor. Use the static method instead.
	 */
	private Engine(Stage stage, List<List<PressedButtons>> replays)	
	{
		Stage.STAGE = stage;
		stage.game = this;
		this.stage = stage;
		elapsedTime = 0;
		globalState = GameState.ONGOING;
		zoom = 1f;
		windowScale = 1;
		currTint = new Color(defaultTint);
		errorIcon = new Texture(Gdx.files.internal("res/data/error.png"));
		justRestarted = true;
		focusObjs = new ArrayList<>();
		viewport = new Dimension();
		
		if(replays == null)
			this.replays = new LinkedList<>();
		else
		{
			this.replays = replays;
			playReplay = true;
		}
	}
	
	
	@Override
	public void render(float delta)
	{
		if(crashed)
		{
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
			
			gui.act(Gdx.graphics.getDeltaTime());
			gui.draw();
		}
		else
		{
			try
			{
				boolean escDown = Gdx.input.isKeyJustPressed(Keys.ESCAPE);
				if((globalState == GameState.ONGOING || globalState == GameState.PAUSED) && !playReplay && escDown)
					globalState = globalState == GameState.PAUSED ? GameState.ONGOING : GameState.PAUSED;
				
				if(escDown && playReplay)
					replayHelp = !replayHelp;
				
				if(globalState == GameState.PAUSED && !playReplay)
				{
					TinySound.setGlobalVolume(.1f);
					batch.begin();
					renderPause();
					batch.end();
				}
				else
				{
					TinySound.setGlobalVolume(masterVolume);
					
					update();
					paint();
				}
			}
			catch(Exception e)
			{
				crashed = true;
				if(stage.music != null)
					stage.music.stop();
				e.printStackTrace();
				showCrashDialog(e);
			}
		}
	}

	private void paint()
	{
		if(clearEachFrame)
		{
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}
		
		for(CameraEffect ce : stage.cameraEffects)
		{
			if(ce.isDone())
				stage.discard(ce);
			else
				ce.update();
		}

		camera.position.set(tx, ty, 0);
		camera.zoom = zoom;
		camera.rotate(angle);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		if(globalState == GameState.COMPLETED)
		{
			Utilities.fadeColor(currTint, wintTint, .005f);
			batch.setColor(currTint);
		}
		
		renderEntities();
		
		camera.rotate(-angle);
		hudCamera();
		
		renderStatusBar();
		renderHelpText();
		renderFPS();
			
		batch.end();
		
		if(showingDialog)
		{
			gui.act(Gdx.graphics.getDeltaTime());
			gui.draw();
		}
	}
	
	private void update()
	{
		updateClock();
		stage.moveEnemies();
		prevTX = tx;
		prevTY = ty;
		updateCamera();
		stage.extra();
		SoundBank.FRAME_COUNTER++;
		
		if(replayHelp && Gdx.input.isKeyPressed(Keys.B))
			runExitEvent();
		else if(globalState == GameState.ENDED)
		{
			if((Gdx.input.isKeyPressed(Keys.R) && !playReplay) || (checkpoint && playReplay && !replayFramesEnded()))
			{
				restart();
				stage.build();
			}
			else if(Gdx.input.isKeyPressed(Keys.B))
			{
				if(checkpoint && !playReplay)
					saveReplay("Looser");

				runExitEvent();
			}
		}
		else if(globalState == GameState.COMPLETED)
			winAction();
	}
	
	@Override
	public void show()
	{
		timeFont = new BitmapFont(Gdx.files.internal("res/data/sansserif32.fnt"), true);
		fpsFont  = new BitmapFont(Gdx.files.internal("res/data/cambria20.fnt"), true);
		
		LASER_BEAM = Image2D.loadImages(new File("res/data/laser"),false);
		LASER_BEGIN = Image2D.loadImages(new File("res/data/laser/rear"),false);
		LASER_IMPACT = Image2D.loadImages(new File("res/data/laser/end"),false);
		LASER_CHARGE = Image2D.loadImages(new File("res/data/charge"),false);
		
		if(MainCharacter.DEFAULT_HEALTH_IMAGE == null)
			MainCharacter.DEFAULT_HEALTH_IMAGE = new Image2D("res/general/hearth.png", false);
		
		TinySound.init();
		TinySound.setGlobalVolume(masterVolume);

		GFX.checkpoint = new Image2D("res/data/checkpoint.png");
		GFX.checkpointReach = TinySound.loadSound(new File("res/data/checkpoint.wav"));

		batch = new SpriteBatch();
		setViewport(800, 600);
		
		if(!playReplay)
		{
			gui = new com.badlogic.gdx.scenes.scene2d.Stage(new ScalingViewport(Scaling.none, (int)(800 * windowScale), (int)(600 * windowScale)), batch);
			skin = new Skin(Gdx.files.internal("res/data/uiskin.json"));
		}

		stage.init();
		stage.build();

		ShaderProgram.pedantic = false;
	}
	
	@Override
	public void dispose()
	{
		TinySound.shutdown();
		stage.dispose();
		timeFont.dispose();
		fpsFont.dispose();
		errorIcon.dispose();
		Stage.disposeBatch(LASER_BEAM, LASER_BEGIN, LASER_IMPACT, LASER_CHARGE, MainCharacter.DEFAULT_HEALTH_IMAGE, GFX.checkpoint, GFX.checkpointReach);
		if(!playReplay)
		{
			skin.dispose();
			gui.dispose();
		}
		instanceCreates = false;
		MainCharacter.DEFAULT_HEALTH_IMAGE = null;
		GFX.checkpoint = null;
		GFX.checkpointReach = null;
		Stage.STAGE = null;
		stage = null;
	}
	
	OrthographicCamera getCamera()
	{
		return camera;
	}
	
	/**
	 * Returns the current state of the game.
	 * @return The current state of the game.
	 */
	public GameState getGlobalState()
	{
		return globalState;
	}
	
	/**
	 * In this function can you manipulate the state of the game. You can for example pause the game, end it etc.
	 * Function is ignored if you are dead or have have finished the stage.
	 * @param globalState The state the game should be changed to.
	 */
	void setGlobalState(GameState globalState)
	{
		if(this.globalState != GameState.COMPLETED && this.globalState != GameState.ENDED)
		{
			this.globalState = globalState;

			if(this.globalState == GameState.ENDED)
			{
				checkpoint = stage.isSafe();
				
				if(!checkpoint)
					saveReplay("Loser");
			}
			else
				stage.onComplete();
		}
	}
	
	public double getGlobalScale()
	{
		return windowScale;
	}
	
	/**
	 * Allow you to increase the size of the viewport, buy zooming.
	 * @param windowScale The value to multiple with and height with.
	 */
	public void setGlobalScale(double windowScale)
	{
		this.windowScale = windowScale;
		setViewport(viewport.width, viewport.height);
	}
	
	/**
	 * Sets the size of the viewport.
	 * @param width The width in pixels.
	 * @param height The height in pixels.
	 */
	public void setViewport(int width, int height)
	{
		viewport.width = width;
		viewport.height = height;
		
		camera = new OrthographicCamera(viewport.width, viewport.height);
		camera.setToOrtho(true,viewport.width, viewport.height);
		
		hudCamera = new OrthographicCamera(viewport.width, viewport.height);
		hudCamera.setToOrtho(true,viewport.width, viewport.height);
		
		Gdx.graphics.setDisplayMode((int)(viewport.width * windowScale), (int)(viewport.height * windowScale), false);
	}
	
	/**
	 * Returns the width of the viewport.
	 * @return The width in pixels.
	 */
	public int getScreenWidth()
	{
		return viewport.width;
	}
	
	/**
	 * Returns the height of the viewport.
	 * @return The height in pixels.
	 */
	public int getScreenHeight()
	{
		return viewport.height;
	}
	
	/**
	 * Checks whether or not the specified unit is in the field of view.
	 * @param obj The unit to test.
	 * @return False if the given unit can be culled.
	 */
	public boolean visible(GameObject obj)
	{
		Rectangle bbox = Fundementals.getBoundingBox(obj);
		return camera.frustum.boundsInFrustum(bbox.x, bbox.y, 0, bbox.width / 2, bbox.height / 2 , 0);
	}
	
	/**
	 * This function allow you to determine which {@code GameObjects} the game should focus on. <br>
	 * The screen will follow the specified {@code GameObjects} whenever they moves. <br>
	 * This is usual set to the main character(s).<br><br>
	 * 
	 * If set, {@code tx, ty} and possibly {@code scale} will be modified.
	 * @param focus The {@code GameObject} to follow.
	 */
	public void addFocusObject(GameObject focus)
	{
		focusObjs.add(focus);
	}
	
	/**
	 * Returns a copy of the focus list.
	 * @return The focus objects.
	 */
	public List<GameObject> getFocusList()
	{
		return new ArrayList<>(focusObjs);
	}
	
	/**
	 * Stops the camera from focusing on the specified object.
	 * @param obj The object to stop film.
	 */
	public void removeFocusObject(GameObject obj)
	{
		focusObjs.remove(obj);
	}
	
	/**
	 * Enables HUD camera.
	 */
	public void hudCamera()
	{
		batch.setProjectionMatrix(hudCamera.combined);
	}
	
	/**
	 * Restores the camera.
	 */
	public void gameCamera()
	{
		batch.setProjectionMatrix(camera.combined);
	}
	
	/**
	 * Returns the translate X value from the previous frame.
	 * @return The value.
	 */
	public float getPrevTx()
	{
		return prevTX;
	}
	
	/**
	 * Returns the translate Y value from the previous frame.
	 * @return The value.
	 */
	public float getPrevTy()
	{
		return prevTY;
	}
	
	/**
	 * Whether or not to show the fps.
	 * @param showFps True to enable fps display.
	 */
	public void showFps(boolean showFps)
	{
		this.showFps = showFps;
	}
	
	/**
	 * The event to launch when the game is exiting. More precise, this event is launched when a stage is terminated.<br>
	 * You usually want to unhide the main menu(or reconstruct it) as well as nullifying the {@code Stage} and {@code Engine} instance.<br>
	 * The resources used by the engine is cleared automatically. The resources used by the stage as disposed by the stage creator, so those do not need to be included here.
	 * @param exitEvent The event to execute upon disposal.
	 */
	public void setExitEvent(Event exitEvent)
	{
		this.exitEvent = exitEvent;
	}
	
	/**
	 * Whether or not the engine is currently displaying a replay and not game play.
	 * @return True if a replay displayed.
	 */
	public boolean playingReplay()
	{
		return playReplay;
	}
	
	/**
	 * Flips the world vertically.
	 */
	public void flipWorldY()
	{
		camera.setToOrtho(flipY);
		flipY = !flipY;
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
	}

	/**
	 * Checks which button of the given controller are down.
	 * @param con The controller.
	 * @return The buttons being held down.
	 */
	public static PressedButtons getPressedButtons(Controller con)
	{
		PressedButtons pb = new PressedButtons();
		pb.down 	  = Gdx.input.isKeyPressed(con.down);
		pb.left 	  = Gdx.input.isKeyPressed(con.left);
		pb.right 	  = Gdx.input.isKeyPressed(con.right);
		pb.up 		  = Gdx.input.isKeyPressed(con.up);
		pb.special1   = Gdx.input.isKeyJustPressed(con.special1);
		pb.special2   = Gdx.input.isKeyJustPressed(con.special2);
		pb.special3   = Gdx.input.isKeyJustPressed(con.special3);
		pb.switchChar = Gdx.input.isKeyJustPressed(con.switchChar);
		pb.suicide    = Gdx.input.isKeyJustPressed(con.suicide);
		
		return pb;
	}

	/**
	 * The amount of milliseconds since the last frame.
	 * @return The millis.
	 */
	public static int getDelta()
	{
		return DELTA_VALUE;
	}
	
	PressedButtons getReplayFrame(int index)
	{
		List<PressedButtons> pbs = replays.get(index);
		
		if(pbs.size() <= 0)
			return MainCharacter.STILL;
		
		PressedButtons pb = pbs.get(0);
		pbs.remove(0);
		
		return pb;
	}
	
	void registerReplayFrame(int index, PressedButtons pbs)
	{
		if(index > replays.size() - 1)
		{
			for(int i = 0; i <= index; i++)
			{
				if(i >= replays.size())
					replays.add(new LinkedList<PressedButtons>());
			}
		}
		
		replays.get(index).add(pbs);
	}
	
	private boolean replayFramesEnded()
	{
		for(List<PressedButtons> pbs : replays)
			if(pbs.size() == 0)
				return true;

		return false;
	}
	
	private void updateCamera()
	{
		final int size = focusObjs.size();
		final GameObject first = focusObjs.get(0);
		
		if(size == 1)
		{
			tx = Math.min(stage.size.width  - viewport.width,   Math.max(0, first.centerX() - viewport.width  / 2)) + viewport.width  / 2; 
			ty = Math.min(stage.size.height - viewport.height,  Math.max(0, first.centerY() - viewport.height / 2)) + viewport.height / 2;
		}
		else
		{
			final float marginX = viewport.width  / 2;
			final float marginY = viewport.height / 2;
			
			float boxX	= first.loc.x;
			float boxY	= first.loc.y; 
			float boxWidth	= boxX + first.width();
			float boxHeight	= boxY + first.height();

			for(int i = 1; i < focusObjs.size(); i++)
			{
				GameObject focus = focusObjs.get(i);
				
				boxX = Math.min( boxX, focus.loc.x );
				boxY = Math.min( boxY, focus.loc.y );
				
				boxWidth  = Math.max( boxWidth,  focus.loc.x + focus.width () );
				boxHeight = Math.max( boxHeight, focus.loc.y + focus.height() );
			}
			boxWidth = boxWidth - boxX;
			boxHeight = boxHeight - boxY;
			
			boxX -= zoomPadding;
			boxY -= zoomPadding;
			boxWidth  += zoomPadding * 2;
			boxHeight += zoomPadding * 2;
			
			boxX = Math.max( boxX, 0 );
			boxX = Math.min( boxX, stage.size.width - boxWidth ); 			

			boxY = Math.max( boxY, 0 );
			boxY = Math.min( boxY, stage.size.height - boxHeight );
			
			if((float)boxWidth / (float)boxHeight > (float)viewport.width / (float)viewport.height)
				zoom = boxWidth / viewport.width;
			else
				zoom = boxHeight / viewport.height;
			
			zoom = Math.max( zoom, 1.0f );

			tx = boxX + ( boxWidth  / 2 );
			ty = boxY + ( boxHeight / 2 );
			
			if(marginX > tx)
				tx = marginX;
			else if(tx > stage.size.width - marginX)
				tx = stage.size.width - marginX;
			
			if(marginY > ty)
				ty = marginY;
			else if(ty > stage.size.height - marginY)
				ty = stage.size.height - marginY;
		}
	}
	
	private void restart()
	{
		justRestarted = true;
		showingDialog = false;
		globalState = GameState.ONGOING;
		DELTA_VALUE = 0;
		batch.setColor(defaultTint);
		currTint = new Color(defaultTint);
		focusObjs.clear();
		stage.cameraEffects.clear();

		if(!playReplay && !checkpoint)
			replays.clear();
		
		if(!checkpoint)
			elapsedTime = 0;
	}
	
	private void updateClock()
	{
		DELTA_VALUE = (int) (Gdx.graphics.getDeltaTime() * 1000f);
		
		if(globalState == GameState.ONGOING)
		{
			if(!justRestarted)
				elapsedTime += DELTA_VALUE;
			else
				justRestarted = false;
		}
	}
	
	private void renderEntities()
	{
		for(GameObject go : stage.stageObjects)
		{
			if (go.visible)
			{
				if(go.drawSpecialBehind)
				{
					go.drawSpecial(batch);
					drawObject(go);
				}
				else
				{
					drawObject(go);
					go.drawSpecial(batch);
				}
			}
		}
	}
	
	private void renderStatusBar()
	{
		timeFont.setColor(globalState == GameState.PAUSED ? Color.WHITE : timeColor);
		timeFont.draw(batch, OtherMath.round((double)elapsedTime/1000, 1) + "", 10, 10);

		for(int index = 0, y = 40; index < stage.mains.size(); index++)
		{
			MainCharacter main = stage.mains.get(index);
			int hp = main.getHP();
			
			if(main.healthImg != null && main.getState() != CharacterState.DEAD && hp > 0)
			{
				final float width = main.healthImg.getWidth() + 3;
				
				for(int i = 0, posX = 10; i < hp; i++, posX += width)
					batch.draw(main.healthImg, posX, y);
				
				y += main.healthImg.getHeight() + 3;
			}
		}
	}
	
	void drawObject(GameObject go)
	{
		for(int i = 0; i < 2; i++)
		{
			Image2D img = null;
			
			if(i == 0)
				img = go.getFrame();
			else if(go.secondImage != null)
				img = go.secondImage.getObject();
				
			if (img != null && go.alpha > 0.0f)
			{
				float width  = img.getWidth();
				float height = img.getHeight();
				
				img.setFlip(go.flipX, !go.flipY);
				img.setPosition(go.loc.x + (i == 0 ? go.offsetX : go.offsetX2), go.loc.y + (i == 0 ? go.offsetY : go.offsetY2));
				img.setAlpha(go.alpha);
				img.setSize(i == 0 ? go.width : img.getWidth(), i == 0 ? go.height : img.getHeight());
				img.setScale(go.scale);
				img.setOriginCenter();
				img.setRotation(go.rotation);
				img.draw(batch);
				
				if(i == 0)
					img.setSize(width, height);
			}
		}
	}
	
	private void saveReplay(String playername)
	{
		if(!playReplay && saveReplays)
		{
			HighScore hs = new HighScore();
			hs.replays = replays;
			hs.meta = stage.getMeta();
			hs.name = playername;
			hs.difficulty = stage.getDifficulty();
			hs.stageName = Utilities.prettify(stage.getClass().getSimpleName());
			hs.time = OtherMath.round((double)elapsedTime/1000, 1);
			hs.date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
			hs.className = stage.getClass();
			hs.result = globalState == GameState.ENDED ? "Death" : "Victorious";
			
			Utilities.exportObject(hs, "replays/" + cleanString(stage.getClass().getSimpleName()) + " " + cleanString(playername) + " " + hs.result + " " + hs.time + " sec " + hs.date + ".hs");
		}
	}	
	
	private void winAction()
	{
		if(playReplay || showingDialog)
			return;

		showingDialog = true;
		Gdx.input.setInputProcessor(gui);
		
		new Dialog("Stage Complete!", skin)
		{
			TextField field;
			
			{
				field = new TextField("", skin);
				text("Congratulations!\nIt took you " + OtherMath.round((double)elapsedTime/1000, 1) + " seconds to finish the stage.\nEnter your name to save your replay.");
				getContentTable().row();
				getContentTable().add(field);
				button("Retry", "retry");
				button("Return To Menu", "menu");
				setModal(false);
			}
			
			protected void result(Object object) 
			{
				String name = field.getText();
				if(name == null || name.isEmpty())
					name = "Player";
				
				if(object.equals("retry"))
				{
					saveReplay(name);
					restart();
					stage.build();
				}
				else if(object.equals("menu"))
				{
					saveReplay(name);
					runExitEvent();
				}
			}
		}.show(gui);
	}
	
	private final void showCrashDialog(final Exception e)
	{
		Gdx.input.setInputProcessor(gui);
		new Dialog("Fatal Error", skin)
		{
			{
				Image img = new Image(errorIcon);
				
				getContentTable().add(img).left();
				getContentTable().row();
				getContentTable().add("Pojahns Game Engine have crashed and is unable to continue:\n").padRight(80).padTop(-52);
				getContentTable().row();
				
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				Label l = new Label(sw.toString(), skin, "default-font", Color.RED);
				getContentTable().add(l);
				
				button("Return");
				setModal(true);
			}
			
			protected void result(Object object) 
			{
				runExitEvent();
			}
		}.show(gui);
	}

	private final void renderPause()
	{
		Gdx.gl.glClearColor(0, 0, 0, .4f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		timeFont.setColor(Color.WHITE);
		timeFont.draw(batch, "Game is paused.", viewport.width / 2 - 120, viewport.height / 2);
		renderStatusBar();
	}
	
	private final void renderHelpText()
	{
		if(globalState == GameState.ENDED)
		{
			if(!replayHelp && !playReplay)
			{
				if(!checkpoint)
					timeFont.draw(batch, "You are dead. Press 'R' to retry or 'B' to go back.", viewport.width / 2 - 320, viewport.height / 2);
				else
				{
					timeFont.draw(batch, "Press 'R' to resume from your latest checkpoint", viewport.width / 2 - 320, viewport.height / 2);
					timeFont.draw(batch, "or 'B' to go back.", viewport.width / 2 - 140, (viewport.height + 100) / 2);
				}
			}
			else
				timeFont.draw(batch, "Press 'B' to return to the main menu.", viewport.width / 2 - 230, viewport.height / 2);
		}
		else if(replayHelp || (playReplay && globalState == GameState.COMPLETED))
		{
			replayHelp = true;
			timeFont.draw(batch, "Press 'B' to return to the main menu.", viewport.width / 2 - 230, viewport.height / 2);
		}
	}
	
	private final void renderFPS()
	{
		if(showFps)
		{
			if(++fpsWriterCounter % 10 == 0)
				fps = (int)(1.0f/Gdx.graphics.getDeltaTime());
			
			fpsFont.setColor(Color.WHITE);
			fpsFont.draw(batch, fps + " fps", viewport.width - 60, 5);
		}
	}
	
	private String cleanString(String source)
	{
		StringBuilder filename = new StringBuilder();

		for (char c : source.toCharArray()) 
		{
			if (c=='.' || Character.isJavaIdentifierPart(c)) 
				filename.append(c);
			else
				filename.append("x");
		} 
		return filename.toString();
	}
	
	private void runExitEvent()
	{
		new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				Gdx.app.postRunnable(new Runnable() 
				{
					@Override
					public void run() 
					{
						exitEvent.eventHandling();
					}
				});
			}
		}).start();
	}
	
	@Override public void hide() {dispose();}
	@Override public void pause() {}
	@Override public void resize(int x, int y) {}
	@Override public void resume() {}
}