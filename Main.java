import vectors.Vector2;
import vectors.Vector3;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

public class Main {
    private static Vector3 eye = new Vector3(0, 0, -4);
    private static Vector3 lookAt = new Vector3(0, 0, 6);
    private static float fov = 0.628319f; // 36 degrees
    public static List<Renderable> scene;
    private static final int HEIGHT = 650, WIDTH = 650;
    private static double p = 0.1;
    private static float BRDF_LAMBDA = 10f;
    private static float BRDF_EPSILON = 0.01f;
    private static final Random RANDOM = new Random();

    private static final Function<Vector3, Vector3> testMaterial = (Vector3 p) -> {
        var cyan = convertSrgbToLinRgb(Color.CYAN);
        return convertSrgbToLinRgb(Color.WHITE).multiply(Math.cos((p.x() + p.y()) * 2 * Math.PI));
    };

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setSize(WIDTH, HEIGHT);
        f.setLayout(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        scene = new ArrayList<>();
        setScene(scene);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bi, 0, 0, null);
            }
        };
        panel.setBounds(0, 0, WIDTH, HEIGHT);
        f.add(panel);
        f.getContentPane().setPreferredSize(new Dimension(WIDTH, HEIGHT));
        f.pack();
        f.setVisible(true);
        render(panel, bi, WIDTH, HEIGHT);
//        paintImage(panel, bi, WIDTH, HEIGHT);
//        panel.repaint();
    }

    public static void paintImage(JPanel panel, BufferedImage bi, int width, int height) {
        BufferedImage image = null;
        try {
            File pathToFile = new File("resources/MinecraftGlowstone.jpg");
            image = ImageIO.read(pathToFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
//                Vector3 c = testMaterial.apply(new Vector3(x, y, 0));
//                int color = convertLinRgbToInt(c.x(), c.y(), c.z());
                int color = image.getRGB(x, y);
                var c = convertIntToSRgb(color);
                color = convertSRgbToInt(c);
                bi.setRGB(x, y, 1, 1, new int[] {color}, 0, width);
            }
        }
    }

    public static void setScene(List<Renderable> sceneObjects) {
        var black = convertSrgbToLinRgb(Color.BLACK);
        var red = convertSrgbToLinRgb(Color.RED);
        var blue = convertSrgbToLinRgb(Color.BLUE);
        var gray = convertSrgbToLinRgb(Color.GRAY);
        var lightGray = convertSrgbToLinRgb(Color.LIGHT_GRAY);
        var white = convertSrgbToLinRgb(Color.WHITE);
        var yellow = convertSrgbToLinRgb(Color.YELLOW);
        var cyan = convertSrgbToLinRgb(Color.CYAN);
        sceneObjects.add(new Sphere(new Vector3(-1001, 0, 0), 1000, new Material(red, black)));
        sceneObjects.add(new Sphere(new Vector3(1001, 0, 0), 1000, new Material(blue, black)));
        sceneObjects.add(new Sphere(new Vector3(0, 0, 1001), 1000, new Material(gray, black)));
        sceneObjects.add(new Sphere(new Vector3(0, -1001, 0), 1000, new Material(lightGray, black)));
        sceneObjects.add(new Sphere(new Vector3(0, 1001, 0), 1000, new Material(white, white.multiply(2))));
        sceneObjects.add(new Sphere(new Vector3(-0.6, -0.7, -0.6), 0.3f, new Material(yellow, yellow.add(white).multiply(0.1), "resources/MinecraftGlowstone.jpg")));
        sceneObjects.add(new Sphere(new Vector3(0.3, -0.4, 0.3), 0.6f, new Material(cyan, black)));
    }

    public static void render(JPanel panel, BufferedImage bi, int width, int height) {
        int[] rgbArr = new int[1];
        float halfW = ((float) width) / 2f;
        float halfH = ((float) height) / 2f;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float posX, posY;
                posX = (x - halfW) / halfW;
                posY = (y - halfH) / halfH;
                Vector2 geoPos = new Vector2(posX, posY);
                var originAndDirection = createEyeRay(eye, lookAt, fov, geoPos);
                var c = ComputeColor(originAndDirection.first(), originAndDirection.second());
                int color = convertLinRgbToInt(c.x(), c.y(), c.z());
                Arrays.fill(rgbArr, color);
                bi.setRGB(x, y, 1, 1, rgbArr, 0, width);
            }
            panel.repaint();
        }
    }

    public static Vector3 ComputeColor(Vector3 o, Vector3 d) {
        Vector3 color = Vector3.ZERO;
        var hp = findClosestHitPoint(o, d);
        if (hp == null) {
            return color;
        }
        if (getRandomDouble() < p) {
            return hp.object().getEmission(hp.pos());
        }
        var normal = calculateNormalAtPoint(hp.pos(), hp.object());
        var w = SampleDirection(normal);
        var normD = Vector3.normalize(d);
        var nextO = hp.pos().add(normD.multiply(BRDF_EPSILON));
        var c1 = ComputeColor(nextO, w);
        var brdf = BRDF(normD, w, hp, normal);
        var c2 = brdf.multiply(Vector3.dot(w, normal) * (Math.PI * 2 / (1 - p)));
//        return hp.object().getColor(hp.pos(), normal);
        return color.add(c1.multiply(c2)).add(hp.object().getEmission(hp.pos()));
    }

    public static Vector3 calculateNormalAtPoint(Vector3 hitPoint, Renderable obj) {
        return Vector3.normalize(Vector3.subtract(hitPoint, obj.position()));
    }
    public static Vector3 BRDF(Vector3 d, Vector3 w, HitPoint hp, Vector3 normal) {
//        var dr = Vector3.normalize(Vector3.reflect(d, Vector3.UNIT_Y));
        double divPi = 1 / Math.PI;
        return hp.object().getColor(hp.pos(), normal).multiply(divPi);
//        if (Vector3.dot(w, dr) > 1 - BRDF_EPSILON) {
//            return hp.object().color().multiply(divPi).add(hp.object().emission().multiply(BRDF_LAMBDA));
//        } else {
//            return hp.object().color().multiply(divPi);
//        }
    }
    public static Vector3 SampleDirection(Vector3 normal) {
        // Generate a random direction in a hemisphere
        Vector3 sample;
        do {
            sample = new Vector3(getRandomDouble() * 2 - 1, getRandomDouble() * 2 - 1, getRandomDouble() * 2 - 1);
        } while (sample.length() > 1 || Vector3.dot(sample, normal) <= 0);

        return Vector3.normalize(sample);
    }

    public static float getRandomFloat() {
        return RANDOM.nextFloat() * 2f;
    }
    public static double getRandomDouble() {
        return RANDOM.nextDouble() * 2;
    }
    public static Tuple<Vector3, Vector3> createEyeRay(Vector3 eye, Vector3 lookAt, float fov, Vector2 pixel) { // returns origin point and direction
        var f = Vector3.normalize(Vector3.subtract(lookAt, eye));
        var r = Vector3.normalize(Vector3.cross(Vector3.UNIT_Y, f));
        var u = Vector3.normalize(Vector3.cross(r, f));
        float fovHalfTan = (float) Math.tan(fov / 2);
        var d = f.add(r.multiply(pixel.x()).multiply(fovHalfTan).add(u.multiply(fovHalfTan).multiply(pixel.y())));
        return new Tuple<>(eye, Vector3.normalize(d));
    }

    public static HitPoint findClosestHitPoint(Vector3 o, Vector3 d) {
        List<Hit> hits = new ArrayList<>();
        for (Renderable obj : scene) {
            var hit = obj.hit(o ,d);
            if(hit != null) hits.add(hit);
        }
        hits.sort(Comparator.comparing(Hit::lambda));
        if (hits.size() > 0) {
            return hits.get(0).hitpoint();
        } else return null;
    }

    public static double clipLinRGB(double c) {
        return Math.min(Math.max(c, 0), 1);
    }

    public static int convertLinRgbToSrgb(double c) {
        return (int) (getGammaCorrection(clipLinRGB(c)) * 255);
    }

    public static Vector3 convertLinRgbToSrgb(Vector3 c) {
        return new Vector3(
                (getGammaCorrection(clipLinRGB(c.x())) * 255),
                (getGammaCorrection(clipLinRGB(c.y())) * 255),
                (getGammaCorrection(clipLinRGB(c.z())) * 255));
    }

    public static Vector3 convertSrgbToLinRgb(Vector3 c) {
        double red = getInverseGammaCorrection((c.x() / 255f));
        double green = getInverseGammaCorrection((c.y() / 255f));
        double blue = getInverseGammaCorrection(c.z() / 255f);
        return new Vector3(red, green, blue);
    }
    public static Vector3 convertSrgbToLinRgb(Color c) {
        return convertSrgbToLinRgb(new Vector3(c.getRed(), c.getGreen(), c.getBlue()));
    }

    public static double getInverseGammaCorrection(double c) {
        return Math.pow(c, 2.2);
    }

    public static double getGammaCorrection(double c) {
        return Math.pow(c, 1 / 2.2); // power of 0.4545..
    }

    public static int convertSRgbToInt(int red, int green, int blue) {
        // 255 -> 2^8 each rrrrrrrrggggggggbbbbbbbb
        return ((red & 0x0ff) << 16) | ((green & 0x0ff) << 8) | (blue & 0x0ff); // conversion to single integer for BufferedImage
    }

    public static int convertSRgbToInt(Vector3 c) {
        return convertSRgbToInt((int) c.x(), (int) c.y(), (int) c.z());
    }

    public static Vector3 convertIntToSRgb(int c) {
        // 255 -> 2^8 each rrrrrrrrggggggggbbbbbbbb
        int red = (c >> 16) & 0xFF;
        int green = (c >> 8) & 0xFF;
        int blue = c & 0xFF;
        return new Vector3(red, green, blue);
    }

    public static int convertLinRgbToInt(double r, double g, double b) {
        int red = convertLinRgbToSrgb(r);
        int green = convertLinRgbToSrgb(g);
        int blue = convertLinRgbToSrgb(b);
        return convertSRgbToInt(red, green, blue);
    }

    public static Vector2 translatePointToSphere(Vector3 n, int width, int height) {
        double s = Math.atan2(n.x(), n.z());
        double t = Math.acos(n.y());
        int sInt = (int) ((s + Math.PI) / (Math.PI * 2) * width);
        int tInt = (int) (t / Math.PI * height);
        return new Vector2(sInt, tInt);
    }

}