using UnityEngine;

public class Movement : MonoBehaviour
{
    Vector3 move;
    Rigidbody rb;
    private float speed;
    Animation anim;
    public GameObject NPC;
    public float Speed
    {
        get { return speed; }
        set { speed = value; }
    }
    public float minX = -4;
    public float maxX = 5;
    public float minZ = -7;
    public float maxZ = 2;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        rb = GetComponent<Rigidbody>();
        move = new Vector3();
        Speed = 10f;
    }
    public void addSpeed()
    {
        Speed += 2f;
        Debug.Log(Speed);
    }
    public float minXAxis() { return minX; }
    public float maxXAxis() { return maxX; }
    public float minZAxis() { return minZ; }
    public float maxZAxis() { return maxZ; }

    // Update is called once per frame
    void Update()
    {
        move = new Vector3(Input.GetAxis("Horizontal"), 0,Input.GetAxis("Vertical"));
    }
    public void FixedUpdate()
    {
        rb.linearVelocity = move * Speed;
        Vector3 pos = rb.position;
        pos.x = Mathf.Clamp(pos.x, minX, maxX);
        pos.z = Mathf.Clamp(pos.z, minZ, maxZ);
        rb.MovePosition(pos);
        rb.rotation = Quaternion.Euler(0, 0, 0);
        if (rb.position.y < 0)
        {
            rb.position = new Vector3(rb.position.x, (float)0.1, rb.position.z);

        }
    }
    public void PLayAni() {
        anim = NPC.GetComponent<Animation>();
        anim.Play();
    }
}
