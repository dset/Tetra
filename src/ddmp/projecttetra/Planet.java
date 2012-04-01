package ddmp.projecttetra;

import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;

import com.badlogic.gdx.physics.box2d.Body;

/**
 * A planet in the game.
 */
public class Planet extends PhysicsConnector {

	public Planet(Sprite planetSprite, Body planetBody) {
		super(planetSprite, planetBody, true, true);
	}

}
