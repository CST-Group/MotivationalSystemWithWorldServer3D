package br.unicamp.mtwsapp.support;

import java.util.TimerTask;

/**
 * Created by du on 25/07/17.
 */
public class MVTimerTask extends TimerTask {
    SimulationController mv;
    boolean enabled = true;

    public MVTimerTask(SimulationController mvi) {
        mv = mvi;
    }

    public void run() {
        if (enabled) mv.tick();
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }
}
