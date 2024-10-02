import vectors.Vector3;
public class Sphere implements Renderable {
    private Vector3 position;
    private float radius;
    private Vector3 color;
    private Vector3 emission;
    private Vector3 reflectionMaterial;

    public Sphere(Vector3 position, float radius, Vector3 color, Vector3 emission) {
        this.position = position;
        this.radius = radius;
        this.color = color;
        this.emission = emission;
    }

    public Sphere(Vector3 position, float radius, Vector3 color, Vector3 emission, Vector3 reflectionMaterial) {
        this.position = position;
        this.radius = radius;
        this.color = color;
        this.emission = emission;
        this.reflectionMaterial = reflectionMaterial;
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

    public Vector3 reflectionMaterial() {
        return reflectionMaterial;
    }
    public Vector3 position() {
        return position;
    }
    public float radius() {
        return radius;
    }
    public Vector3 color() {
        return color;
    }
    public Vector3 emission() {
        return emission;
    }
}
