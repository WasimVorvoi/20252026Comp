using UnityEngine;

public class Rainbow : MonoBehaviour
{
    public Material targetMaterial;
    public float speed = 1f;
    float hue = 0f;
    void Start()
    {

    }

    void Update()
    {
        hue += Time.deltaTime * speed;
        if (hue > 1f) {
            hue -= 1f;
        }
        Color c = Color.HSVToRGB(hue, 1f, 1f);
        targetMaterial.color = c;
    }
}
