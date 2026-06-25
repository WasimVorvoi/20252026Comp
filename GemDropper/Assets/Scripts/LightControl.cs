using UnityEngine;

public class LightControl : MonoBehaviour
{
    public float rotateSpeed = 10f;

    void Update()
    {
        transform.Rotate(Vector3.up * rotateSpeed * Time.deltaTime);
    }
}
