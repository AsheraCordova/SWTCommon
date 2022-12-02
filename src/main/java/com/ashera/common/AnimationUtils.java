package com.ashera.common;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quad;

public class AnimationUtils {
	public static final int Animating = 10000; 
	private boolean isAnimationRunning = true;
	static {
		Tween.registerAccessor(Control.class, new CompositeAccessor());
	}

	public void animate(Control panel, int x, int y, int animationDurationInMs) {
//		System.out.println(animationDurationInMs/1000f);
		aurelienribon.tweenengine.TweenManager t = new aurelienribon.tweenengine.TweenManager();
		Tween.to(panel, CompositeAccessor.POS_X, animationDurationInMs/1000f).target(x, y).ease(Quad.INOUT).start(t);
		new Thread(new Runnable() {
			private long lastMillis = -1;

			@Override
			public void run() {
				while (isAnimationRunning) {
					if (lastMillis > 0) {
						long currentMillis = System.currentTimeMillis();
						final float delta = (currentMillis - lastMillis) / 1000f;

						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								t.update(delta);
							}
						});

						lastMillis = currentMillis;
					} else {
						lastMillis = System.currentTimeMillis();
					}

					try {
						Thread.sleep(1000 / 30);
					} catch (InterruptedException ex) {
					}
				}
			}
		}).start();
	}
}
