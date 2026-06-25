using System.Collections;
using UnityEngine;

public class GemSpin : MonoBehaviour
{
    public GameObject gem;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }
    private void OnCollisionEnter(Collision collision)
    {
        if (collision.gameObject.tag.Equals("stop"))
        {
            Destroy(gameObject);
            Instantiate(gem, GameObject.FindGameObjectWithTag("start").transform.position, Quaternion.identity);
        }
    }

}
