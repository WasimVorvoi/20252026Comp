using UnityEngine;

public class CannonController : MonoBehaviour
{
        public Transform rotater;
        public Transform tilter;
        public Transform spawnPoint;
        public GameObject cannonball;
        public float rotateSpeed = 60f;
        public float tiltSpeed = 40f;
        public float fireForce = 700f;
        public float minTilt = 10f;
        public float maxTilt = 60f;
        public float cannonballRotX = 0f;
        public float cannonballRotY = 0f;
        public float cannonballRotZ = 0f;
    void Update()
    {
        Rotate();
        Tilt();
        Fire();
    }
    void Rotate()
    {
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

        float currentX = tilter.eulerAngles.x;
        if (currentX > 180f) {
            currentX -= 360f; 
        }
        if (Input.GetKey(KeyCode.W))
        {
            currentX -= tiltSpeed * Time.deltaTime;
        }
        if (Input.GetKey(KeyCode.S))
        {
            currentX += tiltSpeed * Time.deltaTime;
        }

        currentX = Mathf.Clamp(currentX, minTilt, maxTilt);

        tilter.Rotate(currentX - tilter.eulerAngles.x, 0, 0);
    }
    void Fire()
    {
        if (Input.GetKeyDown(KeyCode.Space))
        {
                Vector3 spawnPos = spawnPoint.position + spawnPoint.forward * 0.5f;
                Quaternion spawnRot = spawnPoint.rotation * Quaternion.Euler(cannonballRotX, cannonballRotY, cannonballRotZ);
                GameObject ball = Instantiate(cannonball, spawnPos, spawnRot);
                Rigidbody rb = ball.GetComponent<Rigidbody>();
                rb.AddForce(spawnPoint.forward * fireForce);
                rb.useGravity = true;
        }
    }
}
