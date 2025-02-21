/*****************************************************************************
 * Copyright 2007-2015 DCA-FEEC-UNICAMP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *    Klaus Raizer, Andre Paraense, Ricardo Ribeiro Gudwin
 *****************************************************************************/

package br.unicamp.mtwsapp.support;

import br.unicamp.cst.bindings.soar.Plan;
import br.unicamp.cst.core.entities.MemoryObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import br.unicamp.cst.motivational.*;
import br.unicamp.mtwsapp.application.AgentMind;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import com.google.gson.Gson;
import ws3dproxy.CommandExecException;
import ws3dproxy.model.Creature;
import ws3dproxy.model.World;

/**
 * @author rgudwin
 */
public class SimulationController {

    private Timer t;
    private List<MemoryObject> mol = new ArrayList<>();

    private MemoryObject plansSetMO;
    private MemoryObject planSelectedMO;

    private Random r;

    private Creature creature;
    private AgentMind agentMind;
    private MemoryObject creatureInnerSenseMO;
    private Date initDate;

    private int defaultTime = 10;
    private double time = 0;
    private int counterToGenerateThings = 0;

    private File fileEnergySpent;
    private File fileCreatureScore;
    private File fileDrivesActivation;
    private File filePlansCreated;
    private File filePlansExecuted;
    private File fileMoodsValues;
    private File fileEmotionalActivation;
    private File fileAppraisalEvals;
    private File fileSystem;

    private List<Result> resultEnergySpent;
    private List<Result> resultDrivesActivation;
    private List<Result> resultCreatureScore;
    private List<Result> resultPlansCreated;
    private List<Result> resultPlansExecuted;
    private List<Result> resultMoodsValues;
    private List<Result> resultEmotionalActivation;
    private List<Result> resultAppraisalEvals;
    private List<Result> resultSystem1VSSystem2;


    public SimulationController(String name) {
        String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        setResultEnergySpent(new ArrayList<>());
        setResultDrivesActivation(new ArrayList<>());
        setResultCreatureScore(new ArrayList<>());
        setResultPlansCreated(new ArrayList<>());
        setResultPlansExecuted(new ArrayList<>());
        setResultAppraisalEvals(new ArrayList<>());
        setResultMoodsValues(new ArrayList<>());
        setResultEmotionalActivation(new ArrayList<>());
        setResultSystem1VSSystem2(new ArrayList<>());
        setR(new Random());
        setFileEnergySpent(new File("reportFiles/MotivationalSystem_EnergySpent" + timeLog + ".txt"));
        setFileCreatureScore(new File("reportFiles/MotivationalSystem_Score" + timeLog + ".txt"));
        setFileDrivesActivation(new File("reportFiles/MotivationalSystem_DrivesActivation" + timeLog + ".txt"));
        setFilePlansCreated(new File("reportFiles/MotivationalSystem_PlansCreated" + timeLog + ".txt"));
        setFilePlansExecuted(new File("reportFiles/MotivationalSystem_PlansExecuted" + timeLog + ".txt"));
        setFileAppraisalEvals(new File("reportFiles/MotivationalSystem_AppraisalEvals" + timeLog + ".txt"));
        setFileEmotionalActivation(new File("reportFiles/MotivationalSystem_EmotionalActivation" + timeLog + ".txt"));
        setFileMoodsValues(new File("reportFiles/MotivationalSystem_MoodsValues" + timeLog + ".txt"));
        setFileSystem(new File("reportFiles/System1vsSystem2_" + timeLog + ".txt"));
        setInitDate(new Date());

    }

    public void addMO(MemoryObject moi) {
        getMol().add(moi);
    }

    public void setMind(AgentMind agentMind) {
        this.setAgentMind(agentMind);
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public Creature getCreature() {
        return this.creature;
    }

    public void StartTimer() {
        setT(new Timer());
        MVTimerTask tt = new MVTimerTask(this);
        getT().scheduleAtFixedRate(tt, 0, 1000);
    }

    public void tick() {

        if ((getCounterToGenerateThings() / 60) >= 1) {
            createObjectsInWorld();
            createFoodsInWorld();
            setCounterToGenerateThings(0);
        }

        reportEnergySpent(getTime());
        reportDrivesActivation(getTime());
        reportCreatureScore(getTime());
        reportPlansCreated(getTime());
        reportPlansExecuted(getTime());
        reportAppraisalEvals(getTime());
        reportMoodsValue(getTime());
        reportEmotionalActivation(getTime());
        reportSystem1vsSystem2(getTime());

        if ((getTime() / 60) == getDefaultTime()) {
            finalizeReport("Creature's Energy", "Time", "Energy", getResultEnergySpent(), getFileEnergySpent());
            finalizeReport("Creature's Score", "Time", "Score", getResultCreatureScore(), getFileCreatureScore());
            finalizeReport("Creature's Drives", "Time", "Activation", getResultDrivesActivation(), getFileDrivesActivation());
            finalizeReport("Creature's Plans Created", "Time", "Number Of Plans", getResultPlansCreated(), getFilePlansCreated());
            finalizeReport("Creature's Plans Executed", "Time", "Number Of Plans", getResultPlansExecuted(), getFilePlansExecuted());
            finalizeReport("Creature's Appraisal Evaluation", "Time", "Evaluation", getResultAppraisalEvals(), getFileAppraisalEvals());
            finalizeReport("Creature's Emotional Distortion", "Time", "Activation", getResultEmotionalActivation(), getFileEmotionalActivation());
            finalizeReport("Creature's Moods Values", "Time", "Values", getResultMoodsValues(), getFileMoodsValues());
            finalizeReport("System 1 vs System 2", "Time", "Values", getResultSystem1VSSystem2(), getFileSystem());
            getT().cancel();
            getT().purge();
            getAgentMind().shutDown();
            System.exit(1);
        }

        setCounterToGenerateThings(getCounterToGenerateThings() + 1);
        setTime(getTime() + 1);
    }



    private void createObjectsInWorld() {
        try {
            Random random = new Random();
            World.createJewel(random.nextInt(6), getR().nextInt(800), getR().nextInt(600));
            World.createJewel(random.nextInt(6), getR().nextInt(800), getR().nextInt(600));
            World.createJewel(random.nextInt(6), getR().nextInt(800), getR().nextInt(600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createFoodsInWorld() {
        try {
            World.createFood(0, getR().nextInt(800), getR().nextInt(600));
        } catch (CommandExecException e) {
            e.printStackTrace();
        }

    }

    private boolean checkTimeStop() {

        boolean bFinish = false;

        if ((double) ((new Date()).getTime() - getInitDate().getTime()) >= this.getDefaultTime() * 60 * Math.pow(10, 3)) {
            bFinish = true;
        }

        return bFinish;
    }

    private void reportSystem1vsSystem2(double time) {

        this.getResultSystem1VSSystem2().add(new Result("High-Level Motivational Subsystem", time, getPlanSelectedMO().getI() != null ? 1 : 0));
        this.getResultSystem1VSSystem2().add(new Result("Subsumption Motivational Subsystem", time, getPlanSelectedMO().getI() != null ? 0 : 1));

    }

    private void reportEmotionalActivation(double time) {
        List<MemoryObject> drivesMO = getMol().stream().filter(d -> d.getName() == MotivationalCodelet.OUTPUT_DRIVE_MEMORY).collect(Collectors.toList());
        drivesMO.stream().forEach(driveMO -> {
            if (driveMO.getI() != null) {
                this.getResultEmotionalActivation().add(new Result(((Drive) driveMO.getI()).getName(), time, ((Drive) driveMO.getI()).getEmotionalDistortion()));
            }
        });
    }

    private void reportMoodsValue(double time) {
        List<MemoryObject> moodsMO = getMol().stream().filter(d -> d.getName() == MoodCodelet.OUTPUT_MOOD_MEMORY).collect(Collectors.toList());
        moodsMO.stream().forEach(moodMO -> {
            if (moodMO.getI() != null) {
                this.getResultMoodsValues().add(new Result(((Mood) moodMO.getI()).getName(), time, ((Mood) moodMO.getI()).getValue()));
            }
        });
    }

    private void reportAppraisalEvals(double time) {
        List<MemoryObject> appraisalsMO = getMol().stream().filter(d -> d.getName() == AppraisalCodelet.OUTPUT_APPRAISAL_MEMORY).collect(Collectors.toList());
        appraisalsMO.stream().forEach(appraisalMO -> {
            if (appraisalMO.getI() != null) {
                this.getResultAppraisalEvals().add(new Result("Appraisal Evaluation", time, ((Appraisal) appraisalMO.getI()).getEvaluation()));
            }
        });
    }

    private void reportEnergySpent(double time) {
        this.getResultEnergySpent().add(new Result("Energy Spent", time, getCreature().getFuel()));
    }

    private void reportDrivesActivation(double time) {
        List<MemoryObject> drivesMO = getMol().stream().filter(d -> d.getName() == MotivationalCodelet.OUTPUT_DRIVE_MEMORY).collect(Collectors.toList());
        drivesMO.stream().forEach(driveMO -> {
            if (driveMO.getI() != null) {
                this.getResultDrivesActivation().add(new Result(((Drive) (driveMO.getI())).getName(), time, driveMO.getEvaluation()));
            }
        });
    }

    private void reportPlansCreated(double time) {

        HashMap<Integer, Plan> plans = (HashMap<Integer, Plan>) plansSetMO.getI();

        getResultPlansCreated().add(new Result("Plans Created", time, plans.size()));
    }

    private void reportPlansExecuted(double time) {

        HashMap<Integer, Plan> plans = (HashMap<Integer, Plan>) plansSetMO.getI();

        List<Plan> plansExecuted = plans.entrySet().stream().filter(x -> x.getValue().isFinished() == true).map(Map.Entry::getValue)
                .collect(Collectors.toList());

        getResultPlansExecuted().add(new Result("Plans Executed", time, plansExecuted.size()));
    }

    private void reportCreatureScore(double time) {
        this.getResultCreatureScore().add(new Result("Score Obtained", time, ((CreatureInnerSense) getCreatureInnerSenseMO().getI()).getScore()));
    }

    private void finalizeReport(String graphName, String xTitle, String yTitle, List<Result> results, File file) {
        Gson gson = new Gson();
        String sResults = gson.toJson(new Graph(graphName, xTitle, yTitle, results));
        writeInFile(sResults, file);
    }

    private void writeInFile(String line, File file) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(line);
            System.out.println(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public List<Result> getResultEnergySpent() {
        return resultEnergySpent;
    }

    public void setResultEnergySpent(List<Result> resultEnergySpent) {
        this.resultEnergySpent = resultEnergySpent;
    }

    public List<Result> getResultDrivesActivation() {
        return resultDrivesActivation;
    }

    public void setResultDrivesActivation(List<Result> resultDrivesActivation) {
        this.resultDrivesActivation = resultDrivesActivation;
    }

    public List<Result> getResultCreatureScore() {
        return resultCreatureScore;
    }

    public void setResultCreatureScore(List<Result> resultCreatureScore) {
        this.resultCreatureScore = resultCreatureScore;
    }

    public Timer getT() {
        return t;
    }

    public void setT(Timer t) {
        this.t = t;
    }

    public List<MemoryObject> getMol() {
        return mol;
    }

    public void setMol(List<MemoryObject> mol) {
        this.mol = mol;
    }

    public Random getR() {
        return r;
    }

    public void setR(Random r) {
        this.r = r;
    }

    public AgentMind getAgentMind() {
        return agentMind;
    }

    public void setAgentMind(AgentMind agentMind) {
        this.agentMind = agentMind;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public int getDefaultTime() {
        return defaultTime;
    }

    public void setDefaultTime(int defaultTime) {
        this.defaultTime = defaultTime;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getCounterToGenerateThings() {
        return counterToGenerateThings;
    }

    public void setCounterToGenerateThings(int counterToGenerateThings) {
        this.counterToGenerateThings = counterToGenerateThings;
    }

    public File getFileEnergySpent() {
        return fileEnergySpent;
    }

    public void setFileEnergySpent(File fileEnergySpent) {
        this.fileEnergySpent = fileEnergySpent;
    }

    public File getFileCreatureScore() {
        return fileCreatureScore;
    }

    public void setFileCreatureScore(File fileCreatureScore) {
        this.fileCreatureScore = fileCreatureScore;
    }

    public File getFileDrivesActivation() {
        return fileDrivesActivation;
    }

    public void setFileDrivesActivation(File fileDrivesActivation) {
        this.fileDrivesActivation = fileDrivesActivation;
    }

    public MemoryObject getCreatureInnerSenseMO() {
        return creatureInnerSenseMO;
    }

    public void setCreatureInnerSenseMO(MemoryObject creatureInnerSenseMO) {
        this.creatureInnerSenseMO = creatureInnerSenseMO;
    }

    public File getFilePlansCreated() {
        return filePlansCreated;
    }

    public void setFilePlansCreated(File filePlansCreated) {
        this.filePlansCreated = filePlansCreated;
    }

    public File getFilePlansExecuted() {
        return filePlansExecuted;
    }

    public void setFilePlansExecuted(File filePlansExecuted) {
        this.filePlansExecuted = filePlansExecuted;
    }

    public List<Result> getResultPlansCreated() {
        return resultPlansCreated;
    }

    public void setResultPlansCreated(List<Result> resultPlansCreated) {
        this.resultPlansCreated = resultPlansCreated;
    }

    public List<Result> getResultPlansExecuted() {
        return resultPlansExecuted;
    }

    public void setResultPlansExecuted(List<Result> resultPlansExecuted) {
        this.resultPlansExecuted = resultPlansExecuted;
    }

    public MemoryObject getPlansSetMO() {
        return plansSetMO;
    }

    public void setPlansSetMO(MemoryObject plansSetMO) {
        this.plansSetMO = plansSetMO;
    }

    public File getFileMoodsValues() {
        return fileMoodsValues;
    }

    public void setFileMoodsValues(File fileMoodsValues) {
        this.fileMoodsValues = fileMoodsValues;
    }

    public File getFileEmotionalActivation() {
        return fileEmotionalActivation;
    }

    public void setFileEmotionalActivation(File fileEmotionalActivation) {
        this.fileEmotionalActivation = fileEmotionalActivation;
    }

    public File getFileAppraisalEvals() {
        return fileAppraisalEvals;
    }

    public void setFileAppraisalEvals(File fileAppraisalEvals) {
        this.fileAppraisalEvals = fileAppraisalEvals;
    }

    public List<Result> getResultMoodsValues() {
        return resultMoodsValues;
    }

    public void setResultMoodsValues(List<Result> resultMoodsValues) {
        this.resultMoodsValues = resultMoodsValues;
    }

    public List<Result> getResultEmotionalActivation() {
        return resultEmotionalActivation;
    }

    public void setResultEmotionalActivation(List<Result> resultEmotionalActivation) {
        this.resultEmotionalActivation = resultEmotionalActivation;
    }

    public List<Result> getResultAppraisalEvals() {
        return resultAppraisalEvals;
    }

    public void setResultAppraisalEvals(List<Result> resultAppraisalEvals) {
        this.resultAppraisalEvals = resultAppraisalEvals;
    }

    public File getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(File fileSystem) {
        this.fileSystem = fileSystem;
    }

    public List<Result> getResultSystem1VSSystem2() {
        return resultSystem1VSSystem2;
    }

    public void setResultSystem1VSSystem2(List<Result> resultSystem1VSSystem2) {
        this.resultSystem1VSSystem2 = resultSystem1VSSystem2;
    }

    public MemoryObject getPlanSelectedMO() {
        return planSelectedMO;
    }

    public void setPlanSelectedMO(MemoryObject planSelectedMO) {
        this.planSelectedMO = planSelectedMO;
    }
}
