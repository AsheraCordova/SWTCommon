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
