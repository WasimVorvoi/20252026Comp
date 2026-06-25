using UnityEngine;

public class GemDestroy : MonoBehaviour
{
    private GameObject Manager;
    private GameObject Gem;
    private GameObject PLayer;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        Manager = GameObject.FindGameObjectWithTag("mamager");
        Gem = GameObject.FindGameObjectWithTag("Gem");
        PLayer = GameObject.FindGameObjectWithTag("Player");
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    private void OnCollisionEnter(Collision collision)
    {
        if (collision.gameObject.tag.Equals("Player"))
        {
            Manager.GetComponent<Manager>().IncrimentScore();
            PLayer.GetComponent<Movement>().PLayAni();
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
