/**
 * Copyright (C) 2011-2013 Michael Vogt <michu@neophob.com>
 *
 * This file is part of PixelController.
 *
 * PixelController is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PixelController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PixelController.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.neophob.sematrix.core.api.impl;

import java.util.logging.Logger;

/**
 * calculate framedelay / FPS
 * 
 * @author michu
 * 
 */
public class Framerate {

    private static final Logger LOG = Logger.getLogger(Framerate.class.getName());

    private static final float MINIMAL_FPS = 0.001f;

    private long delay;
    private long frameCount;

    private static int SAMPLE_COUNT = 500;
    private int tickindex = 0;
    private long ticksum = 0;
    private long[] ticklist;
    private long lastTime;
    private float fps;
    private float targetFps;

    public Framerate(float targetFps) {
        this.setFps(targetFps);
        this.frameCount = 1;
        ticklist = new long[SAMPLE_COUNT];
        lastTime = System.currentTimeMillis();
    }

    public void setFps(float targetFps) {
        this.targetFps = targetFps;
        if (this.targetFps < MINIMAL_FPS) {
            this.targetFps = MINIMAL_FPS;
        }

        this.delay = (long) (1000f / this.targetFps);
        LOG.info("Target fps: " + this.targetFps + ", delay: " + delay + "ms");
    }

    public float getFps() {
        return fps;
    }

    public long getFrameCount() {
        return frameCount;
    }

    public long getFrameDelay() {
        long newtick = System.currentTimeMillis() - lastTime;
        ticksum -= ticklist[tickindex];
        ticksum += newtick; /* add new value */
        ticklist[tickindex] = newtick;
        if (++tickindex == SAMPLE_COUNT) {
            tickindex = 0;
        }

        if (frameCount % 25 == 24) {
            float f = (float) ticksum / SAMPLE_COUNT;
            this.fps = 1000 / f;
        }

        if (frameCount % (3 * SAMPLE_COUNT) == (3 * SAMPLE_COUNT - 1)) {
            // dynamic adjust delay
            float fpsDelta = this.targetFps - this.fps;
            float correctionThreshold = this.targetFps * 0.1f;
            if (fpsDelta > correctionThreshold) {
                if (delay > 1) {
                    delay--;
                }
            } else if (fpsDelta < -correctionThreshold) {
                delay++;
            }
        }

        frameCount++;
        lastTime = System.currentTimeMillis();
        return delay;
    }
}