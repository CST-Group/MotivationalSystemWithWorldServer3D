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
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createDeliverySpot(rand.nextInt(width),rand.nextInt(height));


            /*World.createJewel(rand.nextInt(6), 500, 450);
            World.createJewel(rand.nextInt(6), 500, 440);
            World.createJewel(rand.nextInt(6), 500, 430);
            World.createJewel(rand.nextInt(6), 500, 420);
            World.createJewel(rand.nextInt(6), 500, 400);
            World.createJewel(rand.nextInt(6), 500, 390);
            World.createJewel(rand.nextInt(6), 500, 380);
            World.createJewel(rand.nextInt(6), 500, 370);
            World.createJewel(rand.nextInt(6), 500, 360);*/


            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            World.createBrick(4, x, y, x + 20, y + 20);

            x = rand.nextInt(width);
            y = rand.nextInt(height);
            World.createBrick(4, x, y, x + 20, y + 20);
            
            /*x = rand.nextInt(width);
            y = rand.nextInt(height);
            World.createBrick(4, x, y, x + 40, y + 40);*/


            c = proxy.createCreature(100, 450, 0);
            c.start();

        } catch (CommandExecException e) {

        }
        System.out.println("Robot " + c.getName() + " is ready to go.");

    }
}
