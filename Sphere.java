import vectors.Vector3;

import java.util.function.Function;

public class Sphere implements Renderable {
    private Vector3 position;
    private float radius;
    private Material material;

    public Sphere(Vector3 position, float radius, Material material) {
        this.position = position;
        this.radius = radius;
        this.material = material;
    }

    public Hit hit(Vector3 o, Vector3 d) {
        float a = 1;
        Vector3 normD = Vector3.normalize(d);
        Vector3 coVec = Vector3.subtract(o, this.position());
        float b = Vector3.dot(coVec.multiply(2), normD);
        float c = coVec.lengthSquared() - this.radius() * this.radius();
        var minPlu = Math.sqrt(b * b - 4 * a * c);
        float lambda1 = (float) ((-b + minPlu) / (2 * a)); // midnight formula
        float lambda2 = (float) ((-b - minPlu) / (2 * a));
        Hit hit = null;
        if (lambda1 >= 0) {
            var intersection1 = o.add(d.multiply(lambda1));
            hit = new Hit(new HitPoint(intersection1, this), lambda1);
        }
        if (lambda2 >= 0 && lambda2 < lambda1) {
            var intersection2 = o.add(d.multiply(lambda2));
            hit = new Hit(new HitPoint(intersection2, this), lambda2);
        }
        return hit;
    }

    public Function reflectionMethod() {
        return material.reflectionMethod();
    }

    public Vector3 getSpecular() {
        return material.specular();
    }

    public Vector3 position() {
        return position;
    }
    public float radius() {
        return radius;
    }
    public Vector3 getColor(Vector3 normal) {
        return material.getColor(normal);
    }
    public Vector3 color() { return material.color(); }

    public Vector3 getEmission(Vector3 n) {
        return material.emission(n);
    }
}
