using UnityEngine;

public class RotateAsteroid : MonoBehaviour
{
    public GameObject asteroid;
    public float rotationSpeed = 50f;
    public bool isPaused = false;
    void Update()
    {
        if (Input.GetKeyDown(KeyCode.Escape))
        {
            isPaused = !isPaused;
        }
        if (!isPaused)
        {
            transform.Rotate(Vector3.up, rotationSpeed * Time.deltaTime);
        }
    }
}
