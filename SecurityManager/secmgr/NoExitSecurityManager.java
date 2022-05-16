/*
 * Copyright 2012 Sven Strickroth <email@cs-ware.de>
 * based on an idea of Timo Rienäcker <timo@rienaecker.info> 
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
 */

package secmgr;

/**
 * Enhanced Security-Manager which also checks exits of the Java VM
 *
 * In the default Security-Manager the normal exitVM permission
 * gets added to all applications in the classpath
 * (http://docs.oracle.com/javase/6/docs/api/java/lang/RuntimePermission.html).
 *   
 * @author Sven Strickroth
 */
public class NoExitSecurityManager extends SecurityManager {
	public void checkExit(int status) {
		super.checkPermission(new RuntimePermission("exitTheVM." + status));
	}
}
