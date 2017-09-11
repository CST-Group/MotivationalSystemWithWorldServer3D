package br.unicamp.mtwsapp.codelets.goal;

import br.unicamp.cst.motivational.Goal;
import br.unicamp.cst.motivational.GoalCodelet;
import br.unicamp.cst.representation.owrl.AbstractObject;
import br.unicamp.cst.representation.owrl.Property;
import br.unicamp.cst.representation.owrl.QualityDimension;
import br.unicamp.mtwsapp.support.NetworkSettings;

import java.util.Arrays;
import java.util.List;

/**
 * Created by du on 13/06/17.
 */
public class GoalGeneratorCodelet extends GoalCodelet{
    public GoalGeneratorCodelet(String id) {
        super(id);
    }

    @Override
    public Goal goalGeneration(AbstractObject hypotheticalSituation) {

        if(hypotheticalSituation != null) {
            AbstractObject goalAO = new AbstractObject("GOAL");
            goalAO.addProperty(new Property("GOALNAME", Arrays.asList(new QualityDimension("VALUE", "PickUpRemainingJewels"))));
            goalAO.addProperty(hypotheticalSituation.getProperties().stream().filter(property -> property.getName().equals("DIFFJEWELS")).findFirst().get());
            return new Goal("PickUpRemainingJewels", goalAO);
        } else{
            return null;
        }

    }
}
