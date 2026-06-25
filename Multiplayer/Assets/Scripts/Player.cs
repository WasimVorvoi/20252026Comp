using UnityEngine;
using Mirror;
public class Player : NetworkBehaviour
{
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    private Rigidbody2D rb;
    private Vector2 move;
    private float speed;
    private SpriteRenderer rendy;
    [SyncVar]private Color color;
    void Start()
    {
        if(isLocalPlayer) {
            rb = GetComponent<Rigidbody2D>();
            ;
            speed = 10;
            ChangeColor();
        }
        rendy = GetComponent<SpriteRenderer>();
    }

    // Update is called once per frame
    void Update()
    {
        if (isLocalPlayer)
        {
            move = new Vector2(Input.GetAxis("Horizontal"), Input.GetAxis("Vertical"));
            if (Input.GetKey(KeyCode.Escape)) {
                Application.Quit();
            }
            if (Input.GetKeyDown(KeyCode.Space)) {
                ChangeColor();
            }
            
        }
        if (rendy.color != color) {
            rendy.color = color;
        }
    
    }
    private void FixedUpdate() {
        if (isLocalPlayer)
        {
            rb.linearVelocity = move * speed;
        }
    }
    [Command]
    public void ChangeColor() { 
        color = Random.ColorHSV(0f, 1f, 0f, 1f, 0.5f, 1f);
    }
}
