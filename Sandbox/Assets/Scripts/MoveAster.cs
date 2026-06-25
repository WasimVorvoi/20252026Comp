using System.Xml;
using UnityEngine;

public class MoveAster : MonoBehaviour
{
    public GameObject aster;
    public float zSpeed = -5f;
    public bool isPaused = false;
    void Update()
    {
        if (Input.GetKeyDown(KeyCode.Escape))
        {
            isPaused = !isPaused;
        }
    }
    private void FixedUpdate()
    {
        if (!isPaused)
        {
            aster.transform.Translate(0f, 0f, zSpeed * Time.fixedDeltaTime, Space.World);
        }
    }
}
