using Unity.VisualScripting;
using UnityEngine;
using UnityEngine.UI;

public class Move : MonoBehaviour
{
    public float moveSpeed = 5f;
    public Rigidbody2D rb;
    public Animator animator;
    public Image green;

    public GameObject objectToSpawn;
    public Vector2 spawnPosition = new Vector2(0f, 0f);

    private Vector2 input;

    float minX = -4.109186f;
    float maxX = 3.890811f;
    float minY = -4.990807f;
    float maxY = 2.509187f;
    public int health = 100;
    public int time;
    public int Timelimit;

    void Start()
    {
        rb.freezeRotation = true;
    }

    void Update()
    {
        //input.x = Input.GetAxisRaw("Horizontal");
        //input.y = Input.GetAxisRaw("Vertical");

        animator.SetBool("walk", input.magnitude > 0.1f);

        if (Input.GetMouseButtonDown(0))
        {
            animator.SetFloat("attackType", Random.Range(0, 3));
            animator.SetTrigger("attack");
        }

        if (Input.GetKeyDown(KeyCode.E))
        {
            animator.SetFloat("castType", 0f);
            animator.SetTrigger("cast");
            animator.SetTrigger("cast");
        }

        if (Input.GetKeyDown(KeyCode.H))
        {
            animator.SetTrigger("hit");
        }

        if (Input.GetKeyDown(KeyCode.K))
        {
            Die();
        }
        SpawnObject();
        Flip();
        ScalePlayer();
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

    void ScalePlayer()
    {
        float biggestScale = 1.5f;
        float smallestScale = 0.7f;

        float t = Mathf.InverseLerp(minY, maxY, transform.position.y);
        float scale = Mathf.Lerp(biggestScale, smallestScale, t);

        float direction = transform.localScale.x < 0 ? -1f : 1f;
        transform.localScale = new Vector3(scale * direction, scale, 1f);
    }

    void SpawnObject()
    {
        time++;
        if (time == Timelimit)
        {
            //Instantiate(objectToSpawn, spawnPosition, Quaternion.identity);
            time = 0;
        }

    }

    public void Die()
    {
        animator.SetFloat("deathType", Random.Range(0, 2));
        animator.SetTrigger("death");
    }

    private void OnCollisionEnter2D(Collision2D collision)
    {
        if (collision.collider.CompareTag("Enemy"))
        {
            Damage();
            green.fillAmount = health / 100f;
            if (health <= 0)
            {
                Die();
            }
        }
    }

    public void Damage()
    {
        health -= 5;
        Debug.Log("Player Health: " + health);
    }
}