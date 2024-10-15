import vectors.Vector3;

public interface Renderable {
    Hit hit(Vector3 o, Vector3 d);
    Vector3 getColor(Vector3 n);
    Vector3 getEmission(Vector3 n);
    Vector3 position();
    Vector3 reflectionMaterial();
}
