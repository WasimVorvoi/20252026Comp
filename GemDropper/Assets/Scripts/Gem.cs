using UnityEngine;

public class Gem : MonoBehaviour
{
    public GameObject GoodGem;
    public GameObject BadGem;
    public GameObject SpecialGem;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        InvokeRepeating("SpawnGood", 1f, 3f);
        InvokeRepeating("SpawnBad", 2f, 3f);
        InvokeRepeating("SpawnSpecial", 3f, 3f);
    }

    // Update is called once per frame
    void Update()
    {
        
    }
    void SpawnGood() {
        Instantiate(GoodGem, new Vector3(Random.Range(-4, 5),10, Random.Range(-7, 0)), Quaternion.identity);
    }
    void SpawnBad() {
        Instantiate(BadGem, new Vector3(Random.Range(-4, 5), 10, Random.Range(-7, 0)), Quaternion.identity);
    }
    void SpawnSpecial() {
        Instantiate(SpecialGem, new Vector3(Random.Range(-4, 5), 10, Random.Range(-7, 0)), Quaternion.identity);
    }
}
