using UnityEngine;

public class FruitSpawner : MonoBehaviour
{
    public GameObject fruit1;
    public GameObject fruit2;
    public GameObject fruit3;
    public GameObject fruit4;
    public GameObject fruit5;
    public GameObject fruit6;
    public GameObject fruit7;
    public GameObject fruit8;
    public GameObject fruit9;
    public GameObject fruit10;

    public Transform spawnCorner1;
    public Transform spawnCorner2;
    public Transform spawnCorner3;
    public Transform spawnCorner4;

    public GameObject gameManagerObject;

    public GameObject bombPrefab;

    public float spawnRate;
    public float spawnSize;
    public float minForce;
    public float maxForce;
    public float upwardAmount;
    public float sideSpread;
    public float spinRange;
    public bool spawning;

    public float bombChance;

    private float timer;
    private float baseSpawnRate;

    void Start()
    {
        baseSpawnRate = spawnRate;
    }

    public void SetDifficulty(int level)
    {
        float t = level - 1;
        spawnRate  = baseSpawnRate * Mathf.Pow(0.85f, t);
        bombChance = t * 0.1f;
    }

void Update()
    {
        if (spawning == false)
        {
            return;
        }

        timer = timer + Time.deltaTime;
        if (timer >= spawnRate)
        {
            timer = 0;
            SpawnOne();
        }
    }

Vector3 GetSpawnPosition()
    {
        float t = Random.value;
        float u = Random.value;
        Vector3 p1 = spawnCorner1.position;
        Vector3 p2 = spawnCorner2.position;
        Vector3 p3 = spawnCorner3.position;
        Vector3 p4 = spawnCorner4.position;
        Vector3 a = p1 + (p2 - p1) * t;
        Vector3 b = p3 + (p4 - p3) * t;
        return a + (b - a) * u;
    }

    void SpawnBomb()
    {
        Vector3 spawnPos = GetSpawnPosition();
        GameObject newBomb = Instantiate(bombPrefab, spawnPos, Random.rotation);
        Fruit bombFruit = newBomb.GetComponent<Fruit>();
        bombFruit.size = spawnSize;
        bombFruit.gameManagerObject = gameManagerObject;

        Rigidbody rb = newBomb.GetComponent<Rigidbody>();
        float force = Random.Range(minForce, maxForce);
        Vector3 dir = new Vector3(0f, upwardAmount, 0f);
        rb.AddForce(dir.normalized * force, ForceMode.Impulse);

        float tx = Random.Range(-spinRange, spinRange);
        float ty = Random.Range(-spinRange, spinRange);
        float tz = Random.Range(-spinRange, spinRange);
        rb.AddTorque(new Vector3(tx, ty, tz), ForceMode.Impulse);
    }

    void SpawnOne()
    {
        if (Random.value < bombChance)
        {
            SpawnBomb();
            return;
        }

        GameObject pickedFruit = fruit1;
        int fruitIndex = Random.Range(0, 10);
        if (fruitIndex == 0) pickedFruit = fruit1;
        else if (fruitIndex == 1) pickedFruit = fruit2;
        else if (fruitIndex == 2) pickedFruit = fruit3;
        else if (fruitIndex == 3) pickedFruit = fruit4;
        else if (fruitIndex == 4) pickedFruit = fruit5;
        else if (fruitIndex == 5) pickedFruit = fruit6;
        else if (fruitIndex == 6) pickedFruit = fruit7;
        else if (fruitIndex == 7) pickedFruit = fruit8;
        else if (fruitIndex == 8) pickedFruit = fruit9;
        else pickedFruit = fruit10;
        Vector3 spawnPos = GetSpawnPosition();
        GameObject newFruit = Instantiate(pickedFruit, spawnPos, Random.rotation);
        Fruit fruitScript = newFruit.GetComponent<Fruit>();
        fruitScript.size = spawnSize;
        fruitScript.gameManagerObject = gameManagerObject;
        Rigidbody rb = newFruit.GetComponent<Rigidbody>();
        float force = Random.Range(minForce, maxForce);
        Vector3 dir = new Vector3(0f, upwardAmount, 0f);
        rb.AddForce(dir.normalized * force, ForceMode.Impulse);
        float tx = Random.Range(-spinRange, spinRange);
        float ty = Random.Range(-spinRange, spinRange);
        float tz = Random.Range(-spinRange, spinRange);
        rb.AddTorque(new Vector3(tx, ty, tz), ForceMode.Impulse);
    }
}
