package br.unicamp.mtwsapp.codelets.mood;

import br.unicamp.cst.motivational.Appraisal;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.Mood;
import br.unicamp.cst.motivational.MoodCodelet;
import br.unicamp.mtwsapp.codelets.appraisal.CurrentAppraisal;


import java.util.List;

/**
 * Created by du on 14/06/17.
 */
public class HungerMoodCodelet extends MoodCodelet {

    public static final String STATE_HUNGER = "STATE_HUNGER";
    public static final String STATE_NORMAL = "STATE_NORMAL";
    public static final String STATE_SATISFIED = "STATE_SATISFIED";

    public HungerMoodCodelet(String id) {
        super(id);
    }

    @Override
    public Mood moodGeneration(List<Drive> listOfDrives, Appraisal appraisal, List<Object> sensors) {
        double moodValue;
        String state;

        Drive hungerDrive = listOfDrives.get(0);

        if(appraisal.getCurrentStateEvaluation().equals(CurrentAppraisal.STATE_BAD)){
            moodValue = appraisal.getEvaluation();
            state = STATE_HUNGER;
        }
        else if(appraisal.getCurrentStateEvaluation().equals(CurrentAppraisal.STATE_GOOD))
        {
            moodValue = -appraisal.getEvaluation();
            state = STATE_SATISFIED;

        } else{
            moodValue = 0d;
            state = STATE_NORMAL;
        }

        return new Mood(getId(), state, moodValue);
    }
}