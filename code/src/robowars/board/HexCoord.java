/*	HexCoord.java
 * 	Created by: Tushita Patel
 * 	Purpose: Coordinate of one hex in the board.
 * 	Revision History:
 *  12/09/2016 - Janelle: Documentation sweep and final edits.
 *  11/11/2016 - Yige : Fixed wrong package name error and also added toString().
 * 	11/11/2016 - Tushita : Create the class and set up all fields and methods
 */

package robowars.board;

public class HexCoord {

	/**
	 * X-coordinate of the hexagon.
	 */
	private int x;
	
	/**
	 * Y-coordinate of the hexagon.
	 */
	private int y;
	
	/**
	 * Z-coordinate of the hexagon.
	 */
	private int z;

	public HexCoord(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
//		this.reduce();
	}
	
	public HexCoord(){
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}
	
	/**
	 * Reduces the coordinate to the standard form (with non-zero x and z, and zero y)
	 */
	public void reduce(){
		this.setX(this.getX() + this.getY());
		this.setZ(this.getY() + this.getZ());
		this.setY(0);
	}
	
	/**
	 *  Un-reduces - puts the coordinate back to the shortest way to get from (0,0,0) to the coordinate.
	 */
	 public void toVector(){
		 
		 this.reduce();
		 
		 if ((this.getX() <=0 && this.getZ() >= 0) || (this.getX()>= 0 && this.getZ()<=0)){
			 return;
		 }
		 else{
			 if (this.getX()<0 && this.getZ()<0){
				 this.setY(Math.max(this.getX(), this.getY()));
				 this.setX(this.getX() - this.getY());
				 this.setZ(this.getZ()-this.getY());
			 }
			 else{
				 this.setY(Math.min(this.getX(), this.getZ()));
				 this.setX(this.getX() - this.getY());
				 this.setZ(this.getZ()-this.getY());
			 }
		 }
	 }
	
	 /**
	  * Compares if the two coordinates are the same.
	  * @param coord : the coordinate to compare to.
	  * @return True if the same coordinate, false otherwise.
	  */
	 public boolean isSameAs(HexCoord coord){
		 coord.reduce();
		 this.reduce();
		 return (this.getX() == coord.getX() && this.getY() == coord.getY() && this.getZ() == coord.getZ());
	 }
	 
	/**
	 * Return coordinates in the format (x, y, z)
	 */
	public String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}
	
	
	public static void main(String args[]){
		
		//  Unit tests for the HexCoord class
		
		HexCoord c = new HexCoord(4, 5, 0);
		c.reduce();
		if (c.getX() != 9 || c.getY() != 0  || c.getZ() != 5){
			System.out.println("Error: " + c.toString());
		}
		
		c.toVector();
		if (c.getX() != 4 || c.getY() != 5  || c.getZ() != 0){
			System.out.println("Error in toVector(): " + c.toString());
		}
		
		c = new HexCoord(1, 0, 0);
		c.reduce();
		if (c.getX() != 1 || c.getY() != 0  || c.getZ() != 0){
			System.out.println("Error: " + c.toString());
		}
		
		c.toVector();
		if (c.getX() != 1 || c.getY() != 0  || c.getZ() != 0){
			System.out.println("Error in toVector(): " + c.toString());
		}
		
		c = new HexCoord(-4, 5, 0);
		c.reduce();
		if (c.getX() != 1 || c.getY() != 0  || c.getZ() != 5){
			System.out.println("Error: " + c.toString());
		}
		c.toVector();
		if (c.getX() != 0 || c.getY() != 1  || c.getZ() != 4){
			System.out.println("Error in toVector(): " + c.toString());
		}
		
		c = new HexCoord(-5, 5, -6);
		c.reduce();
		if (c.getX() != 0 || c.getY() != 0  || c.getZ() != -1){
			System.out.println("Error: " + c.toString());
		}
		c.toVector();
		if (c.getX() != 0 || c.getY() != 0  || c.getZ() != -1){
			System.out.println("Error in toVector(): " + c.toString());
		}
		
		c = new HexCoord(-4, 0, 4);
		c.reduce();
		if (c.getX() != -4 || c.getY() != 0  || c.getZ() != 4){
			System.out.println("Error: " + c.toString());
		}
		c.toVector();
		if (c.getX() != -4 || c.getY() != 0  || c.getZ() != 4){
			System.out.println("Error in toVector(): " + c.toString());
		}
		
		System.out.println("Tests finished.");
	}
}
