import vectors.Vector2;
import vectors.Vector3;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

public class Main {
    private static Vector3 eye = new Vector3(0, 0, -4);
    private static Vector3 lookAt = new Vector3(0, 0, 6);
    private static float fov = 0.628319f; // 36 degrees
    private static float fovHalfTan = (float) Math.tan(fov / 2);
    public static List<Renderable> scene;
    private static final int HEIGHT = 650, WIDTH = 650;
    private static final double p = 0.2;
    private static final int RAYS = 64;
    private static final float STD_DEVIATION = 0.5f;
    public static final float BRDF_LAMBDA = 10f;
    public static final float BRDF_EPSILON = 0.01f;
    private static final Random RANDOM = new Random();
    public static double DIV_PI = 1.0 / Math.PI;

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
        setScene(scene);
        render(panel, bi, WIDTH, HEIGHT);
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
        var pink = convertSrgbToLinRgb(Color.PINK);
        sceneObjects.add(new Sphere(new Vector3(-1001, 0, 0), 1000, new Material(red, black)));
        sceneObjects.add(new Sphere(new Vector3(1001, 0, 0), 1000, new Material(blue, black)));
        sceneObjects.add(new Sphere(new Vector3(0, 0, 1001), 1000, new Material(lightGray, black)));
        sceneObjects.add(new Sphere(new Vector3(0, -1001, 0), 1000, new Material(lightGray, black)));
        sceneObjects.add(new Sphere(new Vector3(0, 1001, 0), 1000, new Material(white, white.multiply(2))));
        sceneObjects.add(new Sphere(new Vector3(-0.6, -0.7, -0.6), 0.3f, new Material(yellow, black, "resources/MinecraftGlowstone.jpg", 1f)));
        sceneObjects.add(new Sphere(new Vector3(0.3, -0.4, 0.3), 0.6f, new Material(cyan, black, white)));
    }

    public static void setSceneCustom1(List<Renderable> sceneObjects) {
        eye = new Vector3(5, 1, -4);
        lookAt = new Vector3(-2.5, -0.5f, 4);
        var black = convertSrgbToLinRgb(Color.BLACK);
        var gray = convertSrgbToLinRgb(Color.GRAY);
        var white = convertSrgbToLinRgb(Color.WHITE);
        var yellow = convertSrgbToLinRgb(Color.YELLOW);
        sceneObjects.add(new Sphere(new Vector3(0, -1001, 0), 1000, new Material(gray, black)));
        sceneObjects.add(new Sphere(new Vector3(0, 1001, 0), 1000, new Material(white, white.multiply(0.1))));
        sceneObjects.add(new Sphere(new Vector3(0.3, -0.4, 0.3), 0.6f, new Material(yellow, black, "resources/Tigerstone.jpg", 0f, yellow)));
        sceneObjects.add(new Sphere(new Vector3(-1750, -150, 2600), 100f, new Material(yellow, black, "resources/MinecraftGlowstone.jpg", 2f)));
    }

    public static void setSceneCustom2(List<Renderable> sceneObjects) {
        var black = convertSrgbToLinRgb(Color.BLACK);
        var cyan = convertSrgbToLinRgb(Color.CYAN).multiply(0.7);
        var yellow = convertSrgbToLinRgb(Color.YELLOW);
        var pink = convertSrgbToLinRgb(Color.PINK);
        sceneObjects.add(new Sphere(new Vector3(0, 0, 1001), 1000, new Material(cyan, black)));
        sceneObjects.add(new Sphere(new Vector3(0, -1001, 0), 1000, new Material(pink, black, pink)));
        sceneObjects.add(new Sphere(new Vector3(0, -1, 0.4), 0.7f, new Material(yellow, black, "resources/MinecraftGlowstone.jpg", 1f)));
    }

    public static void render(JPanel panel, BufferedImage bi, int width, int height) {
        int[] rgbArr = new int[1];
        float halfW = ((float) width) / 2f;
        float halfH = ((float) height) / 2f;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float posX = (x - halfW) / halfW;
                float posY = (y - halfH) / halfH;
                Vector3 c = Vector3.ZERO;
                for(int i = 0; i < RAYS; i++) {
                    float r1 = (float) (RANDOM.nextGaussian() * STD_DEVIATION);
                    float r2 = (float) (RANDOM.nextGaussian() * STD_DEVIATION);
                    Vector2 geoPos = new Vector2(posX + r1 / width, posY + r2 / height);
                    var originAndDirection = createEyeRay(eye, lookAt, fov, geoPos);
                    c = c.add(ComputeColor(originAndDirection.first(), originAndDirection.second()));
                }
                c = c.multiply(1f / RAYS);
                int color = convertLinRgbToInt(c.x(), c.y(), c.z());
                Arrays.fill(rgbArr, color);
                bi.setRGB(x, y, 1, 1, rgbArr, 0, width);
            }
            panel.repaint();
        }
    }
    public static Tuple<Vector3, Vector3> createEyeRay(Vector3 eye, Vector3 lookAt, float fov, Vector2 pixel) { // returns origin point and direction
        var f = Vector3.normalize(Vector3.subtract(lookAt, eye));
        var r = Vector3.normalize(Vector3.cross(Vector3.UNIT_Y, f));
        var u = Vector3.normalize(Vector3.cross(r, f));
        var d = f.add(r.multiply(pixel.x()).multiply(fovHalfTan).add(u.multiply(fovHalfTan).multiply(pixel.y())));
        return new Tuple<>(eye, Vector3.normalize(d));
    }
    public static Vector3 ComputeColor(Vector3 o, Vector3 d) {
        var hp = findClosestHitPoint(o, d);
        if (hp == null) {
            return Vector3.ZERO;
        }
        var normal = calculateNormalAtPointForSphere(hp.pos(), hp.object().position());
        if (getRandomDouble() < p) {
            return hp.object().getEmission(normal);
        }
        var normD = Vector3.normalize(d);
        var nextO = hp.pos().add(normal.multiply(BRDF_EPSILON));
        var w = SampleDirection(normal);
        var normW = Vector3.normalize(w);
        var cR = ComputeColor(nextO, w);
        var brdf = hp.object().getSpecular() != null ? BRDF(normD, normW, normal, hp.object()) : BRDF(normal, hp.object());
        double cFactor = Vector3.dot(normW, normal) * (Math.PI * 2 / (1 - p));
        var color = cR.multiply(brdf.multiply(cFactor));
        return color.add(hp.object().getEmission(normal));
    }

    public static Vector3 calculateNormalAtPointForSphere(Vector3 intersectP, Vector3 center) {
        return Vector3.normalize(Vector3.subtract(intersectP, center));
    }

    public static Vector3 BRDF(Vector3 wi, Vector3 wo, Vector3 normal, Renderable r) {
        var dr = Vector3.normalize(Vector3.reflect(wi, normal));
        var color = r.getColor(normal).multiply(Main.DIV_PI);
        if (Vector3.dot(wo, dr) > 1 - Main.BRDF_EPSILON) {
            return r.getSpecular().multiply(Main.BRDF_LAMBDA).add(color);
        } else {
            return color;
        }
    }
    public static Vector3 BRDF( Vector3 normal, Renderable r) {
        return r.getColor(normal).multiply(DIV_PI);
    }
    public static Vector3 SampleDirection(Vector3 normal) {
        // Generate a random direction in a hemisphere
        Vector3 sample;
        do {
            sample = new Vector3(getRandomDouble() * 2 - 1, getRandomDouble() * 2 - 1, getRandomDouble() * 2 - 1);
        } while (sample.lengthSquared() > 1); // if length < 1 then length^2 < 1
        if (Vector3.dot(sample, normal) <= 0) {
            sample = sample.negate(); // flip vector if not in hemisphere
        }
        return Vector3.normalize(sample);
    }
    public static double getRandomDouble() {
        return RANDOM.nextDouble();
    }
    public static float getRandomFloat() {
        return RANDOM.nextFloat();
    }
    public static HitPoint findClosestHitPoint(Vector3 o, Vector3 d) {
        float minLambda = Float.MAX_VALUE;
        Hit closestHit = null;
        for (Renderable obj : scene) {
            var hit = obj.hit(o ,d);
            if(hit != null && hit.lambda() < minLambda) {
                closestHit = hit;
                minLambda = hit.lambda();
            }
        }
        if (closestHit != null) {
            return closestHit.hitpoint();
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

    public static Vector2 translatePointFromSphere(Vector3 n, int width, int height) {
        double s = Math.atan2(n.x(), n.z());
        double t = Math.acos(n.y());
        int sInt = (int) ((s + Math.PI) / (Math.PI * 2) * (width - 1));
        if(Double.isNaN(t)) t = 0;
        int tInt = (int) (t / Math.PI * (height - 1));
        return new Vector2(sInt, tInt);
    }
}