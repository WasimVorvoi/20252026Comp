using UnityEngine;

public class GemDestroyBad : MonoBehaviour
{
    private GameObject Manager;
    private GameObject Gem;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        Manager = GameObject.FindGameObjectWithTag("mamager");
        Gem = GameObject.FindGameObjectWithTag("Gem");
    }

    // Update is called once per frame
    void Update()
    {

    }

    private void OnCollisionEnter(Collision collision)
    {
        if (collision.gameObject.tag.Equals("Player"))
        {
            Manager.GetComponent<Manager>().bad();
            //Gem.GetComponent<Gem>().here();
            Destroy(gameObject);
        }
        if (collision.gameObject.tag.Equals("Finish"))
        {
            //Gem.GetComponent<Gem>().here();
            Destroy(gameObject);
        }
    }
}
