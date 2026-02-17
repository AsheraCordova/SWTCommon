//start - license
/*
 * Copyright (c) 2025 Ashera Cordova
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
//end - license
package com.ashera.common;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ShellManager {
	private static final String TYPE = "type";
	private static final String CREATION_DATE = "creation_date";

	enum Type { Root, Dialog }
	private static ShellManager INSTANCE = new ShellManager();
	private Shell rootShell;

	private ShellManager() {
	}
	
	public static ShellManager getInstance() {
		return INSTANCE;
	}
	
	public Shell createRootShell(Display display, int style) {
		Shell shell = new Shell(display, style);
		shell.setData(TYPE, Type.Root);
		shell.setData(CREATION_DATE, System.currentTimeMillis());
		this.rootShell = shell;
		return shell;
	}

	public Shell getRootShell() {
		return rootShell;
	}
	
	public Shell createDialogShell(Shell parent, int style) {
		Shell shell = new Shell(parent, style);
		shell.setData(TYPE, Type.Dialog);
		shell.setData(CREATION_DATE, System.currentTimeMillis());
		return shell;
	}
	
	public Shell getActiveShell() {
		Shell activeShell = null;
		Long shellCreationDate = null;
		for (Shell shell : Display.getDefault().getShells()) {
			Long date = (Long) shell.getData(CREATION_DATE);
			
			if (date != null && shell.isVisible()) {
				if (activeShell == null || (shellCreationDate != null && date > shellCreationDate)) {
					activeShell = shell;
					shellCreationDate = date;
				}
			}
		}
		return activeShell;
	}
}
