package basicPackage;

import captureFlagPackage.ClassID;

public interface IObserver {
	/**
	 * Must implement update following the observer pattern.
	 * @param x The integer should represent the id of the particular object that is calling update.
	 */
	public void update(ClassID x);
}
