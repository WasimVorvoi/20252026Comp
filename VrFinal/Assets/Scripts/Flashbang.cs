using UnityEngine;
using UnityEngine.UI;

public class Flashbang : MonoBehaviour
{
    public float flashDuration = 1f;

    public Image img;
    private float timer = -1f;

    public void Trigger()
    {
        timer = 0f;
    }

    void Update()
    {
        if (timer < 0f) { 
            return; 
        }
        timer += Time.unscaledDeltaTime;
        float progress = Mathf.Clamp01(timer / flashDuration);
        float alpha = Mathf.SmoothStep(1f, 0f, progress);
        img.color = new Color(1f, 1f, 1f, alpha);
        if (progress >= 1f) { 
            timer = -1f; 
        }
    }
}
