import vectors.Vector3;

import java.util.function.Function;

public interface Renderable {
    Vector3 position();
    Hit hit(Vector3 o, Vector3 d);
    Vector3 getColor(Vector3 n);
    Vector3 getEmission(Vector3 n);
    Vector3 getSpecular(Vector3 normal);

}
