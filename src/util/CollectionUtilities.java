package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CollectionUtilities {
	public static final String REMOVED_TAG = "removed";
	public static final String ADDED_TAG = "added";

	/**
	 * Gets the changes made to the a list of items
	 * @return HashMap the a list of items removed from the original list
	 * 			and a list of items added to the original list
	 * */
	public static <T> HashMap<String, HashSet<T>> getChangesToList(List<T> original, List<T> edited){
		HashMap<String, HashSet<T>> changeSet = new HashMap<String, HashSet<T>>();
		HashSet<T> removed = new HashSet<T>(original);
		HashSet<T> added = new HashSet<T>(edited);
		removed.removeAll(edited);
		added.removeAll(original);
		
		changeSet.put(REMOVED_TAG, removed);
		changeSet.put(ADDED_TAG, added);
		
		return changeSet;
	}
}
