/*  TeamEnum.java
 *  Created by: Yige
 *  Purpose: Enumeration for each team.
 *  Revision History:
 *  11/11/2016 - Yige : Fixed a typo.
 *  11/10/2016 - Yige : Created the file, added enumeration for teams.
 */

package robowars.pieces;

import java.util.EnumSet;

public enum TeamEnum {

	RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE;

	public static EnumSet<TeamEnum> Two = EnumSet.of(TeamEnum.RED, TeamEnum.GREEN);
	
	public static EnumSet<TeamEnum> Three = EnumSet.of(TeamEnum.RED, TeamEnum.YELLOW, TeamEnum.BLUE);
	

}

