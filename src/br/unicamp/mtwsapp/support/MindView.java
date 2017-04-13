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

import br.unicamp.cst.core.entities.MemoryObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.mtwsapp.application.AgentMind;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import com.google.gson.Gson;
import ws3dproxy.model.Creature;
import ws3dproxy.model.World;

class MVTimerTask extends TimerTask {
    MindView mv;
    boolean enabled = true;

    public MVTimerTask(MindView mvi) {
        mv = mvi;
    }

    public void run() {
        if (enabled) mv.tick();
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }
}

class Graph{

    public Graph(String title, String xTitle, String yTitle, List<Result> results){
        this.title = title;
        this.results = results;
        this.xTitle = xTitle;
        this.yTitle = yTitle;
    }

    public String title;
    public String xTitle;
    public String yTitle;
    public List<Result> results;
}

class Result {

    public Result(String variableName, Object x, Object y) {
        this.variableName = variableName;
        this.x = x;
        this.y = y;
    }

    public String variableName;
    public Object x;
    public Object y;
}


/**
 * @author rgudwin
 */
public class MindView extends javax.swing.JFrame {

    private Timer t;
    private List<MemoryObject> mol = new ArrayList<>();
    private int j = 0;
    private Random r;

    private Creature creature;
    private AgentMind agentMind;
    private CreatureInnerSense creatureInnerSense;
    private Date initDate;

    private int defaultTime = 15;

    private File fileEnergySpent;
    private File fileCreatureScore;
    private File fileDrivesActivation;

    private List<Result> resultEnergySpent;
    private List<Result> resultDrivesActivation;
    private List<Result> resultCreatureScore;

    private Thread reportThread;


    /**
     * Creates new form NewJFrame
     */
    public MindView(String name) {
        String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

        this.setResultEnergySpent(new ArrayList<>());
        this.setResultDrivesActivation(new ArrayList<>());
        this.setResultCreatureScore(new ArrayList<>());
        this.r = new Random();
        this.fileEnergySpent = new File("reportFiles/MotivationalSystem_EnergySpent" + timeLog + ".txt");
        this.fileCreatureScore = new File("reportFiles/MotivationalSystem_FeafletComplete" + timeLog + ".txt");
        this.fileDrivesActivation = new File("reportFiles/MotivationalSystem_DrivesActivation" + timeLog + ".txt");

        initComponents();
        setTitle(name);
    }

    public void addMO(MemoryObject moi) {
        mol.add(moi);
    }

    public void setMind(AgentMind agentMind){
        this.agentMind = agentMind;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public Creature getCreature() {
        return this.creature;
    }

    public void StartTimer() {
        t = new Timer();
        MVTimerTask tt = new MVTimerTask(this);
        t.scheduleAtFixedRate(tt, 0, 250);

    }

    public void tick() {

        if(reportThread == null){
            initThread();
        }


        creature.updateState();
        printTextInView();

        j++;
        if (j == 250) {
            createObjectsInWorld();
            j = 0;
        }
    }


    private void initThread(){
        reportThread = new Thread(){
            public void run(){

                initDate = new Date();
                double time = 0;

                while(time <= (defaultTime*60)) {

                    //double time = (((new Date()).getTime() - initDate.getTime()) / 1000);
                    time+=1;

                    reportEnergySpent(time);
                    reportDrivesActivation(time);
                    reportCreatureScore(time);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                finalizeReport("Creature's Energy", "Time", "Energy", getResultEnergySpent(), fileEnergySpent);
                finalizeReport("Creature's Leaflet", "Time", "Jewels Collected (%)", getResultCreatureScore(), fileCreatureScore);
                finalizeReport("Creature's Drives", "Time", "Activation", getResultDrivesActivation(), fileDrivesActivation);
                t.cancel();
                t.purge();
                agentMind.shutDown();
            }
        };

        reportThread.start();
    }


    private void printTextInView(){
        String alltext = "";

        if (mol.size() != 0)
            for (MemoryObject mo : mol) {
                if (mo.getI() != null) {
                    //Class cl = mo.getT();
                    //Object k = cl.cast(mo.getI());
                    Object k = mo.getI();
                    alltext += mo.getName() + ": " + k + "\n";
                } else
                    alltext += mo.getName() + ": " + mo.getI() + "\n";

            }
        text.setText(alltext);
    }

    private void createObjectsInWorld(){
        try {
            Random random = new Random();
            World.createJewel(random.nextInt(5), r.nextInt(800), r.nextInt(600));
            World.createJewel(random.nextInt(5), r.nextInt(800), r.nextInt(600));
            World.createJewel(random.nextInt(5), r.nextInt(800), r.nextInt(600));
            World.createFood(random.nextInt(2), r.nextInt(800), r.nextInt(600));
            World.createFood(random.nextInt(2), r.nextInt(800), r.nextInt(600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean checkTimeStop() {

        boolean bFinish = false;

        if ((double) ((new Date()).getTime() - initDate.getTime()) >= this.defaultTime * 60 * Math.pow(10, 3)) {
            bFinish  = true;
        }

        return bFinish;
    }

    private void reportEnergySpent(double time) {
        this.getResultEnergySpent().add(new Result("Energy Spent", time, creature.getFuel()));
    }

    private void reportDrivesActivation(double time) {
        List<MemoryObject> drivesMO = mol.stream().filter(d -> d.getName() == MotivationalCodelet.OUTPUT_DRIVE_MEMORY).collect(Collectors.toList());
        drivesMO.stream().forEach(driveMO -> {
            if(driveMO.getI() != null) {
                this.getResultDrivesActivation().add(new Result(((Drive) (driveMO.getI())).getName(), time, driveMO.getEvaluation()));
            }
        });
    }

    private void reportCreatureScore(double time) {
        this.getResultCreatureScore().add(new Result("Percentage of Jewelry Collected", time, getCreatureInnerSense().getLeafletCompleteRate()));
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        text = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        text.setColumns(20);
        text.setRows(5);
        jScrollPane1.setViewportView(text);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MindView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MindView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MindView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MindView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MindView mv;
                mv = new MindView("Motivational Creature");
                mv.setVisible(true);
                mv.StartTimer();
            }
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea text;

    public CreatureInnerSense getCreatureInnerSense() {
        return creatureInnerSense;
    }

    public void setCreatureInnerSense(CreatureInnerSense creatureInnerSense) {
        this.creatureInnerSense = creatureInnerSense;
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
    // End of variables declaration//GEN-END:variables
}
