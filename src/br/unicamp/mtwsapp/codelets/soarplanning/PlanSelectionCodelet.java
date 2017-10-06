package br.unicamp.mtwsapp.codelets.soarplanning;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;

import java.util.*;

public class PlanSelectionCodelet extends Codelet {

    private MemoryObject nextActionMO;
    private MemoryObject selectedPlanMO;
    private MemoryObject innerSenseMO;

    private HashMap<Integer, SoarPlan> plansMap;

    private Creature creature;

    private SoarPlan currentPlanInExecution;

    private int index;

    public static final String OUPUT_SELECTED_PLAN_MO = "OUPUT_SELECTED_PLAN_MO";

    public PlanSelectionCodelet(String id, Creature creature) {
        setName(id);

        setPlansMap(new HashMap<>());

        this.setCreature(creature);
        this.setIndex(0);
        this.setCurrentPlanInExecution(null);
    }

    @Override
    public void accessMemoryObjects() {


        if (getNextActionMO() == null) {
            setNextActionMO((MemoryObject) this.getInput(SoarPlanningCodelet.OUTPUT_COMMAND_MO));
        }

        if (getSelectedPlanMO() == null) {
            setSelectedPlanMO((MemoryObject) this.getOutput(OUPUT_SELECTED_PLAN_MO));
        }

        if (getInnerSenseMO() == null) {
            setInnerSenseMO((MemoryObject) this.getInput("INNER"));
        }

    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {

        if (getNextActionMO().getI() != null) {
            List<Object> nextAction = new ArrayList<>(Collections.synchronizedList((List<Object>) getNextActionMO().getI()));

            SoarPlan soarPlan = (SoarPlan) nextAction.get(0);

            if (getPlansMap().size() == 0) {
                getPlansMap().put(getIndex(), soarPlan);
                setIndex(getIndex() + 1);
            } else {
                if (!verifyExistPlan(soarPlan)) {
                    getPlansMap().put(getIndex(), soarPlan);
                    setIndex(getIndex() + 1);
                }
            }

        }

        if (getCurrentPlanInExecution() != null) {
            if (verifyIfCurrentWasFinished()) {
                getCurrentPlanInExecution().setFinished(true);
                setCurrentPlanInExecution(selectPlanToExecute());
            }
        } else {
            if (getPlansMap().size() > 0) {
                setCurrentPlanInExecution(selectPlanToExecute());
            }
        }

        getSelectedPlanMO().setI(getCurrentPlanInExecution());
    }

    private SoarPlan selectPlanToExecute() {

        SoarPlan soarPlan = null;

        Optional<Map.Entry<Integer, SoarPlan>> first = getPlansMap().entrySet().stream().filter(p -> p.getValue().isFinished() != true).findFirst();

        if (first.isPresent()) {
            Map.Entry<Integer, SoarPlan> soarPlanEntry = first.get();
            Comparator<SoarJewel> comparator = new Comparator<SoarJewel>() {
                @Override
                public int compare(SoarJewel thing1, SoarJewel thing2) {
                    int nearThing = calculateDistanceToJewel(thing1, getCreature()) < calculateDistanceToJewel(thing2, getCreature()) ? 1 : 0;
                    return nearThing;
                }
            };

            Collections.sort(soarPlanEntry.getValue().getSoarJewels(), comparator);

            soarPlan = soarPlanEntry.getValue();
        }

        return soarPlan;

    }

    private boolean verifyIfCurrentWasFinished() {

        int counter = 0;


        if(getCurrentPlanInExecution() != null) {

            for (SoarJewel soarJewel : getCurrentPlanInExecution().getSoarJewels()) {
                CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();
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


    private boolean verifyExistPlan(SoarPlan soarFinishGoal) {

        boolean isEquals = false;

        for (Map.Entry<Integer, SoarPlan> plan : getPlansMap().entrySet()) {
            SoarPlan value = plan.getValue();

            int counterEqual = 0;

            for (SoarJewel soarJewel : soarFinishGoal.getSoarJewels()) {
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

    public HashMap<Integer, SoarPlan> getPlansMap() {
        return plansMap;
    }

    public void setPlansMap(HashMap<Integer, SoarPlan> plansMap) {
        this.plansMap = plansMap;
    }

    public SoarPlan getCurrentPlanInExecution() {
        return currentPlanInExecution;
    }

    public void setCurrentPlanInExecution(SoarPlan currentPlanInExecution) {
        this.currentPlanInExecution = currentPlanInExecution;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public MemoryObject getInnerSenseMO() {
        return innerSenseMO;
    }

    public void setInnerSenseMO(MemoryObject innerSenseMO) {
        this.innerSenseMO = innerSenseMO;
    }
}
