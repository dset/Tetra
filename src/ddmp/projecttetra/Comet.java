package ddmp.projecttetra;

import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;

import com.badlogic.gdx.physics.box2d.Body;

/**
 * The comet that the player controls in the game.
 */
public class Comet extends PhysicsConnector {

	public Comet(Sprite cometSprite, Body cometBody) {
		super(cometSprite, cometBody, true, true);
	}

}
