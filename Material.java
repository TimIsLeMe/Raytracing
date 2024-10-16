import vectors.Vector2;
import vectors.Vector3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class Material {

    private Vector3 color;
    private Vector3 emission;
    private float emissionStrength;
    private Vector3 specular;
    private Function<Tuple<List<Vector3>, HitPoint>, Vector3> brdf;
    private String bitmapPath;
    private BufferedImage texture;

    public Material(Vector3 color, Vector3 emission) {
        this.color = color;
        this.emission = emission;
    }

    public Material(Vector3 color, Vector3 emission, String bitmapPath, float emissionStrength) {
        this.color = color;
        this.emission = emission;
        this.bitmapPath = bitmapPath;
        this.emissionStrength = emissionStrength;
        setTexture();
    }
    public Material(Vector3 color, Vector3 emission, Vector3 specular, Function brdf) {
        this.color = color;
        this.emission = emission;
        this.specular = specular;
        this.brdf = brdf;
    }

    public Material(Vector3 color, Vector3 emission, String bitmapPath, float emissionStrength, Vector3 specular, Function brdf) {
        this.color = color;
        this.emission = emission;
        this.bitmapPath = bitmapPath;
        this.emissionStrength = emissionStrength;
        setTexture();
        this.specular = specular;
        this.brdf = brdf;
    }

    public void setTexture() {
        try {
            File pathToFile = new File(bitmapPath);
            texture = ImageIO.read(pathToFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBitmapPath(String bitmapPath) {
        this.bitmapPath = bitmapPath;
    }

    public Vector3 getColor(Vector3 normal) {
        if(texture != null) {
            if(emissionStrength <= 0f) return Vector3.ZERO;
            try {
                Vector2 p2 = Main.translatePointFromSphere(normal, texture.getWidth(), texture.getHeight());
                var c = Main.convertIntToSRgb(texture.getRGB((int) p2.x(), (int) p2.y())).multiply(emissionStrength);
                return Main.convertSrgbToLinRgb(c);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return color;
    }

    public Vector3 color() {
        return color;
    }

    public Vector3 emission(Vector3 normal) {
        if(texture != null) {
            try {
                Vector2 p2 = Main.translatePointFromSphere(normal, texture.getWidth(), texture.getHeight());
                var c = Main.convertIntToSRgb(texture.getRGB((int) p2.x(), (int) p2.y()));
                return Main.convertSrgbToLinRgb(c);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return emission;
    }
    public Function reflectionMethod() {
        return brdf;
    }

    public Vector3 specular() {
        return specular;
    }
    public static Function<Tuple<Vector3[], Renderable>, Vector3> DefaultReflection = (input) -> {
        Vector3 d, w, normal;
        Renderable r = input.second();
        var vectors = input.first();
        d = vectors[0];
        w = vectors[1];
        normal = vectors[2];
        var dr = Vector3.normalize(Vector3.reflect(d, normal));
        var color = r.getColor(normal).multiply(Main.DIV_PI);
        if (Vector3.dot(w, dr) > 1 - Main.BRDF_EPSILON) {
            return r.getSpecular().multiply(Main.BRDF_LAMBDA).add(color);
        } else {
            return color;
        }
    };
}
