package br.unicamp.mtwsapp.codelets.emotional;

import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.EmotionalCodelet;
import br.unicamp.cst.motivational.Mood;

import java.util.List;

/**
 * Created by du on 11/06/17.
 */
public class HungerEmotionalCodelet extends EmotionalCodelet {
    public HungerEmotionalCodelet(String id) throws CodeletActivationBoundsException {
        super(id);
    }

    @Override
    public synchronized double calculateEmotionalDistortion(List<Drive> listOfDrives, Mood mood) {
        return distortionFunction(mood.getValue());
    }


    public synchronized double distortionFunction(double value) {
        //return -Math.tanh(4*value - 2);
        return Math.tanh(5d / 2d * value);
    }
}
