using System.Collections.Generic;
using UnityEngine;

public class StrawberryScript : MonoBehaviour
{
    public ParticleSystem juiceTrail;

    public void Crumble(Transform t)
    {
        List<Transform> children = new List<Transform>();
        foreach (Transform child in t) children.Add(child);
        foreach (Transform child in children) Crumble(child);

        Rigidbody rb = t.GetComponent<Rigidbody>();
        if (rb == null) rb = t.gameObject.AddComponent<Rigidbody>();

        Vector3 force = new Vector3(Random.Range(-4f, 4f), Random.Range(2f, 6f), Random.Range(-4f, 4f));
        rb.AddForce(force, ForceMode.Impulse);

        if (juiceTrail != null)
            Instantiate(juiceTrail, t.position, Quaternion.identity, t);

        Destroy(t.gameObject, 2f);
    }
}
