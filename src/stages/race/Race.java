package stages.race;

import game.core.Engine;
import game.core.Engine.Direction;
import game.core.Engine.GameState;
import game.core.GameObject.Hitbox;
import game.core.MovableObject;
import game.core.MovableObject.TileEvent;
import game.core.Stage;
import game.essentials.Controller;
import game.essentials.BigImage.RenderOption;
import game.essentials.Controller.PressedButtons;
import game.essentials.BigImage;
import game.essentials.Factory;
import game.essentials.Animation;
import game.essentials.Image2D;
import game.essentials.Utilities;
import game.mains.GravityMan;
import game.movable.Bouncer;
import game.movable.SolidPlatform;
import game.movable.TimedEnemy;
import game.objects.Particle;

import java.io.File;

import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import ui.accessories.Playable;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

@Playable(name="Race", description="Stage: Race\nAuthor: Pojahn Moradi\nDifficulty: 1\nAverage time: 45 sec\nProfessional time: 35 sec\nObjective: Race to the finish!")
public class Race extends Stage
{
	private Pixmap stageImage;
	private Image2D backgroundImg, foregroundImg;
	private Image2D[] deathImg, mainImage, blImg, contImg1, contImg2, contImg3, boImg, bo2Img, flagImg;
	private GravityMan gm, cont1, cont2, cont3;
	private PressedButtons[] replay1, replay2, replay3;
	private Sound jump, jump1, jump2, jump3, bounceball, bounceblock;
	private String pos1, pos2, pos3;
	
	@Override
	@SuppressWarnings("deprecation")
	public void init() 
	{
		try
		{
			mainImage   = Image2D.loadImages(new File("res/general/main"), true);
			bo2Img	    = Image2D.loadImages(new File("res/race/bounce2"), true);
			contImg1	= Image2D.loadImages(new File("res/race/cont1"),true);
			contImg2	= Image2D.loadImages(new File("res/race/cont2"),true);
			contImg3	= Image2D.loadImages(new File("res/race/cont3"),true);
			flagImg	    = Image2D.loadImages(new File("res/race/flag"),false);
			blImg       = Image2D.loadImages("res/race/blocker.png");			
			boImg       = Image2D.loadImages("res/race/bounce.png");
			
			backgroundImg = new BigImage("res/race/background.png", RenderOption.PORTION);
			foregroundImg = new BigImage("res/race/foreground.png", RenderOption.PORTION);
			
			replay1 = (PressedButtons[]) PressedButtons.decode("res/race/cont1.rlp")[1];
			replay2 = (PressedButtons[]) PressedButtons.decode("res/race/cont2.rlp")[1];
			replay3 = (PressedButtons[]) PressedButtons.decode("res/race/cont3.rlp")[1];
			
			deathImg   		  = Image2D.loadImages(new File("res/general/main/death"), false);
			stageImage        = new Pixmap(new FileHandle("res/race/map.png"));
			game.timeColor = Color.WHITE;
			
			jump	    = TinySound.loadSound(new File("res/general/jump.wav"));
			jump1	    = TinySound.loadSound(new File("res/general/jump.wav"));
			jump2	    = TinySound.loadSound(new File("res/general/jump.wav"));
			jump3	    = TinySound.loadSound(new File("res/general/jump.wav"));
			bounceball  = TinySound.loadSound(new File("res/race/bounceball.wav"));
			bounceblock = TinySound.loadSound(new File("res/race/bounceblock.wav"));
			
			setStageMusic(TinySound.loadMusic(new File("res/race/song.ogg"),true), 3.325, 1.0f);
		}
		catch(Exception e)
		{
			System.err.println("Warning: Could not load the resources.");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	@Override
	public void build()
	{
		/*
		 * Standard stuff
		 *******************************************
		 */
		super.build();
		
		stageData = Utilities.createStageData(stageImage);
		
		background(backgroundImg);
		foreground(foregroundImg);
		
		basicInits();
		pos1 = pos2 = pos3 = null;	
		
		/*
		 * Main Character
		 *******************************************
		 */
		gm = new GravityMan();
		gm.setImage(new Animation<>(3, mainImage));
		gm.setMultiFaced(true);
		gm.setController((Controller)Utilities.importObject("res/data/controller1.con"));
		gm.hit(1);
		gm.setJumpingSound(jump);
		gm.moveTo(startX, startY);
		gm.addTileEvent(Factory.slipperWalls(gm));
		gm.zIndex(10);
		gm.deathImg = new Particle();
		gm.deathImg.setImage(4,deathImg);
		gm.deathImg.zIndex(101);
		game.addFocusObject(gm);
		add(gm);

		/*
		 * Contestants
		 *******************************************
		 */
		
		cont1 = new GravityMan();
		cont1.setImage(new Animation<>(3, contImg1));
		cont1.moveTo(startX, startY);
		cont1.addTileEvent(Factory.slipperWalls(cont1));
		cont1.setMultiFaced(true);
		cont1.setJumpingSound(jump1);
		cont1.getSoundBank().useFallOff(true);
		cont1.getSoundBank().maxDistance = 700;
		cont1.getSoundBank().power = 80;
		cont1.removeTileEvent(null);
		cont1.ghostify(replay1);
		cont1.addTileEvent(new TileEvent()
		{	
			boolean used = false;
			
			@Override
			public void eventHandling(byte tileType) 
			{
				if(!used && tileType == Engine.GOAL)
				{
					used = true;
					add(new TimedEnemy()
					{
						{
							zIndex(200);
							time = 150;
						}
						
						@Override
						public void drawSpecial(SpriteBatch batch) 
						{
							if(pos1 == null)
								pos1 = game.getGlobalState() == GameState.COMPLETED ? "4th" : "3rd";
							
							game.hudCamera();
							
							game.timeFont.setColor(Color.WHITE);
							game.timeFont.draw(batch, pos1 + " place goes to Weed Guy!", game.getScreenWidth() / 2 - 190, game.getScreenHeight() / 2 - 50);
							
							game.gameCamera();
						}
					});
				}
			}
		});
		add(cont1);
		
		cont2 = new GravityMan();
		cont2.setImage(new Animation<>(3, contImg2));
		cont2.moveTo(startX, startY);
		cont2.addTileEvent(Factory.slipperWalls(cont2));
		cont2.setMultiFaced(true);
		cont2.setJumpingSound(jump2);
		cont2.getSoundBank().useFallOff(true);
		cont2.getSoundBank().maxDistance = 700;
		cont2.getSoundBank().power = 80;
		cont2.removeTileEvent(null);
		cont2.ghostify(replay2);
		cont2.addTileEvent(new TileEvent()
		{	
			boolean used = false;
			
			@Override
			public void eventHandling(byte tileType) 
			{
				if(!used && tileType == Engine.GOAL)
				{
					used = true;
					add(new TimedEnemy()
					{
						{
							zIndex(200);
							time = 150;
						}
						
						@Override
						public void drawSpecial(SpriteBatch batch) 
						{
							if(pos2 == null)
								pos2 = game.getGlobalState() == GameState.COMPLETED ? "3rd" : "2nd";
							
							game.hudCamera();
							
							game.timeFont.setColor(Color.WHITE);
							game.timeFont.draw(batch, pos2 + " place goes to White Boy!", game.getScreenWidth() / 2 - 190, game.getScreenHeight() / 2 - 50);
							
							game.gameCamera();
						}
					});
				}
			}
		});
		add(cont2);
		
		cont3 = new GravityMan();
		cont3.setImage(new Animation<>(3, contImg3));
		cont3.moveTo(startX, startY);
		cont3.addTileEvent(Factory.slipperWalls(cont3));
		cont3.setMultiFaced(true);
		cont3.setJumpingSound(jump3);
		cont3.getSoundBank().useFallOff(true);
		cont3.getSoundBank().maxDistance = 700;
		cont3.getSoundBank().power = 80;
		cont3.removeTileEvent(null);
		cont3.ghostify(replay3);
		cont3.addTileEvent(new TileEvent()
		{	
			boolean used = false;
			
			@Override
			public void eventHandling(byte tileType) 
			{
				if(!used && tileType == Engine.GOAL)
				{
					used = true;
					add(new TimedEnemy()
					{
						{
							zIndex(200);
							time = 150;
						}
						
						@Override
						public void drawSpecial(SpriteBatch batch) 
						{
							if(pos3 == null)
								pos3 = game.getGlobalState() == GameState.COMPLETED ? "2nd" : "1st";
							
							game.hudCamera();
							
							game.timeFont.setColor(Color.WHITE);
							game.timeFont.draw(batch, pos3 + " place goes to Blackie!", game.getScreenWidth() / 2 - 190, game.getScreenHeight() / 2 - 50);
							
							game.gameCamera();
						}
					});
				}
			}
		});
		add(cont3);
		
		/*
		 * Hindrances
		 *******************************************
		 */
		SolidPlatform blocker1 = new SolidPlatform(1104, 511, gm, cont1, cont2, cont3);
		blocker1.setImage(new Animation<>(1, blImg));
		blocker1.setMoveSpeed(1);
		blocker1.setStrictGlueMode(false);
		blocker1.appendPath(1104, 511, 0, false, null);
		blocker1.appendPath(1176, 511, 0, false, null);
		
		SolidPlatform blocker2 = blocker1.getClone(1176, 310);
		blocker2.clearData();
		blocker2.appendPath(1176, 310, 0, false, null);
		blocker2.appendPath(1104, 310, 0, false, null);
		
		SolidPlatform blocker3 = blocker1.getClone(1104, 119);
		blocker3.clearData();
		blocker3.appendPath(1104, 119, 0, false, null);
		blocker3.appendPath(1176, 119, 0, false, null);
		
		SolidPlatform blocker4 = blocker1.getClone(1272, 511);
		blocker4.clearData();
		blocker4.appendPath(1272, 511, 0, false, null);
		blocker4.appendPath(1363, 511, 0, false, null);
		
		SolidPlatform blocker5 = blocker1.getClone(1363, 310);
		blocker5.clearData();
		blocker5.appendPath(1363, 310, 0, false, null);
		blocker5.appendPath(1272, 310, 0, false, null);
		
		SolidPlatform blocker6 = blocker1.getClone(1272, 119);
		blocker6.clearData();
		blocker6.appendPath(1272, 119, 0, false, null);
		blocker6.appendPath(1363, 119, 0, false, null);
		
		add(blocker1, blocker2, blocker3, blocker4, blocker5, blocker6);

		Bouncer b = new Bouncer(1670, 491, 350, 1, null, gm, cont1, cont2, cont3);
		b.setImage(new Animation<>(1, boImg));
		b.setHitbox(Hitbox.CIRCLE);
		b.setMoveSpeed(2);
		b.setShake(true, 30, 1, 1);
		b.appendPath(1670, 491, 0, false, null);
		b.appendPath(1670, 350, 0, false, null);
		b.setShakeSound(bounceball, 5);
		b.getSoundBank().useFallOff(true);
		
		Bouncer b2 = new Bouncer(1800, 532, 400, 3, Direction.W, gm, cont1, cont2, cont3);
		b2.setImage(new Animation<>(5, bo2Img));
		b2.setHitbox(Hitbox.EXACT);
		b2.setMoveSpeed(2);
		b2.appendPath(1800, 532, 0, false, null);
		b2.appendPath(2110, 532, 0, false, null);
		b2.setShakeSound(bounceblock, 5);
		
		Bouncer b3 = new Bouncer(2110, 500, 400, 3, Direction.W, gm, cont1, cont2, cont3);
		b3.setImage(new Animation<>(5, bo2Img));
		b3.setHitbox(Hitbox.EXACT);
		b3.setMoveSpeed(2);
		b3.appendPath(2110, 500, 0, false, null);
		b3.appendPath(1800, 500, 0, false, null);
		b3.setShakeSound(bounceblock, 5);
		
		add(b, b2, b3);
		
		/*
		 * Other
		 *******************************************
		 */
		MovableObject flag = new MovableObject();
		flag.setImage(new Animation<>(6, flagImg));
		flag.moveTo(3912, 514);
		add(flag);
	}

	@Override
	public void dispose() 
	{
		disposeBatch(stageImage, backgroundImg, foregroundImg, deathImg, mainImage, blImg, contImg1, contImg2, contImg3, boImg, bo2Img, flagImg, jump, jump1, jump2, jump3, bounceball, bounceblock, music);
	}
}
