using UnityEngine;
using EzySlice;

public class BananaScript : MonoBehaviour
{
    public Material sliceMaterial;
    public ParticleSystem juicePrefab;
    public ParticleSystem juiceTrail;
    public Score score;
    public int pointValue = 1;

    void Start()
    {
        score = GameObject.Find("GameManager").GetComponent<Score>();
    }

    void OnCollisionEnter(Collision collision)
    {
        if (!collision.gameObject.CompareTag("Sword")) return;

        Transform sword = collision.transform;
        Vector3 swingDir = collision.relativeVelocity.normalized;
        Vector3 sliceNormal = Vector3.Cross(sword.forward, swingDir).normalized;
        if (sliceNormal == Vector3.zero) sliceNormal = sword.up;

        SlicedHull hull = gameObject.Slice(transform.position, sliceNormal, sliceMaterial);
        if (hull == null) return;

        GameObject upper = hull.CreateUpperHull(gameObject, sliceMaterial);
        GameObject lower = hull.CreateLowerHull(gameObject, sliceMaterial);

        Rigidbody upperRb = upper.AddComponent<Rigidbody>();
        upperRb.AddForce(sliceNormal * 3f, ForceMode.Impulse);
        Rigidbody lowerRb = lower.AddComponent<Rigidbody>();
        lowerRb.AddForce(-sliceNormal * 3f, ForceMode.Impulse);

        if (juiceTrail != null)
        {
            Instantiate(juiceTrail, upper.transform.position, Quaternion.identity, upper.transform);
            Instantiate(juiceTrail, lower.transform.position, Quaternion.identity, lower.transform);
        }

        Instantiate(juicePrefab, transform.position, Quaternion.identity);
        score.AddPoints(pointValue);

        Destroy(gameObject);
        Destroy(upper, 2f);
        Destroy(lower, 2f);
    }
}
