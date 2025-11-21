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

import org.eclipse.swt.graphics.Resource;

public class DisposeUtil {
    public static void disposeAll(Object...controls) {
    	if (controls != null) {
	        for (Object control : controls) {
	        	if (control instanceof Resource) {
	        		Resource resource = (Resource) control;
		            disposeResource(resource);
	        	}
	        	
	        	if (control instanceof r.android.animation.Animator) {
	        		((r.android.animation.Animator) control).end();
	        	}
	        }
    	}
    }

	private static void disposeResource(Resource resource) {
		if (resource != null && !resource.isDisposed()) {
		    resource.dispose();
		}
	}
}
