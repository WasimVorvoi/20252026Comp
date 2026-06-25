using UnityEngine;

public class Slice : MonoBehaviour
{
    public Score score;
    public ParticleSystem juicePrefab;

void OnCollisionEnter(Collision collision)
    {
        GameObject hit = collision.gameObject;
        Vector3 contactPoint = collision.GetContact(0).point;

        int points = 0;
        bool isSmashable = false;

        if (hit.CompareTag("Bomb"))
        {
            points = -5;
            isSmashable = true;
        }
        else if (hit.CompareTag("Golden"))
        {
            points = 5;
            isSmashable = true;
        }
        else if (hit.CompareTag("Strawberry"))
        {
            points = 1;
            isSmashable = true;
        }

        if (!isSmashable) return;

        score.AddPoints(points);
        Instantiate(juicePrefab, contactPoint, Quaternion.identity);

        StrawberryScript crumble = hit.GetComponent<StrawberryScript>();
        if (crumble != null)
            crumble.Crumble(hit.transform);
        else
            Destroy(hit);
    }
}
