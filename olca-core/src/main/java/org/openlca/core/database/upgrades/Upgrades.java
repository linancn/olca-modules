package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for checking the version of a database and upgrading
 * databases.
 */
public class Upgrades {

	private final IUpgrade[] upgrades = {};
	private Logger log = LoggerFactory.getLogger(Upgrades.class);

	private Upgrades() {
	}

	public static VersionState checkVersion(IDatabase database) {
		int version = database.getVersion();
		if (version < 1)
			return VersionState.ERROR;
		if (version == IDatabase.CURRENT_VERSION)
			return VersionState.CURRENT;
		if (version < IDatabase.CURRENT_VERSION)
			return VersionState.OLDER;
		else
			return VersionState.NEWER;
	}

	public static void runUpgrades(IDatabase database) throws Exception {
		Upgrades upgrades = new Upgrades();
		upgrades.run(database);
	}

	private void run(IDatabase database) throws Exception {
		IUpgrade nextUpgrade = null;
		while ((nextUpgrade = findNextUpgrade(database)) != null) {
			nextUpgrade.init(database);
			nextUpgrade.run();
		}
		log.trace("no more upgrades");
	}

	private IUpgrade findNextUpgrade(IDatabase database) {
		int version = database.getVersion();
		for (IUpgrade upgrade : upgrades) {
			if (upgrade.getInitialVersion() == version)
				return upgrade;
		}
		return null;
	}
}
