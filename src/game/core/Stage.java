package game.core;

import game.core.Engine.Direction;
import game.core.Engine.GameState;
import game.core.GameObject.Event;
import game.core.MainCharacter.CharacterState;
import game.essentials.Image2D;
import game.essentials.Utilities;
import game.essentials.Controller.PressedButtons;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import kuusisto.tinysound.Music;

/**
 * Create your own stage by extending this class and adding your unique appearance.
 * @author Pojahn
 */
public abstract class Stage 
{	
	/**
	 * This enum is used when setting the background/foreground image the quick way with the predefined functions found in {@code Stage}.
	 * @author Pojahn Moradi
	 */
	public enum RenderOption
	{
		/**
		 * Renders the entire background/foreground.
		 */
		FULL,
		/**
		 * Render the background/foreground at a fixed position(the screen).<br>
		 */
		FIXED, 
		/**
		 * Render the parts of the background/foreground that is visible to the human eye. Possible performance boosts.
		 */
		PORTION
	};
	
	/**
	 * This is the stage that currently are being played. <br>
	 * This variable is set automatically when a stage has been launched.
	 */
	public static Stage STAGE;

	/**
	 * This is the damage a {@code MainCharacter} takes when he or she interact with LETHAL tile type.
	 */
	public static int LETHAL_DAMAGE = -1;
	
	/**
	 * This is the volume of the stage music. Defaults to 1.0, which is 100%.
	 */
	public static float MUSIC_VOLUME = 1f;
	
	/**
	 * The stage data, which stores all the tile information. The values stored in this matrix are constant found in {@code game.core.Engine}.
	 */
	public byte[][] stageData;
	
	/**
	 * This is the width of the stage. It is usually set automatically.
	 */
	public int width;
	
	/**
	 * This is the height of the stage. It is usually set automatically.
	 */
	public int height;
	
	/**
	 * This is the width of the game window. Can be manually set during development but can not be changed at runtime.
	 */
	public int visibleWidth;
	
	/**
	 * This is the height of the game window. Can be manually set during development but can not be changed at runtime.
	 */
	public int visibleHeight;
	
	/**
	 * A reference to the engine.
	 */
	public Engine game;
	
	/**
	 * {@code startX} respective {@code startY} are the starting position of the main character.
	 */
	protected int startX, startY;
	
	/**
	 * The music that will play in the background. Can be either wav or ogg.
	 */
	protected Music music;
	
	private LinkedList<Object> discardList, appendList, trash;
	private byte[][] stageClone;
	private boolean pending;
	boolean sort;
	List<GameObject> stageObjects;
	List<MainCharacter> mains;
	List<Event> events;
	
	public Stage()
	{
		STAGE = this;
		discardList    = new LinkedList<>();
		appendList     = new LinkedList<>();
		stageObjects   = new LinkedList<>();
		mains          = new LinkedList<>();
		trash		   = new LinkedList<>();
		events 		   = new LinkedList<>();
		visibleWidth = visibleHeight = startX = startY = -1;
	}
	
	/**
	 * Loads and starts the song found on the given path.
	 * @param path The path to where the song can be found.
	 * @param loopStart Where to start, in seconds, after the song have ended.
	 */
	public void setStageMusic (String path, double loopStart)
	{
		setStageMusic(Utilities.loadMusic(path), loopStart);
	}
	
	/**
	 * Starts the given song.
	 * @param music The song to start.
	 * @param loopStart Where to start, in seconds, after the song have ended.
	 */
	public void setStageMusic(Music music, double loopStart)
	{
		music.setVolume(MUSIC_VOLUME);
		music.setLoopPositionBySeconds(loopStart);
		music.play(true);
		this.music = music;
	}
		
	/**
	 * Initialize the starting position, size and the visible size of the stage.
	 */
	public void basicInits()
	{
		width = stageData[0].length;
		height = stageData.length;
		
		if(visibleWidth == -1 || visibleHeight == -1)
		{
			visibleWidth  = width;
			visibleHeight = height;
		}
		
		if(startX == -1 || startY == -1)
			for (int i = 0; i < stageData.length; i++)
				for (int j = 0; j < stageData[0].length; j++)
					if (stageData[i][j] == Engine.START_POSITION)
					{
						startX = j;
						startY = i;
						return;
					}
	}
	
	/**
	 * Every triggerable unit added into this stage will passed to this function by the engine every frame.
	 * @param go The {@code MovableObject} that have moved.
	 * @param tileType A constant representing which tile type this {@code MovableObject} is currently "standing" on.
	 */
	public void tileIntersection(MovableObject mo, byte tileType)
	{
		if (tileType != Engine.HOLLOW)
			mo.runTileEvents(tileType);
	}

	/**
	 * This method is called every frame by the engine and add or removes entities and update all the existing ones.
	 */
	final void moveEnemies() 
	{
		if(pending)
		{
			Object obj;
			if(!discardList.isEmpty())
				while ((obj = discardList.poll()) != null)
				{
					trash.add(obj);
					
					if(obj instanceof GameObject)
						stageObjects.remove(obj);
					else if(obj instanceof Event)
						events.remove(obj);
				}
			
			if(!appendList.isEmpty())
				while ((obj = appendList.poll()) != null)
				{
					if (obj instanceof GameObject)
					{
						stageObjects.add((GameObject)obj);
						sort = true;
					}
					else if(obj instanceof Event)
						events.add((Event)obj);
				}
			
			pending = false;
		}
		
		if(sort)
		{
			Collections.sort(stageObjects, GameObject.Z_INDEX_SORT);
			sort = false;
		}
		
		if(trash.size() > 200)
			trash.clear();
		
		List<MainCharacter> mains = new LinkedList<>();
		
		for(GameObject go : stageObjects)
		{
			if(go instanceof Enemy)
			{
				Enemy enemy = (Enemy) go;
				enemy.moveEnemy();
				
				if(enemy.triggerable)
				{
					enemy.occupyingCells.clear();
					enemy.tileCheck();
					enemy.inspectIntersections();
				}
				
				updateFacing(enemy);
				enemy.prevX = enemy.currX;
				enemy.prevY = enemy.currY;
			}
			else if(go instanceof MainCharacter)
				mains.add((MainCharacter)go);
				
			go.removeQueuedEvents();
			go.runEvents();
		}
		
		int aliveMains = mains.size();
		for(MainCharacter main : mains)	//TODO: Replays are currently not saved. We need to extend this feature to support multiple main characters
		{
			if(main.isGhost())
				aliveMains--;
			
			updateFacing(main);
			main.prevX = main.currX;
			main.prevY = main.currY;
			
			if(game.playingReplay() || main.isGhost())
				main.handleInput(main.getNext());
			else if(game.getState() != GameState.ONGOING || main.getState() != CharacterState.ALIVE)
				main.handleInput(MainCharacter.STILL);
			else//Register a replay frame here
			{
				PressedButtons pbs = game.getPressedButtons(main.con);
				if(!pbs.suicide)
					main.handleInput(pbs);
				else
				{
					main.setState(CharacterState.DEAD);
					main.deathAction();
				}
			}
			if(main.triggerable)
			{
				main.occupyingCells.clear();
				main.tileCheck();
				main.inspectIntersections();
			}
			
			main.removeQueuedEvents();
			main.runEvents();
			
			if(!main.isGhost() && main.getState() == CharacterState.DEAD)
				aliveMains--;
			else if(!main.isGhost() && main.getState() == CharacterState.FINISH)
				game.setGlobalState(GameState.FINISH);
		}   
		
		if(0 >= aliveMains)
			game.setGlobalState(GameState.ENDED);
		
		if(!events.isEmpty())
			for(Event event : events)
				event.eventHandling();
	}

	/**
	 * Adds the given objects to the game.<br>
	 * The object can be an instance of either {@code GameObject} or {@code Event}.
	 * @param objs The object to add.
	 */
	public void add(Object... objs)
	{
		pending = true;
		for(Object obj : objs)
			if(obj != null)
				appendList.add(obj);
	}
	
	/**
	 * Removes the specified objects from the game.<br>
	 * The object can be an instance of either {@code GameObject} or {@code Event}.
	 * @param objs The objects to remove.
	 */
	public void discard(Object... objs)
	{
		pending = true;
		for(Object obj : objs)
		{
			if(obj instanceof GameObject)
			{
				GameObject go = (GameObject) obj;
				go.endUse();
			}
			if(obj != null)
				discardList.add(obj);
		}
	}
	
	/**
	 * An optional way of setting the background. Wraps the given image in a {@code GameObject} with {@code z-index} set to -100.
	 * @param img The image to use as background.
	 */
	public void background(RenderOption type, Image2D... img)
	{
		SceneImage wrapper = new SceneImage(type);
		wrapper.setImage(img);
		wrapper.zIndex(-100);
		add(wrapper);
	}
	
	/**
	 * An optional way of setting the foreground. Wraps the given image in a {@code GameObject} with {@code z-index} set to 100.
	 * @param img The image to use as foreground.
	 */
	public void foreground(RenderOption type, Image2D... img)
	{
		SceneImage wrapper = new SceneImage(type);
		wrapper.setImage(img);
		wrapper.zIndex(100);
		add(wrapper);
	}
	
	/**
	 * This function should be used as constructor when subclassing. Operations that only requires to be performed once(such as image and sound loading) should be done here.
	 */
	public abstract void init();
	
	/**
	 * Called upon start, death and restart. The stage is built from this method.
	 */
	public void build()
	{
		for(GameObject go : stageObjects)
			go.endUse();
		
		trash.clear();
		stageObjects.clear();
		mains.clear();
		events.clear();
		appendList.clear();
		discardList.clear();
		game.elapsedTime = 0;
	}
	
	/**
	 * Dispose all your resources here.
	 */
	public abstract void dispose();
	
	/**
	 * Optional and is called once every frame.
	 */
	public void extra()
	{}
	
	/**
	 * This function return data to the replay file(default=empty string) that needs to be saved.<br>
	 * Called automatically by the server.
	 * @return The meta data to save in the replay file.
	 */
	protected String getMeta()
	{
		return "";
	}
	
	/**
	 * Automatically called by the engine and sends the meta data from the replay, if exists.<br>
	 * By default, a stage does not contain any meta data.
	 * @param meta The meta data, sent by the engine.
	 */
	public void setMeta(String meta) {}
	
	/**
	 * Converts this map to a string. Note that the string can become very large(height * width + height).
	 */
	public String toString()
	{
		StringBuilder bu = new StringBuilder ((height * width) + height);
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
				bu.append(stageData[i][j]);
			
			bu.append('\n');
		}
		return bu.toString();
	}
	
	/**
	 * Returns the tile type from the stage data clone.
	 * @param x The X position.
	 * @param y The Y position.
	 * @return The tile type at the given point.
	 */
	public byte getCloneData(int x, int y)
	{
		if(stageClone == null)
			cloneStageData();
		
		return stageClone[y][x];
	}
	
	void updateFacing(MovableObject mo)
	{
		if((mo.multiFacings || mo.doubleFaced) && !mo.manualFacings)
		{
			Direction dir = EntityStuff.getDirection(EntityStuff.normalize(mo.prevX + mo.width / 2, mo.prevY + mo.height / 2, mo.currX + mo.width / 2, mo.currY + mo.height / 2));
			if(dir != null)
			{
				if(mo.doubleFaced)
				{
					if(dir != Direction.N && dir != Direction.S)
						mo.facing = dir;
				}
				else
					mo.facing = dir;
			}
		}
	}

	private void cloneStageData()
	{
		stageClone = new byte[stageData.length][stageData[0].length];
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				stageClone[y][x] = stageData[y][x];
	}
	
	private static class SceneImage extends GameObject
	{
		RenderOption type;
		
		SceneImage(RenderOption type)
		{
			this.type = type;
			setVisible(true);
		}
		
		@Override
		public Image2D getFrame()
		{
			return null;
		}
		
		@Override
		public void drawSpecial(SpriteBatch batch)
		{
			switch(type)
			{
				case FULL:
					batch.draw(super.getFrame(), 0, 0);
					break;
				case FIXED:
					STAGE.game.clearTransformation();
					batch.draw(super.getFrame(), 0, 0);
					STAGE.game.restoreTransformation();
					break;
				case PORTION:
					OrthographicCamera camera = STAGE.game.getCamera();
					Engine e = STAGE.game;
					int vw = Stage.STAGE.visibleWidth;
					int vh = Stage.STAGE.visibleHeight;
					
					camera.up.set(0, 1, 0);
					camera.direction.set(0, 0, -1);
					camera.update();
					batch.setProjectionMatrix(camera.combined);
					
					batch.draw(super.getFrame().getTexture(),
								e.tx - vw / 2,
								e.ty - vh / 2,
								(int)(e.tx - vw / 2),
								(int)(e.ty - vh / 2),
								vw,
								vh);
					
					camera.up.set(0, -1, 0);
					camera.direction.set(0, 0, 1);
					camera.update();
					batch.setProjectionMatrix(STAGE.game.getCamera().combined);
					break;
			}
		}
	}
}