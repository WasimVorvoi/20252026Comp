using UnityEngine;

public class KartParticles : MonoBehaviour
{
    public Rigidbody body;
    public ParticleSystem exhaust;
    public float maxSpeed = 18f;
    public float maxEmission = 60f;
    public Color slowColor = new Color(1f, 0.6f, 0.1f, 1f);
    public Color fastColor = new Color(0.2f, 0.8f, 1f, 1f);

    public void Update()
    {
        if (body == false || exhaust == false)
        {
            return;
        }

        Vector3 flatVelocity = body.linearVelocity;
        flatVelocity.y = 0f;

        float topSpeed = 1f;
        if (maxSpeed > 0f)
        {
            topSpeed = maxSpeed;
        }

        float normalized = Mathf.Clamp01(flatVelocity.magnitude / topSpeed);

        ParticleSystem.EmissionModule emission = exhaust.emission;
        emission.rateOverTime = normalized * maxEmission;

        ParticleSystem.MainModule main = exhaust.main;
        main.startColor = Color.Lerp(slowColor, fastColor, normalized);
    }
}
