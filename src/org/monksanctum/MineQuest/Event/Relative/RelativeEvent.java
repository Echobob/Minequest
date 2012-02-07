package org.monksanctum.MineQuest.Event.Relative;

import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.NormalEvent;
import org.monksanctum.MineQuest.Quest.Quest;

public abstract class RelativeEvent extends NormalEvent {

	public RelativeEvent(long delay) {
		super(delay);
	}
	
	public static RelativeEvent newRelative(String[] split, Quest quest) throws Exception {
//		RelativeEvent relativeEvent = null;
		
		if (split[3].equals("")) {
			return null;
		} else {
			MineQuest.log("Error: Unknown Relative Event: " + split[3]);
			throw new Exception();
		}
		
//		relativeEvent.setId(Integer.parseInt(split[1]));
		
//		return relativeEvent;
	}

}
