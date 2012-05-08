package ddmp.projecttetra;

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
	
}
