using Unity.VisualScripting;
using UnityEngine;
using UnityEngine.UI;

public class MoveAcc : MonoBehaviour
{
    public float moveSpeed = 5f;
    public Rigidbody2D rb;

    private Vector2 input;

    float minX = -4.109186f;
    float maxX = 3.890811f;
    float minY = -4.990807f;
    float maxY = 2.509187f;
    void Start()
    {
        rb.freezeRotation = true;
    }

    void Update()
    {
        input.x = Input.GetAxisRaw("Horizontal");
        input.y = Input.GetAxisRaw("Vertical");
        Flip();
    }

    void FixedUpdate()
    {
        rb.linearVelocity = input.normalized * moveSpeed;
        float clampedX = Mathf.Clamp(rb.position.x, minX, maxX);
        float clampedY = Mathf.Clamp(rb.position.y, minY, maxY);
        rb.position = new Vector2(clampedX, clampedY);
    }

    void Flip()
    {
        if (input.x > 0.1f)
            transform.localScale = new Vector3(-Mathf.Abs(transform.localScale.x), transform.localScale.y, 1);
        else if (input.x < -0.1f)
            transform.localScale = new Vector3(Mathf.Abs(transform.localScale.x), transform.localScale.y, 1);
    }
}