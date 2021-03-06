package game.essentials;

import static game.core.Engine.*;
import game.core.Engine;
import game.core.Engine.Direction;
import game.core.Fundementals;
import game.core.GameObject;
import game.core.GameObject.Event;
import game.core.MainCharacter;
import game.core.MovableObject;
import game.core.MovableObject.TileEvent;
import game.core.Stage;
import game.mains.GravityMan;
import game.movable.PathDrone;
import game.movable.PathDrone.PathData;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * A collection of static method that mostly returns different type of events.
 * @author Pojahn Moradi
 *
 */
public class Factory 
{	
	/**
	 * Chains the given set of sounds, playing them after each other.
	 * @param indexes Indexes to the sound array.
	 * @param sounds The sounds to play.
	 * @param loop If the chain should loop.
	 * @return The event.
	 */
	public static Event soundChain(final int[] indexes, final Music[] sounds, final boolean loop)
	{
		return new Event() 
		{
			int counter = 0;
			boolean done;
			
			@Override
			public void eventHandling() 
			{
				if(!done)
				{
					if(counter > indexes.length - 1)
					{
						if(loop)
							counter = 0;
						else
						{
							done = true;
							return;
						}
					}
					
					Music sound = sounds[indexes[counter]];

					if(sound.done())
					{
						sound.stop();
						counter++;
					}
					else if(!sound.playing())
						sound.play(false);
					
				}
			}
		};
	}
	
	
	/**
	 * Fades the animation in our or out, depending in the value of {@code strength}. For this event to work correctly, the entire animation should use the same starting alpha value.<br>
	 * If you want to have individual frames in the animation to fade different from each other, you should create multiple instances of this event where each instance is only connected to one(or more) image.<br>
	 * This event is automatically discarded when it is done and should always be added to the stage rather than to a {@code GameObject}.<br>
	 * @param targetAlpha The alpha value we want to reach. When ever the animation alpha have reached its goal, this event will be discarded.
	 * @param strength The amount of alpha to apply to the animation. This can be either negative, for fading out, or positive for fading in. 
	 * @param freq How often to add or subtract {@code strength} to the animation, in frames. 
	 * @param endEvent Not required. This event will be added to the stage when it is done.
	 * @param animation The animation to manipulate the alpha on.
	 * @return The event, which should be added to the stage and NOT a {@code GameObject}.
	 */
	public static Event fade(final float targetAlpha, final float strength, final int freq, final Event endEvent, final Image2D... animation)
	{
		return new Event()
		{
			int counter = 0;
			
			@Override
			public void eventHandling() 
			{
				if(++counter % freq == 0)
				{
					float alpha = animation[0].getColor().a;
						
					if((strength > 0 && alpha > targetAlpha) ||
					   (strength < 0 && alpha < targetAlpha))
					{
						for(Image2D img : animation)
							img.setAlpha(targetAlpha);
						
						Stage.getCurrentStage().discard(this);
						if(endEvent != null)
							Stage.getCurrentStage().add(endEvent);
					}
					else
					{
						float newValue = alpha + strength;
						
						for(Image2D img : animation)
							img.setAlpha(newValue);
					}
				}
			}
		};
	}
	
	/**
	 * Fades a sound in or out, depending on the value given to {@code targetVolume}.
	 * @param sound The sound to fade.
	 * @param targetVolume The target volume.
	 * @param duration The duration, in milliseconds.
	 * @param stopWhenDone Whether or not to stop the sound when the target volume have been reached.
	 * @return The event, which should be appended to the stage.
	 */
	public static Event soundFade(final Music sound, final float targetVolume, final int duration, final boolean stopWhenDone)
	{
		return new Event()
		{	
			int fadeTime = duration;
			double startVolume = sound.getVolume();
			
			@Override
			public void eventHandling() 
			{
				fadeTime -= Engine.getDelta();
				if (fadeTime < 0) 
				{
					fadeTime = 0;
					if (stopWhenDone)
						sound.stop();
					Stage.getCurrentStage().discard(this);
				}
				else
				{
					double offset = (targetVolume - startVolume) * (1 - (fadeTime / (double)duration));
					sound.setVolume(startVolume + offset);
				}
			}
		};
	}
	
	/**
	 * If the distance between {@code go1} and {@code go2} exceed {@code maxDistance}, the sound gets muted. 
	 * @param sound The sound to be used.
	 * @param go1 The first object.
	 * @param go2 The second object.
	 * @param maxDistance The max distance.
	 * @param freq How often to update the volume, in frames. 0 = best visual results but worst performance.
	 * @power How much should the sound increase decrease when approaching it? <br>
	 * Setting this to a low value, such as 1 will means the sound effect will reach {@code max} when the two {@code GameObjects} are really close.
	 * @max The max volume of this sound.
	 * @return The event, which can be appended anywhere.
	 */
	public static Event soundFalloff(final Music sound, final GameObject go1, final GameObject go2, final float maxDistance, final int freq, final float power, final float max)
	{
		return new Event()
		{
			float counter = 0;
			
			@Override
			public void eventHandling() 
			{
				if(freq == 0 || ++counter % freq == 0)
				{
					double distance = Fundementals.distance(go1, go2);
					float candidate = (float) (power * Math.max((1 / Math.sqrt(distance)) - (1 / Math.sqrt(maxDistance)), 0));
					
					sound.setVolume(Math.min(candidate, max));
				}
			}
		};
	}
	
	/**
	 * Reefer to soundFalloff(Music, GameObject, GameObject, float, int, float, float) for usage.
	 */
	public static Event soundFalloff(Music sound, GameObject go, float x2, float y2, float maxDistance, int freq, float power, float max)
	{
		GameObject go2 = new GameObject();
		go2.loc.x = x2;
		go2.loc.y = y2;
		
		return soundFalloff(sound,go,go2,maxDistance,freq,power,max);
	}
	
	/**
	 * Reefer to soundFalloff(Music, GameObject, GameObject, float, int, float, float) for usage.
	 */
	public static Event soundFalloff(final Sound sound, final GameObject go1, final GameObject go2, final float maxDistance, final int freq, final float power, final float max)
	{
		return new Event()
		{
			float counter = 0;
			
			@Override
			public void eventHandling() 
			{
				if(freq == 0 || ++counter % freq == 0)
				{
					double distance = Fundementals.distance(go1, go2);
					float candidate = (float) (power * Math.max((1 / Math.sqrt(distance)) - (1 / Math.sqrt(maxDistance)), 0));
					
					sound.setVolume(Math.min(candidate, max));
				}
			}
		};
	}
	
	/**
	 * Reefer to soundFalloff(Music, GameObject, GameObject, float, int, float, float) for usage.
	 */
	public static Event soundFalloff(Sound sound, GameObject go, float x2, float y2, float maxDistance, int freq, float power, float max)
	{
		GameObject go2 = new GameObject();
		go2.loc.x = x2;
		go2.loc.y = y2;
		
		return soundFalloff(sound,go,go2,maxDistance,freq,power,max);
	}
	
	/**
	 * An alternative way of creating a tile deformer. The tile transformation is applied once every frame if its current position differ from its previous one.<br>
	 * Is not limited to rectangular hitboxes.
	 * @param target The unit which will transform the tile.
	 * @param tileType The tile type to transform to.
	 * @param transformBack Whether or not to transform the tile on its previous position back to its initial state(i e when moving).
	 * @return The event, which can be added anywhere.
	 */
	public static Event tileDeformer(final MovableObject target, final byte tileType, final boolean transformBack)
	{
		return ()->
		{
			if(target.getPrevX() != target.loc.x || target.getPrevY() != target.loc.y)
			{
				byte[][] data  = Stage.getCurrentStage().stageData;
				Animation<Image2D> img = target.getImage();
				boolean stopped = img.isStopped();
				img.stop(true);
				Image2D image = target.getFrame();
				img.stop(stopped);
				
				int prevX = (int) target.getPrevX(),
					prevY = (int) target.getPrevY(),
					currX = (int) target.loc.x,
					currY = (int) target.loc.y;
				
				for(int x1 = prevX, x2 = currX; x1 < prevX + target.width - 1; x1++, x2++)
					for(int y1 = prevY, y2 = currY; y1 < prevY + target.height - 1; y1++, y2++)
					{
						if(transformBack)
							data[y1][x1] = Stage.getCurrentStage().getCloneData(x1, y1);
						
						int color = image.getColor(x2 - currX, y2 - currY);
						if(color != 0)
							data[y2][x2] = tileType;
					}
			}
		};
	}
	
	/**
	 * Prints text on the given {@code GameObject}.
	 * @param text The text to print.
	 * @param textColor The text color. Null is accepted.
	 * @param font The font to use. 
	 * @param duration The amount of frames the text should be visible.
	 * @param position The position the text will be drawn on.
	 * @param ox The X offset of the text.
	 * @param oy The Y offset of the text.
	 * @param endEvent The event that will be executed(once) when done. Null is accepted.
	 * @return The event.
	 */
	public static GameObject printText(final String text, final Color textColor, final BitmapFont font, final int duration, final GameObject position, final float ox, final float oy, final Event endEvent)
	{
		return new GameObject()
		{
			boolean hasWork = true;
			int counter;
			BitmapFont theFont;
			
			{
				if(font != null)
					theFont = font;
				else
					theFont = Stage.getCurrentStage().game.timeFont;
			}
			
			@Override
			public void drawSpecial(SpriteBatch b) 
			{
				if(hasWork)
				{
					if(counter++ > duration)
					{
						hasWork = false;
						if(endEvent != null)
							endEvent.eventHandling();
					}
					if(textColor != null)
						theFont.setColor(textColor);

					theFont.drawMultiLine(b, text, position.loc.x + ox, position.loc.y + oy);
				}
			}
		};
	}
	
	/**
	 * Manipulates the given {@code GameObjects} offset variables to make it "wobble".
	 * @param go The object to wobble.
	 * @param xMin The minimum X position of the wobble.
	 * @param xMax The maximum X position of the wobble.
	 * @param yMin The minimum Y position of the wobble.
	 * @param yMax The maximum Y position of the wobble.
	 * @param freq How often to add the wobble effect.
	 * @return The event.
	 */
	public static Event wobble(final GameObject go, final float xMin, final float xMax, final float yMin, final float yMax, final float freq)
	{
		return new Event()
		{	
			ThreadLocalRandom r = ThreadLocalRandom.current();
			int counter = 0;
			
			@Override
			public void eventHandling() 
			{
				if(++counter % freq == 0)
				{
					go.offsetX = (float) r.nextDouble(xMin, xMax);
					go.offsetY = (float) r.nextDouble(yMin, yMax);
				}
			}
		};
	}
	
	/**
	 * Forces the {@code tail} to follow the {@code target}. Note that the following functionality is instant.
	 * @param target The object to follow.
	 * @param tail The object that will follow someone.
	 * @param offsetX The offset X of the following.
	 * @param offsetY The offset Y of the following.
	 * @return The event.
	 */
	public static Event follow(final GameObject target, final GameObject tail, final float offsetX, final float offsetY)
	{
		return new Event()
		{
			@Override
			public void eventHandling() 
			{
				tail.loc.x = target.loc.x + offsetX;
				tail.loc.y = target.loc.y + offsetY;
			}
		};
	}
	
	/**
	 * Updates the given {@code PathDrone} every frame, giving it a follow functionality.
	 * @param target The unit to follow.
	 * @param tail The follower.
	 * @param offsetX The offset X.
	 * @param offsetY The offset Y.
	 * @return The event.
	 */
	public static Event pathDroneFollow(final GameObject target, final PathDrone tail, final float offsetX, final float offsetY)
	{
		return new Event()
		{
			@Override
			public void eventHandling() 
			{				
				float targetX = target.loc.x + offsetX;
				float targetY = target.loc.y + offsetY;
				
				tail.clearData();
				tail.appendPath(targetX, targetY);
			}
		};
	}
	
	/**
	 * Creates an event that is behaving like a weak platform.
	 * @param target The {@code GameObject} to behave like a weak platform.
	 * @param destroyAnim The animation to use when the platform is demolishing.
	 * @param destroyTime The amount of frames it takes before the platform is fully demolished(i e it is discarded from the game).
	 * @param removeSound The sound to be played when the object is discarded.
	 * @param users The {@code GameObject} capable of interacting with this weak platform.
	 * @return The event.
	 */
	public static Event weakPlatform(final GameObject target, final Animation<Image2D> destroyAnim, final int destroyTime, final Sound removeSound, final MovableObject... users)
	{
		return new Event()
		{
			GameObject dummy = new GameObject();
			boolean collapsing = false;
			int destroyCounter = 0;
			
			{
				for(MovableObject mo : users)
					mo.avoidOverlapping(target);
			}
			
			@Override
			public void eventHandling() 
			{
				dummy.loc.x = target.loc.x - 1;
				dummy.loc.y = target.loc.y - 1;
				dummy.width = target.width + 2;
				dummy.height= target.height+ 2;
				
				if(!collapsing && dummy.collidesWithMultiple(users) != null)
				{
					collapsing = true;
					if(destroyAnim != null)
						target.setImage(destroyAnim);
				}
				if(collapsing && destroyCounter++ > destroyTime)
				{
					for(MovableObject mo : users)
						mo.allowOverlapping(target);
					
					Stage.getCurrentStage().discard(target);
					if(removeSound != null)
						removeSound.play();
				}
			}
		};
	}
	
	/**
	 * Manipulates the {@code GravityMans vx} and {@code vy} variables to add push effect when intersecting with the given tile type. 
	 * @param man The man to apply push effect on.
	 * @param tile The tile the {@code man} should instersect with to trigger the pushing.
	 * @param blowStrength The strength of the push. Must always be posetive.
	 * @param maxStrength The max strength of the pushing. Must always be posetive.
	 * @param dir The direction to push.
	 * @return The event.
	 */
	public static TileEvent windEvent(final GravityMan man, final byte tile, final float blowStrength, final float maxStrength, final Direction dir)
	{
		return new TileEvent()
		{	
			@Override
			public void eventHandling(byte tileType) 
			{
				if(tile == tileType)
				{
					switch (dir)
					{
					case N:
						if(man.vy < maxStrength)
							man.vy += blowStrength;
						break;
					case NE:
						if(man.vy < maxStrength)
							man.vy += blowStrength;
						if(-man.vx < maxStrength)
							man.vx -= blowStrength;
						break;
					case E:
						if(-man.vx < maxStrength)
							man.vx -= blowStrength;
						break;
					case SE:
						if(-man.vy < maxStrength)
							man.vy -= blowStrength;
						if(-man.vx < maxStrength)
							man.vx -= blowStrength;
						break;
					case S:
						if(-man.vy < maxStrength)
							man.vy -= blowStrength;
						break;
					case SW:
						if(-man.vy < maxStrength)
							man.vy -= blowStrength;
						if(man.vx < maxStrength)
							man.vx += blowStrength;
						break;
					case W:
						if(man.vx < maxStrength)
							man.vx += blowStrength;
						break;
					case NW:
						if(man.vy < maxStrength)
							man.vy += blowStrength;
						if(man.vx < maxStrength)
							man.vx += blowStrength;					
						break;
					}
				}
			}
		};
	}
	
	/**
	 * Behaves exactly like {@code Factory.windEvent}, but modifies the position directly rather than altering the velocity.
	 */
	public static TileEvent pushEvent(final MovableObject mo, final byte tile, final float pushStrength, final Direction dir)
	{
		return new TileEvent()
		{	
			@Override
			@SuppressWarnings("deprecation")
			public void eventHandling(byte tileType) 
			{
				if(tile == tileType)
				{
					switch (dir)
					{
					case N:
						if(mo.canGoUp(mo.loc.y - pushStrength))
							mo.loc.y -= pushStrength;
						break;
					case NE:
						if(mo.canGoUp(mo.loc.y - pushStrength))
							mo.loc.y -= pushStrength;
						if(mo.canGoRight(mo.loc.x + pushStrength))
							mo.loc.x += pushStrength;
						break;
					case E:
						if(mo.canGoRight(mo.loc.x + pushStrength))
							mo.loc.x += pushStrength;
						break;
					case SE:
						if(mo.canGoDown(mo.loc.y + pushStrength))
							mo.loc.y += pushStrength;
						if(mo.canGoRight(mo.loc.x + pushStrength))
							mo.loc.x += pushStrength;
						break;
					case S:
						if(mo.canGoDown(mo.loc.y + pushStrength))
							mo.loc.y += pushStrength;
						break;
					case SW:
						if(mo.canGoDown(mo.loc.y + pushStrength))
							mo.loc.y += pushStrength;
						if(mo.canGoLeft(mo.loc.x - pushStrength))
							mo.loc.x -= pushStrength;
						break;
					case W:
						if(mo.canGoLeft(mo.loc.x - pushStrength))
							mo.loc.x -= pushStrength;
						break;
					case NW:
						if(mo.canGoUp(mo.loc.y - pushStrength))
							mo.loc.y -= pushStrength;
						if(mo.canGoLeft(mo.loc.x - pushStrength))
							mo.loc.x -= pushStrength;
						break;
					}
				}
			}
		};
	}
	
	/**
	 * Prints the specified text when the returned {@code GameObject} collides with one of the subjects.
	 * @param text The text to print.
	 * @param textColor The color to use. If null is used, the current color of the graphic context will be used.
	 * @param font The font to use.
	 * @param textDuration How long the text will stay active.
	 * @param ox The x offset of the text.
	 * @param oy The y offset of the text.
	 * @param subjects The {@code GameObjects} that can trigger this event.
	 * @return The printer object.
	 */
	public static GameObject textPrinter(final String text, final Color textColor, final BitmapFont font, final int textDuration, final int ox, final int oy, final GameObject... subjects)
	{
		return new GameObject()
		{
			int counter = 0;
			
			@Override
			public void drawSpecial(SpriteBatch b) 
			{
				if(counter-- > 0)
				{
					BitmapFont f = (font == null) ? Stage.getCurrentStage().game.timeFont : font;
					
					if(textColor != null)
						f.setColor(textColor);
					
					f.draw(b, text, loc.x + ox, loc.y + oy);
				}
				else
				{
					for(GameObject go : subjects)
						if(collidesWith(go))
						{
							counter = textDuration;
							break;
						}
				}
			}
		};
	}
	
	/**
	 * Alters the {@code MainCharacter's} health in case of collision with the given {@code GameObject}.
	 * @param obj The object capable of hitting the main character.
	 * @param main The character capable of getting hit.
	 * @param power The power to add(use negative values to deduct) upon collision.
	 * @return The {@code Event}.
	 */
	public static Event hitMain(final GameObject obj, final MainCharacter main, final int power)
	{
		return ()->
		{
			if(obj.collidesWith(main))
			{
				main.hit(power);
				main.runHitEvent(obj);
			}
		};
	}
	
	/**
	 * When the specified character intersects with AREA_TRIGGER_0, wall jumping and sliding is disabled and is re-enabled when the character intersect with AREA_TRIGGER_1
	 * @param man The character to apply the effect on.
	 * @return The tile event.
	 */
	public static TileEvent slipperWalls(final GravityMan man)
	{
		return new TileEvent()
		{
			@Override
			public void eventHandling(byte tileType) 
			{
				if(tileType == AREA_TRIGGER_0)
				{
					man.enableWallJump(false);
					man.enableWallSlide(false);
				}
				else if(tileType == AREA_TRIGGER_1)
				{
					man.enableWallJump(true);
					man.enableWallSlide(true);					
				}
			}
		};
	}
	
	/**
	 * Decreases the given {@code GameObjects} animation speed until the limit is reached.
	 * @param go The object to modify.
	 * @param brakeSpeed How often, in frames, to slow down the animation speed.
	 * @param limit Stops when the animation speed reaches this number. Higher means slower speed.
	 * @return The event.
	 */
	public static Event animationBrake(final GameObject go, final int brakeSpeed, final int limit)
	{
		return new Event()
		{
			boolean stop = false;
			int counter, endCounter = 0;
			
			@Override
			public void eventHandling() 
			{
				if(!stop && ++counter % brakeSpeed == 0)
				{
					if(endCounter++ > limit)
					{
						stop = true;
						go.setAnimationSpeed(999999999);
						return;
					}
					go.setAnimationSpeed(go.getAnimationSpeed() + 1);
				}
			}
		};
	}
	
	/**
	 * Returns a laser beam with customized look.<br>
	 * Each of the parameters accept null, which means "skip rendering this part".
	 * @param laserBegin The "gunfire" animation, which will be rendered at the source coordinate.
	 * @param laserBeam The actual laser beam. This should be a rectangular image and is stretched and rotated to target destination coordinate.
	 * @param laserImpact The animation to render at the destination point.
	 * @return The beam.
	 */
	public static LaserBeam threeStageLaser(final Animation<Image2D> laserBegin, final Animation<Image2D> laserBeam, final Animation<Image2D> laserImpact)
	{
		return new LaserBeam()
		{
			class Task
			{
				float srcX, srcY, destX, destY;
				int active;
				
				public Task(float srcX, float srcY, float destX, float destY, int active) 
				{
					this.srcX = srcX;
					this.srcY = srcY;
					this.destX = destX;
					this.destY = destY;
					this.active = active;
				}
			}
			
			LinkedList<Task> tasks = new LinkedList<>();
			
			@Override
			public void fireAt(float srcX, float srcY, float destX, float destY, int active) 
			{
				tasks.add(new Task(srcX,srcY,destX,destY,active));
			}
			
			@Override
			public void renderLasers(SpriteBatch b) 
			{
				int size = tasks.size();
				for(int i = 0; i < size; i++)
				{
					final Task t = tasks.get(i);
					final float angle = (float)Fundementals.getAngle(t.srcX, t.srcY, t.destX, t.destY);
					
					if(laserBeam != null)
					{
						Image2D beam = laserBeam.getObject();
						float dx = (float) (beam.getHeight() / 2 * Math.cos(Math.toRadians(angle - 90)));
						float dy = (float) (beam.getHeight() / 2 * Math.sin(Math.toRadians(angle - 90)));
						b.draw(beam, t.srcX + dx, t.srcY + dy, 0, 0, (float)Fundementals.distance(t.srcX + dx, t.srcY + dy, t.destX, t.destY), beam.getHeight(), 1, 1, angle);
					}
					
					if(laserImpact != null)
					{
						Image2D exp = laserImpact.getObject();
						float halfWidth = exp.getWidth() / 2;
						float halfHeight = exp.getHeight() / 2;
						b.draw(exp, t.destX - halfWidth, t.destY - halfHeight, halfWidth, halfHeight, exp.getWidth(), exp.getHeight(), 1, 1, angle);
					}
					
					if(laserBegin != null)
					{
						Image2D begin = laserBegin.getObject();
						float halfWidth = begin.getWidth() / 2;
						float halfHeight = begin.getHeight() / 2;
						b.draw(begin, t.srcX - halfWidth, t.srcY - halfHeight, halfHeight, halfHeight, begin.getWidth(), begin.getHeight(), 1, 1, angle);
					}
					
					if(0 >= --t.active)
					{
						tasks.remove(t);
						size--;
					}
				}
			}
		};
	}
	
	/**
	 * Returns a {@code LaserBeam} with default images.
	 * @return The beam.
	 */
	public static LaserBeam defaultLaser()
	{
		if(LASER_BEGIN == null || LASER_BEAM == null || LASER_IMPACT == null)
			throw new NullPointerException("The laser resources is null. Check if they still exist.");
		
		Animation<Image2D> laserBegin = new Animation<>(3, LASER_BEGIN);
		Animation<Image2D> laserImage = new Animation<>(3, LASER_BEAM);
		Animation<Image2D> laserImpact = new Animation<>(3, LASER_IMPACT);
		laserImage.pingPong(true);
		laserImpact.pingPong(true);
		
		return threeStageLaser(laserBegin, laserImage, laserImpact);
	}

	/**
	 * Returns a charge {@code LaserBeam} with default images.
	 * @return The beam.
	 */
	public static LaserBeam defaultChargeLaser()
	{
		if(LASER_CHARGE == null)
			throw new NullPointerException("The laser resources is null. Check if they still exist.");
		
		Animation<Image2D> charge = new Animation<>(2, LASER_CHARGE);
		charge.pingPong(true);
		
		return threeStageLaser(null, charge, null);
	}
	
	/**
	 * Fade to the room music if inside the given rectangle. When a {@code listener} is colliding with the given rectangle, {@code roomMusic} will start fade in and {@code outsideMusic} out.
	 * @param room The room where {@code roomMusic} is played.
	 * @param roomMusic The music to play in the room.
	 * @param outsideMusic The music thats played outside the room. Usually the stage music. Null is accepted.
	 * @param fadeSpeed The speed to fade in/out.
	 * @param maxVolume The max volume of the songs when fading.
	 * @param listeners The entities interacting with this event.
	 * @return The event.
	 */
	public static Event roomMusic(Rectangle room, Music roomMusic, Music outsideMusic, double fadeSpeed, double maxVolume, GameObject... listeners)	//TODO: TEST
	{
		return ()->
		{
			boolean oneColliding = false;
			
			for(GameObject listener : listeners)
			{
				if(Fundementals.rectangleVsRectangle(listener.loc.x, listener.loc.y, listener.width(), listener.height(), room.x, room.y, room.width, room.height))
				{
					roomMusic.setVolume(Math.min(roomMusic.getVolume() + fadeSpeed, maxVolume));
					pauseCheck(roomMusic);
					
					if(outsideMusic != null)
					{
						outsideMusic.setVolume(Math.max(outsideMusic.getVolume() - fadeSpeed, 0));
						pauseCheck(outsideMusic);
					}
						
					oneColliding = true;
					break;
				}
			}
			
			if(!oneColliding)
			{
				roomMusic.setVolume(Math.max(roomMusic.getVolume() - fadeSpeed, 0));
				pauseCheck(roomMusic);
				
				if(outsideMusic != null)
				{
					outsideMusic.setVolume(Math.min(outsideMusic.getVolume() + fadeSpeed, maxVolume));
					pauseCheck(outsideMusic);
				}
			}
		};
	}
	
	private static void pauseCheck(Music music)
	{
		if(music.getVolume() == 0.0f && music.playing())
			music.pause();
		else if(music.getVolume() > 0.0f && !music.playing())
			music.resume();
	}
	
	/**
	 * Return an array of random waypoints, where the coordinates can be anywhere inside the given rectangle.
	 * @param x The X position of the rectangle.
	 * @param y The y position of the rectangle. 
	 * @param w The width of the rectangle.
	 * @param h The height of the rectangle.
	 * @param quantity The amount of waypoints to generate.
	 * @return The data.
	 */
	public static PathData[] randomWaypoints(float x, float y, float w, float h, int quantity)
	{
		List<PathData> pdlist = new ArrayList<>(quantity);
		
		for(int i = 0; i < quantity; i++)
		{
			float xp = (float) (x + Math.random() * w);
			float yp = (float) (y + Math.random() * h);
			
			pdlist.add(new PathData(xp, yp, 0, false, null));
		}
		
		return pdlist.toArray(new PathData[pdlist.size()]);
	}
	
	/**
	 * Return an array of random waypoints, where the coordinates can be anywhere inside the current stage.
	 * @return The data.
	 */
	public static PathData[] randomWaypoints()
	{
		return randomWaypoints(0, 0, Stage.getCurrentStage().size.width, Stage.getCurrentStage().size.height, new Random().nextInt(100) + 100);
	}
	
	/**
	 * Return an array of random waypoints, where the coordinate simulates a bouncing effect.<br> 
	 * The offset is the entire stage.
	 */
	public static PathData[] randomWallPoints()
	{
		return randomWallPoints(0, 0, Stage.getCurrentStage().size.width, Stage.getCurrentStage().size.height);
	}

	/**
	 * Return an array of random waypoints, where the coordinate simulates a bouncing effect.<br> 
	 * The offset is the entire stage with the given {@code GameObject's} width and height taken into consideration.
	 */
	public static PathData[] randomWallPoints(GameObject go)
	{
		return randomWallPoints(0, 0, (int)(Stage.getCurrentStage().size.width - go.width()), (int)(Stage.getCurrentStage().size.width - go.height()));
	}
	
	/**
	 * Return an array of random waypoints, where the coordinate simulates a bouncing effect.
	 */
	public static PathData[] randomWallPoints(int minX, int maxX, int minY, int maxY)
	{
		int last = -1;
		int quantity = new Random().nextInt(100) + 100;
		List<PathData> pdlist = new ArrayList<>(quantity);
		Random r = new Random();
		
		for(int i = 0; i < quantity; i++)
		{
			int dir = r.nextInt(4);
			if(dir != last)
			{
				last = dir;
				
				Point2D.Float point = getDirection(dir, minX, maxX, minY, maxY);
				pdlist.add(new PathData(point.x, point.y, 0, false, null));
			}
			else
				i--;
		}
		
		return pdlist.toArray(new PathData[pdlist.size()]);
	}
	
	
	static Point2D.Float getDirection(int dir, int minX, int maxX, int minY, int maxY)
	{
		Point2D.Float point = new Point2D.Float();
		final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3;
		
		Random r = new Random();
		
		switch(dir)
		{
		case UP:
			point.x = r.nextInt(maxX - minX) + minX;
			point.y = minY;
			break;
		case DOWN:
			point.x = r.nextInt(maxX - minX) + minX;
			point.y = maxY;			
			break;
		case LEFT:
			point.x = minX;
			point.y = r.nextInt(maxY - minY) + minY;
			break;
		case RIGHT:
			point.x = maxX;
			point.y = r.nextInt(maxY - minY) + minY;
			break;
		}
		
		return point;
	}
	
//	static Point2D.Float getDirection(int dir, GameObject go)
//	{
//		Point2D.Float point = new Point2D.Float();
//		final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3;
//		float w = go == null ? 0 : go.width();
//		float h = go == null ? 0 : go.height();
//		Random r = new Random();
//		
//		switch(dir)
//		{
//		case UP:
//			point.x = r.nextInt(Stage.getCurrentStage().size.width);
//			point.y = 0;
//			break;
//		case DOWN:
//			point.x = r.nextInt(Stage.getCurrentStage().size.width);
//			point.y = Stage.getCurrentStage().size.height - h;			
//			break;
//		case LEFT:
//			point.x = 0;
//			point.y = r.nextInt(Stage.getCurrentStage().size.height);
//			break;
//		case RIGHT:
//			point.x = Stage.getCurrentStage().size.width - w;
//			point.y = r.nextInt(Stage.getCurrentStage().size.height);
//			break;
//		}
//		
//		return point;
//	}
}