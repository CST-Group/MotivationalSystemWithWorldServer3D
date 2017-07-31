package br.unicamp.mtwsapp.codelets.appraisal;

import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.motivational.Appraisal;
import br.unicamp.cst.motivational.AppraisalCodelet;
import br.unicamp.cst.representation.owrl.AbstractObject;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import com.fuzzylite.Engine;
import com.fuzzylite.norm.TNorm;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Trapezoid;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;


/**
 * Created by du on 08/06/17.
 */
public class CurrentAppraisalCodelet extends AppraisalCodelet {

    public static final String STATE_NORMAL = "STATE_NORMAL";
    public static final String STATE_GOOD = "STATE_GOOD";
    public static final String STATE_BAD = "STATE_BAD";

    public static final String HUNGRY = "HUNGRY";
    public static final String NORMAL = "NORMAL";
    public static final String SATISFIED = "SATISFIED";

    public static final String AMBITIOUS = "AMBITIOUS";


    private Engine engine;
    private InputVariable energyIV;
    private InputVariable leafletIV;
    private OutputVariable outputOV;
    private RuleBlock ruleBlock;

    public CurrentAppraisalCodelet(String id) {
        super(id);

        setEngine(new Engine(id));
        buildEnergyInput();
        buildLeafletInput();
        buildOutput();
        buildRules();

        getEngine().configure("AlgebraicProduct", "AlgebraicSum", "AlgebraicProduct", "AlgebraicSum" , "Centroid");
    }

    @Override
    public Appraisal appraisalGeneration(AbstractObject abstractObject) {

        double evaluation = 0d;
        String state = "";

        CreatureInnerSense cis = (CreatureInnerSense) ((MemoryObject) abstractObject.getProperties().get(0).getQualityDimensions().stream()
                .filter(q -> q.getName().equals("cis")).findFirst().get().getValue()).getI();

        double leafletCompleteRate = cis.getLeafletCompleteRate();

        double fuel = cis.getFuel();

        /*evaluation = 0.6*(fuel/1000) + 0.4*(leafletCompleteRate/100);


        if(evaluation > 0.6d) {
            state = STATE_GOOD;
        }
        else if(evaluation < 0.4d)
            state = STATE_BAD;
        else
            state = STATE_NORMAL;*/

        getEnergyIV().setInputValue(fuel);
        getLeafletIV().setInputValue(leafletCompleteRate);
        getEngine().process();

        state = getOutputOV().highestMembershipTerm(getOutputOV().getOutputValue()).getName();
        evaluation = getOutputOV().getOutputValue();



        Appraisal appraisal = new Appraisal(getId(), state, evaluation);

        return appraisal;
    }


    public void buildEnergyInput(){
        setEnergyIV(new InputVariable("Energy"));
        getEnergyIV().setEnabled(true);
        getEnergyIV().setRange(0.000, 1000.000);
        getEnergyIV().addTerm(new Trapezoid(HUNGRY, 0, 0, 200, 400));
        getEnergyIV().addTerm(new Trapezoid(NORMAL, 200, 400, 600, 800));
        getEnergyIV().addTerm(new Trapezoid(SATISFIED, 600, 800, 1000, 1000));
        getEngine().addInputVariable(getEnergyIV());
        
    }

    public void buildLeafletInput(){
        setLeafletIV(new InputVariable("Leaflet"));
        getLeafletIV().setEnabled(true);
        getLeafletIV().setRange(0.000, 100.000);
        getLeafletIV().addTerm(new Trapezoid(AMBITIOUS, 0, 0, 20, 40));
        getLeafletIV().addTerm(new Trapezoid(NORMAL, 20, 40, 60, 80));
        getLeafletIV().addTerm(new Trapezoid(SATISFIED, 60, 80, 100, 100));
        getEngine().addInputVariable(getLeafletIV());
    }


    public void buildOutput(){
        setOutputOV(new OutputVariable());
        getOutputOV().setName("State");
        getOutputOV().setRange(0.000, 1.000);
        getOutputOV().setDefaultValue(Double.NaN);
        getOutputOV().addTerm(new Trapezoid(STATE_BAD, 0, 0, 0.4, 0.5));
        getOutputOV().addTerm(new Trapezoid(STATE_NORMAL, 0.4, 0.5, 0.7, 0.8));
        getOutputOV().addTerm(new Trapezoid(STATE_GOOD,  0.7, 0.8, 1.0, 1.0));
        getEngine().addOutputVariable(getOutputOV());
    }


    public void buildRules(){
        setRuleBlock(new RuleBlock());

        /*getRuleBlock().addRule(Rule.parse("if Energy is "+HUNGRY+" and Leaflet is "+AMBITIOUS+" then State is "+STATE_BAD, getEngine()));
        getRuleBlock().addRule(Rule.parse("if Energy is "+HUNGRY+" and Leaflet is "+NORMAL+" then State is "+STATE_BAD, getEngine()));
        getRuleBlock().addRule(Rule.parse("if Energy is "+HUNGRY+" and Leaflet is "+SATISFIED+" then State is "+STATE_GOOD, getEngine()));

        getRuleBlock().addRule(Rule.parse("if Energy is "+NORMAL+" and Leaflet is "+AMBITIOUS+" then State is "+STATE_GOOD, getEngine()));
        getRuleBlock().addRule(Rule.parse("if Energy is "+NORMAL+" and Leaflet is "+NORMAL+" then State is "+STATE_NORMAL, getEngine()));
        getRuleBlock().addRule(Rule.parse("if Energy is "+NORMAL+" and Leaflet is "+SATISFIED+" then State is "+STATE_NORMAL, getEngine()));

        getRuleBlock().addRule(Rule.parse("if Energy is "+SATISFIED+" and Leaflet is "+AMBITIOUS+" then State is "+STATE_GOOD, getEngine()));
        getRuleBlock().addRule(Rule.parse("if Energy is "+SATISFIED+" and Leaflet is "+NORMAL+" then State is "+STATE_GOOD, getEngine()));
        getRuleBlock().addRule(Rule.parse("if Energy is "+SATISFIED+" and Leaflet is "+SATISFIED+" then State is "+STATE_NORMAL, getEngine()));*/

        getRuleBlock().addRule(Rule.parse("if Energy is "+HUNGRY+" then State is "+STATE_BAD, getEngine()));

        getRuleBlock().addRule(Rule.parse("if Energy is "+NORMAL+" and Leaflet is "+AMBITIOUS+" then State is "+STATE_GOOD, getEngine()));
        getRuleBlock().addRule(Rule.parse("if Energy is "+NORMAL+" and Leaflet is "+NORMAL+" then State is "+STATE_NORMAL, getEngine()));
        getRuleBlock().addRule(Rule.parse("if Energy is "+NORMAL+" and Leaflet is "+SATISFIED+" then State is "+STATE_NORMAL, getEngine()));

        getRuleBlock().addRule(Rule.parse("if Energy is "+SATISFIED+" then State is "+STATE_GOOD, getEngine()));
        getRuleBlock().addRule(Rule.parse("if Energy is "+SATISFIED+" and Leaflet is "+SATISFIED+" then State is "+STATE_NORMAL, getEngine()));

        getEngine().addRuleBlock(getRuleBlock());

    }


    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public InputVariable getEnergyIV() {
        return energyIV;
    }

    public void setEnergyIV(InputVariable energyIV) {
        this.energyIV = energyIV;
    }

    public InputVariable getLeafletIV() {
        return leafletIV;
    }

    public void setLeafletIV(InputVariable leafletIV) {
        this.leafletIV = leafletIV;
    }

    public OutputVariable getOutputOV() {
        return outputOV;
    }

    public void setOutputOV(OutputVariable outputOV) {
        this.outputOV = outputOV;
    }

    public RuleBlock getRuleBlock() {
        return ruleBlock;
    }

    public void setRuleBlock(RuleBlock ruleBlock) {
        this.ruleBlock = ruleBlock;
    }
}
