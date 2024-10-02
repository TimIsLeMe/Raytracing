import vectors.Vector3;

public interface Renderable {
    Hit hit(Vector3 o, Vector3 d);
    Vector3 color();
    Vector3 emission();
    Vector3 position();
    Vector3 reflectionMaterial();
}
