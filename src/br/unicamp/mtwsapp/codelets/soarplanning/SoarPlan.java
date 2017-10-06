package br.unicamp.mtwsapp.codelets.soarplanning;

import java.util.ArrayList;
import java.util.List;

public class SoarPlan {

    public boolean isFinished = false;

    public List<SoarJewel> soarJewels;

    public SoarPlan(){
        setSoarJewels(new ArrayList<>());
    }

    public List<SoarJewel> getSoarJewels() {
        return soarJewels;
    }

    public void setSoarJewels(List<SoarJewel> soarJewels) {
        this.soarJewels = soarJewels;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}
