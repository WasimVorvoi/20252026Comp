using UnityEngine;

public class Spawn : MonoBehaviour
{
    public GameObject[] fruitPrefabs;
    public GameObject bombPrefab;
    public GameObject goldenPrefab;

    public float minLaunchForce = 5f;
    public float maxLaunchForce = 8f;
    public float spawnInterval = 1.5f;
    public float spawnXRange = 3f;
    public float spawnZRange = 1f;

    [Range(0f, 1f)] public float bombChance = 0.15f;
    [Range(0f, 1f)] public float goldenChance = 0.05f;

    private bool isSpawning = false;

    public void BeginSpawning()
    {
        if (isSpawning) return;
        isSpawning = true;
        InvokeRepeating(nameof(SpawnAndLaunch), 1f, spawnInterval);
    }

    public void StopSpawning()
    {
        isSpawning = false;
        CancelInvoke(nameof(SpawnAndLaunch));
    }

    void SpawnAndLaunch()
    {
        GameObject prefabToSpawn;
        float roll = Random.value;

        if (roll < bombChance)
        {
            prefabToSpawn = bombPrefab;
        }
        else if (roll < bombChance + goldenChance)
        {
            prefabToSpawn = goldenPrefab;
        }
        else
        {
            prefabToSpawn = fruitPrefabs[Random.Range(0, fruitPrefabs.Length)];
        }

        Vector3 spawnPos = new Vector3(Random.Range(-spawnXRange, spawnXRange),0.5f,Random.Range(-spawnZRange, spawnZRange));

        GameObject newObj = Instantiate(prefabToSpawn, spawnPos, Random.rotation);
        float scale = Random.Range(0.85f, 1.15f);
        newObj.transform.localScale *= scale;

        Rigidbody rb = newObj.GetComponent<Rigidbody>();
        Vector3 launchDir = new Vector3(Random.Range(-0.3f, 0.3f), 1f,Random.Range(-0.2f, 0.2f)).normalized;
        rb.AddForce(launchDir * Random.Range(minLaunchForce, maxLaunchForce), ForceMode.Impulse);

        Destroy(newObj, 4f);
    }
}

