using System.Linq;
using UnityEngine;
using EzySlice;

public class Fruit : MonoBehaviour
{
    public int points;
    public int badPoints;
    public int specialPoints;
    public bool isBad;
    public bool isSpecial;
    public bool triggersSlowMo;
    public bool triggersDoublePoints;
    public float size;
    public float sizeMultiplier;

    public string batTag;
    public string floorTag;
    public float floorGrace;

    public GameObject meshObject;
    public Material crossMaterial;
    public GameObject particlePrefab;
    public GameObject explosionPrefab;

    public float lifeTime;
    public float halfForce;
    public float halfLifeTime;
    public float halfTorqueRange;
    public float minSliceSpeed;

    public GameObject gameManagerObject;

    private bool sliced;
    private bool floorReady;
    private float floorTimer;
    private float lifeTimer;

void Start()
    {
        sliced = false;
        floorReady = false;
        float finalSize = size * sizeMultiplier;
        transform.localScale = new Vector3(finalSize, finalSize, finalSize);
        floorTimer = floorGrace;
        lifeTimer = lifeTime;

    }

void Update()
    {
        if (floorReady == false)
        {
            floorTimer = floorTimer - Time.deltaTime;
            if (floorTimer <= 0)
            {
                floorReady = true;
            }
        }

        lifeTimer = lifeTimer - Time.deltaTime;
        if (lifeTimer <= 0)
        {
            if (sliced == false)
            {
                Destroy(gameObject);
            }
        }
    }




    void OnCollisionEnter(Collision other)
    {
        if (sliced == true)
        {
            return;
        }

        if (other.gameObject.tag == batTag)
        {
            DoSlice(other);
            return;
        }

        if (other.gameObject.tag == floorTag)
        {
            if (floorReady == true)
            {
                Destroy(gameObject);
            }
        }
    }

void DoSlice(Collision other)
    {
        sliced = true;

        if (isBad == true)
        {
            gameManagerObject.GetComponent<GameManager>().AddScore(badPoints);
        }
        else if (isSpecial == true)
        {
            gameManagerObject.GetComponent<GameManager>().AddScore(specialPoints);
        }
        else
        {
            gameManagerObject.GetComponent<GameManager>().AddScore(points);
        }

        if (triggersSlowMo == true)
        {
            gameManagerObject.GetComponent<GameManager>().TriggerSlowMo();
        }

        if (triggersDoublePoints == true)
        {
            gameManagerObject.GetComponent<GameManager>().TriggerDoublePoints();
        }

        if (isBad == true)
        {
            gameManagerObject.GetComponent<GameManager>().TriggerFlashbang();
        }

        if (isBad == true)
        {
            Instantiate(explosionPrefab, transform.position, Quaternion.identity);
        }
        else
        {
            Instantiate(particlePrefab, transform.position, Quaternion.identity);
        }

        Vector3 planePos = transform.position;
        Vector3 planeNormal = new Vector3(0f, 1f, 0f);

        Rigidbody batRb = other.rigidbody;
        Vector3 vel = batRb.linearVelocity;
        if (vel.magnitude > minSliceSpeed)
        {
            Vector3 a = vel.normalized;
            Vector3 b = other.transform.up;
            Vector3 cross = new Vector3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
            planeNormal = cross.normalized;
            if (planeNormal == new Vector3(0f, 0f, 0f))
            {
                planeNormal = new Vector3(0f, 1f, 0f);
            }
        }

        SlicedHull hull = meshObject.Slice(planePos, planeNormal, crossMaterial);
        if (hull == null) { Destroy(gameObject); return; }

        GameObject upper = hull.CreateUpperHull(meshObject, crossMaterial);
        GameObject lower = hull.CreateLowerHull(meshObject, crossMaterial);
        SetupHalf(upper, planeNormal);
        SetupHalf(lower, -planeNormal);

        Destroy(gameObject);
    }

    void SetupHalf(GameObject g, Vector3 dir)
    {
        g.transform.position = meshObject.transform.position;
        g.transform.rotation = meshObject.transform.rotation;
        g.transform.localScale = meshObject.transform.lossyScale;

        MeshCollider mc = g.AddComponent<MeshCollider>();
        mc.convex = true;

        foreach (Collider batCol in GameObject.FindGameObjectsWithTag(batTag).SelectMany(o => o.GetComponents<Collider>()))
            Physics.IgnoreCollision(mc, batCol);

        Fruit[] liveFruits = FindObjectsOfType<Fruit>();
        for (int i = 0; i < liveFruits.Length; i++)
        {
            Collider[] fruitCols = liveFruits[i].GetComponents<Collider>();
            for (int j = 0; j < fruitCols.Length; j++)
                Physics.IgnoreCollision(mc, fruitCols[j]);
        }

        Rigidbody rb = g.AddComponent<Rigidbody>();
        rb.AddForce(dir * halfForce, ForceMode.Impulse);
        float tx = Random.Range(-halfTorqueRange, halfTorqueRange);
        float ty = Random.Range(-halfTorqueRange, halfTorqueRange);
        float tz = Random.Range(-halfTorqueRange, halfTorqueRange);
        rb.AddTorque(new Vector3(tx, ty, tz), ForceMode.Impulse);

        FruitHalf fh = g.AddComponent<FruitHalf>();
        fh.lifeTime = halfLifeTime;
    }


}
