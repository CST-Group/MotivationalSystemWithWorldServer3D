package br.unicamp.mtwsapp.application;

import java.util.Random;
import ws3dproxy.CommandExecException;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.World;

/**
 *
 * @author Du
 */
public class Environment {

    public String host = "localhost";
    public int port = 4011;
    public int width = 800;
    public int height = 600;
    public Creature c = null;

    public Environment() {
        WS3DProxy proxy = new WS3DProxy();
        try {
            World w = World.getInstance();
            w.reset();

            Random rand = new Random();

            World.createFood(rand.nextInt(2), rand.nextInt(width), rand.nextInt(height));
            //World.createFood(0, rand.nextInt(width), rand.nextInt(height));
            //World.createFood(0, rand.nextInt(width), rand.nextInt(height));

            World.createJewel(0, rand.nextInt(width), rand.nextInt(height));
            World.createJewel(1, rand.nextInt(width), rand.nextInt(height));
            World.createJewel(2, rand.nextInt(width), rand.nextInt(height));
            World.createJewel(3, rand.nextInt(width), rand.nextInt(height));
            World.createJewel(4, rand.nextInt(width), rand.nextInt(height));
            World.createJewel(5, rand.nextInt(width), rand.nextInt(height));

            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            World.createBrick(4, x, y, x + 40, y + 40);

            x = rand.nextInt(width);
            y = rand.nextInt(height);
            World.createBrick(4, x, y, x + 40, y + 40);
            
            x = rand.nextInt(width);
            y = rand.nextInt(height);
            World.createBrick(4, x, y, x + 40, y + 40);
            
            x = rand.nextInt(width);
            y = rand.nextInt(height);
            World.createBrick(4, x, y, x + 40, y + 40);
            
            x = rand.nextInt(width);
            y = rand.nextInt(height);
            World.createBrick(4, x, y, x + 40, y + 40);

            c = proxy.createCreature(100, 450, 0);
            c.start();
            //c.setRobotID("r0");
            //c.startCamera("r0");

        } catch (CommandExecException e) {

        }
        System.out.println("Robot " + c.getName() + " is ready to go.");

    }
}
