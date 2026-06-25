using UnityEngine;

public class Move : MonoBehaviour
{
    Rigidbody2D rb2d;
    Animator ani;
    float moveX;
    public int speed;
    public float jumpForce;
    public bool jump;
    public int jumpsUsed;
    public int maxJumps = 2;
    public GameObject CameraHi;
    public bool CanJump;

    void Start()
    {
        rb2d = GetComponent<Rigidbody2D>();
        ani = GetComponent<Animator>();
        speed = 5;
        moveX = 0;
        jumpForce = 5f;
        jumpsUsed = 0;
    }

    void Update()
    {
        Inputs();
        Animations();
        CameraHi.transform.position = new Vector3(transform.position.x, transform.position.y, CameraHi.transform.position.z);
    }

    public void Inputs()
    {
        moveX = Input.GetAxisRaw("Horizontal");

        if (moveX != 0)
        {
            ani.SetBool("IsRunning", true);
            ani.SetTrigger("Run");
        }
        if (moveX == 0)
        {
            ani.SetBool("IsRunning", false);
        }

        if (moveX > 0)
        {
            transform.localScale = new Vector3(-1, 1, 1);
        }
        else if (moveX < 0)
        {
            transform.localScale = new Vector3(1, 1, 1);
        }

        if ((Input.GetKeyDown(KeyCode.Space) || Input.GetKeyDown(KeyCode.W)) && jumpsUsed < maxJumps)
        {
            if (CanJump)
            {
                jump = true;
                jumpsUsed++;
            }
        }

        if (Input.GetMouseButtonDown(0))
        {
            ani.SetTrigger("Attack");
        }

        if (Input.GetMouseButtonDown(1))
        {
            ani.SetTrigger("Sit");
        }
    }

    public void Animations()
    {
        if (Mathf.Abs(rb2d.linearVelocity.x) > 0.1f)
        {
            ani.SetBool("Run", true);
        }
        else
        {
            ani.SetBool("Run", false);
        }
    }

    void FixedUpdate()
    {
        rb2d.linearVelocity = new Vector2(moveX * speed, rb2d.linearVelocity.y);

        if (jump)
        {
            if(jumpsUsed >= maxJumps)
            {
                CanJump = false;
            }
            else
            {
                CanJump = true;
            }
            rb2d.linearVelocity = new Vector2(rb2d.linearVelocity.x, jumpForce);
            jump = false;
        }
    }

    void OnCollisionEnter2D(Collision2D collision)
    {
        if (collision.gameObject.CompareTag("Ground"))
        {
            jumpsUsed = 0;
            CanJump = true;
            ani.SetBool("Gound", true);
        }
        else
        {
            ani.SetBool("Gound", false);
        }
    }
}