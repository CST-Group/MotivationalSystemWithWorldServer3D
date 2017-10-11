package br.unicamp.mtwsapp.codelets.soarplanning;

import br.unicamp.cst.bindings.soar.*;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

import java.util.*;

public class SoarPlanSelectionCodelet extends PlanSelectionCodelet {

    private MemoryObject nextActionMO;
    private MemoryObject selectedPlanMO;

    private Creature creature;

    public SoarPlanSelectionCodelet(String id, Creature creature) {
        super(id);
        this.setCreature(creature);
    }

    @Override
    public Plan selectPlanToExecute(HashMap<Integer, Plan> plans) {

        Plan plan = null;

        Optional<Map.Entry<Integer, Plan>> first = plans.entrySet().stream().filter(p -> p.getValue().isFinished() != true).findFirst();

        if (first.isPresent()) {
            Map.Entry<Integer, Plan> soarPlanEntry = first.get();
            Comparator<SoarJewel> comparator = new Comparator<SoarJewel>() {
                @Override
                public int compare(SoarJewel thing1, SoarJewel thing2) {
                    int nearThing = calculateDistanceToJewel(thing1, getCreature()) < calculateDistanceToJewel(thing2, getCreature()) ? 1 : 0;
                    return nearThing;
                }
            };

            Collections.sort(((SoarPlan) soarPlanEntry.getValue().getContent()).getSoarJewels(), comparator);

            plan = soarPlanEntry.getValue();
        }

        return plan;
    }

    @Override
    public boolean verifyIfPlanWasFinished(Plan plan) {

        int counter = 0;

        if (plan != null) {

            for (SoarJewel soarJewel : ((SoarPlan) plan.getContent()).getSoarJewels()) {


                CreatureInnerSense cis = (CreatureInnerSense)(getInputDataMC().getI(0));
                Optional<Thing> first = cis.getThingsInWorld().stream().filter(t -> t.getName().equals(soarJewel.getName())).findFirst();

                if (!first.isPresent())
                    counter++;
            }

            if (counter == 3) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }

    }

    @Override
    public boolean verifyExistPlan(Plan soarFinishGoal) {

        boolean isEquals = false;

        for (Map.Entry<Integer, Plan> plan : getPlansMap().entrySet()) {
            SoarPlan value = (SoarPlan) plan.getValue().getContent();

            int counterEqual = 0;

            for (SoarJewel soarJewel : ((SoarPlan) soarFinishGoal.getContent()).getSoarJewels()) {
                Optional<SoarJewel> first = value.getSoarJewels().stream().filter(sj -> sj.getName().equals(soarJewel.getName())).findFirst();

                if (first.isPresent()) {
                    counterEqual++;
                }
            }

            if (counterEqual == 3) {
                isEquals = true;
                break;
            }
        }

        return isEquals;
    }

    public double calculateDistanceToJewel(SoarJewel soarJewel, Creature creature) {

        double distance = Math.sqrt(Math.pow((creature.getPosition().getX() - soarJewel.getX1()), 2) +
                Math.pow((creature.getPosition().getY() - soarJewel.getY1()), 2));

        soarJewel.setDistance(distance);

        return distance;

    }

    public Creature getCreature() {
        return creature;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public MemoryObject getNextActionMO() {
        return nextActionMO;
    }

    public void setNextActionMO(MemoryObject nextActionMO) {
        this.nextActionMO = nextActionMO;
    }

    public MemoryObject getSelectedPlanMO() {
        return selectedPlanMO;
    }

    public void setSelectedPlanMO(MemoryObject selectedPlanMO) {
        this.selectedPlanMO = selectedPlanMO;
    }

}
