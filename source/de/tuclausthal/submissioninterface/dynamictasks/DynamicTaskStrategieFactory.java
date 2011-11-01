/*
 * Copyright 2011 Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of the SubmissionInterface.
 * 
 * SubmissionInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * 
 * SubmissionInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SubmissionInterface. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.dynamictasks;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.dynamictasks.impl.Bin2DecDynamicTaskStrategie;
import de.tuclausthal.submissioninterface.dynamictasks.impl.Dec2BinDynamicTaskStrategie;
import de.tuclausthal.submissioninterface.dynamictasks.impl.HardDiskCalculationDynamicTaskStrategie;
import de.tuclausthal.submissioninterface.dynamictasks.impl.HexFloatMultiplikationDynamkicTaskStrategie;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

/**
 * @author Sven Strickroth
 */
public class DynamicTaskStrategieFactory {
	public static final String[] STRATEGIES = { "dec2bin", "bin2dec", "hexfloat2hexfloatandbin", "harddiskfillstatecalculation" };
	public static final String[] NAMES = { "Integer-Dezimal2Binär (1 Parameter)", "Integer-Binär2Decimal (1 Parameter)", "32-bit HEX-Float2Hex- und Binär-Float (2 Parameter)", "Hard-Disk Space Calculator (4 Parameter)" };

	public static boolean IsValidStrategieName(String dynamicTask) {
		for (String strategie : STRATEGIES) {
			if (strategie.equals(dynamicTask)) {
				return true;
			}
		}
		return false;
	}

	public static DynamicTaskStrategieIf createDynamicTaskStrategie(Session session, String dynamicTask, Task task) {
		if ("dec2bin".equals(dynamicTask)) {
			return new Dec2BinDynamicTaskStrategie(session, task);
		} else if ("bin2dec".equals(dynamicTask)) {
			return new Bin2DecDynamicTaskStrategie(session, task);
		} else if ("hexfloat2hexfloatandbin".equals(dynamicTask)) {
			return new HexFloatMultiplikationDynamkicTaskStrategie(session, task);
		} else if ("harddiskfillstatecalculation".equals(dynamicTask)) {
			return new HardDiskCalculationDynamicTaskStrategie(session, task);
		}
		return null;
	}
}
