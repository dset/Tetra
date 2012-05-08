package ddmp.projecttetra;

import com.badlogic.gdx.math.Vector2;

/**
 * Holds utility methods.
 */
public class Utilities {
	
	/**
	 * Returns a random float f, such that low <= f < high.
	 */
	public static float getRandomFloatBetween(float low, float high) {
		if(high < low) {
			throw new IllegalArgumentException("high < low");
		}
		return (float) (low + (high - low) * Math.random());
	}
	
	/**
	 * Returns a random int i, such that low <= i <= high.
	 */
	public static int getRandomIntBetween(int low, int high) {
		if(high < low) {
			throw new IllegalArgumentException("high < low");
		}
		return (int) (low + (high + 1 - low) * Math.random());
	}
	
	/**
	 * Rotates the given vector by angle radians.
	 */
	public static void rotateVector(Vector2 vector, float angle) {
		if(vector == null) {
			throw new IllegalArgumentException("vector == null");
		}
		
		float x = (float) (vector.x * Math.cos(angle) - vector.y * Math.sin(angle));
		float y = (float) (vector.x * Math.sin(angle) + vector.y * Math.cos(angle));
		vector.set(x, y);
	}
	
}
