using UnityEngine;

public class CannonController : MonoBehaviour
{
    public Transform rotater;      // Base that turns left and right
    public Transform tilter;       // Barrel that tilts up and down
    public Transform spawnPoint;   // Point in front of the cannon
    public GameObject cannonball;  // Cannonball prefab with Rigidbody

    public float rotateSpeed = 60f;
    public float tiltSpeed = 40f;
    public float fireForce = 700f;
    public float minTilt = 10f;   // lowest angle
    public float maxTilt = 60f;   // highest angle

    void Update()
    {
        Rotate();
        Tilt();
        Fire();
    }

    void Rotate()
    {
        if (rotater == null) return;

        if (Input.GetKey(KeyCode.A))
        {
            rotater.Rotate(0, -rotateSpeed * Time.deltaTime, 0);
        }
        if (Input.GetKey(KeyCode.D))
        {
            rotater.Rotate(0, rotateSpeed * Time.deltaTime, 0);
        }
    }

    void Tilt()
    {
        if (tilter == null) return;

        // Get the current X rotation
        float currentX = tilter.eulerAngles.x;
        if (currentX > 180f) currentX -= 360f;

        // Change tilt up or down
        if (Input.GetKey(KeyCode.W))
        {
            currentX -= tiltSpeed * Time.deltaTime;
        }
        if (Input.GetKey(KeyCode.S))
        {
            currentX += tiltSpeed * Time.deltaTime;
        }

        // Clamp the X rotation
        currentX = Mathf.Clamp(currentX, minTilt, maxTilt);

        // Apply the clamped rotation
        Vector3 newRotation = new Vector3(currentX, tilter.eulerAngles.y, tilter.eulerAngles.z);
        tilter.eulerAngles = newRotation;
    }

    void Fire()
    {
        if (Input.GetKeyDown(KeyCode.Space))
        {
            if (cannonball != null && spawnPoint != null)
            {
                Vector3 spawnPos = spawnPoint.position + spawnPoint.forward * 0.5f;
                GameObject ball = Instantiate(cannonball, spawnPos, spawnPoint.rotation);
                Rigidbody rb = ball.GetComponent<Rigidbody>();

                if (rb != null)
                {
                    rb.AddForce(spawnPoint.forward * fireForce);
                    rb.useGravity = true;
                }
            }
        }
    }
}
