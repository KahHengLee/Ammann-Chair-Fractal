package com.company;

import java.awt.*;
import java.util.List;
import java.awt.geom.Path2D;
import java.util.*;
import javax.swing.*;
import static java.lang.Math.*;
import static java.util.stream.Collectors.toList;

//defining class to store tile information
public class AmmannFractal extends JPanel {
    class Tile {
        double x, y, angle, size, sign;
        Type type;

        Tile(Type t, double x, double y, double a, double s, double z) {
            type = t; //which tile - Type.Big, Type.Small
            this.x = x; // Coordinates
            this.y = y; //-ve Y coordinate
            angle = a; //rotation, starting angle
            size = s; //scale
            sign = z;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Tile) {
                Tile t = (Tile) o;
                return type == t.type && x == t.x && y == t.y && angle == t.angle;
            }
            return false;
        }
    }

    enum Type {
        Big, Small
    }

    static final double G = (1 + sqrt(5)) / 2; // golden ratio
    static final double T = toRadians(36); // theta

    List<Tile> tiles = new ArrayList<>();


//program activation, get desire Ammann Chair fractal by modifying n,k,a,b variables
//n=generation, k=k-substitution, a=#small tiles removed, b=#big tile remove
    public AmmannFractal() {
        int n=3, k=5, a=0, b=1;
        int w = 700, h = 450;
        setPreferredSize(new Dimension(w, h));
        setBackground(Color.white);

        tiles = nsub(setupPrototiles(w, h), n, k, a, b);
    }

//Set up initial tile
    List<Tile> setupPrototiles(int w, int h) { //initial tile - 1 small tile
        List<Tile> proto = new ArrayList<>();
        proto.add(new Tile(Type.Small, w / 2, h / 2, 0, w / 2.5, -1)); //a = starting angle,  Type.Small = using which tile
        return proto;
    }

    
//Tile arrangement list of k-substitution algorithm
    List<Tile> deflateTiles(List<Tile> tls, int generation) { //input initial tile, n for gen
        if (generation <= 0)
            return tls;

        List<Tile> next = new ArrayList<>();

        for (Tile tile : tls) {
            double nx, ny, mx, my;
            double size = tile.size / sqrt(G);

            if (tile.type == Type.Small) {
                next.add(new Tile(Type.Big, tile.x, tile.y, tile.angle, size, tile.sign));

            } else {

                nx = tile.x + (Math.pow(G,2.5) * tile.size - G * size) * cos(tile.angle);
                ny = tile.y - (Math.pow(G,2.5) * tile.size - G * size) * sin(tile.angle);
                mx = tile.x + Math.pow(G,2.5) * tile.size * cos(tile.angle);
                my = tile.y - Math.pow(G,2.5) * tile.size * sin(tile.angle);
                next.add(new Tile(Type.Small, nx, ny, tile.angle - PI, size,-1 * tile.sign));
                next.add(new Tile(Type.Big, mx, my, tile.angle - tile.sign * PI/2, size, tile.sign));

            }
        }

        // remove duplicates
        tls = next.stream().distinct().collect(toList());

        return deflateTiles(tls, generation - 1);
    }


//remove tiles from k-subtitution
    List<Tile> RemoveTiles(List<Tile> tls, int a, int b) { //input gen n tile list, int a, int b


        List<Tile> next = new ArrayList<>();

        for (Tile tile : tls) {
            if (tile.type == Type.Small) {
                if (a==0) {
                    next.add(new Tile(Type.Small, tile.x, tile.y, tile.angle, tile.size, tile.sign));
                }
                else {a=a-1;}
            } else {
                if (b==0) {
                    next.add(new Tile(Type.Big, tile.x, tile.y, tile.angle, tile.size, tile.sign));
                }
                else {b=b-1;}
            }
        }

        // remove duplicates
        tls = next.stream().distinct().collect(toList());

        return tls;
    }



//Final tile arrangement: run k-sub for n generations
    List<Tile> nsub(List<Tile> tls, int n, int k,int a,int b) { //input initial tile, k for gen
        if (n <= 0)
            return tls;

        List<Tile> next = new ArrayList<>();

        for (Tile tile : tls) {
            List<Tile> temporarylist = new ArrayList<>();
            temporarylist.add(tile);

            if (tile.type == Type.Small) {
                next.addAll(RemoveTiles(deflateTiles(temporarylist, k), a,b));

            } else{
                next.addAll(deflateTiles(RemoveTiles(deflateTiles(temporarylist, k-1), a,b),1));

            }
        }

        // remove duplicates
        tls = next.stream().distinct().collect(toList());

        return nsub(tls, n - 1, k,a,b);
    }



// draw tiles
    void drawTiles(Graphics2D g) {
        double[] dist = {G, sqrt(G*G+G), sqrt((G+1)*(G+1)+G), sqrt((G+1)*(G+1)+(G+2*(G*G)+G*G*G)), Math.pow(G,2.5)};
        double[] sz = {1, 1/sqrt(G)};
        double[] ang = {-Math.PI/2, -Math.PI/2+atan(1/sqrt(G)), -Math.PI/2+atan(sqrt(G)/(G+1)), -Math.PI/2+atan(sqrt(G)), 0};

        List<Tile> Initial_tile = new ArrayList<>();
        int w = 700, h = 450;
        Initial_tile = setupPrototiles(w, h);

        //draw boundary
        for (AmmannFractal.Tile tile : Initial_tile) { 
            Path2D path = new Path2D.Double();
            path.moveTo(tile.x, tile.y);

            int ord = tile.type.ordinal();
            for (int i = 0; i < 5; i++) {
                double x = tile.x + dist[i] * sz[ord] * tile.size * cos(tile.sign * ang[i]+tile.angle);
                double y = tile.y - dist[i] * sz[ord] * tile.size * sin(tile.sign * ang[i]+tile.angle);
                path.lineTo(x, y);
            }
            path.closePath();
            g.setColor(Color.darkGray);
            g.draw(path);
        }

        //draw tiles from the list
        for (AmmannFractal.Tile tile : tiles) { //for loop of all tiles existing
            Path2D path = new Path2D.Double();
            path.moveTo(tile.x, tile.y);

            int ord = tile.type.ordinal();
            for (int i = 0; i < 5; i++) {
                double x = tile.x + dist[i] * sz[ord] * tile.size * cos(tile.sign * ang[i]+tile.angle); 
                double y = tile.y - dist[i] * sz[ord] * tile.size * sin(tile.sign * ang[i]+tile.angle);
                path.lineTo(x, y);
            }
            path.closePath();
            g.setColor(ord == 0 ? Color.orange : Color.yellow);
            g.fill(path); //colour of inside
            g.setColor(Color.darkGray);
            g.draw(path);
        }
    }

    @Override
    public void paintComponent(Graphics og) {
        super.paintComponent(og);
        Graphics2D g = (Graphics2D) og;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(100, 200);//change this number to move image around
        g.scale(0.5,0.5); //change this number to change the size of the image
        drawTiles(g);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("Ammann Chair Fractal");
            f.setResizable(true); //resize window
            f.add(new AmmannFractal(), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
