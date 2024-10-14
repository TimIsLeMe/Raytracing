import vectors.Vector2;
import vectors.Vector3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public class Material {

    private Vector3 color;
    private Vector3 emission;
    private Vector3 reflection;
    private Function reflectionType;
    private String bitmapPath;
    private BufferedImage texture;
    private Function<Vector3, Vector3> computedColor;

    public Material(Vector3 color, Vector3 emission) {
        this.color = color;
        this.emission = emission;
    }

    public Material(Vector3 color, Vector3 emission, String bitmapPath) {
        this.color = color;
        this.emission = emission;
        this.bitmapPath = bitmapPath;
        setTexture();
    }
    public Material(Vector3 color, Vector3 emission, Vector3 reflection, Function reflectionType, Function<Vector3, Vector3> computedColor) {
        this.color = color;
        this.emission = emission;
        this.reflection = reflection;
        this.reflectionType = reflectionType;
        this.computedColor = computedColor;
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

    public Vector3 getColor(Vector3 p, Vector3 normal) {
        if(bitmapPath != null) {
            try {
                Vector2 p2 = Main.translatePointToSphere(normal, texture.getWidth(), texture.getHeight());
                var c = Main.convertIntToSRgb(texture.getRGB((int) p2.x(), (int) p2.y()));
                return Main.convertSrgbToLinRgb(c);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        if(computedColor != null) {
            return computedColor.apply(p);
        }
        return color;
    }

    public Vector3 color() {
        return color;
    }

    public Vector3 emission() {
        return emission;
    }

    public Vector3 reflection() {
        return reflection;
    }

    public Function reflectionType() {
        return reflectionType;
    }

    public Function<Vector3, Vector3> computedColor() {
        return computedColor;
    }
}
