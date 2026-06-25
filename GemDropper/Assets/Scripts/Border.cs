using UnityEngine;

public class Border : MonoBehaviour
{
    private GameObject BorderPLayer;
    public GameObject player;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        BorderPLayer = GameObject.FindGameObjectWithTag("Player");
        double minX = BorderPLayer.GetComponent<Movement>().minX - 1;
        double maxX = BorderPLayer.GetComponent<Movement>().maxX + 1;
        double minZ = BorderPLayer.GetComponent<Movement>().minZ - 1;
        double maxZ = BorderPLayer.GetComponent<Movement>().maxZ + 1;
    }

    // Update is called once per frame
    void Update()
    {
        if(player.transform.position.x < BorderPLayer.GetComponent<Movement>().minX || player.transform.position.x > BorderPLayer.GetComponent<Movement>().maxX || player.transform.position.z < BorderPLayer.GetComponent<Movement>().minZ || player.transform.position.z > BorderPLayer.GetComponent<Movement>().maxZ || player.transform.position.y < -0.1)
        {
            Debug.Log("Out of bounds");
            player.transform.position = new Vector3(0, (float)0.1, 0);
        }
    }
}
