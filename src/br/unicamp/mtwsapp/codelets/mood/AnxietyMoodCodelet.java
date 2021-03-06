package br.unicamp.mtwsapp.codelets.mood;


import br.unicamp.cst.motivational.Appraisal;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.Mood;
import br.unicamp.cst.motivational.MoodCodelet;
import br.unicamp.mtwsapp.codelets.appraisal.CurrentAppraisalCodelet;


import java.util.List;

/**
 * Created by du on 14/06/17.
 */
public class AnxietyMoodCodelet extends MoodCodelet {

    public static final String STATE_ANXIETY = "STATE_ANXIETY";
    public static final String STATE_NORMAL = "STATE_NORMAL";
    public static final String STATE_SATISFIED = "STATE_SATISFIED";

    public AnxietyMoodCodelet(String id) {
        super(id);
    }

    @Override
    public synchronized Mood moodGeneration(List<Drive> listOfDrives, Appraisal appraisal, List<Object> sensors) {
        double moodValue;
        String state;

        if(appraisal != null) {
            if (appraisal.getCurrentStateEvaluation().equals(CurrentAppraisalCodelet.STATE_BAD)) {
                moodValue = appraisal.getEvaluation();
                state = STATE_ANXIETY;
            } else if (appraisal.getCurrentStateEvaluation().equals(CurrentAppraisalCodelet.STATE_GOOD)) {
                moodValue = -appraisal.getEvaluation();
                state = STATE_SATISFIED;

            } else {
                moodValue = 0d;
                state = STATE_NORMAL;
            }
        } else {
            moodValue = 0d;
            state = STATE_NORMAL;
        }

        return new Mood(getId(), state, moodValue);
    }
}
