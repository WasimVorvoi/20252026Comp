using UnityEngine;

public class FruitHalf : MonoBehaviour
{
    public float lifeTime;
    public string floorTag = "Floor";

    void Start()
    {
        Destroy(gameObject, lifeTime);
    }

    void OnCollisionEnter(Collision other)
    {
        if (other.gameObject.tag == floorTag)
        {
            Destroy(gameObject);
        }
    }
}
