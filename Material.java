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
    public Material(Vector3 color, Vector3 emission, Vector3 specular) {
        this.color = color;
        this.emission = emission;
        this.specular = specular;
    }

    public Material(Vector3 color, Vector3 emission, String bitmapPath, float emissionStrength, Vector3 specular) {
        this.color = color;
        this.emission = emission;
        this.bitmapPath = bitmapPath;
        this.emissionStrength = emissionStrength;
        setTexture();
        this.specular = specular;
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
            Vector2 p2 = Vector2.ZERO;
            try {
                p2 = Main.translatePointFromSphere(normal, texture.getWidth(), texture.getHeight());
                var c = Main.convertIntToSRgb(texture.getRGB((int) p2.x(), (int) p2.y()));
                return Main.convertSrgbToLinRgb(c);
            } catch (IndexOutOfBoundsException e) {
                System.out.println(p2);
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
            if(emissionStrength <= 0f) return Vector3.ZERO;
            try {
                Vector2 p2 = Main.translatePointFromSphere(normal, texture.getWidth(), texture.getHeight());
                var c = Main.convertIntToSRgb(texture.getRGB((int) p2.x(), (int) p2.y())).multiply(emissionStrength);
                return Main.convertSrgbToLinRgb(c);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return emission;
    }
    public Vector3 specular(Vector3 normal) {
        if(texture != null) {
            try {
                Vector2 p2 = Main.translatePointFromSphere(normal, texture.getWidth(), texture.getHeight());
                var c = Main.convertIntToSRgb(texture.getRGB((int) p2.x(), (int) p2.y()));
                return Main.convertSrgbToLinRgb(c);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return specular;
    }
}
